package com.backustech.ipalmap.other;

import android.graphics.Bitmap;

import com.palmap.gl.MapEngine;
import com.palmap.gl.cache.TextOption;
import com.squareup.picasso.Transformation;

/**
 * Created by 王天明 on 2018/3/2.
 */
public class PicassoTextTransformation implements Transformation {

    private boolean iconEnable = false;
    private TextOption textOption = null;

    public PicassoTextTransformation(TextOption textOption, boolean iconEnable) {
        this.iconEnable = iconEnable;
        this.textOption = textOption;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        if (textOption == null) {
            return source;
        }
        TextBitmapBuilder build = new TextBitmapBuilder(textOption.text);
        if (textOption.enableOutLine) {
            build.enableOutline()
                    .setOutlineColor(textOption.outLineColor)
                    .setOutlineWidth(textOption.outLineWidth * MapEngine.DEFAULT_TEXT_SCALE);
        }
        build.textSize(textOption.textSize * MapEngine.DEFAULT_TEXT_SCALE);
        build.textColor(textOption.textColor);
        build.backGrondColor(textOption.backGroundColor);
        if (this.iconEnable) {
            build.icon(source);
        }
        Bitmap result = build.build();
        source.recycle();
        return result;
    }

    @Override
    public String key() {
        return "PicassoTextTransformation " + this.iconEnable + textOption.toString();
    }
}
