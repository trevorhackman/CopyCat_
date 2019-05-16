package hackman.trevor.copycat.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;

import hackman.trevor.copycat.MainActivity;
import hackman.trevor.copycat.R;

@SuppressLint("ViewConstructor")
public class PlaySymbol extends AppCompatImageView {
    private AnimatorSet gyrateSet;
    private ObjectAnimator scaleY;
    private ObjectAnimator scaleX;
    private int gyrationDuration = 725;
    private float scale = 1.05f;

    public PlaySymbol(MainActivity main) {
        super(main);
        setBackground(ContextCompat.getDrawable(main, R.drawable.play_symbol));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslationZ(99);
        }

        setScaleX(0.96f);
        setScaleY(0.96f);
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

    public void fadeOut() {
        gyrateSet.end();
        animate().scaleX(0.3f).scaleY(0.3f).alpha(0.0f)
                .setDuration(GameScreen.mainFadeDuration);
    }

    public void returnToNormal() {
        Runnable onEnd = new Runnable() {
            @Override
            public void run() {
                gyrateSet.start();
            }
        };
        animate().alpha(1.0f).scaleY(1.0f).scaleX(1.0f).withEndAction(onEnd).setDuration(1000);
    }

    public void gyrate() {
        gyrateSet.start();
    }
}
