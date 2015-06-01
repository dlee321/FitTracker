package com.smartfitness.daniellee.fittracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleView2 extends View {

    Paint mPaint;
    Paint mTextPaint;
    Path circle;

    int width;
    int height;

    String stepsString;

    RectF box;

    public CircleView2(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    private void init() {

        stepsString = "0";
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.accentColor));
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
        // TODO: replace with actual goal
        box.set(50, 50, width - 50, height - 50);
        float sweep = 360 * Integer.parseInt(stepsString) / 10000;
        circle.reset();
        circle.addArc(box, 270, sweep);

        int xPos = canvas.getHeight()/2;
        int yPos = (int) ((canvas.getHeight() / 2) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));

        canvas.drawPath(circle, mPaint);
        canvas.drawText(stepsString, xPos, yPos, mTextPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    protected void setStepsString(int steps) {
        stepsString  = "" + steps;
    }

    protected String getStepsString() {
        return stepsString;
    }


}
