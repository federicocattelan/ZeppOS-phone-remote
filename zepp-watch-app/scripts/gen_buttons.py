"""Generate pill-shaped button background images (with baked-in icon) for the
Phone Remote watch app. Sizes match the curve-aware layout computed for the
466x466 round Active 2 display (see page/index.r.layout.js).
"""

from pathlib import Path
from PIL import Image, ImageDraw

ASSET_DIR = Path(__file__).resolve().parent.parent / "assets" / "466x466-amazfit-active-2"
ASSET_DIR.mkdir(parents=True, exist_ok=True)

SCALE = 4  # supersample for smooth anti-aliased edges, then downscale

WAZE_SIZE = (392, 104)
GPS_SIZE = (342, 104)
RADIUS = 52
ICON_BOX = 62
ICON_PAD = 21

WAZE_ICON_BOX = 66
WAZE_ICON_PAD = 19

WAZE_COLOR = (5, 195, 249, 255)
WAZE_PRESS = (4, 150, 194, 255)
GPS_ON_COLOR = (52, 199, 89, 255)
GPS_ON_PRESS = (39, 156, 68, 255)
GPS_OFF_COLOR = (108, 108, 114, 255)
GPS_OFF_PRESS = (82, 82, 87, 255)

WHITE = (255, 255, 255, 255)


def rounded_pill(size, radius, color):
    w, h = size
    img = Image.new("RGBA", (w * SCALE, h * SCALE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw.rounded_rectangle(
        [0, 0, w * SCALE - 1, h * SCALE - 1],
        radius=radius * SCALE,
        fill=color,
    )
    return img, draw


WAZE_ICON_NORMAL = Image.open(Path(__file__).resolve().parent / "waze_icon_normal_raw.png").convert("RGBA")
WAZE_ICON_PRESS = Image.open(Path(__file__).resolve().parent / "waze_icon_press_raw.png").convert("RGBA")
GPS_ICON_ON_NORMAL = Image.open(Path(__file__).resolve().parent / "gps_icon_on_normal_raw.png").convert("RGBA")
GPS_ICON_ON_PRESS = Image.open(Path(__file__).resolve().parent / "gps_icon_on_press_raw.png").convert("RGBA")
GPS_ICON_OFF_NORMAL = Image.open(Path(__file__).resolve().parent / "gps_icon_off_normal_raw.png").convert("RGBA")
GPS_ICON_OFF_PRESS = Image.open(Path(__file__).resolve().parent / "gps_icon_off_press_raw.png").convert("RGBA")


def paste_icon(img, ox, oy, box, source):
    """Paste a pre-rendered glyph (drawn on the exact same background color
    as the pill) so the edges blend seamlessly without needing alpha."""
    s = int(box * SCALE)
    icon = source.resize((s, s), Image.LANCZOS)
    img.paste(icon, (int(ox), int(oy)))


def build_waze(color, filename, icon_source):
    img, draw = rounded_pill(WAZE_SIZE, RADIUS, color)
    paste_icon(
        img, WAZE_ICON_PAD * SCALE, WAZE_ICON_PAD * SCALE, WAZE_ICON_BOX, icon_source
    )
    img = img.resize((WAZE_SIZE[0], WAZE_SIZE[1]), Image.LANCZOS)
    img.save(ASSET_DIR / filename)
    print("wrote", filename, img.size)


def build_gps(color, filename, icon_source):
    img, draw = rounded_pill(GPS_SIZE, RADIUS, color)
    paste_icon(img, ICON_PAD * SCALE, ICON_PAD * SCALE, ICON_BOX, icon_source)
    img = img.resize((GPS_SIZE[0], GPS_SIZE[1]), Image.LANCZOS)
    img.save(ASSET_DIR / filename)
    print("wrote", filename, img.size)


build_waze(WAZE_COLOR, "waze_normal.png", WAZE_ICON_NORMAL)
build_waze(WAZE_PRESS, "waze_press.png", WAZE_ICON_PRESS)
build_gps(GPS_ON_COLOR, "gps_on_normal.png", GPS_ICON_ON_NORMAL)
build_gps(GPS_ON_PRESS, "gps_on_press.png", GPS_ICON_ON_PRESS)
build_gps(GPS_OFF_COLOR, "gps_off_normal.png", GPS_ICON_OFF_NORMAL)
build_gps(GPS_OFF_PRESS, "gps_off_press.png", GPS_ICON_OFF_PRESS)
