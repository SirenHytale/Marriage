package plugin.siren.Utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import plugin.siren.Marriage;

public class MarriageConfig {

    private static final String INFORMATION_DEFAULT = "Confused about what one of these statements do? Go to https://mermaids.dev/marriage/config/ or check out the Marriage page on the Curseforge website and scroll down to Config Extra Info.";
    private static final String WEBSITE_DEFAULT = "https://mermaids.dev/marriage/";
    private static final String DOWNLOAD_SITE_DEFAULT = "https://www.curseforge.com/hytale/mods/marriage";
    private static final int CONFIG_VERSION_DEFAULT = 4;

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
}
