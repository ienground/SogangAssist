package net.ienlab.sogangassist.icon.myiconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.ienlab.sogangassist.icon.MyIconPack

public val MyIconPack.AppIcon: ImageVector
    get() {
        if (_appIcon != null) {
            return _appIcon!!
        }
        _appIcon = Builder(name = "AppIcon", defaultWidth = 2048.0.dp, defaultHeight = 2048.0.dp,
                viewportWidth = 2048.0f, viewportHeight = 2048.0f).apply {
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1696.0f, 1504.0f)
                horizontalLineToRelative(-82.52f)
                curveToRelative(-0.54f, -0.81f, -1.09f, -1.61f, -1.62f, -2.43f)
                curveToRelative(-15.49f, -24.1f, -25.88f, -50.86f, -30.75f, -78.81f)
                horizontalLineToRelative(-178.36f)
                curveToRelative(-32.31f, 0.0f, -63.12f, -9.69f, -89.1f, -28.03f)
                curveToRelative(-23.91f, -16.87f, -42.52f, -40.22f, -53.83f, -67.52f)
                curveToRelative(-11.36f, -27.42f, -14.7f, -57.24f, -9.67f, -86.25f)
                curveToRelative(5.46f, -31.46f, 20.45f, -60.2f, 43.37f, -83.11f)
                lineToRelative(48.09f, -48.09f)
                verticalLineTo(854.15f)
                curveToRelative(0.0f, -110.77f, 29.85f, -212.57f, 86.34f, -294.39f)
                curveToRelative(29.61f, -42.89f, 65.92f, -79.33f, 107.94f, -108.3f)
                curveToRelative(23.16f, -15.98f, 47.9f, -29.57f, 74.0f, -40.68f)
                curveToRelative(3.36f, -20.56f, 10.18f, -40.48f, 20.19f, -58.78f)
                horizontalLineTo(544.0f)
                verticalLineToRelative(384.0f)
                lineToRelative(-96.0f, -96.0f)
                lineToRelative(-96.0f, 96.0f)
                verticalLineTo(352.0f)
                curveToRelative(-52.8f, 0.0f, -96.0f, 43.2f, -96.0f, 96.0f)
                verticalLineToRelative(960.0f)
                curveToRelative(0.0f, 52.8f, 43.2f, 96.0f, 96.0f, 96.0f)
                horizontalLineToRelative(0.0f)
                curveToRelative(-52.8f, 0.0f, -96.0f, 43.2f, -96.0f, 96.0f)
                verticalLineToRelative(0.0f)
                curveToRelative(0.0f, 52.8f, 43.2f, 96.0f, 96.0f, 96.0f)
                horizontalLineToRelative(1344.0f)
                curveToRelative(52.8f, 0.0f, 96.0f, -43.2f, 96.0f, -96.0f)
                verticalLineToRelative(0.0f)
                curveTo(1792.0f, 1547.2f, 1748.8f, 1504.0f, 1696.0f, 1504.0f)
                close()
                moveTo(474.54f, 1213.29f)
                lineToRelative(11.48f, 33.81f)
                curveToRelative(0.0f, 0.0f, -11.63f, 1.8f, -23.53f, 4.56f)
                lineToRelative(19.86f, 53.5f)
                curveToRelative(0.0f, 0.0f, 38.73f, -6.51f, 76.2f, -8.37f)
                lineToRelative(-2.29f, -57.83f)
                curveToRelative(0.0f, 0.0f, -8.2f, -0.07f, -19.5f, 1.09f)
                lineToRelative(-3.82f, -33.71f)
                curveToRelative(19.53f, -1.56f, 39.26f, -2.4f, 59.18f, -2.4f)
                curveToRelative(19.86f, 0.0f, 39.5f, 0.84f, 58.99f, 2.4f)
                lineToRelative(-3.86f, 33.71f)
                curveToRelative(-11.27f, -1.16f, -19.46f, -1.09f, -19.46f, -1.09f)
                lineToRelative(-2.32f, 57.83f)
                curveToRelative(37.49f, 1.86f, 76.24f, 8.37f, 76.24f, 8.37f)
                lineToRelative(19.88f, -53.28f)
                curveToRelative(-11.46f, -3.2f, -23.5f, -4.84f, -23.5f, -4.84f)
                lineToRelative(11.39f, -33.79f)
                curveToRelative(42.12f, 6.69f, 83.07f, 16.8f, 122.52f, 30.22f)
                curveToRelative(-19.93f, 25.39f, -75.18f, 115.26f, -98.25f, 164.52f)
                curveToRelative(0.0f, 0.0f, -130.22f, -30.55f, -283.42f, 0.0f)
                curveToRelative(0.0f, 0.0f, -48.26f, -95.65f, -98.33f, -164.45f)
                curveTo(391.44f, 1230.09f, 432.46f, 1219.96f, 474.54f, 1213.29f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1673.92f, 1385.85f)
                curveToRelative(0.0f, 46.93f, 27.43f, 87.69f, 67.36f, 106.72f)
                curveToRelative(30.13f, -16.24f, 50.72f, -48.12f, 50.72f, -84.57f)
                verticalLineToRelative(-22.15f)
                horizontalLineTo(1673.92f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1744.95f, 365.51f)
                curveToRelative(-24.94f, 15.66f, -41.49f, 43.4f, -41.49f, 75.1f)
                verticalLineToRelative(40.17f)
                curveToRelative(-169.55f, 40.17f, -265.85f, 191.41f, -265.85f, 373.37f)
                verticalLineToRelative(295.38f)
                lineToRelative(-76.21f, 76.21f)
                curveToRelative(-37.22f, 37.22f, -11.22f, 101.02f, 41.35f, 101.02f)
                horizontalLineTo(1792.0f)
                verticalLineTo(448.0f)
                curveTo(1792.0f, 413.05f, 1773.06f, 382.31f, 1744.95f, 365.51f)
                close()
            }
        }
        .build()
        return _appIcon!!
    }

private var _appIcon: ImageVector? = null
