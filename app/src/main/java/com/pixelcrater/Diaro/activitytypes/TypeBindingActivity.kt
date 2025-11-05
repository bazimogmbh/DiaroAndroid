package com.pixelcrater.Diaro.activitytypes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewbinding.ViewBinding
import com.pixelcrater.Diaro.main.ActivityState
import com.pixelcrater.Diaro.utils.MyThemesUtils
import java.util.Objects

abstract class TypeBindingActivity<viewBinding : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding : viewBinding
    var activityState: ActivityState? = null
    var toolbarLayout: View? = null

    abstract fun inflateLayout(layoutInflater: LayoutInflater) : viewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            setTheme(MyThemesUtils.getStyleResId())
        } catch (ignored: Exception) {
        }
        super.onCreate(savedInstanceState)

        binding = inflateLayout(layoutInflater)
        setContentView(binding.root)

        activityState = ActivityState(this@TypeBindingActivity, savedInstanceState)
        activityState!!.setLayoutBackground()
    }

    fun setToolbar(toolbar : Toolbar){
        setSupportActionBar(toolbar)
        activityState!!.setupActionBar(Objects.requireNonNull(supportActionBar))
    }

}