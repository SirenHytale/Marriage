package plugin.siren.Systems;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MarriageComponent implements Component<EntityStore> {

    private List<PlayerRef> requestsList;
    private int divorceTimer;

    private boolean updateChecker;

    public static ComponentType<EntityStore, MarriageComponent> getComponentType(){
        return Marriage.get().getMarriageComponentType();
    }

    public MarriageComponent(){
        this.requestsList = new ArrayList<>();
        this.divorceTimer = 0;

        this.updateChecker = false;
    }

    public MarriageComponent(MarriageComponent other){
        this.requestsList = other.requestsList;
        this.divorceTimer = other.divorceTimer;

        this.updateChecker = other.updateChecker;
    }

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        return new MarriageComponent(this);
    }

    public List<PlayerRef> getRequestsList(){
        return this.requestsList;
    }

    public void addRequestToList(PlayerRef playerRef){
        this.requestsList.add(playerRef);
    }

    public void clearRequestsList(){
        this.requestsList = new ArrayList<>();
    }

    public int getDivorceTimer(){
        return divorceTimer;
    }

    public void setDivorceTimer(int time){
        divorceTimer = time;
    }

    public boolean getUpdateCheckerCheck(){
        return this.updateChecker;
    }

    public void setCheckOnUpdateChecker(boolean checked){
        this.updateChecker = checked;
    }
}
