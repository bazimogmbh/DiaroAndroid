package com.sandstorm.diary.piceditor.features.addtext;

import android.graphics.Color;
import android.graphics.Shader;

import androidx.core.view.InputDeviceCompat;

import java.util.ArrayList;
import java.util.List;

public class AddTextProperties {
    private int backgroundAlpha;
    private int backgroundBorder;
    private int backgroundColor;
    private int backgroundColorIndex;
    private int fontIndex;
    private String fontName;
    private boolean isFullScreen;
    private boolean isShowBackground;
    private int paddingHeight;
    private int paddingWidth;
    private String text;
    private int textAlign;
    private int textAlpha;
    private int textColor;
    private int textColorIndex;
    private int textHeight;
    private Shader textShader;
    private int textShaderIndex;
    private TextShadow textShadow;
    private int textShadowIndex;
    private int textSize;
    private int textWidth;

    public static class TextShadow {
        private int colorShadow;
        private int x;
        private int y;
        private int radius;

        TextShadow(int i, int i2, int i3, int i4) {
            this.radius = i;
            this.x = i2;
            this.y = i3;
            this.colorShadow = i4;
        }

        public int getRadius() {
            return this.radius;
        }

        public void setRadius(int i) {
            this.radius = i;
        }

        public int getDx() {
            return this.x;
        }

        public int getDy() {
            return this.y;
        }

        public int getColorShadow() {
            return this.colorShadow;
        }
    }

   public static List<TextShadow> getLstTextShadow() {
        ArrayList<TextShadow> arrayList = new ArrayList<>();
        arrayList.add(new TextShadow(0, 0, 0, -16711681));
        arrayList.add(new TextShadow(8, 4, 4, Color.parseColor("#FF1493")));
        arrayList.add(new TextShadow(8, 4, 4, -65281));
        arrayList.add(new TextShadow(8, 4, 4, Color.parseColor("#24ffff")));
        arrayList.add(new TextShadow(8, 4, 4, InputDeviceCompat.SOURCE_ANY));
        arrayList.add(new TextShadow(8, 4, 4, -1));
        arrayList.add(new TextShadow(8, 4, 4, -7829368));
        arrayList.add(new TextShadow(8, -4, -4, Color.parseColor("#FF1493")));
        arrayList.add(new TextShadow(8, -4, -4, -65281));
        arrayList.add(new TextShadow(8, -4, -4, Color.parseColor("#24ffff")));
        arrayList.add(new TextShadow(8, -4, -4, InputDeviceCompat.SOURCE_ANY));
        arrayList.add(new TextShadow(8, -4, -4, -1));
        arrayList.add(new TextShadow(8, -4, -4, Color.parseColor("#696969")));
        arrayList.add(new TextShadow(8, -4, 4, Color.parseColor("#FF1493")));
        arrayList.add(new TextShadow(8, -4, 4, -65281));
        arrayList.add(new TextShadow(8, -4, 4, Color.parseColor("#24ffff")));
        arrayList.add(new TextShadow(8, -4, 4, InputDeviceCompat.SOURCE_ANY));
        arrayList.add(new TextShadow(8, -4, 4, -1));
        arrayList.add(new TextShadow(8, -4, 4, Color.parseColor("#696969")));
        arrayList.add(new TextShadow(8, 4, -4, Color.parseColor("#FF1493")));
        arrayList.add(new TextShadow(8, 4, -4, -65281));
        arrayList.add(new TextShadow(8, 4, -4, Color.parseColor("#24ffff")));
        arrayList.add(new TextShadow(8, 4, -4, InputDeviceCompat.SOURCE_ANY));
        arrayList.add(new TextShadow(8, 4, -4, -1));
        arrayList.add(new TextShadow(8, 4, -4, Color.parseColor("#696969")));
        return arrayList;
    }

    public static AddTextProperties getDefaultProperties() {
        AddTextProperties addTextProperties = new AddTextProperties();
        addTextProperties.setTextSize(30);
        addTextProperties.setTextAlign(4);
        addTextProperties.setFontName("36.ttf");
        addTextProperties.setTextColor(-1);
        addTextProperties.setTextAlpha(255);
        addTextProperties.setBackgroundAlpha(255);
        addTextProperties.setPaddingWidth(12);
        addTextProperties.setTextShaderIndex(7);
        addTextProperties.setBackgroundColorIndex(21);
        addTextProperties.setTextColorIndex(16);
        addTextProperties.setFontIndex(0);
        addTextProperties.setShowBackground(false);
        addTextProperties.setBackgroundBorder(8);
        addTextProperties.setTextAlign(4);
        return addTextProperties;
    }


    public int getTextColorIndex() {
        return this.textColorIndex;
    }


    public void setTextColorIndex(int i) {
        this.textColorIndex = i;
    }


    public int getTextShaderIndex() {
        return this.textShaderIndex;
    }


    public void setTextShaderIndex(int i) {
        this.textShaderIndex = i;
    }


    public int getBackgroundColorIndex() {
        return this.backgroundColorIndex;
    }


    public void setBackgroundColorIndex(int i) {
        this.backgroundColorIndex = i;
    }


    public int getFontIndex() {
        return this.fontIndex;
    }


    public void setFontIndex(int i) {
        this.fontIndex = i;
    }


    public int getTextShadowIndex() {
        return this.textShadowIndex;
    }


    public void setTextShadowIndex(int i) {
        this.textShadowIndex = i;
    }

    public TextShadow getTextShadow() {
        return this.textShadow;
    }


    public void setTextShadow(TextShadow textShadow2) {
        this.textShadow = textShadow2;
    }

    public int getBackgroundBorder() {
        return this.backgroundBorder;
    }


    public void setBackgroundBorder(int i) {
        this.backgroundBorder = i;
    }

    public int getTextHeight() {
        return this.textHeight;
    }


    public void setTextHeight(int i) {
        this.textHeight = i;
    }

    public int getTextWidth() {
        return this.textWidth;
    }


    public void setTextWidth(int i) {
        this.textWidth = i;
    }


    public boolean isFullScreen() {
        return this.isFullScreen;
    }


    public void setFullScreen(boolean z) {
        this.isFullScreen = z;
    }

    public int getPaddingWidth() {
        return this.paddingWidth;
    }


    public void setPaddingWidth(int i) {
        this.paddingWidth = i;
    }


    public int getPaddingHeight() {
        return this.paddingHeight;
    }


    public void setPaddingHeight(int i) {
        this.paddingHeight = i;
    }

    public int getTextSize() {
        return this.textSize;
    }

    public void setTextSize(int i) {
        this.textSize = i;
    }

    public int getTextColor() {
        return this.textColor;
    }

    public void setTextColor(int i) {
        this.textColor = i;
    }

    public int getTextAlpha() {
        return this.textAlpha;
    }


    public void setTextAlpha(int i) {
        this.textAlpha = i;
    }

    public Shader getTextShader() {
        return this.textShader;
    }


    public void setTextShader(Shader shader) {
        this.textShader = shader;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String str) {
        this.text = str;
    }

    public int getTextAlign() {
        return this.textAlign;
    }


    public void setTextAlign(int i) {
        this.textAlign = i;
    }

    public String getFontName() {
        return this.fontName;
    }


    public void setFontName(String str) {
        this.fontName = str;
    }

    public int getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(int i) {
        this.backgroundColor = i;
    }

    public boolean isShowBackground() {
        return this.isShowBackground;
    }


    public void setShowBackground(boolean z) {
        this.isShowBackground = z;
    }

    public int getBackgroundAlpha() {
        return this.backgroundAlpha;
    }


    public void setBackgroundAlpha(int i) {
        this.backgroundAlpha = i;
    }
}
