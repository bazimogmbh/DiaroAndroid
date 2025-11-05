package com.pixelcrater.Diaro.moods

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pixelcrater.Diaro.MyApp
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.activitytypes.TypeActivity
import com.pixelcrater.Diaro.model.MoodInfo
import com.pixelcrater.Diaro.moods.adapters.MoodColorItem
import com.pixelcrater.Diaro.moods.adapters.MoodColorsAdapter
import com.pixelcrater.Diaro.moods.adapters.MoodIconItem
import com.pixelcrater.Diaro.moods.adapters.MoodIconsAdapter
import com.pixelcrater.Diaro.storage.Tables
import com.pixelcrater.Diaro.storage.sqlite.SQLiteQueryHelper
import com.pixelcrater.Diaro.utils.AppLog
import com.pixelcrater.Diaro.utils.MyThemesUtils
import com.pixelcrater.Diaro.utils.Static
import com.sandstorm.moods.DefaultMoodAssets
import java.util.*


class MoodsAddEditActivity : TypeActivity() {

    private var moodUid: String? = null
    private var moodTitleEditText: TextInputEditText? = null
    private var folderTitleTextInputLayout: TextInputLayout? = null

    private var colorsRV: RecyclerView? = null
    lateinit var colorsAdapter: MoodColorsAdapter
    private var colorsData: ArrayList<MoodColorItem> = ArrayList()
    var colorsSelectedIndex = 0

    private var iconsRV: RecyclerView? = null
    lateinit var iconsAdapter: MoodIconsAdapter
    private var iconsData: ArrayList<MoodIconItem> = ArrayList()
    var iconsSelectedIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(addViewToContentContainer(R.layout.moods_add_edit))

        activityState.setLayoutBackground()

        // Get intent extras
        val extras = intent.extras
        moodUid = extras!!.getString("moodUid")

        // Activity title
        var titleResId = R.string.mood_add
        if (moodUid != null) {
            titleResId = R.string.mood_edit
        }

        activityState.setActionBarTitle(supportActionBar, titleResId)

        moodTitleEditText = findViewById<View>(R.id.mood_title) as TextInputEditText
        folderTitleTextInputLayout = findViewById<View>(R.id.mood_title_parent) as TextInputLayout

        // colors
        colorsRV = findViewById<View>(R.id.rv_colors) as RecyclerView
        val folderColorsArray: IntArray = resources.getIntArray(R.array.folder_colors)

        for (color in folderColorsArray) {
            colorsData.add(MoodColorItem(color, false))
        }

        colorsAdapter = MoodColorsAdapter(colorsData)
        colorsAdapter.setOnItemClickListener { _, _, position ->
            colorsSelectedIndex = position

            colorsData.forEachIndexed { index, item ->
                item.isSelected = index == position
            }
            colorsAdapter.notifyDataSetChanged()

            // change the icon color too
            iconsData[iconsSelectedIndex].color = colorsData[position].color
            iconsAdapter.notifyDataSetChanged()
        }

        colorsRV?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorsAdapter
        }

        // icons
        iconsRV = findViewById<View>(R.id.rv_icons) as RecyclerView
        for (defaultMoodAsset in DefaultMoodAssets.values()) {
            iconsData.add(
                MoodIconItem(defaultMoodAsset.identifier, defaultMoodAsset.iconRes, 0, false)
            )
        }
        iconsAdapter = MoodIconsAdapter(iconsData)
        iconsAdapter.setOnItemClickListener { _, _, position ->
            iconsSelectedIndex = position

            iconsData[position].color = colorsData[colorsSelectedIndex].color
            iconsData.forEachIndexed { index, item ->
                item.isSelected = index == position
            }
            iconsAdapter.notifyDataSetChanged()

        }
        iconsRV?.apply {
            layoutManager = GridLayoutManager(context, 6)
            adapter = iconsAdapter
        }

        if (moodUid != null) {
            val moodsCursor = MyApp.getInstance().storageMgr.sqLiteAdapter.getSingleMoodCursorByUid(moodUid)

            // If not found
            if (moodsCursor.count == 0) {
                moodsCursor.close()
                finish()
                return
            }

            val moodInfo = MoodInfo(moodsCursor)
            moodsCursor.close()

            // Title
            moodTitleEditText?.setText(moodInfo.title)
            // Color
            var colorFound = false
            colorsData.forEachIndexed { index, item ->
                if (item.color == Color.parseColor(moodInfo.color)) {
                    item.isSelected = true
                    colorFound = true
                }
            }

            if (!colorFound) colorsData.add(0, MoodColorItem(Color.parseColor(moodInfo.color), true))
            // Icon
            iconsData.forEachIndexed { index, item ->
                if (item.iconIdentifier == moodInfo.icon) {
                    item.isSelected = true
                    item.color = Color.parseColor(moodInfo.color)

                }
            }
            iconsAdapter.notifyDataSetChanged()


        } else {
            colorsAdapter.data[0].isSelected = true
            iconsAdapter.data[0].isSelected = true
            iconsAdapter.data[0].color = colorsAdapter.data[0].color
        }


        // Show keyboard
        Static.showSoftKeyboard(moodTitleEditText)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_mood_addedit, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (activityState.isActivityPaused) {
            true
        } else when (item.itemId) {
            android.R.id.home -> {
                Static.hideSoftKeyboard(moodTitleEditText)
                moodTitleEditText?.clearFocus()
                finish()
                true
            }
            R.id.item_save -> {
                Static.hideSoftKeyboard(moodTitleEditText)
                moodTitleEditText?.clearFocus()
                saveMood()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun saveMood() {
        val title: String = moodTitleEditText?.text.toString().trim { it <= ' ' }

        val sameMoodExists = MyApp.getInstance().storageMgr.sqLiteAdapter.findSameMood(moodUid, title) != null

        // Check title
        if (title == "") {
            // Show error
            folderTitleTextInputLayout!!.error = getString(R.string.error_title_empty)
        }   // Check if the same mood does not already exist
        else if (sameMoodExists) {
            // Show error
            folderTitleTextInputLayout!!.error = getString(R.string.error_title_exists)
        } else {

            val userOrder = SQLiteQueryHelper.getMoodsCount()

            val selectedColor = colorsData[colorsSelectedIndex]
            val selectedIcon = iconsData[iconsSelectedIndex]

            val cv = ContentValues()
            cv.put(Tables.KEY_MOOD_COLOR, MyThemesUtils.getHexColor(selectedColor.color))
            cv.put(Tables.KEY_MOOD_ICON, selectedIcon.iconIdentifier)
            cv.put(Tables.KEY_MOOD_TITLE, title)

            if (moodUid == null) {
                cv.put(Tables.KEY_MOOD_WEIGHT, userOrder + 1)
                // Generate uid
                cv.put(Tables.KEY_UID, Static.generateRandomUid())

                // Insert row
                val uid = MyApp.getInstance().storageMgr.insertRow(Tables.TABLE_MOODS, cv)
                AppLog.d("INSERTED uid: $uid")
                if (uid != null) {
                    moodUid = uid
                }
            } else if (cv.size() > 0) {
                // Update row
                MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_MOODS, moodUid, cv)
            }

            MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners()

            // Finish
            val i = Intent()
            setResult(RESULT_OK, i)
            finish()
        }

    }
}