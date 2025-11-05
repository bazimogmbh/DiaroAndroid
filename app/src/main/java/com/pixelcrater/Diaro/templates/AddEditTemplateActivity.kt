package com.pixelcrater.Diaro.templates

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.pixelcrater.Diaro.MyApp
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.activitytypes.TypeActivity
import com.pixelcrater.Diaro.activitytypes.TypeBindingActivity
import com.pixelcrater.Diaro.analytics.AnalyticsConstants
import com.pixelcrater.Diaro.config.Prefs
import com.pixelcrater.Diaro.databinding.TemplateAddEditBinding
import com.pixelcrater.Diaro.databinding.TextRecognizerBinding
import com.pixelcrater.Diaro.model.PersistanceHelper
import com.pixelcrater.Diaro.settings.PreferencesHelper
import com.pixelcrater.Diaro.utils.MyThemesUtils
import org.apache.commons.lang3.StringUtils

class AddEditTemplateActivity : TypeBindingActivity<TemplateAddEditBinding>() {

    var uid: String = "";
    var item: Template? = null
    override fun inflateLayout(layoutInflater: LayoutInflater)  = TemplateAddEditBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setToolbar(binding.toolbar)

        this.item = intent.getParcelableExtra("template")

        if (item != null) {
            activityState!!.setActionBarTitle(supportActionBar, R.string.edit_template)
            uid = item!!.uid
            binding.templateName.setText(item!!.name)
            binding.templateTitle.setText(item!!.title)
            binding.templateText.setText(item!!.text)
        } else
            activityState!!.setActionBarTitle(supportActionBar, R.string.add_template)

        if (!PreferencesHelper.isTitleEnabled()) {
            binding.templateTitle.visibility = View.GONE
        }

        binding.fab.visibility = View.VISIBLE
        binding.fab.backgroundTintList = ColorStateList.valueOf(MyThemesUtils.getAccentColor())
        binding.fab.rippleColor = MyThemesUtils.getDarkColor(MyThemesUtils.getAccentColorCode())

        binding.fab.setOnClickListener {
            saveTemplate();
        }

        setEntryTextSize()
    }

    private fun setEntryTextSize() {
        var textSize = 16
        val textSizeFromPrefs = MyApp.getInstance().prefs.getInt(Prefs.PREF_TEXT_SIZE, Prefs.SIZE_NORMAL)
        if (textSizeFromPrefs == Prefs.SIZE_SMALL) {
            textSize -= 2
        } else if (textSizeFromPrefs == Prefs.SIZE_LARGE) {
            textSize += 2
        } else if (textSizeFromPrefs == Prefs.SIZE_X_LARGE) {
            textSize += 4
        }
        binding.templateName.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
        binding.templateTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
        binding.templateText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        saveTemplate();
    }

    private fun saveTemplate() {
        if (binding.templateName.text.isEmpty()) {

            if (StringUtils.isEmpty(uid) && binding.templateTitle.text.toString().isEmpty() && binding.templateText.text.toString().isEmpty())
                finish();
            else {
                Toast.makeText(applicationContext, "Template name can not be empty!", Toast.LENGTH_LONG).show()
                return
            }

        }

        if (StringUtils.isEmpty(uid)) {
            if (binding.templateName.text.isNullOrEmpty() && binding.templateTitle.text.isNullOrEmpty() && binding.templateText.text.isNullOrEmpty())
                finish()
            else {
                val template = Template(uid, binding.templateName.text.toString(), binding.templateTitle.text.toString(), binding.templateText.text.toString())
                PersistanceHelper.addTemplate(template)
                MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners()
                Toast.makeText(applicationContext, "Template saved!", Toast.LENGTH_SHORT).show()

                activityState!!.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_TEMPLATE_CREATE)
            }


        } else {
            // check if values actually changed?
            if ((binding.templateName.text.toString().compareTo(item!!.name) == 0) && (binding.templateTitle.text.toString().compareTo(item!!.title) == 0) && (binding.templateText.text.toString().compareTo(item!!.text) == 0)) {
                //    Toast.makeText(applicationContext, "Nothing changed!", Toast.LENGTH_LONG).show()

            } else {
                val template = Template(uid, binding.templateName.text.toString(), binding.templateTitle.text.toString(), binding.templateText.text.toString())
                PersistanceHelper.updateTemplate(template)
                MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners()
                Toast.makeText(applicationContext, "Template updated!", Toast.LENGTH_SHORT).show()

                activityState!!.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_TEMPLATE_UPDATE)
            }
        }

        finish();
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (StringUtils.isEmpty(uid))
            menu.findItem(R.id.item_delete).isVisible = false

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_template_add_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                saveTemplate()
                true;
            }
            R.id.item_delete -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.delete)
                builder.setMessage(R.string.confirm_delete)
                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                    PersistanceHelper.deleteTemplate(uid);
                    MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners()
                    finish();
                }
                builder.setNegativeButton(android.R.string.no) { dialog, which -> }
                builder.show()
                true
            }
            R.id.item_save -> {
                saveTemplate()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
