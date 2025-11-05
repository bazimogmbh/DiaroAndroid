package com.sandstorm.diary.piceditor.activities;


import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.internal.view.SupportMenu;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hold1.keyboardheightprovider.KeyboardHeightProvider;
import com.sandstorm.diary.piceditor.R;
import com.sandstorm.diary.piceditor.features.addtext.AddTextProperties;
import com.sandstorm.diary.piceditor.features.addtext.TextEditorDialogFragment;
import com.sandstorm.diary.piceditor.features.adjust.AdjustAdapter;
import com.sandstorm.diary.piceditor.features.adjust.AdjustListener;
import com.sandstorm.diary.piceditor.features.crop.CropDialogFragment;
import com.sandstorm.diary.piceditor.features.draw.BrushColorListener;
import com.sandstorm.diary.piceditor.features.draw.ColorAdapter;
import com.sandstorm.diary.piceditor.features.insta.InstaDialog;
import com.sandstorm.diary.piceditor.features.picker.PhotoPicker;
import com.sandstorm.diary.piceditor.features.picker.utils.PermissionsUtils;
import com.sandstorm.diary.piceditor.features.sticker.adapter.RecyclerTabLayout;
import com.sandstorm.diary.piceditor.features.sticker.adapter.StickerAdapter;
import com.sandstorm.diary.piceditor.features.sticker.adapter.TopTabAdapter;
import com.sandstorm.diary.piceditor.photoeditor.OnPhotoEditorListener;
import com.sandstorm.diary.piceditor.photoeditor.PhotoEditor;
import com.sandstorm.diary.piceditor.photoeditor.PhotoEditorView;
import com.sandstorm.diary.piceditor.photoeditor.ViewType;
import com.sandstorm.diary.piceditor.sticker.BitmapStickerIcon;
import com.sandstorm.diary.piceditor.sticker.DrawableSticker;
import com.sandstorm.diary.piceditor.sticker.Sticker;
import com.sandstorm.diary.piceditor.sticker.StickerView;
import com.sandstorm.diary.piceditor.sticker.TextSticker;
import com.sandstorm.diary.piceditor.sticker.event.AlignHorizontallyEvent;
import com.sandstorm.diary.piceditor.sticker.event.DeleteIconEvent;
import com.sandstorm.diary.piceditor.sticker.event.EditTextIconEvent;
import com.sandstorm.diary.piceditor.sticker.event.FlipHorizontallyEvent;
import com.sandstorm.diary.piceditor.sticker.event.ZoomIconEvent;
import com.sandstorm.diary.piceditor.tools.EditingToolsAdapter;
import com.sandstorm.diary.piceditor.tools.ToolType;
import com.sandstorm.diary.piceditor.utils.AssetUtils;
import com.sandstorm.diary.piceditor.utils.FileUtils;
import com.sandstorm.diary.piceditor.utils.FilterUtils;
import com.sandstorm.diary.piceditor.utils.SharePreferenceUtil;
import com.sandstorm.diary.piceditor.utils.SystemUtil;

import org.wysaid.myUtils.MsgUtil;
import org.wysaid.nativePort.CGENativeLibrary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class ImageEditorActivity extends AppCompatActivity implements OnPhotoEditorListener, View.OnClickListener, StickerAdapter.OnClickStickerListener, CropDialogFragment.OnCropPhoto,
        BrushColorListener, InstaDialog.InstaSaveListener, EditingToolsAdapter.OnItemSelected, AdjustListener {

    public static final int REQUEST_IMG_EDIT = 1024;

    private static final String TAG = "EditImageActivity";
    public ImageView addNewSticker;
    private ImageView addNewText;
    private ConstraintLayout adjustLayout;
    private SeekBar adjustSeekBar;
    private TextView brush, brushBlur;
    private ConstraintLayout brushLayout;
    private SeekBar brushSize;
    private ImageView compareAdjust, compareFilter, compareOverlay;
    public ToolType currentMode = ToolType.NONE;
    private ImageView erase;
    private SeekBar eraseSize;
    public SeekBar filterIntensity;
    public ConstraintLayout filterLayout;
    private KeyboardHeightProvider keyboardHeightProvider;
    private RelativeLayout loadingView;

    private String path = "";

    public AdjustAdapter mAdjustAdapter;
    private RecyclerView mColorBush;
    private final EditingToolsAdapter mEditingToolsAdapter = new EditingToolsAdapter(this);

    public CGENativeLibrary.LoadImageCallback mLoadImageCallback = new CGENativeLibrary.LoadImageCallback() {
        public Bitmap loadImage(String str, Object obj) {
            try {
                return BitmapFactory.decodeStream(ImageEditorActivity.this.getAssets().open(str));
            } catch (IOException io) {
                return null;
            }
        }

        public void loadImageOK(Bitmap bitmap, Object obj) {
            bitmap.recycle();
        }
    };

    public PhotoEditor mPhotoEditor;
    public PhotoEditorView mPhotoEditorView;
    private ConstraintLayout mRootView;
    private RecyclerView mRvAdjust, mRvFilters, mRvOverlays, mRvTools;

    View.OnTouchListener onCompareTouchListener = (view, motionEvent) -> {
        switch (motionEvent.getAction()) {
            case 0:
                ImageEditorActivity.this.mPhotoEditorView.getGLSurfaceView().setAlpha(0.0f);
                return true;
            case 1:
                ImageEditorActivity.this.mPhotoEditorView.getGLSurfaceView().setAlpha(1.0f);
                return false;
            default:
                return true;
        }
    };

    public SeekBar overlayIntensity;

    public ConstraintLayout overlayLayout;
    private ImageView redo;
    private ConstraintLayout saveControl;

    public SeekBar stickerAlpha;
    private ConstraintLayout stickerLayout;

    public TextEditorDialogFragment.TextEditor textEditor;

    public TextEditorDialogFragment textEditorDialogFragment;
    private ConstraintLayout textLayout;
    private ImageView undo;

    public RelativeLayout wrapPhotoView;

    public LinearLayout wrapStickerList;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        makeFullScreen();
        setContentView(R.layout.activity_edit_image);

        initViews();
        CGENativeLibrary.setLoadImageCallback(this.mLoadImageCallback, null);
        if (Build.VERSION.SDK_INT < 26) {
            getWindow().setSoftInputMode(48);
        }
        this.mRvTools.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.mRvTools.setAdapter(this.mEditingToolsAdapter);
        this.mRvFilters.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.mRvFilters.setHasFixedSize(true);
        this.mRvOverlays.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.mRvOverlays.setHasFixedSize(true);
        new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        this.mRvAdjust.setLayoutManager(new GridLayoutManager(this, 4));
        this.mRvAdjust.setHasFixedSize(true);
        this.mAdjustAdapter = new AdjustAdapter(getApplicationContext(), this);
        this.mRvAdjust.setAdapter(this.mAdjustAdapter);
        this.mColorBush.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.mColorBush.setHasFixedSize(true);
        this.mColorBush.setAdapter(new ColorAdapter(getApplicationContext(), this));

        this.mPhotoEditor = new PhotoEditor.Builder(this, this.mPhotoEditorView).setPinchTextScalable(true).build();
        this.mPhotoEditor.setOnPhotoEditorListener(this);
        toogleDrawBottomToolbar(false);
        this.brushLayout.setAlpha(0.0f);
        this.adjustLayout.setAlpha(0.0f);
        this.filterLayout.setAlpha(0.0f);
        this.stickerLayout.setAlpha(0.0f);
        this.textLayout.setAlpha(0.0f);
        this.overlayLayout.setAlpha(0.0f);
        findViewById(R.id.activitylayout).post(() -> {
            slideDown(brushLayout);
            slideDown(adjustLayout);
            slideDown(filterLayout);
            slideDown(stickerLayout);
            slideDown(textLayout);
            slideDown(overlayLayout);
        });
        new Handler().postDelayed(() -> {
            brushLayout.setAlpha(1.0f);
            adjustLayout.setAlpha(1.0f);
            filterLayout.setAlpha(1.0f);
            stickerLayout.setAlpha(1.0f);
            textLayout.setAlpha(1.0f);
            overlayLayout.setAlpha(1.0f);
        }, 1000);
        SharePreferenceUtil.setHeightOfKeyboard(getApplicationContext(), 0);
        this.keyboardHeightProvider = new KeyboardHeightProvider(this);
        this.keyboardHeightProvider.addKeyboardListener(i -> {
            if (i <= 0) {
                SharePreferenceUtil.setHeightOfNotch(getApplicationContext(), -i);
            } else if (textEditorDialogFragment != null) {
                textEditorDialogFragment.updateAddTextBottomToolbarHeight(SharePreferenceUtil.getHeightOfNotch(getApplicationContext()) + i);
                SharePreferenceUtil.setHeightOfKeyboard(getApplicationContext(), i + SharePreferenceUtil.getHeightOfNotch(getApplicationContext()));
            }
        });
        if (SharePreferenceUtil.isPurchased(getApplicationContext())) {
            ((ConstraintLayout.LayoutParams) this.wrapPhotoView.getLayoutParams()).topMargin = SystemUtil.dpToPx(getApplicationContext(), 5);
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            path =  extras.getString(PhotoPicker.KEY_SELECTED_PHOTOS);
            new OnLoadBitmapFromUri().execute(path);
        }
    }

    private void toogleDrawBottomToolbar(boolean z) {
        int i = !z ? View.GONE : View.VISIBLE;
        this.brush.setVisibility(i);
        this.brushBlur.setVisibility(i);
        this.erase.setVisibility(i);
        this.undo.setVisibility(i);
        this.redo.setVisibility(i);
    }

    public void showEraseBrush() {
        this.brushSize.setVisibility(View.GONE);
        this.mColorBush.setVisibility(View.GONE);
        this.eraseSize.setVisibility(View.VISIBLE);
        this.brush.setBackgroundResource(0);
        this.brush.setTextColor(ContextCompat.getColor(this, R.color.textColorMain));
        this.brushBlur.setBackgroundResource(0);
        this.brushBlur.setTextColor(ContextCompat.getColor(this, R.color.textColorMain_50));
        this.erase.setImageResource(R.drawable.erase_selected);
        this.mPhotoEditor.brushEraser();
        this.eraseSize.setProgress(20);
    }

    public void showColorBlurBrush() {
        this.brushSize.setVisibility(View.VISIBLE);
        this.mColorBush.setVisibility(View.VISIBLE);
        ColorAdapter colorAdapter = (ColorAdapter) this.mColorBush.getAdapter();
        if (colorAdapter != null) {
            colorAdapter.setSelectedColorIndex(0);
        }
        this.mColorBush.scrollToPosition(0);
        if (colorAdapter != null) {
            colorAdapter.notifyDataSetChanged();
        }
        this.eraseSize.setVisibility(View.GONE);
        this.erase.setImageResource(R.drawable.erase);
        this.brush.setBackgroundResource(0);
        this.brush.setTextColor(ContextCompat.getColor(this, R.color.textColorMain_50));
        this.brushBlur.setBackground(ContextCompat.getDrawable(this, R.drawable.border_bottom));
        this.brushBlur.setTextColor(ContextCompat.getColor(this, R.color.textColorMain));
        this.mPhotoEditor.setBrushMode(2);
        this.mPhotoEditor.setBrushDrawingMode(true);
        this.brushSize.setProgress(20);
    }

    public void showColorBrush() {
        this.brushSize.setVisibility(View.VISIBLE);
        this.mColorBush.setVisibility(View.VISIBLE);
        this.mColorBush.scrollToPosition(0);
        ColorAdapter colorAdapter = (ColorAdapter) this.mColorBush.getAdapter();
        if (colorAdapter != null) {
            colorAdapter.setSelectedColorIndex(0);
        }
        if (colorAdapter != null) {
            colorAdapter.notifyDataSetChanged();
        }
        this.eraseSize.setVisibility(View.GONE);
        this.erase.setImageResource(R.drawable.erase);
        this.brush.setBackground(ContextCompat.getDrawable(this, R.drawable.border_bottom));
        this.brush.setTextColor(ContextCompat.getColor(this, R.color.textColorMain));
        this.brushBlur.setBackgroundResource(0);
        this.brushBlur.setTextColor(ContextCompat.getColor(this, R.color.textColorMain_50));
        this.mPhotoEditor.setBrushMode(1);
        this.mPhotoEditor.setBrushDrawingMode(true);
        this.brushSize.setProgress(20);
    }

    private void initViews() {
        this.wrapStickerList = findViewById(R.id.wrapStickerList);
        this.mPhotoEditorView = findViewById(R.id.photoEditorView);
        this.mPhotoEditorView.setVisibility(View.INVISIBLE);
        this.mRvTools = findViewById(R.id.rvConstraintTools);
        this.mRvFilters = findViewById(R.id.rvFilterView);
        this.mRvOverlays = findViewById(R.id.rvOverlayView);
        this.mRvAdjust = findViewById(R.id.rvAdjustView);
        this.mRootView = findViewById(R.id.rootView);
        this.filterLayout = findViewById(R.id.filterLayout);
        this.overlayLayout = findViewById(R.id.overlayLayout);
        this.adjustLayout = findViewById(R.id.adjustLayout);
        this.stickerLayout = findViewById(R.id.stickerLayout);
        this.textLayout = findViewById(R.id.textControl);
        ViewPager viewPager = findViewById(R.id.sticker_viewpaper);
        this.filterIntensity = findViewById(R.id.filterIntensity);
        this.overlayIntensity = findViewById(R.id.overlayIntensity);
        this.stickerAlpha = findViewById(R.id.stickerAlpha);
        this.stickerAlpha.setVisibility(View.GONE);
        this.brushLayout = findViewById(R.id.brushLayout);
        this.mColorBush = findViewById(R.id.rvColorBush);
        this.wrapPhotoView = findViewById(R.id.wrap_photo_view);
        this.brush = findViewById(R.id.draw);
        this.erase = findViewById(R.id.erase);
        this.undo = findViewById(R.id.undo);
        this.undo.setVisibility(View.GONE);
        this.redo = findViewById(R.id.redo);
        this.redo.setVisibility(View.GONE);
        this.brushBlur = findViewById(R.id.brush_blur);
        this.brushSize = findViewById(R.id.brushSize);
        this.eraseSize = findViewById(R.id.eraseSize);
        this.loadingView = findViewById(R.id.loadingView);
        this.loadingView.setVisibility(View.VISIBLE);
        TextView saveBitmap = findViewById(R.id.save);
        this.saveControl = findViewById(R.id.saveControl);
        saveBitmap.setOnClickListener(view -> {
            new SaveBitmapAsFile().execute();
        });


        this.compareAdjust = findViewById(R.id.compareAdjust);
        this.compareAdjust.setOnTouchListener(this.onCompareTouchListener);
        this.compareAdjust.setVisibility(View.GONE);

        this.compareFilter = findViewById(R.id.compareFilter);
        this.compareFilter.setOnTouchListener(this.onCompareTouchListener);
        this.compareFilter.setVisibility(View.GONE);

        this.compareOverlay = findViewById(R.id.compareOverlay);
        this.compareOverlay.setOnTouchListener(this.onCompareTouchListener);
        this.compareOverlay.setVisibility(View.GONE);
        findViewById(R.id.exitEditMode).setOnClickListener(view -> ImageEditorActivity.this.onBackPressed());
        this.erase.setOnClickListener(view -> ImageEditorActivity.this.showEraseBrush());
        this.brush.setOnClickListener(view -> ImageEditorActivity.this.showColorBrush());
        this.brushBlur.setOnClickListener(view -> ImageEditorActivity.this.showColorBlurBrush());
        this.eraseSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                ImageEditorActivity.this.mPhotoEditor.setBrushEraserSize((float) i);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                ImageEditorActivity.this.mPhotoEditor.brushEraser();
            }
        });
        this.brushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                ImageEditorActivity.this.mPhotoEditor.setBrushSize((float) (i + 10));
            }
        });
        this.stickerAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                Sticker currentSticker = ImageEditorActivity.this.mPhotoEditorView.getCurrentSticker();
                if (currentSticker != null) {
                    currentSticker.setAlpha(i);
                }
            }
        });
        this.addNewSticker = findViewById(R.id.addNewSticker);
        this.addNewSticker.setVisibility(View.GONE);
        this.addNewSticker.setOnClickListener(view -> {
            ImageEditorActivity.this.addNewSticker.setVisibility(View.GONE);
            ImageEditorActivity.this.slideUp(ImageEditorActivity.this.wrapStickerList);
        });
        this.addNewText = findViewById(R.id.addNewText);
        this.addNewText.setVisibility(View.GONE);
        this.addNewText.setOnClickListener(view -> {
            ImageEditorActivity.this.mPhotoEditorView.setHandlingSticker(null);
            ImageEditorActivity.this.openTextFragment();
        });
        this.adjustSeekBar = findViewById(R.id.adjustLevel);
        this.adjustSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                ImageEditorActivity.this.mAdjustAdapter.getCurrentAdjustModel().setIntensity(ImageEditorActivity.this.mPhotoEditor, ((float) i) / ((float) seekBar.getMax()), true);
            }
        });
        BitmapStickerIcon bitmapStickerIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.sticker_ic_close_white_18dp), 0, BitmapStickerIcon.REMOVE);
        bitmapStickerIcon.setIconEvent(new DeleteIconEvent());
        BitmapStickerIcon bitmapStickerIcon2 = new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.ic_sticker_ic_scale_black_18dp), 3, BitmapStickerIcon.ZOOM);
        bitmapStickerIcon2.setIconEvent(new ZoomIconEvent());
        BitmapStickerIcon bitmapStickerIcon3 = new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.ic_sticker_ic_flip_black_18dp), 1, BitmapStickerIcon.FLIP);
        bitmapStickerIcon3.setIconEvent(new FlipHorizontallyEvent());
        BitmapStickerIcon bitmapStickerIcon4 = new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.ic_rotate_black_18dp), 3, BitmapStickerIcon.ROTATE);
        bitmapStickerIcon4.setIconEvent(new ZoomIconEvent());
        BitmapStickerIcon bitmapStickerIcon5 = new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.ic_edit_black_18dp), 1, BitmapStickerIcon.EDIT);
        bitmapStickerIcon5.setIconEvent(new EditTextIconEvent());
        BitmapStickerIcon bitmapStickerIcon6 = new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.ic_center_black_18dp), 2, BitmapStickerIcon.ALIGN_HORIZONTALLY);
        bitmapStickerIcon6.setIconEvent(new AlignHorizontallyEvent());
        this.mPhotoEditorView.setIcons(Arrays.asList(bitmapStickerIcon, bitmapStickerIcon2, bitmapStickerIcon3, bitmapStickerIcon5, bitmapStickerIcon4, bitmapStickerIcon6));
        this.mPhotoEditorView.setBackgroundColor(-16777216);
        this.mPhotoEditorView.setLocked(false);
        this.mPhotoEditorView.setConstrained(true);
        this.mPhotoEditorView.setOnStickerOperationListener(new StickerView.OnStickerOperationListener() {
            public void onStickerDragFinished(@NonNull Sticker sticker) {
            }

            public void onStickerFlipped(@NonNull Sticker sticker) {
            }

            public void onStickerTouchedDown(@NonNull Sticker sticker) {
            }

            public void onStickerZoomFinished(@NonNull Sticker sticker) {
            }

            public void onTouchDownForBeauty(float f, float f2) {
            }

            public void onTouchDragForBeauty(float f, float f2) {
            }

            public void onTouchUpForBeauty(float f, float f2) {
            }

            public void onStickerAdded(@NonNull Sticker sticker) {
                ImageEditorActivity.this.stickerAlpha.setVisibility(View.VISIBLE);
                ImageEditorActivity.this.stickerAlpha.setProgress(sticker.getAlpha());
            }

            public void onStickerClicked(@NonNull Sticker sticker) {
                if (sticker instanceof TextSticker) {
                    ((TextSticker) sticker).setTextColor(SupportMenu.CATEGORY_MASK);
                    ImageEditorActivity.this.mPhotoEditorView.replace(sticker);
                    ImageEditorActivity.this.mPhotoEditorView.invalidate();
                }
                ImageEditorActivity.this.stickerAlpha.setVisibility(View.VISIBLE);
                ImageEditorActivity.this.stickerAlpha.setProgress(sticker.getAlpha());
            }

            public void onStickerDeleted(@NonNull Sticker sticker) {
                ImageEditorActivity.this.stickerAlpha.setVisibility(View.GONE);
            }

            public void onStickerTouchOutside() {
                ImageEditorActivity.this.stickerAlpha.setVisibility(View.GONE);
            }

            public void onStickerDoubleTapped(@NonNull Sticker sticker) {
                if (sticker instanceof TextSticker) {
                    sticker.setShow(false);
                    ImageEditorActivity.this.mPhotoEditorView.setHandlingSticker(null);
                    ImageEditorActivity.this.textEditorDialogFragment = TextEditorDialogFragment.show(ImageEditorActivity.this, ((TextSticker) sticker).getAddTextProperties());
                    ImageEditorActivity.this.textEditor = new TextEditorDialogFragment.TextEditor() {
                        public void onDone(AddTextProperties addTextProperties) {
                            ImageEditorActivity.this.mPhotoEditorView.getStickers().remove(ImageEditorActivity.this.mPhotoEditorView.getLastHandlingSticker());
                            ImageEditorActivity.this.mPhotoEditorView.addSticker(new TextSticker(ImageEditorActivity.this, addTextProperties));
                        }

                        public void onBackButton() {
                            ImageEditorActivity.this.mPhotoEditorView.showLastHandlingSticker();
                        }
                    };
                    ImageEditorActivity.this.textEditorDialogFragment.setOnTextEditorListener(ImageEditorActivity.this.textEditor);
                }
            }
        });
        this.filterIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                ImageEditorActivity.this.mPhotoEditorView.setFilterIntensity(((float) i) / 100.0f);
            }
        });
        this.overlayIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                ImageEditorActivity.this.mPhotoEditorView.setFilterIntensity(((float) i) / 100.0f);
            }
        });
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        viewPager.setAdapter(new PagerAdapter() {
            public int getCount() {
                return 2;
            }

            public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
                return view.equals(obj);
            }


            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                (container).removeView((View) object);
            }

            @NonNull
            public Object instantiateItem(@NonNull ViewGroup viewGroup, int i) {
                View inflate = LayoutInflater.from(ImageEditorActivity.this.getBaseContext()).inflate(R.layout.sticker_items, null, false);
                RecyclerView recyclerView = inflate.findViewById(R.id.rv);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new GridLayoutManager(ImageEditorActivity.this.getApplicationContext(), 4));
                switch (i) {
                    case 0:
                        recyclerView.setAdapter(new StickerAdapter(ImageEditorActivity.this.getApplicationContext(), AssetUtils.lstEmoj(), i, ImageEditorActivity.this));
                        break;

                    case 1:
                        recyclerView.setAdapter(new StickerAdapter(ImageEditorActivity.this.getApplicationContext(), AssetUtils.lstOthers(), i, ImageEditorActivity.this));
                        break;
                }
                viewGroup.addView(inflate);
                return inflate;
            }
        });
        RecyclerTabLayout recyclerTabLayout = findViewById(R.id.recycler_tab_layout);
        recyclerTabLayout.setUpWithAdapter(new TopTabAdapter(viewPager, getApplicationContext()));
        recyclerTabLayout.setPositionThreshold(0.5f);
        recyclerTabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
    }

    public void showLoading(boolean z) {
        if (z) {
            getWindow().setFlags(16, 16);
            this.loadingView.setVisibility(View.VISIBLE);
            return;
        }
        getWindow().clearFlags(16);
        this.loadingView.setVisibility(View.GONE);
    }

    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
    }

    public void onAddViewListener(ViewType viewType, int i) {
        Log.d(TAG, "onAddViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + i + "]");
    }

    public void onRemoveViewListener(int i) {
        Log.d(TAG, "onRemoveViewListener() called with: numberOfAddedViews = [" + i + "]");
    }

    public void onRemoveViewListener(ViewType viewType, int i) {
        Log.d(TAG, "onRemoveViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + i + "]");
    }

    public void onStartViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    public void onStopViewChangeListener(ViewType viewType) {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [" + viewType + "]");
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.imgCloseAdjust || id == R.id.imgCloseBrush || id == R.id.imgCloseFilter || id == R.id.imgCloseOverlay || id == R.id.imgCloseSticker || id == R.id.imgCloseText) {
            slideDownSaveView();
            onBackPressed();
            return;
        } else if (id == R.id.imgSaveAdjust) {
            new SaveFilterAsBitmap().execute();
            this.compareAdjust.setVisibility(View.GONE);
            slideDown(this.adjustLayout);
            slideUp(this.mRvTools);
            slideDownSaveView();
            this.currentMode = ToolType.NONE;
            return;
        } else if (id == R.id.imgSaveBrush) {
            showLoading(true);
            runOnUiThread(() -> {
                mPhotoEditor.setBrushDrawingMode(false);
                undo.setVisibility(View.GONE);
                redo.setVisibility(View.GONE);
                erase.setVisibility(View.GONE);
                slideDown(brushLayout);
                slideUp(mRvTools);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(mRootView);
                if (!SharePreferenceUtil.isPurchased(getApplicationContext())) {
                    constraintSet.connect(wrapPhotoView.getId(), 3, mRootView.getId(), 3, SystemUtil.dpToPx(getApplicationContext(), 50));
                } else {
                    constraintSet.connect(wrapPhotoView.getId(), 3, mRootView.getId(), 3, 0);
                }
                constraintSet.connect(wrapPhotoView.getId(), 1, mRootView.getId(), 1, 0);
                constraintSet.connect(wrapPhotoView.getId(), 4, mRvTools.getId(), 3, 0);
                constraintSet.connect(wrapPhotoView.getId(), 2, mRootView.getId(), 2, 0);
                constraintSet.applyTo(mRootView);
                mPhotoEditorView.setImageSource(mPhotoEditor.getBrushDrawingView().getDrawBitmap(mPhotoEditorView.getCurrentBitmap()));
                mPhotoEditor.clearBrushAllViews();
                showLoading(false);
                updateLayout();
            });
            slideDownSaveView();
            this.currentMode = ToolType.NONE;
            return;
        } else if (id == R.id.imgSaveFilter) {
            new SaveFilterAsBitmap().execute();
            this.compareFilter.setVisibility(View.GONE);
            slideDown(this.filterLayout);
            slideUp(this.mRvTools);
            slideDownSaveView();
            this.currentMode = ToolType.NONE;
            return;
        } else if (id == R.id.imgSaveOverlay) {
            new SaveFilterAsBitmap().execute();
            slideDown(this.overlayLayout);
            slideUp(this.mRvTools);
            this.compareOverlay.setVisibility(View.GONE);
            slideDownSaveView();
            this.currentMode = ToolType.NONE;
            return;
        } else if (id == R.id.imgSaveSticker) {
            this.mPhotoEditorView.setHandlingSticker(null);
            this.mPhotoEditorView.setLocked(true);
            this.stickerAlpha.setVisibility(View.GONE);
            this.addNewSticker.setVisibility(View.GONE);
            if (!this.mPhotoEditorView.getStickers().isEmpty()) {
                new SaveStickerAsBitmap().execute();
            }
            slideUp(this.wrapStickerList);
            slideDown(this.stickerLayout);
            slideUp(this.mRvTools);
            slideDownSaveView();
            this.currentMode = ToolType.NONE;
            return;
        } else if (id == R.id.imgSaveText) {
            this.mPhotoEditorView.setHandlingSticker(null);
            this.mPhotoEditorView.setLocked(true);
            this.addNewText.setVisibility(View.GONE);
            if (!this.mPhotoEditorView.getStickers().isEmpty()) {
                new SaveStickerAsBitmap().execute();
            }
            slideDown(this.textLayout);
            slideUp(this.mRvTools);
            slideDownSaveView();
            this.currentMode = ToolType.NONE;
            return;
        } else if (id == R.id.redo) {
            this.mPhotoEditor.redoBrush();
            return;
        } else if (id == R.id.undo) {
            this.mPhotoEditor.undoBrush();
            return;
        }
    }


    public void onPause() {
        super.onPause();
        this.keyboardHeightProvider.onPause();
    }

    public void onResume() {
        super.onResume();
        this.keyboardHeightProvider.onResume();
    }

    public void isPermissionGranted(boolean z, String str) {
        if (z) {
            new SaveBitmapAsFile().execute();
        }
    }

    public void openTextFragment() {
        this.textEditorDialogFragment = TextEditorDialogFragment.show(this);
        this.textEditor = new TextEditorDialogFragment.TextEditor() {
            public void onDone(AddTextProperties addTextProperties) {
                ImageEditorActivity.this.mPhotoEditorView.addSticker(new TextSticker(ImageEditorActivity.this.getApplicationContext(), addTextProperties));
            }

            public void onBackButton() {
                if (ImageEditorActivity.this.mPhotoEditorView.getStickers().isEmpty()) {
                    ImageEditorActivity.this.onBackPressed();
                }
            }
        };
        this.textEditorDialogFragment.setOnTextEditorListener(this.textEditor);
    }

    public void onToolSelected(ToolType toolType) {
        this.currentMode = toolType;
        switch (toolType) {
            case BRUSH:
                showColorBrush();
                this.mPhotoEditor.setBrushDrawingMode(true);
                slideDown(this.mRvTools);
                slideUp(this.brushLayout);
                slideUpSaveControl();
                toogleDrawBottomToolbar(true);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(this.mRootView);
                if (!SharePreferenceUtil.isPurchased(getApplicationContext())) {
                    constraintSet.connect(this.wrapPhotoView.getId(), 3, this.mRootView.getId(), 3, SystemUtil.dpToPx(getApplicationContext(), 50));
                } else {
                    constraintSet.connect(this.wrapPhotoView.getId(), 3, this.mRootView.getId(), 3, 0);
                }
                constraintSet.connect(this.wrapPhotoView.getId(), 1, this.mRootView.getId(), 1, 0);
                constraintSet.connect(this.wrapPhotoView.getId(), 4, this.brushLayout.getId(), 3, 0);
                constraintSet.connect(this.wrapPhotoView.getId(), 2, this.mRootView.getId(), 2, 0);
                constraintSet.applyTo(this.mRootView);
                this.mPhotoEditor.setBrushMode(1);
                updateLayout();
                break;
            case TEXT:
                slideUpSaveView();
                this.mPhotoEditorView.setLocked(false);
                openTextFragment();
                slideDown(this.mRvTools);
                slideUp(this.textLayout);
                this.addNewText.setVisibility(View.VISIBLE);
                break;
            case ADJUST:
                slideUpSaveView();
                this.compareAdjust.setVisibility(View.VISIBLE);
                this.mAdjustAdapter = new AdjustAdapter(getApplicationContext(), this);
                this.mRvAdjust.setAdapter(this.mAdjustAdapter);
                this.mAdjustAdapter.setSelectedAdjust(0);
                this.mPhotoEditor.setAdjustFilter(this.mAdjustAdapter.getFilterConfig());
                slideUp(this.adjustLayout);
                slideDown(this.mRvTools);
                break;
            case STICKER:
                slideUpSaveView();
                this.mPhotoEditorView.setLocked(false);
                slideDown(this.mRvTools);
                slideUp(this.stickerLayout);
                break;
            case INSTA:
                new ShowInstaDialog().execute();
                break;
            case CROP:
                CropDialogFragment.show(this, this, this.mPhotoEditorView.getCurrentBitmap());
                break;
        }
        this.mPhotoEditorView.setHandlingSticker(null);
    }

    public void slideUp(View view) {
        ObjectAnimator.ofFloat(view, "translationY", (float) view.getHeight(), 0.0f).start();
    }

    public void slideUpSaveView() {
        this.saveControl.setVisibility(View.GONE);
    }

    public void slideUpSaveControl() {
        this.saveControl.setVisibility(View.GONE);
    }

    public void slideDownSaveControl() {
        this.saveControl.setVisibility(View.VISIBLE);
    }

    public void slideDownSaveView() {
        this.saveControl.setVisibility(View.VISIBLE);
    }

    public void slideDown(View view) {
        ObjectAnimator.ofFloat(view, "translationY", 0.0f, (float) view.getHeight()).start();
    }

    public void onBackPressed() {
        if (this.currentMode != null) {
            try {
                switch (this.currentMode) {
                    case BRUSH:
                        slideDown(this.brushLayout);
                        slideUp(this.mRvTools);
                        slideDownSaveControl();
                        this.undo.setVisibility(View.GONE);
                        this.redo.setVisibility(View.GONE);
                        this.erase.setVisibility(View.GONE);
                        this.mPhotoEditor.setBrushDrawingMode(false);
                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(this.mRootView);
                        if (!SharePreferenceUtil.isPurchased(getApplicationContext())) {
                            constraintSet.connect(this.wrapPhotoView.getId(), 3, this.mRootView.getId(), 3, SystemUtil.dpToPx(getApplicationContext(), 50));
                        } else {
                            constraintSet.connect(this.wrapPhotoView.getId(), 3, this.mRootView.getId(), 3, 0);
                        }
                        constraintSet.connect(this.wrapPhotoView.getId(), 1, this.mRootView.getId(), 1, 0);
                        constraintSet.connect(this.wrapPhotoView.getId(), 4, this.mRvTools.getId(), 3, 0);
                        constraintSet.connect(this.wrapPhotoView.getId(), 2, this.mRootView.getId(), 2, 0);
                        constraintSet.applyTo(this.mRootView);
                        this.mPhotoEditor.clearBrushAllViews();
                        slideDownSaveView();
                        this.currentMode = ToolType.NONE;
                        updateLayout();
                        return;
                    case TEXT:
                        if (!this.mPhotoEditorView.getStickers().isEmpty()) {
                            this.mPhotoEditorView.getStickers().clear();
                            this.mPhotoEditorView.setHandlingSticker(null);
                        }
                        slideDown(this.textLayout);
                        this.addNewText.setVisibility(View.GONE);
                        this.mPhotoEditorView.setHandlingSticker(null);
                        slideUp(this.mRvTools);
                        this.mPhotoEditorView.setLocked(true);
                        slideDownSaveView();
                        this.currentMode = ToolType.NONE;
                        return;
                    case ADJUST:
                        this.mPhotoEditor.setFilterEffect("");
                        this.compareAdjust.setVisibility(View.GONE);
                        slideDown(this.adjustLayout);
                        slideUp(this.mRvTools);
                        slideDownSaveView();
                        this.currentMode = ToolType.NONE;
                        return;
                    case STICKER:
                        if (this.mPhotoEditorView.getStickers().size() <= 0) {
                            slideUp(this.wrapStickerList);
                            slideDown(this.stickerLayout);
                            this.addNewSticker.setVisibility(View.GONE);
                            this.mPhotoEditorView.setHandlingSticker(null);
                            slideUp(this.mRvTools);
                            this.mPhotoEditorView.setLocked(true);
                            this.currentMode = ToolType.NONE;
                        } else if (this.addNewSticker.getVisibility() == View.VISIBLE) {
                            this.mPhotoEditorView.getStickers().clear();
                            this.addNewSticker.setVisibility(View.GONE);
                            this.mPhotoEditorView.setHandlingSticker(null);
                            slideUp(this.wrapStickerList);
                            slideDown(this.stickerLayout);
                            slideUp(this.mRvTools);
                            this.currentMode = ToolType.NONE;
                        } else {
                            slideDown(this.wrapStickerList);
                            this.addNewSticker.setVisibility(View.VISIBLE);
                        }
                        slideDownSaveView();
                        return;
                    case CROP:
                        showDiscardDialog();
                        return;
                    case NONE:
                        showDiscardDialog();
                        return;
                    default:
                        super.onBackPressed();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showDiscardDialog() {
        new AlertDialog.Builder(this).setMessage(R.string.dialog_discard_title).setPositiveButton(R.string.discard, (dialogInterface, i) -> {
            ImageEditorActivity.this.currentMode = null;
            ImageEditorActivity.this.finish();
            //TODO show inter ads


        }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).create().show();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onAdjustSelected(AdjustAdapter.AdjustModel adjustModel) {
        Log.d("XXXXXXXX", "onAdjustSelected " + adjustModel.slierIntensity + " " + this.adjustSeekBar.getMax());
        this.adjustSeekBar.setProgress((int) (adjustModel.slierIntensity * ((float) this.adjustSeekBar.getMax())));
    }

    public void addSticker(Bitmap bitmap) {
        this.mPhotoEditorView.addSticker(new DrawableSticker(new BitmapDrawable(getResources(), bitmap)));
        slideDown(this.wrapStickerList);
        this.addNewSticker.setVisibility(View.VISIBLE);
    }

    public void finishCrop(Bitmap bitmap) {
        this.mPhotoEditorView.setImageSource(bitmap);
        this.currentMode = ToolType.NONE;
        updateLayout();
    }

    public void onColorChanged(String str) {
        this.mPhotoEditor.setBrushColor(Color.parseColor(str));
    }

    public void instaSavedBitmap(Bitmap bitmap) {
        this.mPhotoEditorView.setImageSource(bitmap);
        this.currentMode = ToolType.NONE;
        updateLayout();
    }

    class ShowInstaDialog extends AsyncTask<Void, Bitmap, Bitmap> {
        ShowInstaDialog() {
        }

        public void onPreExecute() {
            ImageEditorActivity.this.showLoading(true);
        }


        public Bitmap doInBackground(Void... voidArr) {
            return FilterUtils.getBlurImageFromBitmap(ImageEditorActivity.this.mPhotoEditorView.getCurrentBitmap(), 5.0f);
        }


        public void onPostExecute(Bitmap bitmap) {
            ImageEditorActivity.this.showLoading(false);
            InstaDialog.show(ImageEditorActivity.this, ImageEditorActivity.this, ImageEditorActivity.this.mPhotoEditorView.getCurrentBitmap(), bitmap);
        }
    }


    class SaveFilterAsBitmap extends AsyncTask<Void, Void, Bitmap> {
        SaveFilterAsBitmap() {
        }

        public void onPreExecute() {
            ImageEditorActivity.this.showLoading(true);
        }

        public Bitmap doInBackground(Void... voidArr) {
            final Bitmap[] bitmapArr = {null};
            ImageEditorActivity.this.mPhotoEditorView.saveGLSurfaceViewAsBitmap(bitmap -> bitmapArr[0] = bitmap);
            while (bitmapArr[0] == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return bitmapArr[0];
        }

        public void onPostExecute(Bitmap bitmap) {
            ImageEditorActivity.this.mPhotoEditorView.setImageSource(bitmap);
            ImageEditorActivity.this.mPhotoEditorView.setFilterEffect("");
            ImageEditorActivity.this.showLoading(false);
        }
    }

    class SaveStickerAsBitmap extends AsyncTask<Void, Void, Bitmap> {
        SaveStickerAsBitmap() {
        }

        public void onPreExecute() {
            ImageEditorActivity.this.mPhotoEditorView.getGLSurfaceView().setAlpha(0.0f);
            ImageEditorActivity.this.showLoading(true);
        }

        public Bitmap doInBackground(Void... voidArr) {
            final Bitmap[] bitmapArr = {null};
            while (bitmapArr[0] == null) {
                try {
                    ImageEditorActivity.this.mPhotoEditor.saveStickerAsBitmap(bitmap -> bitmapArr[0] = bitmap);
                    while (bitmapArr[0] == null) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                }
            }
            return bitmapArr[0];
        }


        public void onPostExecute(Bitmap bitmap) {
            ImageEditorActivity.this.mPhotoEditorView.setImageSource(bitmap);
            ImageEditorActivity.this.mPhotoEditorView.getStickers().clear();
            ImageEditorActivity.this.mPhotoEditorView.getGLSurfaceView().setAlpha(1.0f);
            ImageEditorActivity.this.showLoading(false);
            ImageEditorActivity.this.updateLayout();
        }
    }


    public void onActivityResult(int i, int i2, @Nullable Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 123) {
            if (i2 == -1) {
                try {
                    InputStream openInputStream = getContentResolver().openInputStream(intent.getData());
                    Bitmap decodeStream = BitmapFactory.decodeStream(openInputStream);
                    float width = (float) decodeStream.getWidth();
                    float height = (float) decodeStream.getHeight();
                    float max = Math.max(width / 1280.0f, height / 1280.0f);
                    if (max > 1.0f) {
                        decodeStream = Bitmap.createScaledBitmap(decodeStream, (int) (width / max), (int) (height / max), false);
                    }
                    if (SystemUtil.rotateBitmap(decodeStream, new ExifInterface(openInputStream).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) != decodeStream) {
                        decodeStream.recycle();
                        decodeStream = null;
                    }
                    this.mPhotoEditorView.setImageSource(decodeStream);
                    updateLayout();
                } catch (Exception e) {
                    e.printStackTrace();
                    MsgUtil.toastMsg(this, "Error: Can not open image");
                }
            } else {
                finish();
            }
        }
    }

    class OnLoadBitmapFromUri extends AsyncTask<String, Bitmap, Bitmap> {
        OnLoadBitmapFromUri() {
        }

        public void onPreExecute() {
            ImageEditorActivity.this.showLoading(true);
        }

        public Bitmap doInBackground(String... strArr) {
            try {
                Uri fromFile = Uri.fromFile(new File(strArr[0]));
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(ImageEditorActivity.this.getContentResolver(), fromFile);
                float width = (float) bitmap.getWidth();
                float height = (float) bitmap.getHeight();
                float max = Math.max(width / 1280.0f, height / 1280.0f);
                if (max > 1.0f) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) (width / max), (int) (height / max), false);
                }
                Bitmap rotateBitmap = SystemUtil.rotateBitmap(bitmap, new ExifInterface(ImageEditorActivity.this.getContentResolver().openInputStream(fromFile)).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1));
                if (rotateBitmap != bitmap) {
                    bitmap.recycle();
                }
                return rotateBitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public void onPostExecute(Bitmap bitmap) {
            ImageEditorActivity.this.mPhotoEditorView.setImageSource(bitmap);
            ImageEditorActivity.this.updateLayout();
        }
    }

    public void updateLayout() {
        this.mPhotoEditorView.postDelayed(() -> {
            try {
                Display defaultDisplay = ImageEditorActivity.this.getWindowManager().getDefaultDisplay();
                Point point = new Point();
                defaultDisplay.getSize(point);
                int i = point.x;
                int height = ImageEditorActivity.this.wrapPhotoView.getHeight();
                int i2 = ImageEditorActivity.this.mPhotoEditorView.getGLSurfaceView().getRenderViewport().width;
                float f = (float) ImageEditorActivity.this.mPhotoEditorView.getGLSurfaceView().getRenderViewport().height;
                float f2 = (float) i2;
                if (((int) ((((float) i) * f) / f2)) <= height) {
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -2);
                    layoutParams.addRule(13);
                    ImageEditorActivity.this.mPhotoEditorView.setLayoutParams(layoutParams);
                    ImageEditorActivity.this.mPhotoEditorView.setVisibility(View.VISIBLE);
                } else {
                    RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams((int) ((((float) height) * f2) / f), -1);
                    layoutParams2.addRule(13);
                    ImageEditorActivity.this.mPhotoEditorView.setLayoutParams(layoutParams2);
                    ImageEditorActivity.this.mPhotoEditorView.setVisibility(View.VISIBLE);
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            ImageEditorActivity.this.showLoading(false);
        }, 300);
    }

    class SaveBitmapAsFile extends AsyncTask<Void, String, String> {

        SaveBitmapAsFile() {
        }

        public void onPreExecute() {
            ImageEditorActivity.this.showLoading(true);
        }

        public String doInBackground(Void... voidArr) {

            if(path.isEmpty())
                path = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());

            File image;
            try {
                image = FileUtils.saveImage( ImageEditorActivity.this.mPhotoEditorView.getCurrentBitmap(), path, false);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return image.getAbsolutePath();
        }

        public void onPostExecute(String str) {
            ImageEditorActivity.this.showLoading(false);
            if (str == null) {
                Toast.makeText(ImageEditorActivity.this.getApplicationContext(), "Oop! Something went wrong", Toast.LENGTH_LONG).show();
                return;
            } else {
                 Toast.makeText(ImageEditorActivity.this.getApplicationContext(), "Saved!", Toast.LENGTH_LONG).show();

                 Intent returnIntent = new Intent();
                 returnIntent.putExtra("path", path);
                 setResult(Activity.RESULT_OK, returnIntent);
                 ImageEditorActivity.this.finish();
            }

            /**  Intent intent = new Intent(EditImageActivity.this, SaveAndShareActivity.class);
             intent.putExtra("path", str);
             EditImageActivity.this.startActivity(intent); **/
        }
    }

    public void makeFullScreen() {
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
    }
}
