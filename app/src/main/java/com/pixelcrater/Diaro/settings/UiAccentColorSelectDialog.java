package com.pixelcrater.Diaro.settings;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class UiAccentColorSelectDialog extends DialogFragment {
    // State vars
    private static final String UI_ACCENT_COLOR_CODE_STATE_KEY = "UI_ACCENT_COLOR_CODE_STATE_KEY";

    private String uiAccentColorCode;

    // Dialog listener
    private OnUiColorSelectedListener onUiAccentColorSelectedListener;

    public void setOnUiAccentColorSelectedListener(OnUiColorSelectedListener l) {
        onUiAccentColorSelectedListener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            uiAccentColorCode = savedInstanceState.getString(UI_ACCENT_COLOR_CODE_STATE_KEY);
        }

        // Use the Builder class for convenient dialog construction
        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        builder.setTitle(getString(R.string.settings_ui_accent_color));

        // Set custom view
        builder.setCustomView(R.layout.ui_color_select_dialog);
        View customView = builder.getCustomView();

        // Colors gridview
        GridView colorsGridView = (GridView) customView.findViewById(R.id.colors_gridview);
        ColorsAdapter colorsAdapter = new ColorsAdapter();
        colorsGridView.setAdapter(colorsAdapter);

        builder.setNegativeButton(android.R.string.cancel, null);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void setUiAccentColorCode(String uiAccentColorCode) {
        this.uiAccentColorCode = uiAccentColorCode;
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

        outState.putString(UI_ACCENT_COLOR_CODE_STATE_KEY, uiAccentColorCode);
    }

    public interface OnUiColorSelectedListener {
        void onUiColorSelected(String colorCode);
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

            final String colorCode = MyThemesUtils.getUiAccentColorsArrayList().get(position).key;
            if (colorCode.equals(uiAccentColorCode)) {
                colorView.setImageResource(R.drawable.ic_ok_white_disabled_24dp);
            } else {
                colorView.setImageDrawable(null);
            }

            Drawable bgDrawable = colorView.getBackground();
            bgDrawable.setColorFilter(Color.parseColor(colorCode), PorterDuff.Mode.SRC_ATOP);

            colorView.setOnClickListener(v -> {
                if (onUiAccentColorSelectedListener != null) {
                    onUiAccentColorSelectedListener.onUiColorSelected(colorCode);
                }
                dismiss();
            });

            return colorView;
        }

        @Override
        public int getCount() {
            return MyThemesUtils.getUiAccentColorsArrayList().size();
        }
    }
}
