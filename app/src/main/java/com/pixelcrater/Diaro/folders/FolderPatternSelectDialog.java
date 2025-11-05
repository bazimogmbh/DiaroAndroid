package com.pixelcrater.Diaro.folders;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class FolderPatternSelectDialog extends DialogFragment {

    private static final String FOLDER_COLOR_STATE_KEY = "FOLDER_COLOR_STATE_KEY";

    private int color;

    // Item click listener
    private OnDialogItemClickListener onDialogItemClickListener;

    public void setDialogItemClickListener(OnDialogItemClickListener l) {
        onDialogItemClickListener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            color = savedInstanceState.getInt(FOLDER_COLOR_STATE_KEY);
        }

        // Use the Builder class for convenient dialog construction
        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        builder.setTitle(getActivity().getResources().getString(R.string.pattern));

        // Set custom view
        builder.setCustomView(R.layout.folder_pattern_select_dialog);
        View customView = builder.getCustomView();

        // Cancel button
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
        });

        // Patterns grid
        GridView patternsGrid = (GridView) customView.findViewById(R.id.patterns_grid);
        patternsGrid.setOnItemClickListener((parent, view, position, id) -> {
            // Send the onClick event back to the host activity
            if (onDialogItemClickListener != null)
                onDialogItemClickListener.onDialogItemClick(position);
            getDialog().dismiss();
        });

        PatternAdapter patternAdapter = new PatternAdapter(getActivity(), Static.getPatternsArrayList(), color);
        patternsGrid.setAdapter(patternAdapter);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Prevent dismiss on touch outside
        getDialog().setCanceledOnTouchOutside(false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(FOLDER_COLOR_STATE_KEY, color);
    }

    public interface OnDialogItemClickListener {
        void onDialogItemClick(int position);
    }
}
