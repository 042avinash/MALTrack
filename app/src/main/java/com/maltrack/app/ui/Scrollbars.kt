package com.maltrack.app.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun Modifier.verticalScrollbar(state: LazyListState): Modifier {
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    val style = rememberScrollbarStyle()
    return drawWithContent {
        drawContent()
        val layout = state.layoutInfo
        val totalItems = layout.totalItemsCount
        val visibleItems = layout.visibleItemsInfo
        if ((!state.canScrollForward && !state.canScrollBackward) || visibleItems.isEmpty()) return@drawWithContent

        val viewportHeight = size.height
        val firstItem = visibleItems.first()
        val itemHeight = firstItem.size.coerceAtLeast(1).toFloat()
        val progress = (state.firstVisibleItemIndex + state.firstVisibleItemScrollOffset / itemHeight) /
            (totalItems - 1).coerceAtLeast(1).toFloat()
        drawScrollbarThumb(
            progress = progress,
            viewportStart = 0f,
            viewportHeight = viewportHeight,
            color = color.copy(alpha = style.alpha),
            thickness = style.thickness.toPx()
        )
    }.scrollbarPointerInput(state, style.isInteracting) { progress ->
        val lastItem = (state.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
        state.scrollToItem((progress * lastItem).roundToInt())
    }
}

@Composable
fun Modifier.verticalScrollbar(state: LazyGridState): Modifier {
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    val style = rememberScrollbarStyle()
    return drawWithContent {
        drawContent()
        val layout = state.layoutInfo
        val totalItems = layout.totalItemsCount
        val visibleItems = layout.visibleItemsInfo
        if ((!state.canScrollForward && !state.canScrollBackward) || visibleItems.isEmpty()) return@drawWithContent

        val viewportHeight = size.height
        val firstItem = visibleItems.first()
        val itemHeight = firstItem.size.height.coerceAtLeast(1).toFloat()
        val progress = (state.firstVisibleItemIndex + state.firstVisibleItemScrollOffset / itemHeight) /
            (totalItems - visibleItems.size).coerceAtLeast(1).toFloat()
        drawScrollbarThumb(
            progress = progress,
            viewportStart = 0f,
            viewportHeight = viewportHeight,
            color = color.copy(alpha = style.alpha),
            thickness = style.thickness.toPx()
        )
    }.scrollbarPointerInput(state, style.isInteracting) { progress ->
        val lastItem = (state.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
        state.scrollToItem((progress * lastItem).roundToInt())
    }
}

@Composable
fun Modifier.verticalScrollbar(state: ScrollState): Modifier {
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    val style = rememberScrollbarStyle()
    return drawWithContent {
        drawContent()
        val viewportHeight = size.height
        val maxScroll = state.maxValue
        if (maxScroll <= 0 || viewportHeight <= 0f) return@drawWithContent
        drawScrollbarThumb(
            progress = state.value.toFloat() / maxScroll,
            viewportStart = 0f,
            viewportHeight = viewportHeight,
            color = color.copy(alpha = style.alpha),
            thickness = style.thickness.toPx()
        )
    }.scrollbarPointerInput(state, style.isInteracting) { progress ->
        state.scrollTo((progress * state.maxValue).roundToInt())
    }
}

@Composable
fun Modifier.horizontalScrollbar(state: LazyListState): Modifier {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    val style = rememberScrollbarStyle()
    return drawWithContent {
        drawContent()
        val layout = state.layoutInfo
        val totalItems = layout.totalItemsCount
        val visibleItems = layout.visibleItemsInfo
        if ((!state.canScrollForward && !state.canScrollBackward) || visibleItems.isEmpty()) return@drawWithContent

        val firstItem = visibleItems.first()
        val itemWidth = firstItem.size.coerceAtLeast(1).toFloat()
        val progress = (state.firstVisibleItemIndex + state.firstVisibleItemScrollOffset / itemWidth) /
            (totalItems - 1).coerceAtLeast(1).toFloat()
        drawHorizontalScrollbarThumb(
            progress = progress,
            color = color.copy(alpha = style.alpha),
            thickness = style.thickness.toPx()
        )
    }.horizontalScrollbarPointerInput(state, style.isInteracting) { progress ->
        val lastItem = (state.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
        state.scrollToItem((progress * lastItem).roundToInt())
    }
}

private data class ScrollbarStyle(
    val isInteracting: MutableState<Boolean>,
    val thickness: Dp,
    val alpha: Float
)

@Composable
private fun rememberScrollbarStyle(): ScrollbarStyle {
    val isInteracting = remember { mutableStateOf(false) }
    val thickness by animateDpAsState(
        targetValue = if (isInteracting.value) 6.dp else 3.dp,
        animationSpec = tween(durationMillis = 140),
        label = "scrollbarThickness"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isInteracting.value) 0.95f else 0.55f,
        animationSpec = tween(durationMillis = 140),
        label = "scrollbarAlpha"
    )
    return ScrollbarStyle(isInteracting, thickness, alpha)
}

/** Keep the gesture target to the narrow scrollbar lane so content remains normally clickable. */
@Composable
private fun Modifier.scrollbarPointerInput(
    key: Any,
    isInteracting: MutableState<Boolean>,
    onSeek: suspend (Float) -> Unit
): Modifier {
    val currentOnSeek = rememberUpdatedState(onSeek)
    return pointerInput(key, isInteracting) {
        val laneWidth = 24.dp.toPx()
        val touchSlop = viewConfiguration.touchSlop
        coroutineScope {
            val seekScope = this
            awaitEachGesture {
                val down = awaitFirstDown(
                    requireUnconsumed = false,
                    pass = PointerEventPass.Initial
                )
                if (down.position.x < size.width - laneWidth) return@awaitEachGesture

                var change = down
                var verticalDragStarted = false
                while (change.pressed) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    change = event.changes.firstOrNull { it.id == down.id } ?: break
                    val movement = change.position - down.position
                    if (kotlin.math.abs(movement.x) > touchSlop &&
                        kotlin.math.abs(movement.x) > kotlin.math.abs(movement.y)
                    ) {
                        return@awaitEachGesture
                    }
                    if (kotlin.math.abs(movement.y) > touchSlop &&
                        kotlin.math.abs(movement.y) > kotlin.math.abs(movement.x)
                    ) {
                        change.consume()
                        verticalDragStarted = true
                        break
                    }
                }
                if (!verticalDragStarted) return@awaitEachGesture

                isInteracting.value = true
                try {
                    seekScope.launch {
                        currentOnSeek.value((change.position.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f))
                    }
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.position != change.previousPosition) {
                            change.consume()
                            val progress = (change.position.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
                            seekScope.launch { currentOnSeek.value(progress) }
                        }
                    } while (event.changes.any { it.pressed })
                } finally {
                    isInteracting.value = false
                }
            }
        }
    }
}

@Composable
private fun Modifier.horizontalScrollbarPointerInput(
    key: Any,
    isInteracting: MutableState<Boolean>,
    onSeek: suspend (Float) -> Unit
): Modifier {
    val currentOnSeek = rememberUpdatedState(onSeek)
    return pointerInput(key, isInteracting) {
        val laneHeight = 24.dp.toPx()
        val edgeGestureWidth = 24.dp.toPx()
        val touchSlop = viewConfiguration.touchSlop
        coroutineScope {
            val seekScope = this
            awaitEachGesture {
                val down = awaitFirstDown(
                    requireUnconsumed = false,
                    pass = PointerEventPass.Initial
                )
                if (down.position.y < size.height - laneHeight ||
                    down.position.x < edgeGestureWidth ||
                    down.position.x > size.width - edgeGestureWidth
                ) {
                    return@awaitEachGesture
                }

                var change = down
                var horizontalDragStarted = false
                while (change.pressed) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    change = event.changes.firstOrNull { it.id == down.id } ?: break
                    val movement = change.position - down.position
                    if (kotlin.math.abs(movement.y) > touchSlop &&
                        kotlin.math.abs(movement.y) > kotlin.math.abs(movement.x)
                    ) {
                        return@awaitEachGesture
                    }
                    if (kotlin.math.abs(movement.x) > touchSlop &&
                        kotlin.math.abs(movement.x) > kotlin.math.abs(movement.y)
                    ) {
                        change.consume()
                        horizontalDragStarted = true
                        break
                    }
                }
                if (!horizontalDragStarted) return@awaitEachGesture

                isInteracting.value = true
                try {
                    seekScope.launch {
                        currentOnSeek.value((change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f))
                    }
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.position != change.previousPosition) {
                            change.consume()
                            val progress = (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                            seekScope.launch { currentOnSeek.value(progress) }
                        }
                    } while (event.changes.any { it.pressed })
                } finally {
                    isInteracting.value = false
                }
            }
        }
    }
}

private fun DrawScope.drawScrollbarThumb(
    progress: Float,
    viewportStart: Float,
    viewportHeight: Float,
    color: Color,
    thickness: Float
) {
    if (viewportHeight <= 0f) return
    val thumbHeight = 36.dp.toPx().coerceAtMost(viewportHeight)
    val top = viewportStart + (viewportHeight - thumbHeight) * progress.coerceIn(0f, 1f)
    drawRoundRect(
        color = color,
        topLeft = Offset(size.width - thickness - 2.dp.toPx(), top),
        size = Size(thickness, thumbHeight),
        cornerRadius = CornerRadius(thickness / 2f)
    )
}

private fun DrawScope.drawHorizontalScrollbarThumb(
    progress: Float,
    color: Color,
    thickness: Float
) {
    if (size.width <= 0f || size.height <= 0f) return
    val thumbWidth = 36.dp.toPx().coerceAtMost(size.width)
    val sidePadding = 2.dp.toPx()
    val trackWidth = (size.width - sidePadding * 2).coerceAtLeast(0f)
    val thumbLeft = sidePadding + (trackWidth - thumbWidth).coerceAtLeast(0f) * progress.coerceIn(0f, 1f)
    val top = (size.height - thickness) / 2f
    drawRoundRect(
        color = color.copy(alpha = color.alpha * 0.25f),
        topLeft = Offset(sidePadding, (size.height - 1.dp.toPx()) / 2f),
        size = Size(trackWidth, 1.dp.toPx()),
        cornerRadius = CornerRadius(0.5.dp.toPx())
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(thumbLeft, top),
        size = Size(thumbWidth, thickness),
        cornerRadius = CornerRadius(thickness / 2f)
    )
}
