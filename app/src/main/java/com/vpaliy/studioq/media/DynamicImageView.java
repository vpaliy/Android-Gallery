package com.vpaliy.studioq.media;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class DynamicImageView extends ImageView {

    public DynamicImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicImageView(Context context) {
        super(context);
    }

    public DynamicImageView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(widthMeasureSpec,widthMeasureSpec);
    }
}
