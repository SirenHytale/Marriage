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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Utils.Registry.MarriageManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cancels damage a player deals to their own married partner while the couple's
 * PvP protection toggle (and the master config switch) is on. Runs in the damage
 * filter phase after vanilla's PvP filter; the default shouldProcessEvent gate
 * already skips damage something else cancelled, which is exactly right here.
 * Projectiles are covered for free - Damage.ProjectileSource extends EntitySource
 * and its getRef() is the shooter, not the arrow.
 */
public class MarriagePvPSystem extends DamageEventSystem {

    private static final Query<EntityStore> QUERY = Player.getComponentType();

    private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(
            new SystemDependency<>(Order.AFTER, DamageSystems.PlayerDamageFilterSystem.class)
    );

    /** Attacker UUID -> epoch millis the "protected by your vows" notice may fire again. */
    private static final Map<UUID, Long> NOTICE_COOLDOWN = new ConcurrentHashMap<>();
    private static final long NOTICE_COOLDOWN_MILLIS = 3000L;

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
        if(damage.isCancelled()){
            return;
        }
        if(!(damage.getSource() instanceof Damage.EntitySource entitySource)){
            return;
        }
        Ref<EntityStore> attackerRef = entitySource.getRef();
        if(attackerRef == null || !attackerRef.isValid()){
            return;
        }
        PlayerRef attacker = commandBuffer.getComponent(attackerRef, PlayerRef.getComponentType());
        if(attacker == null){
            return;
        }
        PlayerRef victim = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if(victim == null){
            return;
        }
        if(!MarriageManager.isPvpProtected(attacker.getUuid(), victim.getUuid())){
            return;
        }

        damage.setCancelled(true);

        if(Marriage.getConfig().get().ifPvpProtectionMessage()){
            long now = System.currentTimeMillis();
            Long nextNotice = NOTICE_COOLDOWN.get(attacker.getUuid());
            if(nextNotice == null || nextNotice <= now){
                NOTICE_COOLDOWN.put(attacker.getUuid(), now + NOTICE_COOLDOWN_MILLIS);
                attacker.sendMessage(Message.translation("server.marriage.pvp.protected").color(Color.PINK));
            }
        }
    }
}
