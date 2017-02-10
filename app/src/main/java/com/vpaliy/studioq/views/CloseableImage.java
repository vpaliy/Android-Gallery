package com.vpaliy.studioq.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
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
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.vpaliy.studioq.R;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class CloseableImage extends RelativeLayout{

    private float closeButtonSize = 32;
    private float closeButtonMargin = 5;
    private float cornerRadius= 7;

    private int closeButtonColor = 0xffff7b57;
    private int closeButtonIcon =R.drawable.ic_clear_black_24dp;

    private ImageView imageView;
    private ImageView closeButton;
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
        initUI();
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
                case R.styleable.CloseableImage_image_margin:
                    setTopLeftMargin(array.getDimension(index,10f* density));
                    break;
            }
        }
        array.recycle();
    }

    private void initUI() {
        closeButton = new ImageView(getContext());
        closeButton.setLayoutParams(new RelativeLayout.
            LayoutParams((int)(closeButtonSize),(int)(closeButtonSize)));

        setCloseBackground();
        closeButton.setImageResource(closeButtonIcon);
        closeButton.setClickable(true);
        closeButton.setId(R.id.closeId);
        closeButton.setVisibility(View.INVISIBLE);

        //install the image itself
        imageView = new ErasableImage(getContext());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(Math.round(topLeftMargin), Math.round(topLeftMargin), 0, 0);
        imageView.setLayoutParams(params);

        addView(imageView);
        addView(closeButton);
    }

    public void setCloseButtonSize(float closeButtonSize) {
        this.closeButtonSize=closeButtonSize;
        invalidateButton();
    }

    private void invalidateButton() {
        if(closeButton!=null) {
            closeButton.invalidate();
        }
    }

    public void setTopLeftMargin(float topLeftMargin) {
        this.topLeftMargin=topLeftMargin;
    }

    public void setCloseButtonMargin(float closeButtonMargin) {
        this.closeButtonMargin=closeButtonMargin;
        if(imageView!=null){
            imageView.invalidate();
        }
    }

    public void setCloseButtonColor(int closeButtonColor) {
        this.closeButtonColor = closeButtonColor;
        if(closeButton!=null) {
            setCloseBackground();
        }
    }

    private void setCloseBackground() {
        StateListDrawable drawable = new StateListDrawable();
        ShapeDrawable shape = new ShapeDrawable(new OvalShape());
        ShapeDrawable shapePressed = new ShapeDrawable(new OvalShape());
        shape.setColorFilter(closeButtonColor, PorterDuff.Mode.SRC_ATOP);
        shapePressed.setColorFilter(closeButtonColor - 0x444444, PorterDuff.Mode.SRC_ATOP);//a little bit darker
        drawable.addState(new int[]{android.R.attr.state_pressed}, shapePressed);
        drawable.addState(new int[]{}, shape);
        closeButton.setBackgroundDrawable(drawable);
    }

    public void setCloseButtonIcon(int closeButtonIcon) {
        this.closeButtonIcon=closeButtonIcon;
        if(closeButton!=null) {
            closeButton.setImageResource(closeButtonIcon);
        }
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius=cornerRadius;
        if(imageView!=null) {
            imageView.invalidate();
        }
    }

    public void setBitmap(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    public void setImageDrawable(@NonNull Drawable imageDrawable) {
        imageView.setImageDrawable(imageDrawable);
    }

    public ImageView getImageView() {
        return imageView;
    }


    public void setOnCloseListener(@NonNull final OnCloseListener listener) {
        findViewById(R.id.closeId).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClose(CloseableImage.this);
            }
        });
    }

     public interface OnCloseListener {
        void onClose(View closeableImage);
    }

    class ErasableImage extends SquareImage{

        private Paint mEraser;
        private RectF mRectangle;
        private Path mRectanglePath;

        public ErasableImage(@NonNull Context context) {
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
            //
            canvas.drawCircle((int) ((closeButtonSize * 0.5) - ((LayoutParams) getLayoutParams()).leftMargin),
                    (int) ((closeButtonSize* 0.5) - ((LayoutParams) getLayoutParams()).topMargin),
                    (int) (((closeButtonSize * 0.5) + closeButtonMargin)), mEraser);


            //// FIXME: 2/9/17
            if(closeButton!=null) {
                closeButton.setVisibility(View.VISIBLE);
            }
        }
    }
}
