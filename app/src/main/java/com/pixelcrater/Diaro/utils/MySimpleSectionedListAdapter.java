package com.pixelcrater.Diaro.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pixelcrater.Diaro.R;

import dev.dworks.libs.astickyheader.SimpleSectionedListAdapter;

public class MySimpleSectionedListAdapter extends SimpleSectionedListAdapter {

    public MySimpleSectionedListAdapter(Context context, BaseAdapter baseAdapter, int sectionResourceId, int headerTextViewResId) {
        super(context, baseAdapter, sectionResourceId, headerTextViewResId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        try {
            view = super.getView(position, convertView, parent);
            TextView headerView = (TextView) view.findViewById(R.id.header);
            if (headerView != null) {
                // Set header background by theme
                headerView.setBackgroundResource(MyThemesUtils.getBackgroundColorResIdMainView());
           //     headerView.setTextColor(headerColor);
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }

        return view;
    }
}
