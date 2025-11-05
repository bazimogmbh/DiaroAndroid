package com.pixelcrater.Diaro.moods

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemMoveCallbackListener(val adapter: Listener) : ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> {
                if (viewHolder is BaseViewHolder<*>) {
                    adapter.onRowSelected(viewHolder as BaseViewHolder<Any>)
                }
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    @Suppress("UNCHECKED_CAST")
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder is BaseViewHolder<*>) {
            adapter.onRowClear(viewHolder as BaseViewHolder<Any>)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    interface Listener {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(itemViewHolder: BaseViewHolder<Any>)
        fun onRowClear(itemViewHolder: BaseViewHolder<Any>)
    }
}
