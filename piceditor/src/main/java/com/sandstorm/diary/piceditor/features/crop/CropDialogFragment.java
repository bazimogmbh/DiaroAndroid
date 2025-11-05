package com.sandstorm.diary.piceditor.features.crop;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.isseiaoki.simplecropview.CropImageView;
import com.sandstorm.diary.piceditor.R;
import com.sandstorm.diary.piceditor.features.crop.adapter.AspectRatioCustom;
import com.sandstorm.diary.piceditor.features.crop.adapter.AspectRatioPreviewAdapter;
import com.steelkiwi.cropiwa.AspectRatio;

import java.util.List;

public class CropDialogFragment extends DialogFragment implements AspectRatioPreviewAdapter.OnNewSelectedListener {

    private static final String TAG = "CropDialogFragment";
    private Bitmap bitmap;
    private RelativeLayout loadingView;
    public CropImageView mCropView;
    public OnCropPhoto onCropPhoto;

    AspectRatioPreviewAdapter aspectRatioPreviewAdapter = new AspectRatioPreviewAdapter();

    public interface OnCropPhoto {
        void finishCrop(Bitmap bitmap);
    }

    public void setBitmap(Bitmap bitmap2) {
        this.bitmap = bitmap2;
    }

    public void setAspectAdapter(AspectRatioPreviewAdapter aspectRatioPreviewAdapter) {
        this.aspectRatioPreviewAdapter = aspectRatioPreviewAdapter;
    }

    public static CropDialogFragment show(@NonNull AppCompatActivity appCompatActivity, OnCropPhoto onCropPhoto2, Bitmap bitmap2, List<AspectRatioCustom> ratios) {
        CropDialogFragment cropDialogFragment = new CropDialogFragment();

        cropDialogFragment.setAspectAdapter(new AspectRatioPreviewAdapter(ratios));

        cropDialogFragment.setBitmap(bitmap2);
        cropDialogFragment.setOnCropPhoto(onCropPhoto2);
        cropDialogFragment.show(appCompatActivity.getSupportFragmentManager(), TAG);
        return cropDialogFragment;
    }

    public static CropDialogFragment show(@NonNull AppCompatActivity appCompatActivity, OnCropPhoto onCropPhoto2, Bitmap bitmap2) {
        CropDialogFragment cropDialogFragment = new CropDialogFragment();
        cropDialogFragment.setBitmap(bitmap2);
        cropDialogFragment.setOnCropPhoto(onCropPhoto2);
        cropDialogFragment.show(appCompatActivity.getSupportFragmentManager(), TAG);
        return cropDialogFragment;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    public void setOnCropPhoto(OnCropPhoto onCropPhoto2) {
        this.onCropPhoto = onCropPhoto2;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
    }

    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(-1, -1);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(-16777216));
        }
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        getDialog().getWindow().requestFeature(1);
        getDialog().getWindow().setFlags(1024, 1024);
        View inflate = layoutInflater.inflate(R.layout.crop_layout, viewGroup, false);

        aspectRatioPreviewAdapter.setListener(this);
        RecyclerView recyclerView = inflate.findViewById(R.id.fixed_ratio_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(aspectRatioPreviewAdapter);
        this.mCropView = inflate.findViewById(R.id.crop_view);
        this.mCropView.setCropMode(CropImageView.CropMode.FREE);
        inflate.findViewById(R.id.rotate).setOnClickListener(view -> CropDialogFragment.this.mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D));

        inflate.findViewById(R.id.imgSave).setOnClickListener(view -> new OnSaveCrop().execute());
        this.loadingView = inflate.findViewById(R.id.loadingView);
        this.loadingView.setVisibility(View.GONE);
        inflate.findViewById(R.id.imgClose).setOnClickListener(view -> CropDialogFragment.this.dismiss());
        return inflate;
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mCropView = view.findViewById(R.id.crop_view);
        this.mCropView.setImageBitmap(this.bitmap);
    }

    public void onNewAspectRatioSelected(AspectRatio aspectRatio) {
        if (aspectRatio.getWidth() == 10 && aspectRatio.getHeight() == 10) {
            this.mCropView.setCropMode(CropImageView.CropMode.FREE);
        } else {
            this.mCropView.setCustomRatio(aspectRatio.getWidth(), aspectRatio.getHeight());
        }
    }

    class OnSaveCrop extends AsyncTask<Void, Bitmap, Bitmap> {
        OnSaveCrop() {
        }

        public void onPreExecute() {
            CropDialogFragment.this.showLoading(true);
        }

        public Bitmap doInBackground(Void... voidArr) {
            return CropDialogFragment.this.mCropView.getCroppedBitmap();
        }

        public void onPostExecute(Bitmap bitmap) {
            CropDialogFragment.this.showLoading(false);
            CropDialogFragment.this.onCropPhoto.finishCrop(bitmap);
            CropDialogFragment.this.dismiss();
        }
    }

    public void showLoading(boolean z) {
        if (z) {
            getActivity().getWindow().setFlags(16, 16);
            this.loadingView.setVisibility(View.VISIBLE);
            return;
        }
        getActivity().getWindow().clearFlags(16);
        this.loadingView.setVisibility(View.GONE);
    }
}
