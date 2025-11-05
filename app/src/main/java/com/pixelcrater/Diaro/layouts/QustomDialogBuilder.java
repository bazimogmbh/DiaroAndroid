package com.pixelcrater.Diaro.layouts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;

public class QustomDialogBuilder extends AlertDialog.Builder {

    private final Context mContext;
    private final ImageButton searchButton;
    public final EditText searchEditText;
    /**
     * The custom_body layout
     */
    private View dialogView;

    private View topPanelView;
    /**
     * optional dialog title layout
     */
    private TextView titleView;
    /**
     * optional alert dialog image
     */
    private ImageView iconView;
    /**
     * optional message displayed below title if title exists
     */
    private TextView messageView;
    /**
     * The colored holo divider. You can set its color with the setDividerColor method
     */
    private View mDivider;

    private ViewGroup customViewContainer;

    private ImageButton addNewButton;

    // Search text changed listener
    private OnSearchTextChangeListener onSearchTextChangeListener;

    public QustomDialogBuilder(Context context) {
        super(context);

        mContext = context;

        dialogView = View.inflate(context, R.layout.qustom_dialog_layout, null);
        setView(dialogView);

        // Set dialog background color by theme
//        dialogView.findViewById(R.id.parent_panel).setBackgroundResource(
//                ThemesUtils.getThemeDialogBackgroundColorResId());

        topPanelView = dialogView.findViewById(R.id.top_panel);
        iconView = (ImageView) dialogView.findViewById(R.id.icon);
        titleView = (TextView) dialogView.findViewById(R.id.alert_title);
        searchButton = (ImageButton) dialogView.findViewById(R.id.dialog_search_button);
        searchEditText = (EditText) dialogView.findViewById(R.id.dialog_search_field);

        VectorDrawableCompat vectorDrawableCompatSearch = VectorDrawableCompat.create(mContext.getResources(), R.drawable.ic_search_white_24dp, null);
        searchEditText.setCompoundDrawablesWithIntrinsicBounds( vectorDrawableCompatSearch, null, null, null);

        addNewButton = (ImageButton) dialogView.findViewById(R.id.add_new);
        mDivider = dialogView.findViewById(R.id.title_divider);

        messageView = (TextView) dialogView.findViewById(R.id.message);
        customViewContainer = (ViewGroup) dialogView.findViewById(R.id.custom_view_container);
    }

    public void setOnSearchTextChangeListener(OnSearchTextChangeListener l) {
        onSearchTextChangeListener = l;
    }

    /**
     * Use this method to color the divider between the title and content. Will not display if no title is set.
     *
     * @param colorString for passing "#ffffff"
     */
    public QustomDialogBuilder setDividerColor(String colorString) {
        mDivider.setBackgroundColor(Color.parseColor(colorString));
        return this;
    }

    @Override
    public QustomDialogBuilder setTitle(CharSequence text) {
        topPanelView.setVisibility(View.VISIBLE);
        titleView.setText(text);
        return this;
    }

    public QustomDialogBuilder setTitleColor(String colorString) {
        titleView.setTextColor(Color.parseColor(colorString));
        return this;
    }

    public QustomDialogBuilder setHeaderBackgroundColor(String colorString) {
        topPanelView.setBackgroundColor(Color.parseColor(colorString));
        return this;
    }

    @Override
    public QustomDialogBuilder setMessage(int textResId) {
        messageView.setVisibility(View.VISIBLE);
        messageView.setText(textResId);
        return this;
    }

    @Override
    public QustomDialogBuilder setMessage(CharSequence text) {
        messageView.setVisibility(View.VISIBLE);
        messageView.setText(text);
        return this;
    }

    @Override
    public QustomDialogBuilder setIcon(int drawableResId) {
        iconView.setVisibility(View.VISIBLE);
        iconView.setImageResource(drawableResId);
        return this;
    }

    @Override
    public QustomDialogBuilder setIcon(Drawable icon) {
        iconView.setVisibility(View.VISIBLE);
        iconView.setImageDrawable(icon);
        return this;
    }

    public QustomDialogBuilder showSearchButton() {
        searchButton.setVisibility(View.VISIBLE);
        searchButton.setOnClickListener(v -> showHideSearchField(searchEditText.getVisibility() == View.INVISIBLE));

        // TextChangedListener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (onSearchTextChangeListener != null) {
                    onSearchTextChangeListener.onSearchTextChange(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        return this;
    }

    public String getSearchFieldText() {
        return searchEditText.getText().toString();
    }

    public void showHideSearchField(boolean showSearchField) {
        if (showSearchField) {
            titleView.setVisibility(View.INVISIBLE);
            searchButton.setImageResource(R.drawable.ic_delete_sign_white_24dp);
            searchEditText.setVisibility(View.VISIBLE);

            // Show keyboard
            Static.showSoftKeyboard(searchEditText);
        } else {
            titleView.setVisibility(View.VISIBLE);
            searchButton.setImageResource(R.drawable.ic_search_white_24dp);
            searchEditText.setVisibility(View.INVISIBLE);
            searchEditText.setText("");

            // Hide keyboard
            Static.hideSoftKeyboard(searchEditText);
        }
    }

    public QustomDialogBuilder setAddNewButtonOnClick(OnClickListener onClickListener) {
        addNewButton.setVisibility(View.VISIBLE);
        addNewButton.setOnClickListener(onClickListener);
        return this;
    }

    public void changeAddNewDrawable(int resId){
        addNewButton.setVisibility(View.VISIBLE);
        addNewButton.setImageResource(resId);
    }

    public View getCustomView() {
        return customViewContainer;
    }

    /**
     * This allows you to specify a custom layout for the area below the title divider bar in the dialog. As an example you can look at
     * example_ip_address_layout.xml and how I added it in TestDialogActivity.java
     *
     * @param resId of the layout you would like to add
     */
    public QustomDialogBuilder setCustomView(int resId) {
        customViewContainer.setVisibility(View.VISIBLE);
        View customView = View.inflate(mContext, resId, null);
        customViewContainer.addView(customView);
        return this;
    }

    public interface OnSearchTextChangeListener {
        void onSearchTextChange(String text);
    }
}
