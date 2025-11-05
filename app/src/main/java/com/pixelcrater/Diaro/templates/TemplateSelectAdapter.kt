package com.pixelcrater.Diaro.templates

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.pixelcrater.Diaro.R
import java.util.*

class TemplateSelectAdapter(context: Context?, var mTemplates: List<Template>) : ArrayAdapter<Template?>(context!!, 0, mTemplates) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val template = mTemplates[position]
        val viewHolder: TemplateViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.templates_list_item, parent, false)
            viewHolder = TemplateViewHolder()
            viewHolder.titleTextView = convertView.findViewById(R.id.template_title)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as TemplateViewHolder
        }
        viewHolder.titleTextView!!.text = template.name
        return convertView!!
    }

    internal class TemplateViewHolder {
        var titleTextView: TextView? = null
    }

}