package com.sandstorm.diary.piceditor.activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
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
import com.sandstorm.diary.piceditor.features.collage.CollageView;
import com.sandstorm.diary.piceditor.features.collage.PuzzleLayout;
import com.sandstorm.diary.piceditor.features.collage.PuzzleLayoutParser;
import com.sandstorm.diary.piceditor.features.collage.PuzzlePiece;
import com.sandstorm.diary.piceditor.features.collage.adapter.PuzzleAdapter;
import com.sandstorm.diary.piceditor.features.collage.adapter.PuzzleBackgroundAdapter;
import com.sandstorm.diary.piceditor.features.collage.photopicker.activity.PickImageActivity;
import com.sandstorm.diary.piceditor.features.crop.CropDialogFragment;
import com.sandstorm.diary.piceditor.features.crop.adapter.AspectRatioPreviewAdapter;
import com.sandstorm.diary.piceditor.features.picker.PhotoPicker;
import com.sandstorm.diary.piceditor.features.picker.utils.PermissionsUtils;
import com.sandstorm.diary.piceditor.features.sticker.adapter.RecyclerTabLayout;
import com.sandstorm.diary.piceditor.features.sticker.adapter.StickerAdapter;
import com.sandstorm.diary.piceditor.features.sticker.adapter.TopTabAdapter;
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
import com.sandstorm.diary.piceditor.tools.PieceToolsAdapter;
import com.sandstorm.diary.piceditor.tools.ToolType;
import com.sandstorm.diary.piceditor.utils.AssetUtils;
import com.sandstorm.diary.piceditor.utils.CollageUtils;
import com.sandstorm.diary.piceditor.utils.FileUtils;
import com.sandstorm.diary.piceditor.utils.FilterUtils;
import com.sandstorm.diary.piceditor.utils.SharePreferenceUtil;
import com.sandstorm.diary.piceditor.utils.SystemUtil;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.steelkiwi.cropiwa.AspectRatio;

import org.wysaid.nativePort.CGENativeLibrary;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CollageActivity extends AppCompatActivity implements EditingToolsAdapter.OnItemSelected, AspectRatioPreviewAdapter.OnNewSelectedListener, StickerAdapter.OnClickStickerListener,
        PuzzleBackgroundAdapter.BackgroundChangeListener, CropDialogFragment.OnCropPhoto, PieceToolsAdapter.OnPieceFuncItemSelected, PuzzleAdapter.OnItemClickListener {

    private static CollageActivity instance;
    public CollageActivity puzzle;
    public ImageView addNewSticker, addNewText;
    private View adsContainer;
    public ConstraintLayout changeBackgroundLayout;
    private LinearLayout changeBorder;
    public ConstraintLayout changeLayoutLayout;
    public AspectRatio currentAspect;
    public PuzzleBackgroundAdapter.SquareView currentBackgroundState;
    public ToolType currentMode;
    public int deviceWidth = 0;
    public ConstraintLayout filterLayout;
    private KeyboardHeightProvider keyboardHeightProvider;
    private RelativeLayout loadingView;
    public List<String> lstPaths;

    private final EditingToolsAdapter mEditingToolsAdapter = new EditingToolsAdapter(this, true);

    public RecyclerView mRvTools;
    private ConstraintLayout mainActivity;

    public float pieceBorderRadius;
    public float piecePadding;
    private PieceToolsAdapter pieceToolsAdapter = new PieceToolsAdapter(this);

    public PuzzleLayout puzzleLayout;
    private RecyclerView puzzleList;

    public CollageView collageView;
    private RecyclerView radiusLayout, rvBackgroundBlur, rvBackgroundColor, rvBackgroundGradient;
    public RecyclerView rvPieceControl;
    private TextView saveBitmap;
    private ConstraintLayout saveControl;
    private SeekBar sbChangeBorderRadius;
    private SeekBar sbChangeBorderSize;

    public SeekBar stickerAlpha;

    public ConstraintLayout stickerLayout;

    public List<Target> targets = new ArrayList();

    public TextEditorDialogFragment.TextEditor textEditor;
    public TextEditorDialogFragment textEditorDialogFragment;

    public ConstraintLayout textLayout;
    private TextView tvChangeBackgroundBlur, tvChangeBackgroundColor, tvChangeBackgroundGradient,tvChangeBorder, tvChangeLayout, tvChangeRatio;

    private ConstraintLayout wrapPuzzleView;
    public LinearLayout wrapStickerList;

    public CGENativeLibrary.LoadImageCallback mLoadImageCallback = new CGENativeLibrary.LoadImageCallback() {
        public Bitmap loadImage(String str, Object obj) {
            try {
                return BitmapFactory.decodeStream(CollageActivity.this.getAssets().open(str));
            } catch (IOException io) {
                return null;
            }
        }

        public void loadImageOK(Bitmap bitmap, Object obj) {
            bitmap.recycle();
        }
    };

    public static CollageActivity getInstance() {
        return instance;
    }

    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.collage_layout);

        // Enable edge-to-edge display for Android 15+
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        if (Build.VERSION.SDK_INT < 26) {
            getWindow().setSoftInputMode(48);
        }
        this.adsContainer = findViewById(R.id.adsContainer);
        this.deviceWidth = getResources().getDisplayMetrics().widthPixels;

        findViewById(R.id.exitEditMode).setOnClickListener(view -> CollageActivity.this.onBackPressed());
        this.loadingView = findViewById(R.id.loadingView);
        this.collageView = findViewById(R.id.puzzle_view);
        this.wrapPuzzleView = findViewById(R.id.wrapPuzzleView);
        this.filterLayout = findViewById(R.id.filterLayout);
        this.mRvTools = findViewById(R.id.rvConstraintTools);
        this.mRvTools.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.mRvTools.setAdapter(this.mEditingToolsAdapter);
        this.rvPieceControl = findViewById(R.id.rvPieceControl);
        this.rvPieceControl.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.rvPieceControl.setAdapter(this.pieceToolsAdapter);
        this.sbChangeBorderSize = findViewById(R.id.sk_border);
        this.sbChangeBorderSize.setOnSeekBarChangeListener(this.onSeekBarChangeListener);
        this.sbChangeBorderRadius = findViewById(R.id.sk_border_radius);
        this.sbChangeBorderRadius.setOnSeekBarChangeListener(this.onSeekBarChangeListener);
        this.lstPaths = getIntent().getStringArrayListExtra(PickImageActivity.KEY_DATA_RESULT);
        this.puzzleLayout = CollageUtils.getPuzzleLayouts(this.lstPaths.size()).get(0);
        this.collageView.setPuzzleLayout(this.puzzleLayout);
        this.collageView.setTouchEnable(true);
        this.collageView.setNeedDrawLine(false);
        this.collageView.setNeedDrawOuterLine(false);
        this.collageView.setLineSize(4);
        this.collageView.setPiecePadding(6.0f);
        this.collageView.setPieceRadian(15.0f);
        this.collageView.setLineColor(ContextCompat.getColor(this, R.color.white));
        this.collageView.setSelectedLineColor(ContextCompat.getColor(this, R.color.colorAccent));
        this.collageView.setHandleBarColor(ContextCompat.getColor(this, R.color.colorAccent));
        this.collageView.setAnimateDuration(300);
        this.collageView.setOnPieceSelectedListener((puzzlePiece, i) -> {
            CollageActivity.this.slideDown(CollageActivity.this.mRvTools);
            CollageActivity.this.slideUp(CollageActivity.this.rvPieceControl);
            CollageActivity.this.slideUpSaveView();
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) CollageActivity.this.rvPieceControl.getLayoutParams();
            layoutParams.bottomMargin = SystemUtil.dpToPx(CollageActivity.this.getApplicationContext(), 10);
            CollageActivity.this.rvPieceControl.setLayoutParams(layoutParams);
            CollageActivity.this.currentMode = ToolType.PIECE;
        });
        this.collageView.setOnPieceUnSelectedListener(() -> {
            slideDown(rvPieceControl);
            slideUp(mRvTools);
            slideDownSaveView();
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) rvPieceControl.getLayoutParams();
            layoutParams.bottomMargin = 0;
            rvPieceControl.setLayoutParams(layoutParams);
            currentMode = ToolType.NONE;
        });
        this.saveControl = findViewById(R.id.saveControl);
        this.collageView.post(CollageActivity.this::loadPhoto);
        findViewById(R.id.imgCloseLayout).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgSaveLayout).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgCloseSticker).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgCloseFilter).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgCloseBackground).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgSaveSticker).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgCloseText).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgSaveText).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgSaveFilter).setOnClickListener(this.onClickListener);
        findViewById(R.id.imgSaveBackground).setOnClickListener(this.onClickListener);
        this.changeLayoutLayout = findViewById(R.id.changeLayoutLayout);
        this.changeBorder = findViewById(R.id.change_border);
        this.tvChangeLayout = findViewById(R.id.tv_change_layout);
        this.tvChangeLayout.setOnClickListener(this.onClickListener);
        this.tvChangeBorder = findViewById(R.id.tv_change_border);
        this.tvChangeBorder.setOnClickListener(this.onClickListener);
        this.tvChangeRatio = findViewById(R.id.tv_change_ratio);
        this.tvChangeRatio.setOnClickListener(this.onClickListener);
        this.tvChangeBackgroundColor = findViewById(R.id.tv_color);
        this.tvChangeBackgroundColor.setOnClickListener(this.onClickListener);
        this.tvChangeBackgroundGradient = findViewById(R.id.tv_radian);
        this.tvChangeBackgroundGradient.setOnClickListener(this.onClickListener);
        this.tvChangeBackgroundBlur = findViewById(R.id.tv_blur);
        this.tvChangeBackgroundBlur.setOnClickListener(this.onClickListener);
        PuzzleAdapter puzzleAdapter = new PuzzleAdapter();
        this.puzzleList = findViewById(R.id.puzzleList);
        this.puzzleList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.puzzleList.setAdapter(puzzleAdapter);
        puzzleAdapter.refreshData(CollageUtils.getPuzzleLayouts(this.lstPaths.size()), null);
        puzzleAdapter.setOnItemClickListener(this);
        AspectRatioPreviewAdapter aspectRatioPreviewAdapter = new AspectRatioPreviewAdapter(true);
        aspectRatioPreviewAdapter.setListener(this);
        this.radiusLayout = findViewById(R.id.radioLayout);
        this.radiusLayout.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        this.radiusLayout.setAdapter(aspectRatioPreviewAdapter);
        this.textLayout = findViewById(R.id.textControl);
        this.addNewText = findViewById(R.id.addNewText);
        this.addNewText.setVisibility(View.GONE);
        this.addNewText.setOnClickListener(view -> {
            collageView.setHandlingSticker(null);
            openTextFragment();
        });
        this.wrapStickerList = findViewById(R.id.wrapStickerList);
        ViewPager viewPager = findViewById(R.id.sticker_viewpaper);
        this.stickerLayout = findViewById(R.id.stickerLayout);
        this.stickerAlpha = findViewById(R.id.stickerAlpha);
        this.stickerAlpha.setVisibility(View.GONE);
        this.stickerAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                Sticker currentSticker = CollageActivity.this.collageView.getCurrentSticker();
                if (currentSticker != null) {
                    currentSticker.setAlpha(i);
                }
            }
        });
        this.saveBitmap = findViewById(R.id.save);
        this.saveBitmap.setOnClickListener(view -> {
                Bitmap createBitmap = FileUtils.createBitmap(CollageActivity.this.collageView, 1920);
                Bitmap createBitmap2 = CollageActivity.this.collageView.createBitmap();
                new SavePuzzleAsFile().execute(createBitmap, createBitmap2);

        });
        this.addNewSticker = findViewById(R.id.addNewSticker);
        this.addNewSticker.setVisibility(View.GONE);
        this.addNewSticker.setOnClickListener(view -> {
            addNewSticker.setVisibility(View.GONE);
            slideUp(wrapStickerList);
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
        this.collageView.setIcons(Arrays.asList(bitmapStickerIcon, bitmapStickerIcon2, bitmapStickerIcon3, bitmapStickerIcon5, bitmapStickerIcon4, bitmapStickerIcon6));
        this.collageView.setConstrained(true);
        this.collageView.setOnStickerOperationListener(this.onStickerOperationListener);
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
                View inflate = LayoutInflater.from(CollageActivity.this.getBaseContext()).inflate(R.layout.sticker_items, null, false);
                RecyclerView recyclerView = inflate.findViewById(R.id.rv);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new GridLayoutManager(CollageActivity.this.getApplicationContext(), 4));
                switch (i) {
                    case 0:
                        recyclerView.setAdapter(new StickerAdapter(CollageActivity.this.getApplicationContext(), AssetUtils.lstEmoj(), CollageActivity.this.deviceWidth, CollageActivity.this));
                        break;

                    case 1:
                        recyclerView.setAdapter(new StickerAdapter(CollageActivity.this.getApplicationContext(), AssetUtils.lstOthers(), CollageActivity.this.deviceWidth, CollageActivity.this));
                        break;
                }
                viewGroup.addView(inflate);
                return inflate;
            }
        });
        RecyclerTabLayout recyclerTabLayout = findViewById(R.id.recycler_tab_layout);
        recyclerTabLayout.setUpWithAdapter(new TopTabAdapter(viewPager, getApplicationContext()));
        recyclerTabLayout.setPositionThreshold(0.5f);
        recyclerTabLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_grey_more));
        this.changeBackgroundLayout = findViewById(R.id.changeBackgroundLayout);
        this.mainActivity = findViewById(R.id.puzzle_layout);
        this.changeLayoutLayout.setAlpha(0.0f);
        this.stickerLayout.setAlpha(0.0f);
        this.textLayout.setAlpha(0.0f);
        this.filterLayout.setAlpha(0.0f);
        this.changeBackgroundLayout.setAlpha(0.0f);
        this.rvPieceControl.setAlpha(0.0f);
        this.mainActivity.post(() -> {
            slideDown(changeLayoutLayout);
            slideDown(stickerLayout);
            slideDown(textLayout);
            slideDown(changeBackgroundLayout);
            slideDown(filterLayout);
            slideDown(rvPieceControl);
        });
        new Handler().postDelayed(() -> {
            changeLayoutLayout.setAlpha(1.0f);
            stickerLayout.setAlpha(1.0f);
            textLayout.setAlpha(1.0f);
            filterLayout.setAlpha(1.0f);
            changeBackgroundLayout.setAlpha(1.0f);
            rvPieceControl.setAlpha(1.0f);
        }, 1000);
        SharePreferenceUtil.setHeightOfKeyboard(getApplicationContext(), 0);
        this.keyboardHeightProvider = new KeyboardHeightProvider(this);
        this.keyboardHeightProvider.addKeyboardListener(i -> {

            if (i < 0) {
                SharePreferenceUtil.setHeightOfNotch(getApplicationContext(), -i);
            } else if (textEditorDialogFragment != null) {
                textEditorDialogFragment.updateAddTextBottomToolbarHeight(SharePreferenceUtil.getHeightOfNotch(getApplicationContext()) + i);
                SharePreferenceUtil.setHeightOfKeyboard(getApplicationContext(), i + SharePreferenceUtil.getHeightOfNotch(getApplicationContext()));
            }
        });
        showLoading(false);
        this.currentBackgroundState = new PuzzleBackgroundAdapter.SquareView(Color.parseColor("#ffffff"), "", true);
        this.rvBackgroundColor = findViewById(R.id.colorList);
        this.rvBackgroundColor.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
        this.rvBackgroundColor.setHasFixedSize(true);
        this.rvBackgroundColor.setAdapter(new PuzzleBackgroundAdapter(getApplicationContext(), this));
        this.rvBackgroundGradient = findViewById(R.id.radianList);
        this.rvBackgroundGradient.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
        this.rvBackgroundGradient.setHasFixedSize(true);
        this.rvBackgroundGradient.setAdapter(new PuzzleBackgroundAdapter(getApplicationContext(), (PuzzleBackgroundAdapter.BackgroundChangeListener) this, true));
        this.rvBackgroundBlur = findViewById(R.id.backgroundList);
        this.rvBackgroundBlur.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
        this.rvBackgroundBlur.setHasFixedSize(true);
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) this.collageView.getLayoutParams();
        layoutParams.height = point.x;
        layoutParams.width = point.x;
        this.collageView.setLayoutParams(layoutParams);
        this.currentAspect = new AspectRatio(1, 1);
        this.collageView.setAspectRatio(new AspectRatio(1, 1));
        puzzle = this;
        this.currentMode = ToolType.NONE;
        CGENativeLibrary.setLoadImageCallback(this.mLoadImageCallback, (Object) null);
        instance = this;

        // Apply window insets for edge-to-edge display
        setupWindowInsets();
    }

    private void setupWindowInsets() {
        // Apply top insets to the save control bar (status bar area)
        ViewCompat.setOnApplyWindowInsetsListener(this.saveControl, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });

        // Apply bottom insets to the main toolbar
        ViewCompat.setOnApplyWindowInsetsListener(this.mRvTools, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });

        // Apply bottom insets to piece control
        ViewCompat.setOnApplyWindowInsetsListener(this.rvPieceControl, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.bottomMargin = insets.bottom + (int) (10 * getResources().getDisplayMetrics().density);
            v.setLayoutParams(params);
            return windowInsets;
        });

        // Apply bottom insets to text control
        ViewCompat.setOnApplyWindowInsetsListener(this.textLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });

        // Apply bottom insets to sticker layout
        ViewCompat.setOnApplyWindowInsetsListener(this.stickerLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });

        // Apply bottom insets to change layout panel
        ViewCompat.setOnApplyWindowInsetsListener(this.changeLayoutLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });

        // Apply bottom insets to change background panel
        ViewCompat.setOnApplyWindowInsetsListener(this.changeBackgroundLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });

        // Apply bottom insets to filter layout
        ViewCompat.setOnApplyWindowInsetsListener(this.filterLayout, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });
    }

    public View.OnClickListener onClickListener = view -> {
        int id = view.getId();
        if (id == R.id.imgCloseBackground || id == R.id.imgCloseFilter || id == R.id.imgCloseLayout || id == R.id.imgCloseSticker || id == R.id.imgCloseText) {
            CollageActivity.this.slideDownSaveView();
            CollageActivity.this.onBackPressed();
        } else if (id == R.id.imgSaveBackground) {
            CollageActivity.this.slideDown(CollageActivity.this.changeBackgroundLayout);
            CollageActivity.this.slideUp(CollageActivity.this.mRvTools);
            CollageActivity.this.slideDownSaveView();
            CollageActivity.this.showDownFunction();
            CollageActivity.this.collageView.setLocked(true);
            CollageActivity.this.collageView.setTouchEnable(true);
            if (CollageActivity.this.collageView.getBackgroundResourceMode() == 0) {
                CollageActivity.this.currentBackgroundState.isColor = true;
                CollageActivity.this.currentBackgroundState.isBitmap = false;
                CollageActivity.this.currentBackgroundState.drawableId = ((ColorDrawable) CollageActivity.this.collageView.getBackground()).getColor();
                CollageActivity.this.currentBackgroundState.drawable = null;
            } else if (CollageActivity.this.collageView.getBackgroundResourceMode() == 1) {
                CollageActivity.this.currentBackgroundState.isColor = false;
                CollageActivity.this.currentBackgroundState.isBitmap = false;
                CollageActivity.this.currentBackgroundState.drawable = CollageActivity.this.collageView.getBackground();
            } else {
                CollageActivity.this.currentBackgroundState.isColor = false;
                CollageActivity.this.currentBackgroundState.isBitmap = true;
                CollageActivity.this.currentBackgroundState.drawable = CollageActivity.this.collageView.getBackground();
            }
            CollageActivity.this.currentMode = ToolType.NONE;
        } else if (id == R.id.imgSaveFilter) {
            CollageActivity.this.slideDown(CollageActivity.this.filterLayout);
            CollageActivity.this.slideUp(CollageActivity.this.mRvTools);
            CollageActivity.this.currentMode = ToolType.NONE;
        } else if (id == R.id.imgSaveLayout) {
            CollageActivity.this.slideUp(CollageActivity.this.mRvTools);
            CollageActivity.this.slideDown(CollageActivity.this.changeLayoutLayout);
            CollageActivity.this.slideDownSaveView();
            CollageActivity.this.showDownFunction();
            CollageActivity.this.puzzleLayout = CollageActivity.this.collageView.getPuzzleLayout();
            CollageActivity.this.pieceBorderRadius = CollageActivity.this.collageView.getPieceRadian();
            CollageActivity.this.piecePadding = CollageActivity.this.collageView.getPiecePadding();
            CollageActivity.this.collageView.setLocked(true);
            CollageActivity.this.collageView.setTouchEnable(true);
            CollageActivity.this.currentAspect = CollageActivity.this.collageView.getAspectRatio();
            CollageActivity.this.currentMode = ToolType.NONE;
        } else if (id == R.id.imgSaveSticker) {
            CollageActivity.this.collageView.setHandlingSticker(null);
            CollageActivity.this.stickerAlpha.setVisibility(View.GONE);
            CollageActivity.this.addNewSticker.setVisibility(View.GONE);
            CollageActivity.this.slideUp(CollageActivity.this.wrapStickerList);
            CollageActivity.this.slideDown(CollageActivity.this.stickerLayout);
            CollageActivity.this.slideUp(CollageActivity.this.mRvTools);
            CollageActivity.this.slideDownSaveView();
            CollageActivity.this.collageView.setLocked(true);
            CollageActivity.this.collageView.setTouchEnable(true);
            CollageActivity.this.currentMode = ToolType.NONE;
        } else if (id == R.id.imgSaveText) {
            CollageActivity.this.collageView.setHandlingSticker(null);
            CollageActivity.this.collageView.setLocked(true);
            CollageActivity.this.addNewText.setVisibility(View.GONE);
            CollageActivity.this.slideDown(CollageActivity.this.textLayout);
            CollageActivity.this.slideUp(CollageActivity.this.mRvTools);
            CollageActivity.this.slideDownSaveView();
            CollageActivity.this.collageView.setLocked(true);
            CollageActivity.this.collageView.setTouchEnable(true);
            CollageActivity.this.currentMode = ToolType.NONE;
        } else if (id == R.id.tv_blur) {
            CollageActivity.this.selectBackgroundBlur();
        } else if (id == R.id.tv_change_border) {
            CollageActivity.this.selectBorderTool();
        } else if (id == R.id.tv_change_layout) {
            CollageActivity.this.selectLayoutTool();
        } else if (id == R.id.tv_change_ratio) {
            CollageActivity.this.selectRadiusTool();
        } else if (id == R.id.tv_color) {
            CollageActivity.this.selectBackgroundColorTab();
        } else if (id == R.id.tv_radian) {
            CollageActivity.this.selectBackgroundGradientTab();
        }
    };
    public SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
            int id = seekBar.getId();
            if (id == R.id.sk_border) {
                CollageActivity.this.collageView.setPiecePadding((float) i);
            } else if (id == R.id.sk_border_radius) {
                CollageActivity.this.collageView.setPieceRadian((float) i);
            }
            CollageActivity.this.collageView.invalidate();
        }
    };
    StickerView.OnStickerOperationListener onStickerOperationListener = new StickerView.OnStickerOperationListener() {
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
            CollageActivity.this.stickerAlpha.setVisibility(View.VISIBLE);
            CollageActivity.this.stickerAlpha.setProgress(sticker.getAlpha());
        }

        public void onStickerClicked(@NonNull Sticker sticker) {
            CollageActivity.this.stickerAlpha.setVisibility(View.VISIBLE);
            CollageActivity.this.stickerAlpha.setProgress(sticker.getAlpha());
        }

        public void onStickerDeleted(@NonNull Sticker sticker) {
            CollageActivity.this.stickerAlpha.setVisibility(View.GONE);
        }

        public void onStickerTouchOutside() {
            CollageActivity.this.stickerAlpha.setVisibility(View.GONE);
        }

        public void onStickerDoubleTapped(@NonNull Sticker sticker) {
            if (sticker instanceof TextSticker) {
                sticker.setShow(false);
                CollageActivity.this.collageView.setHandlingSticker(null);
                CollageActivity.this.textEditorDialogFragment = TextEditorDialogFragment.show(CollageActivity.this, ((TextSticker) sticker).getAddTextProperties());
                CollageActivity.this.textEditor = new TextEditorDialogFragment.TextEditor() {
                    public void onDone(AddTextProperties addTextProperties) {
                        CollageActivity.this.collageView.getStickers().remove(CollageActivity.this.collageView.getLastHandlingSticker());
                        CollageActivity.this.collageView.addSticker(new TextSticker(CollageActivity.this, addTextProperties));
                    }

                    public void onBackButton() {
                        CollageActivity.this.collageView.showLastHandlingSticker();
                    }
                };
                CollageActivity.this.textEditorDialogFragment.setOnTextEditorListener(CollageActivity.this.textEditor);
            }
        }
    };


    private void openTextFragment() {
        this.textEditorDialogFragment = TextEditorDialogFragment.show(this);
        this.textEditor = new TextEditorDialogFragment.TextEditor() {
            public void onDone(AddTextProperties addTextProperties) {
                CollageActivity.this.collageView.addSticker(new TextSticker(CollageActivity.this.getApplicationContext(), addTextProperties));
            }

            public void onBackButton() {
                if (CollageActivity.this.collageView.getStickers().isEmpty()) {
                    CollageActivity.this.onBackPressed();
                }
            }
        };
        this.textEditorDialogFragment.setOnTextEditorListener(this.textEditor);
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

    public void selectBackgroundBlur() {
        ArrayList arrayList = new ArrayList();
        for (PuzzlePiece drawable : this.collageView.getPuzzlePieces()) {
            arrayList.add(drawable.getDrawable());
        }
        PuzzleBackgroundAdapter puzzleBackgroundAdapter = new PuzzleBackgroundAdapter(getApplicationContext(), this, (List<Drawable>) arrayList);
        puzzleBackgroundAdapter.setSelectedSquareIndex(-1);
        this.rvBackgroundBlur.setAdapter(puzzleBackgroundAdapter);
        this.rvBackgroundBlur.setVisibility(View.VISIBLE);
        this.tvChangeBackgroundBlur.setBackgroundResource(R.drawable.border_bottom);
        this.tvChangeBackgroundBlur.setTextColor(ContextCompat.getColor(this, R.color.white));
        this.rvBackgroundGradient.setVisibility(View.GONE);
        this.tvChangeBackgroundGradient.setBackgroundResource(0);
        this.tvChangeBackgroundGradient.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.rvBackgroundColor.setVisibility(View.GONE);
        this.tvChangeBackgroundColor.setBackgroundResource(0);
        this.tvChangeBackgroundColor.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
    }

    public void selectBackgroundColorTab() {
        this.rvBackgroundColor.setVisibility(View.VISIBLE);
        this.tvChangeBackgroundColor.setBackgroundResource(R.drawable.border_bottom);
        this.tvChangeBackgroundColor.setTextColor(ContextCompat.getColor(this, R.color.white));
        this.rvBackgroundColor.scrollToPosition(0);
        ((PuzzleBackgroundAdapter) this.rvBackgroundColor.getAdapter()).setSelectedSquareIndex(-1);
        this.rvBackgroundColor.getAdapter().notifyDataSetChanged();
        this.rvBackgroundGradient.setVisibility(View.GONE);
        this.tvChangeBackgroundGradient.setBackgroundResource(0);
        this.tvChangeBackgroundGradient.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.rvBackgroundBlur.setVisibility(View.GONE);
        this.tvChangeBackgroundBlur.setBackgroundResource(0);
        this.tvChangeBackgroundBlur.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
    }

    public void selectBackgroundGradientTab() {
        this.rvBackgroundGradient.setVisibility(View.VISIBLE);
        this.tvChangeBackgroundGradient.setBackgroundResource(R.drawable.border_bottom);
        this.tvChangeBackgroundGradient.setTextColor(ContextCompat.getColor(this, R.color.white));
        this.rvBackgroundGradient.scrollToPosition(0);
        ((PuzzleBackgroundAdapter) this.rvBackgroundGradient.getAdapter()).setSelectedSquareIndex(-1);
        this.rvBackgroundGradient.getAdapter().notifyDataSetChanged();
        this.rvBackgroundColor.setVisibility(View.GONE);
        this.tvChangeBackgroundColor.setBackgroundResource(0);
        this.tvChangeBackgroundColor.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.rvBackgroundBlur.setVisibility(View.GONE);
        this.tvChangeBackgroundBlur.setBackgroundResource(0);
        this.tvChangeBackgroundBlur.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
    }


    public void selectLayoutTool() {
        this.puzzleList.setVisibility(View.VISIBLE);
        this.tvChangeLayout.setBackgroundResource(R.drawable.border_bottom);
        this.tvChangeLayout.setTextColor(ContextCompat.getColor(this, R.color.white));
        this.changeBorder.setVisibility(View.GONE);
        this.tvChangeBorder.setBackgroundResource(0);
        this.tvChangeBorder.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.radiusLayout.setVisibility(View.GONE);
        this.tvChangeRatio.setBackgroundResource(0);
        this.tvChangeRatio.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
    }


    public void selectRadiusTool() {
        this.radiusLayout.setVisibility(View.VISIBLE);
        this.tvChangeRatio.setTextColor(ContextCompat.getColor(this, R.color.white));
        this.tvChangeRatio.setBackgroundResource(R.drawable.border_bottom);
        this.puzzleList.setVisibility(View.GONE);
        this.tvChangeLayout.setBackgroundResource(0);
        this.tvChangeLayout.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.changeBorder.setVisibility(View.GONE);
        this.tvChangeBorder.setBackgroundResource(0);
        this.tvChangeBorder.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
    }


    public void selectBorderTool() {
        this.changeBorder.setVisibility(View.VISIBLE);
        this.tvChangeBorder.setBackgroundResource(R.drawable.border_bottom);
        this.tvChangeBorder.setTextColor(ContextCompat.getColor(this, R.color.white));
        this.puzzleList.setVisibility(View.GONE);
        this.tvChangeLayout.setBackgroundResource(0);
        this.tvChangeLayout.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.radiusLayout.setVisibility(View.GONE);
        this.tvChangeRatio.setBackgroundResource(0);
        this.tvChangeRatio.setTextColor(ContextCompat.getColor(this, R.color.unselected_color));
        this.sbChangeBorderRadius.setProgress((int) this.collageView.getPieceRadian());
        this.sbChangeBorderSize.setProgress((int) this.collageView.getPiecePadding());
    }

    private void showUpFunction(View view) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this.mainActivity);
        constraintSet.connect(this.wrapPuzzleView.getId(), 3, this.adsContainer.getId(), 4, 0);
        constraintSet.connect(this.wrapPuzzleView.getId(), 1, this.mainActivity.getId(), 1, 0);
        constraintSet.connect(this.wrapPuzzleView.getId(), 4, view.getId(), 3, 0);
        constraintSet.connect(this.wrapPuzzleView.getId(), 2, this.mainActivity.getId(), 2, 0);
        constraintSet.applyTo(this.mainActivity);
    }


    public void showDownFunction() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this.mainActivity);
        constraintSet.connect(this.wrapPuzzleView.getId(), 3, this.adsContainer.getId(), 4, 0);
        constraintSet.connect(this.wrapPuzzleView.getId(), 1, this.mainActivity.getId(), 1, 0);
        constraintSet.connect(this.wrapPuzzleView.getId(), 4, this.mRvTools.getId(), 3, 0);
        constraintSet.connect(this.wrapPuzzleView.getId(), 2, this.mainActivity.getId(), 2, 0);
        constraintSet.applyTo(this.mainActivity);
    }

    public void onToolSelected(ToolType toolType) {
        this.currentMode = toolType;
        switch (toolType) {
            case LAYOUT:
                this.puzzleLayout = this.collageView.getPuzzleLayout();
                this.currentAspect = this.collageView.getAspectRatio();
                this.pieceBorderRadius = this.collageView.getPieceRadian();
                this.piecePadding = this.collageView.getPiecePadding();
                this.puzzleList.scrollToPosition(0);
                ((PuzzleAdapter) this.puzzleList.getAdapter()).setSelectedIndex(-1);
                this.puzzleList.getAdapter().notifyDataSetChanged();
                this.radiusLayout.scrollToPosition(0);
                ((AspectRatioPreviewAdapter) this.radiusLayout.getAdapter()).setLastSelectedView(-1);
                this.radiusLayout.getAdapter().notifyDataSetChanged();
                selectLayoutTool();
                slideUpSaveView();
                slideUp(this.changeLayoutLayout);
                slideDown(this.mRvTools);
                showUpFunction(this.changeLayoutLayout);
                this.collageView.setLocked(false);
                this.collageView.setTouchEnable(false);
                return;
            case BORDER:
                this.puzzleLayout = this.collageView.getPuzzleLayout();
                this.currentAspect = this.collageView.getAspectRatio();
                this.pieceBorderRadius = this.collageView.getPieceRadian();
                this.piecePadding = this.collageView.getPiecePadding();
                this.puzzleList.scrollToPosition(0);
                ((PuzzleAdapter) this.puzzleList.getAdapter()).setSelectedIndex(-1);
                this.puzzleList.getAdapter().notifyDataSetChanged();
                this.radiusLayout.scrollToPosition(0);
                ((AspectRatioPreviewAdapter) this.radiusLayout.getAdapter()).setLastSelectedView(-1);
                this.radiusLayout.getAdapter().notifyDataSetChanged();
                selectBorderTool();
                slideUpSaveView();
                slideUp(this.changeLayoutLayout);
                slideDown(this.mRvTools);
                showUpFunction(this.changeLayoutLayout);
                this.collageView.setLocked(false);
                this.collageView.setTouchEnable(false);
                return;
            case RATIO:
                this.puzzleLayout = this.collageView.getPuzzleLayout();
                this.currentAspect = this.collageView.getAspectRatio();
                this.pieceBorderRadius = this.collageView.getPieceRadian();
                this.piecePadding = this.collageView.getPiecePadding();
                this.puzzleList.scrollToPosition(0);
                ((PuzzleAdapter) this.puzzleList.getAdapter()).setSelectedIndex(-1);
                this.puzzleList.getAdapter().notifyDataSetChanged();
                this.radiusLayout.scrollToPosition(0);
                ((AspectRatioPreviewAdapter) this.radiusLayout.getAdapter()).setLastSelectedView(-1);
                this.radiusLayout.getAdapter().notifyDataSetChanged();
                selectRadiusTool();
                slideUpSaveView();
                slideUp(this.changeLayoutLayout);
                slideDown(this.mRvTools);
                showUpFunction(this.changeLayoutLayout);
                this.collageView.setLocked(false);
                this.collageView.setTouchEnable(false);
                return;
            case STICKER:
                this.collageView.setTouchEnable(false);
                slideUpSaveView();
                slideDown(this.mRvTools);
                slideUp(this.stickerLayout);
                this.collageView.setLocked(false);
                this.collageView.setTouchEnable(false);
                return;
            case TEXT:
                this.collageView.setTouchEnable(false);
                slideUpSaveView();
                this.collageView.setLocked(false);
                openTextFragment();
                slideDown(this.mRvTools);
                slideUp(this.textLayout);
                this.addNewText.setVisibility(View.VISIBLE);
                return;
            case BACKGROUND:
                this.collageView.setLocked(false);
                this.collageView.setTouchEnable(false);
                slideUpSaveView();
                selectBackgroundColorTab();
                slideDown(this.mRvTools);
                slideUp(this.changeBackgroundLayout);
                showUpFunction(this.changeBackgroundLayout);
                if (this.collageView.getBackgroundResourceMode() == 0) {
                    this.currentBackgroundState.isColor = true;
                    this.currentBackgroundState.isBitmap = false;
                    this.currentBackgroundState.drawableId = ((ColorDrawable) this.collageView.getBackground()).getColor();
                    return;
                } else if (this.collageView.getBackgroundResourceMode() == 2 || (this.collageView.getBackground() instanceof ColorDrawable)) {
                    this.currentBackgroundState.isBitmap = true;
                    this.currentBackgroundState.isColor = false;
                    this.currentBackgroundState.drawable = this.collageView.getBackground();
                    return;
                } else if (this.collageView.getBackground() instanceof GradientDrawable) {
                    this.currentBackgroundState.isBitmap = false;
                    this.currentBackgroundState.isColor = false;
                    this.currentBackgroundState.drawable = this.collageView.getBackground();
                    return;
                } else {
                    return;
                }
            default:
        }
    }


    public void loadPhoto() {
        final int i;
        final ArrayList arrayList = new ArrayList();
        if (this.lstPaths.size() > this.puzzleLayout.getAreaCount()) {
            i = this.puzzleLayout.getAreaCount();
        } else {
            i = this.lstPaths.size();
        }
        for (int i2 = 0; i2 < i; i2++) {
            Target r4 = new Target() {
                public void onBitmapFailed(Exception exc, Drawable drawable) {
                }

                public void onPrepareLoad(Drawable drawable) {
                }

                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                    int width = bitmap.getWidth();
                    float f = (float) width;
                    float height = (float) bitmap.getHeight();
                    float max = Math.max(f / f, height / f);
                    if (max > 1.0f) {
                        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (f / max), (int) (height / max), false);
                    }
                    arrayList.add(bitmap);
                    if (arrayList.size() == i) {
                        if (CollageActivity.this.lstPaths.size() < CollageActivity.this.puzzleLayout.getAreaCount()) {
                            for (int i = 0; i < CollageActivity.this.puzzleLayout.getAreaCount(); i++) {
                                CollageActivity.this.collageView.addPiece((Bitmap) arrayList.get(i % i));
                            }
                        } else {
                            CollageActivity.this.collageView.addPieces(arrayList);
                        }
                    }
                    CollageActivity.this.targets.remove(this);
                }
            };
            Picasso picasso = Picasso.get();
            picasso.load("file:///" + this.lstPaths.get(i2)).resize(this.deviceWidth, this.deviceWidth).centerInside().config(Bitmap.Config.RGB_565).into((Target) r4);
            this.targets.add(r4);
        }
    }

    public void slideUp(View view) {
        ObjectAnimator.ofFloat(view, "translationY", new float[]{(float) view.getHeight(), 0.0f}).start();
    }


    public void onDestroy() {
        super.onDestroy();
        try {
            this.collageView.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void slideUpSaveView() {
        this.saveControl.setVisibility(View.GONE);
    }

    public void slideDownSaveView() {
        this.saveControl.setVisibility(View.VISIBLE);
    }

    public void slideDown(View view) {
        ObjectAnimator.ofFloat(view, "translationY", 0.0f, (float) view.getHeight()).start();
    }

    public void onBackPressed() {
        if (this.currentMode == null) {
            super.onBackPressed();
            return;
        }
        try {
            switch (this.currentMode) {
                case LAYOUT:
                case BORDER:
                case RATIO:
                    slideDown(this.changeLayoutLayout);
                    slideUp(this.mRvTools);
                    slideDownSaveView();
                    showDownFunction();
                    this.collageView.updateLayout(this.puzzleLayout);
                    this.collageView.setPiecePadding(this.piecePadding);
                    this.collageView.setPieceRadian(this.pieceBorderRadius);
                    this.currentMode = ToolType.NONE;
                    getWindowManager().getDefaultDisplay().getSize(new Point());
                    onNewAspectRatioSelected(this.currentAspect);
                    this.collageView.setAspectRatio(this.currentAspect);
                    this.collageView.setLocked(true);
                    this.collageView.setTouchEnable(true);
                    return;
                case STICKER:
                    if (this.collageView.getStickers().size() <= 0) {
                        slideUp(this.wrapStickerList);
                        slideDown(this.stickerLayout);
                        this.addNewSticker.setVisibility(View.GONE);
                        this.collageView.setHandlingSticker((Sticker) null);
                        slideUp(this.mRvTools);
                        this.collageView.setLocked(true);
                        this.currentMode = ToolType.NONE;
                    } else if (this.addNewSticker.getVisibility() == View.VISIBLE) {
                        this.collageView.getStickers().clear();
                        this.addNewSticker.setVisibility(View.GONE);
                        this.collageView.setHandlingSticker(null);
                        slideUp(this.wrapStickerList);
                        slideDown(this.stickerLayout);
                        slideUp(this.mRvTools);
                        this.collageView.setLocked(true);
                        this.collageView.setTouchEnable(true);
                        this.currentMode = ToolType.NONE;
                    } else {
                        slideDown(this.wrapStickerList);
                        this.addNewSticker.setVisibility(View.VISIBLE);
                    }
                    slideDownSaveView();
                    return;
                case TEXT:
                    if (!this.collageView.getStickers().isEmpty()) {
                        this.collageView.getStickers().clear();
                        this.collageView.setHandlingSticker(null);
                    }
                    slideDown(this.textLayout);
                    this.addNewText.setVisibility(View.GONE);
                    this.collageView.setHandlingSticker(null);
                    slideUp(this.mRvTools);
                    slideDownSaveView();
                    this.collageView.setLocked(true);
                    this.currentMode = ToolType.NONE;
                    this.collageView.setTouchEnable(true);
                    return;
                case BACKGROUND:
                    slideUp(this.mRvTools);
                    slideDown(this.changeBackgroundLayout);
                    this.collageView.setLocked(true);
                    this.collageView.setTouchEnable(true);
                    if (this.currentBackgroundState.isColor) {
                        this.collageView.setBackgroundResourceMode(0);
                        this.collageView.setBackgroundColor(this.currentBackgroundState.drawableId);
                    } else if (this.currentBackgroundState.isBitmap) {
                        this.collageView.setBackgroundResourceMode(2);
                        this.collageView.setBackground(this.currentBackgroundState.drawable);
                    } else {
                        this.collageView.setBackgroundResourceMode(1);
                        if (this.currentBackgroundState.drawable != null) {
                            this.collageView.setBackground(this.currentBackgroundState.drawable);
                        } else {
                            this.collageView.setBackgroundResource(this.currentBackgroundState.drawableId);
                        }
                    }
                    slideDownSaveView();
                    showDownFunction();
                    this.currentMode = ToolType.NONE;
                    return;
                case PIECE:
                    slideDown(this.rvPieceControl);
                    slideUp(this.mRvTools);
                    slideDownSaveView();
                    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) this.rvPieceControl.getLayoutParams();
                    layoutParams.bottomMargin = 0;
                    this.rvPieceControl.setLayoutParams(layoutParams);
                    this.currentMode = ToolType.NONE;
                    this.collageView.setHandlingPiece(null);
                    this.collageView.setPreviousHandlingPiece(null);
                    this.collageView.invalidate();
                    this.currentMode = ToolType.NONE;
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

    private void showDiscardDialog() {
        new AlertDialog.Builder(this).setMessage(R.string.dialog_discard_title).setPositiveButton(R.string.discard, (dialogInterface, i) -> {
            CollageActivity.this.currentMode = null;
            CollageActivity.this.finish();
            //TODO show inter ads


        }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss()).create().show();
    }

    public void onItemClick(PuzzleLayout puzzleLayout2, int i) {
        PuzzleLayout parse = PuzzleLayoutParser.parse(puzzleLayout2.generateInfo());
        puzzleLayout2.setRadian(this.collageView.getPieceRadian());
        puzzleLayout2.setPadding(this.collageView.getPiecePadding());
        this.collageView.updateLayout(parse);
    }

    public void onNewAspectRatioSelected(AspectRatio aspectRatio) {
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        int[] calculateWidthAndHeight = calculateWidthAndHeight(aspectRatio, point);
        this.collageView.setLayoutParams(new ConstraintLayout.LayoutParams(calculateWidthAndHeight[0], calculateWidthAndHeight[1]));
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this.wrapPuzzleView);
        constraintSet.connect(this.collageView.getId(), 3, this.wrapPuzzleView.getId(), 3, 0);
        constraintSet.connect(this.collageView.getId(), 1, this.wrapPuzzleView.getId(), 1, 0);
        constraintSet.connect(this.collageView.getId(), 4, this.wrapPuzzleView.getId(), 4, 0);
        constraintSet.connect(this.collageView.getId(), 2, this.wrapPuzzleView.getId(), 2, 0);
        constraintSet.applyTo(this.wrapPuzzleView);
        this.collageView.setAspectRatio(aspectRatio);
    }

    public void replaceCurrentPiece(String str) {
        new OnLoadBitmapFromUri().execute(str);
    }

    private int[] calculateWidthAndHeight(AspectRatio aspectRatio, Point point) {
        int height = this.wrapPuzzleView.getHeight();
        if (aspectRatio.getHeight() > aspectRatio.getWidth()) {
            int ratio = (int) (aspectRatio.getRatio() * ((float) height));
            if (ratio < point.x) {
                return new int[]{ratio, height};
            }
            return new int[]{point.x, (int) (((float) point.x) / aspectRatio.getRatio())};
        }
        int ratio2 = (int) (((float) point.x) / aspectRatio.getRatio());
        if (ratio2 > height) {
            return new int[]{(int) (((float) height) * aspectRatio.getRatio()), height};
        }
        return new int[]{point.x, ratio2};
    }

    public void onPause() {
        super.onPause();
        this.keyboardHeightProvider.onPause();
    }

    public void onResume() {
        super.onResume();
        this.keyboardHeightProvider.onResume();
    }

    public void addSticker(Bitmap bitmap) {
        this.collageView.addSticker(new DrawableSticker(new BitmapDrawable(getResources(), bitmap)));
        slideDown(this.wrapStickerList);
        this.addNewSticker.setVisibility(View.VISIBLE);
    }

    public void onBackgroundSelected(final PuzzleBackgroundAdapter.SquareView squareView) {
        if (squareView.isColor) {
            this.collageView.setBackgroundColor(squareView.drawableId);
            this.collageView.setBackgroundResourceMode(0);
        } else if (squareView.drawable != null) {
            this.collageView.setBackgroundResourceMode(2);
            new AsyncTask<Void, Bitmap, Bitmap>() {
                public void onPreExecute() {
                    CollageActivity.this.showLoading(true);
                }

                public Bitmap doInBackground(Void... voidArr) {
                    return FilterUtils.getBlurImageFromBitmap(((BitmapDrawable) squareView.drawable).getBitmap(), 5.0f);
                }

                public void onPostExecute(Bitmap bitmap) {
                    CollageActivity.this.showLoading(false);
                    CollageActivity.this.collageView.setBackground(new BitmapDrawable(CollageActivity.this.getResources(), bitmap));
                }
            }.execute();
        } else {
            this.collageView.setBackgroundResource(squareView.drawableId);
            this.collageView.setBackgroundResourceMode(1);
        }
    }

    public void finishCrop(Bitmap bitmap) {
        this.collageView.replace(bitmap, "");
    }

    @Override
    public void onPieceFuncSelected(ToolType toolType) {
        switch (toolType) {
            case REPLACE:
                PhotoPicker.builder().setPhotoCount(1).setPreviewEnabled(false).setShowCamera(false).setForwardMain(true).start(this);
                return;
            case H_FLIP:
                this.collageView.flipHorizontally();
                return;
            case V_FLIP:
                this.collageView.flipVertically();
                return;
            case ROTATE:
                this.collageView.rotate(90.0f);
                return;
            case CROP:
                CropDialogFragment.show(this, this, ((BitmapDrawable) this.collageView.getHandlingPiece().getDrawable()).getBitmap());
        }
    }


    class OnLoadBitmapFromUri extends AsyncTask<String, Bitmap, Bitmap> {
        OnLoadBitmapFromUri() {
        }

        public void onPreExecute() {
            CollageActivity.this.showLoading(true);
        }

        public Bitmap doInBackground(String... strArr) {
            try {
                Uri fromFile = Uri.fromFile(new File(strArr[0]));

                Bitmap rotateBitmap = SystemUtil.rotateBitmap(MediaStore.Images.Media.getBitmap(CollageActivity.this.getContentResolver(), fromFile), new ExifInterface(CollageActivity.this.getContentResolver().openInputStream(fromFile)).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1));

                float width = (float) rotateBitmap.getWidth();
                float height = (float) rotateBitmap.getHeight();
                float max = Math.max(width / 1280.0f, height / 1280.0f);
                return max > 1.0f ? Bitmap.createScaledBitmap(rotateBitmap, (int) (width / max), (int) (height / max), false) : rotateBitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public void onPostExecute(Bitmap bitmap) {
            CollageActivity.this.showLoading(false);
            CollageActivity.this.collageView.replace(bitmap, "");
        }
    }

    class SavePuzzleAsFile extends AsyncTask<Bitmap, String, String> {
        SavePuzzleAsFile() {
        }

        public void onPreExecute() {
            CollageActivity.this.showLoading(true);
        }

        public String doInBackground(Bitmap... bitmapArr) {
            Bitmap bitmap = bitmapArr[0];
            Bitmap bitmap2 = bitmapArr[1];
            Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(createBitmap);
            canvas.drawBitmap(bitmap, null, new RectF(0.0f, 0.0f, (float) bitmap.getWidth(), (float) bitmap.getHeight()), (Paint) null);
            canvas.drawBitmap(bitmap2, null, new RectF(0.0f, 0.0f, (float) bitmap.getWidth(), (float) bitmap.getHeight()), (Paint) null);
            bitmap.recycle();
            bitmap2.recycle();
            String name = getExternalCacheDir() + "/collage.jpg";
            try {
                File image = FileUtils.saveImage(createBitmap, name, true);
                createBitmap.recycle();
                return image.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        public void onPostExecute(String str) {
            CollageActivity.this.showLoading(false);
            Intent returnIntent = new Intent();
            returnIntent.putExtra("path", str);
            returnIntent.setFlags(67108864);
            setResult(Activity.RESULT_OK, returnIntent);
            CollageActivity.this.finish();
        }
    }
}
