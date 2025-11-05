package com.pixelcrater.Diaro.moods

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pixelcrater.Diaro.MyApp
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder
import com.pixelcrater.Diaro.model.MoodInfo
import com.pixelcrater.Diaro.storage.sqlite.SQLiteQueryHelper
import com.pixelcrater.Diaro.utils.MyThemesUtils
import com.pixelcrater.Diaro.utils.Static


class MoodSelectDialog(private var selectedMoodUid: String) : DialogFragment() {

    private var dialog: AlertDialog? = null
    private var builder: QustomDialogBuilder? = null
    private var moodsRV: RecyclerView? = null
    private val moodsAdapter: MoodsAdapter = MoodsAdapter()

    private lateinit var touchHelper: ItemTouchHelper

    // Item click listener
    private var onDialogItemClickListener: OnDialogItemClickListener? = null
    fun setOnDialogItemClickListener(l: OnDialogItemClickListener?) {
        onDialogItemClickListener = l
    }

    interface OnDialogItemClickListener {
        fun onDialogItemClick(moodInfo: MoodInfo?)
    }

    override fun onResume() {
        super.onResume()

        fetchData()
    }

    private fun fetchData() {

        val data: MutableList<MoodInfo> = SQLiteQueryHelper.getMoods() as MutableList<MoodInfo>

        moodsAdapter.setData(data)
        moodsAdapter.notifyDataSetChanged()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        builder = QustomDialogBuilder(activity)
        builder!!.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode())
        builder!!.setTitle(resources.getString(R.string.select_mood))

        // Set custom view
        builder!!.setCustomView(R.layout.moods_list)
        val customView = builder!!.customView

        // Cancel button
        builder!!.setNegativeButton(android.R.string.cancel, null)

        // Add new button
       /** builder!!.setAddNewButtonOnClick { v: View? ->
            startMoodAddEditActivity(
                null
            )
        }**/
        // Search button
        //   builder!!.showSearchButton()

        // Moods list
        moodsRV = customView.findViewById<View>(R.id.moods_list) as RecyclerView


        moodsAdapter.setSelectedMoodUid(selectedMoodUid)

        fetchData()

        moodsRV?.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = moodsAdapter
        }

        val callback: ItemTouchHelper.Callback = ItemMoveCallbackListener(moodsAdapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(moodsRV)

        val dividerItemDecoration = DividerItemDecoration(
            moodsRV?.context,
            LinearLayoutManager.VERTICAL
        )
        moodsRV?.addItemDecoration(dividerItemDecoration)

        moodsAdapter.setOnMoodPickListener(object : MoodsAdapter.OnMoodPickListener {
            override fun onMoodClicked(moodInfo: MoodInfo) {
                onDialogItemClickListener?.onDialogItemClick(moodInfo)
                dialog?.cancel()
            }

            override fun onMoodOverFlowClicked(view: View, moodInfo: MoodInfo) {
                showMoodPopupMenu(view, moodInfo.uid)
            }

        })

        moodsAdapter.setOnDragListener(object : MoodsAdapter.OnDragListener {
            override fun onStartDrag(holder: BaseViewHolder<Any>) {
                touchHelper.startDrag(holder)
            }

            override fun onDragged() {
                val data: MutableList<MoodInfo?> = moodsAdapter.getData()
                data.forEachIndexed { i, e ->
                    data[i]?.weight = i
                    MyApp.getInstance().storageMgr.sqLiteAdapter.updateMoodUserPositionField(data[i]?.uid, i)
                }

                moodsAdapter.notifyDataSetChanged()
                Log.e("on dragged", "finished")
            }
        })


        // TODO : update index of each entry on drag / delete events
        dialog = builder!!.create()
        return dialog!!
    }


    private fun startMoodAddEditActivity(uid: String?) {
        val intent = Intent(
            activity, MoodsAddEditActivity::class.java
        )
        intent.putExtra(Static.EXTRA_SKIP_SC, true)
        intent.putExtra("moodUid", uid)
        startActivityForResult(intent, Static.REQUEST_MOODS_ADDEDIT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Prevent dismiss on touch outside
        getDialog()!!.setCanceledOnTouchOutside(false)
        dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    private fun showMoodPopupMenu(v: View, uid: String) {
        val popupMenu = PopupMenu(requireActivity(), v)
        popupMenu.menuInflater.inflate(R.menu.popupmenu_folder, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.edit -> {
                    startMoodAddEditActivity(uid)
                    return@setOnMenuItemClickListener true
                }
                R.id.delete -> {
                    showMoodDeleteConfirmDialog(uid)
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popupMenu.show()
    }

    private fun showMoodDeleteConfirmDialog(folderUid: String) {
        val dialogTag = Static.DIALOG_CONFIRM_MOOD_DELETE
        if (childFragmentManager.findFragmentByTag(dialogTag) == null) {
            // Show dialog
            val dialog = ConfirmDialog()
            dialog.customString = folderUid
            dialog.setTitle(getString(R.string.delete))
            dialog.message = getString(R.string.folder_confirm_delete)
            dialog.show(childFragmentManager, dialogTag)

            // Set dialog listener
            setMoodDeleteConfirmDialogListener(dialog)
        }
    }


    private fun setMoodDeleteConfirmDialogListener(dialog: ConfirmDialog) {
        dialog.setDialogPositiveClickListener {
            if (!isAdded) {
                return@setDialogPositiveClickListener
            }

            // Delete mood in background
            MoodStatic.deleteMoodInBackground(dialog.customString)

            fetchData()
        }
    }

}


