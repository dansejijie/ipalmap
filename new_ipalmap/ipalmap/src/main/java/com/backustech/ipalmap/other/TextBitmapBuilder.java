package com.backustech.ipalmap.other;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;

/**
 * Created by wtm on 2017/11/29.
 */
public class TextBitmapBuilder {

    //画笔
    private Paint paint = new Paint();
    //文字
    private String text = null;
    //图片的背景色
    private int backGrondColor = Color.TRANSPARENT;
    //文字的颜色
    private int textColor = Color.BLACK;

    private int outlineColor = Color.WHITE;

    private float textSize = 20;

    private Typeface typeface;

    private boolean enableOutline;

    private float outlineWidth = 1;

    private Bitmap icon;

    public TextBitmapBuilder(String text) {
        this.text = text;
        paint = new Paint();
    }

    public TextBitmapBuilder text(String text) {
        this.text = text;
        return this;
    }

    public TextBitmapBuilder textSize(float textSize) {
        this.textSize = textSize;
        return this;
    }

    public TextBitmapBuilder textColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public TextBitmapBuilder backGrondColor(int backgrondColor) {
        this.backGrondColor = backgrondColor;
        return this;
    }

    public TextBitmapBuilder enableOutline() {
        this.enableOutline = true;
        return this;
    }

    public TextBitmapBuilder disableOutline() {
        this.enableOutline = false;
        return this;
    }

    public TextBitmapBuilder setTypeface(Typeface typeface) {
        this.typeface = typeface;
        this.paint.setTypeface(typeface);
        return this;
    }

    public TextBitmapBuilder setTextColor(int color) {
        this.textColor = color;
        return this;
    }

    public TextBitmapBuilder setOutlineColor(int color) {
        this.outlineColor = color;
        return this;
    }

    public TextBitmapBuilder setOutlineWidth(float lineWidth) {
        this.outlineWidth = lineWidth;
        this.paint.setStrokeWidth(lineWidth);
        return this;
    }

    public TextBitmapBuilder icon(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            this.icon = bitmap;
        }
        return this;
    }

    public Bitmap build() {
        if (TextUtils.isEmpty(text)) {
            throw new IllegalArgumentException("Could not build a bitmap , text is null!");
        }
        if (icon == null) {
            return createByText();
        }
        Bitmap textBitmap = createByText();
        int offset = 0;
        int width = textBitmap.getWidth() + icon.getWidth() + offset;
        int height = textBitmap.getHeight() > icon.getHeight() ? textBitmap.getHeight() : icon.getHeight();

        Paint paint = new Paint();

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);

        Canvas canvas = new Canvas(result);

        int iconTop = (height - icon.getHeight()) / 2;
        int textTop = (height - textBitmap.getHeight()) / 2;

        canvas.drawBitmap(icon, 0, iconTop, paint);

        canvas.drawBitmap(textBitmap, icon.getWidth() + offset, textTop, paint);

        textBitmap.recycle();
        return result;
    }

    private Bitmap createByText() {
        this.paint.setTextSize(textSize);
        Rect scratchBounds = new Rect();
        this.paint.getTextBounds(text, 0, text.length(), scratchBounds);
        int x = -scratchBounds.left + 1;
        int y = -scratchBounds.top + 1;
        int width = scratchBounds.width() + 2;
        int height = scratchBounds.height() + 2;

        if (this.enableOutline) {
            int strokeWidth_2 = (int) Math.ceil(this.paint.getStrokeWidth() * 0.5f);
            x += strokeWidth_2;
            y += strokeWidth_2;
            width += (strokeWidth_2 * 2);
            height += (strokeWidth_2 * 2);
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(backGrondColor);
        if (this.enableOutline) {
            this.paint.setStyle(Paint.Style.FILL_AND_STROKE);
            this.paint.setColor(this.outlineColor);
            canvas.drawText(text, 0, text.length(), x, y, this.paint);
        }

        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setColor(this.textColor);
        canvas.drawText(text, 0, text.length(), x, y, this.paint);
        return bitmap;
    }

}