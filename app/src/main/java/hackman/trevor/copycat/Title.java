package hackman.trevor.copycat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

/**
 * Created by Trevor on 3/3/2018.
 * The Title
 */

public class Title extends android.support.v7.widget.AppCompatImageView {
    private static final int titlePopDuration = 1000;

    private ObjectAnimator scaleY;
    private ObjectAnimator scaleX;
    private ObjectAnimator fadeOutTitle;
    private ObjectAnimator fadeInTitle;
    private AnimatorSet animatorSet;

    public Title(Context context, AttributeSet attrs) {
        super(context, attrs);

        fadeOutTitle = ObjectAnimator.ofFloat(this, "alpha", 0.0f);
        fadeOutTitle.setDuration(MainActivity.mainFadeDuration);

        fadeInTitle = ObjectAnimator.ofFloat(this, "alpha", 1.0f);
        fadeInTitle.setDuration(MainActivity.mainFadeDuration);
    }

    void fadeIn() {
        fadeInTitle.start();
    }

    void fadeOut() {
        fadeOutTitle.start();
    }

    void popIn() {
        this.setAlpha(1.0f);
        
        this.setScaleY(0);
        this.setScaleX(0);
        scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1.0f);
        scaleX.setDuration(titlePopDuration);
        scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1.0f);
        scaleY.setDuration(titlePopDuration);
        animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleY, scaleX);
        animatorSet.setStartDelay(0);
        animatorSet.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float v) {
                return -2*v*v + 3*v; // Allows me two animations in one! Peaks at v = 0.75 then down to (1,1)
            }
        });

        animatorSet.start();
    }

    void flexSize(int height, int width) {
        ViewGroup.LayoutParams params = this.getLayoutParams();
        // Effectively Portrait
        if (width <= height) {
            params.width = width;
        }
        // Effectively Landscape
        else //if (height < width)
            params.width = Math.min((width + 3 * height) / 4, width);
    }
}