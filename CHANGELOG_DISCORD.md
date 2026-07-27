# Marriage Discord Changelog - v2.0.0

Discord chat markdown doesn't support inline colors (that's a CurseForge-only HTML trick), so this uses Discord's native bold/heading/bullet formatting instead. Copy-paste the block below directly into Discord.

Note: a plain user message caps out at 2000 characters. The block below fits inside that, so it posts as a single message -- no splitting needed (and it's comfortably one webhook embed too, those cap at 4096).

---

## 💍 Marriage v2.0.0 - Vows, Reworked

Everything the mod did before is still here -- rings, proposals, teleports, whispers -- but the machinery underneath is new, and a lot arrived on top of it.

**Your existing marriages carry over.** Each one imports itself the first time a partner logs in. Nothing to reset.

**💗 The Marriage Menu**
- `/marriage` opens a rose-themed menu: your partner, how long you've lasted, gifts, teleports and love notes -- with **Teleport, Gift and Divorce** one click away.
- Proposals show up right there with **Accept** and **Decline** buttons.
- **Married Couples** lists every couple on the server, online or not, plus totals: couples now, marriages ever, divorces.

**⚔️ Being Married Now Does Something**
- **PvP protection** -- partners can't hurt each other, melee or arrows. Per-couple toggle, in case your love language is combat.
- **Partner regeneration** -- stay near your beloved and your health slowly returns.
- **Gifts** -- `/marry gift` hands your held stack to your partner. Full inventory? It lands at their feet, never lost.

**📖 A Registry Instead Of A Guess**
- Marriages live in a **server-wide file** now, so `/marry list` finally shows every couple, not just who's online.
- **Divorce while your partner is offline** -- they're told at their next login.
- Proposals **expire on their own**, and you can refuse requests entirely.

**⚙️ For Owners And Modders**
- The config went from 3 real options to **over 20**, all editable **in-game** on the Admin page.
- A public **MarriageAPI**: lookups, stats, marry/divorce, cancellable events -- and your mod can add its own pages to the menu.
- Running **Cultivation**? Partnered Cultivation reads marriages through it.
- Fully translated into **Simplified Chinese**.

@Marriage Changelog
<https://www.curseforge.com/hytale/mods/marriage/files/???????>
