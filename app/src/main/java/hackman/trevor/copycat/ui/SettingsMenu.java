package hackman.trevor.copycat.ui;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import hackman.trevor.copycat.MainActivity;
import hackman.trevor.copycat.R;
import hackman.trevor.copycat.system.AndroidSound;
import hackman.trevor.copycat.system.Keys;
import hackman.trevor.tlibrary.library.TDimensions;

import static hackman.trevor.tlibrary.library.TLogging.report;

public class SettingsMenu extends LinearLayout {
    public SettingsMenu(Context context, AttributeSet attrs) { super(context, attrs); }

    public boolean isSettingsScreenUp; // Keeps track of whether settings menu is open/opening or not
    private boolean isSettingsScreenCompletelyUp; // Keeps track of whether menu is completely open (fade in animation over)

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

    private Runnable fadeOutOnEnd;
    private Runnable fadeInOnEnd;

    private ViewPropertyAnimator fadeOut;

    public void setUp(final MainActivity main) {
        this.main = main;
        this.setAlpha(0f);

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

        // The overcomplicated way of creating deep-copies of drawables
        leftArrowButton1.setBackground(leftArrowButton0.getBackground().getConstantState().newDrawable().mutate());
        rightArrowButton1.setBackground(rightArrowButton0.getBackground().getConstantState().newDrawable().mutate());

        setSettingsCloseButton();
        setArrowButtons();

        fadeInOnEnd = new Runnable() {
            @Override
            public void run() {
                isSettingsScreenCompletelyUp = true;
                settingsCloseButton.setEnabled(true);
            }
        };

        fadeOutOnEnd = new Runnable() {
            @Override
            public void run() {
                main.gameScreen().enableNonColorButtons(); // Enable buttons
                settingsCloseButton.setEnabled(true);

                // Manual move to back
                final ViewGroup parent = (ViewGroup) SettingsMenu.this.getParent();
                parent.removeView(SettingsMenu.this);
                parent.addView(SettingsMenu.this, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    SettingsMenu.this.setTranslationZ(-1); // Brings to back
                }
            }
        };
    }

    public void flex(int height, int width) {
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

        float minHeadSize       = TDimensions.dpToPixel(headTextSize);
        float minLeftSize       = TDimensions.dpToPixel(leftTextSize);
        float minRightSize      = TDimensions.dpToPixel(rightTextSize);
        float minButtonSize     = TDimensions.dpToPixel(buttonTextSize);
        float minArrowSize      = TDimensions.dpToPixel(arrowSize);

        float calculatedHeadSize    = TDimensions.mdToPixels(headTextSize * scale);
        float calculatedLeftSize    = TDimensions.mdToPixels(leftTextSize * scale);
        float calculatedRightSize   = TDimensions.mdToPixels(rightTextSize * scale);
        float calculatedButtonSize  = TDimensions.mdToPixels(buttonTextSize * scale);
        float calculatedArrowSize   = TDimensions.mdToPixels(arrowSize * scale);

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

        settingsCloseButton.getLayoutParams().height = (int)(finalButtonSize * 2);

    }

    public void open() {
        isSettingsScreenUp = true;
        main.gameScreen().disableNonColorButtons(); // Disable buttons

        // Bring to front
        this.bringToFront();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            getParent().requestLayout();
            ((View) getParent()).invalidate();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setTranslationZ(999); // Fix for bringToFront not completely working on newer APIs with relativeLayout
        }

        settingsCloseButton.setEnabled(false); // Prevent weirdness while opening (disables clicking) and visually shows the button as disabled
        animate().alpha(1.0f).setDuration(fadeInDuration).withEndAction(fadeInOnEnd);
    }

    public void close() {
        if (isSettingsScreenCompletelyUp) {
            isSettingsScreenUp = false;
            isSettingsScreenCompletelyUp = false;
            main.gameScreen().enableSettingsButton();
            settingsCloseButton.setEnabled(false);
            fadeOut = animate().alpha(0.0f).setDuration(fadeOutDuration).withEndAction(fadeOutOnEnd);
        }
    }

    public void getSettings() {
        setSpeed();
        setColors();
    }

    private void setSettingsCloseButton() {
        settingsCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();

                // Remove the banner ad if there is one
                main.gameScreen().removeBannerAd();

                // Play button sound
                AndroidSound.click.play(main);
            }
        });
    }

    private void setArrowButtons() {
        rightArrowButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSettingsScreenUp) {
                    int speedSetting = main.tPreferences().getInt(Keys.speed, NORMAL);
                    if (speedSetting < INSANE) {
                        main.tPreferences().putInt(Keys.speed, ++speedSetting);
                        setSpeed();

                        // Play button sound
                        AndroidSound.click.play(main);
                    }
                }
            }
        });

        leftArrowButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSettingsScreenUp) {
                    int speedSetting = main.tPreferences().getInt(Keys.speed, NORMAL);
                    if (speedSetting > NORMAL) {
                        main.tPreferences().putInt(Keys.speed, --speedSetting);
                        setSpeed();

                        // Play button sound
                        AndroidSound.click.play(main);
                    }
                }
            }
        });

        rightArrowButton1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSettingsScreenUp) {
                    int colorsSetting = main.tPreferences().getInt(Keys.colors, CLASSIC);
                    if (colorsSetting < GREYED) {
                        main.tPreferences().putInt(Keys.colors, ++colorsSetting);
                        setColors();

                        // Play button sound
                        AndroidSound.click.play(main);
                    }
                }
            }
        });

        leftArrowButton1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSettingsScreenUp) {
                    int colorsSetting = main.tPreferences().getInt(Keys.colors, CLASSIC);
                    if (colorsSetting > CLASSIC) {
                        main.tPreferences().putInt(Keys.colors, --colorsSetting);
                        setColors();

                        // Play button sound
                        AndroidSound.click.play(main);
                    }
                }
            }
        });
    }

    // To be called on creation and every time speed setting is changed
    // Also fades left&right arrow buttons appropriately
    private static final int NORMAL = 0;
    private static final int FAST = 1;
    private static final int EXTREME = 2;
    private static final int INSANE = 3;
    private void setSpeed() {
        int speed = main.tPreferences().getInt(Keys.speed, NORMAL /* default value */);
        switch (speed) {
            case NORMAL: // Default
                main.gameScreen().milliSecondsToLight = 500;
                main.gameScreen().milliSecondsDelay = 90;
                txt_speedText.setText(R.string.Normal);

                // Fade out left arrow since no more settings to the left
                leftArrowButton0.getBackground().setColorFilter(0x44ffffff, PorterDuff.Mode.MULTIPLY);
                break;
            case FAST:
                main.gameScreen().milliSecondsToLight = 300;
                main.gameScreen().milliSecondsDelay = 65;
                txt_speedText.setText(R.string.Fast);

                // Unfade left arrow
                leftArrowButton0.getBackground().clearColorFilter();
                break;
            case EXTREME:
                main.gameScreen().milliSecondsToLight = 150;
                main.gameScreen().milliSecondsDelay = 45;
                txt_speedText.setText(R.string.Extreme);

                // Unfade right arrow
                rightArrowButton0.getBackground().clearColorFilter();
                break;
            case INSANE:
                main.gameScreen().milliSecondsToLight = 75;
                main.gameScreen().milliSecondsDelay = 30;
                txt_speedText.setText(R.string.Insane);

                // Fade out right arrow since no more settings to the right
                rightArrowButton0.getBackground().setColorFilter(0x44ffffff, PorterDuff.Mode.MULTIPLY);
                break;
            default: // This should never happen
                report("Invalid speed setting: " + speed);

        }
    }

    // To be called on creation and every time speed setting is changed
    // Also fades left&right arrow buttons appropriately
    public static final int CLASSIC = 0;
    private static final int WARM = 1;
    private static final int BLUES = 2;
    private static final int PURPLES = 3;
    private static final int INVERTED = 4;
    private static final int GREYED = 5;
    private void setColors() {
        int colors = main.tPreferences().getInt(Keys.colors, CLASSIC /* default value */);
        switch (colors) {
            case CLASSIC:
                txt_colorsText.setText(R.string.Classic);
                main.gameScreen().colorButtons[0].setColor(ContextCompat.getColor(main, R.color.green));
                main.gameScreen().colorButtons[1].setColor(ContextCompat.getColor(main, R.color.red));
                main.gameScreen().colorButtons[2].setColor(ContextCompat.getColor(main, R.color.yellow));
                main.gameScreen().colorButtons[3].setColor(ContextCompat.getColor(main, R.color.blue));
                for (ColorButton cb : main.gameScreen().colorButtons) {
                    cb.flex();
                }

                // Fade out left arrow since no more settings to the left
                leftArrowButton1.getBackground().setColorFilter(0x44ffffff, PorterDuff.Mode.MULTIPLY);
                break;
            case WARM:
                txt_colorsText.setText(R.string.Warm);
                main.gameScreen().colorButtons[0].setColor(ContextCompat.getColor(main, R.color.warm0));
                main.gameScreen().colorButtons[1].setColor(ContextCompat.getColor(main, R.color.warm1));
                main.gameScreen().colorButtons[2].setColor(ContextCompat.getColor(main, R.color.warm2));
                main.gameScreen().colorButtons[3].setColor(ContextCompat.getColor(main, R.color.warm3));
                for (ColorButton cb : main.gameScreen().colorButtons) {
                    cb.flex();
                }

                // Unfade left arrow
                leftArrowButton1.getBackground().clearColorFilter();
                break;
            case BLUES:
                txt_colorsText.setText(R.string.Cool);
                main.gameScreen().colorButtons[0].setColor(ContextCompat.getColor(main, R.color.cool0));
                main.gameScreen().colorButtons[1].setColor(ContextCompat.getColor(main, R.color.cool1));
                main.gameScreen().colorButtons[2].setColor(ContextCompat.getColor(main, R.color.cool2));
                main.gameScreen().colorButtons[3].setColor(ContextCompat.getColor(main, R.color.cool3));
                for (ColorButton cb : main.gameScreen().colorButtons) {
                    cb.flex();
                }
                break;
            case PURPLES:
                txt_colorsText.setText(R.string.Royal);
                main.gameScreen().colorButtons[0].setColor(ContextCompat.getColor(main, R.color.royal0));
                main.gameScreen().colorButtons[1].setColor(ContextCompat.getColor(main, R.color.royal1));
                main.gameScreen().colorButtons[2].setColor(ContextCompat.getColor(main, R.color.royal2));
                main.gameScreen().colorButtons[3].setColor(ContextCompat.getColor(main, R.color.royal3));
                for (ColorButton cb : main.gameScreen().colorButtons) {
                    cb.flex();
                }
                break;
            case INVERTED:
                txt_colorsText.setText(R.string.Inverted);
                main.gameScreen().colorButtons[0].setColor(ContextCompat.getColor(main, R.color.inverted0));
                main.gameScreen().colorButtons[1].setColor(ContextCompat.getColor(main, R.color.inverted1));
                main.gameScreen().colorButtons[2].setColor(ContextCompat.getColor(main, R.color.inverted2));
                main.gameScreen().colorButtons[3].setColor(ContextCompat.getColor(main, R.color.inverted3));
                for (ColorButton cb : main.gameScreen().colorButtons) {
                    cb.flex();
                }

                // Unfade right arrow
                rightArrowButton1.getBackground().clearColorFilter();
                break;
            case GREYED:
                txt_colorsText.setText(R.string.Greyed);
                main.gameScreen().colorButtons[0].setColor(ContextCompat.getColor(main, R.color.greyed));
                main.gameScreen().colorButtons[1].setColor(ContextCompat.getColor(main, R.color.greyed));
                main.gameScreen().colorButtons[2].setColor(ContextCompat.getColor(main, R.color.greyed));
                main.gameScreen().colorButtons[3].setColor(ContextCompat.getColor(main, R.color.greyed));
                for (ColorButton cb : main.gameScreen().colorButtons) {
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
