package com.pixelcrater.Diaro.settings;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.ColorPickerPopupCustom;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import java.util.ArrayList;

public class UiColorSelectDialog extends DialogFragment {
    // State vars
    private static final String UI_COLOR_CODE_STATE_KEY = "UI_COLOR_CODE_STATE_KEY";

    private String uiColorCode;

    private ArrayList<String> colorsArrayList = new ArrayList<>();

    // Dialog listener
    private OnUiColorSelectedListener onUiColorSelectedListener;

    public void setOnUiColorSelectedListener(OnUiColorSelectedListener l) {
        onUiColorSelectedListener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            uiColorCode = savedInstanceState.getString(UI_COLOR_CODE_STATE_KEY);
        }

        initColorsList();

        // Use the Builder class for convenient dialog construction
        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        builder.setTitle(getString(R.string.settings_ui_color));

        // Set custom view
        builder.setCustomView(R.layout.ui_color_select_dialog);
        View customView = builder.getCustomView();

        builder.changeAddNewDrawable(R.drawable.ic_palette_24dp);
        builder.setAddNewButtonOnClick(v -> new ColorPickerPopupCustom.Builder(getActivity())
                .initialColor(Color.RED)
                .enableBrightness(true) // Enable brightness slider or not
                .enableAlpha(false) // Enable alpha slider or not
                .okTitle(getString(android.R.string.ok))
                .cancelTitle(getString(android.R.string.cancel))
                .showIndicator(true)
                .showValue(false)
                .build()
                .show(v, new ColorPickerPopupCustom.ColorPickerObserver() {
                    @Override
                    public void onColorPicked(int color) {
                        uiColorCode = MyThemesUtils.getHexColor(color);
                        MyThemesUtils.setPrimaryColorCode(uiColorCode);

                        // Update the UI
                        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());
                    }

                    public void onColor(int color, boolean fromUser) {

                    }
                }));

        // Colors gridview
        GridView colorsGridView = (GridView) customView.findViewById(R.id.colors_gridview);
        ColorsAdapter colorsAdapter = new ColorsAdapter();
        colorsGridView.setAdapter(colorsAdapter);

        String oldColorUIcolorCode = uiColorCode;

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            MyThemesUtils.setPrimaryColorCode(oldColorUIcolorCode);
            if (onUiColorSelectedListener != null) {
                onUiColorSelectedListener.restart();
            }
            dialog.dismiss();

        });

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {

            if (onUiColorSelectedListener != null) {
                onUiColorSelectedListener.restart();
            }
            dialog.dismiss();
        });

        colorsGridView.setOnItemClickListener((parent, view, position, id) -> {

            uiColorCode = colorsArrayList.get(position);

            MyThemesUtils.setPrimaryColorCode(uiColorCode);

            // Update the UI
            builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());
            colorsAdapter.notifyDataSetChanged();

            if (onUiColorSelectedListener != null) {
                onUiColorSelectedListener.onUiColorSelected(uiColorCode);
            }

        });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void setUiColorCode(String uiColorCode) {
        this.uiColorCode = uiColorCode;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Prevent dismiss on touch outside
        getDialog().setCanceledOnTouchOutside(false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(UI_COLOR_CODE_STATE_KEY, uiColorCode);
    }

    public interface OnUiColorSelectedListener {
        void onUiColorSelected(String colorCode);

        void restart();
    }

    public class ColorsAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;


        public ColorsAdapter() {
            super(getActivity(), R.layout.ui_color);

            inflater = getActivity().getLayoutInflater();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView colorView;
            if (convertView == null) {
                colorView = (ImageView) inflater.inflate(R.layout.ui_color, parent, false);
            } else {
                colorView = (ImageView) convertView;
            }

            final String colorCode = colorsArrayList.get(position);
            if (colorCode.equals(uiColorCode))
                colorView.setImageResource(R.drawable.ic_ok_white_disabled_24dp);
            else {
                colorView.setImageDrawable(null);
            }

            Drawable bgDrawable = colorView.getBackground();
            bgDrawable.setColorFilter(Color.parseColor(colorCode), PorterDuff.Mode.SRC_ATOP);

            return colorView;
        }

        @Override
        public int getCount() {
            return colorsArrayList.size();
        }
    }

    private void initColorsList() {
        // Main colors
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.diaro_default));


        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_red_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_red_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_pink_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_pink_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_purple_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_purple_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_deep_purple_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_deep_purple_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_indigo_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_indigo_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_blue_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_blue_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_light_blue_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_light_blue_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_cyan_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_cyan_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_teal_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_teal_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_green_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_green_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_light_green_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_light_green_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_lime_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_lime_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_yellow_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_yellow_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_amber_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_amber_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_orange_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_orange_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_deep_orange_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_deep_orange_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_brown_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_brown_500));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_blue_grey_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_blue_grey_500));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_blue_grey_900));

        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_grey_400));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_grey_500));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_grey_600));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_grey_700));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_grey_800));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_grey_900));
        colorsArrayList.add(MyThemesUtils.getHexColorFromResId(R.color.md_black_1000));
    }


}
