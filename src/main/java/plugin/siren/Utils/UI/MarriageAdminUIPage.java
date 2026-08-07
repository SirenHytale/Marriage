package plugin.siren.Utils.UI;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import plugin.siren.Marriage;
import plugin.siren.Utils.LenientDoubleCodec;
import plugin.siren.Utils.MarriageConfig;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * Edits the server's Marriage config from in game, without touching the JSON.
 *
 * <p>Reached from the /marriage menu's Admin category and gated on
 * {@code marriage.admin}, which the category also checks before it lists the
 * sidebar entry at all - so a player without it never sees the entrance, and
 * re-checking here means a revoked permission takes effect on the next click
 * rather than the next restart.</p>
 *
 * <p><b>Nothing is applied as it is typed.</b> Edits accumulate in
 * {@link #pendingNumbers} / {@link #pendingToggles} and only reach the live config
 * and the disk when Save is pressed - a half-typed number is a real intermediate
 * state of a NumberField, and applying "1" on the way to "150" would briefly
 * reconfigure a running server.</p>
 *
 * <p>Adapted from Mermaids' {@code MermaidsAdminUIPage} (itself modelled on
 * Cultivation's admin config tab), including the piece that is not obvious -
 * number fields decode through {@link LenientDoubleCodec}, because a cleared
 * NumberField sends a quoted empty string that the plain double codec cannot
 * parse, and that failure discards every other field in the same submission.</p>
 */
public class MarriageAdminUIPage extends InteractiveCustomUIPage<MarriageAdminUIPage.AdminEventData> {
    /** Also checked by the Admin category before it lists itself. */
    public static final String ADMIN_PERMISSION = "marriage.admin";

    private static final String BASE = "Pages/SirensMarriage/";
    private static final String PAGE = BASE + "MarriageAdminUIPage.ui";
    private static final String SECTION_ENTRY = BASE + "Parts/CategoryEntry.ui";
    private static final String NUMBER_ROW = BASE + "Parts/ConfigNumberRow.ui";
    private static final String TOGGLE_ROW = BASE + "Parts/CheckRow.ui";

    private static final String LANG = "server.customUI.marriage.adminui.";

    private final Map<String, Double> pendingNumbers = new HashMap<>();
    private final Map<String, Boolean> pendingToggles = new HashMap<>();

    private String selectedSectionId;
    private String lastStatus;

    public MarriageAdminUIPage(@Nonnull PlayerRef playerRef) {
        this(playerRef, null);
    }

    public MarriageAdminUIPage(@Nonnull PlayerRef playerRef, String sectionId) {
        super(playerRef, CustomPageLifetime.CanDismiss, AdminEventData.CODEC);
        this.selectedSectionId = sectionId;
    }

    // --- The settings on offer, mirroring MarriageConfig's own section comments --

    @Nonnull
    private static List<Section> sections() {
        MarriageConfig config = Marriage.getConfig().get();
        List<Section> sections = new ArrayList<>();

        sections.add(new Section("general", LANG + "section.general", LANG + "section.general.hint", List.of(
                Field.toggle("requireRing", LANG + "field.requireRing", "Require-Ring-To-Marry",
                        config::ifRequireRing, config::setRequireRing),
                Field.toggle("consumeRing", LANG + "field.consumeRing", "Consume-Ring-On-Marry",
                        config::ifConsumeRing, config::setConsumeRing),
                Field.toggle("announceMarriages", LANG + "field.announceMarriages", "Announce-Marriages",
                        config::ifAnnounceMarriages, config::setAnnounceMarriages),
                Field.toggle("announceDivorces", LANG + "field.announceDivorces", "Announce-Divorces",
                        config::ifAnnounceDivorces, config::setAnnounceDivorces))));

        sections.add(new Section("requests", LANG + "section.requests", LANG + "section.requests.hint", List.of(
                Field.number("requestExpirySeconds", LANG + "field.requestExpirySeconds", "Request-Expiry-Seconds",
                        config::getRequestExpirySeconds, value -> config.setRequestExpirySeconds((int) value)),
                Field.number("divorceConfirmSeconds", LANG + "field.divorceConfirmSeconds", "Divorce-Confirm-Seconds",
                        config::getDivorceConfirmSeconds, value -> config.setDivorceConfirmSeconds((int) value)))));

        sections.add(new Section("menu", LANG + "section.menu", LANG + "section.menu.hint", List.of(
                Field.toggle("menuEnabled", LANG + "field.menuEnabled", "Enable-Menu",
                        config::ifMenuEnabled, config::setMenuEnabled))));

        sections.add(new Section("pvp", LANG + "section.pvp", LANG + "section.pvp.hint", List.of(
                Field.toggle("partnerPvpProtection", LANG + "field.partnerPvpProtection", "Enable-Partner-Pvp-Protection",
                        config::ifPartnerPvpProtection, config::setPartnerPvpProtection),
                Field.toggle("defaultPvpProtection", LANG + "field.defaultPvpProtection", "Default-Pvp-Protection",
                        config::ifDefaultPvpProtection, config::setDefaultPvpProtection),
                Field.toggle("pvpProtectionMessage", LANG + "field.pvpProtectionMessage", "Pvp-Protection-Message",
                        config::ifPvpProtectionMessage, config::setPvpProtectionMessage))));

        sections.add(new Section("regen", LANG + "section.regen", LANG + "section.regen.hint", List.of(
                Field.toggle("partnerRegen", LANG + "field.partnerRegen", "Enable-Partner-Regen",
                        config::ifPartnerRegen, config::setPartnerRegen),
                Field.number("regenRadiusBlocks", LANG + "field.regenRadiusBlocks", "Regen-Radius-Blocks",
                        config::getRegenRadiusBlocks, config::setRegenRadiusBlocks),
                Field.number("regenIntervalSeconds", LANG + "field.regenIntervalSeconds", "Regen-Interval-Seconds",
                        config::getRegenIntervalSeconds, config::setRegenIntervalSeconds),
                Field.number("regenAmountPerInterval", LANG + "field.regenAmountPerInterval", "Regen-Amount-Per-Interval",
                        config::getRegenAmountPerInterval, config::setRegenAmountPerInterval))));

        sections.add(new Section("gifts", LANG + "section.gifts", LANG + "section.gifts.hint", List.of(
                Field.toggle("giftEnabled", LANG + "field.giftEnabled", "Enable-Gift",
                        config::ifGiftEnabled, config::setGiftEnabled),
                Field.number("giftRangeBlocks", LANG + "field.giftRangeBlocks", "Gift-Range-Blocks",
                        config::getGiftRangeBlocks, config::setGiftRangeBlocks),
                Field.number("giftCooldownSeconds", LANG + "field.giftCooldownSeconds", "Gift-Cooldown-Seconds",
                        config::getGiftCooldownSeconds, value -> config.setGiftCooldownSeconds((int) value)))));

        sections.add(new Section("tpMsg", LANG + "section.tpMsg", LANG + "section.tpMsg.hint", List.of(
                Field.toggle("tpEnabled", LANG + "field.tpEnabled", "Enable-Partner-Tp",
                        config::ifTpEnabled, config::setTpEnabled),
                Field.number("tpCooldownSeconds", LANG + "field.tpCooldownSeconds", "Tp-Cooldown-Seconds",
                        config::getTpCooldownSeconds, value -> config.setTpCooldownSeconds((int) value)),
                Field.toggle("msgEnabled", LANG + "field.msgEnabled", "Enable-Partner-Msg",
                        config::ifMsgEnabled, config::setMsgEnabled))));

        sections.add(new Section("bond", LANG + "section.bond", LANG + "section.bond.hint", List.of(
                Field.toggle("bondEnabled", LANG + "field.bondEnabled", "Enable-Bond",
                        config::ifBondEnabled, config::setBondEnabled),
                Field.number("bondMaxLevel", LANG + "field.bondMaxLevel", "Bond-Max-Level",
                        config::getBondMaxLevel, value -> config.setBondMaxLevel((int) value)),
                Field.number("bondXpPerLevel", LANG + "field.bondXpPerLevel", "Bond-Xp-Per-Level",
                        config::getBondXpPerLevel, config::setBondXpPerLevel),
                Field.number("bondXpPerHour", LANG + "field.bondXpPerHour", "Bond-Xp-Per-Hour-Married",
                        config::getBondXpPerHourMarried, config::setBondXpPerHourMarried),
                Field.number("bondXpPerMinuteTogether", LANG + "field.bondXpPerMinuteTogether", "Bond-Xp-Per-Minute-Together",
                        config::getBondXpPerMinuteTogether, config::setBondXpPerMinuteTogether),
                Field.number("bondRangeBlocks", LANG + "field.bondRangeBlocks", "Bond-Range-Blocks",
                        config::getBondRangeBlocks, config::setBondRangeBlocks),
                Field.number("bondXpPerGift", LANG + "field.bondXpPerGift", "Bond-Xp-Per-Gift",
                        config::getBondXpPerGift, config::setBondXpPerGift),
                Field.number("bondXpPerCoopKill", LANG + "field.bondXpPerCoopKill", "Bond-Xp-Per-Coop-Kill",
                        config::getBondXpPerCoopKill, config::setBondXpPerCoopKill),
                Field.number("bondRegenRadiusPerLevel", LANG + "field.bondRegenRadiusPerLevel", "Bond-Regen-Radius-Percent-Per-Level",
                        config::getBondRegenRadiusPercentPerLevel, config::setBondRegenRadiusPercentPerLevel),
                Field.number("bondTpCooldownPerLevel", LANG + "field.bondTpCooldownPerLevel", "Bond-Tp-Cooldown-Percent-Per-Level",
                        config::getBondTpCooldownPercentPerLevel, config::setBondTpCooldownPercentPerLevel))));

        sections.add(new Section("anniversaries", LANG + "section.anniversaries", LANG + "section.anniversaries.hint", List.of(
                Field.toggle("anniversariesEnabled", LANG + "field.anniversariesEnabled", "Enable-Anniversaries",
                        config::ifAnniversariesEnabled, config::setAnniversariesEnabled),
                Field.toggle("announceAnniversaries", LANG + "field.announceAnniversaries", "Announce-Anniversaries",
                        config::ifAnnounceAnniversaries, config::setAnnounceAnniversaries),
                Field.number("anniversaryBondXp", LANG + "field.anniversaryBondXp", "Anniversary-Bond-Xp",
                        config::getAnniversaryBondXp, config::setAnniversaryBondXp))));

        sections.add(new Section("bondDecay", LANG + "section.bondDecay", LANG + "section.bondDecay.hint", List.of(
                Field.toggle("bondDecayEnabled", LANG + "field.bondDecayEnabled", "Enable-Bond-Decay",
                        config::ifBondDecayEnabled, config::setBondDecayEnabled),
                Field.number("bondDecayGraceDays", LANG + "field.bondDecayGraceDays", "Bond-Decay-Grace-Days",
                        config::getBondDecayGraceDays, config::setBondDecayGraceDays),
                Field.number("bondDecayXpPerDay", LANG + "field.bondDecayXpPerDay", "Bond-Decay-Xp-Per-Day",
                        config::getBondDecayXpPerDay, config::setBondDecayXpPerDay),
                Field.number("bondDecayMinLevel", LANG + "field.bondDecayMinLevel", "Bond-Decay-Min-Level",
                        config::getBondDecayMinLevel, value -> config.setBondDecayMinLevel((int) value)))));

        sections.add(new Section("home", LANG + "section.home", LANG + "section.home.hint", List.of(
                Field.toggle("sharedHomeEnabled", LANG + "field.sharedHomeEnabled", "Enable-Shared-Home",
                        config::ifSharedHomeEnabled, config::setSharedHomeEnabled),
                Field.number("sharedHomeBondLevel", LANG + "field.sharedHomeBondLevel", "Shared-Home-Bond-Level",
                        config::getSharedHomeBondLevel, value -> config.setSharedHomeBondLevel((int) value)),
                Field.number("homeCooldownSeconds", LANG + "field.homeCooldownSeconds", "Home-Cooldown-Seconds",
                        config::getHomeCooldownSeconds, value -> config.setHomeCooldownSeconds((int) value)),
                Field.toggle("showHomeOnMap", LANG + "field.showHomeOnMap", "Show-Home-On-Map",
                        config::ifShowHomeOnMap, config::setShowHomeOnMap))));

        sections.add(new Section("markers", LANG + "section.markers", LANG + "section.markers.hint", List.of(
                Field.toggle("partnerMarkerEnabled", LANG + "field.partnerMarkerEnabled", "Enable-Partner-Marker",
                        config::ifPartnerMarkerEnabled, config::setPartnerMarkerEnabled),
                Field.number("partnerMarkerBondLevel", LANG + "field.partnerMarkerBondLevel", "Partner-Marker-Bond-Level",
                        config::getPartnerMarkerBondLevel, value -> config.setPartnerMarkerBondLevel((int) value)),
                Field.toggle("deathAlertEnabled", LANG + "field.deathAlertEnabled", "Enable-Death-Alert",
                        config::ifDeathAlertEnabled, config::setDeathAlertEnabled),
                Field.number("deathAlertBondLevel", LANG + "field.deathAlertBondLevel", "Death-Alert-Bond-Level",
                        config::getDeathAlertBondLevel, value -> config.setDeathAlertBondLevel((int) value)),
                Field.number("deathMarkerMinutes", LANG + "field.deathMarkerMinutes", "Death-Marker-Minutes",
                        config::getDeathMarkerMinutes, config::setDeathMarkerMinutes))));

        sections.add(new Section("chest", LANG + "section.chest", LANG + "section.chest.hint", List.of(
                Field.toggle("hopeChestEnabled", LANG + "field.hopeChestEnabled", "Enable-Hope-Chest",
                        config::ifHopeChestEnabled, config::setHopeChestEnabled),
                Field.number("hopeChestBondLevel", LANG + "field.hopeChestBondLevel", "Hope-Chest-Bond-Level",
                        config::getHopeChestBondLevel, value -> config.setHopeChestBondLevel((int) value)),
                Field.number("hopeChestSlots", LANG + "field.hopeChestSlots", "Hope-Chest-Slots",
                        config::getHopeChestSlots, value -> config.setHopeChestSlots((int) value)))));

        sections.add(new Section("combatPerks", LANG + "section.combatPerks", LANG + "section.combatPerks.hint", List.of(
                Field.toggle("bondOfPainEnabled", LANG + "field.bondOfPainEnabled", "Enable-Bond-Of-Pain",
                        config::ifBondOfPainEnabled, config::setBondOfPainEnabled),
                Field.number("bondOfPainBondLevel", LANG + "field.bondOfPainBondLevel", "Bond-Of-Pain-Bond-Level",
                        config::getBondOfPainBondLevel, value -> config.setBondOfPainBondLevel((int) value)),
                Field.number("bondOfPainSharePercent", LANG + "field.bondOfPainSharePercent", "Bond-Of-Pain-Share-Percent",
                        config::getBondOfPainSharePercent, config::setBondOfPainSharePercent),
                Field.number("bondOfPainRangeBlocks", LANG + "field.bondOfPainRangeBlocks", "Bond-Of-Pain-Range-Blocks",
                        config::getBondOfPainRangeBlocks, config::setBondOfPainRangeBlocks),
                Field.toggle("lastStandEnabled", LANG + "field.lastStandEnabled", "Enable-Last-Stand",
                        config::ifLastStandEnabled, config::setLastStandEnabled),
                Field.number("lastStandBondLevel", LANG + "field.lastStandBondLevel", "Last-Stand-Bond-Level",
                        config::getLastStandBondLevel, value -> config.setLastStandBondLevel((int) value)),
                Field.number("lastStandDurationSeconds", LANG + "field.lastStandDurationSeconds", "Last-Stand-Duration-Seconds",
                        config::getLastStandDurationSeconds, config::setLastStandDurationSeconds))));

        sections.add(new Section("social", LANG + "section.social", LANG + "section.social.hint", List.of(
                Field.toggle("coupleChannelEnabled", LANG + "field.coupleChannelEnabled", "Enable-Couple-Channel",
                        config::ifCoupleChannelEnabled, config::setCoupleChannelEnabled),
                Field.toggle("loveLettersEnabled", LANG + "field.loveLettersEnabled", "Enable-Love-Letters",
                        config::ifLoveLettersEnabled, config::setLoveLettersEnabled),
                Field.number("loveLetterMaxLength", LANG + "field.loveLetterMaxLength", "Love-Letter-Max-Length",
                        config::getLoveLetterMaxLength, value -> config.setLoveLetterMaxLength((int) value)),
                Field.toggle("partnerHudEnabled", LANG + "field.partnerHudEnabled", "Enable-Partner-Hud",
                        config::ifPartnerHudEnabled, config::setPartnerHudEnabled),
                Field.number("partnerHudRangeBlocks", LANG + "field.partnerHudRangeBlocks", "Partner-Hud-Range-Blocks",
                        config::getPartnerHudRangeBlocks, config::setPartnerHudRangeBlocks),
                Field.toggle("recordsEnabled", LANG + "field.recordsEnabled", "Enable-Records",
                        config::ifRecordsEnabled, config::setRecordsEnabled))));

        sections.add(new Section("rings", LANG + "section.rings", LANG + "section.rings.hint", List.of(
                Field.toggle("ringTiersEnabled", LANG + "field.ringTiersEnabled", "Enable-Ring-Tiers",
                        config::ifRingTiersEnabled, config::setRingTiersEnabled),
                Field.toggle("engraveRings", LANG + "field.engraveRings", "Engrave-Rings",
                        config::ifEngraveRings, config::setEngraveRings),
                Field.number("goldRingXpMultiplier", LANG + "field.goldRingXpMultiplier", "Gold-Ring-Xp-Multiplier",
                        config::getGoldRingXpMultiplier, config::setGoldRingXpMultiplier),
                Field.number("goldRingPerkDiscount", LANG + "field.goldRingPerkDiscount", "Gold-Ring-Perk-Discount",
                        config::getGoldRingPerkDiscount, value -> config.setGoldRingPerkDiscount((int) value)),
                Field.number("diamondRingXpMultiplier", LANG + "field.diamondRingXpMultiplier", "Diamond-Ring-Xp-Multiplier",
                        config::getDiamondRingXpMultiplier, config::setDiamondRingXpMultiplier),
                Field.number("diamondRingPerkDiscount", LANG + "field.diamondRingPerkDiscount", "Diamond-Ring-Perk-Discount",
                        config::getDiamondRingPerkDiscount, value -> config.setDiamondRingPerkDiscount((int) value)))));

        sections.add(new Section("vows", LANG + "section.vows", LANG + "section.vows.hint", List.of(
                Field.toggle("vowsEnabled", LANG + "field.vowsEnabled", "Enable-Vows",
                        config::ifVowsEnabled, config::setVowsEnabled),
                Field.number("vowPerkDiscount", LANG + "field.vowPerkDiscount", "Vow-Perk-Discount",
                        config::getVowPerkDiscount, value -> config.setVowPerkDiscount((int) value)),
                Field.number("vowHearthSlots", LANG + "field.vowHearthSlots", "Vow-Hearth-Chest-Bonus-Slots",
                        config::getVowHearthChestBonusSlots, value -> config.setVowHearthChestBonusSlots((int) value)),
                Field.number("vowBladeShare", LANG + "field.vowBladeShare", "Vow-Blade-Share-Bonus-Percent",
                        config::getVowBladeShareBonusPercent, config::setVowBladeShareBonusPercent),
                Field.number("vowWanderCut", LANG + "field.vowWanderCut", "Vow-Wander-Cooldown-Cut-Percent",
                        config::getVowWanderCooldownCutPercent, config::setVowWanderCooldownCutPercent),
                Field.toggle("renewEnabled", LANG + "field.renewEnabled", "Enable-Renew-Vows",
                        config::ifRenewEnabled, config::setRenewEnabled),
                Field.number("renewMinDays", LANG + "field.renewMinDays", "Renew-Min-Days",
                        config::getRenewMinDays, config::setRenewMinDays),
                Field.number("renewBondXp", LANG + "field.renewBondXp", "Renew-Bond-Xp",
                        config::getRenewBondXp, config::setRenewBondXp))));

        sections.add(new Section("honeymoon", LANG + "section.honeymoon", LANG + "section.honeymoon.hint", List.of(
                Field.toggle("honeymoonEnabled", LANG + "field.honeymoonEnabled", "Enable-Honeymoon",
                        config::ifHoneymoonEnabled, config::setHoneymoonEnabled),
                Field.number("honeymoonHours", LANG + "field.honeymoonHours", "Honeymoon-Hours",
                        config::getHoneymoonHours, config::setHoneymoonHours),
                Field.number("honeymoonMultiplier", LANG + "field.honeymoonMultiplier", "Honeymoon-Xp-Multiplier",
                        config::getHoneymoonXpMultiplier, config::setHoneymoonXpMultiplier),
                Field.number("anniversaryRewardQuantity", LANG + "field.anniversaryRewardQuantity", "Anniversary-Reward-Quantity",
                        config::getAnniversaryRewardQuantity, value -> config.setAnniversaryRewardQuantity((int) value)))));

        sections.add(new Section("divorceWeight", LANG + "section.divorceWeight", LANG + "section.divorceWeight.hint", List.of(
                Field.toggle("divorceWaitEnabled", LANG + "field.divorceWaitEnabled", "Enable-Divorce-Waiting-Period",
                        config::ifDivorceWaitingPeriod, config::setDivorceWaitingPeriod),
                Field.number("divorceWaitingHours", LANG + "field.divorceWaitingHours", "Divorce-Waiting-Hours",
                        config::getDivorceWaitingHours, config::setDivorceWaitingHours),
                Field.number("divorceUncontestedFactor", LANG + "field.divorceUncontestedFactor", "Divorce-Uncontested-Factor",
                        config::getDivorceUncontestedFactor, config::setDivorceUncontestedFactor),
                Field.number("remarryCooldownSeconds", LANG + "field.remarryCooldownSeconds", "Remarry-Cooldown-Seconds",
                        config::getRemarryCooldownSeconds, value -> config.setRemarryCooldownSeconds((int) value)))));

        sections.add(new Section("emotes", LANG + "section.emotes", LANG + "section.emotes.hint", List.of(
                Field.toggle("emotesEnabled", LANG + "field.emotesEnabled", "Enable-Emotes",
                        config::ifEmotesEnabled, config::setEmotesEnabled),
                Field.number("emoteRangeBlocks", LANG + "field.emoteRangeBlocks", "Emote-Range-Blocks",
                        config::getEmoteRangeBlocks, config::setEmoteRangeBlocks),
                Field.number("emoteBroadcastRadius", LANG + "field.emoteBroadcastRadius", "Emote-Broadcast-Radius-Blocks",
                        config::getEmoteBroadcastRadiusBlocks, config::setEmoteBroadcastRadiusBlocks),
                Field.number("emoteCooldownSeconds", LANG + "field.emoteCooldownSeconds", "Emote-Cooldown-Seconds",
                        config::getEmoteCooldownSeconds, value -> config.setEmoteCooldownSeconds((int) value)))));

        sections.add(new Section("respawn", LANG + "section.respawn", LANG + "section.respawn.hint", List.of(
                Field.toggle("respawnAtPartnerEnabled", LANG + "field.respawnAtPartnerEnabled", "Enable-Respawn-At-Partner",
                        config::ifRespawnAtPartnerEnabled, config::setRespawnAtPartnerEnabled),
                Field.number("respawnAtPartnerBondLevel", LANG + "field.respawnAtPartnerBondLevel", "Respawn-At-Partner-Bond-Level",
                        config::getRespawnAtPartnerBondLevel, value -> config.setRespawnAtPartnerBondLevel((int) value)),
                Field.number("respawnAtPartnerCooldownSeconds", LANG + "field.respawnAtPartnerCooldownSeconds", "Respawn-At-Partner-Cooldown-Seconds",
                        config::getRespawnAtPartnerCooldownSeconds, value -> config.setRespawnAtPartnerCooldownSeconds((int) value)))));

        return sections;
    }

    // --- Lifecycle -----------------------------------------------------------

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        commandBuilder.append(PAGE);

        if (!this.playerRef.hasPermission(ADMIN_PERMISSION)) {
            Marriage.LOGGER.atWarning().log(this.playerRef.getUsername()
                    + " opened the Marriage admin page without " + ADMIN_PERMISSION + " - showing nothing.");
            commandBuilder.set("#SectionTitle.TextSpans", Message.translation(LANG + "denied"));
            commandBuilder.set("#SectionHint.TextSpans", Message.translation(LANG + "denied.hint"));
            commandBuilder.set("#SaveButton.Disabled", true);
            commandBuilder.set("#RevertButton.Disabled", true);
            return;
        }

        render(commandBuilder, eventBuilder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull AdminEventData data) {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();

        // Re-checked every click, not just on open: an admin whose permission is
        // revoked while the page sits open must not still be able to press Save.
        if (!this.playerRef.hasPermission(ADMIN_PERMISSION)) {
            this.sendUpdate(commandBuilder, eventBuilder, false);
            return;
        }

        if (data.section != null) {
            this.selectedSectionId = data.section;
        } else if (data.field != null) {
            // A row pushes its own value on every keystroke or tick. Store it; do not
            // apply it.
            if (data.fieldValue != null) {
                this.pendingNumbers.put(data.field, data.fieldValue);
            }
            if (data.fieldToggle != null) {
                this.pendingToggles.put(data.field, data.fieldToggle);
            }
            // No redraw: rebuilding the list while somebody is typing in it would
            // fight the cursor.
            this.sendUpdate(commandBuilder, eventBuilder, false);
            return;
        } else if (data.action != null) {
            applyAction(data.action);
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            MarriageAdminUIPage page = new MarriageAdminUIPage(this.playerRef, this.selectedSectionId);
            page.pendingNumbers.putAll(this.pendingNumbers);
            page.pendingToggles.putAll(this.pendingToggles);
            page.lastStatus = this.lastStatus;
            player.getPageManager().openCustomPage(ref, store, page);
            return;
        }

        render(commandBuilder, eventBuilder);
        this.sendUpdate(commandBuilder, eventBuilder, false);
    }

    private void applyAction(@Nonnull String action) {
        if (AdminEventData.ACTION_REVERT.equals(action)) {
            int discarded = this.pendingNumbers.size() + this.pendingToggles.size();
            this.pendingNumbers.clear();
            this.pendingToggles.clear();
            this.lastStatus = discarded == 0 ? LANG + "status.nothingToRevert" : LANG + "status.reverted";
            return;
        }

        if (!AdminEventData.ACTION_SAVE.equals(action)) {
            return;
        }

        int applied = 0;
        for (Section section : sections()) {
            for (Field field : section.fields()) {
                if (field.toggle()) {
                    Boolean pending = this.pendingToggles.get(field.key());
                    if (pending != null && pending != field.readToggle()) {
                        field.writeToggle(pending);
                        applied++;
                    }
                } else {
                    Double pending = this.pendingNumbers.get(field.key());
                    if (pending != null && field.differsFrom(pending)) {
                        field.writeNumber(pending);
                        applied++;
                    }
                }
            }
        }

        this.pendingNumbers.clear();
        this.pendingToggles.clear();

        if (applied == 0) {
            this.lastStatus = LANG + "status.noChanges";
            return;
        }

        Marriage.getConfig().save();
        this.lastStatus = LANG + "status.saved";
        Marriage.LOGGER.atInfo().log(this.playerRef.getUsername()
                + " changed " + applied + " Marriage config value(s) from the admin menu.");
    }

    // --- Rendering -------------------------------------------------------------

    private void render(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        List<Section> sections = sections();
        Section selected = null;
        for (Section section : sections) {
            if (section.id().equals(this.selectedSectionId)) {
                selected = section;
            }
        }
        if (selected == null && !sections.isEmpty()) {
            selected = sections.get(0);
        }
        this.selectedSectionId = selected == null ? null : selected.id();

        renderStatus(commandBuilder);
        renderSectionList(sections, selected, commandBuilder, eventBuilder);
        renderFields(selected, commandBuilder, eventBuilder);
    }

    private void renderStatus(@Nonnull UICommandBuilder commandBuilder) {
        int pending = this.pendingNumbers.size() + this.pendingToggles.size();

        if (pending > 0) {
            commandBuilder.set("#StatusLabel.TextSpans", Message.translation(LANG + "status.pending").param("count", pending));
        } else if (this.lastStatus != null) {
            commandBuilder.set("#StatusLabel.TextSpans", Message.translation(this.lastStatus));
        } else {
            commandBuilder.set("#StatusLabel.TextSpans", Message.translation(LANG + "status.idle"));
        }

        commandBuilder.set("#SaveButton.Disabled", false);
        commandBuilder.set("#RevertButton.Disabled", this.pendingNumbers.isEmpty() && this.pendingToggles.isEmpty());
    }

    private void renderSectionList(
            @Nonnull List<Section> sections,
            Section selected,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder
    ) {
        commandBuilder.clear("#SectionList");

        for (int index = 0; index < sections.size(); index++) {
            Section section = sections.get(index);
            boolean isSelected = selected != null && selected.id().equals(section.id());

            commandBuilder.append("#SectionList", SECTION_ENTRY);
            String entry = "#SectionList[" + index + "]";

            commandBuilder.set(entry + " #Button.TextSpans", Message.translation(section.nameKey()));
            commandBuilder.set(entry + " #Button.Disabled", isSelected);
            commandBuilder.set(entry + " #Spine.Visible", isSelected);

            if (!isSelected) {
                eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, entry + " #Button",
                        EventData.of(AdminEventData.KEY_SECTION, section.id()));
            }
        }

        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SaveButton",
                EventData.of(AdminEventData.KEY_ACTION, AdminEventData.ACTION_SAVE));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#RevertButton",
                EventData.of(AdminEventData.KEY_ACTION, AdminEventData.ACTION_REVERT));
    }

    private void renderFields(Section section, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        commandBuilder.clear("#ConfigList");

        if (section == null) {
            commandBuilder.set("#SectionTitle.TextSpans", Message.empty());
            commandBuilder.set("#SectionHint.TextSpans", Message.empty());
            return;
        }

        commandBuilder.set("#SectionTitle.TextSpans", Message.translation(section.nameKey()));
        commandBuilder.set("#SectionHint.TextSpans", Message.translation(section.hintKey()));

        int rowIndex = 0;
        for (Field field : section.fields()) {
            commandBuilder.append("#ConfigList", field.toggle() ? TOGGLE_ROW : NUMBER_ROW);
            String row = "#ConfigList[" + rowIndex + "]";
            rowIndex++;

            commandBuilder.set(row + " #Label.TextSpans", Message.translation(field.labelKey()));
            // The raw config key, so an admin can match a row to the JSON. Always
            // shown - CheckRow.ui defaults #Detail to hidden for the Settings
            // category's conditional blurbs, so the admin reuse of that same
            // fragment must say so explicitly.
            commandBuilder.set(row + " #Detail.Text", field.configKey());
            commandBuilder.set(row + " #Detail.Visible", true);

            if (field.toggle()) {
                Boolean pending = this.pendingToggles.get(field.key());
                commandBuilder.set(row + " #CheckBox.Value", pending != null ? pending : field.readToggle());

                // '@' on the value key is what makes the client resolve the selector
                // into the checkbox's current state instead of echoing the selector
                // text back; locksInterface=false because typing needs no page update.
                eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, row + " #CheckBox",
                        new EventData()
                                .append(AdminEventData.KEY_FIELD, field.key())
                                .append(AdminEventData.KEY_FIELD_TOGGLE, row + " #CheckBox.Value"), false);
            } else {
                Double pending = this.pendingNumbers.get(field.key());
                commandBuilder.set(row + " #Input.Value", pending != null ? pending : field.readNumber());

                eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, row + " #Input",
                        new EventData()
                                .append(AdminEventData.KEY_FIELD, field.key())
                                .append(AdminEventData.KEY_FIELD_VALUE, row + " #Input.Value"), false);
            }
        }
    }

    // --- Field model -------------------------------------------------------

    /** One editable config value. Either a number or a toggle, never both. */
    private record Field(
            String key,
            String labelKey,
            String configKey,
            boolean toggle,
            DoubleSupplier numberGetter,
            // DoubleConsumer, not Consumer<Double>: the setters below narrow to int or
            // double, and you cannot cast a boxed Double straight to a primitive.
            DoubleConsumer numberSetter,
            BooleanSupplier toggleGetter,
            Consumer<Boolean> toggleSetter
    ) {
        static Field number(String key, String labelKey, String configKey, DoubleSupplier getter, DoubleConsumer setter) {
            return new Field(key, labelKey, configKey, false, getter, setter, null, null);
        }

        static Field toggle(String key, String labelKey, String configKey, BooleanSupplier getter, Consumer<Boolean> setter) {
            return new Field(key, labelKey, configKey, true, null, null, getter, setter);
        }

        double readNumber() {
            return this.numberGetter.getAsDouble();
        }

        void writeNumber(double value) {
            this.numberSetter.accept(value);
        }

        /** True when the pending edit differs from what the config already holds. */
        boolean differsFrom(double pending) {
            return Double.compare(pending, readNumber()) != 0;
        }

        boolean readToggle() {
            return this.toggleGetter.getAsBoolean();
        }

        void writeToggle(boolean value) {
            this.toggleSetter.accept(value);
        }
    }

    private record Section(String id, String nameKey, String hintKey, List<Field> fields) {}

    // --- Event payload -----------------------------------------------------

    public static class AdminEventData {
        static final String KEY_SECTION = "Section";
        static final String KEY_ACTION = "Action";
        static final String KEY_FIELD = "Field";
        // '@'-prefixed: these two are selector-resolved, i.e. the client replaces the
        // selector string with the widget's live value before sending.
        static final String KEY_FIELD_VALUE = "@FieldValue";
        static final String KEY_FIELD_TOGGLE = "@FieldToggle";

        static final String ACTION_SAVE = "save";
        static final String ACTION_REVERT = "revert";

        public static final BuilderCodec<AdminEventData> CODEC =
                BuilderCodec.builder(AdminEventData.class, AdminEventData::new)
                        .addField(new KeyedCodec<>(KEY_SECTION, Codec.STRING),
                                (entry, value) -> entry.section = value, entry -> entry.section)
                        .addField(new KeyedCodec<>(KEY_ACTION, Codec.STRING),
                                (entry, value) -> entry.action = value, entry -> entry.action)
                        .addField(new KeyedCodec<>(KEY_FIELD, Codec.STRING),
                                (entry, value) -> entry.field = value, entry -> entry.field)
                        // Lenient: a cleared NumberField sends a quoted empty string,
                        // and the strict double codec would throw and discard the
                        // whole payload rather than just this one value.
                        .addField(new KeyedCodec<>(KEY_FIELD_VALUE, LenientDoubleCodec.INSTANCE),
                                (entry, value) -> entry.fieldValue = value, entry -> entry.fieldValue)
                        .addField(new KeyedCodec<>(KEY_FIELD_TOGGLE, Codec.BOOLEAN),
                                (entry, value) -> entry.fieldToggle = value, entry -> entry.fieldToggle)
                        .build();

        private String section;
        private String action;
        private String field;
        private Double fieldValue;
        private Boolean fieldToggle;

        public AdminEventData() {
        }
    }
}
