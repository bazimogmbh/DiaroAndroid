package com.pixelcrater.Diaro.moods.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.ImageView
import androidx.core.widget.ImageViewCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.pixelcrater.Diaro.R

class MoodIconsAdapter(data: MutableList<MoodIconItem>) : BaseQuickAdapter<MoodIconItem, BaseViewHolder>(R.layout.item_mood_icons, data) {

    override fun convert(holder: BaseViewHolder, item: MoodIconItem) {
        holder.setImageResource(R.id.iv_icon, item.iconResID)

        if (item.isSelected) {
            ImageViewCompat.setImageTintList((holder.getView(R.id.iv_icon) as ImageView), ColorStateList.valueOf(item.color))
            holder.setBackgroundResource(R.id.iv_icon, R.drawable.shape_border)

            // (holder.getView(R.id.iv_icon) as ImageView).background.setColorFilter(item.selectedColor, android.graphics.PorterDuff.Mode.SRC_OVER);
            //  (holder.getView(R.id.iv_icon) as ImageView).setColorFilter(item.selectedColor, android.graphics.PorterDuff.Mode.SRC_ATOP);

            /**   (holder.getView(R.id.iv_icon) as ImageView).backgroundTintList = ColorStateList.valueOf(item.selectedColor) ColorStateList.valueOf(item.selectedColor) **/
        } else
        {
            ImageViewCompat.setImageTintList((holder.getView(R.id.iv_icon) as ImageView), ColorStateList.valueOf(Color.GRAY))
            holder.setBackgroundResource(R.id.iv_icon,0)
        }

    }

}

data class MoodIconItem(
    val iconIdentifier: String,
    val iconResID: Int,
    var color: Int,
    var isSelected: Boolean
)