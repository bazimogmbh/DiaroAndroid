package com.pixelcrater.Diaro.moods.adapters


import android.content.res.ColorStateList
import android.widget.ImageView
import androidx.core.widget.ImageViewCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.pixelcrater.Diaro.R

class MoodColorsAdapter(data: MutableList<MoodColorItem>) : BaseQuickAdapter<MoodColorItem, BaseViewHolder>(com.sandstorm.diary.moods.R.layout.item_mood_colors, data) {

    override fun convert(holder: BaseViewHolder, item: MoodColorItem) {
        ImageViewCompat.setImageTintList((holder.getView(R.id.iv_icon) as ImageView), ColorStateList.valueOf(item.color))
        holder.setVisible(com.sandstorm.diary.moods.R.id.iv_check, item.isSelected)
    }
}

data class MoodColorItem(val color: Int, var isSelected: Boolean)