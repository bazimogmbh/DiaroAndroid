package com.pixelcrater.Diaro.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.pixelcrater.Diaro.R;

import java.util.Locale;

import top.defaults.colorpicker.ColorObserver;
import top.defaults.colorpicker.ColorPickerView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ColorPickerPopupCustom {

    private Context context;
    private PopupWindow popupWindow;
    private int initialColor;
    private boolean enableBrightness;
    private boolean enableAlpha;
    private String okTitle;
    private String cancelTitle;
    private boolean showIndicator;
    private boolean showValue;
    private boolean onlyUpdateOnTouchEventUp;

    private ColorPickerPopupCustom(Builder builder) {
        this.context = builder.context;
        this.initialColor = builder.initialColor;
        this.enableBrightness = builder.enableBrightness;
        this.enableAlpha = builder.enableAlpha;
        this.okTitle = builder.okTitle;
        this.cancelTitle = builder.cancelTitle;
        this.showIndicator = builder.showIndicator;
        this.showValue = builder.showValue;
        this.onlyUpdateOnTouchEventUp = builder.onlyUpdateOnTouchEventUp;
    }

    public void show(final ColorPickerObserver observer) {
        show(null, observer);
    }

    public void show(View parent, final ColorPickerObserver observer) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater == null) return;

        @SuppressLint("InflateParams")
        View layout = inflater.inflate(R.layout.color_picker_popup, null);
        final ColorPickerView colorPickerView = layout.findViewById(R.id.colorPickerView);
        popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ColorDrawable cd = new ColorDrawable(ContextCompat.getColor(context, MyThemesUtils.getBackgroundColorResIdMainView()));
      //  cd.setAlpha(0);
        popupWindow.setBackgroundDrawable(cd);

        popupWindow.setOutsideTouchable(true);
        colorPickerView.setInitialColor(initialColor);
        colorPickerView.setEnabledBrightness(enableBrightness);
        colorPickerView.setEnabledAlpha(enableAlpha);
        colorPickerView.setOnlyUpdateOnTouchEventUp(onlyUpdateOnTouchEventUp);
        colorPickerView.subscribe(observer);
        TextView cancel = layout.findViewById(R.id.cancel);
        cancel.setTextColor(MyThemesUtils.getThemeAwareBWColor());
        cancel.setText(cancelTitle);
        cancel.setOnClickListener(v -> popupWindow.dismiss());
        TextView ok = layout.findViewById(R.id.ok);
        ok.setTextColor(MyThemesUtils.getThemeAwareBWColor());
        ok.setText(okTitle);
        ok.setOnClickListener(v -> {
            popupWindow.dismiss();
            if (observer != null) {
                observer.onColorPicked(colorPickerView.getColor());
            }
        });

        final View colorIndicator = layout.findViewById(R.id.colorIndicator);
        final TextView colorHex = layout.findViewById(R.id.colorHex);

        colorIndicator.setVisibility(showIndicator ? View.VISIBLE : View.GONE);
        colorHex.setVisibility(showValue ? View.VISIBLE : View.GONE);

        if (showIndicator) {
            colorIndicator.setBackgroundColor(initialColor);
        }
        if (showValue) {
            colorHex.setText(colorHex(initialColor));
        }
        colorPickerView.subscribe(new ColorObserver() {
            @Override
            public void onColor(int color, boolean fromUser, boolean shouldPropagate) {
                if (showIndicator) {
                    colorIndicator.setBackgroundColor(color);
                }
                if (showValue) {
                    colorHex.setText(colorHex(color));
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            popupWindow.setElevation(10.0f);
        }

        popupWindow.setAnimationStyle(R.style.TopDefaultsViewColorPickerPopupAnimation);
        if (parent == null) parent = layout;
        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
    }

    public void dismiss() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    public static class Builder {

        private Context context;
        private int initialColor = Color.MAGENTA;
        private boolean enableBrightness = true;
        private boolean enableAlpha = false;
        private String okTitle = "OK";
        private String cancelTitle = "Cancel";
        private boolean showIndicator = true;
        private boolean showValue = true;
        private boolean onlyUpdateOnTouchEventUp = false;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder initialColor(int color) {
            initialColor = color;
            return this;
        }

        public Builder enableBrightness(boolean enable) {
            enableBrightness = enable;
            return this;
        }


        public Builder enableAlpha(boolean enable) {
            enableAlpha = enable;
            return this;
        }

        public Builder okTitle(String title) {
            okTitle = title;
            return this;
        }

        public Builder cancelTitle(String title) {
            cancelTitle = title;
            return this;
        }

        public Builder showIndicator(boolean show) {
            showIndicator = show;
            return this;
        }

        public Builder showValue(boolean show) {
            showValue = show;
            return this;
        }

        public Builder onlyUpdateOnTouchEventUp(boolean only) {
            onlyUpdateOnTouchEventUp = only;
            return this;
        }

        public ColorPickerPopupCustom build() {
            return new ColorPickerPopupCustom(this);
        }
    }

    private String colorHex(int color) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return String.format(Locale.getDefault(), "0x%02X%02X%02X%02X", a, r, g, b);
    }

    public abstract static class ColorPickerObserver implements ColorObserver {
        public abstract void onColorPicked(int color);

        @Override
        public final void onColor(int color, boolean fromUser, boolean shouldPropagate) {

        }
    }
}