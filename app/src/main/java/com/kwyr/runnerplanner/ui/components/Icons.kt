package com.kwyr.runnerplanner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun HomeIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(22.dp)) {
        val scale = size.width / 24f
        val path = Path().apply {
            moveTo(3f * scale, 9f * scale)
            lineTo(12f * scale, 2f * scale)
            lineTo(21f * scale, 9f * scale)
            lineTo(21f * scale, 20f * scale)
            cubicTo(21f * scale, 20.5304f * scale, 20.7893f * scale, 21.0391f * scale, 20.4142f * scale, 21.4142f * scale)
            cubicTo(20.0391f * scale, 20.7893f * scale, 19.5304f * scale, 22f * scale, 19f * scale, 22f * scale)
            lineTo(5f * scale, 22f * scale)
            cubicTo(4.46957f * scale, 22f * scale, 3.96086f * scale, 21.7893f * scale, 3.58579f * scale, 21.4142f * scale)
            cubicTo(3.21071f * scale, 21.0391f * scale, 3f * scale, 20.5304f * scale, 3f * scale, 20f * scale)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun MapPinIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(28.dp)) {
        val scale = size.width / 24f
        val path = Path().apply {
            moveTo(21f * scale, 10f * scale)
            cubicTo(21f * scale, 17f * scale, 12f * scale, 23f * scale, 12f * scale, 23f * scale)
            cubicTo(12f * scale, 23f * scale, 3f * scale, 17f * scale, 3f * scale, 10f * scale)
            cubicTo(3f * scale, 7.61305f * scale, 3.94821f * scale, 5.32387f * scale, 5.63604f * scale, 3.63604f * scale)
            cubicTo(7.32387f * scale, 1.94821f * scale, 9.61305f * scale, 1f * scale, 12f * scale, 1f * scale)
            cubicTo(14.3869f * scale, 1f * scale, 16.6761f * scale, 1.94821f * scale, 18.364f * scale, 3.63604f * scale)
            cubicTo(20.0518f * scale, 5.32387f * scale, 21f * scale, 7.61305f * scale, 21f * scale, 10f * scale)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawCircle(
            color = color,
            radius = 3f * scale,
            center = Offset(12f * scale, 10f * scale),
            style = Stroke(width = 2.5f)
        )
    }
}

@Composable
fun WatchIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(28.dp)) {
        val scale = size.width / 24f
        drawCircle(
            color = color,
            radius = 7f * scale,
            center = Offset(12f * scale, 12f * scale),
            style = Stroke(width = 2.5f)
        )
        drawLine(
            color = color,
            start = Offset(12f * scale, 12f * scale),
            end = Offset(12f * scale, 8f * scale),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(12f * scale, 12f * scale),
            end = Offset(15f * scale, 12f * scale),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun UserIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(28.dp)) {
        val scale = size.width / 24f
        drawCircle(
            color = color,
            radius = 4f * scale,
            center = Offset(12f * scale, 8f * scale),
            style = Stroke(width = 2.5f)
        )
        val path = Path().apply {
            moveTo(20f * scale, 21f * scale)
            lineTo(20f * scale, 19f * scale)
            cubicTo(20f * scale, 17.9391f * scale, 19.5786f * scale, 16.9217f * scale, 18.8284f * scale, 16.1716f * scale)
            cubicTo(18.0783f * scale, 15.4214f * scale, 17.0609f * scale, 15f * scale, 16f * scale, 15f * scale)
            lineTo(8f * scale, 15f * scale)
            cubicTo(6.93913f * scale, 15f * scale, 5.92172f * scale, 15.4214f * scale, 5.17157f * scale, 16.1716f * scale)
            cubicTo(4.42143f * scale, 16.9217f * scale, 4f * scale, 17.9391f * scale, 4f * scale, 19f * scale)
            lineTo(4f * scale, 21f * scale)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun SettingsIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(24.dp)) {
        drawCircle(
            color = color,
            radius = 3f,
            center = Offset(12f, 12f),
            style = Stroke(width = 2f)
        )
        val path = Path().apply {
            moveTo(12f, 1f)
            lineTo(12f, 3f)
            moveTo(12f, 21f)
            lineTo(12f, 23f)
            moveTo(4.22f, 4.22f)
            lineTo(5.64f, 5.64f)
            moveTo(18.36f, 18.36f)
            lineTo(19.78f, 19.78f)
            moveTo(1f, 12f)
            lineTo(3f, 12f)
            moveTo(21f, 12f)
            lineTo(23f, 12f)
            moveTo(4.22f, 19.78f)
            lineTo(5.64f, 18.36f)
            moveTo(18.36f, 5.64f)
            lineTo(19.78f, 4.22f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun UploadIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(22.dp)) {
        val scale = size.width / 24f
        val path = Path().apply {
            moveTo(21f * scale, 15f * scale)
            lineTo(21f * scale, 19f * scale)
            cubicTo(21f * scale, 19.5304f * scale, 20.7893f * scale, 20.0391f * scale, 20.4142f * scale, 20.4142f * scale)
            cubicTo(20.0391f * scale, 20.7893f * scale, 19.5304f * scale, 21f * scale, 19f * scale, 21f * scale)
            lineTo(5f * scale, 21f * scale)
            cubicTo(4.46957f * scale, 21f * scale, 3.96086f * scale, 20.7893f * scale, 3.58579f * scale, 20.4142f * scale)
            cubicTo(3.21071f * scale, 20.0391f * scale, 3f * scale, 19.5304f * scale, 3f * scale, 19f * scale)
            lineTo(3f * scale, 15f * scale)
            moveTo(17f * scale, 8f * scale)
            lineTo(12f * scale, 3f * scale)
            lineTo(7f * scale, 8f * scale)
            moveTo(12f * scale, 3f * scale)
            lineTo(12f * scale, 15f * scale)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun DownloadIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(22.dp)) {
        val scale = size.width / 24f
        val path = Path().apply {
            moveTo(21f * scale, 15f * scale)
            lineTo(21f * scale, 19f * scale)
            cubicTo(21f * scale, 19.5304f * scale, 20.7893f * scale, 20.0391f * scale, 20.4142f * scale, 20.4142f * scale)
            cubicTo(20.0391f * scale, 20.7893f * scale, 19.5304f * scale, 21f * scale, 19f * scale, 21f * scale)
            lineTo(5f * scale, 21f * scale)
            cubicTo(4.46957f * scale, 21f * scale, 3.96086f * scale, 20.7893f * scale, 3.58579f * scale, 20.4142f * scale)
            cubicTo(3.21071f * scale, 20.0391f * scale, 3f * scale, 19.5304f * scale, 3f * scale, 19f * scale)
            lineTo(3f * scale, 15f * scale)
            moveTo(7f * scale, 10f * scale)
            lineTo(12f * scale, 15f * scale)
            lineTo(17f * scale, 10f * scale)
            moveTo(12f * scale, 15f * scale)
            lineTo(12f * scale, 3f * scale)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun ChevronLeftIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(15f, 18f)
            lineTo(9f, 12f)
            lineTo(15f, 6f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun ChevronRightIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(24.dp)) {
        val path = Path().apply {
            moveTo(9f, 18f)
            lineTo(15f, 12f)
            lineTo(9f, 6f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun PlusIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(24.dp)) {
        val scale = size.width / 24f
        val path = Path().apply {
            moveTo(12f * scale, 5f * scale)
            lineTo(12f * scale, 19f * scale)
            moveTo(5f * scale, 12f * scale)
            lineTo(19f * scale, 12f * scale)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun CalendarIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(24.dp)) {
        val scale = size.width / 24f
        val path = Path().apply {
            // Calendar outline - simple rounded rectangle
            moveTo(5f * scale, 6f * scale)
            lineTo(5f * scale, 20f * scale)
            cubicTo(5f * scale, 20.5523f * scale, 5.44772f * scale, 21f * scale, 6f * scale, 21f * scale)
            lineTo(18f * scale, 21f * scale)
            cubicTo(18.5523f * scale, 21f * scale, 19f * scale, 20.5523f * scale, 19f * scale, 20f * scale)
            lineTo(19f * scale, 6f * scale)
            cubicTo(19f * scale, 5.44772f * scale, 18.5523f * scale, 5f * scale, 18f * scale, 5f * scale)
            lineTo(6f * scale, 5f * scale)
            cubicTo(5.44772f * scale, 5f * scale, 5f * scale, 5.44772f * scale, 5f * scale, 6f * scale)
            close()
            
            // Top hooks (minimal lines)
            moveTo(8f * scale, 3f * scale)
            lineTo(8f * scale, 7f * scale)
            
            moveTo(16f * scale, 3f * scale)
            lineTo(16f * scale, 7f * scale)
            
            // Horizontal line separating header
            moveTo(5f * scale, 9f * scale)
            lineTo(19f * scale, 9f * scale)
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun BikeIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(24.dp)) {
        val scale = size.width / 24f
        // Rear wheel
        drawCircle(
            color = color,
            radius = 4f * scale,
            center = Offset(5.5f * scale, 16f * scale),
            style = Stroke(width = 2f)
        )
        // Front wheel
        drawCircle(
            color = color,
            radius = 4f * scale,
            center = Offset(18.5f * scale, 16f * scale),
            style = Stroke(width = 2f)
        )
        // Frame
        val framePath = Path().apply {
            // Seat tube: seat to bottom bracket
            moveTo(9f * scale, 7f * scale)
            lineTo(9f * scale, 16f * scale)
            // Chain stay: BB to rear wheel
            lineTo(5.5f * scale, 16f * scale)
            // Seat stay: seat to rear wheel
            moveTo(9f * scale, 7f * scale)
            lineTo(5.5f * scale, 16f * scale)
            // Top tube + down tube: seat to head tube
            moveTo(9f * scale, 7f * scale)
            lineTo(14f * scale, 7f * scale)
            // Down tube: head to BB
            lineTo(9f * scale, 16f * scale)
            // Fork: head to front wheel
            moveTo(14f * scale, 7f * scale)
            lineTo(18.5f * scale, 16f * scale)
        }
        drawPath(framePath, color = color, style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        // Handlebar
        drawLine(color = color, start = Offset(14f * scale, 7f * scale), end = Offset(16f * scale, 5f * scale), strokeWidth = 2f, cap = StrokeCap.Round)
        // Saddle
        drawLine(color = color, start = Offset(7.5f * scale, 7f * scale), end = Offset(10.5f * scale, 7f * scale), strokeWidth = 2f, cap = StrokeCap.Round)
    }
}

@Composable
fun TrashIcon(modifier: Modifier = Modifier, color: Color = Color.Black) {
    Canvas(modifier = modifier.size(24.dp)) {
        val scale = size.width / 24f
        val path = Path().apply {
            moveTo(3f * scale, 6f * scale)
            lineTo(5f * scale, 6f * scale)
            lineTo(21f * scale, 6f * scale)
            moveTo(19f * scale, 6f * scale)
            lineTo(19f * scale, 20f * scale)
            cubicTo(19f * scale, 20.5304f * scale, 18.7893f * scale, 21.0391f * scale, 18.4142f * scale, 21.4142f * scale)
            cubicTo(18.0391f * scale, 21.7893f * scale, 17.5304f * scale, 22f * scale, 17f * scale, 22f * scale)
            lineTo(7f * scale, 22f * scale)
            cubicTo(6.46957f * scale, 22f * scale, 5.96086f * scale, 21.7893f * scale, 5.58579f * scale, 21.4142f * scale)
            cubicTo(5.21071f * scale, 21.0391f * scale, 5f * scale, 20.5304f * scale, 5f * scale, 20f * scale)
            lineTo(5f * scale, 6f * scale)
            moveTo(8f * scale, 6f * scale)
            lineTo(8f * scale, 4f * scale)
            cubicTo(8f * scale, 3.46957f * scale, 8.21071f * scale, 2.96086f * scale, 8.58579f * scale, 2.58579f * scale)
            cubicTo(8.96086f * scale, 2.21071f * scale, 9.46957f * scale, 2f * scale, 10f * scale, 2f * scale)
            lineTo(14f * scale, 2f * scale)
            cubicTo(14.5304f * scale, 2f * scale, 15.0391f * scale, 2.21071f * scale, 15.4142f * scale, 2.58579f * scale)
            cubicTo(15.7893f * scale, 2.96086f * scale, 16f * scale, 3.46957f * scale, 16f * scale, 4f * scale)
            lineTo(16f * scale, 6f * scale)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
