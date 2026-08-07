package plugin.siren.Utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import plugin.siren.Marriage;

import javax.annotation.Nonnull;

public class MarriageConfig {

    private static final String INFORMATION_DEFAULT = "Confused about what one of these statements do? Go to https://mermaids.dev/marriage/config/ or check out the Marriage page on the Curseforge website and scroll down to Config Extra Info.";
    private static final String WEBSITE_DEFAULT = "https://mermaids.dev/marriage/";
    private static final String DOWNLOAD_SITE_DEFAULT = "https://www.curseforge.com/hytale/mods/marriage";
    private static final int CONFIG_VERSION_DEFAULT = 5;

    private static final String DESC_REQUIRE_RING = "When true, both players must hold a Marriage Ring in their hand to request or accept a marriage.";
    private static final String DESC_CONSUME_RING = "When true (and rings are required), the rings are consumed when the marriage completes.";
    private static final String DESC_CMD_PERMISSION = "When true, most /marry commands require their marriage.* permission (see the mod page for the list).";
    private static final String DESC_ANNOUNCE_MARRIAGES = "When true, the whole server is told when two players get married.";
    private static final String DESC_ANNOUNCE_DIVORCES = "When true, the whole server is told when two players divorce.";
    private static final String DESC_REQUEST_EXPIRY = "Seconds a marriage request stays pending before it quietly expires.";
    private static final String DESC_DIVORCE_CONFIRM = "Seconds the player has to repeat /marry divorce (or click again) to confirm.";
    private static final String DESC_ENABLE_MENU = "When true, /marriage opens the in-game Marriage menu.";
    private static final String DESC_GRANT_MENU_PERMISSION = "When true, the base 'marriage' permission is granted once to the default player groups so everyone can open the menu.";
    private static final String DESC_GRANTED_MENU_PERMISSION = "Internal latch so the permission grant only happens once. Leave this alone.";
    private static final String DESC_PVP_PROTECTION = "Master switch for partner PvP protection. When true, married partners cannot hurt each other while their couple toggle is on.";
    private static final String DESC_DEFAULT_PVP_PROTECTION = "The PvP protection setting newly married couples start with. Couples change theirs in the menu.";
    private static final String DESC_PVP_PROTECTION_MESSAGE = "When true, an attacker is told (briefly, with a cooldown) why their hit on their partner did nothing.";
    private static final String DESC_ENABLE_REGEN = "Master switch for partner regeneration. When true, married partners near each other slowly regenerate health.";
    private static final String DESC_REGEN_RADIUS = "How close (in blocks) partners must be, in the same world, for partner regeneration to apply.";
    private static final String DESC_REGEN_INTERVAL = "Seconds between partner regeneration ticks. Applied at server start - changing it needs a restart.";
    private static final String DESC_REGEN_AMOUNT = "Health restored per regeneration tick while near your partner (vanilla max health is 100).";
    private static final String DESC_ENABLE_GIFT = "Master switch for /marry gift - handing the item stack you are holding to your partner.";
    private static final String DESC_GIFT_RANGE = "How close (in blocks) partners must be to send a gift. Set 0 to allow gifting from anywhere, even across worlds.";
    private static final String DESC_GIFT_COOLDOWN = "Seconds between gifts per player. Set 0 for no cooldown.";
    private static final String DESC_ENABLE_TP = "Master switch for /marry tp - teleporting to your partner.";
    private static final String DESC_TP_COOLDOWN = "Seconds between partner teleports per player. Set 0 for no cooldown.";
    private static final String DESC_ENABLE_MSG = "Master switch for /marry msg - private messages to your partner.";
    private static final String DESC_NEW_VERSION = "When true, admins are told in chat when a newer Marriage version is released.";

    private static final String DESC_ENABLE_BOND = "Master switch for the bond - a marriage's own progression. When false, every enabled perk below is simply available from the wedding day and no bond XP is tracked.";
    private static final String DESC_BOND_MAX_LEVEL = "The highest bond level a couple can reach.";
    private static final String DESC_BOND_XP_PER_LEVEL = "Base cost of a bond level. The real cost rises with each level (level N costs N times this), so reaching the maximum costs far more than this number alone suggests.";
    private static final String DESC_BOND_XP_PER_HOUR = "Bond XP granted per hour married. Accrues whether or not either partner is online, but only catches up while at least one of them is.";
    private static final String DESC_BOND_XP_PER_MINUTE_TOGETHER = "Bond XP granted per minute both partners spend within Bond-Range-Blocks of each other.";
    private static final String DESC_BOND_RANGE = "How close (in blocks) partners must be, in the same world, to count as together for bond XP and bond decay.";
    private static final String DESC_BOND_XP_PER_GIFT = "Bond XP granted when a gift lands with the partner.";
    private static final String DESC_BOND_XP_PER_COOP_KILL = "Bond XP granted when a player kills something with their partner within Bond-Range-Blocks.";
    private static final String DESC_BOND_REGEN_RADIUS_PER_LEVEL = "Percent the partner regeneration radius grows per bond level.";
    private static final String DESC_BOND_TP_COOLDOWN_PER_LEVEL = "Percent the partner teleport cooldown shrinks per bond level.";

    private static final String DESC_ENABLE_ANNIVERSARIES = "When true, couples are congratulated and paid bond XP at 1, 7, 30 and 365 days married.";
    private static final String DESC_ANNOUNCE_ANNIVERSARIES = "When true, the whole server is told when a couple reaches an anniversary.";
    private static final String DESC_ANNIVERSARY_BOND_XP = "Bond XP granted for each anniversary milestone reached.";

    private static final String DESC_ENABLE_BOND_DECAY = "When true, a bond loses XP once the partners have not been near each other for Bond-Decay-Grace-Days. Off by default.";
    private static final String DESC_BOND_DECAY_GRACE_DAYS = "Days apart before bond decay starts. Time only counts while at least one partner is online.";
    private static final String DESC_BOND_DECAY_XP_PER_DAY = "Bond XP lost per day once decay has started.";
    private static final String DESC_BOND_DECAY_MIN_LEVEL = "The bond level decay can never drag a couple below.";

    private static final String DESC_ENABLE_SHARED_HOME = "Master switch for the couple's shared home - /marry sethome and /marry home.";
    private static final String DESC_SHARED_HOME_LEVEL = "Bond level needed to unlock the shared home. 0 unlocks it on the wedding day.";
    private static final String DESC_HOME_COOLDOWN = "Seconds between shared-home teleports per player. Set 0 for no cooldown.";
    private static final String DESC_SHOW_HOME_ON_MAP = "When true, the couple's shared home is pinned on their world map.";

    private static final String DESC_ENABLE_PARTNER_MARKER = "Master switch for showing your partner's live position on your world map.";
    private static final String DESC_PARTNER_MARKER_LEVEL = "Bond level needed to unlock the partner map marker. 0 unlocks it on the wedding day.";

    private static final String DESC_ENABLE_DEATH_ALERT = "Master switch for being told where your partner died, with a marker on the spot.";
    private static final String DESC_DEATH_ALERT_LEVEL = "Bond level needed to unlock death alerts. 0 unlocks them on the wedding day.";
    private static final String DESC_DEATH_MARKER_MINUTES = "How many minutes a partner's death marker stays on your map. Set 0 to send the message without a marker.";

    private static final String DESC_ENABLE_HOPE_CHEST = "Master switch for the Hope Chest - one container both partners can open from anywhere.";
    private static final String DESC_HOPE_CHEST_LEVEL = "Bond level needed to unlock the Hope Chest. 0 unlocks it on the wedding day.";
    private static final String DESC_HOPE_CHEST_SLOTS = "How many slots the Hope Chest holds. Raising this grows existing chests; lowering it only affects chests created afterwards, so nothing is ever deleted.";

    private static final String DESC_ENABLE_BOND_OF_PAIN = "Master switch for the bond of pain - a share of the damage aimed at one partner lands on the other instead.";
    private static final String DESC_BOND_OF_PAIN_LEVEL = "Bond level needed to unlock the bond of pain. 0 unlocks it on the wedding day.";
    private static final String DESC_BOND_OF_PAIN_SHARE = "Percent of incoming damage moved from the wounded partner onto the other one.";
    private static final String DESC_BOND_OF_PAIN_RANGE = "How close (in blocks) partners must be, in the same world, for the bond of pain to carry damage.";

    private static final String DESC_ENABLE_LAST_STAND = "Master switch for last stand - a burst of regeneration for the survivor when their partner falls.";
    private static final String DESC_LAST_STAND_LEVEL = "Bond level needed to unlock last stand. 0 unlocks it on the wedding day.";
    private static final String DESC_LAST_STAND_EFFECT = "The entity effect applied to the survivor. Any effect your server ships; an unknown id simply does nothing.";
    private static final String DESC_LAST_STAND_DURATION = "Seconds the last stand effect lasts.";

    private static final String DESC_ENABLE_RESPAWN_AT_PARTNER = "Master switch for respawning beside your living partner instead of at your bed.";
    private static final String DESC_RESPAWN_AT_PARTNER_LEVEL = "Bond level needed to unlock respawning at your partner. 0 unlocks it on the wedding day.";
    private static final String DESC_RESPAWN_AT_PARTNER_COOLDOWN = "Seconds between partner respawns per player. Deliberately long - this is a free trip home.";

    private static final String DESC_ENABLE_COUPLE_CHANNEL = "Master switch for the couple chat channel - a toggle that sends everything a player types to their partner instead of the whole server.";
    private static final String DESC_ENABLE_LOVE_LETTERS = "Master switch for Love Letters - a writable item that can be gifted and read.";
    private static final String DESC_LOVE_LETTER_MAX_LENGTH = "Longest a love letter may be, in characters.";
    private static final String DESC_ENABLE_PARTNER_HUD = "Master switch for the small on-screen plate showing your partner's health and distance while they are near.";
    private static final String DESC_PARTNER_HUD_RANGE = "How close (in blocks) your partner must be, in the same world, for the partner HUD to appear.";
    private static final String DESC_ENABLE_RECORDS = "When true, the Marriage menu shows a Records page - longest marriage, most gifts given, most divorces.";
    private static final String DESC_ENABLE_RING_TIERS = "Master switch for ring tiers. When false, every band behaves like the base Copper one and grants no bonus.";
    private static final String DESC_ENGRAVE_RINGS = "When true, a band that is NOT consumed at the wedding is engraved with both partners' names.";
    private static final String DESC_GOLD_RING_XP_MULTIPLIER = "Bond XP multiplier for a couple who married with Gold bands.";
    private static final String DESC_GOLD_RING_PERK_DISCOUNT = "Bond levels a Gold band knocks off every perk's requirement.";
    private static final String DESC_DIAMOND_RING_XP_MULTIPLIER = "Bond XP multiplier for a couple who married with Diamond bands.";
    private static final String DESC_DIAMOND_RING_PERK_DISCOUNT = "Bond levels a Diamond band knocks off every perk's requirement.";

    private static final String DESC_ENABLE_VOWS = "Master switch for vows - the promise a couple swears, which shapes which perks come to them first.";
    private static final String DESC_VOW_PERK_DISCOUNT = "Bond levels a vow knocks off the requirement of the two perks it favors. Only those two.";
    private static final String DESC_VOW_HEARTH_SLOTS = "Extra Hope Chest slots for a couple sworn to the Hearth.";
    private static final String DESC_VOW_BLADE_SHARE = "Extra percent of damage the bond of pain moves for a couple sworn to the Blade.";
    private static final String DESC_VOW_WANDER_CUT = "Percent off the partner teleport and respawn cooldowns for a couple sworn to Wandering.";

    private static final String DESC_ENABLE_HONEYMOON = "When true, a newly married couple gain bond XP faster for their first hours together.";
    private static final String DESC_HONEYMOON_HOURS = "How many hours after the wedding the honeymoon lasts. Measured from the wedding, so renewing vows does not restart it.";
    private static final String DESC_HONEYMOON_MULTIPLIER = "Bond XP multiplier during the honeymoon.";

    private static final String DESC_ENABLE_DIVORCE_WAIT = "When true, a one-sided divorce waits out a period before it completes, and the other partner is told. A divorce both partners ask for is always immediate.";
    private static final String DESC_DIVORCE_WAIT_HOURS = "Hours a contested divorce waits before completing.";
    private static final String DESC_DIVORCE_UNCONTESTED_FACTOR = "Fraction of the waiting period an UNCONTESTED filing serves. 0.5 means an unanswered divorce completes in half the time a contested one does.";
    private static final String DESC_REMARRY_COOLDOWN = "Seconds after a divorce before either former partner may marry again. Set 0 to allow it immediately.";

    private static final String DESC_ENABLE_RENEW = "Master switch for renewing vows - a second ceremony granting bond XP, resetting the decay clock, upgrading the band and reopening the vow choice.";
    private static final String DESC_RENEW_MIN_DAYS = "Days a couple must be married (or since their last renewal) before they may renew.";
    private static final String DESC_RENEW_BOND_XP = "Bond XP granted for renewing vows.";
    private static final String DESC_RENEW_OFFER_SECONDS = "Seconds a renewal offer waits for the other partner to answer with their own.";

    private static final String DESC_ANNIVERSARY_REWARD_1 = "Item given to both partners at their 1-day anniversary. Leave blank for none.";
    private static final String DESC_ANNIVERSARY_REWARD_7 = "Item given to both partners at their 7-day anniversary. Leave blank for none.";
    private static final String DESC_ANNIVERSARY_REWARD_30 = "Item given to both partners at their 30-day anniversary. Leave blank for none.";
    private static final String DESC_ANNIVERSARY_REWARD_365 = "Item given to both partners at their 365-day anniversary. Leave blank for none.";
    private static final String DESC_ANNIVERSARY_REWARD_QUANTITY = "How many of the anniversary item each partner gets.";

    private static final String DESC_ENABLE_EMOTES = "Master switch for couple emotes - /marry hug, kiss and dance.";
    private static final String DESC_EMOTE_RANGE = "How close (in blocks) partners must be, in the same world, to emote at each other.";
    private static final String DESC_EMOTE_BROADCAST_RADIUS = "How far (in blocks) an emote is announced to onlookers. Set 0 to tell only the couple.";
    private static final String DESC_EMOTE_COOLDOWN = "Seconds between emotes per player.";
    private static final String DESC_EMOTE_HUG_PARTICLE = "Particle system spawned by /marry hug. Blank for none; an unknown id is skipped.";
    private static final String DESC_EMOTE_KISS_PARTICLE = "Particle system spawned by /marry kiss. Blank for none; an unknown id is skipped.";
    private static final String DESC_EMOTE_DANCE_PARTICLE = "Particle system spawned by /marry dance. Blank for none; an unknown id is skipped.";
    private static final String DESC_EMOTE_HUG_SOUND = "Sound event played by /marry hug. Blank for none; an unknown id is skipped.";
    private static final String DESC_EMOTE_KISS_SOUND = "Sound event played by /marry kiss. Blank for none; an unknown id is skipped.";
    private static final String DESC_EMOTE_DANCE_SOUND = "Sound event played by /marry dance. Blank for none; an unknown id is skipped.";

    public static final BuilderCodec<MarriageConfig> CODEC = BuilderCodec.builder(MarriageConfig.class, MarriageConfig::new)
            .append(new KeyedCodec<String>("Config-Information", Codec.STRING),
                    (config, value) -> config.Information = value,
                    (config) -> config.Information)
            .add()
            .append(new KeyedCodec<Integer>("ConfigVersion", Codec.INTEGER),
                    (config, value) -> config.ConfigVersion = value,
                    (config) -> config.ConfigVersion)
            .add()
            .append(new KeyedCodec<String>("PluginName", Codec.STRING),
                    (config, value) -> config.PluginName = value,
                    (config) -> config.PluginName)
            .add()
            .append(new KeyedCodec<String>("Version", Codec.STRING),
                    (config, value) -> config.Version = value,
                    (config) -> config.Version)
            .add()
            .append(new KeyedCodec<String>("Website", Codec.STRING),
                    (config, value) -> config.Website = value,
                    (config) -> config.Website)
            .add()
            .append(new KeyedCodec<String>("Download-Site", Codec.STRING),
                    (config, value) -> config.DownloadSite = value,
                    (config) -> config.DownloadSite)
            .add()
            .append(new KeyedCodec<Boolean>("New-Version-Message", Codec.BOOLEAN),
                    (config, value) -> config.NewVersion = value,
                    (config) -> config.NewVersion)
            .add()
            .append(new KeyedCodec<String>("Description-New-Version-Message", Codec.STRING),
                    (config, value) -> config.NewVersionDesc = value,
                    (config) -> config.NewVersionDesc)
            .add()

            // === General ===
            .append(new KeyedCodec<String>(" ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Require-Ring-To-Marry", Codec.BOOLEAN),
                    (config, value) -> config.RequireRing = value,
                    (config) -> config.RequireRing)
            .add()
            .append(new KeyedCodec<String>("Description-Require-Ring-To-Marry", Codec.STRING),
                    (config, value) -> config.RequireRingDesc = value,
                    (config) -> config.RequireRingDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Consume-Ring-On-Marry", Codec.BOOLEAN),
                    (config, value) -> config.ConsumeRing = value,
                    (config) -> config.ConsumeRing)
            .add()
            .append(new KeyedCodec<String>("Description-Consume-Ring-On-Marry", Codec.STRING),
                    (config, value) -> config.ConsumeRingDesc = value,
                    (config) -> config.ConsumeRingDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Use-Permissions-For-Commands", Codec.BOOLEAN),
                    (config, value) -> config.CmdPermission = value,
                    (config) -> config.CmdPermission)
            .add()
            .append(new KeyedCodec<String>("Description-Use-Permissions-For-Commands", Codec.STRING),
                    (config, value) -> config.CmdPermissionDesc = value,
                    (config) -> config.CmdPermissionDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Announce-Marriages", Codec.BOOLEAN),
                    (config, value) -> config.AnnounceMarriages = value,
                    (config) -> config.AnnounceMarriages)
            .add()
            .append(new KeyedCodec<String>("Description-Announce-Marriages", Codec.STRING),
                    (config, value) -> config.AnnounceMarriagesDesc = value,
                    (config) -> config.AnnounceMarriagesDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Announce-Divorces", Codec.BOOLEAN),
                    (config, value) -> config.AnnounceDivorces = value,
                    (config) -> config.AnnounceDivorces)
            .add()
            .append(new KeyedCodec<String>("Description-Announce-Divorces", Codec.STRING),
                    (config, value) -> config.AnnounceDivorcesDesc = value,
                    (config) -> config.AnnounceDivorcesDesc)
            .add()

            // === Requests & divorce ===
            .append(new KeyedCodec<String>("  ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Integer>("Request-Expiry-Seconds", Codec.INTEGER),
                    (config, value) -> config.RequestExpirySeconds = value,
                    (config) -> config.RequestExpirySeconds)
            .add()
            .append(new KeyedCodec<String>("Description-Request-Expiry-Seconds", Codec.STRING),
                    (config, value) -> config.RequestExpiryDesc = value,
                    (config) -> config.RequestExpiryDesc)
            .add()
            .append(new KeyedCodec<Integer>("Divorce-Confirm-Seconds", Codec.INTEGER),
                    (config, value) -> config.DivorceConfirmSeconds = value,
                    (config) -> config.DivorceConfirmSeconds)
            .add()
            .append(new KeyedCodec<String>("Description-Divorce-Confirm-Seconds", Codec.STRING),
                    (config, value) -> config.DivorceConfirmDesc = value,
                    (config) -> config.DivorceConfirmDesc)
            .add()

            // === Menu ===
            .append(new KeyedCodec<String>("   ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Menu", Codec.BOOLEAN),
                    (config, value) -> config.EnableMenu = value,
                    (config) -> config.EnableMenu)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Menu", Codec.STRING),
                    (config, value) -> config.EnableMenuDesc = value,
                    (config) -> config.EnableMenuDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Grant-Menu-Permission", Codec.BOOLEAN),
                    (config, value) -> config.GrantMenuPermission = value,
                    (config) -> config.GrantMenuPermission)
            .add()
            .append(new KeyedCodec<String>("Description-Grant-Menu-Permission", Codec.STRING),
                    (config, value) -> config.GrantMenuPermissionDesc = value,
                    (config) -> config.GrantMenuPermissionDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Granted-Menu-Permission", Codec.BOOLEAN),
                    (config, value) -> config.GrantedMenuPermission = value,
                    (config) -> config.GrantedMenuPermission)
            .add()
            .append(new KeyedCodec<String>("Description-Granted-Menu-Permission", Codec.STRING),
                    (config, value) -> config.GrantedMenuPermissionDesc = value,
                    (config) -> config.GrantedMenuPermissionDesc)
            .add()

            // === PvP protection ===
            .append(new KeyedCodec<String>("    ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Partner-Pvp-Protection", Codec.BOOLEAN),
                    (config, value) -> config.PartnerPvpProtection = value,
                    (config) -> config.PartnerPvpProtection)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Partner-Pvp-Protection", Codec.STRING),
                    (config, value) -> config.PartnerPvpProtectionDesc = value,
                    (config) -> config.PartnerPvpProtectionDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Default-Pvp-Protection", Codec.BOOLEAN),
                    (config, value) -> config.DefaultPvpProtection = value,
                    (config) -> config.DefaultPvpProtection)
            .add()
            .append(new KeyedCodec<String>("Description-Default-Pvp-Protection", Codec.STRING),
                    (config, value) -> config.DefaultPvpProtectionDesc = value,
                    (config) -> config.DefaultPvpProtectionDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Pvp-Protection-Message", Codec.BOOLEAN),
                    (config, value) -> config.PvpProtectionMessage = value,
                    (config) -> config.PvpProtectionMessage)
            .add()
            .append(new KeyedCodec<String>("Description-Pvp-Protection-Message", Codec.STRING),
                    (config, value) -> config.PvpProtectionMessageDesc = value,
                    (config) -> config.PvpProtectionMessageDesc)
            .add()

            // === Partner regeneration ===
            .append(new KeyedCodec<String>("     ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Partner-Regen", Codec.BOOLEAN),
                    (config, value) -> config.PartnerRegen = value,
                    (config) -> config.PartnerRegen)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Partner-Regen", Codec.STRING),
                    (config, value) -> config.PartnerRegenDesc = value,
                    (config) -> config.PartnerRegenDesc)
            .add()
            .append(new KeyedCodec<Double>("Regen-Radius-Blocks", Codec.DOUBLE),
                    (config, value) -> config.RegenRadiusBlocks = value,
                    (config) -> config.RegenRadiusBlocks)
            .add()
            .append(new KeyedCodec<String>("Description-Regen-Radius-Blocks", Codec.STRING),
                    (config, value) -> config.RegenRadiusDesc = value,
                    (config) -> config.RegenRadiusDesc)
            .add()
            .append(new KeyedCodec<Double>("Regen-Interval-Seconds", Codec.DOUBLE),
                    (config, value) -> config.RegenIntervalSeconds = value,
                    (config) -> config.RegenIntervalSeconds)
            .add()
            .append(new KeyedCodec<String>("Description-Regen-Interval-Seconds", Codec.STRING),
                    (config, value) -> config.RegenIntervalDesc = value,
                    (config) -> config.RegenIntervalDesc)
            .add()
            .append(new KeyedCodec<Double>("Regen-Amount-Per-Interval", Codec.DOUBLE),
                    (config, value) -> config.RegenAmountPerInterval = value,
                    (config) -> config.RegenAmountPerInterval)
            .add()
            .append(new KeyedCodec<String>("Description-Regen-Amount-Per-Interval", Codec.STRING),
                    (config, value) -> config.RegenAmountDesc = value,
                    (config) -> config.RegenAmountDesc)
            .add()

            // === Gifts ===
            .append(new KeyedCodec<String>("      ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Gift", Codec.BOOLEAN),
                    (config, value) -> config.EnableGift = value,
                    (config) -> config.EnableGift)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Gift", Codec.STRING),
                    (config, value) -> config.EnableGiftDesc = value,
                    (config) -> config.EnableGiftDesc)
            .add()
            .append(new KeyedCodec<Double>("Gift-Range-Blocks", Codec.DOUBLE),
                    (config, value) -> config.GiftRangeBlocks = value,
                    (config) -> config.GiftRangeBlocks)
            .add()
            .append(new KeyedCodec<String>("Description-Gift-Range-Blocks", Codec.STRING),
                    (config, value) -> config.GiftRangeDesc = value,
                    (config) -> config.GiftRangeDesc)
            .add()
            .append(new KeyedCodec<Integer>("Gift-Cooldown-Seconds", Codec.INTEGER),
                    (config, value) -> config.GiftCooldownSeconds = value,
                    (config) -> config.GiftCooldownSeconds)
            .add()
            .append(new KeyedCodec<String>("Description-Gift-Cooldown-Seconds", Codec.STRING),
                    (config, value) -> config.GiftCooldownDesc = value,
                    (config) -> config.GiftCooldownDesc)
            .add()

            // === Teleport & messages ===
            .append(new KeyedCodec<String>("       ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Partner-Tp", Codec.BOOLEAN),
                    (config, value) -> config.EnableTp = value,
                    (config) -> config.EnableTp)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Partner-Tp", Codec.STRING),
                    (config, value) -> config.EnableTpDesc = value,
                    (config) -> config.EnableTpDesc)
            .add()
            .append(new KeyedCodec<Integer>("Tp-Cooldown-Seconds", Codec.INTEGER),
                    (config, value) -> config.TpCooldownSeconds = value,
                    (config) -> config.TpCooldownSeconds)
            .add()
            .append(new KeyedCodec<String>("Description-Tp-Cooldown-Seconds", Codec.STRING),
                    (config, value) -> config.TpCooldownDesc = value,
                    (config) -> config.TpCooldownDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Partner-Msg", Codec.BOOLEAN),
                    (config, value) -> config.EnableMsg = value,
                    (config) -> config.EnableMsg)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Partner-Msg", Codec.STRING),
                    (config, value) -> config.EnableMsgDesc = value,
                    (config) -> config.EnableMsgDesc)
            .add()

            // === Bond ===
            .append(new KeyedCodec<String>("        ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Bond", Codec.BOOLEAN),
                    (config, value) -> config.EnableBond = value,
                    (config) -> config.EnableBond)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Bond", Codec.STRING),
                    (config, value) -> config.EnableBondDesc = value,
                    (config) -> config.EnableBondDesc)
            .add()
            .append(new KeyedCodec<Integer>("Bond-Max-Level", Codec.INTEGER),
                    (config, value) -> config.BondMaxLevel = value,
                    (config) -> config.BondMaxLevel)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Max-Level", Codec.STRING),
                    (config, value) -> config.BondMaxLevelDesc = value,
                    (config) -> config.BondMaxLevelDesc)
            .add()
            .append(new KeyedCodec<Double>("Bond-Xp-Per-Level", Codec.DOUBLE),
                    (config, value) -> config.BondXpPerLevel = value,
                    (config) -> config.BondXpPerLevel)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Xp-Per-Level", Codec.STRING),
                    (config, value) -> config.BondXpPerLevelDesc = value,
                    (config) -> config.BondXpPerLevelDesc)
            .add()
            .append(new KeyedCodec<Double>("Bond-Xp-Per-Hour-Married", Codec.DOUBLE),
                    (config, value) -> config.BondXpPerHourMarried = value,
                    (config) -> config.BondXpPerHourMarried)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Xp-Per-Hour-Married", Codec.STRING),
                    (config, value) -> config.BondXpPerHourDesc = value,
                    (config) -> config.BondXpPerHourDesc)
            .add()
            .append(new KeyedCodec<Double>("Bond-Xp-Per-Minute-Together", Codec.DOUBLE),
                    (config, value) -> config.BondXpPerMinuteTogether = value,
                    (config) -> config.BondXpPerMinuteTogether)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Xp-Per-Minute-Together", Codec.STRING),
                    (config, value) -> config.BondXpPerMinuteTogetherDesc = value,
                    (config) -> config.BondXpPerMinuteTogetherDesc)
            .add()
            .append(new KeyedCodec<Double>("Bond-Range-Blocks", Codec.DOUBLE),
                    (config, value) -> config.BondRangeBlocks = value,
                    (config) -> config.BondRangeBlocks)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Range-Blocks", Codec.STRING),
                    (config, value) -> config.BondRangeDesc = value,
                    (config) -> config.BondRangeDesc)
            .add()
            .append(new KeyedCodec<Double>("Bond-Xp-Per-Gift", Codec.DOUBLE),
                    (config, value) -> config.BondXpPerGift = value,
                    (config) -> config.BondXpPerGift)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Xp-Per-Gift", Codec.STRING),
                    (config, value) -> config.BondXpPerGiftDesc = value,
                    (config) -> config.BondXpPerGiftDesc)
            .add()
            .append(new KeyedCodec<Double>("Bond-Xp-Per-Coop-Kill", Codec.DOUBLE),
                    (config, value) -> config.BondXpPerCoopKill = value,
                    (config) -> config.BondXpPerCoopKill)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Xp-Per-Coop-Kill", Codec.STRING),
                    (config, value) -> config.BondXpPerCoopKillDesc = value,
                    (config) -> config.BondXpPerCoopKillDesc)
            .add()
            .append(new KeyedCodec<Double>("Bond-Regen-Radius-Percent-Per-Level", Codec.DOUBLE),
                    (config, value) -> config.BondRegenRadiusPercentPerLevel = value,
                    (config) -> config.BondRegenRadiusPercentPerLevel)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Regen-Radius-Percent-Per-Level", Codec.STRING),
                    (config, value) -> config.BondRegenRadiusPerLevelDesc = value,
                    (config) -> config.BondRegenRadiusPerLevelDesc)
            .add()
            .append(new KeyedCodec<Double>("Bond-Tp-Cooldown-Percent-Per-Level", Codec.DOUBLE),
                    (config, value) -> config.BondTpCooldownPercentPerLevel = value,
                    (config) -> config.BondTpCooldownPercentPerLevel)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Tp-Cooldown-Percent-Per-Level", Codec.STRING),
                    (config, value) -> config.BondTpCooldownPerLevelDesc = value,
                    (config) -> config.BondTpCooldownPerLevelDesc)
            .add()

            // === Anniversaries ===
            .append(new KeyedCodec<String>("         ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Anniversaries", Codec.BOOLEAN),
                    (config, value) -> config.EnableAnniversaries = value,
                    (config) -> config.EnableAnniversaries)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Anniversaries", Codec.STRING),
                    (config, value) -> config.EnableAnniversariesDesc = value,
                    (config) -> config.EnableAnniversariesDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Announce-Anniversaries", Codec.BOOLEAN),
                    (config, value) -> config.AnnounceAnniversaries = value,
                    (config) -> config.AnnounceAnniversaries)
            .add()
            .append(new KeyedCodec<String>("Description-Announce-Anniversaries", Codec.STRING),
                    (config, value) -> config.AnnounceAnniversariesDesc = value,
                    (config) -> config.AnnounceAnniversariesDesc)
            .add()
            .append(new KeyedCodec<Double>("Anniversary-Bond-Xp", Codec.DOUBLE),
                    (config, value) -> config.AnniversaryBondXp = value,
                    (config) -> config.AnniversaryBondXp)
            .add()
            .append(new KeyedCodec<String>("Description-Anniversary-Bond-Xp", Codec.STRING),
                    (config, value) -> config.AnniversaryBondXpDesc = value,
                    (config) -> config.AnniversaryBondXpDesc)
            .add()

            // === Bond decay ===
            .append(new KeyedCodec<String>("          ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Bond-Decay", Codec.BOOLEAN),
                    (config, value) -> config.EnableBondDecay = value,
                    (config) -> config.EnableBondDecay)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Bond-Decay", Codec.STRING),
                    (config, value) -> config.EnableBondDecayDesc = value,
                    (config) -> config.EnableBondDecayDesc)
            .add()
            .append(new KeyedCodec<Double>("Bond-Decay-Grace-Days", Codec.DOUBLE),
                    (config, value) -> config.BondDecayGraceDays = value,
                    (config) -> config.BondDecayGraceDays)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Decay-Grace-Days", Codec.STRING),
                    (config, value) -> config.BondDecayGraceDaysDesc = value,
                    (config) -> config.BondDecayGraceDaysDesc)
            .add()
            .append(new KeyedCodec<Double>("Bond-Decay-Xp-Per-Day", Codec.DOUBLE),
                    (config, value) -> config.BondDecayXpPerDay = value,
                    (config) -> config.BondDecayXpPerDay)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Decay-Xp-Per-Day", Codec.STRING),
                    (config, value) -> config.BondDecayXpPerDayDesc = value,
                    (config) -> config.BondDecayXpPerDayDesc)
            .add()
            .append(new KeyedCodec<Integer>("Bond-Decay-Min-Level", Codec.INTEGER),
                    (config, value) -> config.BondDecayMinLevel = value,
                    (config) -> config.BondDecayMinLevel)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Decay-Min-Level", Codec.STRING),
                    (config, value) -> config.BondDecayMinLevelDesc = value,
                    (config) -> config.BondDecayMinLevelDesc)
            .add()

            // === Perk: shared home ===
            .append(new KeyedCodec<String>("           ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Shared-Home", Codec.BOOLEAN),
                    (config, value) -> config.EnableSharedHome = value,
                    (config) -> config.EnableSharedHome)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Shared-Home", Codec.STRING),
                    (config, value) -> config.EnableSharedHomeDesc = value,
                    (config) -> config.EnableSharedHomeDesc)
            .add()
            .append(new KeyedCodec<Integer>("Shared-Home-Bond-Level", Codec.INTEGER),
                    (config, value) -> config.SharedHomeBondLevel = value,
                    (config) -> config.SharedHomeBondLevel)
            .add()
            .append(new KeyedCodec<String>("Description-Shared-Home-Bond-Level", Codec.STRING),
                    (config, value) -> config.SharedHomeBondLevelDesc = value,
                    (config) -> config.SharedHomeBondLevelDesc)
            .add()
            .append(new KeyedCodec<Integer>("Home-Cooldown-Seconds", Codec.INTEGER),
                    (config, value) -> config.HomeCooldownSeconds = value,
                    (config) -> config.HomeCooldownSeconds)
            .add()
            .append(new KeyedCodec<String>("Description-Home-Cooldown-Seconds", Codec.STRING),
                    (config, value) -> config.HomeCooldownDesc = value,
                    (config) -> config.HomeCooldownDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Show-Home-On-Map", Codec.BOOLEAN),
                    (config, value) -> config.ShowHomeOnMap = value,
                    (config) -> config.ShowHomeOnMap)
            .add()
            .append(new KeyedCodec<String>("Description-Show-Home-On-Map", Codec.STRING),
                    (config, value) -> config.ShowHomeOnMapDesc = value,
                    (config) -> config.ShowHomeOnMapDesc)
            .add()

            // === Perk: partner marker ===
            .append(new KeyedCodec<String>("            ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Partner-Marker", Codec.BOOLEAN),
                    (config, value) -> config.EnablePartnerMarker = value,
                    (config) -> config.EnablePartnerMarker)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Partner-Marker", Codec.STRING),
                    (config, value) -> config.EnablePartnerMarkerDesc = value,
                    (config) -> config.EnablePartnerMarkerDesc)
            .add()
            .append(new KeyedCodec<Integer>("Partner-Marker-Bond-Level", Codec.INTEGER),
                    (config, value) -> config.PartnerMarkerBondLevel = value,
                    (config) -> config.PartnerMarkerBondLevel)
            .add()
            .append(new KeyedCodec<String>("Description-Partner-Marker-Bond-Level", Codec.STRING),
                    (config, value) -> config.PartnerMarkerBondLevelDesc = value,
                    (config) -> config.PartnerMarkerBondLevelDesc)
            .add()

            // === Perk: death alert ===
            .append(new KeyedCodec<String>("             ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Death-Alert", Codec.BOOLEAN),
                    (config, value) -> config.EnableDeathAlert = value,
                    (config) -> config.EnableDeathAlert)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Death-Alert", Codec.STRING),
                    (config, value) -> config.EnableDeathAlertDesc = value,
                    (config) -> config.EnableDeathAlertDesc)
            .add()
            .append(new KeyedCodec<Integer>("Death-Alert-Bond-Level", Codec.INTEGER),
                    (config, value) -> config.DeathAlertBondLevel = value,
                    (config) -> config.DeathAlertBondLevel)
            .add()
            .append(new KeyedCodec<String>("Description-Death-Alert-Bond-Level", Codec.STRING),
                    (config, value) -> config.DeathAlertBondLevelDesc = value,
                    (config) -> config.DeathAlertBondLevelDesc)
            .add()
            .append(new KeyedCodec<Double>("Death-Marker-Minutes", Codec.DOUBLE),
                    (config, value) -> config.DeathMarkerMinutes = value,
                    (config) -> config.DeathMarkerMinutes)
            .add()
            .append(new KeyedCodec<String>("Description-Death-Marker-Minutes", Codec.STRING),
                    (config, value) -> config.DeathMarkerMinutesDesc = value,
                    (config) -> config.DeathMarkerMinutesDesc)
            .add()

            // === Perk: hope chest ===
            .append(new KeyedCodec<String>("              ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Hope-Chest", Codec.BOOLEAN),
                    (config, value) -> config.EnableHopeChest = value,
                    (config) -> config.EnableHopeChest)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Hope-Chest", Codec.STRING),
                    (config, value) -> config.EnableHopeChestDesc = value,
                    (config) -> config.EnableHopeChestDesc)
            .add()
            .append(new KeyedCodec<Integer>("Hope-Chest-Bond-Level", Codec.INTEGER),
                    (config, value) -> config.HopeChestBondLevel = value,
                    (config) -> config.HopeChestBondLevel)
            .add()
            .append(new KeyedCodec<String>("Description-Hope-Chest-Bond-Level", Codec.STRING),
                    (config, value) -> config.HopeChestBondLevelDesc = value,
                    (config) -> config.HopeChestBondLevelDesc)
            .add()
            .append(new KeyedCodec<Integer>("Hope-Chest-Slots", Codec.INTEGER),
                    (config, value) -> config.HopeChestSlots = value,
                    (config) -> config.HopeChestSlots)
            .add()
            .append(new KeyedCodec<String>("Description-Hope-Chest-Slots", Codec.STRING),
                    (config, value) -> config.HopeChestSlotsDesc = value,
                    (config) -> config.HopeChestSlotsDesc)
            .add()

            // === Perk: bond of pain ===
            .append(new KeyedCodec<String>("               ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Bond-Of-Pain", Codec.BOOLEAN),
                    (config, value) -> config.EnableBondOfPain = value,
                    (config) -> config.EnableBondOfPain)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Bond-Of-Pain", Codec.STRING),
                    (config, value) -> config.EnableBondOfPainDesc = value,
                    (config) -> config.EnableBondOfPainDesc)
            .add()
            .append(new KeyedCodec<Integer>("Bond-Of-Pain-Bond-Level", Codec.INTEGER),
                    (config, value) -> config.BondOfPainBondLevel = value,
                    (config) -> config.BondOfPainBondLevel)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Of-Pain-Bond-Level", Codec.STRING),
                    (config, value) -> config.BondOfPainBondLevelDesc = value,
                    (config) -> config.BondOfPainBondLevelDesc)
            .add()
            .append(new KeyedCodec<Double>("Bond-Of-Pain-Share-Percent", Codec.DOUBLE),
                    (config, value) -> config.BondOfPainSharePercent = value,
                    (config) -> config.BondOfPainSharePercent)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Of-Pain-Share-Percent", Codec.STRING),
                    (config, value) -> config.BondOfPainShareDesc = value,
                    (config) -> config.BondOfPainShareDesc)
            .add()
            .append(new KeyedCodec<Double>("Bond-Of-Pain-Range-Blocks", Codec.DOUBLE),
                    (config, value) -> config.BondOfPainRangeBlocks = value,
                    (config) -> config.BondOfPainRangeBlocks)
            .add()
            .append(new KeyedCodec<String>("Description-Bond-Of-Pain-Range-Blocks", Codec.STRING),
                    (config, value) -> config.BondOfPainRangeDesc = value,
                    (config) -> config.BondOfPainRangeDesc)
            .add()

            // === Perk: last stand ===
            .append(new KeyedCodec<String>("                ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Last-Stand", Codec.BOOLEAN),
                    (config, value) -> config.EnableLastStand = value,
                    (config) -> config.EnableLastStand)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Last-Stand", Codec.STRING),
                    (config, value) -> config.EnableLastStandDesc = value,
                    (config) -> config.EnableLastStandDesc)
            .add()
            .append(new KeyedCodec<Integer>("Last-Stand-Bond-Level", Codec.INTEGER),
                    (config, value) -> config.LastStandBondLevel = value,
                    (config) -> config.LastStandBondLevel)
            .add()
            .append(new KeyedCodec<String>("Description-Last-Stand-Bond-Level", Codec.STRING),
                    (config, value) -> config.LastStandBondLevelDesc = value,
                    (config) -> config.LastStandBondLevelDesc)
            .add()
            .append(new KeyedCodec<String>("Last-Stand-Effect", Codec.STRING),
                    (config, value) -> config.LastStandEffect = value,
                    (config) -> config.LastStandEffect)
            .add()
            .append(new KeyedCodec<String>("Description-Last-Stand-Effect", Codec.STRING),
                    (config, value) -> config.LastStandEffectDesc = value,
                    (config) -> config.LastStandEffectDesc)
            .add()
            .append(new KeyedCodec<Double>("Last-Stand-Duration-Seconds", Codec.DOUBLE),
                    (config, value) -> config.LastStandDurationSeconds = value,
                    (config) -> config.LastStandDurationSeconds)
            .add()
            .append(new KeyedCodec<String>("Description-Last-Stand-Duration-Seconds", Codec.STRING),
                    (config, value) -> config.LastStandDurationDesc = value,
                    (config) -> config.LastStandDurationDesc)
            .add()

            // === Perk: respawn at partner ===
            .append(new KeyedCodec<String>("                 ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Respawn-At-Partner", Codec.BOOLEAN),
                    (config, value) -> config.EnableRespawnAtPartner = value,
                    (config) -> config.EnableRespawnAtPartner)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Respawn-At-Partner", Codec.STRING),
                    (config, value) -> config.EnableRespawnAtPartnerDesc = value,
                    (config) -> config.EnableRespawnAtPartnerDesc)
            .add()
            .append(new KeyedCodec<Integer>("Respawn-At-Partner-Bond-Level", Codec.INTEGER),
                    (config, value) -> config.RespawnAtPartnerBondLevel = value,
                    (config) -> config.RespawnAtPartnerBondLevel)
            .add()
            .append(new KeyedCodec<String>("Description-Respawn-At-Partner-Bond-Level", Codec.STRING),
                    (config, value) -> config.RespawnAtPartnerBondLevelDesc = value,
                    (config) -> config.RespawnAtPartnerBondLevelDesc)
            .add()
            .append(new KeyedCodec<Integer>("Respawn-At-Partner-Cooldown-Seconds", Codec.INTEGER),
                    (config, value) -> config.RespawnAtPartnerCooldownSeconds = value,
                    (config) -> config.RespawnAtPartnerCooldownSeconds)
            .add()
            .append(new KeyedCodec<String>("Description-Respawn-At-Partner-Cooldown-Seconds", Codec.STRING),
                    (config, value) -> config.RespawnAtPartnerCooldownDesc = value,
                    (config) -> config.RespawnAtPartnerCooldownDesc)
            .add()

            // === Couple channel, letters, HUD, records ===
            .append(new KeyedCodec<String>("                  ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Couple-Channel", Codec.BOOLEAN),
                    (config, value) -> config.EnableCoupleChannel = value,
                    (config) -> config.EnableCoupleChannel)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Couple-Channel", Codec.STRING),
                    (config, value) -> config.EnableCoupleChannelDesc = value,
                    (config) -> config.EnableCoupleChannelDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Love-Letters", Codec.BOOLEAN),
                    (config, value) -> config.EnableLoveLetters = value,
                    (config) -> config.EnableLoveLetters)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Love-Letters", Codec.STRING),
                    (config, value) -> config.EnableLoveLettersDesc = value,
                    (config) -> config.EnableLoveLettersDesc)
            .add()
            .append(new KeyedCodec<Integer>("Love-Letter-Max-Length", Codec.INTEGER),
                    (config, value) -> config.LoveLetterMaxLength = value,
                    (config) -> config.LoveLetterMaxLength)
            .add()
            .append(new KeyedCodec<String>("Description-Love-Letter-Max-Length", Codec.STRING),
                    (config, value) -> config.LoveLetterMaxLengthDesc = value,
                    (config) -> config.LoveLetterMaxLengthDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Partner-Hud", Codec.BOOLEAN),
                    (config, value) -> config.EnablePartnerHud = value,
                    (config) -> config.EnablePartnerHud)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Partner-Hud", Codec.STRING),
                    (config, value) -> config.EnablePartnerHudDesc = value,
                    (config) -> config.EnablePartnerHudDesc)
            .add()
            .append(new KeyedCodec<Double>("Partner-Hud-Range-Blocks", Codec.DOUBLE),
                    (config, value) -> config.PartnerHudRangeBlocks = value,
                    (config) -> config.PartnerHudRangeBlocks)
            .add()
            .append(new KeyedCodec<String>("Description-Partner-Hud-Range-Blocks", Codec.STRING),
                    (config, value) -> config.PartnerHudRangeDesc = value,
                    (config) -> config.PartnerHudRangeDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Records", Codec.BOOLEAN),
                    (config, value) -> config.EnableRecords = value,
                    (config) -> config.EnableRecords)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Records", Codec.STRING),
                    (config, value) -> config.EnableRecordsDesc = value,
                    (config) -> config.EnableRecordsDesc)
            .add()

            // === Ring tiers ===
            .append(new KeyedCodec<String>("                   ", Codec.STRING),
                    (config, value) -> config.Space = value,
                    (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Ring-Tiers", Codec.BOOLEAN),
                    (config, value) -> config.EnableRingTiers = value,
                    (config) -> config.EnableRingTiers)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Ring-Tiers", Codec.STRING),
                    (config, value) -> config.EnableRingTiersDesc = value,
                    (config) -> config.EnableRingTiersDesc)
            .add()
            .append(new KeyedCodec<Boolean>("Engrave-Rings", Codec.BOOLEAN),
                    (config, value) -> config.EngraveRings = value,
                    (config) -> config.EngraveRings)
            .add()
            .append(new KeyedCodec<String>("Description-Engrave-Rings", Codec.STRING),
                    (config, value) -> config.EngraveRingsDesc = value,
                    (config) -> config.EngraveRingsDesc)
            .add()
            .append(new KeyedCodec<Double>("Gold-Ring-Xp-Multiplier", Codec.DOUBLE),
                    (config, value) -> config.GoldRingXpMultiplier = value,
                    (config) -> config.GoldRingXpMultiplier)
            .add()
            .append(new KeyedCodec<String>("Description-Gold-Ring-Xp-Multiplier", Codec.STRING),
                    (config, value) -> config.GoldRingXpMultiplierDesc = value,
                    (config) -> config.GoldRingXpMultiplierDesc)
            .add()
            .append(new KeyedCodec<Integer>("Gold-Ring-Perk-Discount", Codec.INTEGER),
                    (config, value) -> config.GoldRingPerkDiscount = value,
                    (config) -> config.GoldRingPerkDiscount)
            .add()
            .append(new KeyedCodec<String>("Description-Gold-Ring-Perk-Discount", Codec.STRING),
                    (config, value) -> config.GoldRingPerkDiscountDesc = value,
                    (config) -> config.GoldRingPerkDiscountDesc)
            .add()
            .append(new KeyedCodec<Double>("Diamond-Ring-Xp-Multiplier", Codec.DOUBLE),
                    (config, value) -> config.DiamondRingXpMultiplier = value,
                    (config) -> config.DiamondRingXpMultiplier)
            .add()
            .append(new KeyedCodec<String>("Description-Diamond-Ring-Xp-Multiplier", Codec.STRING),
                    (config, value) -> config.DiamondRingXpMultiplierDesc = value,
                    (config) -> config.DiamondRingXpMultiplierDesc)
            .add()
            .append(new KeyedCodec<Integer>("Diamond-Ring-Perk-Discount", Codec.INTEGER),
                    (config, value) -> config.DiamondRingPerkDiscount = value,
                    (config) -> config.DiamondRingPerkDiscount)
            .add()
            .append(new KeyedCodec<String>("Description-Diamond-Ring-Perk-Discount", Codec.STRING),
                    (config, value) -> config.DiamondRingPerkDiscountDesc = value,
                    (config) -> config.DiamondRingPerkDiscountDesc)
            .add()

            // === Vows ===
            .append(new KeyedCodec<String>("                    ", Codec.STRING),
                    (config, value) -> config.Space = value, (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Vows", Codec.BOOLEAN),
                    (config, value) -> config.EnableVows = value, (config) -> config.EnableVows)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Vows", Codec.STRING),
                    (config, value) -> config.EnableVowsDesc = value, (config) -> config.EnableVowsDesc)
            .add()
            .append(new KeyedCodec<Integer>("Vow-Perk-Discount", Codec.INTEGER),
                    (config, value) -> config.VowPerkDiscount = value, (config) -> config.VowPerkDiscount)
            .add()
            .append(new KeyedCodec<String>("Description-Vow-Perk-Discount", Codec.STRING),
                    (config, value) -> config.VowPerkDiscountDesc = value, (config) -> config.VowPerkDiscountDesc)
            .add()
            .append(new KeyedCodec<Integer>("Vow-Hearth-Chest-Bonus-Slots", Codec.INTEGER),
                    (config, value) -> config.VowHearthChestBonusSlots = value, (config) -> config.VowHearthChestBonusSlots)
            .add()
            .append(new KeyedCodec<String>("Description-Vow-Hearth-Chest-Bonus-Slots", Codec.STRING),
                    (config, value) -> config.VowHearthSlotsDesc = value, (config) -> config.VowHearthSlotsDesc)
            .add()
            .append(new KeyedCodec<Double>("Vow-Blade-Share-Bonus-Percent", Codec.DOUBLE),
                    (config, value) -> config.VowBladeShareBonusPercent = value, (config) -> config.VowBladeShareBonusPercent)
            .add()
            .append(new KeyedCodec<String>("Description-Vow-Blade-Share-Bonus-Percent", Codec.STRING),
                    (config, value) -> config.VowBladeShareDesc = value, (config) -> config.VowBladeShareDesc)
            .add()
            .append(new KeyedCodec<Double>("Vow-Wander-Cooldown-Cut-Percent", Codec.DOUBLE),
                    (config, value) -> config.VowWanderCooldownCutPercent = value, (config) -> config.VowWanderCooldownCutPercent)
            .add()
            .append(new KeyedCodec<String>("Description-Vow-Wander-Cooldown-Cut-Percent", Codec.STRING),
                    (config, value) -> config.VowWanderCutDesc = value, (config) -> config.VowWanderCutDesc)
            .add()

            // === Honeymoon ===
            .append(new KeyedCodec<String>("                     ", Codec.STRING),
                    (config, value) -> config.Space = value, (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Honeymoon", Codec.BOOLEAN),
                    (config, value) -> config.EnableHoneymoon = value, (config) -> config.EnableHoneymoon)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Honeymoon", Codec.STRING),
                    (config, value) -> config.EnableHoneymoonDesc = value, (config) -> config.EnableHoneymoonDesc)
            .add()
            .append(new KeyedCodec<Double>("Honeymoon-Hours", Codec.DOUBLE),
                    (config, value) -> config.HoneymoonHours = value, (config) -> config.HoneymoonHours)
            .add()
            .append(new KeyedCodec<String>("Description-Honeymoon-Hours", Codec.STRING),
                    (config, value) -> config.HoneymoonHoursDesc = value, (config) -> config.HoneymoonHoursDesc)
            .add()
            .append(new KeyedCodec<Double>("Honeymoon-Xp-Multiplier", Codec.DOUBLE),
                    (config, value) -> config.HoneymoonXpMultiplier = value, (config) -> config.HoneymoonXpMultiplier)
            .add()
            .append(new KeyedCodec<String>("Description-Honeymoon-Xp-Multiplier", Codec.STRING),
                    (config, value) -> config.HoneymoonMultiplierDesc = value, (config) -> config.HoneymoonMultiplierDesc)
            .add()

            // === Divorce weight ===
            .append(new KeyedCodec<String>("                      ", Codec.STRING),
                    (config, value) -> config.Space = value, (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Divorce-Waiting-Period", Codec.BOOLEAN),
                    (config, value) -> config.EnableDivorceWait = value, (config) -> config.EnableDivorceWait)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Divorce-Waiting-Period", Codec.STRING),
                    (config, value) -> config.EnableDivorceWaitDesc = value, (config) -> config.EnableDivorceWaitDesc)
            .add()
            .append(new KeyedCodec<Double>("Divorce-Waiting-Hours", Codec.DOUBLE),
                    (config, value) -> config.DivorceWaitingHours = value, (config) -> config.DivorceWaitingHours)
            .add()
            .append(new KeyedCodec<String>("Description-Divorce-Waiting-Hours", Codec.STRING),
                    (config, value) -> config.DivorceWaitingHoursDesc = value, (config) -> config.DivorceWaitingHoursDesc)
            .add()
            .append(new KeyedCodec<Double>("Divorce-Uncontested-Factor", Codec.DOUBLE),
                    (config, value) -> config.DivorceUncontestedFactor = value, (config) -> config.DivorceUncontestedFactor)
            .add()
            .append(new KeyedCodec<String>("Description-Divorce-Uncontested-Factor", Codec.STRING),
                    (config, value) -> config.DivorceUncontestedFactorDesc = value, (config) -> config.DivorceUncontestedFactorDesc)
            .add()
            .append(new KeyedCodec<Integer>("Remarry-Cooldown-Seconds", Codec.INTEGER),
                    (config, value) -> config.RemarryCooldownSeconds = value, (config) -> config.RemarryCooldownSeconds)
            .add()
            .append(new KeyedCodec<String>("Description-Remarry-Cooldown-Seconds", Codec.STRING),
                    (config, value) -> config.RemarryCooldownDesc = value, (config) -> config.RemarryCooldownDesc)
            .add()

            // === Renewing vows ===
            .append(new KeyedCodec<String>("                       ", Codec.STRING),
                    (config, value) -> config.Space = value, (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Renew-Vows", Codec.BOOLEAN),
                    (config, value) -> config.EnableRenew = value, (config) -> config.EnableRenew)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Renew-Vows", Codec.STRING),
                    (config, value) -> config.EnableRenewDesc = value, (config) -> config.EnableRenewDesc)
            .add()
            .append(new KeyedCodec<Double>("Renew-Min-Days", Codec.DOUBLE),
                    (config, value) -> config.RenewMinDays = value, (config) -> config.RenewMinDays)
            .add()
            .append(new KeyedCodec<String>("Description-Renew-Min-Days", Codec.STRING),
                    (config, value) -> config.RenewMinDaysDesc = value, (config) -> config.RenewMinDaysDesc)
            .add()
            .append(new KeyedCodec<Double>("Renew-Bond-Xp", Codec.DOUBLE),
                    (config, value) -> config.RenewBondXp = value, (config) -> config.RenewBondXp)
            .add()
            .append(new KeyedCodec<String>("Description-Renew-Bond-Xp", Codec.STRING),
                    (config, value) -> config.RenewBondXpDesc = value, (config) -> config.RenewBondXpDesc)
            .add()
            .append(new KeyedCodec<Integer>("Renew-Offer-Seconds", Codec.INTEGER),
                    (config, value) -> config.RenewOfferSeconds = value, (config) -> config.RenewOfferSeconds)
            .add()
            .append(new KeyedCodec<String>("Description-Renew-Offer-Seconds", Codec.STRING),
                    (config, value) -> config.RenewOfferSecondsDesc = value, (config) -> config.RenewOfferSecondsDesc)
            .add()

            // === Anniversary rewards ===
            .append(new KeyedCodec<String>("                        ", Codec.STRING),
                    (config, value) -> config.Space = value, (config) -> " ")
            .add()
            .append(new KeyedCodec<String>("Anniversary-Reward-Item-1-Day", Codec.STRING),
                    (config, value) -> config.AnniversaryReward1 = value, (config) -> config.AnniversaryReward1)
            .add()
            .append(new KeyedCodec<String>("Description-Anniversary-Reward-Item-1-Day", Codec.STRING),
                    (config, value) -> config.AnniversaryReward1Desc = value, (config) -> config.AnniversaryReward1Desc)
            .add()
            .append(new KeyedCodec<String>("Anniversary-Reward-Item-7-Day", Codec.STRING),
                    (config, value) -> config.AnniversaryReward7 = value, (config) -> config.AnniversaryReward7)
            .add()
            .append(new KeyedCodec<String>("Description-Anniversary-Reward-Item-7-Day", Codec.STRING),
                    (config, value) -> config.AnniversaryReward7Desc = value, (config) -> config.AnniversaryReward7Desc)
            .add()
            .append(new KeyedCodec<String>("Anniversary-Reward-Item-30-Day", Codec.STRING),
                    (config, value) -> config.AnniversaryReward30 = value, (config) -> config.AnniversaryReward30)
            .add()
            .append(new KeyedCodec<String>("Description-Anniversary-Reward-Item-30-Day", Codec.STRING),
                    (config, value) -> config.AnniversaryReward30Desc = value, (config) -> config.AnniversaryReward30Desc)
            .add()
            .append(new KeyedCodec<String>("Anniversary-Reward-Item-365-Day", Codec.STRING),
                    (config, value) -> config.AnniversaryReward365 = value, (config) -> config.AnniversaryReward365)
            .add()
            .append(new KeyedCodec<String>("Description-Anniversary-Reward-Item-365-Day", Codec.STRING),
                    (config, value) -> config.AnniversaryReward365Desc = value, (config) -> config.AnniversaryReward365Desc)
            .add()
            .append(new KeyedCodec<Integer>("Anniversary-Reward-Quantity", Codec.INTEGER),
                    (config, value) -> config.AnniversaryRewardQuantity = value, (config) -> config.AnniversaryRewardQuantity)
            .add()
            .append(new KeyedCodec<String>("Description-Anniversary-Reward-Quantity", Codec.STRING),
                    (config, value) -> config.AnniversaryRewardQuantityDesc = value, (config) -> config.AnniversaryRewardQuantityDesc)
            .add()

            // === Emotes ===
            .append(new KeyedCodec<String>("                         ", Codec.STRING),
                    (config, value) -> config.Space = value, (config) -> " ")
            .add()
            .append(new KeyedCodec<Boolean>("Enable-Emotes", Codec.BOOLEAN),
                    (config, value) -> config.EnableEmotes = value, (config) -> config.EnableEmotes)
            .add()
            .append(new KeyedCodec<String>("Description-Enable-Emotes", Codec.STRING),
                    (config, value) -> config.EnableEmotesDesc = value, (config) -> config.EnableEmotesDesc)
            .add()
            .append(new KeyedCodec<Double>("Emote-Range-Blocks", Codec.DOUBLE),
                    (config, value) -> config.EmoteRangeBlocks = value, (config) -> config.EmoteRangeBlocks)
            .add()
            .append(new KeyedCodec<String>("Description-Emote-Range-Blocks", Codec.STRING),
                    (config, value) -> config.EmoteRangeDesc = value, (config) -> config.EmoteRangeDesc)
            .add()
            .append(new KeyedCodec<Double>("Emote-Broadcast-Radius-Blocks", Codec.DOUBLE),
                    (config, value) -> config.EmoteBroadcastRadiusBlocks = value, (config) -> config.EmoteBroadcastRadiusBlocks)
            .add()
            .append(new KeyedCodec<String>("Description-Emote-Broadcast-Radius-Blocks", Codec.STRING),
                    (config, value) -> config.EmoteBroadcastRadiusDesc = value, (config) -> config.EmoteBroadcastRadiusDesc)
            .add()
            .append(new KeyedCodec<Integer>("Emote-Cooldown-Seconds", Codec.INTEGER),
                    (config, value) -> config.EmoteCooldownSeconds = value, (config) -> config.EmoteCooldownSeconds)
            .add()
            .append(new KeyedCodec<String>("Description-Emote-Cooldown-Seconds", Codec.STRING),
                    (config, value) -> config.EmoteCooldownDesc = value, (config) -> config.EmoteCooldownDesc)
            .add()
            .append(new KeyedCodec<String>("Emote-Hug-Particle", Codec.STRING),
                    (config, value) -> config.EmoteHugParticle = value, (config) -> config.EmoteHugParticle)
            .add()
            .append(new KeyedCodec<String>("Description-Emote-Hug-Particle", Codec.STRING),
                    (config, value) -> config.EmoteHugParticleDesc = value, (config) -> config.EmoteHugParticleDesc)
            .add()
            .append(new KeyedCodec<String>("Emote-Kiss-Particle", Codec.STRING),
                    (config, value) -> config.EmoteKissParticle = value, (config) -> config.EmoteKissParticle)
            .add()
            .append(new KeyedCodec<String>("Description-Emote-Kiss-Particle", Codec.STRING),
                    (config, value) -> config.EmoteKissParticleDesc = value, (config) -> config.EmoteKissParticleDesc)
            .add()
            .append(new KeyedCodec<String>("Emote-Dance-Particle", Codec.STRING),
                    (config, value) -> config.EmoteDanceParticle = value, (config) -> config.EmoteDanceParticle)
            .add()
            .append(new KeyedCodec<String>("Description-Emote-Dance-Particle", Codec.STRING),
                    (config, value) -> config.EmoteDanceParticleDesc = value, (config) -> config.EmoteDanceParticleDesc)
            .add()
            .append(new KeyedCodec<String>("Emote-Hug-Sound", Codec.STRING),
                    (config, value) -> config.EmoteHugSound = value, (config) -> config.EmoteHugSound)
            .add()
            .append(new KeyedCodec<String>("Description-Emote-Hug-Sound", Codec.STRING),
                    (config, value) -> config.EmoteHugSoundDesc = value, (config) -> config.EmoteHugSoundDesc)
            .add()
            .append(new KeyedCodec<String>("Emote-Kiss-Sound", Codec.STRING),
                    (config, value) -> config.EmoteKissSound = value, (config) -> config.EmoteKissSound)
            .add()
            .append(new KeyedCodec<String>("Description-Emote-Kiss-Sound", Codec.STRING),
                    (config, value) -> config.EmoteKissSoundDesc = value, (config) -> config.EmoteKissSoundDesc)
            .add()
            .append(new KeyedCodec<String>("Emote-Dance-Sound", Codec.STRING),
                    (config, value) -> config.EmoteDanceSound = value, (config) -> config.EmoteDanceSound)
            .add()
            .append(new KeyedCodec<String>("Description-Emote-Dance-Sound", Codec.STRING),
                    (config, value) -> config.EmoteDanceSoundDesc = value, (config) -> config.EmoteDanceSoundDesc)
            .add()
            .build();

    private String Information = INFORMATION_DEFAULT;
    private int ConfigVersion = CONFIG_VERSION_DEFAULT;
    private String PluginName = "Marriage";
    private String Version = Marriage.getVersion();
    private String Website = WEBSITE_DEFAULT;
    private String DownloadSite = DOWNLOAD_SITE_DEFAULT;
    private boolean NewVersion = true;
    private String NewVersionDesc = DESC_NEW_VERSION;
    private String Space = " ";

    private boolean RequireRing = false;
    private String RequireRingDesc = DESC_REQUIRE_RING;
    private boolean ConsumeRing = false;
    private String ConsumeRingDesc = DESC_CONSUME_RING;
    private boolean CmdPermission = false;
    private String CmdPermissionDesc = DESC_CMD_PERMISSION;
    private boolean AnnounceMarriages = true;
    private String AnnounceMarriagesDesc = DESC_ANNOUNCE_MARRIAGES;
    private boolean AnnounceDivorces = true;
    private String AnnounceDivorcesDesc = DESC_ANNOUNCE_DIVORCES;

    private int RequestExpirySeconds = 120;
    private String RequestExpiryDesc = DESC_REQUEST_EXPIRY;
    private int DivorceConfirmSeconds = 10;
    private String DivorceConfirmDesc = DESC_DIVORCE_CONFIRM;

    private boolean EnableMenu = true;
    private String EnableMenuDesc = DESC_ENABLE_MENU;
    private boolean GrantMenuPermission = true;
    private String GrantMenuPermissionDesc = DESC_GRANT_MENU_PERMISSION;
    private boolean GrantedMenuPermission = false;
    private String GrantedMenuPermissionDesc = DESC_GRANTED_MENU_PERMISSION;

    private boolean PartnerPvpProtection = true;
    private String PartnerPvpProtectionDesc = DESC_PVP_PROTECTION;
    private boolean DefaultPvpProtection = true;
    private String DefaultPvpProtectionDesc = DESC_DEFAULT_PVP_PROTECTION;
    private boolean PvpProtectionMessage = true;
    private String PvpProtectionMessageDesc = DESC_PVP_PROTECTION_MESSAGE;

    private boolean PartnerRegen = true;
    private String PartnerRegenDesc = DESC_ENABLE_REGEN;
    private double RegenRadiusBlocks = 10.0;
    private String RegenRadiusDesc = DESC_REGEN_RADIUS;
    private double RegenIntervalSeconds = 2.0;
    private String RegenIntervalDesc = DESC_REGEN_INTERVAL;
    private double RegenAmountPerInterval = 1.0;
    private String RegenAmountDesc = DESC_REGEN_AMOUNT;

    private boolean EnableGift = true;
    private String EnableGiftDesc = DESC_ENABLE_GIFT;
    private double GiftRangeBlocks = 8.0;
    private String GiftRangeDesc = DESC_GIFT_RANGE;
    private int GiftCooldownSeconds = 30;
    private String GiftCooldownDesc = DESC_GIFT_COOLDOWN;

    private boolean EnableTp = true;
    private String EnableTpDesc = DESC_ENABLE_TP;
    private int TpCooldownSeconds = 0;
    private String TpCooldownDesc = DESC_TP_COOLDOWN;
    private boolean EnableMsg = true;
    private String EnableMsgDesc = DESC_ENABLE_MSG;

    private boolean EnableBond = true;
    private String EnableBondDesc = DESC_ENABLE_BOND;
    private int BondMaxLevel = 10;
    private String BondMaxLevelDesc = DESC_BOND_MAX_LEVEL;
    private double BondXpPerLevel = 120.0;
    private String BondXpPerLevelDesc = DESC_BOND_XP_PER_LEVEL;
    private double BondXpPerHourMarried = 6.0;
    private String BondXpPerHourDesc = DESC_BOND_XP_PER_HOUR;
    private double BondXpPerMinuteTogether = 1.0;
    private String BondXpPerMinuteTogetherDesc = DESC_BOND_XP_PER_MINUTE_TOGETHER;
    private double BondRangeBlocks = 24.0;
    private String BondRangeDesc = DESC_BOND_RANGE;
    private double BondXpPerGift = 8.0;
    private String BondXpPerGiftDesc = DESC_BOND_XP_PER_GIFT;
    private double BondXpPerCoopKill = 1.0;
    private String BondXpPerCoopKillDesc = DESC_BOND_XP_PER_COOP_KILL;
    private double BondRegenRadiusPercentPerLevel = 10.0;
    private String BondRegenRadiusPerLevelDesc = DESC_BOND_REGEN_RADIUS_PER_LEVEL;
    private double BondTpCooldownPercentPerLevel = 8.0;
    private String BondTpCooldownPerLevelDesc = DESC_BOND_TP_COOLDOWN_PER_LEVEL;

    private boolean EnableAnniversaries = true;
    private String EnableAnniversariesDesc = DESC_ENABLE_ANNIVERSARIES;
    private boolean AnnounceAnniversaries = true;
    private String AnnounceAnniversariesDesc = DESC_ANNOUNCE_ANNIVERSARIES;
    private double AnniversaryBondXp = 150.0;
    private String AnniversaryBondXpDesc = DESC_ANNIVERSARY_BOND_XP;

    private boolean EnableBondDecay = false;
    private String EnableBondDecayDesc = DESC_ENABLE_BOND_DECAY;
    private double BondDecayGraceDays = 7.0;
    private String BondDecayGraceDaysDesc = DESC_BOND_DECAY_GRACE_DAYS;
    private double BondDecayXpPerDay = 25.0;
    private String BondDecayXpPerDayDesc = DESC_BOND_DECAY_XP_PER_DAY;
    private int BondDecayMinLevel = 0;
    private String BondDecayMinLevelDesc = DESC_BOND_DECAY_MIN_LEVEL;

    private boolean EnableSharedHome = true;
    private String EnableSharedHomeDesc = DESC_ENABLE_SHARED_HOME;
    private int SharedHomeBondLevel = 2;
    private String SharedHomeBondLevelDesc = DESC_SHARED_HOME_LEVEL;
    private int HomeCooldownSeconds = 300;
    private String HomeCooldownDesc = DESC_HOME_COOLDOWN;
    private boolean ShowHomeOnMap = true;
    private String ShowHomeOnMapDesc = DESC_SHOW_HOME_ON_MAP;

    private boolean EnablePartnerMarker = true;
    private String EnablePartnerMarkerDesc = DESC_ENABLE_PARTNER_MARKER;
    private int PartnerMarkerBondLevel = 3;
    private String PartnerMarkerBondLevelDesc = DESC_PARTNER_MARKER_LEVEL;

    private boolean EnableDeathAlert = true;
    private String EnableDeathAlertDesc = DESC_ENABLE_DEATH_ALERT;
    private int DeathAlertBondLevel = 1;
    private String DeathAlertBondLevelDesc = DESC_DEATH_ALERT_LEVEL;
    private double DeathMarkerMinutes = 10.0;
    private String DeathMarkerMinutesDesc = DESC_DEATH_MARKER_MINUTES;

    private boolean EnableHopeChest = true;
    private String EnableHopeChestDesc = DESC_ENABLE_HOPE_CHEST;
    private int HopeChestBondLevel = 5;
    private String HopeChestBondLevelDesc = DESC_HOPE_CHEST_LEVEL;
    private int HopeChestSlots = 27;
    private String HopeChestSlotsDesc = DESC_HOPE_CHEST_SLOTS;

    private boolean EnableBondOfPain = true;
    private String EnableBondOfPainDesc = DESC_ENABLE_BOND_OF_PAIN;
    private int BondOfPainBondLevel = 4;
    private String BondOfPainBondLevelDesc = DESC_BOND_OF_PAIN_LEVEL;
    private double BondOfPainSharePercent = 25.0;
    private String BondOfPainShareDesc = DESC_BOND_OF_PAIN_SHARE;
    private double BondOfPainRangeBlocks = 32.0;
    private String BondOfPainRangeDesc = DESC_BOND_OF_PAIN_RANGE;

    private boolean EnableLastStand = true;
    private String EnableLastStandDesc = DESC_ENABLE_LAST_STAND;
    private int LastStandBondLevel = 6;
    private String LastStandBondLevelDesc = DESC_LAST_STAND_LEVEL;
    private String LastStandEffect = "Potion_Health_Regen_Greater";
    private String LastStandEffectDesc = DESC_LAST_STAND_EFFECT;
    private double LastStandDurationSeconds = 10.0;
    private String LastStandDurationDesc = DESC_LAST_STAND_DURATION;

    private boolean EnableRespawnAtPartner = true;
    private String EnableRespawnAtPartnerDesc = DESC_ENABLE_RESPAWN_AT_PARTNER;
    private int RespawnAtPartnerBondLevel = 8;
    private String RespawnAtPartnerBondLevelDesc = DESC_RESPAWN_AT_PARTNER_LEVEL;
    private int RespawnAtPartnerCooldownSeconds = 900;
    private String RespawnAtPartnerCooldownDesc = DESC_RESPAWN_AT_PARTNER_COOLDOWN;

    private boolean EnableCoupleChannel = true;
    private String EnableCoupleChannelDesc = DESC_ENABLE_COUPLE_CHANNEL;
    private boolean EnableLoveLetters = true;
    private String EnableLoveLettersDesc = DESC_ENABLE_LOVE_LETTERS;
    private int LoveLetterMaxLength = 500;
    private String LoveLetterMaxLengthDesc = DESC_LOVE_LETTER_MAX_LENGTH;
    private boolean EnablePartnerHud = true;
    private String EnablePartnerHudDesc = DESC_ENABLE_PARTNER_HUD;
    private double PartnerHudRangeBlocks = 32.0;
    private String PartnerHudRangeDesc = DESC_PARTNER_HUD_RANGE;
    private boolean EnableRecords = true;
    private String EnableRecordsDesc = DESC_ENABLE_RECORDS;

    private boolean EnableRingTiers = true;
    private String EnableRingTiersDesc = DESC_ENABLE_RING_TIERS;
    private boolean EngraveRings = true;
    private String EngraveRingsDesc = DESC_ENGRAVE_RINGS;
    private double GoldRingXpMultiplier = 1.25;
    private String GoldRingXpMultiplierDesc = DESC_GOLD_RING_XP_MULTIPLIER;
    private int GoldRingPerkDiscount = 1;
    private String GoldRingPerkDiscountDesc = DESC_GOLD_RING_PERK_DISCOUNT;
    private double DiamondRingXpMultiplier = 1.5;
    private String DiamondRingXpMultiplierDesc = DESC_DIAMOND_RING_XP_MULTIPLIER;
    private int DiamondRingPerkDiscount = 2;
    private String DiamondRingPerkDiscountDesc = DESC_DIAMOND_RING_PERK_DISCOUNT;

    private boolean EnableVows = true;
    private String EnableVowsDesc = DESC_ENABLE_VOWS;
    private int VowPerkDiscount = 2;
    private String VowPerkDiscountDesc = DESC_VOW_PERK_DISCOUNT;
    private int VowHearthChestBonusSlots = 9;
    private String VowHearthSlotsDesc = DESC_VOW_HEARTH_SLOTS;
    private double VowBladeShareBonusPercent = 15.0;
    private String VowBladeShareDesc = DESC_VOW_BLADE_SHARE;
    private double VowWanderCooldownCutPercent = 35.0;
    private String VowWanderCutDesc = DESC_VOW_WANDER_CUT;

    private boolean EnableHoneymoon = true;
    private String EnableHoneymoonDesc = DESC_ENABLE_HONEYMOON;
    private double HoneymoonHours = 24.0;
    private String HoneymoonHoursDesc = DESC_HONEYMOON_HOURS;
    private double HoneymoonXpMultiplier = 2.0;
    private String HoneymoonMultiplierDesc = DESC_HONEYMOON_MULTIPLIER;

    private boolean EnableDivorceWait = true;
    private String EnableDivorceWaitDesc = DESC_ENABLE_DIVORCE_WAIT;
    private double DivorceWaitingHours = 24.0;
    private String DivorceWaitingHoursDesc = DESC_DIVORCE_WAIT_HOURS;
    private double DivorceUncontestedFactor = 0.5;
    private String DivorceUncontestedFactorDesc = DESC_DIVORCE_UNCONTESTED_FACTOR;
    private int RemarryCooldownSeconds = 3600;
    private String RemarryCooldownDesc = DESC_REMARRY_COOLDOWN;

    private boolean EnableRenew = true;
    private String EnableRenewDesc = DESC_ENABLE_RENEW;
    private double RenewMinDays = 30.0;
    private String RenewMinDaysDesc = DESC_RENEW_MIN_DAYS;
    private double RenewBondXp = 250.0;
    private String RenewBondXpDesc = DESC_RENEW_BOND_XP;
    private int RenewOfferSeconds = 60;
    private String RenewOfferSecondsDesc = DESC_RENEW_OFFER_SECONDS;

    private String AnniversaryReward1 = "";
    private String AnniversaryReward1Desc = DESC_ANNIVERSARY_REWARD_1;
    private String AnniversaryReward7 = "Marriage_LoveLetter";
    private String AnniversaryReward7Desc = DESC_ANNIVERSARY_REWARD_7;
    private String AnniversaryReward30 = "Marriage_Ring_Gold";
    private String AnniversaryReward30Desc = DESC_ANNIVERSARY_REWARD_30;
    private String AnniversaryReward365 = "Marriage_Ring_Diamond";
    private String AnniversaryReward365Desc = DESC_ANNIVERSARY_REWARD_365;
    private int AnniversaryRewardQuantity = 1;
    private String AnniversaryRewardQuantityDesc = DESC_ANNIVERSARY_REWARD_QUANTITY;

    private boolean EnableEmotes = true;
    private String EnableEmotesDesc = DESC_ENABLE_EMOTES;
    private double EmoteRangeBlocks = 8.0;
    private String EmoteRangeDesc = DESC_EMOTE_RANGE;
    private double EmoteBroadcastRadiusBlocks = 24.0;
    private String EmoteBroadcastRadiusDesc = DESC_EMOTE_BROADCAST_RADIUS;
    private int EmoteCooldownSeconds = 5;
    private String EmoteCooldownDesc = DESC_EMOTE_COOLDOWN;
    // Verified against the vanilla particle set (Hearts / Hearts_Subtle live in
    // NPC/Emotions, Dust_Sparkles in Weather) and the shipped SoundEvent assets.
    private String EmoteHugParticle = "Hearts_Subtle";
    private String EmoteHugParticleDesc = DESC_EMOTE_HUG_PARTICLE;
    private String EmoteKissParticle = "Hearts";
    private String EmoteKissParticleDesc = DESC_EMOTE_KISS_PARTICLE;
    private String EmoteDanceParticle = "Dust_Sparkles";
    private String EmoteDanceParticleDesc = DESC_EMOTE_DANCE_PARTICLE;
    private String EmoteHugSound = "SFX_Deployable_Totem_Heal_Spawn";
    private String EmoteHugSoundDesc = DESC_EMOTE_HUG_SOUND;
    private String EmoteKissSound = "SFX_Deployable_Totem_Heal_Despawn";
    private String EmoteKissSoundDesc = DESC_EMOTE_KISS_SOUND;
    private String EmoteDanceSound = "SFX_Stamina_Potion_Success";
    private String EmoteDanceSoundDesc = DESC_EMOTE_DANCE_SOUND;

    public MarriageConfig(){}

    public boolean ifConfigUpdate(){
        boolean configUpdated = false;

        if(CONFIG_VERSION_DEFAULT > ConfigVersion){
            configUpdated = true;
            ConfigVersion = CONFIG_VERSION_DEFAULT;
        }
        if(!Version.equalsIgnoreCase(Marriage.getVersion())){
            configUpdated = true;
            Version = Marriage.getVersion();
        }
        if(!Information.equals(INFORMATION_DEFAULT)){
            configUpdated = true;
            Information = INFORMATION_DEFAULT;
        }
        if(!Website.equals(WEBSITE_DEFAULT)){
            configUpdated = true;
            Website = WEBSITE_DEFAULT;
        }
        if(!DownloadSite.equals(DOWNLOAD_SITE_DEFAULT)){
            configUpdated = true;
            DownloadSite = DOWNLOAD_SITE_DEFAULT;
        }

        configUpdated |= resetDescriptions();

        return configUpdated;
    }

    private boolean resetDescriptions(){
        boolean drifted = false;
        if(!NewVersionDesc.equals(DESC_NEW_VERSION)){ drifted = true; NewVersionDesc = DESC_NEW_VERSION; }
        if(!RequireRingDesc.equals(DESC_REQUIRE_RING)){ drifted = true; RequireRingDesc = DESC_REQUIRE_RING; }
        if(!ConsumeRingDesc.equals(DESC_CONSUME_RING)){ drifted = true; ConsumeRingDesc = DESC_CONSUME_RING; }
        if(!CmdPermissionDesc.equals(DESC_CMD_PERMISSION)){ drifted = true; CmdPermissionDesc = DESC_CMD_PERMISSION; }
        if(!AnnounceMarriagesDesc.equals(DESC_ANNOUNCE_MARRIAGES)){ drifted = true; AnnounceMarriagesDesc = DESC_ANNOUNCE_MARRIAGES; }
        if(!AnnounceDivorcesDesc.equals(DESC_ANNOUNCE_DIVORCES)){ drifted = true; AnnounceDivorcesDesc = DESC_ANNOUNCE_DIVORCES; }
        if(!RequestExpiryDesc.equals(DESC_REQUEST_EXPIRY)){ drifted = true; RequestExpiryDesc = DESC_REQUEST_EXPIRY; }
        if(!DivorceConfirmDesc.equals(DESC_DIVORCE_CONFIRM)){ drifted = true; DivorceConfirmDesc = DESC_DIVORCE_CONFIRM; }
        if(!EnableMenuDesc.equals(DESC_ENABLE_MENU)){ drifted = true; EnableMenuDesc = DESC_ENABLE_MENU; }
        if(!GrantMenuPermissionDesc.equals(DESC_GRANT_MENU_PERMISSION)){ drifted = true; GrantMenuPermissionDesc = DESC_GRANT_MENU_PERMISSION; }
        if(!GrantedMenuPermissionDesc.equals(DESC_GRANTED_MENU_PERMISSION)){ drifted = true; GrantedMenuPermissionDesc = DESC_GRANTED_MENU_PERMISSION; }
        if(!PartnerPvpProtectionDesc.equals(DESC_PVP_PROTECTION)){ drifted = true; PartnerPvpProtectionDesc = DESC_PVP_PROTECTION; }
        if(!DefaultPvpProtectionDesc.equals(DESC_DEFAULT_PVP_PROTECTION)){ drifted = true; DefaultPvpProtectionDesc = DESC_DEFAULT_PVP_PROTECTION; }
        if(!PvpProtectionMessageDesc.equals(DESC_PVP_PROTECTION_MESSAGE)){ drifted = true; PvpProtectionMessageDesc = DESC_PVP_PROTECTION_MESSAGE; }
        if(!PartnerRegenDesc.equals(DESC_ENABLE_REGEN)){ drifted = true; PartnerRegenDesc = DESC_ENABLE_REGEN; }
        if(!RegenRadiusDesc.equals(DESC_REGEN_RADIUS)){ drifted = true; RegenRadiusDesc = DESC_REGEN_RADIUS; }
        if(!RegenIntervalDesc.equals(DESC_REGEN_INTERVAL)){ drifted = true; RegenIntervalDesc = DESC_REGEN_INTERVAL; }
        if(!RegenAmountDesc.equals(DESC_REGEN_AMOUNT)){ drifted = true; RegenAmountDesc = DESC_REGEN_AMOUNT; }
        if(!EnableGiftDesc.equals(DESC_ENABLE_GIFT)){ drifted = true; EnableGiftDesc = DESC_ENABLE_GIFT; }
        if(!GiftRangeDesc.equals(DESC_GIFT_RANGE)){ drifted = true; GiftRangeDesc = DESC_GIFT_RANGE; }
        if(!GiftCooldownDesc.equals(DESC_GIFT_COOLDOWN)){ drifted = true; GiftCooldownDesc = DESC_GIFT_COOLDOWN; }
        if(!EnableTpDesc.equals(DESC_ENABLE_TP)){ drifted = true; EnableTpDesc = DESC_ENABLE_TP; }
        if(!TpCooldownDesc.equals(DESC_TP_COOLDOWN)){ drifted = true; TpCooldownDesc = DESC_TP_COOLDOWN; }
        if(!EnableMsgDesc.equals(DESC_ENABLE_MSG)){ drifted = true; EnableMsgDesc = DESC_ENABLE_MSG; }
        if(!EnableBondDesc.equals(DESC_ENABLE_BOND)){ drifted = true; EnableBondDesc = DESC_ENABLE_BOND; }
        if(!BondMaxLevelDesc.equals(DESC_BOND_MAX_LEVEL)){ drifted = true; BondMaxLevelDesc = DESC_BOND_MAX_LEVEL; }
        if(!BondXpPerLevelDesc.equals(DESC_BOND_XP_PER_LEVEL)){ drifted = true; BondXpPerLevelDesc = DESC_BOND_XP_PER_LEVEL; }
        if(!BondXpPerHourDesc.equals(DESC_BOND_XP_PER_HOUR)){ drifted = true; BondXpPerHourDesc = DESC_BOND_XP_PER_HOUR; }
        if(!BondXpPerMinuteTogetherDesc.equals(DESC_BOND_XP_PER_MINUTE_TOGETHER)){ drifted = true; BondXpPerMinuteTogetherDesc = DESC_BOND_XP_PER_MINUTE_TOGETHER; }
        if(!BondRangeDesc.equals(DESC_BOND_RANGE)){ drifted = true; BondRangeDesc = DESC_BOND_RANGE; }
        if(!BondXpPerGiftDesc.equals(DESC_BOND_XP_PER_GIFT)){ drifted = true; BondXpPerGiftDesc = DESC_BOND_XP_PER_GIFT; }
        if(!BondXpPerCoopKillDesc.equals(DESC_BOND_XP_PER_COOP_KILL)){ drifted = true; BondXpPerCoopKillDesc = DESC_BOND_XP_PER_COOP_KILL; }
        if(!BondRegenRadiusPerLevelDesc.equals(DESC_BOND_REGEN_RADIUS_PER_LEVEL)){ drifted = true; BondRegenRadiusPerLevelDesc = DESC_BOND_REGEN_RADIUS_PER_LEVEL; }
        if(!BondTpCooldownPerLevelDesc.equals(DESC_BOND_TP_COOLDOWN_PER_LEVEL)){ drifted = true; BondTpCooldownPerLevelDesc = DESC_BOND_TP_COOLDOWN_PER_LEVEL; }
        if(!EnableAnniversariesDesc.equals(DESC_ENABLE_ANNIVERSARIES)){ drifted = true; EnableAnniversariesDesc = DESC_ENABLE_ANNIVERSARIES; }
        if(!AnnounceAnniversariesDesc.equals(DESC_ANNOUNCE_ANNIVERSARIES)){ drifted = true; AnnounceAnniversariesDesc = DESC_ANNOUNCE_ANNIVERSARIES; }
        if(!AnniversaryBondXpDesc.equals(DESC_ANNIVERSARY_BOND_XP)){ drifted = true; AnniversaryBondXpDesc = DESC_ANNIVERSARY_BOND_XP; }
        if(!EnableBondDecayDesc.equals(DESC_ENABLE_BOND_DECAY)){ drifted = true; EnableBondDecayDesc = DESC_ENABLE_BOND_DECAY; }
        if(!BondDecayGraceDaysDesc.equals(DESC_BOND_DECAY_GRACE_DAYS)){ drifted = true; BondDecayGraceDaysDesc = DESC_BOND_DECAY_GRACE_DAYS; }
        if(!BondDecayXpPerDayDesc.equals(DESC_BOND_DECAY_XP_PER_DAY)){ drifted = true; BondDecayXpPerDayDesc = DESC_BOND_DECAY_XP_PER_DAY; }
        if(!BondDecayMinLevelDesc.equals(DESC_BOND_DECAY_MIN_LEVEL)){ drifted = true; BondDecayMinLevelDesc = DESC_BOND_DECAY_MIN_LEVEL; }
        if(!EnableSharedHomeDesc.equals(DESC_ENABLE_SHARED_HOME)){ drifted = true; EnableSharedHomeDesc = DESC_ENABLE_SHARED_HOME; }
        if(!SharedHomeBondLevelDesc.equals(DESC_SHARED_HOME_LEVEL)){ drifted = true; SharedHomeBondLevelDesc = DESC_SHARED_HOME_LEVEL; }
        if(!HomeCooldownDesc.equals(DESC_HOME_COOLDOWN)){ drifted = true; HomeCooldownDesc = DESC_HOME_COOLDOWN; }
        if(!ShowHomeOnMapDesc.equals(DESC_SHOW_HOME_ON_MAP)){ drifted = true; ShowHomeOnMapDesc = DESC_SHOW_HOME_ON_MAP; }
        if(!EnablePartnerMarkerDesc.equals(DESC_ENABLE_PARTNER_MARKER)){ drifted = true; EnablePartnerMarkerDesc = DESC_ENABLE_PARTNER_MARKER; }
        if(!PartnerMarkerBondLevelDesc.equals(DESC_PARTNER_MARKER_LEVEL)){ drifted = true; PartnerMarkerBondLevelDesc = DESC_PARTNER_MARKER_LEVEL; }
        if(!EnableDeathAlertDesc.equals(DESC_ENABLE_DEATH_ALERT)){ drifted = true; EnableDeathAlertDesc = DESC_ENABLE_DEATH_ALERT; }
        if(!DeathAlertBondLevelDesc.equals(DESC_DEATH_ALERT_LEVEL)){ drifted = true; DeathAlertBondLevelDesc = DESC_DEATH_ALERT_LEVEL; }
        if(!DeathMarkerMinutesDesc.equals(DESC_DEATH_MARKER_MINUTES)){ drifted = true; DeathMarkerMinutesDesc = DESC_DEATH_MARKER_MINUTES; }
        if(!EnableHopeChestDesc.equals(DESC_ENABLE_HOPE_CHEST)){ drifted = true; EnableHopeChestDesc = DESC_ENABLE_HOPE_CHEST; }
        if(!HopeChestBondLevelDesc.equals(DESC_HOPE_CHEST_LEVEL)){ drifted = true; HopeChestBondLevelDesc = DESC_HOPE_CHEST_LEVEL; }
        if(!HopeChestSlotsDesc.equals(DESC_HOPE_CHEST_SLOTS)){ drifted = true; HopeChestSlotsDesc = DESC_HOPE_CHEST_SLOTS; }
        if(!EnableBondOfPainDesc.equals(DESC_ENABLE_BOND_OF_PAIN)){ drifted = true; EnableBondOfPainDesc = DESC_ENABLE_BOND_OF_PAIN; }
        if(!BondOfPainBondLevelDesc.equals(DESC_BOND_OF_PAIN_LEVEL)){ drifted = true; BondOfPainBondLevelDesc = DESC_BOND_OF_PAIN_LEVEL; }
        if(!BondOfPainShareDesc.equals(DESC_BOND_OF_PAIN_SHARE)){ drifted = true; BondOfPainShareDesc = DESC_BOND_OF_PAIN_SHARE; }
        if(!BondOfPainRangeDesc.equals(DESC_BOND_OF_PAIN_RANGE)){ drifted = true; BondOfPainRangeDesc = DESC_BOND_OF_PAIN_RANGE; }
        if(!EnableLastStandDesc.equals(DESC_ENABLE_LAST_STAND)){ drifted = true; EnableLastStandDesc = DESC_ENABLE_LAST_STAND; }
        if(!LastStandBondLevelDesc.equals(DESC_LAST_STAND_LEVEL)){ drifted = true; LastStandBondLevelDesc = DESC_LAST_STAND_LEVEL; }
        if(!LastStandEffectDesc.equals(DESC_LAST_STAND_EFFECT)){ drifted = true; LastStandEffectDesc = DESC_LAST_STAND_EFFECT; }
        if(!LastStandDurationDesc.equals(DESC_LAST_STAND_DURATION)){ drifted = true; LastStandDurationDesc = DESC_LAST_STAND_DURATION; }
        if(!EnableRespawnAtPartnerDesc.equals(DESC_ENABLE_RESPAWN_AT_PARTNER)){ drifted = true; EnableRespawnAtPartnerDesc = DESC_ENABLE_RESPAWN_AT_PARTNER; }
        if(!RespawnAtPartnerBondLevelDesc.equals(DESC_RESPAWN_AT_PARTNER_LEVEL)){ drifted = true; RespawnAtPartnerBondLevelDesc = DESC_RESPAWN_AT_PARTNER_LEVEL; }
        if(!RespawnAtPartnerCooldownDesc.equals(DESC_RESPAWN_AT_PARTNER_COOLDOWN)){ drifted = true; RespawnAtPartnerCooldownDesc = DESC_RESPAWN_AT_PARTNER_COOLDOWN; }
        if(!EnableCoupleChannelDesc.equals(DESC_ENABLE_COUPLE_CHANNEL)){ drifted = true; EnableCoupleChannelDesc = DESC_ENABLE_COUPLE_CHANNEL; }
        if(!EnableLoveLettersDesc.equals(DESC_ENABLE_LOVE_LETTERS)){ drifted = true; EnableLoveLettersDesc = DESC_ENABLE_LOVE_LETTERS; }
        if(!LoveLetterMaxLengthDesc.equals(DESC_LOVE_LETTER_MAX_LENGTH)){ drifted = true; LoveLetterMaxLengthDesc = DESC_LOVE_LETTER_MAX_LENGTH; }
        if(!EnablePartnerHudDesc.equals(DESC_ENABLE_PARTNER_HUD)){ drifted = true; EnablePartnerHudDesc = DESC_ENABLE_PARTNER_HUD; }
        if(!PartnerHudRangeDesc.equals(DESC_PARTNER_HUD_RANGE)){ drifted = true; PartnerHudRangeDesc = DESC_PARTNER_HUD_RANGE; }
        if(!EnableRecordsDesc.equals(DESC_ENABLE_RECORDS)){ drifted = true; EnableRecordsDesc = DESC_ENABLE_RECORDS; }
        if(!EnableRingTiersDesc.equals(DESC_ENABLE_RING_TIERS)){ drifted = true; EnableRingTiersDesc = DESC_ENABLE_RING_TIERS; }
        if(!EngraveRingsDesc.equals(DESC_ENGRAVE_RINGS)){ drifted = true; EngraveRingsDesc = DESC_ENGRAVE_RINGS; }
        if(!GoldRingXpMultiplierDesc.equals(DESC_GOLD_RING_XP_MULTIPLIER)){ drifted = true; GoldRingXpMultiplierDesc = DESC_GOLD_RING_XP_MULTIPLIER; }
        if(!GoldRingPerkDiscountDesc.equals(DESC_GOLD_RING_PERK_DISCOUNT)){ drifted = true; GoldRingPerkDiscountDesc = DESC_GOLD_RING_PERK_DISCOUNT; }
        if(!DiamondRingXpMultiplierDesc.equals(DESC_DIAMOND_RING_XP_MULTIPLIER)){ drifted = true; DiamondRingXpMultiplierDesc = DESC_DIAMOND_RING_XP_MULTIPLIER; }
        if(!DiamondRingPerkDiscountDesc.equals(DESC_DIAMOND_RING_PERK_DISCOUNT)){ drifted = true; DiamondRingPerkDiscountDesc = DESC_DIAMOND_RING_PERK_DISCOUNT; }
        if(!EnableVowsDesc.equals(DESC_ENABLE_VOWS)){ drifted = true; EnableVowsDesc = DESC_ENABLE_VOWS; }
        if(!VowPerkDiscountDesc.equals(DESC_VOW_PERK_DISCOUNT)){ drifted = true; VowPerkDiscountDesc = DESC_VOW_PERK_DISCOUNT; }
        if(!VowHearthSlotsDesc.equals(DESC_VOW_HEARTH_SLOTS)){ drifted = true; VowHearthSlotsDesc = DESC_VOW_HEARTH_SLOTS; }
        if(!VowBladeShareDesc.equals(DESC_VOW_BLADE_SHARE)){ drifted = true; VowBladeShareDesc = DESC_VOW_BLADE_SHARE; }
        if(!VowWanderCutDesc.equals(DESC_VOW_WANDER_CUT)){ drifted = true; VowWanderCutDesc = DESC_VOW_WANDER_CUT; }
        if(!EnableHoneymoonDesc.equals(DESC_ENABLE_HONEYMOON)){ drifted = true; EnableHoneymoonDesc = DESC_ENABLE_HONEYMOON; }
        if(!HoneymoonHoursDesc.equals(DESC_HONEYMOON_HOURS)){ drifted = true; HoneymoonHoursDesc = DESC_HONEYMOON_HOURS; }
        if(!HoneymoonMultiplierDesc.equals(DESC_HONEYMOON_MULTIPLIER)){ drifted = true; HoneymoonMultiplierDesc = DESC_HONEYMOON_MULTIPLIER; }
        if(!EnableDivorceWaitDesc.equals(DESC_ENABLE_DIVORCE_WAIT)){ drifted = true; EnableDivorceWaitDesc = DESC_ENABLE_DIVORCE_WAIT; }
        if(!DivorceWaitingHoursDesc.equals(DESC_DIVORCE_WAIT_HOURS)){ drifted = true; DivorceWaitingHoursDesc = DESC_DIVORCE_WAIT_HOURS; }
        if(!DivorceUncontestedFactorDesc.equals(DESC_DIVORCE_UNCONTESTED_FACTOR)){ drifted = true; DivorceUncontestedFactorDesc = DESC_DIVORCE_UNCONTESTED_FACTOR; }
        if(!RemarryCooldownDesc.equals(DESC_REMARRY_COOLDOWN)){ drifted = true; RemarryCooldownDesc = DESC_REMARRY_COOLDOWN; }
        if(!EnableRenewDesc.equals(DESC_ENABLE_RENEW)){ drifted = true; EnableRenewDesc = DESC_ENABLE_RENEW; }
        if(!RenewMinDaysDesc.equals(DESC_RENEW_MIN_DAYS)){ drifted = true; RenewMinDaysDesc = DESC_RENEW_MIN_DAYS; }
        if(!RenewBondXpDesc.equals(DESC_RENEW_BOND_XP)){ drifted = true; RenewBondXpDesc = DESC_RENEW_BOND_XP; }
        if(!RenewOfferSecondsDesc.equals(DESC_RENEW_OFFER_SECONDS)){ drifted = true; RenewOfferSecondsDesc = DESC_RENEW_OFFER_SECONDS; }
        if(!AnniversaryReward1Desc.equals(DESC_ANNIVERSARY_REWARD_1)){ drifted = true; AnniversaryReward1Desc = DESC_ANNIVERSARY_REWARD_1; }
        if(!AnniversaryReward7Desc.equals(DESC_ANNIVERSARY_REWARD_7)){ drifted = true; AnniversaryReward7Desc = DESC_ANNIVERSARY_REWARD_7; }
        if(!AnniversaryReward30Desc.equals(DESC_ANNIVERSARY_REWARD_30)){ drifted = true; AnniversaryReward30Desc = DESC_ANNIVERSARY_REWARD_30; }
        if(!AnniversaryReward365Desc.equals(DESC_ANNIVERSARY_REWARD_365)){ drifted = true; AnniversaryReward365Desc = DESC_ANNIVERSARY_REWARD_365; }
        if(!AnniversaryRewardQuantityDesc.equals(DESC_ANNIVERSARY_REWARD_QUANTITY)){ drifted = true; AnniversaryRewardQuantityDesc = DESC_ANNIVERSARY_REWARD_QUANTITY; }
        if(!EnableEmotesDesc.equals(DESC_ENABLE_EMOTES)){ drifted = true; EnableEmotesDesc = DESC_ENABLE_EMOTES; }
        if(!EmoteRangeDesc.equals(DESC_EMOTE_RANGE)){ drifted = true; EmoteRangeDesc = DESC_EMOTE_RANGE; }
        if(!EmoteBroadcastRadiusDesc.equals(DESC_EMOTE_BROADCAST_RADIUS)){ drifted = true; EmoteBroadcastRadiusDesc = DESC_EMOTE_BROADCAST_RADIUS; }
        if(!EmoteCooldownDesc.equals(DESC_EMOTE_COOLDOWN)){ drifted = true; EmoteCooldownDesc = DESC_EMOTE_COOLDOWN; }
        if(!EmoteHugParticleDesc.equals(DESC_EMOTE_HUG_PARTICLE)){ drifted = true; EmoteHugParticleDesc = DESC_EMOTE_HUG_PARTICLE; }
        if(!EmoteKissParticleDesc.equals(DESC_EMOTE_KISS_PARTICLE)){ drifted = true; EmoteKissParticleDesc = DESC_EMOTE_KISS_PARTICLE; }
        if(!EmoteDanceParticleDesc.equals(DESC_EMOTE_DANCE_PARTICLE)){ drifted = true; EmoteDanceParticleDesc = DESC_EMOTE_DANCE_PARTICLE; }
        if(!EmoteHugSoundDesc.equals(DESC_EMOTE_HUG_SOUND)){ drifted = true; EmoteHugSoundDesc = DESC_EMOTE_HUG_SOUND; }
        if(!EmoteKissSoundDesc.equals(DESC_EMOTE_KISS_SOUND)){ drifted = true; EmoteKissSoundDesc = DESC_EMOTE_KISS_SOUND; }
        if(!EmoteDanceSoundDesc.equals(DESC_EMOTE_DANCE_SOUND)){ drifted = true; EmoteDanceSoundDesc = DESC_EMOTE_DANCE_SOUND; }
        return drifted;
    }

    public int getConfigVersion(){
        return this.ConfigVersion;
    }

    public String getPluginVersion(){
        return this.Version;
    }

    // === General ===

    public boolean ifRequireRing(){
        return this.RequireRing;
    }

    public void setRequireRing(boolean requireRing){
        this.RequireRing = requireRing;
    }

    public boolean ifConsumeRing(){
        return this.ConsumeRing;
    }

    public void setConsumeRing(boolean consumeRing){
        this.ConsumeRing = consumeRing;
    }

    public boolean ifCmdPermission(){
        return this.CmdPermission;
    }

    public boolean ifAnnounceMarriages(){
        return this.AnnounceMarriages;
    }

    public void setAnnounceMarriages(boolean announce){
        this.AnnounceMarriages = announce;
    }

    public boolean ifAnnounceDivorces(){
        return this.AnnounceDivorces;
    }

    public void setAnnounceDivorces(boolean announce){
        this.AnnounceDivorces = announce;
    }

    public boolean ifNewVersion(){
        return this.NewVersion;
    }

    // === Requests & divorce ===

    public int getRequestExpirySeconds(){
        return Math.max(5, this.RequestExpirySeconds);
    }

    public void setRequestExpirySeconds(int seconds){
        this.RequestExpirySeconds = seconds;
    }

    public int getDivorceConfirmSeconds(){
        return Math.max(3, this.DivorceConfirmSeconds);
    }

    public void setDivorceConfirmSeconds(int seconds){
        this.DivorceConfirmSeconds = seconds;
    }

    // === Menu ===

    public boolean ifMenuEnabled(){
        return this.EnableMenu;
    }

    public void setMenuEnabled(boolean enabled){
        this.EnableMenu = enabled;
    }

    public boolean ifGrantMenuPermission(){
        return this.GrantMenuPermission;
    }

    public boolean ifGrantedMenuPermission(){
        return this.GrantedMenuPermission;
    }

    public void setGrantedMenuPermission(boolean granted){
        this.GrantedMenuPermission = granted;
    }

    // === PvP protection ===

    public boolean ifPartnerPvpProtection(){
        return this.PartnerPvpProtection;
    }

    public void setPartnerPvpProtection(boolean enabled){
        this.PartnerPvpProtection = enabled;
    }

    public boolean ifDefaultPvpProtection(){
        return this.DefaultPvpProtection;
    }

    public void setDefaultPvpProtection(boolean enabled){
        this.DefaultPvpProtection = enabled;
    }

    public boolean ifPvpProtectionMessage(){
        return this.PvpProtectionMessage;
    }

    public void setPvpProtectionMessage(boolean enabled){
        this.PvpProtectionMessage = enabled;
    }

    // === Partner regeneration ===

    public boolean ifPartnerRegen(){
        return this.PartnerRegen;
    }

    public void setPartnerRegen(boolean enabled){
        this.PartnerRegen = enabled;
    }

    public double getRegenRadiusBlocks(){
        return Math.max(1.0, this.RegenRadiusBlocks);
    }

    public void setRegenRadiusBlocks(double radius){
        this.RegenRadiusBlocks = radius;
    }

    public double getRegenIntervalSeconds(){
        return Math.max(0.25, this.RegenIntervalSeconds);
    }

    public void setRegenIntervalSeconds(double seconds){
        this.RegenIntervalSeconds = seconds;
    }

    public double getRegenAmountPerInterval(){
        return Math.max(0.0, this.RegenAmountPerInterval);
    }

    public void setRegenAmountPerInterval(double amount){
        this.RegenAmountPerInterval = amount;
    }

    // === Gifts ===

    public boolean ifGiftEnabled(){
        return this.EnableGift;
    }

    public void setGiftEnabled(boolean enabled){
        this.EnableGift = enabled;
    }

    /** 0 means no range or same-world requirement. */
    public double getGiftRangeBlocks(){
        return Math.max(0.0, this.GiftRangeBlocks);
    }

    public void setGiftRangeBlocks(double range){
        this.GiftRangeBlocks = range;
    }

    public int getGiftCooldownSeconds(){
        return Math.max(0, this.GiftCooldownSeconds);
    }

    public void setGiftCooldownSeconds(int seconds){
        this.GiftCooldownSeconds = seconds;
    }

    // === Teleport & messages ===

    public boolean ifTpEnabled(){
        return this.EnableTp;
    }

    public void setTpEnabled(boolean enabled){
        this.EnableTp = enabled;
    }

    public int getTpCooldownSeconds(){
        return Math.max(0, this.TpCooldownSeconds);
    }

    public void setTpCooldownSeconds(int seconds){
        this.TpCooldownSeconds = seconds;
    }

    public boolean ifMsgEnabled(){
        return this.EnableMsg;
    }

    public void setMsgEnabled(boolean enabled){
        this.EnableMsg = enabled;
    }

    // === Bond ===

    public boolean ifBondEnabled(){
        return this.EnableBond;
    }

    public void setBondEnabled(boolean enabled){
        this.EnableBond = enabled;
    }

    public int getBondMaxLevel(){
        return Math.max(1, this.BondMaxLevel);
    }

    public void setBondMaxLevel(int level){
        this.BondMaxLevel = level;
    }

    public double getBondXpPerLevel(){
        return Math.max(1.0, this.BondXpPerLevel);
    }

    public void setBondXpPerLevel(double xp){
        this.BondXpPerLevel = xp;
    }

    public double getBondXpPerHourMarried(){
        return Math.max(0.0, this.BondXpPerHourMarried);
    }

    public void setBondXpPerHourMarried(double xp){
        this.BondXpPerHourMarried = xp;
    }

    public double getBondXpPerMinuteTogether(){
        return Math.max(0.0, this.BondXpPerMinuteTogether);
    }

    public void setBondXpPerMinuteTogether(double xp){
        this.BondXpPerMinuteTogether = xp;
    }

    public double getBondRangeBlocks(){
        return Math.max(1.0, this.BondRangeBlocks);
    }

    public void setBondRangeBlocks(double blocks){
        this.BondRangeBlocks = blocks;
    }

    public double getBondXpPerGift(){
        return Math.max(0.0, this.BondXpPerGift);
    }

    public void setBondXpPerGift(double xp){
        this.BondXpPerGift = xp;
    }

    public double getBondXpPerCoopKill(){
        return Math.max(0.0, this.BondXpPerCoopKill);
    }

    public void setBondXpPerCoopKill(double xp){
        this.BondXpPerCoopKill = xp;
    }

    public double getBondRegenRadiusPercentPerLevel(){
        return Math.max(0.0, this.BondRegenRadiusPercentPerLevel);
    }

    public void setBondRegenRadiusPercentPerLevel(double percent){
        this.BondRegenRadiusPercentPerLevel = percent;
    }

    public double getBondTpCooldownPercentPerLevel(){
        return Math.max(0.0, this.BondTpCooldownPercentPerLevel);
    }

    public void setBondTpCooldownPercentPerLevel(double percent){
        this.BondTpCooldownPercentPerLevel = percent;
    }

    // === Anniversaries ===

    public boolean ifAnniversariesEnabled(){
        return this.EnableAnniversaries;
    }

    public void setAnniversariesEnabled(boolean enabled){
        this.EnableAnniversaries = enabled;
    }

    public boolean ifAnnounceAnniversaries(){
        return this.AnnounceAnniversaries;
    }

    public void setAnnounceAnniversaries(boolean announce){
        this.AnnounceAnniversaries = announce;
    }

    public double getAnniversaryBondXp(){
        return Math.max(0.0, this.AnniversaryBondXp);
    }

    public void setAnniversaryBondXp(double xp){
        this.AnniversaryBondXp = xp;
    }

    // === Bond decay ===

    public boolean ifBondDecayEnabled(){
        return this.EnableBondDecay;
    }

    public void setBondDecayEnabled(boolean enabled){
        this.EnableBondDecay = enabled;
    }

    public double getBondDecayGraceDays(){
        return Math.max(0.0, this.BondDecayGraceDays);
    }

    public void setBondDecayGraceDays(double days){
        this.BondDecayGraceDays = days;
    }

    public double getBondDecayXpPerDay(){
        return Math.max(0.0, this.BondDecayXpPerDay);
    }

    public void setBondDecayXpPerDay(double xp){
        this.BondDecayXpPerDay = xp;
    }

    public int getBondDecayMinLevel(){
        return Math.max(0, this.BondDecayMinLevel);
    }

    public void setBondDecayMinLevel(int level){
        this.BondDecayMinLevel = level;
    }

    // === Perk: shared home ===

    public boolean ifSharedHomeEnabled(){
        return this.EnableSharedHome;
    }

    public void setSharedHomeEnabled(boolean enabled){
        this.EnableSharedHome = enabled;
    }

    public int getSharedHomeBondLevel(){
        return Math.max(0, this.SharedHomeBondLevel);
    }

    public void setSharedHomeBondLevel(int level){
        this.SharedHomeBondLevel = level;
    }

    public int getHomeCooldownSeconds(){
        return Math.max(0, this.HomeCooldownSeconds);
    }

    public void setHomeCooldownSeconds(int seconds){
        this.HomeCooldownSeconds = seconds;
    }

    public boolean ifShowHomeOnMap(){
        return this.ShowHomeOnMap;
    }

    public void setShowHomeOnMap(boolean show){
        this.ShowHomeOnMap = show;
    }

    // === Perk: partner marker ===

    public boolean ifPartnerMarkerEnabled(){
        return this.EnablePartnerMarker;
    }

    public void setPartnerMarkerEnabled(boolean enabled){
        this.EnablePartnerMarker = enabled;
    }

    public int getPartnerMarkerBondLevel(){
        return Math.max(0, this.PartnerMarkerBondLevel);
    }

    public void setPartnerMarkerBondLevel(int level){
        this.PartnerMarkerBondLevel = level;
    }

    // === Perk: death alert ===

    public boolean ifDeathAlertEnabled(){
        return this.EnableDeathAlert;
    }

    public void setDeathAlertEnabled(boolean enabled){
        this.EnableDeathAlert = enabled;
    }

    public int getDeathAlertBondLevel(){
        return Math.max(0, this.DeathAlertBondLevel);
    }

    public void setDeathAlertBondLevel(int level){
        this.DeathAlertBondLevel = level;
    }

    public double getDeathMarkerMinutes(){
        return Math.max(0.0, this.DeathMarkerMinutes);
    }

    public void setDeathMarkerMinutes(double minutes){
        this.DeathMarkerMinutes = minutes;
    }

    // === Perk: hope chest ===

    public boolean ifHopeChestEnabled(){
        return this.EnableHopeChest;
    }

    public void setHopeChestEnabled(boolean enabled){
        this.EnableHopeChest = enabled;
    }

    public int getHopeChestBondLevel(){
        return Math.max(0, this.HopeChestBondLevel);
    }

    public void setHopeChestBondLevel(int level){
        this.HopeChestBondLevel = level;
    }

    /** Clamped to what one Container window can show without a scroll the client does not offer. */
    public int getHopeChestSlots(){
        return Math.min(54, Math.max(1, this.HopeChestSlots));
    }

    public void setHopeChestSlots(int slots){
        this.HopeChestSlots = slots;
    }

    // === Perk: bond of pain ===

    public boolean ifBondOfPainEnabled(){
        return this.EnableBondOfPain;
    }

    public void setBondOfPainEnabled(boolean enabled){
        this.EnableBondOfPain = enabled;
    }

    public int getBondOfPainBondLevel(){
        return Math.max(0, this.BondOfPainBondLevel);
    }

    public void setBondOfPainBondLevel(int level){
        this.BondOfPainBondLevel = level;
    }

    /**
     * Capped at 90%: a couple must never be able to make one partner effectively
     * immortal by routing everything onto the other.
     */
    public double getBondOfPainSharePercent(){
        return Math.min(90.0, Math.max(0.0, this.BondOfPainSharePercent));
    }

    public void setBondOfPainSharePercent(double percent){
        this.BondOfPainSharePercent = percent;
    }

    public double getBondOfPainRangeBlocks(){
        return Math.max(1.0, this.BondOfPainRangeBlocks);
    }

    public void setBondOfPainRangeBlocks(double blocks){
        this.BondOfPainRangeBlocks = blocks;
    }

    // === Perk: last stand ===

    public boolean ifLastStandEnabled(){
        return this.EnableLastStand;
    }

    public void setLastStandEnabled(boolean enabled){
        this.EnableLastStand = enabled;
    }

    public int getLastStandBondLevel(){
        return Math.max(0, this.LastStandBondLevel);
    }

    public void setLastStandBondLevel(int level){
        this.LastStandBondLevel = level;
    }

    public String getLastStandEffect(){
        return this.LastStandEffect == null ? "" : this.LastStandEffect;
    }

    public double getLastStandDurationSeconds(){
        return Math.max(0.5, this.LastStandDurationSeconds);
    }

    public void setLastStandDurationSeconds(double seconds){
        this.LastStandDurationSeconds = seconds;
    }

    // === Perk: respawn at partner ===

    public boolean ifRespawnAtPartnerEnabled(){
        return this.EnableRespawnAtPartner;
    }

    public void setRespawnAtPartnerEnabled(boolean enabled){
        this.EnableRespawnAtPartner = enabled;
    }

    public int getRespawnAtPartnerBondLevel(){
        return Math.max(0, this.RespawnAtPartnerBondLevel);
    }

    public void setRespawnAtPartnerBondLevel(int level){
        this.RespawnAtPartnerBondLevel = level;
    }

    public int getRespawnAtPartnerCooldownSeconds(){
        return Math.max(0, this.RespawnAtPartnerCooldownSeconds);
    }

    public void setRespawnAtPartnerCooldownSeconds(int seconds){
        this.RespawnAtPartnerCooldownSeconds = seconds;
    }

    // === Couple channel, letters, HUD, records ===

    public boolean ifCoupleChannelEnabled(){
        return this.EnableCoupleChannel;
    }

    public void setCoupleChannelEnabled(boolean enabled){
        this.EnableCoupleChannel = enabled;
    }

    public boolean ifLoveLettersEnabled(){
        return this.EnableLoveLetters;
    }

    public void setLoveLettersEnabled(boolean enabled){
        this.EnableLoveLetters = enabled;
    }

    /** Clamped so a letter can never be long enough to be a denial-of-service on the chat window. */
    public int getLoveLetterMaxLength(){
        return Math.min(2000, Math.max(1, this.LoveLetterMaxLength));
    }

    public void setLoveLetterMaxLength(int length){
        this.LoveLetterMaxLength = length;
    }

    public boolean ifPartnerHudEnabled(){
        return this.EnablePartnerHud;
    }

    public void setPartnerHudEnabled(boolean enabled){
        this.EnablePartnerHud = enabled;
    }

    public double getPartnerHudRangeBlocks(){
        return Math.max(1.0, this.PartnerHudRangeBlocks);
    }

    public void setPartnerHudRangeBlocks(double blocks){
        this.PartnerHudRangeBlocks = blocks;
    }

    public boolean ifRecordsEnabled(){
        return this.EnableRecords;
    }

    public void setRecordsEnabled(boolean enabled){
        this.EnableRecords = enabled;
    }

    // === Ring tiers ===

    public boolean ifRingTiersEnabled(){
        return this.EnableRingTiers;
    }

    public void setRingTiersEnabled(boolean enabled){
        this.EnableRingTiers = enabled;
    }

    public boolean ifEngraveRings(){
        return this.EngraveRings;
    }

    public void setEngraveRings(boolean engrave){
        this.EngraveRings = engrave;
    }

    public double getGoldRingXpMultiplier(){
        return Math.max(1.0, this.GoldRingXpMultiplier);
    }

    public void setGoldRingXpMultiplier(double multiplier){
        this.GoldRingXpMultiplier = multiplier;
    }

    public int getGoldRingPerkDiscount(){
        return Math.max(0, this.GoldRingPerkDiscount);
    }

    public void setGoldRingPerkDiscount(int levels){
        this.GoldRingPerkDiscount = levels;
    }

    public double getDiamondRingXpMultiplier(){
        return Math.max(1.0, this.DiamondRingXpMultiplier);
    }

    public void setDiamondRingXpMultiplier(double multiplier){
        this.DiamondRingXpMultiplier = multiplier;
    }

    public int getDiamondRingPerkDiscount(){
        return Math.max(0, this.DiamondRingPerkDiscount);
    }

    public void setDiamondRingPerkDiscount(int levels){
        this.DiamondRingPerkDiscount = levels;
    }

    // === Vows ===

    public boolean ifVowsEnabled(){
        return this.EnableVows;
    }

    public void setVowsEnabled(boolean enabled){
        this.EnableVows = enabled;
    }

    public int getVowPerkDiscount(){
        return Math.max(0, this.VowPerkDiscount);
    }

    public void setVowPerkDiscount(int levels){
        this.VowPerkDiscount = levels;
    }

    public int getVowHearthChestBonusSlots(){
        return Math.max(0, this.VowHearthChestBonusSlots);
    }

    public void setVowHearthChestBonusSlots(int slots){
        this.VowHearthChestBonusSlots = slots;
    }

    public double getVowBladeShareBonusPercent(){
        return Math.max(0.0, this.VowBladeShareBonusPercent);
    }

    public void setVowBladeShareBonusPercent(double percent){
        this.VowBladeShareBonusPercent = percent;
    }

    /** Capped at 90% so a vow can never take a cooldown to nothing. */
    public double getVowWanderCooldownCutPercent(){
        return Math.min(90.0, Math.max(0.0, this.VowWanderCooldownCutPercent));
    }

    public void setVowWanderCooldownCutPercent(double percent){
        this.VowWanderCooldownCutPercent = percent;
    }

    // === Honeymoon ===

    public boolean ifHoneymoonEnabled(){
        return this.EnableHoneymoon;
    }

    public void setHoneymoonEnabled(boolean enabled){
        this.EnableHoneymoon = enabled;
    }

    public double getHoneymoonHours(){
        return Math.max(0.0, this.HoneymoonHours);
    }

    public void setHoneymoonHours(double hours){
        this.HoneymoonHours = hours;
    }

    public double getHoneymoonXpMultiplier(){
        return Math.max(1.0, this.HoneymoonXpMultiplier);
    }

    public void setHoneymoonXpMultiplier(double multiplier){
        this.HoneymoonXpMultiplier = multiplier;
    }

    // === Divorce weight ===

    public boolean ifDivorceWaitingPeriod(){
        return this.EnableDivorceWait;
    }

    public void setDivorceWaitingPeriod(boolean enabled){
        this.EnableDivorceWait = enabled;
    }

    public double getDivorceWaitingHours(){
        return Math.max(0.0, this.DivorceWaitingHours);
    }

    public void setDivorceWaitingHours(double hours){
        this.DivorceWaitingHours = hours;
    }

    /** Clamped to 0..1 - an uncontested divorce can be quicker, never slower. */
    public double getDivorceUncontestedFactor(){
        return Math.min(1.0, Math.max(0.0, this.DivorceUncontestedFactor));
    }

    public void setDivorceUncontestedFactor(double factor){
        this.DivorceUncontestedFactor = factor;
    }

    public int getRemarryCooldownSeconds(){
        return Math.max(0, this.RemarryCooldownSeconds);
    }

    public void setRemarryCooldownSeconds(int seconds){
        this.RemarryCooldownSeconds = seconds;
    }

    // === Renewing vows ===

    public boolean ifRenewEnabled(){
        return this.EnableRenew;
    }

    public void setRenewEnabled(boolean enabled){
        this.EnableRenew = enabled;
    }

    public double getRenewMinDays(){
        return Math.max(0.0, this.RenewMinDays);
    }

    public void setRenewMinDays(double days){
        this.RenewMinDays = days;
    }

    public double getRenewBondXp(){
        return Math.max(0.0, this.RenewBondXp);
    }

    public void setRenewBondXp(double xp){
        this.RenewBondXp = xp;
    }

    public int getRenewOfferSeconds(){
        return Math.max(5, this.RenewOfferSeconds);
    }

    public void setRenewOfferSeconds(int seconds){
        this.RenewOfferSeconds = seconds;
    }

    // === Anniversary rewards ===

    /** The item for a milestone by index into {@code BondManager.ANNIVERSARY_DAYS}. */
    @Nonnull
    public String getAnniversaryRewardItem(int milestoneIndex){
        String item = switch(milestoneIndex){
            case 0 -> this.AnniversaryReward1;
            case 1 -> this.AnniversaryReward7;
            case 2 -> this.AnniversaryReward30;
            case 3 -> this.AnniversaryReward365;
            default -> "";
        };
        return item == null ? "" : item;
    }

    public int getAnniversaryRewardQuantity(){
        return Math.max(0, this.AnniversaryRewardQuantity);
    }

    public void setAnniversaryRewardQuantity(int quantity){
        this.AnniversaryRewardQuantity = quantity;
    }

    // === Emotes ===

    public boolean ifEmotesEnabled(){
        return this.EnableEmotes;
    }

    public void setEmotesEnabled(boolean enabled){
        this.EnableEmotes = enabled;
    }

    public double getEmoteRangeBlocks(){
        return Math.max(1.0, this.EmoteRangeBlocks);
    }

    public void setEmoteRangeBlocks(double blocks){
        this.EmoteRangeBlocks = blocks;
    }

    public double getEmoteBroadcastRadiusBlocks(){
        return Math.max(0.0, this.EmoteBroadcastRadiusBlocks);
    }

    public void setEmoteBroadcastRadiusBlocks(double blocks){
        this.EmoteBroadcastRadiusBlocks = blocks;
    }

    public int getEmoteCooldownSeconds(){
        return Math.max(0, this.EmoteCooldownSeconds);
    }

    public void setEmoteCooldownSeconds(int seconds){
        this.EmoteCooldownSeconds = seconds;
    }

    @Nonnull
    public String getEmoteParticle(@Nonnull EmoteManager.Emote emote){
        String id = switch(emote){
            case HUG -> this.EmoteHugParticle;
            case KISS -> this.EmoteKissParticle;
            case DANCE -> this.EmoteDanceParticle;
        };
        return id == null ? "" : id;
    }

    @Nonnull
    public String getEmoteSound(@Nonnull EmoteManager.Emote emote){
        String id = switch(emote){
            case HUG -> this.EmoteHugSound;
            case KISS -> this.EmoteKissSound;
            case DANCE -> this.EmoteDanceSound;
        };
        return id == null ? "" : id;
    }
}
