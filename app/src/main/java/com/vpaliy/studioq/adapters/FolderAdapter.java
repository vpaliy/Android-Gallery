package com.vpaliy.studioq.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.common.dataUtils.Subscriber;
import com.vpaliy.studioq.common.eventBus.EventBusProvider;
import com.vpaliy.studioq.common.eventBus.Launcher;
import com.vpaliy.studioq.adapters.multipleChoice.BaseAdapter;
import com.vpaliy.studioq.adapters.multipleChoice.MultiMode;
import com.vpaliy.studioq.controllers.DataController;
import com.vpaliy.studioq.model.DummyFolder;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.model.MediaFolder;
import com.vpaliy.studioq.common.utils.ProjectUtils;
import com.vpaliy.studioq.views.MediaView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import android.support.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FolderAdapter extends BaseAdapter
        implements Subscriber {

    private final String KEY_MODE="adapter:mode";
    private final String KEY_DATA="adapter:data";

    private final String TAG=FolderAdapter.class.getSimpleName();

    private final static float SCALE_F=0.85f;
    private LayoutInflater inflater;
    private List<MediaFolder> mediaFolderList;

    private List<MediaFolder> currentFolderList;
    private Mode adapterMode=Mode.ALL;


    public FolderAdapter(Context context, MultiMode mode, List<MediaFolder> mediaFolderList) {
        super(mode,true);
        this.inflater=LayoutInflater.from(context);
        this.mediaFolderList=mediaFolderList;
        this.currentFolderList=mediaFolderList;
    }


    public FolderAdapter(Context context, @NonNull  MultiMode mode, @NonNull Bundle state) {
        super(mode,true,state);
        this.mediaFolderList=state.getParcelableArrayList(KEY_DATA);
        this.currentFolderList=mediaFolderList;
        this.inflater=LayoutInflater.from(context);
        adapterMode=Mode.valueOf(state.getString(KEY_MODE,Mode.ALL.name()));
        initCurrentList();
    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup parentGroup, int viewType) {
        View root=inflater.inflate(R.layout.adapter_folder_item,parentGroup,false);
        return new FolderViewHolder(root);
    }


    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBindData();
    }

    @Override
    public int getItemCount() {
        return currentFolderList.size();
    }

    class FolderViewHolder extends BaseAdapter.BaseViewHolder {

        @BindView(R.id.mainContent) MediaView mainImage;
        @BindView(R.id.folderName) TextView folderName;
        @BindView(R.id.imageCount) TextView mediaCount;

        FolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            mainImage.setDescriptionGravity(Gravity.CENTER);
        }

        @Override
        public void enterState() {
            super.enterState();
            itemView.animate()
                    .scaleX(SCALE_F)
                    .scaleY(SCALE_F)
                    .setDuration(180).start();
        }

        @Override
        public void exitState() {
            super.exitState();
            if (itemView.getScaleY() < 1.f) {
                itemView.animate().setDuration(180)
                        .scaleY(1.f).scaleX(1.f)
                        .start();
            }
        }

        @Override
        public void animatedState() {
            itemView.setScaleX(SCALE_F);
            itemView.setScaleY(SCALE_F);
        }

        @Override
        public void defaultState() {
            if(itemView.getScaleX()<1f) {
                itemView.setScaleX(1.f);
                itemView.setScaleY(1.f);
            }
        }



        @Override
        public void onClick(View view) {
            if(!isMultiModeActivated()) {
                MediaFolder resultFolder=currentFolderList.get(getAdapterPosition());
                if(adapterMode==Mode.IMAGE) {
                    resultFolder = resultFolder.createImageSubfolder();
                }else if(adapterMode==Mode.VIDEO) {
                    resultFolder = resultFolder.createVideoSubfolder();
                }
                Bundle data=new Bundle();
                data.putParcelable(ProjectUtils.MEDIA_FOLDER,resultFolder);
                EventBusProvider.defaultBus().post(new Launcher<>(data,view));
            }
            super.onClick(view);
        }


        @Override
        public void updateBackground() {

        }

        @Override
        public void onBindData() {
            int position=getAdapterPosition();
            MediaFile mediaFile=loaderCover(position);
            Glide.with(itemView.getContext())
                    .load(mediaFile.mediaFile())
                    .centerCrop()
                    .priority(Priority.HIGH)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .thumbnail(0.5f)
                    .placeholder(R.drawable.placeholder)
                    .animate(R.anim.fade_in)
                    .into(new ImageViewTarget<GlideDrawable>(mainImage.getMainContent()) {
                        @Override
                        protected void setResource(GlideDrawable resource) {
                            mainImage.setMainContent(resource);
                        }
                    });


            folderName.setText(currentFolderList.get(position).name());
            mediaCount.setText(String.format(Locale.US,"%d",currentFolderList.get(position).size()));
            determineDescription(mediaFile.getType());
            determineState();
        }

        private void determineDescription(MediaFile.Type type) {
            if(type== MediaFile.Type.GIF) {
                mainImage.setDescriptionIcon(R.drawable.ic_gif_white_24dp);
            }else if(type== MediaFile.Type.VIDEO) {
                mainImage.setDescriptionIcon(R.drawable.ic_play_circle_outline_white_48dp);
            }
        }

    }


    public void setAdapterMode(Mode mode) {
        if(mode!=adapterMode) {
            this.adapterMode=mode;
            initCurrentList();
            notifyDataSetChanged();
        }
    }

    private MediaFile loaderCover(int position) {
        if(adapterMode==Mode.ALL) {
            return currentFolderList.get(position).cover();
        }else if(adapterMode==Mode.IMAGE) {
            return currentFolderList.get(position).imageCover();
        }
        return currentFolderList.get(position).videoCover();
    }


    private void initCurrentList() {
        if(adapterMode==Mode.ALL) {
            this.currentFolderList=mediaFolderList;
        }else if(adapterMode==Mode.IMAGE) {
            ArrayList<MediaFolder> imageFolderList=new ArrayList<>();
            for(MediaFolder folder:mediaFolderList) {
                if(folder.imageCover()!=null) {
                    imageFolderList.add(folder);
                }
            }
            currentFolderList=imageFolderList;
        }else {
            ArrayList<MediaFolder> videoFolderList=new ArrayList<>();
            for(MediaFolder folder:mediaFolderList) {
                if(folder.videoCover()!=null) {
                    videoFolderList.add(folder);
                }
            }
            currentFolderList=videoFolderList;
        }
    }

    public List<MediaFolder> folderList() {
        return mediaFolderList;
    }

    public ArrayList<MediaFolder> getAllChecked() {
        int[] checked=super.getAllChecked(false);
        if(checked!=null) {
            ArrayList<MediaFolder> resultList = new ArrayList<>(checked.length);
            for (int index : checked) {
                resultList.add(mediaFolderList.get(index));
            }
            return resultList;
        }
        return null;
    }

    public void removeAt(int index) {
        super.removeAt(index,false);
        MediaFolder folder=currentFolderList.get(index);
        currentFolderList.remove(index);
        if(currentFolderList!=mediaFolderList) {
            mediaFolderList.remove(folder);
        }
        notifyItemRemoved(index);
    }

    private void removeWith(MediaFolder folder) {
        if(folder!=null) {
            int removeIndex=currentFolderList.indexOf(folder);
            if(removeIndex!=-1) {
                currentFolderList.remove(removeIndex);
                if (adapterMode != Mode.ALL) {
                    int index = mediaFolderList.indexOf(folder);
                    if (index != -1) {
                        if (mediaFolderList.get(index).removeAll(folder)) {
                            mediaFolderList.remove(index);
                        }
                    }
                }
                notifyItemRemoved(removeIndex);
            }
        }
    }

    //TODO block the file
    public void update(@NonNull List<MediaFolder> mediaFolders) {
        for(MediaFolder folder:mediaFolders) {
            int index=currentFolderList.indexOf(folder);
            if(index!=-1) {
                MediaFolder mediaFolder=currentFolderList.get(index);
                mediaFolder.updateWith(folder);
                if(adapterMode!=Mode.ALL) {
                    int jIndex=mediaFolderList.indexOf(folder);
                    if(jIndex!=-1) {
                        mediaFolderList.get(jIndex).updateWith(folder);
                    }
                }

                //TODO update size

            }else {
                currentFolderList.add(folder);
                notifyItemInserted(currentFolderList.size()-1);
            }
        }
    }

    public void replace(@NonNull MediaFolder folder) {
        if(folder.isEmpty()) {
            removeWith(folder);
        }else {
            int index=currentFolderList.indexOf(folder);
            if(index!=-1) {
                currentFolderList.set(index,folder);
                if(adapterMode!=Mode.ALL) {
                    int jIndex=mediaFolderList.indexOf(folder);
                    if(jIndex!=-1) {
                        MediaFolder temp = mediaFolderList.get(jIndex);
                        Collection<MediaFile> collection = folder.list();
                        if (adapterMode == Mode.IMAGE) {
                            temp.imageList().retainAll(collection);
                        } else {
                            temp.videoList().retainAll(collection);
                        }
                    }
                }
                notifyItemChanged(index);
            }
        }
    }

    public void addFolder(@NonNull MediaFolder folder) {
        int index=mediaFolderList.size()!=0?mediaFolderList.size()-1:0;
        mediaFolderList.add(index,folder);
        if(adapterMode!=Mode.ALL) {
            index=currentFolderList.size()!=0?currentFolderList.size()-1:0;
            if(adapterMode==Mode.IMAGE) {
                if (folder.imageCover() != null)
                    currentFolderList.add(index,folder);
            }else if(folder.videoCover()!=null) {
                currentFolderList.add(index,folder);
            }
        }
        notifyItemInserted(index);
    }

    public void setData(List<MediaFolder> folderList) {
        if(folderList!=null) {
            currentFolderList=folderList;
            if(adapterMode!=Mode.ALL) {
                mediaFolderList.addAll(folderList);
            }else {
                mediaFolderList = folderList;
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public void notifyAboutChange() {
        mediaFolderList= DataController.controllerInstance()
                .getFolders();
        if(adapterMode==Mode.ALL) {
            currentFolderList=mediaFolderList;
        }else {
            initCurrentList();
        }
        notifyDataSetChanged();
    }

    public  List<MediaFolder> getData() {
        return currentFolderList;
    }

    public Mode getAdapterMode() {
        return adapterMode;
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelableArrayList(KEY_DATA,(ArrayList<MediaFolder>)(mediaFolderList));
        outState.putString(KEY_MODE,adapterMode.name());
    }

    public enum Mode {

        ALL (1),

        VIDEO(2),

        IMAGE (0);

        Mode(int ni){
            nativeInt = ni;
        }

        final int nativeInt;

    }

}
