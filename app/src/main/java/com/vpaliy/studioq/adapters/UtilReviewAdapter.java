package com.vpaliy.studioq.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.common.graphicalUtils.ScaleBuilder;
import com.vpaliy.studioq.common.eventBus.EventBusProvider;
import com.vpaliy.studioq.common.eventBus.ReviewStateTrigger;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.common.utils.ProjectUtils;
import com.vpaliy.studioq.views.CloseableImage;
import java.util.ArrayList;
import java.util.List;

import android.support.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import static com.vpaliy.studioq.fragments.MediaUtilReviewFragment.Callback;


public class UtilReviewAdapter extends RecyclerView.Adapter<UtilReviewAdapter.AbstractHolder>
        implements SavableAdapter {

    private static final String KEY="util:review:adapter:title";

    private static final int HEADER_TYPE=0;
    private static final int CONTENT_TYPE=1;


    private Callback callback;
    private ArrayList<MediaFile> mediaFileList;
    private LayoutInflater inflater;
    private String titleText;

    private volatile boolean animationFinished=true;

    private UtilReviewAdapter(@NonNull Callback callback,@NonNull Context context) {
        this.inflater=LayoutInflater.from(context);
        this.callback=callback;
    }

    public UtilReviewAdapter(@NonNull Callback callback,@NonNull Context context, @NonNull ArrayList<MediaFile> mDataModel) {
        this(callback,context);
        this.mediaFileList=mDataModel;
    }

    public UtilReviewAdapter(@NonNull Callback callback, @NonNull Context context, @NonNull Bundle state) {
        this(callback,context);
        restoreState(state);
    }

    abstract class AbstractHolder extends RecyclerView.ViewHolder {
        AbstractHolder(View itemView) {
            super(itemView);
        }
        public void onBindData(int position){}
    }

    @SuppressWarnings("WeakerAccess")
    public class ContentHolder extends AbstractHolder {

        @BindView(R.id.image)
        CloseableImage image;

        @BindView(R.id.description)
        ImageView description;

        ContentHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
                image.setOnCloseListener(new CloseableImage.OnCloseListener() {
                    @Override
                    public void onClose(View closeableImage) {
                        if(animationFinished) {
                            ScaleBuilder.start(image, 0f)
                                    .duration(150)
                                    .accelerate()
                                    .listener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {
                                            super.onAnimationStart(animation);
                                            animationFinished = false;
                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            int position = getAdapterPosition() - 1;
                                            MediaFile mediaFile = mediaFileList.get(position);
                                            EventBusProvider.defaultBus().post(new ReviewStateTrigger(mediaFile));
                                            mediaFileList.remove(position);
                                            if (mediaFileList.size() == 0) {
                                                callback.onFinish();
                                            }
                                            notifyItemRemoved(position + 1);
                                            animationFinished = true;
                                        }
                                    }).execute();
                        }

                    }
                });
        }

        @Override
        public void onBindData(int position) {
            position--;
            if(itemView.getScaleX()<1.f) {
                itemView.setScaleX(1f);
                itemView.setScaleY(1f);
            }

            determineIcon(position);

            Glide.with(itemView.getContext())
                    .load(mediaFileList.get(position).mediaFile())
                    .asBitmap()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(new ImageViewTarget<Bitmap>(image.getImageView()) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            image.setBitmap(resource);
                        }
                    });

        }

        private void determineIcon(int position) {
            MediaFile mediaFile=mediaFileList.get(position);
            description.setImageDrawable(null);
            if(mediaFile.getType()== MediaFile.Type.VIDEO) {
                Drawable drawable=itemView.getContext().getResources().
                    getDrawable(R.drawable.ic_play_circle_filled_white_24dp);
                description.setImageDrawable(drawable);
            }else if(mediaFile.getType()== MediaFile.Type.GIF) {
                Drawable drawable=itemView.getContext().getResources().
                        getDrawable(R.drawable.ic_gif_white_24dp);
                description.setImageDrawable(drawable);
            }
        }
    }


    @SuppressWarnings("WeakerAccess")
    public class CaptionHolder extends AbstractHolder
            implements TextWatcher {

        @BindView(R.id.caption)
        EditText captionText;

        CaptionHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            captionText.addTextChangedListener(this);
        }

        @Override
        public void onBindData(int position) {
            captionText.setText(titleText);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count){}

        @Override
        public void afterTextChanged(Editable s) {
            if(s!=null) {
                titleText = s.toString();
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(position==0)
            return HEADER_TYPE;
        return CONTENT_TYPE;
    }

    @Override
    public int getItemCount() {
        return mediaFileList.size()+1;
    }


    @Override
    public AbstractHolder onCreateViewHolder(ViewGroup parentGroup, int viewType) {
        View root;
        switch (viewType) {
            case HEADER_TYPE:
                root=inflater.inflate(R.layout.adapter_caption_item,parentGroup,false);
                return new CaptionHolder(root);
            default:
                root=inflater.inflate(R.layout.adapter_review_item,parentGroup,false);
                return new ContentHolder(root);
        }
    }

    public String getTitle() {
        return titleText;
    }

    @Override
    public void onBindViewHolder(AbstractHolder holder, int position) {
        holder.onBindData(position);
    }

    public List<MediaFile> getSelectedMediaFiles() {
        return mediaFileList;
    }

    public void saveState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(ProjectUtils.MEDIA_DATA,mediaFileList);
        outState.putString(KEY,titleText);
    }

    @Override
    public void restoreState(@NonNull Bundle savedInstanceState) {
        titleText=savedInstanceState.getString(KEY);
        mediaFileList=savedInstanceState.getParcelableArrayList(ProjectUtils.MEDIA_DATA);
    }
}