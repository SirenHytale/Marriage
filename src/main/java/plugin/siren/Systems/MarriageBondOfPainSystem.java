package plugin.siren.Systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Utils.Bond.BondManager;
import plugin.siren.Utils.Bond.BondPerk;
import plugin.siren.Utils.MarriageConfig;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The bond of pain: a share of the damage aimed at one partner lands on the other
 * instead.
 *
 * <p>Runs in the damage filter phase <b>after</b> {@link MarriagePvPSystem}, so a
 * blow that partner PvP protection already cancelled is never split — the
 * inherited {@code shouldProcessEvent} gate skips cancelled damage, and the
 * explicit dependency is what guarantees the ordering rather than hoping for it.</p>
 *
 * <h2>The re-entrancy guard</h2>
 *
 * <p>Moving damage onto the partner means calling
 * {@link DamageSystems#executeDamage} while already inside a damage event, and
 * that transferred blow runs this same system again with the partners swapped —
 * which would bounce the remainder back and forth until the stack ran out. The
 * partners are guaranteed to be on one world thread here (the same-store gate
 * below), so a {@link ThreadLocal} latch is a complete fix: while a transfer is in
 * flight, this system declines to transfer anything.</p>
 *
 * <h2>Why it is a transfer and not a second hit</h2>
 *
 * <p>The victim's damage is reduced by exactly what the partner is dealt, so a
 * couple with the perk take the same total as a couple without it. It moves harm
 * around; it does not create or destroy any.</p>
 */
public class MarriageBondOfPainSystem extends DamageEventSystem {

    private static final Query<EntityStore> QUERY = Player.getComponentType();

    private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(
            new SystemDependency<>(Order.AFTER, MarriagePvPSystem.class)
    );

    /** True while this thread is in the middle of handing damage to a partner. */
    private static final ThreadLocal<Boolean> TRANSFERRING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /** Partner UUID -> epoch millis the "you felt that" notice may fire again. */
    private static final Map<UUID, Long> NOTICE_COOLDOWN = new ConcurrentHashMap<>();
    private static final long NOTICE_COOLDOWN_MILLIS = 4000L;

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup(){
        return DamageModule.get().getFilterDamageGroup();
    }

    @Nonnull
    @Override
    public Set<Dependency<EntityStore>> getDependencies(){
        return DEPENDENCIES;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery(){
        return QUERY;
    }

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Damage damage){
        if(damage.isCancelled() || Boolean.TRUE.equals(TRANSFERRING.get())){
            return;
        }

        MarriageConfig config = Marriage.getConfig().get();
        if(!config.ifBondOfPainEnabled()){
            return;
        }

        float amount = damage.getAmount();
        if(amount <= 0f){
            return;
        }

        PlayerRef victim = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if(victim == null || !victim.isValid()){
            return;
        }

        UUID victimUuid = victim.getUuid();
        MarriageRecord record = MarriageManager.getRecord(victimUuid);
        if(!BondManager.hasPerk(record, BondPerk.BOND_OF_PAIN) || record == null){
            return;
        }

        UUID partnerUuid = record.getPartnerUuid(victimUuid);
        if(partnerUuid == null){
            return;
        }

        // A couple who turned partner PvP protection off want to actually fight each
        // other. Sharing the damage back onto the attacking partner would make every
        // blow hurt them too, which is not sparring - it is the perk quietly undoing
        // the setting they deliberately changed.
        if(damage.getSource() instanceof Damage.EntitySource entitySource){
            Ref<EntityStore> attackerRef = entitySource.getRef();
            if(attackerRef != null && attackerRef.isValid()){
                PlayerRef attacker = commandBuffer.getComponent(attackerRef, PlayerRef.getComponentType());
                if(attacker != null && partnerUuid.equals(attacker.getUuid())){
                    return;
                }
            }
        }

        PlayerRef partner = Universe.get().getPlayer(partnerUuid);
        if(partner == null || !partner.isValid()){
            return;
        }
        Ref<EntityStore> partnerRef = partner.getReference();
        if(partnerRef == null || !partnerRef.isValid()){
            return;
        }

        Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(index);
        if(victimRef == null || !victimRef.isValid()){
            return;
        }
        // Same store AND in range - the same gate every other cross-partner read in
        // this mod uses, and here it is also what makes the ThreadLocal latch sound.
        if(!MarriagePresenceSystem.isWithinBondRange(store, victimRef, partnerRef, config.getBondOfPainRangeBlocks())){
            return;
        }

        // Bond-scaled: the Blade vow moves more of it (see BondManager.painShareFor).
        float share = (float) (amount * BondManager.painShareFor(record));
        if(share <= 0f){
            return;
        }

        damage.setAmount(amount - share);

        TRANSFERRING.set(Boolean.TRUE);
        try{
            // The original source is carried over rather than blamed on the victim:
            // whatever killed you is what hurt your partner, which is what any death
            // message, damage-cause bonus or third-party mod reading it should see.
            DamageSystems.executeDamage(partnerRef, commandBuffer,
                    new Damage(damage.getSource(), damage.getDamageCauseIndex(), share));
        }finally{
            TRANSFERRING.set(Boolean.FALSE);
        }

        long now = System.currentTimeMillis();
        Long nextNotice = NOTICE_COOLDOWN.get(partnerUuid);
        if(nextNotice == null || nextNotice <= now){
            NOTICE_COOLDOWN.put(partnerUuid, now + NOTICE_COOLDOWN_MILLIS);
            partner.sendMessage(Message.translation("server.marriage.perk.bondOfPain")
                    .param("partnerUsername", victim.getUsername())
                    .color(Color.PINK));
        }
    }

    /** Drops a disconnected player's notice cooldown. */
    public static void forget(@Nonnull UUID player){
        NOTICE_COOLDOWN.remove(player);
    }
}
