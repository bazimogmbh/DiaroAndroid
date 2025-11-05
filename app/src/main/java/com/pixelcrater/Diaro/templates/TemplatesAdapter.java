package com.pixelcrater.Diaro.templates;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TemplatesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Template> mTemplates = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;

    private Typeface mEntryFont;

    private int textSize;

    public TemplatesAdapter(Context context, ArrayList<Template> items) {
        this.mTemplates = items;
        mEntryFont = PreferencesHelper.getPrefTypeFace(context);

        setTextSizes();
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.template_item, parent, false);
        v.setBackgroundResource(MyThemesUtils.getEntryListItemColorResId());
        vh = new OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, final int position) {
        Template template = mTemplates.get(position);

        OriginalViewHolder view = (OriginalViewHolder) holder;

        // Item color
        view.parentLayout.setBackgroundResource(MyThemesUtils.getEntryListItemColorResId());

        if (template.getTitle().equals("") || !PreferencesHelper.isTitleEnabled()) {
            view.title.setVisibility(View.GONE);
        } else {
            view.title.setVisibility(View.VISIBLE);
        }

        view.name.setText(template.getName());
        view.name.setTypeface(mEntryFont, Typeface.BOLD);
        view.title.setText(template.getTitle());
        view.text.setText(template.getText().trim().replaceAll("[\\t\\n\\r]", " "));

        view.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize + 2);
        view.title.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        view.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize - 2);

        view.parentLayout.setOnClickListener(view1 -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view1, mTemplates.get(position), position);
            }
        });
    }

    private void setTextSizes() {
        // Normal
        textSize = 15;
        int dispayDensity = PreferencesHelper.getDisplayDensity();
        if (dispayDensity == 1) {
            textSize = 14;
        }
        switch (MyApp.getInstance().prefs.getInt(Prefs.PREF_TEXT_SIZE, Prefs.SIZE_NORMAL)) {
            // Small
            case Prefs.SIZE_SMALL:
                textSize -= 2;
                break;
            // Large
            case Prefs.SIZE_LARGE:
                textSize += 1;
                break;
            // Extra Large
            case Prefs.SIZE_X_LARGE:
                textSize += 2;
                break;
        }
    }


    @Override
    public int getItemCount() {
        return mTemplates.size();
    }

    static class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView title;
        public TextView text;
        public LinearLayout parentLayout;

        public OriginalViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            title = v.findViewById(R.id.title);
            text = v.findViewById(R.id.text);
            parentLayout = v.findViewById(R.id.parentLayout);

        }
    }

    public void setData(List<Template> templates) {
        mTemplates = templates;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Template obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }
}