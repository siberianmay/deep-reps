package com.deepreps.core.ui.component

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * State holder for drag-to-reorder within a LazyColumn.
 *
 * Tracks the currently dragged item index, the cumulative drag offset,
 * and provides methods to initiate, update, and finalize drag operations.
 *
 * @param lazyListState The LazyListState of the target LazyColumn.
 * @param onMove Callback invoked when items should swap positions.
 */
class DragDropState(
    private val lazyListState: LazyListState,
    private val onMove: (fromIndex: Int, toIndex: Int) -> Unit,
) {
    /** Index of the item currently being dragged, or -1 if idle. */
    var draggedItemIndex by mutableIntStateOf(-1)
        private set

    /** Cumulative vertical drag offset in pixels. */
    var dragOffset by mutableFloatStateOf(0f)
        private set

    /** Whether a drag operation is actively in progress. */
    val isDragging: Boolean get() = draggedItemIndex >= 0

    /**
     * Begin a drag operation on the item at [index].
     */
    fun onDragStart(index: Int) {
        draggedItemIndex = index
        dragOffset = 0f
    }

    /**
     * Update the drag offset by [delta] pixels and check if a swap should occur.
     */
    fun onDrag(delta: Float) {
        if (!isDragging) return
        dragOffset += delta
        checkForSwap()
    }

    /**
     * End the current drag operation, resetting all state.
     */
    fun onDragEnd() {
        draggedItemIndex = -1
        dragOffset = 0f
    }

    /**
     * Cancel the current drag (identical to end for visual purposes).
     */
    fun onDragCancel() {
        onDragEnd()
    }

    private fun checkForSwap() {
        val draggedInfo = findItemInfo(draggedItemIndex) ?: return
        val draggedCenter = draggedInfo.offset + draggedInfo.size / 2 + dragOffset

        // Check if we should swap with the item above
        if (draggedItemIndex > 0) {
            val aboveInfo = findItemInfo(draggedItemIndex - 1)
            if (aboveInfo != null) {
                val aboveCenter = aboveInfo.offset + aboveInfo.size / 2
                if (draggedCenter < aboveCenter) {
                    val fromIndex = draggedItemIndex
                    val toIndex = draggedItemIndex - 1
                    // Adjust offset to account for the swap so the item stays under the finger
                    dragOffset += (draggedInfo.offset - aboveInfo.offset).toFloat()
                    draggedItemIndex = toIndex
                    onMove(fromIndex, toIndex)
                    return
                }
            }
        }

        // Check if we should swap with the item below
        val belowInfo = findItemInfo(draggedItemIndex + 1)
        if (belowInfo != null) {
            val belowCenter = belowInfo.offset + belowInfo.size / 2
            if (draggedCenter > belowCenter) {
                val fromIndex = draggedItemIndex
                val toIndex = draggedItemIndex + 1
                dragOffset += (draggedInfo.offset - belowInfo.offset).toFloat()
                draggedItemIndex = toIndex
                onMove(fromIndex, toIndex)
            }
        }
    }

    private fun findItemInfo(index: Int): LazyListItemInfo? {
        return lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
    }
}

/**
 * Remember a [DragDropState] scoped to composition.
 *
 * @param lazyListState The LazyListState of the target LazyColumn.
 * @param onMove Callback invoked when two items should swap positions.
 */
@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
): DragDropState {
    return remember(lazyListState) {
        DragDropState(
            lazyListState = lazyListState,
            onMove = onMove,
        )
    }
}
