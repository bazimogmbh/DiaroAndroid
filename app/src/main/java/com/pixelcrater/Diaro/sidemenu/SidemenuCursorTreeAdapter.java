package com.pixelcrater.Diaro.sidemenu;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.storage.Tables;

public class SidemenuCursorTreeAdapter extends MyCursorTreeAdapter {

    public static final int GROUP_FOLDERS = 0;
    public static final int GROUP_TAGS = 1;
    public static final int GROUP_LOCATIONS = 2;
    public static final int GROUP_MOODS = 3;

    public final SidemenuFolders sidemenuFolders;
    public final SidemenuTags sidemenuTags;
    public final SidemenuLocations sidemenuLocations;
    public final SidemenuMoods sidemenuMoods;

    private final LayoutInflater inflater;
    private final SidemenuFragment mSidemenuFragment;

    public SidemenuCursorTreeAdapter(Cursor cursor, Context context, SidemenuFragment sidemenuFragment) {
        super(cursor, context);
//        AppLog.d("cursor: " + cursor);

        inflater = ((Activity) context).getLayoutInflater();

        mSidemenuFragment = sidemenuFragment;

        sidemenuFolders = new SidemenuFolders();
        sidemenuTags = new SidemenuTags();
        sidemenuLocations = new SidemenuLocations();
        sidemenuMoods = new SidemenuMoods();
    }

    public void setGroupCursor(Cursor cursor) {
        super.setGroupCursor(cursor);
    }

    @Override
    public Cursor getGroup(int groupPosition) {
//        AppLog.d("groupPosition: " + groupPosition);
        return super.getGroup(groupPosition);
    }


    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
//        AppLog.d("groupPosition: " + groupPosition);
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
//        AppLog.d("groupPosition: " + groupPosition);

        if (mSidemenuFragment.isAdded()) {
            mSidemenuFragment.loaderManager.destroyLoader(groupPosition);
        }
    }

    @Override
    public void setChildrenCursor(int groupPosition, Cursor childrenCursor) {
//        AppLog.d("groupPosition: " + groupPosition + ", childrenCursor: " + childrenCursor);
        super.setChildrenCursor(groupPosition, childrenCursor);
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
//        AppLog.d("groupCursor.getPosition(): " + groupCursor.getPosition());
//                + ", getLoader(" + groupCursor.getPosition() + "): " + loaderManager.getLoader(groupCursor.getPosition()));

        if (mSidemenuFragment.isAdded()) {
            mSidemenuFragment.loaderManager.initLoader(groupCursor.getPosition(), null, mSidemenuFragment);
        }

        return null;
    }

    public String getChildItemUid(int groupPosition, int childPosition) {
        Cursor cursor = getChild(groupPosition, childPosition);
        return cursor.getString(cursor.getColumnIndex(Tables.KEY_UID));
    }

    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
        final View view = inflater.inflate(R.layout.sidemenu_list_group, parent, false);
        view.setTag(new GroupViewHolder(view));
        return view;
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, final boolean isExpanded) {
        final GroupViewHolder holder = (GroupViewHolder) view.getTag();
        holder.groupContainerViewGroup.setBackgroundColor(mSidemenuFragment.uiColor);
        holder.groupContainerViewGroup.setVisibility(View.VISIBLE);
        holder.addNewButton.setVisibility(View.VISIBLE);

        switch (cursor.getPosition()) {
            case GROUP_FOLDERS:
                bindFoldersGroup(holder, isExpanded);
                break;

            case GROUP_TAGS:
                bindTagsGroup(holder, isExpanded);
                break;

            case GROUP_LOCATIONS:
                bindLocationsGroup(holder, isExpanded);
                break;

            case GROUP_MOODS:
                bindMoodsGroup(holder, isExpanded);
                holder.addNewButton.setVisibility(View.INVISIBLE);
                break;
        }

        // Expanded/Collapsed icon
        updateExpandedCollapsedIcon(isExpanded, holder.titleTextView);

        // Group title
        holder.titleTextView.setText(cursor.getString(cursor.getColumnIndex("title")));
    }

    private void bindFoldersGroup(GroupViewHolder holder, final boolean isExpanded) {
//        AppLog.d("isExpanded: " + isExpanded);

        // Group icon
        if (sidemenuFolders.getSelectedFolderUid() != null) {
            holder.groupIcoButton.setEnabled(true);
            holder.groupIcoButton.setImageResource(R.drawable.ic_folder_clear_white_24dp);
        } else {
            holder.groupIcoButton.setEnabled(false);
            holder.groupIcoButton.setImageResource(R.drawable.ic_folder_white_24dp);
        }

        // OnClickListener
        holder.groupIcoButton.setOnClickListener(v -> {
            sidemenuFolders.clearActiveFolder();
            if (mSidemenuFragment.mListener != null) {
                mSidemenuFragment.mListener.onActiveFiltersChanged();
            }
        });

        // Title collapse/expand
        holder.titleTextView.setOnClickListener(v -> {
            MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_SIDEMENU_FOLDERS_OPEN, !isExpanded).apply();
            mSidemenuFragment.openCloseFolders();
        });

        // Add new button
        holder.addNewButton.setOnClickListener(v -> {
            if (mSidemenuFragment.mListener != null) {
                mSidemenuFragment.mListener.onShouldStartFolderAddEditActivity(null);
            }
        });

        // More button
        holder.moreButton.setOnClickListener(mSidemenuFragment::showFoldersHeaderPopupMenu);

        // OnOverflowItemClickListener
        sidemenuFolders.setOnOverflowItemClickListener(mSidemenuFragment::showFolderPopupMenu);
    }

    private void bindTagsGroup(GroupViewHolder holder, final boolean isExpanded) {
//        AppLog.d("isExpanded: " + isExpanded);

        // Group icon
        if (sidemenuTags.getSelectedTagsUidsArrayList().size() > 0) {
            holder.groupIcoButton.setEnabled(true);
            holder.groupIcoButton.setImageResource(R.drawable.ic_tags_clear_white_24dp);
        } else {
            holder.groupIcoButton.setEnabled(false);
            holder.groupIcoButton.setImageResource(R.drawable.ic_tag_white_24dp);
        }

        // OnClickListener
        holder.groupIcoButton.setOnClickListener(v -> {
            sidemenuTags.clearActiveTags();
            if (mSidemenuFragment.mListener != null) {
                mSidemenuFragment.mListener.onActiveFiltersChanged();
            }
        });

        // Title collapse/expand
        holder.titleTextView.setOnClickListener(v -> {
            MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_SIDEMENU_TAGS_OPEN, !isExpanded).apply();
            mSidemenuFragment.openCloseTags();
        });

        // Add new button
        holder.addNewButton.setOnClickListener(v -> {
            if (mSidemenuFragment.mListener != null) {
                mSidemenuFragment.mListener.onShouldStartTagAddEditActivity(null);
            }
        });

        // More button
        holder.moreButton.setOnClickListener(mSidemenuFragment::showTagsHeaderPopupMenu);

        // OnOverflowItemClickListener
        sidemenuTags.setOnOverflowItemClickListener(mSidemenuFragment::showTagPopupMenu);
    }

    private void bindLocationsGroup(GroupViewHolder holder, final boolean isExpanded) {
//        AppLog.d("isExpanded: " + isExpanded);

        // Group icon
        if (sidemenuLocations.getSelectedLocationsUidsArrayList().size() > 0) {
            holder.groupIcoButton.setEnabled(true);
            holder.groupIcoButton.setImageResource(R.drawable.ic_location_clear_white_24dp);
        } else {
            holder.groupIcoButton.setEnabled(false);
            holder.groupIcoButton.setImageResource(R.drawable.ic_place_white_24dp);
        }

        // OnClickListener
        holder.groupIcoButton.setOnClickListener(v -> {
            sidemenuLocations.clearActiveLocations();
            if (mSidemenuFragment.mListener != null) {
                mSidemenuFragment.mListener.onActiveFiltersChanged();
            }
        });

        // Title collapse/expand
        holder.titleTextView.setOnClickListener(v -> {
            MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_SIDEMENU_LOCATIONS_OPEN, !isExpanded).apply();
            mSidemenuFragment.openCloseLocations();
        });

        // Add new button
        holder.addNewButton.setOnClickListener(v -> {
            if (mSidemenuFragment.mListener != null) {
                mSidemenuFragment.mListener.onShouldStartLocationAddEditActivity(null);
            }
        });

        // More button
        holder.moreButton.setOnClickListener(mSidemenuFragment::showLocationsHeaderPopupMenu);

        // OnOverflowItemClickListener
        sidemenuLocations.setOnOverflowItemClickListener(mSidemenuFragment::showLocationPopupMenu);
    }

    private void bindMoodsGroup(GroupViewHolder holder, final boolean isExpanded) {

        // Group icon
        if (sidemenuMoods.getSelectedMoodUid() != null) {
            holder.groupIcoButton.setEnabled(true);
            holder.groupIcoButton.setImageResource(R.drawable.ic_mood_x);
        } else {
            holder.groupIcoButton.setEnabled(false);
            holder.groupIcoButton.setImageResource(R.drawable.ic_mood);
        }

        // OnClickListener
        holder.groupIcoButton.setOnClickListener(v -> {
            sidemenuMoods.clearActiveMood();
            if (mSidemenuFragment.mListener != null) {
                mSidemenuFragment.mListener.onActiveFiltersChanged();
            }
        });

        // Title collapse/expand
        holder.titleTextView.setOnClickListener(v -> {
            MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_SIDEMENU_MOODS_OPEN, !isExpanded).apply();
            mSidemenuFragment.openCloseMoods();
        });

        //   Add new button
        holder.addNewButton.setOnClickListener(v -> {
            if (mSidemenuFragment.mListener != null) {
                //mSidemenuFragment.mListener.onShouldStartMoodAddEditActivity(null);
            }
        });

        //  More button
       // holder.moreButton.setOnClickListener(mSidemenuFragment::showMoodsHeaderPopupMenu);

        // OnOverflowItemClickListener
      //  sidemenuMoods.setOnOverflowItemClickListener(mSidemenuFragment::showMoodsPopupMenu);

    }

    private void updateExpandedCollapsedIcon(boolean isExpanded, TextView titleTextView) {
        int resId = R.drawable.ic_keyboard_arrow_right_white_18dp;
        if (isExpanded) {
            resId = R.drawable.ic_keyboard_arrow_down_white_18dp;
        }
        titleTextView.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(MyApp.getInstance().getResources(), resId, null), null, null, null);
    }

    private int getGroupIdByCursor(Cursor cursor) {
        // Get group id by checking cursor fields

        if (cursor.getColumnIndex(Tables.KEY_FOLDER_PATTERN) != -1) {
            return GROUP_FOLDERS;
        } else if (cursor.getColumnIndex(Tables.KEY_LOCATION_ADDRESS) != -1) {
            return GROUP_LOCATIONS;
        } else if (cursor.getColumnIndex(Tables.KEY_MOOD_ICON) != -1) {
            return GROUP_MOODS;
        }

        return GROUP_TAGS;
    }

    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
//        AppLog.d("cursor.getPosition(): " + cursor.getPosition() + ", isLastChild: " + isLastChild);

        final View view = inflater.inflate(R.layout.sidemenu_list_child, parent, false);
        view.setTag(new ChildViewHolder(view));
        return view;
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
//        AppLog.d("");

        int groupId = getGroupIdByCursor(cursor);
//        AppLog.d("groupId: " + groupId);

        switch (groupId) {
            case GROUP_FOLDERS:
                sidemenuFolders.bindView((ChildViewHolder) view.getTag(), cursor);
                break;

            case GROUP_TAGS:
                sidemenuTags.bindView((ChildViewHolder) view.getTag(), cursor);
                break;

            case GROUP_LOCATIONS:
                sidemenuLocations.bindView((ChildViewHolder) view.getTag(), cursor);
                break;

            case GROUP_MOODS:
                sidemenuMoods.bindView((ChildViewHolder) view.getTag(), cursor);
                break;
        }
    }

    static class GroupViewHolder {
        final ViewGroup groupContainerViewGroup;
        final ImageButton groupIcoButton;
        final TextView titleTextView;
        final ImageButton addNewButton;
        final ImageButton moreButton;

        GroupViewHolder(View view) {
            groupContainerViewGroup = (ViewGroup) view.findViewById(R.id.group_container);
            groupIcoButton = (ImageButton) view.findViewById(R.id.group_ico);
            titleTextView = (TextView) view.findViewById(R.id.group_title);
            addNewButton = (ImageButton) view.findViewById(R.id.add_new);
            moreButton = (ImageButton) view.findViewById(R.id.more);
        }
    }

    static class ChildViewHolder {
        final View colorView;
        final ImageView iconView;
        final CheckBox checkboxView;
        final TextView titleTextView;
        final TextView countTextView;
        final ImageView overflowView;

        ChildViewHolder(View view) {
            colorView = view.findViewById(R.id.color);
            iconView = (ImageView) view.findViewById(R.id.icon);
            checkboxView = (CheckBox) view.findViewById(R.id.checkbox);
            titleTextView = (TextView) view.findViewById(R.id.title);
            countTextView = (TextView) view.findViewById(R.id.count);
            overflowView = (ImageView) view.findViewById(R.id.overflow);
        }
    }
}
