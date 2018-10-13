package hackman.trevor.copycat;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import hackman.trevor.tlibrary.library.TMath;

public class DeathScreen extends LinearLayout {
    private final int deathScreenInDuration = 1000;
    private final int deathScreenOutDuration = 500;

    private DeathScreen deathScreen = this;

    private TimeInterpolator rollInInterpolator;

    private ObjectAnimator rollIn;
    private ObjectAnimator fadeOut;

    private Button mainMenuButton;
    private Button playAgainButton;

    private TextView txt_gameOver;
    private TextView txt_scoreValue;
    private TextView txt_bestValue;
    private TextView txt_pressedValue;
    private TextView txt_correctValue;

    private LinearLayout row0;
    private LinearLayout row1;
    private LinearLayout row2;
    private LinearLayout row3;

    private MainActivity main;
    private int screenHeight;

    TextView txt_score;
    TextView txt_best;
    TextView txt_pressed;
    TextView txt_correct;

    boolean isDeathScreenUp = false;

    // The pressed/correct choices for the classic colors
    private String[] classicNames;

    // The pressed/correct choices for other color sets
    private String[] genericNames;

    public DeathScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void setUp(final MainActivity main) {
        this.main = main;
        mainMenuButton = findViewById(R.id.mainMenuButton);
        playAgainButton = findViewById(R.id.playAgainButton);
        setMainMenuButton(main);
        setPlayAgainButton(main);

        txt_gameOver   = findViewById(R.id.txt_gameOver);
        txt_scoreValue = findViewById(R.id.txt_score);
        txt_bestValue = findViewById(R.id.txt_best);
        txt_pressedValue = findViewById(R.id.txt_pressed);
        txt_correctValue = findViewById(R.id.txt_correct);

        txt_score = findViewById(R.id.score);
        txt_best = findViewById(R.id.best);
        txt_pressed = findViewById(R.id.pressed);
        txt_correct = findViewById(R.id.correct);

        row0 = findViewById(R.id.deathScreenRow0);
        row1 = findViewById(R.id.deathScreenRow1);
        row2 = findViewById(R.id.deathScreenRow2);
        row3 = findViewById(R.id.deathScreenRow3);

        fadeOut = ObjectAnimator.ofFloat(this, "alpha", 0.0f);
        fadeOut.setDuration(deathScreenOutDuration);
        fadeOut.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationStart(Animator animation) {
                // Don't want to be clickable while fading out, also stops multiclicks
                playAgainButton.setClickable(false);
                mainMenuButton.setClickable(false);

                isDeathScreenUp = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (main.startGameAfterFadeOut) {
                    main.startGameAfterFadeOut = false;
                    main.startGame();
                }

                // Manual bring to back
                final ViewGroup parent = (ViewGroup) deathScreen.getParent();
                parent.removeView(deathScreen);
                parent.addView(deathScreen, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    deathScreen.setTranslationZ(-1);
                }

                deathScreen.setAlpha(1.0f); // Make visible again
            }
        });

        rollInInterpolator = new TimeInterpolator() {
            @Override
            public float getInterpolation(float x) {
                return (float)Math.sin(x*Math.PI/2);
            }
        };

        // Get name sets for color sets
        classicNames = new String[4];
        genericNames = new String[4];
        classicNames[0] = main.getString(R.string.Green);
        classicNames[1] = main.getString(R.string.Red);
        classicNames[2] = main.getString(R.string.Yellow);
        classicNames[3] = main.getString(R.string.Blue);
        genericNames[0] = main.getString(R.string.Top_Left);
        genericNames[1] = main.getString(R.string.Top_Right);
        genericNames[2] = main.getString(R.string.Bot_Left);
        genericNames[3] = main.getString(R.string.Bot_Right);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int height = b-t;
        screenHeight = MainActivity.displayMetrics.heightPixels;

        // For dealing with configuration changes mid-animation
        boolean runAgain = false;
        if (rollIn != null && rollIn.isRunning()) {
            runAgain = true;
            rollIn.removeAllListeners();
        }

        rollIn = ObjectAnimator.ofFloat(this, "Y", screenHeight, (screenHeight - height)/2);
        rollIn.setDuration(deathScreenInDuration);
        Animator.AnimatorListener rollInListener = new Animator.AnimatorListener() {
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}

            @Override
            public void onAnimationStart(Animator animation) {
                // Prevent buttons from being clickable, from going to press state, and visually show that they're disabled during animation
                mainMenuButton.setEnabled(false);
                playAgainButton.setEnabled(false);

                deathScreen.bringToFront();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) { // Required pre-Kitkat for layouts coming to front to be visible
                    getParent().requestLayout();
                    ((View) getParent()).invalidate();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // TranslationZ/elevation added in api 21
                    deathScreen.setTranslationZ(999);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isDeathScreenUp = true;
                mainMenuButton.setEnabled(true);
                playAgainButton.setEnabled(true);

                // Make deathscreen buttons clickable once completely animated in
                playAgainButton.setClickable(true);
                mainMenuButton.setClickable(true);

                if (!main.inGame) {
                    main.mainButton.setText("");
                    main.playSymbolButton.reset();
                    playAgainButton.setClickable(true);
                    mainMenuButton.setClickable(true);
                }
            }
        };
        rollIn.setInterpolator(rollInInterpolator);


        if (!runAgain) {
            rollIn.addListener(rollInListener);
        }
        else {
            rollIn.start();
            rollIn.addListener(rollInListener);
        }
    }

    void flex(int height, int width) {
        ViewGroup.LayoutParams params = this.getLayoutParams();

        // Portrait
        if (width < height) params.width = LayoutParams.MATCH_PARENT;
        else { // Landscape
            params.width = (height + width)/2;
        }

        final float scale = 1f;
        float headTextSize = 36f;
        float innerTextSize = 21.6f;
        final float minScaleInner = 0.75f;
        float buttonTextSize = 16.2f;
        float rowBottomMargin = 6f;

        float minHeadText = TMath.convertDpToPixel(headTextSize, main);
        float minInnerText = TMath.convertDpToPixel(innerTextSize, main) * minScaleInner;
        float minButtonTextSize = TMath.convertDpToPixel(buttonTextSize, main);
        float minRowBottomMargin = TMath.convertDpToPixel(rowBottomMargin, main);

        float calculatedHeadText = TMath.convertMdToPixel(headTextSize * scale, main);
        float calculatedInnerText = TMath.convertMdToPixel(innerTextSize * scale, main);
        float calculatedButtonText = TMath.convertMdToPixel(buttonTextSize * scale, main);
        float calculatedRowBottomMargin = TMath.convertMdToPixel(rowBottomMargin * scale, main);

        float finalHeadText = Math.max(calculatedHeadText, minHeadText);
        float finalInnerText = Math.max(calculatedInnerText, minInnerText);
        float finalButtonSize = Math.max(calculatedButtonText, minButtonTextSize);
        float finalRowBottomMargin = Math.max(calculatedRowBottomMargin, minRowBottomMargin);

        txt_gameOver.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalHeadText);

        txt_scoreValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_bestValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_pressedValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_correctValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);

        txt_score.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_best.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_pressed.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_correct.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);

        LinearLayout.LayoutParams paramsR0 = (LinearLayout.LayoutParams)row0.getLayoutParams();
        LinearLayout.LayoutParams paramsR1 = (LinearLayout.LayoutParams)row1.getLayoutParams();
        LinearLayout.LayoutParams paramsR2 = (LinearLayout.LayoutParams)row2.getLayoutParams();
        LinearLayout.LayoutParams paramsR3 = (LinearLayout.LayoutParams)row3.getLayoutParams();

        paramsR0.setMargins(0, 0, 0, (int)finalRowBottomMargin);
        paramsR1.setMargins(0, 0, 0, (int)finalRowBottomMargin);
        paramsR2.setMargins(0, 0, 0, (int)finalRowBottomMargin);
        paramsR3.setMargins(0, 0, 0, (int)finalRowBottomMargin);

        mainMenuButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalButtonSize);
        playAgainButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalButtonSize);

        float multiplier = 2f;
        ViewGroup.LayoutParams mainMenuButtonParams = mainMenuButton.getLayoutParams();
        ViewGroup.LayoutParams playAgainButtonParams = playAgainButton.getLayoutParams();

        mainMenuButtonParams.height = (int)(finalButtonSize * multiplier);
        playAgainButtonParams.height = (int)(finalButtonSize * multiplier);
    }

    void setValues(int score, int pressed, int correct) {
        String scoreNum_text = "" + score;
        deathScreen.txt_score.setText(scoreNum_text);

        int highScoreNum = main.myPreferences.getInt("highscore", 0);
        String highScoreNum_text = "" + highScoreNum;
        deathScreen.txt_best.setText(highScoreNum_text);

        // Select the proper nameSet for the colorSet
        String[] names = main.myPreferences.getInt("colors", SettingsScreen.CLASSIC) == SettingsScreen.CLASSIC ? classicNames : genericNames;

        // Indicate what the pressed and correct buttons were
        String pressedString = names[pressed];

        // Occurs if user hits extra buttons and therefore there is no correct button
        String correctString;
        if (correct == -1) correctString = "---";
        else correctString = names[correct];

        deathScreen.txt_pressed.setText(pressedString);
        deathScreen.txt_correct.setText(correctString);
    }

    void animateIn() {
        rollIn.start();
    }

    void animateOut() {
        fadeOut.start();
    }

    void performMainMenuClick() {
        mainMenuButton.performClick();
    }

    private void setMainMenuButton(final MainActivity main) {
        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                main.mainButtonEnabled = true;
                deathScreen.animateOut();

                // Fade in UI
                main.mainFadeInAnimation();

                // Play button sound
                AndroidSound.sounds[AndroidSound.click].play(AndroidSound.VOLUME_CLICK);
            }
        });
        mainMenuButton.setClickable(false); // setOnClickListener resets clickable to true, so we need to set it to false here
    }

    private void setPlayAgainButton(final MainActivity main) {
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.playSymbolButton.setAlpha(0.0f);

                main.startGameAfterFadeOut = true;
                main.mainButton.setText("1"); // Starting level is always 1
                deathScreen.animateOut();

                // Play button sound
                AndroidSound.sounds[AndroidSound.click].play(AndroidSound.VOLUME_CLICK);
            }
        });
        playAgainButton.setClickable(false); // setOnClickListener resets clickable to true, so we need to set it to false here
    }
}
