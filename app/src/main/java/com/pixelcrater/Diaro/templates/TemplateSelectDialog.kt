package com.pixelcrater.Diaro.templates

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder
import com.pixelcrater.Diaro.storage.sqlite.SQLiteQueryHelper
import com.pixelcrater.Diaro.utils.MyThemesUtils
import com.pixelcrater.Diaro.utils.Static
import java.util.*

class TemplateSelectDialog : DialogFragment() {

    private var dialog: AlertDialog? = null
    var items: ArrayList<Template> = SQLiteQueryHelper.getTemplates()
    var templateItemAdapter: TemplateSelectAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Use the Builder class for convenient dialog construction
        val builder = QustomDialogBuilder(activity)

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode())

        // Title
        builder.setTitle(requireActivity().resources.getString(R.string.select_template))

        // Set custom view
        builder.setCustomView(R.layout.templates_list)
        val customView = builder.customView

        // Cancel button
        builder.setNegativeButton(android.R.string.cancel, null)

        // Add new button
        builder.setAddNewButtonOnClick { _: View? -> startAddEditTemplateActivity() }

        // Folders list
        val templatesListView = customView.findViewById<View>(R.id.templates_list) as ListView

        templateItemAdapter  = TemplateSelectAdapter(activity, items)
        templatesListView.adapter = templateItemAdapter

        // OnItemClickListener
        templatesListView.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            if (onDialogItemClickListener != null) {
                onDialogItemClickListener!!.onDialogItemClick(templateItemAdapter!!.getItem(position))
            }
            dialog!!.dismiss()
        }
        dialog = builder.create()
        return dialog!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Prevent dismiss on touch outside
        getDialog()!!.setCanceledOnTouchOutside(false)
        dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    private fun startAddEditTemplateActivity() {
        val intent = Intent(activity, AddEditTemplateActivity::class.java)
        intent.putExtra(Static.EXTRA_SKIP_SC, true)
        startActivityForResult(intent, Static.REQUEST_VIEW_EDIT_ENTRY)
    }

    // Item click listener
    private var onDialogItemClickListener: OnDialogItemClickListener? = null
    fun setOnDialogItemClickListener(l: OnDialogItemClickListener?) {
        onDialogItemClickListener = l
    }

    interface OnDialogItemClickListener {
        fun onDialogItemClick(template: Template?)
    }

    override fun onResume() {
        super.onResume()

        items.clear()
        items.addAll(SQLiteQueryHelper.getTemplates())
        templateItemAdapter?.notifyDataSetChanged()
    }
}