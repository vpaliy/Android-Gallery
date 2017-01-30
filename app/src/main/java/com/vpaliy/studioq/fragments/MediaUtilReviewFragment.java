package com.vpaliy.studioq.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.activities.utils.eventBus.ExitEvent;
import com.vpaliy.studioq.model.DynamicImageView;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.activities.utils.eventBus.EventBusProvider;
import com.vpaliy.studioq.activities.utils.eventBus.ReviewStateTrigger;
import com.vpaliy.studioq.utils.ProjectUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

import static butterknife.ButterKnife.findById;

public class MediaUtilReviewFragment extends Fragment
        implements View.OnClickListener{

    private final static String TAG=MediaUtilReviewFragment.class.getSimpleName();

    private ArrayList<MediaFile> reviewMediaFileList;
    private MediaAdapter adapter;


    public static MediaUtilReviewFragment newInstance(ArrayList<MediaFile> mediaFileList) {
        MediaUtilReviewFragment fragment=new MediaUtilReviewFragment();
        Bundle args=new Bundle();
        args.putParcelableArrayList(ProjectUtils.MEDIA_DATA,mediaFileList);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if(savedInstanceState==null)
            savedInstanceState=getArguments();
        reviewMediaFileList=savedInstanceState.getParcelableArrayList(ProjectUtils.MEDIA_DATA);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.media_review_layout,container,false);
    }


    @Override
    public void onViewCreated(final View root, @Nullable Bundle savedInstanceState) {
        if(root!=null) {
            final RecyclerView mediaRecyclerView=findById(root,R.id.mediaRecyclerView);
            //TODO xml representation
            GridLayoutManager manager=new GridLayoutManager(getContext(),2, GridLayoutManager.VERTICAL,false);
            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if(position==0)
                        return 2;
                    return 1;
                }
            });
            mediaRecyclerView.setLayoutManager(manager);
            mediaRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mediaRecyclerView.setAdapter(adapter=new MediaAdapter(getContext(),reviewMediaFileList));
            ImageButton proceedButton=findById(root,R.id.proceed);
            proceedButton.setOnClickListener(this);

        }
    }

    private boolean isTitleCorrect(String title) {
        //Never gonna happen
        if(title==null)
            return false;
        title=title.trim();
        if(title.isEmpty()) {
            //TODO apply support for languages
            Toast.makeText(getContext(), "Empty title", Toast.LENGTH_SHORT).show();
            return false;
        }else if(new File(Environment.getExternalStorageDirectory() + File.separator +title).exists()) {
            //TODO apply support for languages
            Toast.makeText(getContext(), "Folder already exists", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                break;
            case R.id.proceed:
                if(!isTitleCorrect(adapter.getTitle()))
                    return;
                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.CopyOrMove)
                        .setItems(R.array.selectItem, new DialogInterface.OnClickListener() {

                            private final int COPY_ITEM=0;
                            private final int MOVE_ITEM=1;

                            @Override
                            public void onClick(DialogInterface dialogInterface, int itemIndex) {
                                Intent data = new Intent();
                                data.putExtra(ProjectUtils.MEDIA_DATA,
                                        (ArrayList<MediaFile>)(adapter.getSelectedMediaFiles()));
                                data.putExtra(ProjectUtils.MEDIA_TITLE,adapter.getTitle());
                                switch (itemIndex) {
                                    case COPY_ITEM:
                                        data.putExtra(ProjectUtils.MOVE_FILE_TO,false);
                                        EventBusProvider.defaultBus().post(new ExitEvent(data));
                                        break;
                                    case MOVE_ITEM:
                                        data.putExtra(ProjectUtils.MOVE_FILE_TO,true);
                                        EventBusProvider.defaultBus().post(new ExitEvent(data));
                                        break;
                                }

                            }
                        })
                        .setNegativeButton(R.string.Cancel,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int index) {
                                dialogInterface.cancel();
                            }
                        })
                        .create().show();

        }
    }


    public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.AbstractMediaItem> {

        private List<MediaFile> mediaFileList;
        private LayoutInflater inflater;
        private final Glide glideInstance;
        private CaptionItem captionItem;

        private final static int HEADER_TYPE=0;

        private final static int CONTENT_TYPE=1;

         MediaAdapter(Context context, ArrayList<MediaFile> mDataModel) {
            this.inflater=LayoutInflater.from(context);
            this.mediaFileList=mDataModel;
            this.glideInstance=Glide.get(context);
            glideInstance.setMemoryCategory(MemoryCategory.HIGH);
        }


        public abstract class AbstractMediaItem extends RecyclerView.ViewHolder {


            public AbstractMediaItem(View itemView) {
                super(itemView);
            }

            public abstract void onBindData(int position);

        }


        public class ContentItem extends AbstractMediaItem {

            @BindView(R.id.image) DynamicImageView image;
            @BindView(R.id.deleteAction) FloatingActionButton deleteButton;

            public ContentItem(View itemView) {
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
                            getActivity().getSupportFragmentManager().popBackStack();
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

        class CaptionItem extends AbstractMediaItem {

            @BindView(R.id.caption) EditText captionText;

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
}
