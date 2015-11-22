package com.smartfitness.daniellee.fittracker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by danie_000 on 11/14/2015.
 */
public class HorizontalProgress extends View {

    Paint backgroundPaint;
    Paint foregroundPaint;

    int color;

    int width;

    float partDone;

    public HorizontalProgress(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.d("HorizontalProgress", "HorizontalProgress constructor");

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.HorizontalProgress,
                0, 0);


        try {
            color = a.getColor(R.styleable.HorizontalProgress_barColor, 0);
        } finally {
            a.recycle();
        }

        partDone = 0;

        init();
    }

    public void setPartDone(float d) {
        partDone = d;
    }

    public float getPartDone() {
        return partDone;
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(ContextCompat.getColor(getContext(), R.color.light_gray));
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeJoin(Paint.Join.BEVEL);
        backgroundPaint.setStrokeCap(Paint.Cap.SQUARE);
        backgroundPaint.setStrokeWidth(40);

        foregroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        foregroundPaint.setColor(color);
        foregroundPaint.setStyle(Paint.Style.STROKE);
        foregroundPaint.setStrokeJoin(Paint.Join.BEVEL);
        foregroundPaint.setStrokeCap(Paint.Cap.SQUARE);
        foregroundPaint.setStrokeWidth(40);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, 0, width, 0, backgroundPaint);
        canvas.drawLine(0, 0, partDone * width, 0, foregroundPaint);
    }
}
