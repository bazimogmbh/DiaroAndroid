package com.pixelcrater.Diaro.folders;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import java.util.ArrayList;

public class FolderColorsAdapter extends ArrayAdapter<String> {

    private int folderColor;
    private ArrayList<Integer> colorsArrayList = new ArrayList<>();
    private LayoutInflater inflater;

    public FolderColorsAdapter(Activity activity) {
        super(activity, R.layout.ui_color);

        int[] folderColorsArray = activity.getResources().getIntArray(R.array.folder_colors);
        for (int i = 0; i < folderColorsArray.length; i++) {
            colorsArrayList.add(folderColorsArray[i]);
        }
        inflater = activity.getLayoutInflater();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView colorView;
        if (convertView == null) {
            colorView = (ImageView) inflater.inflate(R.layout.ui_color, parent, false);
        } else {
            colorView = (ImageView) convertView;
        }

        final int colorCodeInt = colorsArrayList.get(position);

       if (colorCodeInt == folderColor) {
            colorView.setImageResource(R.drawable.ic_ok_white_disabled_24dp);
        } else {
            colorView.setImageDrawable(null);
        }

        Drawable bgDrawable = colorView.getBackground();
        bgDrawable.setColorFilter(colorCodeInt, PorterDuff.Mode.SRC_ATOP);

        colorView.setOnClickListener(v -> {
            if (onColorSelectedListener != null) {
                String hexColor = MyThemesUtils.getHexColor(colorCodeInt);
                onColorSelectedListener.onColorSelected(hexColor);
            }
        });

        return colorView;
    }

    public void setFolderColor(String folderColor) {
        this.folderColor = Color.parseColor(folderColor);
    }

    @Override
    public int getCount() {
        return colorsArrayList.size();
    }

    private OnColorSelectedListener onColorSelectedListener;
    public void setOnColorSelectedListener(OnColorSelectedListener l) {
        onColorSelectedListener = l;
    }

    public interface OnColorSelectedListener {
        void onColorSelected(String colorCode);
    }
}
