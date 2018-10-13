package hackman.trevor.copycat;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by Trevor on 3/3/2018.
 * The Title
 */

public class Title extends android.support.v7.widget.AppCompatImageView {
    private static final int titlePopDuration = 1100;
    private TimeInterpolator interpolator;

    public Title(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Gotta start unseen for pop-in animation
        this.setAlpha(0.0f);
        this.setScaleY(0);
        this.setScaleX(0);

        interpolator = new TimeInterpolator() {
            @Override
            public float getInterpolation(float v) {
                return -2*v*v + 3*v; // Allows me two animations in one! Peaks at v = 0.75 then down to (1,1)
            }
        };
    }

    void fadeIn() {
        this.animate().alpha(1.0f).setDuration(MainActivity.mainFadeDuration);
    }

    void fadeOut() {
        this.animate().alpha(0.0f).setDuration(MainActivity.mainFadeDuration);
    }

    void popIn() {
        this.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setDuration(titlePopDuration)
                .setInterpolator(interpolator);
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