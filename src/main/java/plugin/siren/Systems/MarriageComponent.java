package plugin.siren.Systems;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;

import javax.annotation.Nullable;

/**
 * Transient per-player runtime state. Since 2.0.0, marriage requests, the divorce
 * confirmation window, and cooldowns live in MarriageManager's in-memory maps
 * (UUID-keyed, no cross-player component access) — this component only carries
 * per-session flags.
 */
public class MarriageComponent implements Component<EntityStore> {

    private boolean updateChecker;

    /**
     * When this player last ran the couple's bond bookkeeping. Purely a throttle so
     * {@code MarriagePresenceSystem}'s two-second tick only reaches the registry
     * once a minute — the authoritative clock is on the marriage record, which is
     * what makes the throttle safe to lose on a restart.
     */
    private long lastBondTickMillis;

    public static ComponentType<EntityStore, MarriageComponent> getComponentType(){
        return Marriage.get().getMarriageComponentType();
    }

    public MarriageComponent(){
        this.updateChecker = false;
    }

    public MarriageComponent(MarriageComponent other){
        this.updateChecker = other.updateChecker;
        this.lastBondTickMillis = other.lastBondTickMillis;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new MarriageComponent(this);
    }

    public boolean getUpdateCheckerCheck(){
        return this.updateChecker;
    }

    public void setCheckOnUpdateChecker(boolean checked){
        this.updateChecker = checked;
    }

    public long getLastBondTickMillis(){
        return this.lastBondTickMillis;
    }

    public void setLastBondTickMillis(long millis){
        this.lastBondTickMillis = millis;
    }
}
