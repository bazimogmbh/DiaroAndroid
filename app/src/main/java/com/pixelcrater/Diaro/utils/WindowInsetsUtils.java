package com.pixelcrater.Diaro.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Utility class for handling window insets for edge-to-edge display on Android 15+.
 * Use these methods to ensure UI elements don't overlap with system bars.
 */
public class WindowInsetsUtils {

    private WindowInsetsUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Applies bottom system bar insets as padding to a view.
     * Use for views that should not be obscured by the navigation bar.
     * @param view The view to apply bottom insets to
     */
    public static void applyBottomInsets(View view) {
        if (view == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });
    }

    /**
     * Applies bottom system bar insets as padding to a view, with additional padding in dp.
     * @param view The view to apply bottom insets to
     * @param additionalPaddingDp Additional padding to add (in dp)
     */
    public static void applyBottomInsetsWithPadding(View view, int additionalPaddingDp) {
        if (view == null) return;
        float density = view.getResources().getDisplayMetrics().density;
        int additionalPaddingPx = (int) (additionalPaddingDp * density);
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom + additionalPaddingPx);
            return windowInsets;
        });
    }

    /**
     * Applies bottom insets as margin to a view (e.g., for FABs or floating elements).
     * @param view The view to apply bottom margin insets to
     * @param additionalMarginDp Additional margin to add (in dp)
     */
    public static void applyBottomInsetsAsMargin(View view, int additionalMarginDp) {
        if (view == null) return;
        float density = view.getResources().getDisplayMetrics().density;
        int additionalMarginPx = (int) (additionalMarginDp * density);
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = insets.bottom + additionalMarginPx;
            v.setLayoutParams(params);
            return windowInsets;
        });
    }

    /**
     * Applies top insets as margin to a view (e.g., for close buttons in fullscreen activities).
     * @param view The view to apply top margin insets to
     * @param additionalMarginDp Additional margin to add (in dp)
     */
    public static void applyTopInsetsAsMargin(View view, int additionalMarginDp) {
        if (view == null) return;
        float density = view.getResources().getDisplayMetrics().density;
        int additionalMarginPx = (int) (additionalMarginDp * density);
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = insets.top + additionalMarginPx;
            v.setLayoutParams(params);
            return windowInsets;
        });
    }

    /**
     * Applies both IME (keyboard) and system bar insets as margin to a view.
     * Use for editor toolbars that should appear above both keyboard and navigation bar.
     * @param view The view to apply insets to
     */
    public static void applyKeyboardAndBottomInsets(View view) {
        if (view == null) return;

        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (!(lp instanceof ViewGroup.MarginLayoutParams)) return;

        // Store original margin
        final int originalMarginBottom = ((ViewGroup.MarginLayoutParams) lp).bottomMargin;

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            int bottomInset = Math.max(imeInsets.bottom, systemBarInsets.bottom);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = originalMarginBottom + bottomInset;
            v.requestLayout(); // Lighter than setLayoutParams()

            return windowInsets;
        });
    }

        /**
         * Applies both top and bottom system bar insets as padding.
         * Use for fullscreen content that should avoid all system bars.
         * @param view The view to apply insets to
         */
    public static void applySystemBarInsets(View view) {
        if (view == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return windowInsets;
        });
    }
}
