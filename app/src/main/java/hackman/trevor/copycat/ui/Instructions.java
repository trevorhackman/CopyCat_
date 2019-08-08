package hackman.trevor.copycat.ui;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.TypedValue;
import android.view.animation.Interpolator;

import androidx.core.content.ContextCompat;

import hackman.trevor.copycat.MainActivity;
import hackman.trevor.copycat.R;
import hackman.trevor.copycat.logic.Game;
import hackman.trevor.copycat.system.Keys;
import hackman.trevor.tlibrary.library.TDimensions;
import hackman.trevor.tlibrary.library.ui.GoodTextView;

@SuppressLint("ViewConstructor")
public class Instructions extends GoodTextView {
    private static final int instructionsInDuration = 500;
    private static final int instructionsOutDuration = 1200;
    private static final int instructionsOutDelay = 2000;

    private Runnable onFadeInEnd;
    private MainActivity main;

    public Instructions(MainActivity main) {
        super(main);
        this.main = main;

        setBackground(ContextCompat.getDrawable(main, R.drawable.instructions_rectangle));
        int padding = TDimensions.dpToPixel(7);
        setPadding(padding, padding, padding, padding);
        enableBold();
        modeUpdate();
        setAlpha(0f);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslationZ(9999);
        }

        final Interpolator interpolator = new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return (float)Math.pow(input, 2.5);
            }
        };

        onFadeInEnd = new Runnable() {
            @Override
            public void run() {
                animate()
                        .setStartDelay(instructionsOutDelay)
                        .alpha(0.0f)
                        .scaleY(0.25f)
                        .translationY(TDimensions.hpToPixel(120))
                        .setInterpolator(interpolator).setDuration(instructionsOutDuration).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        Instructions.this.setScaleY(1);
                        Instructions.this.setTranslationY(0);
                    }
                });
            }
        };
    }

    public void flex() {
        float scale = 1f;
        float textSize = 20;
        float minTextSize = TDimensions.dpToPixel(textSize);
        float calculatedTextSize = TDimensions.mdToPixels(textSize * scale);
        float finalTextSize = Math.max(minTextSize, calculatedTextSize);

        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalTextSize);
    }

    public void fadeIn() {
        animate()
                .setStartDelay(0)
                .alpha(1.0f)
                .setDuration(instructionsInDuration)
                .withEndAction(onFadeInEnd);
    }

    public void endAnimations() {
        animate().cancel(); // A view only has one ViewPropertyAnimator, so this cancels both the fade in and the fade out
        setScaleY(1);
        setTranslationY(0);
        setAlpha(0.0f);
    }

    public void modeUpdate() {
        Game.GameMode mode = Game.GameMode.valueOf(main.tPreferences().getString(Keys.gameMode, Game.GameMode.Classic.name()));
        if (mode == Game.GameMode.Classic) {
            setText(main.getString(R.string.Watch_and_repeat));
        }
        else {
            String text = main.getString(R.string.Playing) + " " + mode.displayName();
            setText(text);
        }
    }
}
