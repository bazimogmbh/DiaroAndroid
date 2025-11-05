package com.sandstorm.diary.piceditor.features.sticker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.sandstorm.diary.piceditor.R;

public class TopTabAdapter extends RecyclerTabLayout.Adapter<TopTabAdapter.ViewHolder> {

    private final Context context;
    private final PagerAdapter mAdapater = this.mViewPager.getAdapter();

    public TopTabAdapter(ViewPager viewPager, Context context2) {
        super(viewPager);
        this.context = context2;
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.top_tab_view, viewGroup, false));
    }

    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        switch (i) {
            case 0:
                viewHolder.imageView.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.emoij));
                break;
            case 1:
                viewHolder.imageView.setImageDrawable(ContextCompat.getDrawable(this.context, R.drawable.other));
                break;
        }
        viewHolder.imageView.setSelected(i == getCurrentIndicatorPosition());
    }

    public int getItemCount() {
        return this.mAdapater.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            this.imageView = view.findViewById(R.id.image);
            view.setOnClickListener(view1 -> TopTabAdapter.this.getViewPager().setCurrentItem(ViewHolder.this.getAdapterPosition()));
        }
    }
}
