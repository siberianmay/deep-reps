#!/usr/bin/env python3
"""Generate VectorDrawable XML files from anatomy SVG template.

Parses resources/anatomy_template.svg and splits paths into per-muscle-group
VectorDrawable files for use in the Android app.

Pre-applies the SVG transform matrix(0.13333333,0,0,-0.13333333,0,1333.3333)
to all path coordinates so the VectorDrawable paths are in the final viewport
space (0-1333). This avoids AAPT2's STRING_TOO_LARGE limit on pathData
attributes by shrinking coordinate values.

Usage:
    python scripts/generate_anatomy_drawables.py
"""

import os
import re
import sys
import xml.etree.ElementTree as ET

# Project root is one level up from scripts/
PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

SVG_PATH = os.path.join(PROJECT_ROOT, "resources", "anatomy_template.svg")
OUTPUT_DIR = os.path.join(
    PROJECT_ROOT, "core", "ui", "src", "main", "res", "drawable"
)

SVG_NS = "http://www.w3.org/2000/svg"

# Path ID -> muscle group mapping (matches AnatomyPathMapping.kt)
MAPPING = {
    "chest": [
        "path55", "path56", "path65", "path162", "path165",
        # Removed: path57 (498x1150), path161 (895x1074) — body outlines
    ],
    "shoulders": [
        "path54", "path76", "path77", "path78", "path79", "path80", "path81",
        "path82", "path83", "path88", "path89", "path90", "path91", "path92",
        "path93", "path94", "path95", "path157", "path159", "path160",
        "path192", "path193", "path197", "path198", "path234", "path237",
        # Removed: path158 (961x960), path196 (1055x1130) — body outlines
    ],
    "arms": [
        "path66", "path68", "path69", "path70", "path71", "path73", "path74",
        "path75", "path84", "path85", "path86", "path87", "path97", "path101",
        "path108", "path109", "path110", "path111", "path113",
        "path115", "path116", "path117", "path118", "path119",
        "path120", "path121", "path122", "path124", "path148", "path149",
        "path150", "path151", "path163", "path166", "path168",
        "path169", "path170", "path177", "path178", "path194", "path195",
        "path199", "path201", "path209", "path210",
        "path212", "path213", "path214", "path235",
        # Removed: path112 (281x912), path114 (343x875), path167 (892x782),
        #   path208 (987x962), path211 (1043x822), path236 (1080x1081) — body outlines
    ],
    "core": [
        "path58", "path59", "path62", "path230", "path172", "path173",
        "path174", "path175", "path176",
    ],
    "legs": [
        "path53", "path123", "path125", "path127", "path128",
        "path129", "path130", "path131", "path132", "path133", "path134",
        "path135", "path137", "path138", "path139", "path140",
        "path141", "path143", "path144", "path145", "path147",
        "path153", "path154", "path155", "path156", "path179",
        "path180", "path181", "path182", "path183", "path185",
        "path186", "path187", "path188", "path190", "path215", "path216",
        "path217", "path218", "path219", "path220", "path221",
        "path223", "path224", "path225", "path226", "path228",
        # Removed: path126 (334x691), path136 (367x254 area=93K), path146 (322x958),
        #   path152 (378x755), path184 (969x417), path222 (982x745) — body outlines
    ],
    "back": [
        "path61", "path63", "path64", "path67", "path72", "path164",
    ],
    "lower_back": [
        "path60", "path231", "path171",
    ],
    "base": [
        "path49", "path50", "path51", "path52", "path96", "path98", "path99",
        "path100", "path102", "path103", "path104", "path105", "path106",
        "path107", "path200", "path202", "path203", "path204", "path205",
        "path206", "path207", "path142", "path189", "path191", "path227",
        "path229", "path232", "path233",
        # Oversized paths moved from muscle groups (body outlines/shading):
        "path57", "path161",          # from chest
        "path158", "path196",          # from shoulders
        "path112", "path114", "path167", "path208", "path211", "path236",  # from arms
        "path126", "path136", "path146", "path152", "path184", "path222",  # from legs
    ],
}

# SVG transform: matrix(0.13333333,0,0,-0.13333333,0,1333.3333)
#   x' = 0.13333333 * x
#   y' = -0.13333333 * y + 1333.3333
SX = 0.13333333
SY = -0.13333333
TY = 1333.3333

VIEWPORT_WIDTH = "1333.33"
VIEWPORT_HEIGHT = "1333.33"
DRAWABLE_SIZE = "280dp"
FILL_COLOR = "#FFFFFF"


def fmt(v: float) -> str:
    """Format a float with 1 decimal place precision.

    At 1333x1333 viewport displayed at 280dp, 0.1 viewport units = 0.02dp.
    Sub-pixel precision — no visual difference from 2 decimals, but saves
    enough characters to keep all paths under AAPT2's 32K limit.
    """
    s = f"{v:.1f}"
    if "." in s:
        s = s.rstrip("0").rstrip(".")
    return s


def transform_path(d: str) -> str:
    """Pre-apply the SVG transform to path data.

    Tokenizes the SVG path string, transforms coordinate values based on
    the command type (absolute vs relative, and coordinate semantics),
    then reassembles.
    """
    # Tokenize: split into commands and numbers
    tokens = re.findall(r'[A-Za-z]|[+-]?(?:\d+\.?\d*|\.\d+)(?:[eE][+-]?\d+)?', d)

    result = []
    i = 0
    cmd = None
    first_move = True       # SVG spec: first 'm' is treated as absolute 'M'
    first_pair_done = False  # Track whether first pair of converted M is done

    while i < len(tokens):
        token = tokens[i]

        if token.isalpha():
            cmd = token
            first_pair_done = False
            # Per SVG spec, the first 'm' in a path is absolute
            if first_move and cmd == 'm':
                cmd = 'M'
                first_move = False
            elif cmd in ('M', 'm'):
                first_move = False
            result.append(cmd)
            i += 1
            continue

        # SVG spec: after M, subsequent implicit pairs are L;
        #           after m, subsequent implicit pairs are l.
        # We only need special handling when first 'm' was converted to 'M':
        # the first pair is absolute, subsequent pairs must be relative 'l'.
        if first_pair_done and cmd == 'M' and not first_move:
            # This was a converted m->M; subsequent pairs are implicit 'l'
            cmd = 'l'
            result.append(cmd)

        # Determine how many numbers this command consumes per iteration
        # and which are x vs y coordinates
        if cmd in ('z', 'Z'):
            continue

        nums = []
        if cmd in ('M', 'L', 'T'):  # absolute x,y
            nums = _read_floats(tokens, i, 2)
            nums[0] = SX * nums[0]
            nums[1] = SY * nums[1] + TY
            i += 2
            first_pair_done = True
        elif cmd in ('m', 'l', 't'):  # relative dx,dy
            nums = _read_floats(tokens, i, 2)
            nums[0] = SX * nums[0]
            nums[1] = SY * nums[1]
            i += 2
        elif cmd == 'H':  # absolute x
            nums = _read_floats(tokens, i, 1)
            nums[0] = SX * nums[0]
            i += 1
        elif cmd == 'h':  # relative dx
            nums = _read_floats(tokens, i, 1)
            nums[0] = SX * nums[0]
            i += 1
        elif cmd == 'V':  # absolute y
            nums = _read_floats(tokens, i, 1)
            nums[0] = SY * nums[0] + TY
            i += 1
        elif cmd == 'v':  # relative dy
            nums = _read_floats(tokens, i, 1)
            nums[0] = SY * nums[0]
            i += 1
        elif cmd in ('C', 'S'):  # absolute cubic: x1,y1,x2,y2,x,y or x2,y2,x,y
            count = 6 if cmd == 'C' else 4
            nums = _read_floats(tokens, i, count)
            for j in range(0, count, 2):
                nums[j] = SX * nums[j]
                nums[j + 1] = SY * nums[j + 1] + TY
            i += count
        elif cmd in ('c', 's'):  # relative cubic
            count = 6 if cmd == 'c' else 4
            nums = _read_floats(tokens, i, count)
            for j in range(0, count, 2):
                nums[j] = SX * nums[j]
                nums[j + 1] = SY * nums[j + 1]
            i += count
        elif cmd in ('Q',):  # absolute quadratic: x1,y1,x,y
            nums = _read_floats(tokens, i, 4)
            for j in range(0, 4, 2):
                nums[j] = SX * nums[j]
                nums[j + 1] = SY * nums[j + 1] + TY
            i += 4
        elif cmd in ('q',):  # relative quadratic
            nums = _read_floats(tokens, i, 4)
            for j in range(0, 4, 2):
                nums[j] = SX * nums[j]
                nums[j + 1] = SY * nums[j + 1]
            i += 4
        elif cmd == 'A':  # absolute arc: rx,ry,rotation,largeArc,sweep,x,y
            nums = _read_floats(tokens, i, 7)
            nums[0] = abs(SX) * nums[0]  # rx scale
            nums[1] = abs(SY) * nums[1]  # ry scale
            # rotation stays the same (index 2)
            # largeArc flag stays (index 3)
            # sweep flag flips because of negative scaleY
            nums[4] = 1.0 - nums[4]
            nums[5] = SX * nums[5]
            nums[6] = SY * nums[6] + TY
            i += 7
        elif cmd == 'a':  # relative arc
            nums = _read_floats(tokens, i, 7)
            nums[0] = abs(SX) * nums[0]  # rx scale
            nums[1] = abs(SY) * nums[1]  # ry scale
            # rotation stays
            # largeArc stays
            nums[4] = 1.0 - nums[4]  # sweep flag flips
            nums[5] = SX * nums[5]
            nums[6] = SY * nums[6]
            i += 7
        else:
            # Unknown command, pass through
            result.append(token)
            i += 1
            continue

        result.extend(fmt(n) for n in nums)

    return " ".join(result)


def _read_floats(tokens: list, start: int, count: int) -> list[float]:
    """Read `count` float values from tokens starting at `start`."""
    return [float(tokens[start + j]) for j in range(count)]


def parse_svg_paths(svg_path: str) -> dict[str, str]:
    """Parse SVG and return dict of path_id -> path_data (d attribute)."""
    tree = ET.parse(svg_path)
    root = tree.getroot()

    paths = root.findall(f".//{{{SVG_NS}}}path")
    result = {}
    for path in paths:
        path_id = path.get("id")
        path_data = path.get("d")
        if path_id and path_data:
            result[path_id] = path_data
    return result


def generate_vector_drawable(group_name: str, path_ids: list[str],
                             all_paths: dict[str, str]) -> str:
    """Generate VectorDrawable XML string for a muscle group.

    Coordinates are pre-transformed — no <group> transform needed.
    """
    lines = [
        '<?xml version="1.0" encoding="utf-8"?>',
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        f'    android:width="{DRAWABLE_SIZE}"',
        f'    android:height="{DRAWABLE_SIZE}"',
        f'    android:viewportWidth="{VIEWPORT_WIDTH}"',
        f'    android:viewportHeight="{VIEWPORT_HEIGHT}">',
    ]

    for path_id in path_ids:
        path_data = all_paths.get(path_id)
        if path_data is None:
            print(f"WARNING: {path_id} not found in SVG (group: {group_name})",
                  file=sys.stderr)
            continue

        transformed = transform_path(path_data)

        # Safety check: AAPT2 limit is ~32K chars per attribute
        if len(transformed) > 30000:
            print(f"WARNING: {path_id} pathData is {len(transformed)} chars "
                  f"(group: {group_name}) — splitting", file=sys.stderr)
            parts = _split_pathdata(transformed)
            for idx, part in enumerate(parts):
                suffix = f"_{chr(98 + idx)}" if idx > 0 else ""
                lines.append('    <path')
                lines.append(f'        android:name="{path_id}{suffix}"')
                lines.append(f'        android:pathData="{part}"')
                lines.append(f'        android:fillColor="{FILL_COLOR}" />')
        else:
            lines.append('    <path')
            lines.append(f'        android:name="{path_id}"')
            lines.append(f'        android:pathData="{transformed}"')
            lines.append(f'        android:fillColor="{FILL_COLOR}" />')

    lines.append('</vector>')
    lines.append('')  # trailing newline

    return '\n'.join(lines)


def _split_pathdata(pathdata: str, max_len: int = 28000) -> list[str]:
    """Split an oversized pathData at command boundaries."""
    cmd_positions = [m.start() for m in re.finditer(r'[A-Za-z]', pathdata)]
    parts = []
    start = 0

    for pos in cmd_positions:
        if pos - start > max_len and pos > start:
            parts.append(pathdata[start:pos].rstrip(" ,"))
            start = pos

    parts.append(pathdata[start:])
    return parts


def main():
    if not os.path.exists(SVG_PATH):
        print(f"ERROR: SVG not found at {SVG_PATH}", file=sys.stderr)
        sys.exit(1)

    print(f"Parsing SVG: {SVG_PATH}")
    all_paths = parse_svg_paths(SVG_PATH)
    print(f"Found {len(all_paths)} paths in SVG")

    os.makedirs(OUTPUT_DIR, exist_ok=True)

    for group_name, path_ids in MAPPING.items():
        output_file = os.path.join(OUTPUT_DIR, f"ic_anatomy_{group_name}.xml")
        xml_content = generate_vector_drawable(group_name, path_ids, all_paths)
        with open(output_file, "w", encoding="utf-8", newline="\n") as f:
            f.write(xml_content)

        # Report pathData sizes
        import xml.etree.ElementTree as ET2
        root = ET2.fromstring(xml_content)
        max_pd = 0
        for p in root.iter():
            pd = p.get('{http://schemas.android.com/apk/res/android}pathData', '')
            max_pd = max(max_pd, len(pd))
        print(f"Generated ic_anatomy_{group_name}.xml "
              f"({len(path_ids)} paths, max pathData: {max_pd} chars)")

    total_paths = sum(len(ids) for ids in MAPPING.values())
    print(f"\nDone. Generated {len(MAPPING)} files with {total_paths} total paths.")


if __name__ == "__main__":
    main()
