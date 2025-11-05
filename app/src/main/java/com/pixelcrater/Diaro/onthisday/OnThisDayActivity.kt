package com.pixelcrater.Diaro.onthisday

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pixelcrater.Diaro.MyApp
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.activitytypes.TypeActivity
import com.pixelcrater.Diaro.config.Prefs
import com.pixelcrater.Diaro.entries.viewedit.EntryViewEditActivity
import com.pixelcrater.Diaro.model.EntryInfo
import com.pixelcrater.Diaro.storage.sqlite.SQLiteQueryHelper
import com.pixelcrater.Diaro.utils.Static
import java.util.*

class OnThisDayActivity : TypeActivity() {

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: OnThisDayAdapter? = null
    var entries: ArrayList<EntryInfo> = ArrayList()
    var noEntriesFoundView: RelativeLayout? = null
    var mEntryCountView: TextView? = null
    var includeCurrentYear = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(addViewToContentContainer(R.layout.on_this_day))
        activityState.setLayoutBackground()
        activityState.setActionBarTitle(Objects.requireNonNull(supportActionBar), getString(R.string.on_this_day))

        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView!!.layoutManager = LinearLayoutManager(this)
        mRecyclerView!!.setHasFixedSize(false)
        noEntriesFoundView = findViewById(R.id.no_entries_found)
        mEntryCountView = findViewById(R.id.entryCountView)

        mAdapter = OnThisDayAdapter(Glide.with(this), this, entries)
        mAdapter!!.setOnItemClickListener { _: View?, obj: EntryInfo, _: Int ->
            val intent = Intent(this@OnThisDayActivity, EntryViewEditActivity::class.java)
            intent.putExtra(Static.EXTRA_SKIP_SC, true)
            intent.putExtra("entryUid", obj.uid)
            startActivityForResult(intent, Static.REQUEST_VIEW_EDIT_ENTRY)
        }
        mRecyclerView!!.adapter = mAdapter

        setupData()
    }

    private fun setupData() {
        includeCurrentYear = MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ON_THIS_DAY_INCLUDE_CURRENT_YEAR, false)

        entries.clear()
        entries.addAll(SQLiteQueryHelper.getEntriesByThisDay(includeCurrentYear))

        if (entries.size == 0) {
            noEntriesFoundView!!.visibility = View.VISIBLE
            mRecyclerView!!.visibility = View.GONE
        } else {
            noEntriesFoundView!!.visibility = View.GONE
            mRecyclerView!!.visibility = View.VISIBLE
            mEntryCountView!!.text = resources.getQuantityString(R.plurals.entry_on_this_day, entries.size, entries.size)

            mAdapter?.notifyDataSetChanged()
            mRecyclerView?.invalidate()
        }
    }

    public override fun onResume() {
        super.onResume()
        setupData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_onthisday, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val checkable = menu.findItem(R.id.item_currentYear)
        checkable.isCheckable = true
        checkable.isChecked = MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ON_THIS_DAY_INCLUDE_CURRENT_YEAR, false)
        return super.onPrepareOptionsMenu(menu)
    }

    private var isChecked = false
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (activityState.isActivityPaused) {
            true
        } else when (item.itemId) {
            R.id.item_currentYear -> {
                isChecked = !item.isChecked
                item.isChecked = isChecked
                if (isChecked) {
                    MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_ON_THIS_DAY_INCLUDE_CURRENT_YEAR, true).apply()
                } else {
                    MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_ON_THIS_DAY_INCLUDE_CURRENT_YEAR, false).apply()
                }
                setupData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}