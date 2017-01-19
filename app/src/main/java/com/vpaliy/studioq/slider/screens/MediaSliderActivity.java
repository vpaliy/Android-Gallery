package com.vpaliy.studioq.slider.screens;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import com.vpaliy.studioq.R;
import com.vpaliy.studioq.media.MediaFile;
import com.vpaliy.studioq.slider.adapters.ContentAdapter;
import com.vpaliy.studioq.slider.adapters.NavigationAdapter;
import com.vpaliy.studioq.slider.listeners.OnBarChangeStateListener;
import com.vpaliy.studioq.slider.listeners.OnSliderEventListener;
import com.vpaliy.studioq.slider.tranformations.ZoomIn;
import com.vpaliy.studioq.slider.utils.PhotoSlider;
import com.vpaliy.studioq.slider.utils.detailsProvider.DetailsProvider;
import com.vpaliy.studioq.utils.FileUtils;
import com.vpaliy.studioq.utils.ProjectUtils;
import java.util.ArrayList;

public class MediaSliderActivity extends AppCompatActivity
        implements OnBarChangeStateListener, OnSliderEventListener {

    private ArrayList<MediaFile> mMediaFileList;
    private boolean hasDeletionOccurred;
    private int mStartPosition;
    private Toolbar mActionBar;
    private RecyclerView mNavigationRecyclerView;
    private PhotoSlider mSlidePager;
    private final VisibilityController mVisibilityController=new VisibilityController();
    private  Thread mNavigationRecyclerViewThread=new Thread(mVisibilityController);

    private ContentAdapter contentAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slider_media_layout);

        if(savedInstanceState==null) {
            savedInstanceState = getIntent().getExtras();
        }
        initUI(savedInstanceState);
        initActionBar();
    }

    private void initActionBar() {
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        mActionBar=(Toolbar)(findViewById(R.id.actionBar));
        mActionBar.setVisibility(View.INVISIBLE);
        // mActionBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mActionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        //TODO don't forget about different sizes of icon
        //TODO consider screen rotation here
        if(getSupportActionBar()==null) {
            //TODO consider something about this action bar
            mActionBar.setTitle("");
            setSupportActionBar(mActionBar);
        }

    }

    private void initUI(Bundle args) {
        mMediaFileList=args.getParcelableArrayList(ProjectUtils.MEDIA_DATA);
        mStartPosition=args.getInt(ProjectUtils.POSITION);
        hasDeletionOccurred=args.getBoolean(ProjectUtils.DELETED);
        mSlidePager=(PhotoSlider) (findViewById(R.id.slider));
        mSlidePager.setAdapter((contentAdapter=new ContentAdapter(this,mMediaFileList,mStartPosition,this)));
        mSlidePager.setCurrentItem(mStartPosition);
        initPager();
        mNavigationRecyclerView = (RecyclerView) (findViewById(R.id.mediaSliderList));
        mNavigationRecyclerView.setAdapter(new NavigationAdapter(MediaSliderActivity.this,mMediaFileList,this));
        mNavigationRecyclerView.setLayoutManager(new LinearLayoutManager(MediaSliderActivity.this, LinearLayoutManager.HORIZONTAL,false));
        mNavigationRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mNavigationRecyclerView.setVisibility(View.INVISIBLE);
        mNavigationRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mVisibilityController.turnOn();
            }
        });
        onClick(mStartPosition);


    }


    private void initPager() {
        mSlidePager.setPageTransformer(false,new ZoomIn());
    }


    @Override
    public void onClick(int position) {
        mSlidePager.setCurrentItem(position);
        //TODO make the rect for selected navigation image
        mNavigationRecyclerView.scrollToPosition(position);
        if(mNavigationRecyclerViewThread.isAlive()) {
            mNavigationRecyclerViewThread.interrupt();
            hideNavigationContentList();
        } else {
            showNavigationContentList();
        }
    }



    @Override
    public void onSwitchActionBarOn() {
        mActionBar.setVisibility(View.VISIBLE);
        mActionBar.setAlpha(0.0f);
        mActionBar.animate()
                .translationY(0.0f)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mActionBar.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onSwitchActionBarOff() {
        mActionBar.animate()
                .translationY(-mActionBar.getHeight())
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mActionBar.setVisibility(View.INVISIBLE);
                    }
                });
    }


    //TODO take care of hiding and showing status bar or make it transparent
    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MediaSliderActivity.super.onWindowFocusChanged(hasFocus);
                View decorView=getWindow().getDecorView();
                if (!hasFocus) {
                    int flags=   View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN;

                    if(Build.VERSION.SDK_INT>=19) {
                        flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
                    }

                    decorView.setSystemUiVisibility(flags);
                }else {
                    decorView.setSystemUiVisibility(0);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.slider_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.home:
                onBackPressed();
                return true;

            case R.id.deleteItem: {
                final MediaFile mediaFile = mMediaFileList.get(mSlidePager.getCurrentItem());
                //TODO consider languages here
                new AlertDialog.Builder(this)
                        .setTitle("Are you sure that you want to delete " + mediaFile.mediaFile().getName())
                        .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //EMPTY block
                            }
                        })
                        .setPositiveButton(R.string.Okay, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int index = mSlidePager.getCurrentItem();
                                hasDeletionOccurred = true;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        FileUtils.deleteFile(MediaSliderActivity.this, mediaFile);
                                    }
                                }).start();
                                if (mMediaFileList.size() == 1) {
                                    mMediaFileList.remove(index);
                                    onBackPressed();
                                } else {
                                    mMediaFileList.remove(index);
                                    mSlidePager.getAdapter().notifyDataSetChanged();
                                    mNavigationRecyclerView.getAdapter().notifyItemRemoved(index);
                                }
                            }
                        })
                        .show();
                return true;
            }
            case R.id.shareItem:
                final MediaFile mediaFile=mMediaFileList.get(mSlidePager.getCurrentItem());
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mediaFile.mediaFile()));
                if(mediaFile.getType()== MediaFile.Type.VIDEO) {
                    intent.setType("video/*");
                }else {
                    intent.setType("image/*");
                }
                //TODO language here
                startActivity(Intent.createChooser(intent,"Share via"));
                return true;

            case R.id.showInfo:
                DetailsProvider.provideFor(this,mMediaFileList.get(mSlidePager.getCurrentItem()));
                break;

            case R.id.filter: {
                Intent dataIntent=new Intent(this,FilterActivity.class);
                dataIntent.putExtra(ProjectUtils.MEDIA_DATA,mMediaFileList.get(mSlidePager.getCurrentItem()));
            //    dataIntent.putExtra(ProjectUtils.BITMAP,contentAdapter.getCurrentBitmap());
                startActivity(dataIntent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }



    //TODO before release consider replacement with orientation|keyboard|andStuff
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ProjectUtils.MEDIA_DATA,(mMediaFileList));
        outState.putInt(ProjectUtils.POSITION,mStartPosition);
        outState.putBoolean(ProjectUtils.DELETED,hasDeletionOccurred);
    }


    private final class VisibilityController implements Runnable {

        private volatile  boolean isTurnedOff;

        @Override
        public void run() {
            do {
                isTurnedOff = true;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    return;
                }
            }while(!isTurnedOff);

            //TODO you don't need to worry about this, because you can implement a double-click to enlarge the image
          //  if(mNavigationRecyclerView.getAnimation()==null||mNavigationRecyclerView.getAnimation().hasEnded())
                hideNavigationContentList();
        }

        public void turnOn() {
            isTurnedOff = false;
        }

    }


    private void hideNavigationContentList() {
        mNavigationRecyclerView.animate()
                .translationY(mNavigationRecyclerView.getHeight())
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mNavigationRecyclerView.setVisibility(View.INVISIBLE);
                    }
                });
        onWindowFocusChanged(false);
        onSwitchActionBarOff();
    }

    private void showNavigationContentList() {
        mNavigationRecyclerView.setVisibility(View.VISIBLE);
        mNavigationRecyclerView.setAlpha(0.f);
        mNavigationRecyclerView.animate()
                .translationY(0)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        onSwitchActionBarOn();
                        onWindowFocusChanged(true);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if(mNavigationRecyclerViewThread.getState()!=Thread.State.NEW)
                            mNavigationRecyclerViewThread=new Thread(mVisibilityController);
                        mNavigationRecyclerViewThread.start();
                    }
                });
    }



    @Override
    public boolean isNavigationActivated() {
        return mNavigationRecyclerView.getVisibility()==View.VISIBLE;
    }

    @Override
    public void onBackPressed() {
        if(hasDeletionOccurred) {
            Intent data=new Intent();
            data.putParcelableArrayListExtra(ProjectUtils.MEDIA_DATA,mMediaFileList);
            setResult(RESULT_OK,data);
        }
        super.onBackPressed();
    }
}
