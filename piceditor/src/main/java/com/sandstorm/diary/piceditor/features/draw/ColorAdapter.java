package com.sandstorm.diary.piceditor.features.draw;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pavlospt.CircleView;
import com.sandstorm.diary.piceditor.R;
import com.sandstorm.diary.piceditor.utils.ColorUtils;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {

    public BrushColorListener brushColorListener;
    public List<String> colors = ColorUtils.lstColorForBrush();
    private Context context;
    public int selectedColorIndex;

    public ColorAdapter(Context context2, BrushColorListener brushColorListener2) {
        this.context = context2;
        this.brushColorListener = brushColorListener2;
    }

    public ColorAdapter(Context context2, BrushColorListener brushColorListener2, boolean z) {
        this.context = context2;
        this.brushColorListener = brushColorListener2;
        this.colors.add(0, "#00000000");
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.color_item, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.color.setFillColor(Color.parseColor(this.colors.get(i)));
        viewHolder.color.setStrokeColor(Color.parseColor(this.colors.get(i)));
        if (this.selectedColorIndex == i) {
            if (this.colors.get(i).equals("#00000000")) {
                viewHolder.color.setBackgroundColor(Color.parseColor("#00000000"));
                viewHolder.color.setStrokeColor(Color.parseColor("#FF4081"));
                return;
            }
            viewHolder.color.setBackgroundColor(-1);
        } else if (this.colors.get(i).equals("#00000000")) {
            viewHolder.color.setBackground(ContextCompat.getDrawable(this.context, R.drawable.none));
            viewHolder.color.setBackgroundColor(Color.parseColor("#00000000"));
        } else {
            viewHolder.color.setBackgroundColor(Color.parseColor(this.colors.get(i)));
        }
    }

    public int getItemCount() {
        return this.colors.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CircleView color;

        ViewHolder(View view) {
            super(view);
            this.color = view.findViewById(R.id.color);
            this.color.setOnClickListener(view1 -> {
                ColorAdapter.this.selectedColorIndex = ViewHolder.this.getLayoutPosition();
                ColorAdapter.this.brushColorListener.onColorChanged(ColorAdapter.this.colors.get(ColorAdapter.this.selectedColorIndex));
                ColorAdapter.this.notifyDataSetChanged();
            });
        }
    }

    public void setSelectedColorIndex(int i) {
        this.selectedColorIndex = i;
    }
}
