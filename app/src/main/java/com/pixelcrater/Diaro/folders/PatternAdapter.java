package com.pixelcrater.Diaro.folders;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.Pattern;

import java.util.ArrayList;

public class PatternAdapter extends BaseAdapter {

    private Activity mActivity;
    private LayoutInflater inflater;
    private ArrayList<Pattern> mPatternsArrayList;
    private int mColor;

    public PatternAdapter(Activity activity, ArrayList<Pattern> patternsArrayList, int color) {
        mActivity = activity;
        mPatternsArrayList = patternsArrayList;
        mColor = color;

        inflater = activity.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return mPatternsArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mPatternsArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//		if (convertView == null) {
        convertView = inflater.inflate(R.layout.pattern_grid_item, parent, false);
//		}

        ViewGroup patternColor = (ViewGroup) convertView.findViewById(R.id.pattern_color);

        // Pattern
        ImageView patternView = (ImageView) patternColor.findViewById(R.id.pattern);

        Pattern o = (Pattern) getItem(position);

        Static.setBgColorWithAlpha(mColor, patternColor);

        if (position == 0 || position == 1) {
            patternView.setBackgroundColor(mActivity.getResources().getColor(o.patternRepeat));
        } else {
            patternView.setImageResource(o.patternThumb);
        }

        return convertView;
    }
}
