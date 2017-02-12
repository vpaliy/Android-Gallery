package com.vpaliy.studioq.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;

import com.vpaliy.studioq.R;

public class MediaView extends RelativeLayout {

    private float cornerRadius=7f;

    private int descriptionMarginTop;
    private int descriptionMarginBottom;
    private int descriptionMarginLeft;
    private int descriptionMarginRight;


    @DrawableRes
    private int icon=-1;
    private int descriptionGravity=Gravity.TOP | Gravity.END;

    private ImageView descriptionIcon;
    private ImageView mainContent;

    public MediaView(@NonNull Context context) {
        this(context,null,0);
    }

    public MediaView(@NonNull Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public MediaView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        initAttrs(attrs);
        initUI();
    }

    private void initAttrs(@Nullable AttributeSet attrs) {
        if(attrs==null)
            return;
        TypedArray array=getContext().obtainStyledAttributes(attrs,
                R.styleable.MediaView);
        int size=array.getIndexCount();
        float density = getResources().getDisplayMetrics().density;
        for(int index=0;index<size;index++) {
            switch (array.getIndex(index)) {
                case R.styleable.MediaView_radius:
                    setCornerRadius(array.getDimension(array.getIndex(index),7f*density));
                    break;
                case R.styleable.MediaView_description_gravity:
                    setDescriptionGravity(array.getInteger(array.getIndex(index),Gravity.TOP|Gravity.END));
                    break;
                case R.styleable.MediaView_description_margin:
                    setDescriptionMargin((int)array.getDimension(array.getIndex(index),-1));
                    break;
                case R.styleable.MediaView_description_margin_left:
                    setDescriptionMarginLeft((int)array.getDimension(array.getIndex(index),descriptionMarginLeft));
                    break;
                case R.styleable.MediaView_description_margin_right:
                    setDescriptionMarginRight((int)array.getDimension(array.getIndex(index),descriptionMarginRight));
                    break;
                case R.styleable.MediaView_description_margin_top:
                    setDescriptionMarginTop((int)array.getDimension(array.getIndex(index),descriptionMarginTop));
                    break;
                case R.styleable.MediaView_description_margin_bottom:
                    setDescriptionMarginBottom((int)array.getDimension(array.getIndex(index),descriptionMarginBottom));
                    break;
                //
                case R.styleable.MediaView_description_icon:
                    setDescriptionIcon(array.getResourceId(array.getIndex(index),icon));
            }
        }
        array.recycle();
    }

    private void initUI() {
        //install the icon
        descriptionIcon=new ImageView(getContext());
        setDescriptionIcon(icon);
        RelativeLayout.LayoutParams descriptionParams= new RelativeLayout.
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descriptionParams.leftMargin=descriptionMarginLeft;
        descriptionParams.rightMargin=descriptionMarginRight;
        descriptionParams.bottomMargin=descriptionMarginBottom;
        descriptionParams.topMargin=descriptionMarginTop;
        descriptionIcon.setLayoutParams(descriptionParams);
        setDescriptionGravity(descriptionGravity);

        //
        mainContent=new SquareImage(getContext());
        RelativeLayout.LayoutParams contentParams=new RelativeLayout.
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mainContent.setLayoutParams(contentParams);

        addView(mainContent);
        addView(descriptionIcon);

    }



    public void setCornerRadius(float radius) {
        this.cornerRadius=radius;
        if(mainContent!=null) {
            mainContent.invalidate();
        }
    }

    public void setDescriptionGravity(int gravity) {
        this.descriptionGravity=gravity;
        adjustDescriptionIcon();
    }


    private void adjustDescriptionIcon() {
        RelativeLayout.LayoutParams params=fetchDescriptionParams();
        if(params!=null) {
            int[] rules=params.getRules();
            for(int index=0;index<rules.length;index++) {
                rules[index] = 0;
            }
            switch (descriptionGravity) {
                case Gravity.TOP|Gravity.END:
                case Gravity.TOP|Gravity.RIGHT:
                    rules[ALIGN_PARENT_TOP]=RelativeLayout.TRUE;
                    rules[ALIGN_PARENT_RIGHT]=RelativeLayout.TRUE;
                    break;
                case Gravity.TOP|Gravity.START:
                case Gravity.TOP|Gravity.LEFT:
                    rules[ALIGN_PARENT_TOP]=RelativeLayout.TRUE;
                    rules[ALIGN_PARENT_LEFT]=RelativeLayout.TRUE;
                    break;
                case Gravity.BOTTOM|Gravity.END:
                case Gravity.BOTTOM|Gravity.RIGHT:
                    rules[ALIGN_PARENT_BOTTOM]=RelativeLayout.TRUE;
                    rules[ALIGN_PARENT_RIGHT]=RelativeLayout.TRUE;
                    break;
                case Gravity.BOTTOM|Gravity.START:
                case Gravity.BOTTOM|Gravity.LEFT:
                    rules[ALIGN_PARENT_BOTTOM]=RelativeLayout.TRUE;
                    rules[ALIGN_PARENT_LEFT]=RelativeLayout.TRUE;
                    break;
                case Gravity.TOP|Gravity.CENTER:
                    rules[ALIGN_PARENT_TOP]=RelativeLayout.TRUE;
                    rules[CENTER_HORIZONTAL]=RelativeLayout.TRUE;
                    break;
                case Gravity.BOTTOM|Gravity.CENTER:
                    rules[ALIGN_PARENT_BOTTOM]=RelativeLayout.TRUE;
                    rules[CENTER_HORIZONTAL]=RelativeLayout.TRUE;
                    break;
                case Gravity.CENTER:
                    rules[CENTER_IN_PARENT]=RelativeLayout.TRUE;
                    break;
            }
            descriptionIcon.setLayoutParams(params);
        }
    }

    private RelativeLayout.LayoutParams fetchDescriptionParams() {
        if(descriptionIcon!=null) {
            return RelativeLayout.LayoutParams.class.cast(descriptionIcon.getLayoutParams());
        }
        return null;
    }

    private RelativeLayout.LayoutParams fetchContentParams() {
        if(mainContent!=null) {
            return RelativeLayout.LayoutParams.class.cast(mainContent.getLayoutParams());
        }
        return null;
    }

    public void setDescriptionMarginTop(int marginTop) {
        this.descriptionMarginTop=marginTop;
        RelativeLayout.LayoutParams params=fetchDescriptionParams();
        if(params!=null) {
            params.topMargin=marginTop;
            descriptionIcon.setLayoutParams(params);
        }
    }

    public void setDescriptionMarginBottom(int marginBottom) {
        this.descriptionMarginBottom=marginBottom;
        RelativeLayout.LayoutParams params=fetchDescriptionParams();
        if(params!=null) {
            params.bottomMargin=marginBottom;
            descriptionIcon.setLayoutParams(params);
        }
    }

    public void setDescriptionMarginLeft(int marginLeft) {
        this.descriptionMarginLeft=marginLeft;
        RelativeLayout.LayoutParams params=fetchDescriptionParams();
        if(params!=null) {
            params.leftMargin=marginLeft;
            descriptionIcon.setLayoutParams(params);
        }
    }

    public void setDescriptionMarginRight(int marginRight) {
        this.descriptionMarginRight=marginRight;
        RelativeLayout.LayoutParams params=fetchDescriptionParams();
        if(params!=null) {
            params.rightMargin=marginRight;
            descriptionIcon.setLayoutParams(params);
        }
    }

    public void setDescriptionMargin(int margin) {
        if(margin!=-1) {
            setDescriptionMarginBottom(margin);
            setDescriptionMarginLeft(margin);
            setDescriptionMarginRight(margin);
            setDescriptionMarginTop(margin);
        }
    }

    public void setDescriptionIcon(Drawable icon) {
        descriptionIcon.setImageDrawable(icon);
    }

    public void setDescriptionIcon(@DrawableRes int resId) {
        if(resId!=-1) {
            this.icon=resId;
            setDescriptionIcon(getResources().getDrawable(resId));
        }
    }

    public void setMainContent(@NonNull Drawable drawable) {
        mainContent.setImageDrawable(drawable);
    }

    public ImageView getMainContent() {
        return mainContent;
    }

    class MainImage extends SquareImage{

        private Paint mEraser;
        private RectF mRectangle;
        private Path mRectanglePath;

        public MainImage(@NonNull Context context) {
            super(context);
            init();
        }

        private void init() {
            mEraser = new Paint();
            mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mEraser.setAntiAlias(true);
            mRectanglePath = new Path();
        }


        protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
            if (w != oldWidth || h != oldHeight) {
                mRectanglePath.reset();
                mRectangle = new RectF(0, 0, getWidth(), getHeight());
                mRectanglePath.addRoundRect(mRectangle, cornerRadius, cornerRadius, Path.Direction.CW);
                mRectanglePath.setFillType(Path.FillType.INVERSE_EVEN_ODD);
            }
            super.onSizeChanged(w, h, oldWidth, oldHeight);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(), 255, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
            super.onDraw(canvas);
            canvas.drawPath(mRectanglePath, mEraser);

        }
    }

}
