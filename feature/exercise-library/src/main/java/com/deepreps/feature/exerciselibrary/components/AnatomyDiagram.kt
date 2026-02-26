@file:Suppress("TooManyFunctions")

package com.deepreps.feature.exerciselibrary.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.ui.theme.DeepRepsTheme

private val BaseColor = Color(0xFF717171)
private val OutlineColor = Color(0xFF4A4A54)
private val SkinColor = Color(0xFF9A9A9A)
private const val PRIMARY_ALPHA = 0.85f

/**
 * Simplified front-facing body silhouette drawn with Compose Canvas.
 *
 * Each muscle group maps to body zones (rectangles/rounded rects). The primary
 * muscle group is highlighted with its accent color at 85% opacity; all other
 * zones use the #717171 base fill. Head, hands, and feet use #9A9A9A.
 * Body outline stroke: #4A4A54, 1.5dp.
 *
 * @param exerciseId The exercise whose muscles to highlight (reserved for future use).
 * @param primaryGroupId The primary muscle group ID (1-indexed, maps to [MuscleGroup] ordinal + 1).
 * @param modifier External modifier.
 */
@Composable
fun AnatomyDiagram(
    @Suppress("UnusedParameter") exerciseId: Long,
    primaryGroupId: Long,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    val muscleGroup = muscleGroupFromId(primaryGroupId)
    val primaryColor = if (muscleGroup != null) {
        colors.colorForMuscleGroup(muscleGroup).copy(alpha = PRIMARY_ALPHA)
    } else {
        BaseColor
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
        ) {
            drawBodyDiagram(muscleGroup, primaryColor)
        }

        if (muscleGroup != null) {
            Text(
                text = formatMuscleGroupLabel(muscleGroup),
                style = typography.labelMedium,
                color = primaryColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Canvas drawing
// ---------------------------------------------------------------------------

@Suppress("LongMethod")
private fun DrawScope.drawBodyDiagram(
    highlighted: MuscleGroup?,
    primaryColor: Color,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val outlineStroke = Stroke(width = 1.5.dp.toPx())

    fun zoneColor(group: MuscleGroup): Color =
        if (highlighted == group) primaryColor else BaseColor

    // -- Head --
    val headRadius = w * 0.055f
    val headCy = h * 0.07f
    drawCircle(color = SkinColor, radius = headRadius, center = Offset(cx, headCy))
    drawCircle(
        color = OutlineColor,
        radius = headRadius,
        center = Offset(cx, headCy),
        style = outlineStroke,
    )

    // -- Neck --
    drawNeck(cx, h, w, outlineStroke)

    // -- Shoulders --
    drawShoulderZones(cx, h, w, zoneColor(MuscleGroup.SHOULDERS), outlineStroke)

    // -- Chest (upper torso) --
    drawChestZone(cx, h, w, zoneColor(MuscleGroup.CHEST), outlineStroke)

    // -- Back (lat spread, visible from front as side torso) --
    drawBackZones(cx, h, w, zoneColor(MuscleGroup.BACK), outlineStroke)

    // -- Core (mid torso) --
    drawCoreZone(cx, h, w, zoneColor(MuscleGroup.CORE), outlineStroke)

    // -- Lower Back --
    drawLowerBackZone(cx, h, w, zoneColor(MuscleGroup.LOWER_BACK), outlineStroke)

    // -- Arms (upper arms) --
    drawArmZones(cx, h, w, zoneColor(MuscleGroup.ARMS), outlineStroke)

    // -- Forearms (always base) --
    drawForearmZones(cx, h, w, outlineStroke)

    // -- Hands --
    drawHands(cx, h, w, outlineStroke)

    // -- Legs (thighs + calves) --
    drawLegZones(cx, h, w, zoneColor(MuscleGroup.LEGS), outlineStroke)

    // -- Feet --
    drawFeet(cx, h, w, outlineStroke)
}

// ---------------------------------------------------------------------------
// Body zone draw helpers
// ---------------------------------------------------------------------------

private fun DrawScope.drawNeck(cx: Float, h: Float, w: Float, stroke: Stroke) {
    val neckTopLeft = Offset(cx - w * 0.025f, h * 0.115f)
    val neckSize = Size(w * 0.05f, h * 0.045f)
    drawRect(color = BaseColor, topLeft = neckTopLeft, size = neckSize)
    drawRect(color = OutlineColor, topLeft = neckTopLeft, size = neckSize, style = stroke)
}

private fun DrawScope.drawShoulderZones(
    cx: Float,
    h: Float,
    w: Float,
    color: Color,
    stroke: Stroke,
) {
    val sy = h * 0.16f
    val sh = h * 0.055f
    val sw = w * 0.085f
    val cr = CornerRadius(4f, 4f)

    // Left shoulder
    val leftTop = Offset(cx - w * 0.21f, sy)
    drawRoundRect(color = color, topLeft = leftTop, size = Size(sw, sh), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = leftTop,
        size = Size(sw, sh),
        cornerRadius = cr,
        style = stroke,
    )

    // Right shoulder
    val rightTop = Offset(cx + w * 0.125f, sy)
    drawRoundRect(color = color, topLeft = rightTop, size = Size(sw, sh), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = rightTop,
        size = Size(sw, sh),
        cornerRadius = cr,
        style = stroke,
    )
}

private fun DrawScope.drawChestZone(
    cx: Float,
    h: Float,
    w: Float,
    color: Color,
    stroke: Stroke,
) {
    val topLeft = Offset(cx - w * 0.125f, h * 0.16f)
    val boxSize = Size(w * 0.25f, h * 0.115f)
    val cr = CornerRadius(8f, 8f)
    drawRoundRect(color = color, topLeft = topLeft, size = boxSize, cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = topLeft,
        size = boxSize,
        cornerRadius = cr,
        style = stroke,
    )
}

private fun DrawScope.drawBackZones(
    cx: Float,
    h: Float,
    w: Float,
    color: Color,
    stroke: Stroke,
) {
    val cr = CornerRadius(4f, 4f)
    val bw = w * 0.035f
    val bh = h * 0.17f
    val by = h * 0.21f

    // Left lat
    val leftTop = Offset(cx - w * 0.145f, by)
    drawRoundRect(color = color, topLeft = leftTop, size = Size(bw, bh), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = leftTop,
        size = Size(bw, bh),
        cornerRadius = cr,
        style = stroke,
    )

    // Right lat
    val rightTop = Offset(cx + w * 0.11f, by)
    drawRoundRect(color = color, topLeft = rightTop, size = Size(bw, bh), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = rightTop,
        size = Size(bw, bh),
        cornerRadius = cr,
        style = stroke,
    )
}

private fun DrawScope.drawCoreZone(
    cx: Float,
    h: Float,
    w: Float,
    color: Color,
    stroke: Stroke,
) {
    val topLeft = Offset(cx - w * 0.11f, h * 0.275f)
    val boxSize = Size(w * 0.22f, h * 0.13f)
    val cr = CornerRadius(4f, 4f)
    drawRoundRect(color = color, topLeft = topLeft, size = boxSize, cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = topLeft,
        size = boxSize,
        cornerRadius = cr,
        style = stroke,
    )
}

private fun DrawScope.drawLowerBackZone(
    cx: Float,
    h: Float,
    w: Float,
    color: Color,
    stroke: Stroke,
) {
    val topLeft = Offset(cx - w * 0.095f, h * 0.39f)
    val boxSize = Size(w * 0.19f, h * 0.05f)
    val cr = CornerRadius(4f, 4f)
    drawRoundRect(color = color, topLeft = topLeft, size = boxSize, cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = topLeft,
        size = boxSize,
        cornerRadius = cr,
        style = stroke,
    )
}

private fun DrawScope.drawArmZones(
    cx: Float,
    h: Float,
    w: Float,
    color: Color,
    stroke: Stroke,
) {
    val cr = CornerRadius(8f, 8f)
    val aw = w * 0.055f
    val ah = h * 0.19f
    val ay = h * 0.215f

    // Left arm
    val leftTop = Offset(cx - w * 0.245f, ay)
    drawRoundRect(color = color, topLeft = leftTop, size = Size(aw, ah), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = leftTop,
        size = Size(aw, ah),
        cornerRadius = cr,
        style = stroke,
    )

    // Right arm
    val rightTop = Offset(cx + w * 0.19f, ay)
    drawRoundRect(color = color, topLeft = rightTop, size = Size(aw, ah), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = rightTop,
        size = Size(aw, ah),
        cornerRadius = cr,
        style = stroke,
    )
}

private fun DrawScope.drawForearmZones(
    cx: Float,
    h: Float,
    w: Float,
    stroke: Stroke,
) {
    val cr = CornerRadius(6f, 6f)
    val fw = w * 0.045f
    val fh = h * 0.14f
    val fy = h * 0.41f

    // Left forearm
    val leftTop = Offset(cx - w * 0.26f, fy)
    drawRoundRect(color = BaseColor, topLeft = leftTop, size = Size(fw, fh), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = leftTop,
        size = Size(fw, fh),
        cornerRadius = cr,
        style = stroke,
    )

    // Right forearm
    val rightTop = Offset(cx + w * 0.215f, fy)
    drawRoundRect(color = BaseColor, topLeft = rightTop, size = Size(fw, fh), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = rightTop,
        size = Size(fw, fh),
        cornerRadius = cr,
        style = stroke,
    )
}

private fun DrawScope.drawHands(cx: Float, h: Float, w: Float, stroke: Stroke) {
    val handRadius = w * 0.018f
    val handY = h * 0.56f
    val leftCenter = Offset(cx - w * 0.24f, handY)
    val rightCenter = Offset(cx + w * 0.24f, handY)

    drawCircle(color = SkinColor, radius = handRadius, center = leftCenter)
    drawCircle(color = OutlineColor, radius = handRadius, center = leftCenter, style = stroke)
    drawCircle(color = SkinColor, radius = handRadius, center = rightCenter)
    drawCircle(color = OutlineColor, radius = handRadius, center = rightCenter, style = stroke)
}

private fun DrawScope.drawLegZones(
    cx: Float,
    h: Float,
    w: Float,
    color: Color,
    stroke: Stroke,
) {
    drawThighZones(cx, h, w, color, stroke)
    drawCalfZones(cx, h, w, color, stroke)
}

private fun DrawScope.drawThighZones(
    cx: Float,
    h: Float,
    w: Float,
    color: Color,
    stroke: Stroke,
) {
    val cr = CornerRadius(8f, 8f)
    val tw = w * 0.095f
    val th = h * 0.20f
    val ty = h * 0.44f

    val leftThigh = Offset(cx - w * 0.115f, ty)
    drawRoundRect(color = color, topLeft = leftThigh, size = Size(tw, th), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = leftThigh,
        size = Size(tw, th),
        cornerRadius = cr,
        style = stroke,
    )

    val rightThigh = Offset(cx + w * 0.02f, ty)
    drawRoundRect(color = color, topLeft = rightThigh, size = Size(tw, th), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = rightThigh,
        size = Size(tw, th),
        cornerRadius = cr,
        style = stroke,
    )
}

private fun DrawScope.drawCalfZones(
    cx: Float,
    h: Float,
    w: Float,
    color: Color,
    stroke: Stroke,
) {
    val cr = CornerRadius(6f, 6f)
    val cw = w * 0.065f
    val ch = h * 0.20f
    val cy = h * 0.65f

    val leftCalf = Offset(cx - w * 0.095f, cy)
    drawRoundRect(color = color, topLeft = leftCalf, size = Size(cw, ch), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = leftCalf,
        size = Size(cw, ch),
        cornerRadius = cr,
        style = stroke,
    )

    val rightCalf = Offset(cx + w * 0.03f, cy)
    drawRoundRect(color = color, topLeft = rightCalf, size = Size(cw, ch), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = rightCalf,
        size = Size(cw, ch),
        cornerRadius = cr,
        style = stroke,
    )
}

private fun DrawScope.drawFeet(cx: Float, h: Float, w: Float, stroke: Stroke) {
    val cr = CornerRadius(4f, 4f)
    val fw = w * 0.065f
    val fh = h * 0.035f
    val fy = h * 0.86f

    val leftFoot = Offset(cx - w * 0.095f, fy)
    drawRoundRect(color = BaseColor, topLeft = leftFoot, size = Size(fw, fh), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = leftFoot,
        size = Size(fw, fh),
        cornerRadius = cr,
        style = stroke,
    )

    val rightFoot = Offset(cx + w * 0.03f, fy)
    drawRoundRect(color = BaseColor, topLeft = rightFoot, size = Size(fw, fh), cornerRadius = cr)
    drawRoundRect(
        color = OutlineColor,
        topLeft = rightFoot,
        size = Size(fw, fh),
        cornerRadius = cr,
        style = stroke,
    )
}

// ---------------------------------------------------------------------------
// Utility
// ---------------------------------------------------------------------------

/**
 * Maps a muscle group database ID back to the [MuscleGroup] enum.
 * Returns null if the ID doesn't map to a known group.
 *
 * Assumes 1-indexed IDs matching [MuscleGroup] enum ordinal + 1.
 */
private fun muscleGroupFromId(groupId: Long): MuscleGroup? {
    val index = (groupId - 1).toInt()
    return MuscleGroup.entries.getOrNull(index)
}

/**
 * Formats a [MuscleGroup] enum value as a user-facing label.
 * e.g. LOWER_BACK -> "Lower Back"
 */
private fun formatMuscleGroupLabel(group: MuscleGroup): String =
    group.value
        .split("_")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Chest - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ChestDiagramDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        AnatomyDiagram(
            exerciseId = 1,
            primaryGroupId = 3, // Chest
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Legs - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun LegsDiagramDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        AnatomyDiagram(
            exerciseId = 2,
            primaryGroupId = 1, // Legs
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Back - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun BackDiagramDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        AnatomyDiagram(
            exerciseId = 3,
            primaryGroupId = 4, // Back
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Arms - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ArmsDiagramDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        AnatomyDiagram(
            exerciseId = 4,
            primaryGroupId = 6, // Arms
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Core - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun CoreDiagramDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        AnatomyDiagram(
            exerciseId = 5,
            primaryGroupId = 7, // Core
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Shoulders - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ShouldersDiagramDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        AnatomyDiagram(
            exerciseId = 6,
            primaryGroupId = 5, // Shoulders
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Lower Back - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun LowerBackDiagramDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        AnatomyDiagram(
            exerciseId = 7,
            primaryGroupId = 2, // Lower Back
            modifier = Modifier.padding(16.dp),
        )
    }
}
