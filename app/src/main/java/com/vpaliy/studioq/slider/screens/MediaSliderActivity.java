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
import com.vpaliy.studioq.model.MediaFile;
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
        // mActionBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        //TODO don't forget about different sizes of icon
        //TODO consider screen rotation here
        if(getSupportActionBar()==null) {
            //TODO consider something about this action bar
            actionBar.setTitle("");
            setSupportActionBar(actionBar);
        }

    }

    private void initUI(Bundle args) {
        mediaData=args.getParcelableArrayList(ProjectUtils.MEDIA_DATA);
        startPosition=args.getInt(ProjectUtils.POSITION);
        hasDeletionOccurred=args.getBoolean(ProjectUtils.DELETED);
        mediaSlider.setAdapter((contentAdapter=new ContentAdapter(this,mediaData,startPosition,this)));
        mediaSlider.setCurrentItem(startPosition);

        initTransformation();

        mediaNavigator.setAdapter(new NavigationAdapter(MediaSliderActivity.this,mediaData,this));
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


    //TODO take care of hiding and showing status bar or make it transparent
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
                final MediaFile mediaFile = mediaData.get(mediaSlider.getCurrentItem());
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
                                int index = mediaSlider.getCurrentItem();
                                hasDeletionOccurred = true;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        FileUtils.deleteFile(MediaSliderActivity.this, mediaFile);
                                    }
                                }).start();
                                if (mediaData.size() == 1) {
                                    mediaData.remove(index);
                                    onBackPressed();
                                } else {
                                    mediaData.remove(index);
                                    mediaSlider.getAdapter().notifyDataSetChanged();
                                    mediaNavigator.getAdapter().notifyItemRemoved(index);
                                }
                            }
                        })
                        .show();
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



    //TODO before release consider replacement with orientation|keyboard|andStuff
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
        onWindowFocusChanged(false);
        onSwitchActionBarOff();
    }

    private void showNavigationContentList() {
        mediaNavigator.setVisibility(View.VISIBLE);
        mediaNavigator.setAlpha(0.f);
        mediaNavigator.animate()
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