package com.backustech.ipalmap.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.backustech.ipalmap.R;

/**
 * Created by tygzx on 2018/3/13.
 */

public class BookShelf extends View {

    private int rows=1;
    private int columns=1;
    private int targetRows=1;
    private int targetColumns=1;
    private boolean isLoaded=false;

    private float offsetLeftTextToBookShelf=15;//左边文字里书架的距离
    private float offsetTopTextToBookShelf=10;//左边文字里书架的距离
    private float textWidth=13;
    private float textHeight=13;

    private Paint mPaintText;
    private Paint mPaintLine;

    private Bitmap book_shelf_l= BitmapFactory.decodeResource(getResources(), R.drawable.ipalmap_bookshelf_l);
    private Bitmap book_shelf_r= BitmapFactory.decodeResource(getResources(), R.drawable.ipalmap_bookshelf_r);

    public BookShelf(Context context) {
        super(context);
        init();
    }

    public BookShelf(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BookShelf(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mPaintText=new Paint();
        mPaintText.setAntiAlias(true);
        mPaintText.setTextSize(sp2px(getContext(),13));
        mPaintText.setColor(Color.parseColor("#202E2E"));

        mPaintLine=new Paint();
        mPaintLine.setAntiAlias(true);
        mPaintLine.setColor(Color.parseColor("#23C4B7"));

        offsetLeftTextToBookShelf=dip2px(getContext(),15);
        offsetTopTextToBookShelf=dip2px(getContext(),11);

        //获取文字的宽高
        Rect rect = new Rect();
        String temp="1列";
        mPaintText.getTextBounds(temp,0,temp.length(),rect);
        textWidth=rect.width();
        textHeight=rect.height();



    }

    public void setData(int rows,int columns,int targetRows,int targetColumns){
        this.rows=rows;
        this.columns=columns;
        this.targetRows=targetRows;
        this.targetColumns=targetColumns;

        isLoaded=true;

        postInvalidate();
    }



    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if(!isLoaded){
            return;
        }
        drawLabel(canvas);
        drawCell(canvas);
        drawLine(canvas);
        drawSelectedCell(canvas);
    }

    private void drawLabel(Canvas canvas){

        int viewWidth=getMeasuredWidth();
        int viewHeight=getMeasuredHeight();
        float cellWith=(viewWidth-offsetLeftTextToBookShelf-textWidth)/columns;
        float cellHeight=(viewHeight-offsetTopTextToBookShelf-textHeight)/rows;
        float offsetTextToCellHeight=(cellHeight-textHeight)/2;
        float offsetTextToCellWidth=(cellWith-textWidth)/2;
        for(int i=0;i<rows;i++){
            String label=(i+1)+"行";
            canvas.drawText(label,0,offsetTopTextToBookShelf+textHeight*2+cellHeight*i+offsetTextToCellHeight,mPaintText);
        }
        for(int j=0;j<columns;j++){
            String label=(j+1)+"列";
            canvas.drawText(label,offsetLeftTextToBookShelf+textWidth+cellWith*j+offsetTextToCellWidth,textHeight,mPaintText);
        }
    }

    private void drawCell(Canvas canvas){

        int viewWidth=getMeasuredWidth();
        int viewHeight=getMeasuredHeight();
        float cellWith=(viewWidth-offsetLeftTextToBookShelf-textWidth)/columns;
        float cellHeight=(viewHeight-offsetTopTextToBookShelf-textHeight)/rows;

        RectF rectF=new RectF();
        for(int i=0;i<rows;i++){
            for (int j=0;j<columns;j++){

                rectF.left=j*cellWith+offsetLeftTextToBookShelf+textWidth;
                rectF.top=offsetTopTextToBookShelf+textHeight+i*cellHeight;
                rectF.right=rectF.left+cellWith;
                rectF.bottom=rectF.top+cellHeight;
                canvas.drawBitmap(getBitmap(i,j),null,rectF,null);
            }
        }

    }

    private void drawLine(Canvas canvas){

        int viewWidth=getMeasuredWidth();
        int viewHeight=getMeasuredHeight();
        float cellWith=(viewWidth-offsetLeftTextToBookShelf-textWidth)/columns;
        float cellHeight=(viewHeight-offsetTopTextToBookShelf-textHeight)/rows;


        //画外边框
        float lineWidth=dip2px(getContext(),4);
        mPaintLine.setStrokeWidth(lineWidth);
        //左边线
        canvas.drawLine(offsetLeftTextToBookShelf+textWidth,offsetTopTextToBookShelf+textHeight,offsetLeftTextToBookShelf+textWidth,viewHeight,mPaintLine);
        //右边线
        canvas.drawLine(viewWidth-lineWidth/2,offsetTopTextToBookShelf+textHeight,viewWidth-lineWidth/2,viewHeight,mPaintLine);
        //上边线
        canvas.drawLine(offsetLeftTextToBookShelf+textWidth,offsetTopTextToBookShelf+textHeight+lineWidth/2,viewWidth-lineWidth,offsetTopTextToBookShelf+textHeight+lineWidth/2,mPaintLine);
        //下边线
        canvas.drawLine(offsetLeftTextToBookShelf+textWidth,viewHeight-lineWidth/2,viewWidth-lineWidth,viewHeight-lineWidth/2,mPaintLine);

        //画内边框
        lineWidth=dip2px(getContext(),2);
        mPaintLine.setStrokeWidth(lineWidth);
        //先画横线
        for(int i=1;i<rows;i++){
            canvas.drawLine(offsetLeftTextToBookShelf+textWidth,offsetTopTextToBookShelf+textHeight+cellHeight*i,viewWidth-lineWidth/2,offsetTopTextToBookShelf+textHeight+cellHeight*i,mPaintLine);
        }
        //再画竖线
        for(int j=1;j<rows;j++){
            canvas.drawLine(offsetLeftTextToBookShelf+textWidth+cellWith*j,offsetTopTextToBookShelf+textHeight,offsetLeftTextToBookShelf+textWidth+cellWith*j,viewHeight-lineWidth/2,mPaintLine);
        }
    }

    private void drawSelectedCell(Canvas canvas){

        int viewWidth=getMeasuredWidth();
        int viewHeight=getMeasuredHeight();
        float cellWith=(viewWidth-offsetLeftTextToBookShelf-textWidth)/columns;
        float cellHeight=(viewHeight-offsetTopTextToBookShelf-textHeight)/rows;
        float lineWidth=dip2px(getContext(),2);
        RectF rectF=new RectF();
        rectF.left=(targetColumns-1)*cellWith+offsetLeftTextToBookShelf+textWidth;
        rectF.top=offsetTopTextToBookShelf+textHeight+(targetRows-1)*cellHeight+lineWidth/2;
        rectF.right=rectF.left+cellWith;
        rectF.bottom=rectF.top+cellHeight;

        Paint paint=new Paint();

        paint.setStrokeWidth(lineWidth);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.parseColor("#44FF7F00"));
        canvas.drawRect(rectF,paint);

    }


    private Bitmap getBitmap(int i,int j){
        if((i+j)%2==0){
            return book_shelf_l;
        }else {
            return book_shelf_r;
        }
    }


    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
