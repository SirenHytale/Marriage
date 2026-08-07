package plugin.siren.API;

import plugin.siren.Marriage;
import plugin.siren.Utils.Bond.BondXpReason;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Listener bus for other mods. Register once from your plugin's setup():
 * <pre>MarriageEvents.registerListener(new MarriageEvents.MarriageListener(){ ... });</pre>
 * Pre-events are cancellable; post-events are informational snapshots.
 * Listeners run synchronously on whichever world thread triggered the action —
 * keep them fast and never touch another world's components from them.
 */
public final class MarriageEvents {

    private static final List<MarriageListener> LISTENERS = new CopyOnWriteArrayList<>();

    private MarriageEvents(){}

    public interface MarriageListener {
        /** Fired before two players marry; cancel() blocks the marriage. */
        default void onPreMarry(@Nonnull PreMarryEvent event){}

        /** Fired after a marriage is created and persisted. */
        default void onMarry(@Nonnull MarriageInfo marriage){}

        /** Fired before a confirmed divorce completes; cancel() blocks it. */
        default void onPreDivorce(@Nonnull PreDivorceEvent event){}

        /** Fired after a divorce, with a snapshot of the ended marriage. */
        default void onDivorce(@Nonnull MarriageInfo endedMarriage){}

        /** Fired before a gift transfers; cancel() blocks it (item stays with the giver). */
        default void onPreGift(@Nonnull PreGiftEvent event){}

        /** Fired after a gift lands with the partner. */
        default void onGift(@Nonnull GiftEvent event){}

        /** Fired when a couple's PvP protection setting changes. */
        default void onPvpProtectionToggle(@Nonnull MarriageInfo marriage){}

        /** Fired when a couple's bond level changes, up or down. */
        default void onBondLevelChange(@Nonnull BondLevelChangeEvent event){}

        /** Fired when a couple reaches an anniversary milestone. */
        default void onAnniversary(@Nonnull AnniversaryEvent event){}

        /** Fired when a couple sets or moves their shared home. */
        default void onHomeSet(@Nonnull HomeSetEvent event){}
    }

    public static void registerListener(@Nonnull MarriageListener listener){
        LISTENERS.add(listener);
    }

    public static void unregisterListener(@Nonnull MarriageListener listener){
        LISTENERS.remove(listener);
    }

    public static class Cancellable {
        private boolean cancelled;

        public boolean isCancelled(){
            return this.cancelled;
        }

        public void cancel(){
            this.cancelled = true;
        }
    }

    public static final class PreMarryEvent extends Cancellable {
        private final UUID player1;
        private final String player1Name;
        private final UUID player2;
        private final String player2Name;

        public PreMarryEvent(UUID player1, String player1Name, UUID player2, String player2Name){
            this.player1 = player1;
            this.player1Name = player1Name;
            this.player2 = player2;
            this.player2Name = player2Name;
        }

        public UUID getPlayer1(){
            return this.player1;
        }

        public String getPlayer1Name(){
            return this.player1Name;
        }

        public UUID getPlayer2(){
            return this.player2;
        }

        public String getPlayer2Name(){
            return this.player2Name;
        }
    }

    public static final class PreDivorceEvent extends Cancellable {
        private final MarriageInfo marriage;
        private final UUID initiator;

        public PreDivorceEvent(MarriageInfo marriage, UUID initiator){
            this.marriage = marriage;
            this.initiator = initiator;
        }

        public MarriageInfo getMarriage(){
            return this.marriage;
        }

        public UUID getInitiator(){
            return this.initiator;
        }
    }

    public static final class PreGiftEvent extends Cancellable {
        private final MarriageInfo marriage;
        private final UUID giver;
        private final String itemId;
        private final int quantity;

        public PreGiftEvent(MarriageInfo marriage, UUID giver, String itemId, int quantity){
            this.marriage = marriage;
            this.giver = giver;
            this.itemId = itemId;
            this.quantity = quantity;
        }

        public MarriageInfo getMarriage(){
            return this.marriage;
        }

        public UUID getGiver(){
            return this.giver;
        }

        public String getItemId(){
            return this.itemId;
        }

        public int getQuantity(){
            return this.quantity;
        }
    }

    public static final class GiftEvent {
        private final MarriageInfo marriage;
        private final UUID giver;
        private final String itemId;
        private final int quantity;

        public GiftEvent(MarriageInfo marriage, UUID giver, String itemId, int quantity){
            this.marriage = marriage;
            this.giver = giver;
            this.itemId = itemId;
            this.quantity = quantity;
        }

        public MarriageInfo getMarriage(){
            return this.marriage;
        }

        public UUID getGiver(){
            return this.giver;
        }

        public String getItemId(){
            return this.itemId;
        }

        public int getQuantity(){
            return this.quantity;
        }
    }

    public static boolean firePreMarry(@Nonnull PreMarryEvent event){
        for(MarriageListener listener : LISTENERS){
            try{
                listener.onPreMarry(event);
            }catch(Exception e){
                Marriage.LOGGER.atSevere().log("MarriageListener threw in onPreMarry: " + e);
            }
        }
        return !event.isCancelled();
    }

    public static void fireMarry(@Nonnull MarriageInfo marriage){
        for(MarriageListener listener : LISTENERS){
            try{
                listener.onMarry(marriage);
            }catch(Exception e){
                Marriage.LOGGER.atSevere().log("MarriageListener threw in onMarry: " + e);
            }
        }
    }

    public static boolean firePreDivorce(@Nonnull PreDivorceEvent event){
        for(MarriageListener listener : LISTENERS){
            try{
                listener.onPreDivorce(event);
            }catch(Exception e){
                Marriage.LOGGER.atSevere().log("MarriageListener threw in onPreDivorce: " + e);
            }
        }
        return !event.isCancelled();
    }

    public static void fireDivorce(@Nonnull MarriageInfo endedMarriage){
        for(MarriageListener listener : LISTENERS){
            try{
                listener.onDivorce(endedMarriage);
            }catch(Exception e){
                Marriage.LOGGER.atSevere().log("MarriageListener threw in onDivorce: " + e);
            }
        }
    }

    public static boolean firePreGift(@Nonnull PreGiftEvent event){
        for(MarriageListener listener : LISTENERS){
            try{
                listener.onPreGift(event);
            }catch(Exception e){
                Marriage.LOGGER.atSevere().log("MarriageListener threw in onPreGift: " + e);
            }
        }
        return !event.isCancelled();
    }

    public static void fireGift(@Nonnull GiftEvent event){
        for(MarriageListener listener : LISTENERS){
            try{
                listener.onGift(event);
            }catch(Exception e){
                Marriage.LOGGER.atSevere().log("MarriageListener threw in onGift: " + e);
            }
        }
    }

    public static void firePvpProtectionToggle(@Nonnull MarriageInfo marriage){
        for(MarriageListener listener : LISTENERS){
            try{
                listener.onPvpProtectionToggle(marriage);
            }catch(Exception e){
                Marriage.LOGGER.atSevere().log("MarriageListener threw in onPvpProtectionToggle: " + e);
            }
        }
    }

    public static void fireBondLevelChange(@Nonnull BondLevelChangeEvent event){
        for(MarriageListener listener : LISTENERS){
            try{
                listener.onBondLevelChange(event);
            }catch(Exception e){
                Marriage.LOGGER.atSevere().log("MarriageListener threw in onBondLevelChange: " + e);
            }
        }
    }

    public static void fireAnniversary(@Nonnull AnniversaryEvent event){
        for(MarriageListener listener : LISTENERS){
            try{
                listener.onAnniversary(event);
            }catch(Exception e){
                Marriage.LOGGER.atSevere().log("MarriageListener threw in onAnniversary: " + e);
            }
        }
    }

    public static void fireHomeSet(@Nonnull HomeSetEvent event){
        for(MarriageListener listener : LISTENERS){
            try{
                listener.onHomeSet(event);
            }catch(Exception e){
                Marriage.LOGGER.atSevere().log("MarriageListener threw in onHomeSet: " + e);
            }
        }
    }

    // === 2.1.0 event payloads ===

    /**
     * A couple's bond level moved. Fired for drops as well as gains, so
     * {@code newLevel < oldLevel} means decay (or an admin) took it down.
     */
    public static final class BondLevelChangeEvent {
        private final MarriageInfo marriage;
        private final int oldLevel;
        private final int newLevel;
        private final BondXpReason reason;

        public BondLevelChangeEvent(MarriageInfo marriage, int oldLevel, int newLevel, BondXpReason reason){
            this.marriage = marriage;
            this.oldLevel = oldLevel;
            this.newLevel = newLevel;
            this.reason = reason;
        }

        @Nonnull
        public MarriageInfo getMarriage(){
            return this.marriage;
        }

        public int getOldLevel(){
            return this.oldLevel;
        }

        public int getNewLevel(){
            return this.newLevel;
        }

        public boolean isDeepened(){
            return this.newLevel > this.oldLevel;
        }

        /** What earned (or cost) the XP that crossed the level. */
        @Nonnull
        public BondXpReason getReason(){
            return this.reason;
        }
    }

    /** A couple reached an anniversary milestone. {@code days} is 1, 7, 30 or 365. */
    public static final class AnniversaryEvent {
        private final MarriageInfo marriage;
        private final int days;

        public AnniversaryEvent(MarriageInfo marriage, int days){
            this.marriage = marriage;
            this.days = days;
        }

        @Nonnull
        public MarriageInfo getMarriage(){
            return this.marriage;
        }

        public int getDays(){
            return this.days;
        }
    }

    /** A couple set (or moved) their shared home. */
    public static final class HomeSetEvent {
        private final MarriageInfo marriage;
        private final UUID setBy;
        private final String world;
        private final double x;
        private final double y;
        private final double z;

        public HomeSetEvent(MarriageInfo marriage, UUID setBy, String world, double x, double y, double z){
            this.marriage = marriage;
            this.setBy = setBy;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Nonnull
        public MarriageInfo getMarriage(){
            return this.marriage;
        }

        /** Which half of the couple moved it. */
        @Nonnull
        public UUID getSetBy(){
            return this.setBy;
        }

        @Nonnull
        public String getWorld(){
            return this.world;
        }

        public double getX(){
            return this.x;
        }

        public double getY(){
            return this.y;
        }

        public double getZ(){
            return this.z;
        }
    }
}
