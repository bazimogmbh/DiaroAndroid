package com.pixelcrater.Diaro.moods


import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.model.MoodInfo
import com.pixelcrater.Diaro.utils.MyThemesUtils
import com.sandstorm.moods.DefaultMoodAssets
import java.util.*

class MoodsAdapter : RecyclerView.Adapter<BaseViewHolder<MoodInfo>>(),
    ItemMoveCallbackListener.Listener {

    private val listItemTextColor: Int
    private val listSelectedItemTextColor: Int
    private var selectedMoodUid: String

    private var onDragListener: OnDragListener? = null
    private var onMoodClickListener: OnMoodPickListener? = null

    private var data = emptyList<MoodInfo?>().toMutableList()

    init {
        listItemTextColor = MyThemesUtils.getListItemTextColor()
        listSelectedItemTextColor = MyThemesUtils.getSelectedListItemTextColor()
        selectedMoodUid = ""
    }


    fun setSelectedMoodUid(moodUid: String) {
        selectedMoodUid = moodUid
    }

    fun setOnMoodPickListener(oapl: OnMoodPickListener) {
        onMoodClickListener = oapl
    }

    fun setOnDragListener(l: OnDragListener) {
        onDragListener = l
    }

    fun setData(moodsList: List<MoodInfo>) {
        data.clear()
        data.addAll(moodsList)
        notifyDataSetChanged()
    }

    public fun getData(): MutableList<MoodInfo?> {
        return data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<MoodInfo> {
        return MoodViewHolder(
            LayoutInflater.from(parent.context), parent
        )
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_DEFAULT
    }

    override fun onBindViewHolder(holder: BaseViewHolder<MoodInfo>, position: Int) {
        data[position]?.apply {
            holder.onBind(this)
        }
    }

    override fun getItemCount() = data.size

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                swap(data, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                swap(data, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(itemViewHolder: BaseViewHolder<Any>) {
    }

    override fun onRowClear(itemViewHolder: BaseViewHolder<Any>) {
        onDragListener?.onDragged()
    }

    private fun swap(list: MutableList<MoodInfo?>, i: Int, j: Int) {
        list[i] = list.set(j, list[i])
    }

    @Suppress("UNCHECKED_CAST")
    inner class MoodViewHolder(inflater: LayoutInflater, parent: ViewGroup) : BaseViewHolder<MoodInfo>(
        inflater.inflate(R.layout.mood_list_item, parent, false)
    ) {
        private val mRoot = itemView.findViewById<LinearLayout>(R.id.ll_root)
        private val ivDrag = itemView.findViewById<ImageView>(R.id.iv_drag)
        private val tvTitle = itemView.findViewById<TextView>(R.id.tv_title)
        private val ivIcon = itemView.findViewById<ImageView>(R.id.iv_icon)
        private val ivOverflow = itemView.findViewById<ImageView>(R.id.iv_overflow)

        @SuppressLint("ClickableViewAccessibility")
        override fun onBind(data: MoodInfo) {
            tvTitle.text = data.title

            //+ ", " + data.weight

            if (data.uid == selectedMoodUid) {
                tvTitle.setTextColor(listSelectedItemTextColor)
            } else
                tvTitle.setTextColor(listItemTextColor)

            mRoot.setOnClickListener {
                onMoodClickListener?.onMoodClicked(data)
            }
            val moodAsset = DefaultMoodAssets.getByIconIdentifier(data.icon)
            try {
                if (moodAsset != null) {
                    ivIcon.setImageResource(moodAsset.iconRes)

                    val colorStateList = ColorStateList.valueOf(Color.parseColor(data.color))
                    ImageViewCompat.setImageTintList(ivIcon, colorStateList)
                }

            } catch (e: Exception) {
            }

            itemView.setOnLongClickListener {
                onDragListener?.onStartDrag(this as BaseViewHolder<Any>)
                return@setOnLongClickListener true
            }
            ivDrag.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    onDragListener?.onStartDrag(this as BaseViewHolder<Any>)
                }
                return@setOnTouchListener false
            }

         /**   if (data.uid == "") {
                ivOverflow.visibility = View.INVISIBLE
                ivDrag.visibility = View.GONE
            } else {
                ivOverflow.visibility = View.VISIBLE
                ivOverflow.setOnClickListener {
                    onMoodClickListener?.onMoodOverFlowClicked(it, data)
                }

                ivDrag.visibility = View.VISIBLE
            } **/

            ivDrag.visibility = View.GONE
            ivOverflow.visibility = View.INVISIBLE
        }

    }

    companion object {
        const val VIEW_TYPE_DEFAULT = 1
        const val VIEW_TYPE_DIVIDER = 3
    }

    interface OnMoodPickListener {
        fun onMoodClicked(moodInfo: MoodInfo) {}
        fun onMoodOverFlowClicked(view: View, moodInfo: MoodInfo) {}
    }

    interface OnDragListener {
        fun onStartDrag(holder: BaseViewHolder<Any>) {}
        fun onDragged() {}
    }
}