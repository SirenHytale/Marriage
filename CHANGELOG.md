# Marriage v2.0.0 - Changelog (CurseForge)

Paste the block below into the CurseForge "Changelog" field when uploading the v2.0.0 file.

---

## <span style="color: #b96ad9;"><strong>v2.0.0 - Vows, Reworked</strong></span>

<span style="color: #eccafa;">Marriage grew up. Everything the mod did before is still here -- rings, proposals, teleports, whispers, the announcement when two names turn pink in chat -- but the machinery underneath is new, and a lot arrived on top of it: a full in-game menu, partner perks, gifts, and a public API.</span>

<span style="color: #eccafa;"><strong>Existing marriages carry over.</strong> The first time each partner logs in on 2.0.0, their marriage is imported into the new server-wide registry automatically. Nothing to reset, nothing to re-run.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

### <span style="color: #b96ad9;"><strong>The Marriage Menu</strong></span>

*   <span style="color: #eccafa;">Type <strong>/marriage</strong> and a rose-themed menu opens: <strong>Our Marriage</strong> (your partner and whether they're online, how long you've been married, gifts given and received, teleports used, love notes sent -- plus one-click Teleport, Gift, and a click-twice-to-confirm Divorce).</span>
*   <span style="color: #eccafa;">Received a proposal? It appears right there with <strong>Accept</strong> and <strong>Decline</strong> buttons, and a live countdown to when it expires.</span>
*   <span style="color: #eccafa;"><strong>Married Couples</strong> lists every couple on the server -- online or not -- with how long each has lasted, plus server totals: couples now, marriages ever, divorces.</span>
*   <span style="color: #eccafa;"><strong>Settings</strong> holds your personal toggles, and admins get a full <strong>in-game config editor</strong> (every value, pending-until-Save, with Revert).</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

### <span style="color: #b96ad9;"><strong>Being Married Now Does Something</strong></span>

*   <span style="color: #eccafa;"><strong>PvP protection</strong> -- married partners cannot hurt each other, melee or arrows. Each couple can turn it off if their love language is combat. Attackers are told, gently, why nothing happened.</span>
*   <span style="color: #eccafa;"><strong>Partner regeneration</strong> -- stay near your beloved and your health slowly returns. Radius, interval, and amount are all configurable.</span>
*   <span style="color: #eccafa;"><strong>Gifts</strong> -- <strong>/marry gift</strong> (or the menu button) hands the item stack you're holding straight to your partner. Full inventory? It lands at their feet, never lost. Configurable range (0 = anywhere, even across worlds) and cooldown.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

### <span style="color: #b96ad9;"><strong>A Registry Instead of a Guess</strong></span>

*   <span style="color: #eccafa;">Marriages now live in a <strong>server-wide registry file</strong>, not just on each player. <strong>/marry list</strong> finally shows every couple, not just whoever is online.</span>
*   <span style="color: #eccafa;">You can <strong>divorce while your partner is offline</strong>; they're told what happened next time they log in.</span>
*   <span style="color: #eccafa;">Proposals now <strong>expire on their own</strong> (configurable), and a new personal toggle lets you refuse marriage requests entirely.</span>
*   <span style="color: #eccafa;">New optional rule: <strong>consume the rings</strong> when the wedding completes.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

### <span style="color: #b96ad9;"><strong>For Server Owners & Mod Authors</strong></span>

*   <span style="color: #eccafa;">The config grew from 3 real options to <strong>over 20</strong>, every one with its description sitting next to it in the file -- and all of it editable in-game on the Admin page. Your existing config is upgraded in place to version 4.</span>
*   <span style="color: #eccafa;">A public <strong>MarriageAPI</strong>: lookups, stats, the full couples list, marry/divorce operations, and cancellable events for marriages, divorces, and gifts. Your mod can even add its own pages to the Marriage menu.</span>
*   <span style="color: #eccafa;">Ships in <strong>English and Chinese</strong>.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>

### <span style="color: #b96ad9;"><strong>Notes</strong></span>

*   <span style="color: #eccafa;">Built for Hytale server <strong>0.5.x</strong>.</span>
*   <span style="color: #eccafa;">Running <strong>Cultivation</strong>? Its Partnered Cultivation (双修) feature reads marriages through this API -- married couples meditating together cultivate faster.</span>

<span style="color: #843fa1;">===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===--===</span>
