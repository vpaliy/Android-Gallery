package com.vpaliy.studioq.slider.screens.cases;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.support.v7.widget.Toolbar;

import com.vpaliy.studioq.R;
import com.vpaliy.studioq.utils.Permissions;

import android.view.View;
import android.view.animation.DecelerateInterpolator;

import android.support.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("All")
public class NavigationCase {

    @BindView(R.id.actionBar)
    protected Toolbar actionBar;

    @BindView(R.id.mediaNavigator)
    protected View navigationView;

    @NonNull
    private View decorView;

    private int showDuration=150;
    private int hideDuration=150;

    private final VisibilityController visibilityController=new VisibilityController();
    private  Thread navigationThread=new Thread(visibilityController);

    private volatile boolean isBlocked=false;

    private NavigationCase(@NonNull Activity activity, @NonNull View decorView) {
        ButterKnife.bind(this,activity);
        this.decorView=decorView;
    }

    public void show() {
        if(!isBlocked) {
            navigationView.post(new Runnable() {
                @Override
                public void run() {
                    navigationView.setVisibility(View.VISIBLE);
                    navigationView.animate()
                            .setDuration(showDuration)
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
                            }).start();
                }
            });
        }
    }

    public void hide(){
        navigationView.post(new Runnable() {
            @Override
            public void run() {
                navigationView.animate()
                        .translationY(navigationView.getHeight())
                        .setDuration(hideDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
                                onSwitchActionBarOff();
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                navigationView.setVisibility(View.INVISIBLE);
                                //it redraws, so the UI should be invisible by this time
                                onWindowFocusChanged(false);
                            }
                        }).start();
            }
        });
    }

    public void block() {
        isBlocked=true;
        hide();
    }

    public void unBlock() {
        isBlocked=false;
    }

    private void onSwitchActionBarOn() {
        actionBar.setVisibility(View.VISIBLE);
        actionBar.animate()
                .translationY(0.0f)
                .setDuration(showDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        actionBar.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void onSwitchActionBarOff() {
        actionBar.animate()
                .translationY(-actionBar.getHeight())
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .alpha(1.0f)
                .setDuration(hideDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        actionBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void onWindowFocusChanged(boolean hasFocus) {
        int flags=0;
        if (!hasFocus) {
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            if (Permissions.checkForVersion(19)) {
                flags|= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            }else if(Permissions.checkForVersion(16)) {
                flags|=View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            }
        }
        applyFlags(flags);

    }

    private void applyFlags(final int flags) {
        decorView.post(new Runnable() {
            @Override
            public void run() {
                decorView.setSystemUiVisibility(flags);
            }
        });
    }

    public void makeAction() {
        if(navigationThread.isAlive()) {
            navigationThread.interrupt();
            hide();
        } else {
            show();
        }
    }

    public void carryOn() {
        visibilityController.turnOn();
    }

    public boolean isActivated() {
        return navigationView.getVisibility()==View.VISIBLE;
    }

    public static NavigationCase start(@NonNull Activity activity, @NonNull View decorView) {
        return new NavigationCase(activity,decorView);
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

            hide();
        }

        void turnOn() {
            isTurnedOff = false;
        }

    }
}
