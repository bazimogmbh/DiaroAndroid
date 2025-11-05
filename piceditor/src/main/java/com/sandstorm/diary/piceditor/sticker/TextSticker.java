package com.sandstorm.diary.piceditor.sticker;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sandstorm.diary.piceditor.features.addtext.AddTextProperties;
import com.sandstorm.diary.piceditor.utils.SystemUtil;

public class TextSticker extends Sticker {

    private AddTextProperties addTextProperties;

    private int backgroundAlpha;

    private int backgroundBorder;

    private int backgroundColor;

    private BitmapDrawable backgroundDrawable;

    private final Context context;

    private Drawable drawable;

    private boolean isShowBackground;

    private float lineSpacingExtra = 0.0F;

    private float lineSpacingMultiplier = 1.0F;

    private float maxTextSizePixels;

    private float minTextSizePixels;

    private int paddingHeight;

    private int paddingWidth;

    private StaticLayout staticLayout;

    private String text;

    private Layout.Alignment textAlign;

    private int textAlpha;

    private int textColor;

    private int textHeight;

    private final TextPaint textPaint;

    private AddTextProperties.TextShadow textShadow;

    private int textWidth;


    public TextSticker(@NonNull Context paramContext, AddTextProperties paramAddTextProperties) {
        this.context = paramContext;
        this.addTextProperties = paramAddTextProperties;
        this.textPaint = new TextPaint(1);
        TextSticker textSticker = setTextSize(paramAddTextProperties.getTextSize()).setTextWidth(paramAddTextProperties.getTextWidth()).setTextHeight(paramAddTextProperties.getTextHeight()).setText(paramAddTextProperties.getText()).setPaddingWidth(SystemUtil.dpToPx(paramContext, paramAddTextProperties.getPaddingWidth())).setBackgroundBorder(SystemUtil.dpToPx(paramContext, paramAddTextProperties.getBackgroundBorder())).setTextShadow(paramAddTextProperties.getTextShadow()).setTextColor(paramAddTextProperties.getTextColor()).setTextAlpha(paramAddTextProperties.getTextAlpha()).setBackgroundColor(paramAddTextProperties.getBackgroundColor()).setBackgroundAlpha(paramAddTextProperties.getBackgroundAlpha()).setShowBackground(paramAddTextProperties.isShowBackground()).setTextColor(paramAddTextProperties.getTextColor());
        AssetManager assetManager = paramContext.getAssets();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("fonts/");
        stringBuilder.append(paramAddTextProperties.getFontName());
        textSticker.setTypeface(Typeface.createFromAsset(assetManager, stringBuilder.toString())).setTextAlign(paramAddTextProperties.getTextAlign()).setTextShare(paramAddTextProperties.getTextShader()).resizeText();
    }

    private float convertSpToPx(float paramFloat) {
        return paramFloat * (this.context.getResources().getDisplayMetrics()).scaledDensity;
    }

    public void draw(@NonNull Canvas paramCanvas) {
        Matrix matrix = getMatrix();
        paramCanvas.save();
        paramCanvas.concat(matrix);
        if (this.isShowBackground) {
            Paint paint = new Paint();
            if (this.backgroundDrawable != null) {
                paint.setShader(new BitmapShader(this.backgroundDrawable.getBitmap(), Shader.TileMode.MIRROR, Shader.TileMode.MIRROR));
                paint.setAlpha(this.backgroundAlpha);
            } else {
                paint.setARGB(this.backgroundAlpha, Color.red(this.backgroundColor), Color.green(this.backgroundColor), Color.blue(this.backgroundColor));
            }
            paramCanvas.drawRoundRect(0.0F, 0.0F, this.textWidth, this.textHeight, this.backgroundBorder, this.backgroundBorder, paint);
            paramCanvas.restore();
            paramCanvas.save();
            paramCanvas.concat(matrix);
        }
        paramCanvas.restore();
        paramCanvas.save();
        paramCanvas.concat(matrix);
        int i = this.paddingWidth;
        int j = this.textHeight / 2;
        int k = this.staticLayout.getHeight() / 2;
        paramCanvas.translate(i, (j - k));
        this.staticLayout.draw(paramCanvas);
        paramCanvas.restore();
        paramCanvas.save();
        paramCanvas.concat(matrix);
        paramCanvas.restore();
    }

    public AddTextProperties getAddTextProperties() {
        return this.addTextProperties;
    }

    public int getAlpha() {
        return this.textPaint.getAlpha();
    }


    @NonNull
    public Drawable getDrawable() {
        return this.drawable;
    }

    public int getHeight() {
        return this.textHeight;
    }


    @Nullable
    public String getText() {
        return this.text;
    }

    public int getWidth() {
        return this.textWidth;
    }

    public void release() {
        super.release();
        if (this.drawable != null)
            this.drawable = null;
    }

    @NonNull
    public TextSticker resizeText() {
        String str = getText();
        if (str != null) {
            if (str.length() <= 0)
                return this;
            if (this.textShadow != null)
                this.textPaint.setShadowLayer(this.textShadow.getRadius(), this.textShadow.getDx(), this.textShadow.getDy(), this.textShadow.getColorShadow());
            this.textPaint.setTextAlign(Paint.Align.LEFT);
            this.textPaint.setARGB(this.textAlpha, Color.red(this.textColor), Color.green(this.textColor), Color.blue(this.textColor));
            int i = this.textWidth - this.paddingWidth * 2;
            if (i <= 0)
                i = 100;
            StaticLayout.Builder builder = StaticLayout.Builder.obtain(this.text, 0, this.text.length(), textPaint, i);
            this.staticLayout = builder.build();
            return this;
        }
        return this;
    }

    @NonNull
    public TextSticker setAlpha(@IntRange(from = 0L, to = 255L) int paramInt) {
        this.textPaint.setAlpha(paramInt);
        return this;
    }

    public TextSticker setBackgroundAlpha(int paramInt) {
        this.backgroundAlpha = paramInt;
        return this;
    }

    public TextSticker setBackgroundBorder(int paramInt) {
        this.backgroundBorder = paramInt;
        return this;
    }

    public TextSticker setBackgroundColor(int paramInt) {
        this.backgroundColor = paramInt;
        return this;
    }


    public TextSticker setDrawable(@NonNull Drawable paramDrawable) {
        this.drawable = paramDrawable;
        return this;
    }


    public TextSticker setPaddingWidth(int paramInt) {
        this.paddingWidth = paramInt;
        return this;
    }


    public TextSticker setShowBackground(boolean paramBoolean) {
        this.isShowBackground = paramBoolean;
        return this;
    }

    @NonNull
    public TextSticker setText(@Nullable String paramString) {
        this.text = paramString;
        return this;
    }

    @NonNull
    public TextSticker setTextAlign(@NonNull int paramInt) {
        switch (paramInt) {
            default:
                return this;
            case 4:
                this.textAlign = Layout.Alignment.ALIGN_CENTER;
                return this;
            case 3:
                this.textAlign = Layout.Alignment.ALIGN_OPPOSITE;
                return this;
            case 2:
                break;
        }
        this.textAlign = Layout.Alignment.ALIGN_NORMAL;
        return this;
    }

    public TextSticker setTextAlpha(int paramInt) {
        this.textAlpha = paramInt;
        return this;
    }

    @NonNull
    public TextSticker setTextColor(@ColorInt int paramInt) {
        this.textColor = paramInt;
        return this;
    }

    public TextSticker setTextHeight(int paramInt) {
        this.textHeight = paramInt;
        return this;
    }

    public TextSticker setTextShadow(AddTextProperties.TextShadow paramTextShadow) {
        this.textShadow = paramTextShadow;
        return this;
    }

    @NonNull
    public TextSticker setTextShare(@Nullable Shader paramShader) {
        this.textPaint.setShader(paramShader);
        return this;
    }

    @NonNull
    public TextSticker setTextSize(int paramInt) {
        this.textPaint.setTextSize(convertSpToPx(paramInt));
        return this;
    }

    public TextSticker setTextWidth(int paramInt) {
        this.textWidth = paramInt;
        return this;
    }

    @NonNull
    public TextSticker setTypeface(@Nullable Typeface paramTypeface) {
        this.textPaint.setTypeface(paramTypeface);
        return this;
    }
}
