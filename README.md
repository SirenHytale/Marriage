Version 2.1.0

<span style="color: #eccafa;">Want players to be able to <span style="color: #b96ad9;"><strong>Marry</strong></span>? Now you can with Marriage, this plugin allows the <span style="color: #b96ad9;"><strong>user to marry other users</strong></span>!</span>

<span style="color: #eccafa;">And it goes somewhere. A marriage grows a <span style="color: #b96ad9;"><strong>bond</strong></span> that deepens the longer it lasts, unlocks <span style="color: #b96ad9;"><strong>perks</strong></span> as it does, and is shaped by the <span style="color: #b96ad9;"><strong>vow</strong></span> the couple swears. All of it from one in-game menu.</span>

<span style="color: #eccafa;">Built for Hytale server <span style="color: #b96ad9;"><strong>0.5.x</strong></span>. No other mods required - drop it in and it works. Upgrading from an older version? <span style="color: #b96ad9;"><strong>Existing marriages and your config carry over</strong></span>, nothing to re-run.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

## <span style="color: #b96ad9;"><strong>Features</strong>:</span>

*   <span style="color: #eccafa;">Two players can marry each other, with requests that expire on their own.</span>
*   <span style="color: #eccafa;">A rose-themed <span style="color: #b96ad9;"><strong>Marriage menu</strong></span> (/marriage) that does <strong>everything the commands do</strong> - propose, answer a proposal, teleport, gift, write letters, set your home, open your chest, divorce. Players never have to type a command.</span>
*   <span style="color: #eccafa;">A <span style="color: #b96ad9;"><strong>bond</strong></span> that deepens with every hour married, every minute spent together, every gift and every kill made side by side - and unlocks perks as it grows.</span>
*   <span style="color: #eccafa;">Seven bond perks: <span style="color: #b96ad9;"><strong>death alerts</strong></span>, your <span style="color: #b96ad9;"><strong>partner on the map</strong></span>, a <span style="color: #b96ad9;"><strong>shared home</strong></span>, the <span style="color: #b96ad9;"><strong>bond of pain</strong></span>, <span style="color: #b96ad9;"><strong>last stand</strong></span>, the <span style="color: #b96ad9;"><strong>Hope Chest</strong></span>, and <span style="color: #b96ad9;"><strong>respawning at your partner</strong></span>.</span>
*   <span style="color: #eccafa;">A <span style="color: #b96ad9;"><strong>vow</strong></span> - Hearth, Blade or Wandering - deciding which perks reach you first. <span style="color: #b96ad9;"><strong>Renew your vows</strong></span> later to deepen the bond, reset the decay, upgrade your band and swear anew.</span>
*   <span style="color: #eccafa;"><span style="color: #b96ad9;"><strong>Anniversaries</strong></span> at 1, 7, 30 and 365 days, with gifts you can actually hold - and a <span style="color: #b96ad9;"><strong>honeymoon</strong></span> for the first day.</span>
*   <span style="color: #eccafa;"><span style="color: #b96ad9;"><strong>Divorce with weight</strong></span> - mutual is instant, one-sided waits, and the other partner may agree or contest. Plus a remarry cooldown and a remembered history.</span>
*   <span style="color: #eccafa;"><span style="color: #b96ad9;"><strong>Ring tiers</strong></span> - Copper, Gold and Diamond bands. A finer band deepens the bond faster and opens perks sooner, and a kept band is engraved with both names.</span>
*   <span style="color: #eccafa;"><span style="color: #b96ad9;"><strong>Love Letters</strong></span> you write, seal, gift and read - the words live on the item itself.</span>
*   <span style="color: #eccafa;">A persistent <span style="color: #b96ad9;"><strong>couple chat channel</strong></span>, <span style="color: #b96ad9;"><strong>gifts</strong></span>, <span style="color: #b96ad9;"><strong>emotes</strong></span> (hug, kiss, dance), and a <span style="color: #b96ad9;"><strong>partner plate</strong></span> showing their health while they are near.</span>
*   <span style="color: #eccafa;">A <span style="color: #b96ad9;"><strong>global married list</strong></span> including offline couples, plus a <span style="color: #b96ad9;"><strong>Records</strong></span> page - longest marriage, most generous, most divorced.</span>
*   <span style="color: #eccafa;"><span style="color: #b96ad9;"><strong>Partner PvP protection</strong></span> and <span style="color: #b96ad9;"><strong>partner regeneration</strong></span>, both configurable.</span>
*   <span style="color: #eccafa;">Full <span style="color: #b96ad9;"><strong>admin tooling</strong></span>: force marry, force divorce and force info (offline players included), plus an in-game config editor.</span>
*   <span style="color: #eccafa;">A <span style="color: #b96ad9;"><strong>public API</strong></span> (MarriageAPI) with lookups, bond reads, cancellable events, and admin operations for other mods.</span>
*   <span style="color: #eccafa;">Over 100 config values, permissions, and English + Chinese localization.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

## <span style="color: #b96ad9;"><strong>Commands, Config, and&nbsp;Permissions</strong>:</span>

### <span style="color: #eccafa;">Commands Extra Info:</span>

| <span style="color: #b96ad9;">Command:</span> |<span style="color: #b96ad9;">Description:</span> |<span style="color: #b96ad9;">Permission:</span> |
| ------------------------ |------------------------------------------------------------------- |----------------------------------- |
| <span style="color: #eccafa;">/marriage</span> |<span style="color: #eccafa;">Opens the Marriage menu - everything below has a button in it.</span> |<span style="color: #eccafa;">marriage (granted automatically)</span> |
| <span style="color: #eccafa;">/marry</span> |<span style="color: #eccafa;">The marry command line.</span> |&nbsp; |
| <span style="color: #eccafa;">/marry player {Username}</span> |<span style="color: #eccafa;">Send a marry request, or accept one they already sent you.</span> |<span style="color: #eccafa;">May require: marriage.marry</span> |
| <span style="color: #eccafa;">/marry partner</span> |<span style="color: #eccafa;">See who you're married to and for how long.</span> |&nbsp; |
| <span style="color: #eccafa;">/marry bond</span> |<span style="color: #eccafa;">See how deep your bond runs and which perks it has unlocked.</span> |&nbsp; |
| <span style="color: #eccafa;">/marry list</span> |<span style="color: #eccafa;">Every married couple on the server, online or not.</span> |&nbsp; |
| <span style="color: #eccafa;">/marry tp</span> |<span style="color: #eccafa;">Teleport to your partner.</span> |<span style="color: #eccafa;">May require: marriage.tp</span> |
| <span style="color: #eccafa;">/marry msg {msg}</span> |<span style="color: #eccafa;">Send a private message to your partner.</span> |<span style="color: #eccafa;">May require: marriage.msg</span> |
| <span style="color: #eccafa;">/marry channel</span> |<span style="color: #eccafa;">Toggle the couple channel - everything you type goes to your partner.</span> |<span style="color: #eccafa;">May require: marriage.msg</span> |
| <span style="color: #eccafa;">/marry gift</span> |<span style="color: #eccafa;">Give the item stack you are holding to your partner.</span> |<span style="color: #eccafa;">May require: marriage.gift</span> |
| <span style="color: #eccafa;">/marry letter</span> |<span style="color: #eccafa;">Open the Love Letter in your hand, to write it or to read it.</span> |&nbsp; |
| <span style="color: #eccafa;">/marry home</span> |<span style="color: #eccafa;">Travel to the home you and your partner share.</span> |<span style="color: #eccafa;">May require: marriage.home</span> |
| <span style="color: #eccafa;">/marry sethome</span> |<span style="color: #eccafa;">Make where you stand your shared home.</span> |<span style="color: #eccafa;">May require: marriage.home</span> |
| <span style="color: #eccafa;">/marry clearhome</span> |<span style="color: #eccafa;">Forget your shared home.</span> |<span style="color: #eccafa;">May require: marriage.home</span> |
| <span style="color: #eccafa;">/marry chest</span> |<span style="color: #eccafa;">Open the Hope Chest you share - from anywhere, both at once.</span> |<span style="color: #eccafa;">May require: marriage.chest</span> |
| <span style="color: #eccafa;">/marry renew</span> |<span style="color: #eccafa;">Renew your vows. Both of you must ask.</span> |&nbsp; |
| <span style="color: #eccafa;">/marry hug, kiss, dance</span> |<span style="color: #eccafa;">Emotes, with hearts and a sound for anyone close enough to see.</span> |&nbsp; |
| <span style="color: #eccafa;">/marry divorce</span> |<span style="color: #eccafa;">File for divorce, or agree to one already filed. Works while your partner is offline.</span> |<span style="color: #eccafa;">May require: marriage.divorce</span> |
| <span style="color: #eccafa;">/marriage requirering {true/false}</span> |<span style="color: #eccafa;">Toggle whether a ring is required to marry.</span> |<span style="color: #eccafa;">Require: marriage.admin.requirering</span> |
| <span style="color: #eccafa;">/marriage force marry {Username} {Username}</span> |<span style="color: #eccafa;">Marry two online players outright - no proposal, no ring.</span> |<span style="color: #eccafa;">Require: marriage.admin.force</span> |
| <span style="color: #eccafa;">/marriage force divorce {Username}</span> |<span style="color: #eccafa;">End a player's marriage, online or not.</span> |<span style="color: #eccafa;">Require: marriage.admin.force</span> |
| <span style="color: #eccafa;">/marriage force info {Username}</span> |<span style="color: #eccafa;">Everything the server knows about that player's marriage. Works offline.</span> |<span style="color: #eccafa;">Require: marriage.admin.force</span> |

### <span style="color: #eccafa;">Permissions Extra Info:</span>

| <span style="color: #b96ad9;">Permission:</span> |<span style="color: #b96ad9;">Description:</span> |
| -------------------------- |-------------------------------------------------------------------------------------------------------------------- |
| <span style="color: #eccafa;">marriage</span> |<span style="color: #eccafa;">Allows the player to open the Marriage menu. Granted to the default groups automatically once (config: Grant-Menu-Permission).</span> |
| <span style="color: #eccafa;">marriage.marry</span> |<span style="color: #eccafa;">Allows /marry player, when config option Use-Permissions-For-Commands is set as true.</span> |
| <span style="color: #eccafa;">marriage.divorce</span> |<span style="color: #eccafa;">Allows /marry divorce, when config option Use-Permissions-For-Commands is set as true.</span> |
| <span style="color: #eccafa;">marriage.tp</span> |<span style="color: #eccafa;">Allows /marry tp, when config option Use-Permissions-For-Commands is set as true.</span> |
| <span style="color: #eccafa;">marriage.msg</span> |<span style="color: #eccafa;">Allows /marry msg and /marry channel, when config option Use-Permissions-For-Commands is set as true.</span> |
| <span style="color: #eccafa;">marriage.gift</span> |<span style="color: #eccafa;">Allows /marry gift, when config option Use-Permissions-For-Commands is set as true.</span> |
| <span style="color: #eccafa;">marriage.home</span> |<span style="color: #eccafa;">Allows /marry home, sethome and clearhome, when config option Use-Permissions-For-Commands is set as true.</span> |
| <span style="color: #eccafa;">marriage.chest</span> |<span style="color: #eccafa;">Allows /marry chest, when config option Use-Permissions-For-Commands is set as true.</span> |
| <span style="color: #eccafa;">marriage.admin</span> |<span style="color: #eccafa;">Shows the Admin page inside the Marriage menu (in-game config editor), and a Divorce control on the Married Couples list.</span> |
| <span style="color: #eccafa;">marriage.admin.requirering</span> |<span style="color: #eccafa;">Allows /marriage requirering.</span> |
| <span style="color: #eccafa;">marriage.admin.force</span> |<span style="color: #eccafa;">Allows /marriage force marry, force divorce and force info.</span> |

### <span style="color: #eccafa;">Config Extra Info:</span>

<span style="color: #eccafa;">Config version 5. There are over 100 values now, so rather than list them all: <span style="color: #b96ad9;"><strong>every value has its Description sitting right next to it in the config file</strong></span>, and <span style="color: #b96ad9;"><strong>every one is editable in game</strong></span> on the menu's Admin page (edits are pending until you press Save). Your existing config is upgraded in place.</span>

| <span style="color: #b96ad9;">Section:</span> |<span style="color: #b96ad9;">What it controls:</span> |
| ------------------------------ |------------------------------------------------------------------------------------------- |
| <span style="color: #eccafa;">General</span> |<span style="color: #eccafa;">"Require-Ring-To-Marry", "Consume-Ring-On-Marry", "Announce-Marriages", "Announce-Divorces", "Use-Permissions-For-Commands".</span> |
| <span style="color: #eccafa;">Requests &amp; Divorce</span> |<span style="color: #eccafa;">"Request-Expiry-Seconds", "Divorce-Confirm-Seconds".</span> |
| <span style="color: #eccafa;">Divorce Weight</span> |<span style="color: #eccafa;">"Enable-Divorce-Waiting-Period", "Divorce-Waiting-Hours", "Divorce-Uncontested-Factor", "Remarry-Cooldown-Seconds".</span> |
| <span style="color: #eccafa;">Menu</span> |<span style="color: #eccafa;">"Enable-Menu", "Grant-Menu-Permission".</span> |
| <span style="color: #eccafa;">PvP Protection</span> |<span style="color: #eccafa;">"Enable-Partner-Pvp-Protection", "Default-Pvp-Protection", "Pvp-Protection-Message".</span> |
| <span style="color: #eccafa;">Partner Regeneration</span> |<span style="color: #eccafa;">"Enable-Partner-Regen", "Regen-Radius-Blocks", "Regen-Interval-Seconds", "Regen-Amount-Per-Interval".</span> |
| <span style="color: #eccafa;">Gifts, Teleport &amp; Messages</span> |<span style="color: #eccafa;">"Enable-Gift", "Gift-Range-Blocks", "Gift-Cooldown-Seconds", "Enable-Partner-Tp", "Tp-Cooldown-Seconds", "Enable-Partner-Msg".</span> |
| <span style="color: #eccafa;">The Bond</span> |<span style="color: #eccafa;">"Enable-Bond", "Bond-Max-Level", "Bond-Xp-Per-Level", and the XP rates for time married, time together, gifts and co-op kills. Turn Enable-Bond off and every enabled perk is simply available from the wedding day.</span> |
| <span style="color: #eccafa;">Anniversaries &amp; Honeymoon</span> |<span style="color: #eccafa;">"Enable-Anniversaries", "Announce-Anniversaries", the four "Anniversary-Reward-Item-*" values, "Enable-Honeymoon", "Honeymoon-Hours", "Honeymoon-Xp-Multiplier".</span> |
| <span style="color: #eccafa;">Bond Decay</span> |<span style="color: #eccafa;">"Enable-Bond-Decay" (off by default), its grace period, drain rate and floor.</span> |
| <span style="color: #eccafa;">Vows</span> |<span style="color: #eccafa;">"Enable-Vows", "Vow-Perk-Discount", and each vow's bonus. Plus "Enable-Renew-Vows", "Renew-Min-Days", "Renew-Bond-Xp".</span> |
| <span style="color: #eccafa;">Ring Tiers</span> |<span style="color: #eccafa;">"Enable-Ring-Tiers", "Engrave-Rings", and the XP multiplier and perk discount for the Gold and Diamond bands.</span> |
| <span style="color: #eccafa;">The Perks</span> |<span style="color: #eccafa;">Each of the seven has its own "Enable-*" switch and a "*-Bond-Level" - set a level to 0 to hand that perk out on the wedding day.</span> |
| <span style="color: #eccafa;">Channel, Letters, Plate &amp; Records</span> |<span style="color: #eccafa;">"Enable-Couple-Channel", "Enable-Love-Letters", "Love-Letter-Max-Length", "Enable-Partner-Hud", "Partner-Hud-Range-Blocks", "Enable-Records".</span> |
| <span style="color: #eccafa;">Emotes</span> |<span style="color: #eccafa;">"Enable-Emotes", range, broadcast radius, cooldown, and the particle and sound for each emote.</span> |

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

## <span style="color: #b96ad9;"><strong>Works With Cultivation</strong>:</span>

<span style="color: #eccafa;">Running <span style="color: #b96ad9;"><strong>Cultivation</strong></span> too? Its Partnered Cultivation (双修) gives married couples a faster Qi rate while they meditate together - and Marriage now shows it. A <span style="color: #b96ad9;"><strong>Dual Cultivation</strong></span> page appears in the menu by itself, with your Qi multiplier, how close you must sit, the realm you need, and whether the two of you are dual cultivating right now. On a server without Cultivation the page simply never appears.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

## <span style="color: #b96ad9;"><strong>For Mod Developers</strong>:</span>

<span style="color: #eccafa;">MarriageAPI (plugin.siren.API) gives you isMarried, getPartner, getMarriage stats (married since, gifts, teleports, love notes, bond level and progress, time together), getAllMarriages including offline couples, server totals, bond reads (getBondLevel, getBondProgress, addBondXp, hasPerk) and marry/divorce operations. MarriageEvents lets you listen to - and cancel - marriages, divorces and gifts, and hear about bond level changes, anniversaries and shared homes. MarriageUIRegistry lets your mod add its own pages to the Marriage menu.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

## <span style="color: #b96ad9;"><strong>Support the Creator of Marriage</strong>:</span>

<span style="color: #eccafa;">You can support me by using my creator code <span style="color: #b96ad9;"><strong>SIREN</strong></span> at checkout.</span>

<span style="color: #eccafa;">Marriage is free and will always stay free. If you would like to support its development, my store -- <span style="color: #b96ad9;"><strong>The Treasure Pavilion</strong></span> -- is at <strong><a href="https://xianxia.dev/store/">https://xianxia.dev/store/</a></strong>.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>
