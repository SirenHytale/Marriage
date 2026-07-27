package plugin.siren.API.CustomUI;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Everything a category or option needs to know about the player whose menu is
 * being built, handed to every registry callback.
 *
 * <p>Modelled on Mermaids' {@code CreatureUIContext}. Held only for the duration of
 * one build or one click - never cache it, and in particular never keep the
 * {@link Ref} or {@link Store} past the call, as both belong to the world thread
 * that is currently running.</p>
 */
public final class MarriageUIContext {
    private final PlayerRef playerRef;
    private final Ref<EntityStore> ref;
    private final Store<EntityStore> store;

    public MarriageUIContext(
            @Nonnull PlayerRef playerRef,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store
    ) {
        this.playerRef = playerRef;
        this.ref = ref;
        this.store = store;
    }

    @Nonnull
    public PlayerRef getPlayerRef() {
        return this.playerRef;
    }

    @Nonnull
    public Ref<EntityStore> getRef() {
        return this.ref;
    }

    @Nonnull
    public Store<EntityStore> getStore() {
        return this.store;
    }

    @Nonnull
    public UUID getUuid() {
        return this.playerRef.getUuid();
    }

    /**
     * Any component on the player, or null if they do not have it. Null is a
     * normal answer, not an error - callers must null-check rather than assume.
     */
    @Nullable
    public <T extends Component<EntityStore>> T getComponent(@Nonnull ComponentType<EntityStore, T> componentType) {
        return this.store.getComponent(this.ref, componentType);
    }

    public boolean hasPermission(@Nonnull String permission) {
        return this.playerRef.hasPermission(permission);
    }
}
