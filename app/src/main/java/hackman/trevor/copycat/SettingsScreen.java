package hackman.trevor.copycat;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import hackman.trevor.tlibrary.library.TMath;

import static hackman.trevor.tlibrary.library.TLogging.log;
import static hackman.trevor.tlibrary.library.TLogging.report;

public class SettingsScreen extends LinearLayout {
    public SettingsScreen(Context context, AttributeSet attrs) { super(context, attrs); }

    private boolean isSettingsScreenUp; // Keeps track of whether settings screen is open/opening or not
    private boolean isSettingsScreenCompletelyUp; // Keeps track of whether screen is completely open (fade in animation over)
    private boolean listenToEnd = true;

    private final int fadeInDuration = 500;
    private final int fadeOutDuration = 300;

    private MainActivity main;
    private Button leftArrowButton0;
    private Button rightArrowButton0;
    private Button leftArrowButton1;
    private Button rightArrowButton1;
    private Button settingsCloseButton;
    private TextView txt_settings;
    private TextView txt_speedText;
    private TextView txt_colorsText;
    private TextView txt_setting0;
    private TextView txt_setting1;

    private ObjectAnimator fadeIn;
    private ObjectAnimator fadeOut;
    private Animator.AnimatorListener fadeOutListener;
    private Animator.AnimatorListener fadeInListener;

    void setUp(final MainActivity main) {
        this.main = main;
        this.setAlpha(0f);

        isSettingsScreenUp = false;
        isSettingsScreenCompletelyUp = false;

        txt_settings = findViewById(R.id.settingsHead);
        settingsCloseButton = findViewById(R.id.settingsCloseButton);
        leftArrowButton0 = findViewById(R.id.leftArrow0);
        rightArrowButton0 = findViewById(R.id.rightArrow0);
        leftArrowButton1 = findViewById(R.id.leftArrow1);
        rightArrowButton1 = findViewById(R.id.rightArrow1);
        txt_speedText = findViewById(R.id.speedText);
        txt_colorsText = findViewById(R.id.colorsText);
        txt_setting0 = findViewById(R.id.setting0);
        txt_setting1 = findViewById(R.id.setting1);

        // The overcomplicated way of creating deepcopies of drawables
        leftArrowButton1.setBackground(leftArrowButton0.getBackground().getConstantState().newDrawable().mutate());
        rightArrowButton1.setBackground(rightArrowButton0.getBackground().getConstantState().newDrawable().mutate());

        setSettingsCloseButton();
        setArrowButtons();

        fadeInListener = new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animator) { }
            @Override public void onAnimationRepeat(Animator animator) { }
            @Override public void onAnimationCancel(Animator animator) { }
            @Override public void onAnimationEnd(Animator animator) {
                isSettingsScreenCompletelyUp = true;
            }
        };

        fadeOutListener = new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) { }
            @Override public void onAnimationRepeat(Animator animation) { }
            @Override public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listenToEnd) {
                    // Manual move to back
                    final ViewGroup parent = (ViewGroup) SettingsScreen.this.getParent();
                    parent.removeView(SettingsScreen.this);
                    parent.addView(SettingsScreen.this, 0);
                    SettingsScreen.this.setTranslationZ(0); // Bring elevation back to zero
                    main.mainButtonEnabled = true;
                }
            }
        };
        fadeOut = ObjectAnimator.ofFloat(this, "alpha", 0.0f);
        fadeOut.setDuration(fadeOutDuration);
        fadeOut.addListener(fadeOutListener);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    boolean isSettingsScreenUp() {
        return isSettingsScreenUp;
    }

    void flex(int height, int width) {
        ViewGroup.LayoutParams params = this.getLayoutParams();

        // Portrait
        if (width < height) params.width = LayoutParams.MATCH_PARENT;
        else { // Landscape
            params.width = (height + width)/2;
        }

        final float scale       = 1.2f;
        float headTextSize      = 30f;
        float leftTextSize      = 20.25f;
        float rightTextSize     = 16.2f;
        float buttonTextSize    = 16.2f;
        float arrowSize         = 35f;

        float minHeadSize       = TMath.convertDpToPixel(headTextSize, main);
        float minLeftSize       = TMath.convertDpToPixel(leftTextSize, main);
        float minRightSize      = TMath.convertDpToPixel(rightTextSize, main);
        float minButtonSize     = TMath.convertDpToPixel(buttonTextSize, main);
        float minArrowSize      = TMath.convertDpToPixel(arrowSize, main);

        float calculatedHeadSize    = TMath.convertMdToPixel(headTextSize * scale, main);
        float calculatedLeftSize    = TMath.convertMdToPixel(leftTextSize * scale, main);
        float calculatedRightSize   = TMath.convertMdToPixel(rightTextSize * scale, main);
        float calculatedButtonSize  = TMath.convertMdToPixel(buttonTextSize * scale, main);
        float calculatedArrowSize   = TMath.convertMdToPixel(arrowSize * scale, main);

        float finalHeadSize     = Math.max(minHeadSize, calculatedHeadSize);
        float finalLeftSize     = Math.max(minLeftSize, calculatedLeftSize);
        float finalRightSize    = Math.max(minRightSize, calculatedRightSize);
        float finalButtonSize   = Math.max(minButtonSize, calculatedButtonSize);
        float finalArrowSize    = Math.max(minArrowSize, calculatedArrowSize);

        txt_settings.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalHeadSize);
        txt_setting0.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalLeftSize);
        txt_setting1.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalLeftSize);
        txt_speedText.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalRightSize);
        txt_colorsText.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalRightSize);
        settingsCloseButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, finalButtonSize);

        ViewGroup.LayoutParams s0Params = txt_speedText.getLayoutParams();
        ViewGroup.LayoutParams s1Params = txt_colorsText.getLayoutParams();
        s0Params.width = (int)(finalLeftSize * 4);
        s1Params.width = (int)(finalLeftSize * 4);

        ViewGroup.LayoutParams arrowParams0 = rightArrowButton0.getLayoutParams();
        ViewGroup.LayoutParams arrowParams1 = rightArrowButton1.getLayoutParams();
        ViewGroup.LayoutParams arrowParams2 = leftArrowButton0.getLayoutParams();
        ViewGroup.LayoutParams arrowParams3 = leftArrowButton1.getLayoutParams();
        ViewGroup.LayoutParams[] apArray = new ViewGroup.LayoutParams[4];
        apArray[0] = arrowParams0;
        apArray[1] = arrowParams1;
        apArray[2] = arrowParams2;
        apArray[3] = arrowParams3;

        for (ViewGroup.LayoutParams lp : apArray) {
            lp.height = (int)finalArrowSize;
            lp.width = (int)finalArrowSize;
        }

        float multiplier = 2;
        ViewGroup.LayoutParams scbParams = settingsCloseButton.getLayoutParams();
        scbParams.height = (int)(finalButtonSize * multiplier);
    }

    void display() {
        isSettingsScreenUp = true;

        if (fadeOut != null && fadeOut.isRunning()) {
            listenToEnd = false;
            fadeOut.cancel();
        }

        main.mainButtonEnabled = false; // Disable mainbutton while settings are up
        this.bringToFront();
        this.setTranslationZ(999); // Fix for bringToFront not completely working on newer APIs with relativeLayout

        // Causes fade in from current alpha value which may have been modified since creation
        fadeIn = ObjectAnimator.ofFloat(this, "alpha", 1.0f);
        fadeIn.setDuration(fadeInDuration);
        fadeIn.addListener(fadeInListener);

        fadeIn.start();

        listenToEnd = true;
    }

    void close() {
        if (isSettingsScreenCompletelyUp) {
            isSettingsScreenUp = false;
            isSettingsScreenCompletelyUp = false;

            fadeOut.start();
        }
    }

    void getSettings() {
        setSpeed();
        setColors();
    }

    private void setSettingsCloseButton() {
        settingsCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });
    }

    private void setArrowButtons() {
        rightArrowButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSettingsScreenUp) {
                    int speedSetting = main.myPreferences.getInt("speed", NORMAL);
                    if (speedSetting < INSANE) {
                        main.myPreferences.putInt("speed", ++speedSetting);
                        setSpeed();
                    }
                }
            }
        });

        leftArrowButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSettingsScreenUp) {
                    int speedSetting = main.myPreferences.getInt("speed", NORMAL);
                    if (speedSetting > NORMAL) {
                        main.myPreferences.putInt("speed", --speedSetting);
                        setSpeed();
                    }
                }
            }
        });

        rightArrowButton1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSettingsScreenUp) {
                    int colorsSetting = main.myPreferences.getInt("colors", CLASSIC);
                    if (colorsSetting < GREYED) {
                        main.myPreferences.putInt("colors", ++colorsSetting);
                        setColors();
                    }
                }
            }
        });

        leftArrowButton1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSettingsScreenUp) {
                    int colorsSetting = main.myPreferences.getInt("colors", CLASSIC);
                    if (colorsSetting > CLASSIC) {
                        main.myPreferences.putInt("colors", --colorsSetting);
                        setColors();
                    }
                }
            }
        });
    }

    // To be called on creation and every time speed setting is changed
    // Also fades left&right arrow buttons appropiately
    private static final int NORMAL = 0;
    private static final int FAST = 1;
    private static final int EXTREME = 2;
    private static final int INSANE = 3;
    private void setSpeed() {
        int speed = main.myPreferences.getInt("speed", NORMAL /* default value */);
        switch (speed) {
            case NORMAL: // Default
                main.milliSecondsToLight = 500;
                main.milliSecondsDelay = 90;
                txt_speedText.setText(R.string.Normal);

                // Fade out left arrow since no more settings to the left
                leftArrowButton0.getBackground().setColorFilter(0x44ffffff, PorterDuff.Mode.MULTIPLY);
                break;
            case FAST:
                main.milliSecondsToLight = 300;
                main.milliSecondsDelay = 65;
                txt_speedText.setText(R.string.Fast);

                // Unfade left arrow
                leftArrowButton0.getBackground().clearColorFilter();
                break;
            case EXTREME:
                main.milliSecondsToLight = 150;
                main.milliSecondsDelay = 45;
                txt_speedText.setText(R.string.Extreme);

                // Unfade right arrow
                rightArrowButton0.getBackground().clearColorFilter();
                break;
            case INSANE:
                main.milliSecondsToLight = 75;
                main.milliSecondsDelay = 30;
                txt_speedText.setText(R.string.Insane);

                // Fade out right arrow since no more settings to the right
                rightArrowButton0.getBackground().setColorFilter(0x44ffffff, PorterDuff.Mode.MULTIPLY);
                break;
            default: // This should never happen
                report("Invalid speed setting: " + speed);

        }
    }

    // To be called on creation and every time speed setting is changed
    // Also fades left&right arrow buttons appropiately
    static final int CLASSIC = 0;
    private static final int WARM = 1;
    private static final int BLUES = 2;
    private static final int PURPLES = 3;
    private static final int INVERTED = 4;
    private static final int GREYED = 5;
    private void setColors() {
        int colors = main.myPreferences.getInt("colors", CLASSIC /* default value */);
        switch (colors) {
            case CLASSIC:
                txt_colorsText.setText(R.string.Classic);
                main.colorButtons[0].setColor(ContextCompat.getColor(main, R.color.green));
                main.colorButtons[1].setColor(ContextCompat.getColor(main, R.color.red));
                main.colorButtons[2].setColor(ContextCompat.getColor(main, R.color.yellow));
                main.colorButtons[3].setColor(ContextCompat.getColor(main, R.color.blue));
                for (ColorButton cb : main.colorButtons) {
                    cb.flex();
                }

                // Fade out left arrow since no more settings to the left
                leftArrowButton1.getBackground().setColorFilter(0x44ffffff, PorterDuff.Mode.MULTIPLY);
                break;
            case WARM:
                txt_colorsText.setText(R.string.Warm);
                main.colorButtons[0].setColor(ContextCompat.getColor(main, R.color.warm0));
                main.colorButtons[1].setColor(ContextCompat.getColor(main, R.color.warm1));
                main.colorButtons[2].setColor(ContextCompat.getColor(main, R.color.warm2));
                main.colorButtons[3].setColor(ContextCompat.getColor(main, R.color.warm3));
                for (ColorButton cb : main.colorButtons) {
                    cb.flex();
                }

                // Unfade left arrow
                leftArrowButton1.getBackground().clearColorFilter();
                break;
            case BLUES:
                txt_colorsText.setText(R.string.Cool);
                main.colorButtons[0].setColor(ContextCompat.getColor(main, R.color.cool0));
                main.colorButtons[1].setColor(ContextCompat.getColor(main, R.color.cool1));
                main.colorButtons[2].setColor(ContextCompat.getColor(main, R.color.cool2));
                main.colorButtons[3].setColor(ContextCompat.getColor(main, R.color.cool3));
                for (ColorButton cb : main.colorButtons) {
                    cb.flex();
                }
                break;
            case PURPLES:
                txt_colorsText.setText(R.string.Royal);
                main.colorButtons[0].setColor(ContextCompat.getColor(main, R.color.royal0));
                main.colorButtons[1].setColor(ContextCompat.getColor(main, R.color.royal1));
                main.colorButtons[2].setColor(ContextCompat.getColor(main, R.color.royal2));
                main.colorButtons[3].setColor(ContextCompat.getColor(main, R.color.royal3));
                for (ColorButton cb : main.colorButtons) {
                    cb.flex();
                }
                break;
            case INVERTED:
                txt_colorsText.setText(R.string.Inverted);
                main.colorButtons[0].setColor(ContextCompat.getColor(main, R.color.inverted0));
                main.colorButtons[1].setColor(ContextCompat.getColor(main, R.color.inverted1));
                main.colorButtons[2].setColor(ContextCompat.getColor(main, R.color.inverted2));
                main.colorButtons[3].setColor(ContextCompat.getColor(main, R.color.inverted3));
                for (ColorButton cb : main.colorButtons) {
                    cb.flex();
                }

                // Unfade right arrow
                rightArrowButton1.getBackground().clearColorFilter();
                break;
            case GREYED:
                txt_colorsText.setText(R.string.Greyed);
                main.colorButtons[0].setColor(ContextCompat.getColor(main, R.color.greyed));
                main.colorButtons[1].setColor(ContextCompat.getColor(main, R.color.greyed));
                main.colorButtons[2].setColor(ContextCompat.getColor(main, R.color.greyed));
                main.colorButtons[3].setColor(ContextCompat.getColor(main, R.color.greyed));
                for (ColorButton cb : main.colorButtons) {
                    cb.flex();
                }

                // Fade out right arrow since no more settings to the right
                rightArrowButton1.getBackground().setColorFilter(0x44ffffff, PorterDuff.Mode.MULTIPLY);
                break;
            default: // This should never happen
                report("Invalid colors setting: " + colors);
        }
    }
}
