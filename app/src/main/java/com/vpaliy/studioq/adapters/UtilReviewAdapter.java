package com.vpaliy.studioq.adapters;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.bumptech.glide.Glide;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.activities.utils.eventBus.EventBusProvider;
import com.vpaliy.studioq.activities.utils.eventBus.ReviewStateTrigger;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.views.SquareImage;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UtilReviewAdapter extends RecyclerView.Adapter<UtilReviewAdapter.AbstractMediaItem> {

    private List<MediaFile> mediaFileList;
    private LayoutInflater inflater;
    private CaptionItem captionItem;

    private final static int HEADER_TYPE=0;

    private final static int CONTENT_TYPE=1;

    public UtilReviewAdapter(Context context, ArrayList<MediaFile> mDataModel) {
        this.inflater=LayoutInflater.from(context);
        this.mediaFileList=mDataModel;
    }


    abstract class AbstractMediaItem extends RecyclerView.ViewHolder {


        AbstractMediaItem(View itemView) {
            super(itemView);
        }

        public abstract void onBindData(int position);

    }


    public class ContentItem extends AbstractMediaItem {

        @BindView(R.id.image) SquareImage image;
        @BindView(R.id.deleteAction) FloatingActionButton deleteButton;

         ContentItem(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position=getAdapterPosition()-1;
                    MediaFile mediaFile=mediaFileList.get(position);
                    EventBusProvider.defaultBus().post(new ReviewStateTrigger(mediaFile));
                    mediaFileList.remove(position);
                    if(mediaFileList.size()==0) {
                        //TODO ask to pop this back
                    }
                    notifyItemRemoved(position+1);
                }
            });

        }

        @Override
        public void onBindData(int position) {
            Glide.with(itemView.getContext())
                    .load(mediaFileList.get(position).mediaFile())
                    .asBitmap()
                    .centerCrop()
                    .into(image);
        }

    }

    public class CaptionItem extends AbstractMediaItem {

        @BindView(R.id.caption)
        EditText captionText;

        CaptionItem(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }

        @Override
        public void onBindData(int position) {

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
    public AbstractMediaItem onCreateViewHolder(ViewGroup parentGroup, int viewType) {
        View root;
        switch (viewType) {
            case HEADER_TYPE:
                root=inflater.inflate(R.layout.caption_layout_item,parentGroup,false);
                if(captionItem==null)
                    captionItem=new CaptionItem(root);
                return captionItem;
            default:
                root=inflater.inflate(R.layout.media_review_item,parentGroup,false);
                return new ContentItem(root);
        }
    }

    public String getTitle() {
        return captionItem.captionText.getText().toString();
    }

    @Override
    public void onBindViewHolder(AbstractMediaItem holder, int position) {
        if(position!=0)
            position--;
        holder.onBindData(position);
    }

    public List<MediaFile> getSelectedMediaFiles() {
        return mediaFileList;
    }
}