package com.vpaliy.studioq.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquareImage extends ImageView {

    public SquareImage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImage(Context context) {
        super(context);
    }

    public SquareImage(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec,widthMeasureSpec);
    }

}
