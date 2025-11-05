package com.sandstorm.diary.piceditor.tools;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sandstorm.diary.piceditor.R;

import java.util.ArrayList;
import java.util.List;

public class PieceToolsAdapter extends RecyclerView.Adapter<PieceToolsAdapter.ViewHolder> {

    public List<ToolModel> mToolList = new ArrayList<>();

    public OnPieceFuncItemSelected onPieceFuncItemSelected;

    public interface OnPieceFuncItemSelected {
        void onPieceFuncSelected(ToolType toolType);
    }

    public PieceToolsAdapter(OnPieceFuncItemSelected onPieceFuncItemSelected2) {
        this.onPieceFuncItemSelected = onPieceFuncItemSelected2;
        this.mToolList.add(new ToolModel("Change", R.drawable.ic_change, ToolType.REPLACE));
        this.mToolList.add(new ToolModel("Crop", R.drawable.ic_crop, ToolType.CROP));
        this.mToolList.add(new ToolModel("Rotate", R.drawable.ic_rotate, ToolType.ROTATE));
        this.mToolList.add(new ToolModel("H Flip", R.drawable.ic_h, ToolType.H_FLIP));
        this.mToolList.add(new ToolModel("V Flip", R.drawable.ic_v, ToolType.V_FLIP));
    }

    class ToolModel {

        public int mToolIcon;

        public String mToolName;

        public ToolType mToolType;

        ToolModel(String str, int i, ToolType toolType) {
            this.mToolName = str;
            this.mToolIcon = i;
            this.mToolType = toolType;
        }
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_piece_tools, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ToolModel toolModel = this.mToolList.get(i);
        viewHolder.txtTool.setText(toolModel.mToolName);
        viewHolder.imgToolIcon.setImageResource(toolModel.mToolIcon);
    }

    public int getItemCount() {
        return this.mToolList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgToolIcon;
        TextView txtTool;

        ViewHolder(View view) {
            super(view);
            this.imgToolIcon = view.findViewById(R.id.imgToolIcon);
            this.txtTool = view.findViewById(R.id.txtTool);
            view.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    ToolModel toolModel = PieceToolsAdapter.this.mToolList.get(ViewHolder.this.getLayoutPosition());
                    ToolType toolType = toolModel.mToolType;
                    PieceToolsAdapter.this.onPieceFuncItemSelected.onPieceFuncSelected(toolType);
                }
            });
        }
    }
}
