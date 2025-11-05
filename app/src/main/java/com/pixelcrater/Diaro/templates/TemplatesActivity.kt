package com.pixelcrater.Diaro.templates

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.activitytypes.TypeActivity
import com.pixelcrater.Diaro.activitytypes.TypeBindingActivity
import com.pixelcrater.Diaro.analytics.AnalyticsConstants
import com.pixelcrater.Diaro.databinding.MainNoEntriesBinding
import com.pixelcrater.Diaro.databinding.TemplateAddEditBinding
import com.pixelcrater.Diaro.databinding.TemplatesBinding
import com.pixelcrater.Diaro.storage.sqlite.SQLiteQueryHelper
import com.pixelcrater.Diaro.utils.MyThemesUtils
import com.pixelcrater.Diaro.utils.Static
import java.util.ArrayList

class TemplatesActivity : TypeBindingActivity<TemplatesBinding>() {

    private var mAdapter: TemplatesAdapter? = null
    var entries: ArrayList<Template> =  ArrayList()
    override fun inflateLayout(layoutInflater: LayoutInflater)  = TemplatesBinding.inflate(layoutInflater)

    private lateinit var bindingEmpty : MainNoEntriesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setToolbar(binding.toolbar)

        bindingEmpty = MainNoEntriesBinding.inflate(layoutInflater)
        activityState!!.setActionBarTitle(supportActionBar, R.string.templates)

        binding.fab.visibility = View.VISIBLE
        binding.fab.backgroundTintList = ColorStateList.valueOf(MyThemesUtils.getAccentColor())
        binding.fab.rippleColor = MyThemesUtils.getDarkColor(MyThemesUtils.getAccentColorCode())

        // OnClickListener
        binding.fab.setOnClickListener { _: View? ->
            activityState!!.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_TEMPLATE_ADD_EDIT)

            val intent = Intent(this@TemplatesActivity, AddEditTemplateActivity::class.java)
            intent.putExtra(Static.EXTRA_SKIP_SC, true)
            startActivityForResult(intent, Static.REQUEST_VIEW_EDIT_ENTRY)
        }

        binding. recyclerView.layoutManager = LinearLayoutManager(this)
       // recyclerView.hasFixedSize()
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        mAdapter = TemplatesAdapter(this, entries)
        binding.recyclerView.adapter = mAdapter

        setupData()
    }

    public override fun onResume() {
        super.onResume()
        setupData()
    }

    private fun setupData() {
        entries.clear()
        entries.addAll(SQLiteQueryHelper.getTemplates())

        if (entries.size == 0) {
            bindingEmpty.noEntriesFound.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            bindingEmpty.noEntriesFound.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE

            val clickListener = TemplatesAdapter.OnItemClickListener { _, obj, _ ->
                val intent = Intent(this@TemplatesActivity, AddEditTemplateActivity::class.java)
                intent.putExtra(Static.EXTRA_SKIP_SC, true)
                intent.putExtra("template", obj)
                startActivityForResult(intent, Static.REQUEST_VIEW_EDIT_ENTRY)
            }

            mAdapter?.setOnItemClickListener(clickListener);
            mAdapter?.notifyDataSetChanged()
            binding.recyclerView.invalidate()
        }
    }


}