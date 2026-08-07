package plugin.siren.Utils;

import com.hypixel.hytale.assetstore.map.AssetMapWithIndexes;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.SoundCategory;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import plugin.siren.Marriage;
import plugin.siren.Systems.MarriagePresenceSystem;
import plugin.siren.Utils.Registry.MarriageManager;
import plugin.siren.Utils.Registry.MarriageRecord;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Little things couples do at each other: a hug, a kiss, a dance.
 *
 * <p>Mechanically nothing — no buff, no bond XP. That is on purpose: the moment
 * an emote pays out, people spam it at each other for the reward instead of doing
 * it because they meant it, and it needs an anti-farm gate rather than a
 * cooldown. This is roleplay, and the cooldown here only exists to keep chat
 * readable.</p>
 *
 * <h2>What each one does</h2>
 *
 * <p>A particle burst at both partners, a positional sound, and a line to
 * everyone standing close enough to have seen it. Particle ids and sound events
 * are looked up against the running asset set and silently skipped when absent,
 * so a server whose assets differ loses the sparkle rather than the emote.</p>
 */
public final class EmoteManager {

    /**
     * The three emotes. Particle SystemIds are verified against the vanilla set
     * ({@code Hearts} and {@code Hearts_Subtle} live in NPC/Emotions,
     * {@code Dust_Sparkles} in Weather); sound events are real assets under
     * Server/Audio/SoundEvents. All are config-overridable.
     */
    public enum Emote {
        HUG("hug"),
        KISS("kiss"),
        DANCE("dance");

        private final String id;

        Emote(String id){
            this.id = id;
        }

        @Nonnull
        public String getId(){
            return this.id;
        }

        @Nonnull
        public String getNameKey(){
            return "server.marriage.emote." + this.id + ".name";
        }

        /** Message shown to the pair and to onlookers. */
        @Nonnull
        public String getBroadcastKey(){
            return "server.marriage.emote." + this.id + ".broadcast";
        }

        @Nonnull
        public String getParticleId(){
            return Marriage.getConfig().get().getEmoteParticle(this);
        }

        @Nonnull
        public String getSoundEventId(){
            return Marriage.getConfig().get().getEmoteSound(this);
        }
    }

    /** player -> epoch millis their next emote is allowed. Transient by design. */
    private static final Map<UUID, Long> COOLDOWN = new ConcurrentHashMap<>();

    private EmoteManager(){}

    /**
     * Performs an emote at the player's partner. Must be called on the player's
     * own world thread with their own store/ref. Sends all feedback itself.
     */
    public static void perform(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                               @Nonnull PlayerRef playerRef, @Nonnull Emote emote){
        MarriageConfig config = Marriage.getConfig().get();
        if(!config.ifEmotesEnabled()){
            playerRef.sendMessage(Message.translation("server.marriage.emote.disabled"));
            return;
        }

        UUID uuid = playerRef.getUuid();
        MarriageRecord record = MarriageManager.getRecord(uuid);
        if(record == null){
            playerRef.sendMessage(Message.translation("server.marriage.emote.unmarried"));
            return;
        }

        long remaining = cooldownRemaining(uuid);
        if(remaining > 0L){
            playerRef.sendMessage(Message.translation("server.marriage.emote.cooldown")
                    .param("seconds", String.valueOf(remaining)));
            return;
        }

        UUID partnerUuid = record.getPartnerUuid(uuid);
        PlayerRef partner = partnerUuid == null ? null : Universe.get().getPlayer(partnerUuid);
        if(partner == null || !partner.isValid()){
            playerRef.sendMessage(Message.translation("server.marriage.emote.partnerOffline")
                    .param("partnerUsername", MarriageUtil.displayName(record.getPartnerName(uuid))));
            return;
        }

        Ref<EntityStore> partnerRef = partner.getReference();
        // Same store and in range - you cannot hug someone in another world, and
        // their components could not be read from here anyway.
        if(partnerRef == null || !partnerRef.isValid() || partnerRef.getStore() != store
                || !MarriagePresenceSystem.isWithinBondRange(store, ref, partnerRef, config.getEmoteRangeBlocks())){
            playerRef.sendMessage(Message.translation("server.marriage.emote.tooFar")
                    .param("partnerUsername", MarriageUtil.displayName(record.getPartnerName(uuid))));
            return;
        }

        TransformComponent selfTransform = store.getComponent(ref, TransformComponent.getComponentType());
        TransformComponent partnerTransform = store.getComponent(partnerRef, TransformComponent.getComponentType());
        if(selfTransform == null || partnerTransform == null){
            return;
        }

        markUsed(uuid);

        spawnAndSound(store, ref, emote, selfTransform.getPosition());
        spawnAndSound(store, partnerRef, emote, partnerTransform.getPosition());

        Message line = Message.translation(emote.getBroadcastKey())
                .param("username", playerRef.getUsername())
                .param("partnerUsername", partner.getUsername())
                .color(Color.PINK);

        playerRef.sendMessage(line);
        partner.sendMessage(line);
        tellOnlookers(store, playerRef, partner, selfTransform.getPosition(), line);
    }

    /**
     * A particle burst and a one-shot sound at a position.
     *
     * <p>Both are skipped silently when the running asset set has no such id -
     * an emote should never be the thing that throws on a server whose assets
     * differ from the one this was written against.</p>
     */
    private static void spawnAndSound(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> at,
                                      @Nonnull Emote emote, @Nonnull Vector3dc position){
        String particleId = emote.getParticleId();
        if(!particleId.isBlank()){
            // Chest height rather than the transform, which sits at the feet - a
            // burst of hearts coming out of the floor reads as a bug.
            ParticleUtil.spawnParticleEffect(particleId,
                    new Vector3d(position.x(), position.y() + 1.0, position.z()), store);
        }

        String soundId = emote.getSoundEventId();
        if(soundId.isBlank()){
            return;
        }
        int soundIndex = SoundEvent.getAssetMap().getIndex(soundId);
        if(soundIndex == AssetMapWithIndexes.NOT_FOUND){
            return;
        }
        SoundUtil.playSoundEvent3d(soundIndex, SoundCategory.SFX,
                position.x(), position.y(), position.z(), 1f, 1f, store);
    }

    /**
     * Tells everyone close enough to have seen it - and nobody else. An emote is
     * a thing you do in a place, not an announcement.
     */
    private static void tellOnlookers(@Nonnull Store<EntityStore> store, @Nonnull PlayerRef self,
                                      @Nonnull PlayerRef partner, @Nonnull Vector3dc at, @Nonnull Message line){
        double radius = Marriage.getConfig().get().getEmoteBroadcastRadiusBlocks();
        if(radius <= 0.0){
            return;
        }

        for(PlayerRef onlooker : Universe.get().getPlayers()){
            if(onlooker == null || !onlooker.isValid()
                    || onlooker.getUuid().equals(self.getUuid())
                    || onlooker.getUuid().equals(partner.getUuid())){
                continue;
            }
            Ref<EntityStore> onlookerRef = onlooker.getReference();
            if(onlookerRef == null || !onlookerRef.isValid() || onlookerRef.getStore() != store){
                continue;
            }
            TransformComponent transform = store.getComponent(onlookerRef, TransformComponent.getComponentType());
            if(transform != null && transform.getPosition().distanceSquared(at) <= radius * radius){
                onlooker.sendMessage(line);
            }
        }
    }

    private static long cooldownRemaining(@Nonnull UUID player){
        Long until = COOLDOWN.get(player);
        if(until == null){
            return 0L;
        }
        long remaining = until - System.currentTimeMillis();
        if(remaining <= 0L){
            COOLDOWN.remove(player);
            return 0L;
        }
        return (remaining + 999L) / 1000L;
    }

    private static void markUsed(@Nonnull UUID player){
        COOLDOWN.put(player, System.currentTimeMillis()
                + Marriage.getConfig().get().getEmoteCooldownSeconds() * 1000L);
    }

    /** Drops a disconnected player's cooldown. */
    public static void forget(@Nonnull UUID player){
        COOLDOWN.remove(player);
    }
}
