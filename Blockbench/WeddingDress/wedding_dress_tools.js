// Wedding Dress review helpers, layered on Cultivation/Blockbench/Items/cultivation_items_tools.js (load that first).
// Blockbench is shared with other sessions, and ciWear names its project 'Wear' and closes any other 'Wear' - two
// sessions rendering at once swap each other's model mid-render. mdWear builds its own uniquely named project, and
// every render below runs in one synchronous stretch after the single texture-load await, so no other MCP call can
// switch the active project between loading and rendering. It closes only its own project and reselects the one
// that was active before.
//
//   mdReview(dir, views, o)   wear the dress, render each view -> <dir>_review\<prefix><view>.png
//                             o.anim = clip path under HytaleAssets/Common, o.frames = [frame, ...] poses each view
//                             at each frame (file <prefix><view>_f<frame>.png)
window.MD_NAME = 'Marriage_WeddingDress';

// the player's greyscale skin tinted to a skin tone the way the client tints it (multiply), so cut lace is judged
// against skin rather than gray
window.mdSkin = async function (tint) {
  const fs = require('fs');
  const img = new Image();
  img.src = 'data:image/png;base64,' +
    fs.readFileSync(CULT_COMMON + 'Characters\\Player_Textures\\Player_Greyscale.png').toString('base64');
  await new Promise(r => { img.onload = r; });
  const cv = document.createElement('canvas');
  cv.width = img.width; cv.height = img.height;
  const cx = cv.getContext('2d');
  cx.drawImage(img, 0, 0);
  const d = cx.getImageData(0, 0, cv.width, cv.height);
  for (let i = 0; i < d.data.length; i += 4) {
    d.data[i] *= tint[0]; d.data[i + 1] *= tint[1]; d.data[i + 2] *= tint[2];
  }
  cx.putImageData(d, 0, 0);
  return cv.toDataURL('image/png');
};

window.mdWear = async function (dir, tint) {
  const fs = require('fs');
  const p = CULT_COMMON + 'Characters\\Player.blockymodel';
  const skinUrl = await mdSkin(tint || [1.0, 0.8, 0.66]);
  const proj = new ModelProject({ format: Formats.hytale_character });
  proj.select();
  Codecs.blockymodel.parse(JSON.parse(fs.readFileSync(p, 'utf-8')), p);
  Project.name = 'MarriageWear_' + Date.now();
  Texture.all.slice().forEach(t => t.remove(true));
  const skin = new Texture({ name: 'skin.png' }).fromDataURL(skinUrl).add(false);
  skin.uv_width = 256; skin.uv_height = 128; skin.use_as_default = true;
  const ap = dir + MD_NAME + '.blockymodel';
  const content = Codecs.blockymodel.parse(JSON.parse(fs.readFileSync(ap, 'utf-8')), ap, { attachment: MD_NAME });
  (content.new_textures || []).forEach(t => t.remove(true));
  const png = fs.readFileSync(dir + 'Textures\\' + MD_NAME + '.png');
  const tex = new Texture({ name: MD_NAME + '.png' }).fromDataURL('data:image/png;base64,' + png.toString('base64')).add(false);
  tex.uv_width = png.readUInt32BE(16); tex.uv_height = png.readUInt32BE(20);
  const ng = content.new_groups;
  const col = new Collection({ name: MD_NAME, children: ng.filter(g => !ng.includes(g.parent)).map(g => g.uuid),
    export_codec: 'blockymodel', visibility: true }).add();
  col.texture = tex.uuid;
  ng.forEach(g => g.children.forEach(c => {
    if (c instanceof Cube) for (const f in c.faces) if (c.faces[f].texture !== null) c.faces[f].texture = tex.uuid;
  }));
  return proj;
};

// the inventory icon: the dress alone (its own .bbmodel, rest pose), on a clear background -> <dir>_icons\<name>.png
// at o.px (install_wedding_dress.py downsamples it to 64, as vanilla's ItemsGenerated icons are)
window.mdIcon = async function (dir, o) {
  o = o || {};
  const fs = require('fs');
  const prev = Project;
  if (!fs.existsSync(dir + '_icons')) fs.mkdirSync(dir + '_icons');
  ciOpen(dir + MD_NAME + '.bbmodel');
  const proj = Project;
  await new Promise(r => setTimeout(r, 400));
  // --- synchronous from here ---
  if (!ModelProject.all.includes(proj)) return { error: 'icon project was closed by another caller' };
  proj.select();
  Canvas.updateAll();
  try {
    const f = ciFit(o.dir, o.margin === undefined ? 0.04 : o.margin, o.roll);
    return ciRender(dir + '_icons\\' + MD_NAME + '.png',
      { dir: o.dir, roll: o.roll, target: f.target, size: f.size, w: o.px || 256, h: o.px || 256, bg: null });
  } finally {
    proj.saved = true;
    proj.close(true);
    if (prev && ModelProject.all.includes(prev)) prev.select();
  }
};

window.mdReview = async function (dir, views, o) {
  o = o || {};
  const fs = require('fs');
  const prev = Project;
  const prevMode = Modes.selected && Modes.selected.id;
  if (!fs.existsSync(dir + '_review')) fs.mkdirSync(dir + '_review');
  const proj = await mdWear(dir, o.tint);
  await new Promise(r => setTimeout(r, 400));
  // --- synchronous from here: nothing else can run until the function returns ---
  if (!ModelProject.all.includes(proj)) return { error: 'wear project was closed by another caller' };
  proj.select();
  Canvas.updateAll();
  const prefix = o.prefix || 'wear_';
  const out = [];
  const shoot = (suffix) => {
    for (const k in views) {
      const f = ciFit(views[k], o.margin === undefined ? 0.06 : o.margin);
      out.push(ciRender(dir + '_review\\' + prefix + k + suffix + '.png',
        { dir: views[k], target: o.target || f.target, size: o.size || f.size, w: o.w || 300, h: o.h || 420 }));
    }
  };
  try {
    if (o.anim) {
      for (const fr of (o.frames || [0])) {
        ciAnim(o.anim, fr);
        Animator.preview();
        shoot('_f' + fr);
      }
    } else {
      shoot('');
    }
  } finally {
    Animation.all.slice().forEach(a => a.remove(false));
    proj.saved = true;
    proj.close(true);
    if (prev && ModelProject.all.includes(prev)) prev.select();
    if (prevMode && Modes.options[prevMode] && Modes.selected.id !== prevMode) Modes.options[prevMode].select();
  }
  return out;
};
