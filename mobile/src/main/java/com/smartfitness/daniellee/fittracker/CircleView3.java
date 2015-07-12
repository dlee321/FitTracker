package com.smartfitness.daniellee.fittracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleView3 extends View {

    Paint mPaint;
    Paint mTextPaint;
    Path circle;

    int width;
    int height;

    int sleepTime;
    String sleepTimeString;

    RectF box;

    public CircleView3(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    private void init() {

        sleepTimeString = "No Sleep Data";
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.colorPrimarySleep));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(20);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.GRAY);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setStrokeWidth(2);
        mTextPaint.setTextSize(50);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        circle = new Path();

        box = new RectF(0,0,width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        box.set(50, 50, width - 50, height - 50);
        float sweep = 360 * sleepTime / (8*60);
        circle.reset();
        circle.addArc(box, 270, sweep);

        int xPos = canvas.getHeight()/2;
        int yPos = (int) ((canvas.getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));

        canvas.drawPath(circle, mPaint);
        canvas.drawText(sleepTimeString, xPos, yPos, mTextPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    protected void setSleepTime(int mins) {
        sleepTime = mins;
    }

    protected void setSleepTimeString(String time) {
        sleepTimeString = time;
    }

    protected void reset() {
        sleepTimeString = "No Sleep Data";
        sleepTime = 0;
    }


}
