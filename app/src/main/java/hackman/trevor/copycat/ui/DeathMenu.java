package hackman.trevor.copycat.ui;

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

import hackman.trevor.copycat.MainActivity;
import hackman.trevor.copycat.R;
import hackman.trevor.copycat.logic.Game;
import hackman.trevor.copycat.system.AndroidSound;
import hackman.trevor.copycat.system.Keys;
import hackman.trevor.tlibrary.library.TDimensions;
import hackman.trevor.tlibrary.library.ui.Llp;

public class DeathMenu extends LinearLayout {
    private final int deathScreenInDuration = 1000;
    private final int deathScreenOutDuration = 500;

    private DeathMenu deathMenu = this;

    private TimeInterpolator rollInInterpolator;

    private ObjectAnimator rollIn;
    private Runnable fadeOutStart; // Runs at start of fade out animation
    private Runnable fadeOutEnd; // Runs at end of fade out animation

    private Button mainMenuButton;
    private Button playAgainButton;

    private TextView txt_gameOver;
    private TextView txt_modeLeft;
    private TextView txt_scoreLeft;
    private TextView txt_bestLeft;
    private TextView txt_pressedLeft;
    private TextView txt_correctLeft;

    private LinearLayout rowM1;
    private LinearLayout row0;
    private LinearLayout row1;
    private LinearLayout row2;
    private LinearLayout row3;

    private MainActivity main;
    private int screenHeight;

    private TextView txt_mode;
    private TextView txt_score;
    private TextView txt_best;
    private TextView txt_pressed;
    private TextView txt_correct;

    public boolean isDeathScreenUp = false;
    public boolean isDeathScreenComing = false;

    // The pressed/correct choices for the classic colors
    private String[] classicNames;

    // The pressed/correct choices for other color sets
    private String[] genericNames;

    public DeathMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setUp(final MainActivity main) {
        this.main = main;
        mainMenuButton = findViewById(R.id.mainMenuButton);
        playAgainButton = findViewById(R.id.playAgainButton);
        setMainMenuButton(main);
        setPlayAgainButton(main);

        txt_gameOver = findViewById(R.id.txt_gameOver);
        txt_modeLeft = findViewById(R.id.dm_txt_mode);
        txt_scoreLeft = findViewById(R.id.txt_score);
        txt_bestLeft = findViewById(R.id.txt_best);
        txt_pressedLeft = findViewById(R.id.txt_pressed);
        txt_correctLeft = findViewById(R.id.txt_correct);

        txt_mode = findViewById(R.id.dm_mode);
        txt_score = findViewById(R.id.score);
        txt_best = findViewById(R.id.best);
        txt_pressed = findViewById(R.id.pressed);
        txt_correct = findViewById(R.id.correct);

        rowM1 = findViewById(R.id.deathScreenRowM1);
        row0 = findViewById(R.id.deathScreenRow0);
        row1 = findViewById(R.id.deathScreenRow1);
        row2 = findViewById(R.id.deathScreenRow2);
        row3 = findViewById(R.id.deathScreenRow3);

        fadeOutStart = new Runnable() {
            @Override
            public void run() {
                // Don't want to be clickable while fading out, also stops multi-clicks
                playAgainButton.setEnabled(false);
                mainMenuButton.setEnabled(false);

                isDeathScreenUp = false;
            }
        };
        fadeOutEnd = new Runnable() {
            @Override
            public void run() {
                if (main.gameScreen().startGameAfterFadeOut) {
                    main.gameScreen().startGameAfterFadeOut = false;
                    main.gameScreen().startGame();
                }
                else {
                    main.gameScreen().enableNonColorButtons(); // Enable buttons
                }

                // Manual bring to back
                final ViewGroup parent = (ViewGroup) deathMenu.getParent();
                parent.removeView(deathMenu);
                parent.addView(deathMenu, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    deathMenu.setTranslationZ(-1);
                }

                deathMenu.setAlpha(1.0f); // Make visible again
            }
        };

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
        screenHeight = TDimensions.getHeightPixels();

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
                isDeathScreenComing = true; // Set flag

                // Prevent buttons from being clickable, from going to press state, and visually show that they're disabled during animation
                mainMenuButton.setEnabled(false);
                playAgainButton.setEnabled(false);

                deathMenu.bringToFront();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) { // Required pre-Kitkat for layouts coming to front to be visible
                    getParent().requestLayout();
                    ((View) getParent()).invalidate();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // TranslationZ/elevation added in api 21
                    deathMenu.setTranslationZ(999);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isDeathScreenUp = true; // Set flag
                isDeathScreenComing = false; // Set flag
                main.gameScreen().mainButton.setText(""); // Clear score tracker

                // Give buttons enabled graphic and make clickable once completely animated in
                playAgainButton.setClickable(true);
                mainMenuButton.setClickable(true);
                playAgainButton.setEnabled(true);
                mainMenuButton.setEnabled(true);
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

    public void flex(int height, int width) {
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

        float minHeadText = TDimensions.dpToPixel(headTextSize);
        float minInnerText = TDimensions.dpToPixel(innerTextSize) * minScaleInner;
        float minButtonTextSize = TDimensions.dpToPixel(buttonTextSize);
        float minRowBottomMargin = TDimensions.dpToPixel(rowBottomMargin);

        float calculatedHeadText = TDimensions.mdToPixels(headTextSize * scale);
        float calculatedInnerText = TDimensions.mdToPixels(innerTextSize * scale);
        float calculatedButtonText = TDimensions.mdToPixels(buttonTextSize * scale);
        float calculatedRowBottomMargin = TDimensions.mdToPixels(rowBottomMargin * scale);

        float finalHeadText = Math.max(calculatedHeadText, minHeadText);
        float finalInnerText = Math.max(calculatedInnerText, minInnerText);
        float finalButtonSize = Math.max(calculatedButtonText, minButtonTextSize);
        float finalRowBottomMargin = Math.max(calculatedRowBottomMargin, minRowBottomMargin);

        txt_gameOver.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalHeadText);

        txt_modeLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_scoreLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_bestLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_pressedLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_correctLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);

        txt_mode.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_score.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_best.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_pressed.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);
        txt_correct.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalInnerText);

        Llp rowsLp = new Llp(Llp.MATCH);
        rowsLp.setPixelMargins(0, 0, 0, (int)finalRowBottomMargin);
        rowM1.setLayoutParams(rowsLp);
        row0.setLayoutParams(rowsLp);
        row1.setLayoutParams(rowsLp);
        row2.setLayoutParams(rowsLp);
        row3.setLayoutParams(rowsLp);

        mainMenuButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalButtonSize);
        playAgainButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalButtonSize);

        float multiplier = 2f;
        ViewGroup.LayoutParams mainMenuButtonParams = mainMenuButton.getLayoutParams();
        ViewGroup.LayoutParams playAgainButtonParams = playAgainButton.getLayoutParams();

        mainMenuButtonParams.height = (int)(finalButtonSize * multiplier);
        playAgainButtonParams.height = (int)(finalButtonSize * multiplier);
    }

    public void setValues(int score, int pressed, int correct) {
        String scoreNum_text = "" + score;
        txt_score.setText(scoreNum_text);

        int highScoreNum = main.tPreferences().getInt(main.gameScreen().game.getGameMode().name() + Keys.modeBest, 0);
        String highScoreNum_text = "" + highScoreNum;
        txt_best.setText(highScoreNum_text);

        // Select the proper nameSet for the colorSet
        String[] names = main.tPreferences().getInt(Keys.colors, SettingsMenu.CLASSIC) == SettingsMenu.CLASSIC ? classicNames : genericNames;

        // Indicate what the pressed and correct buttons were
        String pressedString = names[pressed];

        // Occurs if user hits extra buttons and therefore there is no correct button
        String correctString;
        if (correct == -1) correctString = "---";
        else correctString = names[correct];

        txt_pressed.setText(pressedString);
        txt_correct.setText(correctString);

        String mode = Game.GameMode.valueOf(main.tPreferences().getString(Keys.gameMode, Game.GameMode.Classic.name())).displayName();
        txt_mode.setText(mode);
    }

    public void animateIn() {
        rollIn.start();
    }

    private void animateOut() {
        this.animate().alpha(0.0f).setDuration(deathScreenOutDuration).withStartAction(fadeOutStart).withEndAction(fadeOutEnd);
    }

    public void performMainMenuClick() {
        mainMenuButton.performClick();
    }

    private void setMainMenuButton(final MainActivity main) {
        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                deathMenu.animateOut();

                // Fade in UI
                main.gameScreen().mainFadeInAnimation();

                // Play button sound
                AndroidSound.click.play(main);
            }
        });
        mainMenuButton.setClickable(false); // setOnClickListener resets clickable to true, so we need to set it to false here
    }

    private void setPlayAgainButton(final MainActivity main) {
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.gameScreen().playSymbol.setAlpha(0.0f);

                main.gameScreen().startGameAfterFadeOut = true;
                main.gameScreen().mainButton.setText("1"); // Starting level is always 1
                deathMenu.animateOut();

                // Play button sound
                AndroidSound.click.play(main);
            }
        });
        playAgainButton.setClickable(false); // setOnClickListener resets clickable to true, so we need to set it to false here
    }
}
