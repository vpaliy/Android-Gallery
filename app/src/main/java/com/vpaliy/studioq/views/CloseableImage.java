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
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.vpaliy.studioq.R;

public class CloseableImage extends RelativeLayout{

    private float closeButtonSize = 32; //google messenger approx 27
    private float closeButtonMargin = 5; //google messenger approx 27
    private float cornerRadius= 7; //google messenger approx 27

    private int closeButtonColor = 0xffff7b57; //google messenger approx 27
    private int closeButtonIcon =R.drawable.ic_clear_black_24dp;// R.drawable.ic_action_close;

    private ImageView imageView;
    private float topLeftMargin = 10f;

    public CloseableImage(@NonNull Context context) {
        this(context,null,0);
    }

    public CloseableImage(@NonNull Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public CloseableImage(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        initAttrs(attrs);
    }

    private void initAttrs(@Nullable AttributeSet attrs){
        if(attrs==null)
            return;
        TypedArray array=getContext().obtainStyledAttributes(attrs,
                R.styleable.CloseableImage);
        final int N=array.getIndexCount();
        float density = getResources().getDisplayMetrics().density;
        for(int index=0;index<N;index++) {
            switch (array.getIndex(index)) {
                case R.styleable.CloseableImage_close_color:
                    setCloseButtonColor(array.getColor(index,0xffffb57));
                    break;
                case R.styleable.CloseableImage_close_size:
                    setCloseButtonSize(array.getDimension(index, 32f*density));
                    break;
                case R.styleable.CloseableImage_close_margin:
                    setCloseButtonMargin(array.getDimension(index,5f*density));
                    break;
                case R.styleable.CloseableImage_corner_radius:
                    setCornerRadius(array.getDimension(index,7f*density));
                    break;
                case R.styleable.CloseableImage_close_icon:
                    setCloseButtonIcon(array.getResourceId(index,R.drawable.ic_cancel_black_24dp));
                    break;
            }
        }

        array.recycle();
    }

    private void initUI() {
        ImageView closeButton = new ImageView(getContext());
        closeButton.setLayoutParams(new RelativeLayout.
            LayoutParams((int)(closeButtonSize),(int)(closeButtonSize)));

        StateListDrawable drawable = new StateListDrawable();
        ShapeDrawable shape = new ShapeDrawable(new OvalShape());
        ShapeDrawable shapePressed = new ShapeDrawable(new OvalShape());
        shape.setColorFilter(closeButtonColor, PorterDuff.Mode.SRC_ATOP);
        shapePressed.setColorFilter(closeButtonColor - 0x444444, PorterDuff.Mode.SRC_ATOP);//a little bit darker
        drawable.addState(new int[]{android.R.attr.state_pressed}, shapePressed);
        drawable.addState(new int[]{}, shape);
        closeButton.setImageResource(closeButtonIcon);
        closeButton.setBackgroundDrawable(drawable);
        closeButton.setClickable(true);
        closeButton.setId(R.id.closeId);
        imageView = new CustomImageView(getContext(), closeButtonSize, closeButtonMargin, cornerRadius);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(Math.round(topLeftMargin), Math.round(topLeftMargin), 0, 0);
        imageView.setLayoutParams(params);
        imageView.setAdjustViewBounds(true);

        addView(imageView);
        addView(closeButton);
    }

    public void setCloseButtonSize(float closeButtonSize) {
        this.closeButtonSize=closeButtonSize;
    }

    public void setCloseButtonMargin(float closeButtonMargin) {
        this.closeButtonMargin=closeButtonMargin;
    }

    public void setCloseButtonColor(int closeButtonColor) {
        this.closeButtonColor = closeButtonColor;
    }

    public void setCloseButtonIcon(int closeButtonIcon) {
        this.closeButtonIcon=closeButtonIcon;
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius=cornerRadius;
    }

    public void setImageDrawable(@NonNull Drawable imageDrawable) {
        imageView.setImageDrawable(imageDrawable);
    }

    static class CustomImageView extends SquareImage {

        private final float mCloseButtonMargin;
        private final float mCornerRadius;
        private float mCloseSize;
        private Paint mEraser;
        private RectF mRectangle;
        private Path mRectanglePath;

        public CustomImageView(Context context, float closeSize, float closeButtonMargin, float cornerRadius) {
            super(context);
            mCloseSize = closeSize;
            mCloseButtonMargin = closeButtonMargin;
            mCornerRadius = cornerRadius;
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
                mRectanglePath.addRoundRect(mRectangle, mCornerRadius, mCornerRadius, Path.Direction.CW);
                mRectanglePath.setFillType(Path.FillType.INVERSE_EVEN_ODD);
            }
            super.onSizeChanged(w, h, oldWidth, oldHeight);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(), 255, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
            super.onDraw(canvas);
            canvas.drawPath(mRectanglePath, mEraser);
            //
            canvas.drawCircle((int) ((mCloseSize * 0.5) - ((LayoutParams) getLayoutParams()).leftMargin),
                    (int) ((mCloseSize * 0.5) - ((LayoutParams) getLayoutParams()).topMargin),
                    (int) (((mCloseSize * 0.5) + mCloseButtonMargin)), mEraser);
        }
    }


}
