package com.vpaliy.studioq.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.activities.utils.eventBus.ExitEvent;
import com.vpaliy.studioq.adapters.UtilReviewAdapter;
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.activities.utils.eventBus.EventBusProvider;
import com.vpaliy.studioq.utils.ProjectUtils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import static butterknife.ButterKnife.findById;

public class MediaUtilReviewFragment extends Fragment {

    private final static String TAG=MediaUtilReviewFragment.class.getSimpleName();


    private UtilReviewAdapter adapter;


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
        if(savedInstanceState==null) {
            savedInstanceState = getArguments();
            ArrayList<MediaFile> temp=savedInstanceState.getParcelableArrayList(ProjectUtils.MEDIA_DATA);
            adapter = new UtilReviewAdapter(getContext(), temp);
        }else {
            adapter=new UtilReviewAdapter(getContext(),savedInstanceState);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_util_review,container,false);
    }


    @Override
    public void onViewCreated(final View root, @Nullable Bundle savedInstanceState) {
        if(root!=null) {
            Toolbar actionBar=findById(root,R.id.actionBar);
            initToolbar(actionBar);
            //
            RecyclerView mediaRecyclerView = findById(root, R.id.mediaRecyclerView);
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
            mediaRecyclerView.setAdapter(adapter);

        }
    }

    private void initToolbar(@NonNull Toolbar actionBar) {
        actionBar.inflateMenu(R.menu.review_menu);
        actionBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.create:
                        showDialog();
                        return true;
                }
                return false;
            }
        });
        actionBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        actionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

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


    private void showDialog() {
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        adapter.saveState(outState);
        super.onSaveInstanceState(outState);
    }
}
