"""Marriage_WeddingDress - the Wedding Dress: a white bridal gown for the chest armor slot.

  python gen_wedding_dress.py   -> Marriage_WeddingDress.bbmodel (+ Textures/Marriage_WeddingDress.png)

Built the way Cultivation's Spirit-Woven Robe is (Cultivation/Blockbench/Items/Robe/gen_robe.py), on the same shared
builder (Cultivation/Blockbench/Items/bbgen.py, imported in place, not copied): a worn armor model is the item's own
Model, an attachment whose shapeless pieces bind to the player's bones (Pelvis > Belly > Chest > R/L-Arm >
R/L-Forearm, geometry in plain child bones), every piece's origin on its bone's BOX CENTER (attachment piece rules),
all laid out in the vanilla player's own space (Chest box center (0,79,0), Belly (0,63,0), Pelvis (0,51,0); the
player's arm and forearm boxes are 8 x 12 deep around (+-15.5, 78.59 / 60.59, -0.94)).
  - a fitted satin bodice with a sweetheart neckline edged in pearls, lace above it (a lace yoke and lace shoulders)
    and an illusion lace back with a column of pearl buttons;
  - long lace sleeves, fitted, ending in a scalloped edge and a pearled wristband;
  - a blush satin sash with a bow at the small of the back, its two tails hanging over the skirt;
  - a floor-length skirt hung on the cloth bones the vanilla walk / run clips sway by name (Front/Back/L_Side/
    R_Side_Cloth_1/2) with the vanilla LongDress's leans (Cosmetics/Overtops/LongDress_Long_Sleeve: 15 + 10
    degrees). Each panel leans out from its top and its lower half leans further, the back more than the front, so
    the skirt is a bell from the front and sweeps back from the side. Every panel is cut (clear texels) to exactly
    where its neighbors stand at the same height, so the four close at their corners all the way down. The clips
    swing the panels apart as the legs move (vanilla closes those gaps with flaps); here four still gussets stand
    just inside the bell's corners, hidden at rest, so a gap shows skirt rather than legs. The hem dips from the
    front to the back and is edged with scalloped lace and a row of pearls, with a silver vine embroidered above it.
    There is no separate train: a flat one flips up like a tray whenever the clips swing the back panel.
Lace is cut, not painted: the space between its flowers is clear, so the wearer shows through (the item hides the
Undertop and Overtop cosmetics, so that is skin, not a shirt).
The texture is 256 x 128, the worn-attachment cap (API_EVIDENCE: a larger character texture broke every mouth).
"""
import json, math, os, sys
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "..", "..", "Cultivation", "Blockbench", "Items"))
from bbgen import Model, clamp, ramp, noise, lerp

CH, BE, PE = (0, 79, 0), (0, 63, 0), (0, 51, 0)
ARM = {"R": (-15.5, 78.594, -0.939), "L": (15.5, 78.594, -0.939)}
FOREARM = {"R": (-15.5, 60.594, -0.939), "L": (15.5, 60.594, -0.939)}

NECK = 82            # the lace starts above this height at the sides and back
TOP = 46             # where the skirt hangs from (the hips)
JOIN = 27            # about where each panel's lower half starts, measured in the world (after the lean)
HEM_FRONT = 2.0      # the hem's height at the front; it dips to the back panel's own bottom edge behind
# per panel: (how far out it hangs at TOP, the lean of its upper half, the extra lean of its lower half), degrees
SKIRT = {"Front": (11.5, 8, 4), "Back": (11.5, 12, 15), "Side": (15.5, 10, 4)}
GUSSET_INSET, GUSSET_W = 6.0, 11     # how far in from each corner the gussets stand, and their width


def build(kind, hem):
    """Panel lengths for one kind, and where the join and the bottom edge land in the world."""
    r0, a1, a2 = SKIRT[kind]
    c1, c2 = math.cos(math.radians(a1)), math.cos(math.radians(a1 + a2))
    l1 = round((TOP - JOIN) / c1)
    join = TOP - l1 * c1
    l2 = math.ceil((join - hem) / c2)
    return {"l1": l1, "l2": l2, "join": join, "bottom": join - l2 * c2}


PANEL = {"Front": build("Front", HEM_FRONT), "Back": build("Back", 1.0), "Side": build("Side", 1.0)}
HEM_BACK = PANEL["Back"]["bottom"]


def reach(kind, y):
    """How far out (from the body's middle) a panel of this kind stands at world height y."""
    r0, a1, a2 = SKIRT[kind]
    p = PANEL[kind]
    if y >= p["join"]:
        return r0 + (TOP - y) * math.tan(math.radians(a1))
    return r0 + p["l1"] * math.sin(math.radians(a1)) + (p["join"] - y) * math.tan(math.radians(a1 + a2))


FRONT_HEM_Z, BACK_HEM_Z = reach("Front", HEM_FRONT), reach("Back", HEM_BACK)


def hemline(part, z):
    """The hem's world height: level at the front and back, dipping smoothly along each side toward the back."""
    if part.startswith("front"):
        return HEM_FRONT
    if part.startswith("back"):
        return HEM_BACK
    s = clamp((FRONT_HEM_Z - z) / (FRONT_HEM_Z + BACK_HEM_Z))
    s = clamp((s - 0.3) / 0.7)
    return HEM_FRONT + (HEM_BACK - HEM_FRONT) * s * s * (3 - 2 * s)


def inside_bell(part, P):
    """True where a skirt panel is cloth, from its texel's world point: a front / back panel reaches sideways to
    where the side panels stand at that height, a side panel forward and back to the front and back panels."""
    x, y, z = P
    if part.startswith(("front", "back")):
        return abs(x) <= reach("Side", y) + 0.5
    if part.startswith("side"):
        return -reach("Back", y) - 0.5 <= z <= reach("Front", y) + 0.5
    return abs(x) <= reach("Side", y) - 0.3 and -reach("Back", y) + 0.3 <= z <= reach("Front", y) - 0.3


m = Model("Marriage_WeddingDress", 256, 128)
m.bone("Pelvis", PE, piece=True)
m.bone("Belly", BE, "Pelvis", piece=True)
m.bone("Chest", CH, "Belly", piece=True)
m.bone("Dress-Bodice", CH, "Chest")
m.box("Dress-Bodice", "Bodice", (-15, 67, -11), (15, 90, 11), "bodice", hidden=("down",))
m.bone("Dress-Waist", BE, "Belly")
m.box("Dress-Waist", "Waist", (-14, 54, -10), (14, 68, 10), "waist", hidden=("up",))
m.box("Dress-Waist", "Sash", (-15, 56, -11), (15, 61, 11), "sash", hidden=("up", "down"))
m.box("Dress-Waist", "Bow-Knot", (-2, 56, -13), (2, 61, -11), "bowknot", hidden=("south",))
m.box("Dress-Waist", "Bow-Loop-R", (-9, 56, -13), (-2, 62, -12), "bowloop", rot=(0, 0, -14), hidden=("south",))
m.box("Dress-Waist", "Bow-Loop-L", (2, 56, -13), (9, 62, -12), "bowloop", rot=(0, 0, 14), hidden=("south",))
for s, sx in (("R", -1), ("L", 1)):
    ax, ay, az = ARM[s]
    m.bone(f"{s}-Arm", ARM[s], "Chest", piece=True)
    m.bone(f"Sleeve-{s}", ARM[s], f"{s}-Arm")
    m.box(f"Sleeve-{s}", f"Sleeve-{s}", (ax - 4.5, ay - 10, az - 6.5), (ax + 4.5, ay + 11, az + 6.5), "sleeve")
    fx, fy, fz = FOREARM[s]
    m.bone(f"{s}-Forearm", FOREARM[s], f"{s}-Arm", piece=True)
    m.bone(f"Lace-{s}", FOREARM[s], f"{s}-Forearm")
    m.box(f"Lace-{s}", f"Forearm-{s}", (fx - 4.5, fy - 7, fz - 6.5), (fx + 4.5, fy + 8, fz + 6.5), "forearm",
          hidden=("up", "down"))
    m.box(f"Lace-{s}", f"Wrist-{s}", (fx - 5, fy - 9, fz - 7), (fx + 5, fy - 7, fz + 7), "wrist", hidden=("up",))
m.bone("Dress-Hips", PE, "Pelvis")
m.box("Dress-Hips", "Hips", (-15, 44, -11), (15, 57, 11), "hips", hidden=("up",))
# the bow's tails, from the knot down over the hips (still) and on down the back panel (swaying with it)
TAIL_Z = -SKIRT["Back"][0] - 0.8
m.box("Dress-Hips", "Bow-Tail-Top-R", (-5, TOP, TAIL_Z), (-1, 57, TAIL_Z), "bowtailtop", quad="north")
m.box("Dress-Hips", "Bow-Tail-Top-L", (1, TOP, TAIL_Z), (5, 57, TAIL_Z), "bowtailtop", quad="north")


def width(kind, y):
    """A panel's half-width (front / back) or forward / back reach (sides), to cover the bell down to y."""
    return math.ceil(reach(kind, y) + 0.5)


# the skirt on the vanilla cloth bones: each upper panel leans out about its top edge, each lower panel about the
# join, on top of the upper one's lean
for name, z0, face, sgn in (("Front", SKIRT["Front"][0], "south", -1), ("Back", -SKIRT["Back"][0], "north", 1)):
    _, a1, a2 = SKIRT[name]
    p = PANEL[name]
    jy = TOP - p["l1"]
    by = jy - p["l2"]
    part = name.lower()
    b1 = m.bone(f"{name}_Cloth_1", (0, TOP, z0), "Pelvis", rot=(sgn * a1, 0, 0))
    w1 = width("Side", p["join"])
    m.box(b1, f"{name}_Cloth_1", (-w1, jy, z0), (w1, TOP, z0), part + "1", quad=face)
    b2 = m.bone(f"{name}_Cloth_2", (0, jy, z0), b1, rot=(sgn * a2, 0, 0))
    w2 = width("Side", p["bottom"])
    m.box(b2, f"{name}_Cloth_2", (-w2, by, z0), (w2, jy, z0), part + "2", quad=face)
    if name == "Back":
        m.box(b1, "Bow-Tail-R", (-5, 31, TAIL_Z), (-1, TOP, TAIL_Z), "bowtail", quad="north")
        m.box(b1, "Bow-Tail-L", (1, 33, TAIL_Z), (5, TOP, TAIL_Z), "bowtail", quad="north")
for side, sx, face in (("L", 1, "east"), ("R", -1, "west")):
    _, a1, a2 = SKIRT["Side"]
    p = PANEL["Side"]
    x = sx * SKIRT["Side"][0]
    jy = TOP - p["l1"]
    by = jy - p["l2"]
    b1 = m.bone(f"{side}_Side_Cloth_1", (x, TOP, 0), "Pelvis", rot=(0, 0, sx * a1))
    m.box(b1, f"{side}_Side_Cloth_1", (x, jy, -width("Back", p["join"])), (x, TOP, width("Front", p["join"])),
          "side1", quad=face)
    b2 = m.bone(f"{side}_Side_Cloth_2", (x, jy, 0), b1, rot=(0, 0, sx * a2))
    m.box(b2, f"{side}_Side_Cloth_2", (x, by, -width("Back", p["bottom"])), (x, jy, width("Front", p["bottom"])),
          "side2", quad=face)
# the gussets: one inside each corner of the bell, square to its diagonal, leaning out as fast as the corner does
# (upper and lower half), so they stay GUSSET_INSET in from it all the way down; still (on the Pelvis), so when the
# clips swing two panels apart the gap between them shows skirt
for corner, sx, sz, fb in (("FL", 1, 1, "Front"), ("FR", -1, 1, "Front"), ("BL", 1, -1, "Back"), ("BR", -1, -1, "Back")):
    diag = []
    for k in (1, 2):
        ts = math.tan(math.radians(sum(SKIRT["Side"][1:1 + k])))
        tf = math.tan(math.radians(sum(SKIRT[fb][1:1 + k])))
        diag.append(math.degrees(math.atan((ts + tf) / math.sqrt(2))))
    cx = sx * (SKIRT["Side"][0] - GUSSET_INSET / math.sqrt(2))
    cz = sz * (SKIRT[fb][0] - GUSSET_INSET / math.sqrt(2))
    l1 = round((TOP - JOIN) / math.cos(math.radians(diag[0])))
    gjoin = TOP - l1 * math.cos(math.radians(diag[0]))
    l2 = math.ceil((gjoin - min(HEM_FRONT, HEM_BACK)) / math.cos(math.radians(diag[1])))
    gy = m.bone(f"Gusset-{corner}", (cx, TOP, cz), "Pelvis", rot=(0, math.degrees(math.atan2(sx, sz)), 0))
    g1 = m.bone(f"Gusset-{corner}-1", (cx, TOP, cz), gy, rot=(-diag[0], 0, 0))
    hw = GUSSET_W / 2
    m.box(g1, f"Gusset-{corner}-1", (cx - hw, TOP - l1, cz), (cx + hw, TOP, cz), "gusset1", quad="south")
    g2 = m.bone(f"Gusset-{corner}-2", (cx, TOP - l1, cz), g1, rot=(-(diag[1] - diag[0]), 0, 0))
    m.box(g2, f"Gusset-{corner}-2", (cx - hw, TOP - l1 - l2, cz), (cx + hw, TOP - l1, cz), "gusset2", quad="south")
used = m.layout()

SATIN = [(176, 168, 160), (210, 204, 196), (232, 228, 222), (246, 244, 240), (255, 255, 253)]
LACE = [(188, 176, 156), (214, 204, 184), (234, 226, 210), (248, 244, 234)]
PEARL = [(206, 200, 196), (238, 234, 230), (255, 255, 255)]
BLUSH = [(150, 92, 98), (196, 132, 136), (226, 172, 172), (244, 206, 204)]
SILVER = (196, 204, 220)
SILVER_SOFT = (216, 222, 234)
FACE = {"up": 0.95, "west": 0.78, "north": 0.66, "south": 0.8, "east": 0.6, "down": 0.4}


def hash01(i, j, seed):
    return 0.5 + 0.5 * noise(i, j, seed)


def surface(t):
    """The two in-plane coordinates of a texel's point, for patterns that must run on across neighboring faces."""
    x, y, z = t.L
    if t.face in ("south", "north"):
        return x, y
    if t.face in ("east", "west"):
        return z, y
    return x, z


def satin(t, extra=0.0):
    """Warm white satin: face shading, a faint weave, a soft sheen down the middle of each face."""
    sh = FACE[t.face]
    across = (t.i + 0.5) / t.w
    sheen = 0.06 * math.exp(-((across - 0.5) / 0.22) ** 2)
    return ramp(SATIN, clamp(0.4 + 0.5 * sh + extra + sheen + 0.015 * noise(t.x, t.y, 311)))


CELL = 7


def lace(t, a, b):
    """Cut lace: five-petaled flowers in offset rows, each ringed by a raised cord, on a sheer tulle with one texel
    in four clear (None) - a fine mesh the wearer shows through, as through a veil."""
    sh = FACE[t.face]
    row = math.floor(b / CELL)
    off = CELL / 2 * (row % 2)
    col = math.floor((a - off) / CELL)
    u, v = a - (col * CELL + off + CELL / 2), b - (row * CELL + CELL / 2)
    r = math.hypot(u, v)
    if r < 0.8:
        return ramp(LACE, 0.3 + 0.3 * sh)                                # the flower's heart
    if r < 2.4:
        ang = math.atan2(v, u) + 6.283 * hash01(col, row, 5)
        if math.cos(5 * ang) > -0.3:
            return ramp(SATIN, 0.75 + 0.25 * sh)                         # its petals, bright white
    elif r < 3.0:
        return ramp(LACE, 0.3 + 0.3 * sh)                                # the cord round it
    edge = t.i in (0, t.w - 1) or t.j in (0, t.h - 1)
    if math.floor(a) % 2 == 0 and math.floor(b) % 2 == 0 and not edge:
        return None                                                      # the tulle between: one texel in four
                                                                         # clear (sheer, not speckled), hemmed
                                                                         # solid at every edge so none frays
    return ramp(LACE, 0.4 + 0.3 * sh)


def pearl(i):
    return PEARL[2] if i % 2 == 0 else PEARL[1]


def sweetheart(x):
    """The neckline's height across the bodice front: two arcs meeting in a dip at the middle."""
    ax = abs(x)
    if ax >= 12:
        return 79.0
    return 79.0 + 0.95 * math.sqrt(max(0.0, 36.0 - (ax - 6.0) ** 2))


def hem_border(t, d, a):
    """The skirt's hem, d world units above the hemline: scallops cut along the very edge, an opaque lace border
    with a little flower in every scallop, a cord following the scallops, and a row of pearls along its top.
    False above the border (plain skirt)."""
    sh = FACE[t.face]
    u = (a / 5.0) % 1.0
    cut = 1.5 * (1 - math.sqrt(max(0.0, 1 - (2 * u - 1) ** 2)))
    if d < cut:
        return None
    if d < cut + 0.7:
        return ramp(LACE, 0.45 + 0.3 * sh)                               # the cord along the scallops
    if d < 3.6:
        fd = math.hypot((u - 0.5) * 5.0, d - 2.2)
        if fd < 0.6:
            return ramp(LACE, 0.3 + 0.3 * sh)
        if fd < 1.4:
            return ramp(LACE, 0.75 + 0.25 * sh)
        return ramp(LACE, 0.6 + 0.3 * sh)
    if d < 4.6:
        return pearl(math.floor(a)) if math.floor(a) % 2 == 0 else ramp(LACE, 0.7 + 0.3 * sh)
    return False


def skirt(t, a, d):
    """Satin skirt: soft vertical folds, a silver vine above the lace hem, a little sparkle."""
    c = lerp(satin(t), SATIN[0], 0.08 * (0.5 + 0.5 * math.sin(a * 2 * math.pi / 7)))
    vine = 7.5 + 1.5 * math.sin(a * 2 * math.pi / 11)
    if abs(d - vine) < 0.55:
        c = SILVER
    elif abs(d - (vine + 1.8)) < 0.6 and math.floor(a) % 11 in (2, 8):
        c = SILVER_SOFT                                                  # its leaves
    elif abs(d - (vine - 1.6)) < 0.6 and math.floor(a) % 11 == 5:
        c = PEARL[2]                                                     # and a pearl bud between them
    if noise(t.x, t.y, 977) > 0.93 and noise(t.x + 3, t.y, 41) > 0.6:
        c = (255, 255, 255)                                              # sparkle
    return c


def painter_for(name, v):
    def paint(t):
        p = t.part
        x, y, z = t.L
        a, b = surface(t)
        if p == "bodice":
            if t.face == "south":
                neck = sweetheart(x)
                if y > neck + 0.6:
                    return lace(t, a, b)                                 # the lace yoke above the neckline
                if y > neck - 0.6:
                    return pearl(math.floor(x))                          # pearls along the neckline
                # the satin rounds away toward the sides, with a pearl cluster at the neckline's dip
                c = satin(t, -0.1 * (abs(x) / 15) ** 2)
                if math.hypot(x, y - (sweetheart(0) - 2.2)) < 1.2:
                    return pearl(math.floor(x + y))
                return c
            if t.face == "north":
                if y > NECK:
                    return lace(t, a, b)                                 # the illusion back
                if abs(x) < 0.6 and math.floor(y) % 2 == 0:
                    return pearl(1 if y > 80 else 0)                     # pearl buttons down the back
                return satin(t)
            if t.face in ("east", "west"):
                return lace(t, a, b) if y > NECK else satin(t)
            return lace(t, a, b)                                         # the lace shoulders
        if p in ("waist", "hips"):
            return satin(t)
        if p == "sash":
            c = ramp(BLUSH, 0.45 + 0.45 * FACE[t.face])
            if t.j in (0, t.h - 1):
                c = ramp(BLUSH, 0.95)                                    # a lighter edge on the ribbon
            return c
        if p in ("bowknot", "bowloop"):
            return ramp(BLUSH, 0.35 + 0.55 * FACE[t.face] + (0.08 if t.j < 2 else 0))
        if p == "bowtailtop":
            return ramp(BLUSH, 0.55 + 0.2 * (t.j + 0.5) / t.h)
        if p == "bowtail":
            vv = 1 - (t.j + 0.5) / t.h
            if vv * t.h < 1.6 and abs(t.i + 0.5 - t.w / 2) < 1.6 - vv * t.h:
                return None                                              # a V-cut end
            return ramp(BLUSH, 0.4 + 0.35 * vv)
        if p == "sleeve":
            return lace(t, a, b)
        if p == "forearm":
            hem = t.h - 1 - t.j
            if hem < 1:
                return None if math.floor(a) % 3 == 0 else ramp(LACE, 0.7)   # a scalloped edge into the wristband
            return lace(t, a, b)
        if p == "wrist":
            if t.face == "down":                                         # a ring round the wrist, open in the middle
                ring = t.i in (0, t.w - 1) or t.j in (0, t.h - 1)
                return ramp(SATIN, 0.55) if ring else None
            return pearl(t.i) if t.j == t.h // 2 else ramp(SATIN, 0.75 + 0.2 * FACE[t.face])
        if p in ("front1", "back1", "side1", "front2", "back2", "side2"):
            if not inside_bell(p, t.P):
                return None                                              # clear corners: the bell's cut
            d = t.P[1] - hemline(p, t.P[2])
            edge = hem_border(t, d, a)
            if edge is not False:
                return edge
            return skirt(t, a, d)
        if p in ("gusset1", "gusset2"):
            if not inside_bell(p, t.P):
                return None                                              # never through the panels at rest
            d = t.P[1] - hemline("side", t.P[2])
            edge = hem_border(t, d, a)
            if edge is None or edge is False:
                return None if edge is None else lerp(skirt(t, a, 99), SATIN[1], 0.25)   # the skirt's inner layer
            return edge
        return (255, 0, 255)
    return paint


out = m.write(HERE, {"Marriage_WeddingDress": {}}, painter_for)
d = json.load(open(out, encoding="utf-8"))
d["meta"]["model_format"] = "hytale_character"                           # an attachment: Hytale Character
json.dump(d, open(out, "w", encoding="utf-8"), indent=1)
lo, hi = m.bounds()
print(f"panels {json.dumps({k: {kk: round(vv, 2) for kk, vv in p.items()} for k, p in PANEL.items()})}")
print(f"hem reach front {FRONT_HEM_Z:.1f} back {BACK_HEM_Z:.1f} side {reach('Side', HEM_BACK):.1f}")
print(f"bounds {[round(v, 1) for v in lo]} .. {[round(v, 1) for v in hi]}")
print(f"elements {len(m.elements)}, bones {len(m.groups)}, texels used {used} of {m.W * m.H}")
