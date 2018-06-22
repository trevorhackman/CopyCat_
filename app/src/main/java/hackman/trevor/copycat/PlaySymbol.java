package hackman.trevor.copycat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.ViewGroup;

import hackman.trevor.tlibrary.library.TMath;

import static hackman.trevor.tlibrary.library.TLogging.log;

/**
 * Created by Trevor on 3/14/2018.
 * The Play Symbol
 */

public class PlaySymbol extends AppCompatButton {
    private ObjectAnimator scaleY;
    private ObjectAnimator scaleX;
    private AnimatorSet animatorSet;
    private int animationDuration = 725;
    private float scale = 1.09f;

    public PlaySymbol(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    void myAnimation() {
        scaleX = ObjectAnimator.ofFloat(this, "scaleX", scale);
        scaleY = ObjectAnimator.ofFloat(this, "scaleY", scale);
        scaleX.setDuration(animationDuration);
        scaleY.setDuration(animationDuration);
        scaleX.setRepeatCount(-1);
        scaleY.setRepeatCount(-1);
        scaleX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleY.setRepeatMode(ObjectAnimator.REVERSE);

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }

    void flexSize(int height, int width) {
        ViewGroup.LayoutParams params = this.getLayoutParams();

        float scale = 0.40f;
        float minDimension = Math.min(height, width);
        float dimensionSize = minDimension * scale;
        float minSize = TMath.convertDpToPixel(100, getContext());

        // divide Math.pow(2,0.5) to incribe into MainButton circle
        int size = (int)(Math.max(minSize, dimensionSize) / Math.pow(2, 0.5));

        params.height = size;
        params.width = size;
    }
}
