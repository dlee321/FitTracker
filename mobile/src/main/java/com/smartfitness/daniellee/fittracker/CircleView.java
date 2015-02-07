package com.smartfitness.daniellee.fittracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircleView extends View {

    Paint mPaint;
    Paint mTextPaint;
    Paint mTextPaint2;
    Path circle;

    int width;
    int height;

    static String stepsString;
    String goalString;

    Canvas mCanvas = null;

    RectF box;

    public CircleView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    private void init() {

        stepsString = "0";
        goalString = "10000";
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.accentColor));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(50);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.GRAY);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setStrokeWidth(15);
        mTextPaint.setTextSize(250);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mTextPaint2 = new Paint();
        mTextPaint2.setColor(Color.GRAY);
        mTextPaint2.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint2.setStrokeWidth(10);
        mTextPaint2.setTextSize(120);
        mTextPaint2.setTextAlign(Paint.Align.CENTER);

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
        canvas.drawText(stepsString, xPos, yPos - 50, mTextPaint);
        canvas.drawText(goalString, xPos, yPos + 100, mTextPaint2);
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


}
