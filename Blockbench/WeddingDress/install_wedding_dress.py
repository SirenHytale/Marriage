"""Install the Wedding Dress into Marriage's resources.

  python install_wedding_dress.py [--dry]

Run after gen_wedding_dress.py, then (in Blockbench, through the MCP's risky_eval, with
Cultivation/Blockbench/Items/cultivation_items_tools.js and wedding_dress_tools.js loaded)
ciExport('<this folder>\\Marriage_WeddingDress.bbmodel') and mdIcon('<this folder>\\', {dir: [0.55, 0.22, 1]}).
  - Marriage_WeddingDress.blockymodel + Textures/Marriage_WeddingDress.png -> Common/Items/Marriage/Armor/
  - _icons/Marriage_WeddingDress.png -> Common/Icons/ItemsGenerated/, downsampled to 64 (vanilla's icon size)
The item itself (Server/Item/Items/MISC/Marriage/Marriage_WeddingDress.json) points at those paths and is not
rewritten here.
"""
import json, os, shutil, sys
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
COMMON = os.path.normpath(os.path.join(HERE, "..", "..", "src", "main", "resources", "Common"))
NAME = "Marriage_WeddingDress"
DRY = "--dry" in sys.argv
FILES = {
    f"Items/Marriage/Armor/{NAME}.blockymodel": f"{NAME}.blockymodel",
    f"Items/Marriage/Armor/{NAME}.png": f"Textures/{NAME}.png",
}
ICON = (f"Icons/ItemsGenerated/{NAME}.png", f"_icons/{NAME}.png")


def depth(n, d=1):
    return max([d] + [depth(c, d + 1) for c in n.get("children", [])])


def main():
    model = json.load(open(os.path.join(HERE, f"{NAME}.blockymodel"), encoding="utf-8"))
    deep = max(depth(n) for n in model["nodes"])
    if deep > 20:
        raise SystemExit(f"{NAME}.blockymodel nests {deep} deep - the client drops a model past JSON depth 64")
    tex = Image.open(os.path.join(HERE, "Textures", f"{NAME}.png"))
    if tex.size[0] > 256 or tex.size[1] > 128:
        raise SystemExit(f"texture {tex.size} is over the 256 x 128 worn-attachment cap")
    for dst, src in list(FILES.items()) + [ICON]:
        if not os.path.exists(os.path.join(HERE, *src.split("/"))):
            raise SystemExit(f"missing {src} - run the generator / ciExport / mdIcon first")
    for dst, src in FILES.items():
        out = os.path.join(COMMON, *dst.split("/"))
        if not DRY:
            os.makedirs(os.path.dirname(out), exist_ok=True)
            shutil.copyfile(os.path.join(HERE, *src.split("/")), out)
        print(f"  {dst}")
    out = os.path.join(COMMON, *ICON[0].split("/"))
    if not DRY:
        os.makedirs(os.path.dirname(out), exist_ok=True)
        Image.open(os.path.join(HERE, *ICON[1].split("/"))).convert("RGBA").resize((64, 64), Image.LANCZOS).save(out)
    print(f"  {ICON[0]} (64 px)")
    print(f"node depth {deep}, texture {tex.size[0]}x{tex.size[1]}")


main()
