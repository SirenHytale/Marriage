Version 2.0.0

<span style="color: #eccafa;">Want players to be able to <span style="color: #b96ad9;"><strong>Marry</strong></span>? Now you can with Marriage, this plugin allows the <span style="color: #b96ad9;"><strong>user to marry other users</strong></span>!</span>

<span style="color: #eccafa;">Marriage 2.0 is a full rework: a brand new in-game menu, a server-wide marriage registry, partner perks, gifts, and a public API for other mods.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

## <span style="color: #b96ad9;"><strong>Features</strong>:</span>

*   <span style="color: #eccafa;">Two players can marry each other, with requests that expire on their own.</span>
*   <span style="color: #eccafa;">A rose-themed <span style="color: #b96ad9;"><strong>Marriage menu</strong></span> (/marriage): your marriage overview, every married couple on the server, settings toggles, and an in-game admin config editor.</span>
*   <span style="color: #eccafa;">A <span style="color: #b96ad9;"><strong>global married list</strong></span> that includes offline couples - marriages survive restarts in their own registry file.</span>
*   <span style="color: #eccafa;"><span style="color: #b96ad9;"><strong>Partner PvP protection</strong></span> - married partners can't hurt each other (per-couple toggle).</span>
*   <span style="color: #eccafa;"><span style="color: #b96ad9;"><strong>Partner regeneration</strong></span> - regenerate health faster while near your beloved.</span>
*   <span style="color: #eccafa;"><span style="color: #b96ad9;"><strong>Gifts</strong></span> - hand the item you're holding straight to your partner (/marry gift).</span>
*   <span style="color: #eccafa;">Special commands for married couples [marry tp, msg, and gift].</span>
*   <span style="color: #eccafa;">A craftable Marriage Ring, optional ring requirement, and optional ring consumption.</span>
*   <span style="color: #eccafa;">A <span style="color: #b96ad9;"><strong>public API</strong></span> (MarriageAPI) with marriage lookups, stats, cancellable events, and admin operations for other mods.</span>
*   <span style="color: #eccafa;">Lots of config values, permissions, and English + Chinese localization.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

## <span style="color: #b96ad9;"><strong>Commands, Config, and&nbsp;Permissions</strong>:</span>

### <span style="color: #eccafa;">Commands Extra Info:</span>

*   <span style="color: #eccafa;">/marriage</span>
    *   <span style="color: #eccafa;">Opens the Marriage menu.</span>
    *   <span style="color: #eccafa;">/marriage requirering {true/false}</span>
        *   <span style="color: #eccafa;">Admin toggle to require a ring to marry. Requires permission: marriage.admin.requirering</span>
*   <span style="color: #eccafa;">/marry</span>
    *   <span style="color: #eccafa;">The marry command line</span>
    *   <span style="color: #eccafa;">/marry player {Username}</span>
        *   <span style="color: #eccafa;">Send a marry request or accept a marry request.</span>
        *   <span style="color: #eccafa;">May require permission: marriage.marry</span>
    *   <span style="color: #eccafa;">/marry partner</span>
        *   <span style="color: #eccafa;">See who you're married to and for how long.</span>
    *   <span style="color: #eccafa;">/marry tp</span>
        *   <span style="color: #eccafa;">Teleports the player to their married partner.</span>
        *   <span style="color: #eccafa;">May require permission: marriage.tp</span>
    *   <span style="color: #eccafa;">/marry msg {msg}</span>
        *   <span style="color: #eccafa;">Send a private message to your married partner.</span>
        *   <span style="color: #eccafa;">May require permission: marriage.msg</span>
    *   <span style="color: #eccafa;">/marry gift</span>
        *   <span style="color: #eccafa;">Give the item stack you are holding to your married partner.</span>
        *   <span style="color: #eccafa;">May require permission: marriage.gift</span>
    *   <span style="color: #eccafa;">/marry divorce</span>
        *   <span style="color: #eccafa;">Divorce your partner, has a confirmation. Works even while your partner is offline.</span>
        *   <span style="color: #eccafa;">May require permission: marriage.divorce</span>
    *   <span style="color: #eccafa;">/marry list</span>
        *   <span style="color: #eccafa;">Get a list of every married couple on the server, online or not.</span>

### <span style="color: #eccafa;">Permissions Extra Info:</span>

*   <span style="color: #eccafa;">marriage</span>
    *   <span style="color: #eccafa;">Allows the player to open the Marriage menu. Granted to the default groups automatically once (config: Grant-Menu-Permission).</span>
*   <span style="color: #eccafa;">marriage.marry / marriage.divorce / marriage.tp / marriage.msg / marriage.gift</span>
    *   <span style="color: #eccafa;">Gate the matching /marry subcommand when config option Use-Permissions-For-Commands is set as true.</span>
*   <span style="color: #eccafa;">marriage.admin</span>
    *   <span style="color: #eccafa;">Shows the Admin page inside the Marriage menu (in-game config editor).</span>
*   <span style="color: #eccafa;">marriage.admin.requirering</span>
    *   <span style="color: #eccafa;">Allows /marriage requirering.</span>

### <span style="color: #eccafa;">Config Extra Info:</span>

<span style="color: #eccafa;">Config version 4. Every value has a Description right next to it in the config file, and admins can edit everything in-game on the menu's Admin page. Highlights:</span>

*   <span style="color: #eccafa;">"Require-Ring-To-Marry" / "Consume-Ring-On-Marry"</span>
    *   <span style="color: #eccafa;">Ask for a held Marriage Ring to marry, and optionally consume it.</span>
*   <span style="color: #eccafa;">"Announce-Marriages" / "Announce-Divorces"</span>
    *   <span style="color: #eccafa;">Server-wide announcements.</span>
*   <span style="color: #eccafa;">"Request-Expiry-Seconds" / "Divorce-Confirm-Seconds"</span>
    *   <span style="color: #eccafa;">How long requests and divorce confirmations stay open.</span>
*   <span style="color: #eccafa;">"Enable-Partner-Pvp-Protection" / "Default-Pvp-Protection" / "Pvp-Protection-Message"</span>
    *   <span style="color: #eccafa;">Partner PvP protection master switch, the default for new couples, and the attacker notice.</span>
*   <span style="color: #eccafa;">"Enable-Partner-Regen" / "Regen-Radius-Blocks" / "Regen-Interval-Seconds" / "Regen-Amount-Per-Interval"</span>
    *   <span style="color: #eccafa;">Partner regeneration and its tuning.</span>
*   <span style="color: #eccafa;">"Enable-Gift" / "Gift-Range-Blocks" / "Gift-Cooldown-Seconds"</span>
    *   <span style="color: #eccafa;">Gifting, its range (0 = anywhere), and its cooldown.</span>
*   <span style="color: #eccafa;">"Enable-Partner-Tp" / "Tp-Cooldown-Seconds" / "Enable-Partner-Msg" / "Enable-Menu"</span>
    *   <span style="color: #eccafa;">Feature switches for teleports, messages, and the menu.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

## <span style="color: #b96ad9;"><strong>For Mod Developers</strong>:</span>

<span style="color: #eccafa;">MarriageAPI (plugin.siren.API) gives you isMarried, getPartner, getMarriage stats (married since, gifts, teleports, love notes), getAllMarriages including offline couples, server totals, and marry/divorce operations. MarriageEvents lets you listen to (and cancel) marriages, divorces, and gifts. MarriageUIRegistry lets your mod add its own pages to the Marriage menu.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

## <span style="color: #b96ad9;"><strong>Support the Creator of Mermaids</strong>:</span>

<span style="color: #eccafa;">You can support me by using my creator code <span style="color: #b96ad9;"><strong>SIREN</strong></span> at checkout.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>
