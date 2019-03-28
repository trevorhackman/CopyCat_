package hackman.trevor.copycat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.ViewGroup;

import hackman.trevor.tlibrary.library.TDimensions;

import static hackman.trevor.copycat.MainActivity.mainFadeDuration;

/**
 * Created by Trevor on 3/14/2018.
 * The Play Symbol
 */

public class PlaySymbol extends AppCompatButton {
    private AnimatorSet gyrateSet;
    private ObjectAnimator scaleY;
    private ObjectAnimator scaleX;
    private int gyrationDuration = 725;
    private float scale = 1.09f;

    public PlaySymbol(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        scaleX = ObjectAnimator.ofFloat(this, "scaleX", scale);
        scaleY = ObjectAnimator.ofFloat(this, "scaleY", scale);
        scaleX.setDuration(gyrationDuration);
        scaleY.setDuration(gyrationDuration);
        scaleX.setRepeatCount(-1);
        scaleY.setRepeatCount(-1);
        scaleX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleY.setRepeatMode(ObjectAnimator.REVERSE);

        gyrateSet = new AnimatorSet();
        gyrateSet.playTogether(scaleX, scaleY);
    }

    void fadeOut() {
        gyrateSet.end();
        animate().scaleX(0.3f).scaleY(0.3f).alpha(0.0f)
                .setDuration(mainFadeDuration);
    }

    void returnToNormal() {
        Runnable onEnd = new Runnable() {
            @Override
            public void run() {
                gyrateSet.start();
            }
        };
        animate().alpha(1.0f).scaleY(1.0f).scaleX(1.0f).withEndAction(onEnd).setDuration(1000);
    }

    void gyrate() {
        gyrateSet.start();
    }

    void flexSize(int height, int width) {
        ViewGroup.LayoutParams params = this.getLayoutParams();

        float scale = 0.40f;
        float minDimension = Math.min(height, width);
        float dimensionSize = minDimension * scale;
        float minSize = TDimensions.dpToPixel(100);

        // divide Math.pow(2,0.5) to inscribe into MainButton circle
        int size = (int)(Math.max(minSize, dimensionSize) / Math.pow(2, 0.5));

        params.height = size;
        params.width = size;
    }
}
