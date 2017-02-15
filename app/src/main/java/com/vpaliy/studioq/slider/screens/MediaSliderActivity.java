package com.vpaliy.studioq.slider.screens;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.vpaliy.studioq.model.MediaFile;
import com.vpaliy.studioq.slider.adapters.ContentAdapter;
import com.vpaliy.studioq.slider.adapters.NavigationAdapter;
import com.vpaliy.studioq.slider.listeners.OnBarChangeStateListener;
import com.vpaliy.studioq.slider.listeners.OnSliderEventListener;
import com.vpaliy.studioq.slider.tranformations.ZoomIn;
import com.vpaliy.studioq.slider.utils.DeleteCase;
import com.vpaliy.studioq.slider.utils.PhotoSlider;
import com.vpaliy.studioq.slider.utils.detailsProvider.DetailsProvider;
import com.vpaliy.studioq.utils.ProjectUtils;
import com.vpaliy.studioq.utils.snackbarUtils.ActionCallback;
import com.vpaliy.studioq.utils.snackbarUtils.SnackbarWrapper;

import java.util.ArrayList;

import android.support.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MediaSliderActivity extends AppCompatActivity
        implements OnBarChangeStateListener, OnSliderEventListener {

    @BindView(R.id.actionBar)
    protected Toolbar actionBar;

    @BindView(R.id.mediaNavigator)
    protected RecyclerView mediaNavigator;

    @BindView(R.id.mediaSlider)
    protected PhotoSlider mediaSlider;

    private final VisibilityController visibilityController=new VisibilityController();
    private  Thread navigationThread=new Thread(visibilityController);

    private ArrayList<MediaFile> mediaData;
    private boolean hasDeletionOccurred;
    private int startPosition;

    private ContentAdapter contentAdapter;
    private NavigationAdapter navigationAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);
        ButterKnife.bind(this);
        if(savedInstanceState==null) {
            savedInstanceState = getIntent().getExtras();
        }
        initUI(savedInstanceState);
        initActionBar();
    }

    private void initActionBar() {
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);

        actionBar.setVisibility(View.INVISIBLE);
        actionBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        actionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if(getSupportActionBar()==null) {
            actionBar.setTitle(null);
            setSupportActionBar(actionBar);
        }

    }

    private void initUI(Bundle args) {
        mediaData=args.getParcelableArrayList(ProjectUtils.MEDIA_DATA);
        startPosition=args.getInt(ProjectUtils.POSITION);
        hasDeletionOccurred=args.getBoolean(ProjectUtils.DELETED);
        mediaSlider.setAdapter((contentAdapter=new ContentAdapter(this,mediaData,this)));
        mediaSlider.post(new Runnable() {
            @Override
            public void run() {
                mediaSlider.setCurrentItem(startPosition);
            }
        });

        initTransformation();

        mediaNavigator.setAdapter((navigationAdapter=new NavigationAdapter(MediaSliderActivity.this,mediaData,this)));
        mediaNavigator.setLayoutManager(new LinearLayoutManager(MediaSliderActivity.this, LinearLayoutManager.HORIZONTAL,false));
        mediaNavigator.setItemAnimator(new DefaultItemAnimator());
        mediaNavigator.setVisibility(View.INVISIBLE);
        mediaNavigator.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                visibilityController.turnOn();
            }
        });
        onClick(startPosition);


    }


    private void initTransformation() {
        mediaSlider.setPageTransformer(false,new ZoomIn());
    }

    @Override
    public void onClick(int position) {
        mediaSlider.setCurrentItem(position);
        //TODO make the rect for selected navigation image
        mediaNavigator.scrollToPosition(position);
        if(navigationThread.isAlive()) {
            navigationThread.interrupt();
            hideNavigationContentList();
        } else {
            showNavigationContentList();
        }
    }



    @Override
    public void onSwitchActionBarOn() {
        actionBar.setVisibility(View.VISIBLE);
        actionBar.setAlpha(0.0f);
        actionBar.animate()
                .translationY(0.0f)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        actionBar.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onSwitchActionBarOff() {
        actionBar.animate()
                .translationY(-actionBar.getHeight())
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        actionBar.setVisibility(View.INVISIBLE);
                    }
                });
    }


    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        int flags=0;
        if (!hasFocus) {
            flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;

            if (Build.VERSION.SDK_INT >= 19) {
                flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
        }
        applyFlags(flags);

    }

    private void applyFlags(final int flags) {
        final View decorView=getWindow().getDecorView();
        decorView.post(new Runnable() {
            @Override
            public void run() {
                decorView.setSystemUiVisibility(flags);
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
              //  snapNavigation();
                DeleteCase.startWith(this,mediaData)
                    .subscribeForChange(contentAdapter)
                    .subscribeForChange(navigationAdapter)
                    .startUIChain();
                return true;
            }
            case R.id.shareItem:
                final MediaFile mediaFile=mediaData.get(mediaSlider.getCurrentItem());
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
                DetailsProvider.provideFor(this,mediaData.get(mediaSlider.getCurrentItem()));
                break;

            case R.id.filter: {
                Intent dataIntent=new Intent(this,FilterActivity.class);
                dataIntent.putExtra(ProjectUtils.MEDIA_DATA,mediaData.get(mediaSlider.getCurrentItem()));
                //    dataIntent.putExtra(ProjectUtils.BITMAP,contentAdapter.getCurrentBitmap());
                startActivity(dataIntent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void deleteOption() {
        final View victim=mediaSlider.findViewWithTag(mediaSlider.getCurrentItem());
        victim.animate()
                .scaleY(0.0f)
                .scaleX(0.0f)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        int currentIndex=mediaSlider.getCurrentItem();
                        if(mediaData.size()>1) {
                            mediaSlider.setScrollDurationFactor(3);
                            //move forward in this case
                            if(currentIndex!=(mediaData.size()-1)) {
                                mediaSlider.setCurrentItem(currentIndex+1);
                                mediaSlider.lockLeftOnTransform();
                            }else {
                                //or move back in this case
                                mediaSlider.setCurrentItem(currentIndex-1);
                                mediaSlider.lockRightOnTransform();
                            }
                            mediaSlider.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mediaSlider.setScrollDurationFactor(1);
                                    mediaSlider.unLockSwiping();
                                }
                            },300);
                        }
                        performDelete(mediaData.get(currentIndex));

                    }
                }).start();
    }

    private void performDelete(@NonNull MediaFile deleteFile) {
        SnackbarWrapper.start(ButterKnife.findById(this,R.id.rootView),
            "File:"+deleteFile.mediaFile().getName()+" has been moved to trash",7000)
        .callback(new ActionCallback("UNDO") {
            @Override
            public void onCancel() {

            }

            @Override
            public void onPerform() {

            }
        }).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ProjectUtils.MEDIA_DATA,(mediaData));
        outState.putInt(ProjectUtils.POSITION,startPosition);
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

            hideNavigationContentList();
        }

        void turnOn() {
            isTurnedOff = false;
        }

    }


    private void snapNavigation() {
        mediaNavigator.animate()
                .translationY(mediaNavigator.getHeight())
                .setDuration(50)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mediaNavigator.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void hideNavigationContentList() {
        mediaNavigator.animate()
                .translationY(mediaNavigator.getHeight())
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mediaNavigator.setVisibility(View.INVISIBLE);
                    }
                });
        onSwitchActionBarOff();
    }

    private void showNavigationContentList() {
        mediaNavigator.setVisibility(View.VISIBLE);
        mediaNavigator.animate()
                .translationY(0)
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
                        if(navigationThread.getState()!=Thread.State.NEW)
                            navigationThread=new Thread(visibilityController);
                        navigationThread.start();
                    }
                });
    }



    @Override
    public boolean isNavigationActivated() {
        return mediaNavigator.getVisibility()==View.VISIBLE;
    }

    @Override
    public void onBackPressed() {
        if(hasDeletionOccurred) {
            Intent data=new Intent();
            data.putParcelableArrayListExtra(ProjectUtils.MEDIA_DATA,mediaData);
            setResult(RESULT_OK,data);
        }
        super.onBackPressed();
    }
}