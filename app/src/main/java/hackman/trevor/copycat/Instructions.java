package hackman.trevor.copycat;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

import hackman.trevor.tlibrary.library.TMath;

public class Instructions extends android.support.v7.widget.AppCompatTextView {
    private static final int instructionsInDuration = 500;
    private static final int instructionsOutDuration = 1200;
    private static final int instructionsOutDelay = 2000;

    private ObjectAnimator fadeIn;
    private ObjectAnimator fadeOut;
    private ObjectAnimator dropOut;
    private ObjectAnimator yShrinkOut;
    private Animator.AnimatorListener endListener;

    public Instructions(Context context, AttributeSet attrs) {
        super(context, attrs);
        final android.support.v7.widget.AppCompatTextView this_ = this;

        fadeOut = ObjectAnimator.ofFloat(this, "alpha", 0.0f);
        fadeOut.setDuration(instructionsOutDuration);
        fadeOut.setStartDelay(instructionsOutDelay);

        fadeIn = ObjectAnimator.ofFloat(this, "alpha", 1.0f);
        fadeIn.setDuration(instructionsInDuration);

        Interpolator interpolator = new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return (float)Math.pow(input, 2.5);
            }
        };

        dropOut = ObjectAnimator.ofFloat(this, "translationY", TMath.convertHpToPixel(120, context));
        dropOut.setDuration(instructionsOutDuration);
        dropOut.setStartDelay(instructionsOutDelay);
        dropOut.setInterpolator(interpolator);
        dropOut.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) {}
            @Override public void onAnimationCancel(Animator animator) {}
            @Override public void onAnimationRepeat(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                this_.setTranslationY(0);
            }
        });

        yShrinkOut = ObjectAnimator.ofFloat(this, "scaleY", 0.25f);
        yShrinkOut.setDuration(instructionsOutDuration);
        yShrinkOut.setStartDelay(instructionsOutDelay);
        yShrinkOut.setInterpolator(interpolator);
        yShrinkOut.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) {}
            @Override public void onAnimationCancel(Animator animator) {}
            @Override public void onAnimationRepeat(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                this_.setScaleY(1);
            }
        });

        endListener = new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) {}
            @Override public void onAnimationCancel(Animator animator) {}
            @Override public void onAnimationRepeat(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                fadeOut.start();
                dropOut.start();
                yShrinkOut.start();
            }
        };

        fadeIn.addListener(endListener);
    }

    void flex() {
        float scale = .35f;
        float textSize = 20;
        float minTextSize = TMath.convertDpToPixel(textSize, getContext());
        float calculatedTextSize = TMath.convertMdToPixel(textSize * scale, getContext());

        this.setTextSize(Math.max(minTextSize, calculatedTextSize));
    }

    void fadeIn() {
        fadeIn.start();
    }

    void endAnimations() {
        fadeIn.removeListener(endListener);
        fadeIn.cancel();
        fadeOut.end();
        dropOut.end();
        yShrinkOut.end();
        fadeIn.addListener(endListener);
    }
}
