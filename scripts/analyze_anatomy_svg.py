#!/usr/bin/env python3
"""
Analyze anatomy_template.svg to extract path bounding boxes and classify
each path into a muscle group region based on spatial position.

The SVG has all paths with transform="matrix(0.13333333,0,0,-0.13333333,0,1333.3333)"
This means: x' = 0.13333333 * x, y' = -0.13333333 * y + 1333.3333
(Scale down and flip Y axis)

All path `d` data uses coordinates in a ~10000x10000 space.
"""

import xml.etree.ElementTree as ET
import re
from dataclasses import dataclass


@dataclass
class BBox:
    min_x: float
    min_y: float
    max_x: float
    max_y: float

    @property
    def center_x(self):
        return (self.min_x + self.max_x) / 2

    @property
    def center_y(self):
        return (self.min_y + self.max_y) / 2

    @property
    def width(self):
        return self.max_x - self.min_x

    @property
    def height(self):
        return self.max_y - self.min_y

    @property
    def area(self):
        return self.width * self.height


def tokenize_path(d: str):
    """Tokenize SVG path d attribute into commands and numbers."""
    tokens = re.findall(r'[MmZzLlHhVvCcSsQqTtAa]|[-+]?(?:\d+\.?\d*|\.\d+)(?:[eE][-+]?\d+)?', d)
    return tokens


def extract_coordinates_from_path(d: str):
    """Extract all coordinate points from an SVG path d attribute."""
    tokens = tokenize_path(d)
    points = []
    current_x, current_y = 0.0, 0.0
    start_x, start_y = 0.0, 0.0
    i = 0
    cmd = 'M'

    def next_float():
        nonlocal i
        while i < len(tokens) and tokens[i] in 'MmZzLlHhVvCcSsQqTtAa':
            i += 1
        if i < len(tokens):
            val = float(tokens[i])
            i += 1
            return val
        return 0.0

    while i < len(tokens):
        token = tokens[i]
        if token in 'MmZzLlHhVvCcSsQqTtAa':
            cmd = token
            i += 1
        else:
            if cmd == 'M':
                cmd = 'L'
            elif cmd == 'm':
                cmd = 'l'

        if cmd == 'M':
            current_x = next_float()
            current_y = next_float()
            start_x, start_y = current_x, current_y
            points.append((current_x, current_y))
        elif cmd == 'm':
            current_x += next_float()
            current_y += next_float()
            start_x, start_y = current_x, current_y
            points.append((current_x, current_y))
        elif cmd == 'L':
            current_x = next_float()
            current_y = next_float()
            points.append((current_x, current_y))
        elif cmd == 'l':
            current_x += next_float()
            current_y += next_float()
            points.append((current_x, current_y))
        elif cmd == 'H':
            current_x = next_float()
            points.append((current_x, current_y))
        elif cmd == 'h':
            current_x += next_float()
            points.append((current_x, current_y))
        elif cmd == 'V':
            current_y = next_float()
            points.append((current_x, current_y))
        elif cmd == 'v':
            current_y += next_float()
            points.append((current_x, current_y))
        elif cmd == 'C':
            for _ in range(3):
                x = next_float()
                y = next_float()
                points.append((x, y))
            current_x, current_y = points[-1]
        elif cmd == 'c':
            for _ in range(3):
                dx = next_float()
                dy = next_float()
                points.append((current_x + dx, current_y + dy))
            current_x, current_y = points[-1]
        elif cmd == 'S':
            for _ in range(2):
                x = next_float()
                y = next_float()
                points.append((x, y))
            current_x, current_y = points[-1]
        elif cmd == 's':
            for _ in range(2):
                dx = next_float()
                dy = next_float()
                points.append((current_x + dx, current_y + dy))
            current_x, current_y = points[-1]
        elif cmd == 'Q':
            for _ in range(2):
                x = next_float()
                y = next_float()
                points.append((x, y))
            current_x, current_y = points[-1]
        elif cmd == 'q':
            for _ in range(2):
                dx = next_float()
                dy = next_float()
                points.append((current_x + dx, current_y + dy))
            current_x, current_y = points[-1]
        elif cmd == 'T':
            current_x = next_float()
            current_y = next_float()
            points.append((current_x, current_y))
        elif cmd == 't':
            current_x += next_float()
            current_y += next_float()
            points.append((current_x, current_y))
        elif cmd in ('A', 'a'):
            next_float()  # rx
            next_float()  # ry
            next_float()  # x-rotation
            next_float()  # large-arc-flag
            next_float()  # sweep-flag
            if cmd == 'A':
                current_x = next_float()
                current_y = next_float()
            else:
                current_x += next_float()
                current_y += next_float()
            points.append((current_x, current_y))
        elif cmd in ('Z', 'z'):
            current_x, current_y = start_x, start_y
        else:
            i += 1

    return points


def compute_bbox_from_path(d: str, transform=None) -> BBox:
    """Compute bounding box of an SVG path, applying transform if given."""
    points = extract_coordinates_from_path(d)
    if not points:
        return BBox(0, 0, 0, 0)

    if transform:
        m = re.search(r'matrix\(([-\d.]+),([-\d.]+),([-\d.]+),([-\d.]+),([-\d.]+),([-\d.]+)\)',
                       transform.replace(' ', ''))
        if m:
            a, b, c, d_val, e, f = [float(x) for x in m.groups()]
            transformed = [(a * x + c * y + e, b * x + d_val * y + f) for x, y in points]
            points = transformed

    xs = [p[0] for p in points]
    ys = [p[1] for p in points]

    return BBox(min(xs), min(ys), max(xs), max(ys))


def classify_region(bbox: BBox) -> str:
    """
    Classify a path into a body region based on its bounding box.

    The figure is a front-facing muscular male on a 1333x1333 canvas.
    Two halves: left body ~x 180-530, right body ~x 800-1160.
    Body center line is approximately x=667.

    Vertical zones (approximate):
    - Head/neck: y < 200
    - Shoulders: y 150-320 at outer torso
    - Chest: y 220-410 inner torso
    - Arms: y 250-650 outside torso width
    - Core: y 400-580 center torso
    - Back/lats: y 350-550 between arms and core
    - Lower back: y 480-620 outer core
    - Legs: y > 540 between arms
    - Hands: y 400-650 far from center
    - Feet: y > 1100
    """
    cx, cy = bbox.center_x, bbox.center_y
    w, h = bbox.width, bbox.height
    area = bbox.area

    # Very large paths are outlines
    if area > 200000:
        return "BASE_OUTLINE"

    # Shadow/ground at bottom
    if cy > 1200:
        return "BASE_GROUND"

    # Feet: very bottom
    if cy > 1100:
        return "BASE_FEET"

    # Head region
    if cy < 120:
        return "BASE_HEAD"

    # Neck
    if 120 <= cy < 185 and 560 < cx < 770:
        return "BASE_NECK"

    # Define torso boundaries for left and right halves
    # Left half torso: roughly x 460-540
    # Right half torso: roughly x 790-870
    left_torso_inner = 530
    left_torso_outer = 460
    right_torso_inner = 800
    right_torso_outer = 870

    # Define arm boundaries
    # Left arm: x < 460 (outside left torso)
    # Right arm: x > 870 (outside right torso)

    # --- HANDS (far extremities at arm level) ---
    # Left side hands/forearms: x < 260 at low arm y, or very far out
    if cy > 400 and cx < 260:
        return "BASE_HANDS"
    # Right side hands: x > 1090 at low arm y
    if cy > 400 and cx > 1090:
        return "BASE_HANDS"

    # --- SHOULDERS ---
    if 150 <= cy < 330:
        # Left shoulder area
        if 280 < cx < 530:
            return "SHOULDERS"
        # Right shoulder area
        if 800 < cx < 1120:
            return "SHOULDERS"

    # --- CHEST ---
    if 330 <= cy < 420:
        # Left chest: inner torso area
        if 480 < cx < 560:
            return "CHEST"
        # Right chest: inner torso area
        if 790 < cx < 880:
            return "CHEST"

    # --- ARMS (upper arms, biceps, triceps) ---
    if 300 <= cy < 650:
        # Left arm: outside torso
        if cx < 470:
            return "ARMS"
        # Right arm: outside torso
        if cx > 870:
            return "ARMS"

    # --- CORE (abs) ---
    if 400 <= cy < 580:
        # Left side abs
        if 510 < cx < 570:
            return "CORE"
        # Right side abs
        if 770 < cx < 830:
            return "CORE"

    # --- BACK (lats from front) ---
    if 370 <= cy < 530:
        # Left lat area: between arm and abs
        if 470 <= cx <= 520:
            return "BACK"
        # Right lat area
        if 810 <= cx <= 860:
            return "BACK"

    # --- LOWER_BACK ---
    if 520 <= cy < 620:
        # Left oblique/lower back area
        if 470 <= cx <= 555:
            return "LOWER_BACK"
        # Right side
        if 790 <= cx <= 850:
            return "LOWER_BACK"

    # --- LEGS ---
    # Everything in the leg zone that's between the outer arm edges
    if cy >= 560:
        # Left leg zone
        if 260 < cx < 530:
            return "LEGS"
        # Right leg zone
        if 870 < cx < 1100:
            return "LEGS"
        # Inner legs / groin
        if 530 <= cx <= 870 and cy > 570:
            return "LEGS"

    # Hip transition zone
    if 540 <= cy < 650:
        if 460 < cx < 560:
            return "LEGS"
        if 780 < cx < 880:
            return "LEGS"

    # Catch-all
    return "UNCLASSIFIED"


def main():
    svg_path = "resources/anatomy_template.svg"
    tree = ET.parse(svg_path)
    root = tree.getroot()

    results = []
    for path_elem in root.iter('{http://www.w3.org/2000/svg}path'):
        pid = path_elem.get('id', 'unknown')
        d = path_elem.get('d', '')
        transform = path_elem.get('transform', None)
        style = path_elem.get('style', '')

        fill_match = re.search(r'fill:(#[A-Fa-f0-9]+)', style)
        fill = fill_match.group(1).upper() if fill_match else 'none'

        bbox = compute_bbox_from_path(d, transform)
        region = classify_region(bbox)

        results.append({
            'id': pid,
            'cx': bbox.center_x,
            'cy': bbox.center_y,
            'w': bbox.width,
            'h': bbox.height,
            'area': bbox.area,
            'min_x': bbox.min_x,
            'min_y': bbox.min_y,
            'max_x': bbox.max_x,
            'max_y': bbox.max_y,
            'fill': fill,
            'region': region,
        })

    # Sort by center_y then center_x
    results.sort(key=lambda r: (r['cy'], r['cx']))

    # Print all paths with bounding box info
    print("=" * 120)
    print(f"{'PATH ID':<12} {'CX':>8} {'CY':>8} {'WIDTH':>8} {'HEIGHT':>8} {'AREA':>12} {'FILL':>9} {'REGION':<20}")
    print("=" * 120)
    for r in results:
        print(f"{r['id']:<12} {r['cx']:8.1f} {r['cy']:8.1f} {r['w']:8.1f} {r['h']:8.1f} {r['area']:12.0f} {r['fill']:>9} {r['region']:<20}")

    # Group by region
    print("\n" + "=" * 80)
    print("GROUPED BY REGION")
    print("=" * 80)

    regions = {}
    for r in results:
        region = r['region']
        if region not in regions:
            regions[region] = []
        regions[region].append(r['id'])

    for region in sorted(regions.keys()):
        ids = regions[region]
        print(f"\n{region} ({len(ids)} paths):")
        print(f"  {ids}")

    # Generate Kotlin mapping
    print("\n" + "=" * 80)
    print("KOTLIN MAPPING")
    print("=" * 80)

    muscle_groups = {
        'CHEST': regions.get('CHEST', []),
        'SHOULDERS': regions.get('SHOULDERS', []),
        'ARMS': regions.get('ARMS', []),
        'CORE': regions.get('CORE', []),
        'LEGS': regions.get('LEGS', []),
        'BACK': regions.get('BACK', []),
        'LOWER_BACK': regions.get('LOWER_BACK', []),
    }

    base_paths = []
    for key in regions:
        if key.startswith('BASE_') or key == 'UNCLASSIFIED':
            base_paths.extend(regions[key])

    print("\nval MUSCLE_GROUP_PATHS: Map<MuscleGroup, Set<String>> = mapOf(")
    for mg, ids in muscle_groups.items():
        id_str = ', '.join(f'"{pid}"' for pid in sorted(ids, key=lambda x: int(x.replace('path', ''))))
        print(f'    MuscleGroup.{mg} to setOf({id_str}),')
    print(")")

    print(f"\nval BASE_PATHS: Set<String> = setOf(")
    base_sorted = sorted(base_paths, key=lambda x: int(x.replace('path', '')))
    for i in range(0, len(base_sorted), 8):
        chunk = base_sorted[i:i+8]
        id_str = ', '.join(f'"{pid}"' for pid in chunk)
        comma = ',' if i + 8 < len(base_sorted) else ''
        print(f'    {id_str}{comma}')
    print(")")

    total_classified = sum(len(v) for v in muscle_groups.values())
    print(f"\nTotal paths: {len(results)}")
    print(f"Classified into muscle groups: {total_classified}")
    print(f"Base/unclassified: {len(base_paths)}")
    print(f"Unclassified specifically: {len(regions.get('UNCLASSIFIED', []))}")

    if 'UNCLASSIFIED' in regions:
        print("\nUNCLASSIFIED DETAILS (need manual review):")
        for r in results:
            if r['region'] == 'UNCLASSIFIED':
                print(f"  {r['id']}: cx={r['cx']:.1f} cy={r['cy']:.1f} w={r['w']:.1f} h={r['h']:.1f} bbox=({r['min_x']:.1f},{r['min_y']:.1f})-({r['max_x']:.1f},{r['max_y']:.1f})")


if __name__ == '__main__':
    main()
