package hackman.trevor.copycat;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import hackman.trevor.tlibrary.library.TDialog;
import hackman.trevor.tlibrary.library.TLogging;
import hackman.trevor.tlibrary.library.TPreferences;
import io.fabric.sdk.android.Fabric;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER;
import static hackman.trevor.copycat.AndroidSound.sounds;
import static hackman.trevor.copycat.MainActivity.noAdsStatus.NOT_OWNED;
import static hackman.trevor.copycat.MainActivity.noAdsStatus.OWNED;
import static hackman.trevor.copycat.MainActivity.noAdsStatus.REQUEST_FAILED;
import static hackman.trevor.tlibrary.library.TLogging.flog;
import static hackman.trevor.tlibrary.library.TLogging.log;
import static hackman.trevor.tlibrary.library.TLogging.report;
import static hackman.trevor.tlibrary.library.TMiscellaneous.moreGamesIntent;
import static hackman.trevor.tlibrary.library.TMiscellaneous.rateGameIntent;

public class MainActivity extends AppCompatActivity {
    // TODO Make this false for release, keep true for testing
    final static boolean TESTING = false; // Disables ads and crash reporting

    // Ad
    InterstitialAd interstitialAd;

    // Saved Data
    TPreferences myPreferences;

    // For Billing
    IInAppBillingService mService;
    final ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            flog("mServiceConn connected");

            noAdsStatus = isNoAdsPurchased(); // We have to wait for connection else nullReferenceError
            if (noAdsStatus == NOT_OWNED)
                requestNewInterstitial();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            flog("mServiceConn disconnected");
        }
    };

    DeathScreen deathScreen;
    LinearLayout buttonBar;
    LinearLayout settingsScreen;

    PlaySymbol playSymbolButton; // Inside the main button, displays play symbol
    Button mainButton; // The circle, click on it to play, displays level number
    Button settingsCloseButton;
    Button leftArrowButton;
    Button rightArrowButton;

    // Button bar
    Button settingsButton;
    Button starButton;
    Button noAdsButton;
    Button moreGamesButton;
    // Button[] buttonBarArray = {moreGamesButton, noAdsButton, starButton, settingsButton};

    ColorButton[] colorButtons = new ColorButton[4];
    ColorButton greenButton;
    ColorButton redButton;
    ColorButton yellowButton;
    ColorButton blueButton;

    Title title;
    ImageView top_fade;

    Instructions txt_instructions;
    TextView txt_score;
    TextView txt_highscore;
    TextView txt_pressed;
    TextView txt_correct;
    TextView txt_speedText;

    View.OnTouchListener mainButtonListener;

    // Enums
    final static int GREEN = 0; // Highest
    final static int RED = 1; // 2nd Highest
    final static int YELLOW = 2; // 3rd Highest
    final static int BLUE = 3; // 4th Highest
    // enum Color { Green, Red, Yellow, Blue }

    private ColorButton getButton(int which) {
        switch (which) {
            case GREEN:
                return greenButton;
            case RED:
                return redButton;
            case YELLOW:
                return yellowButton;
            case BLUE:
                return blueButton;
        }
        return null;
    }

    // Tracks whether or not the game is stopped
    static boolean stopped;

    // This is the handler along with my own method I've come up with to stop the
    // potential memory leaks w/o a static trail making a mess of the entire class
    static Runner runner;
    private class Runner {
        private void playColor(int key) {
            MainActivity.this.playColor(getButton(key));
        }
    }
    final static MyHandler handler = new MyHandler();
    private static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            final int what = message.what;
            switch (what) {
                case GREEN:
                    runner.playColor(GREEN);
                    break;
                case RED:
                    runner.playColor(RED);
                    break;
                case YELLOW:
                    runner.playColor(YELLOW);
                    break;
                case BLUE:
                    runner.playColor(BLUE);
                    break;
            }
        }
    }

    // Final objects
    final Context context = this;
    final ArrayList<Integer> sequence = new ArrayList<>(); // The sequence of colors to be played
    final Random random = new Random();

    // Animators
    ObjectAnimator fadeOutTopFade;
    ObjectAnimator fadeOutButtonBar;
    ObjectAnimator fadeOutPlaySymbol;
    ObjectAnimator fadeInTopFade;
    ObjectAnimator fadeInButtonBar;

    // Durations, in milliseconds
    static final int mainFadeDuration = 1000;
    static final int titlePopDuration = 1000;
    static final int instructionsInDuration = 500;
    static final int instructionsOutDuration = 1200;
    static final int instructionsOutDelay = 2000;
    static final int deathScreenInDuration = 700;
    static final int deathScreenOutDuration = 500;

    // Screen size, obtained in onCreate
    static DisplayMetrics displayMetrics = new DisplayMetrics();
    int pixelHeight;
    int pixelWidth;

    // Declare variables
    MainActivity.noAdsStatus noAdsStatus; // Tracks the response from isNoAdsPurchased()
    boolean allowColorInput; // Close input while sequence is playing, open when it's the player's turn to repeat
    boolean inGame; // Keeps track of whether in game or not
    boolean mainButtonEnabled; // Enables and disables main button, effectively tracks when game is allowed to start
    boolean isSettingsScreenUp; // Keeps track of whether settings screen is open or not
    boolean startGameAfterFadeOut; // If true, game will start after the deathscreen finishes fading away. Set on playAgainButton click
    boolean startNextSequence; // False until sequence is finished, true to continue to next level/sequence
    boolean popInRan; // Title pop-in plays in onResume once per creation
    long milliSecondsToLight; // The length of time a color is played (visual and sound) for during a sequence
    long milliSecondsDelay; // Delay between lights
    int level; // What level the user is on
    int sequenceTraveler; // Iterates as the sequence and player plays

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Crashlytics disabled be default, automatically enable it here if not testing
        if (!TESTING) {
            Fabric.with(this, new Crashlytics());
        }
        else {
            TLogging.disableCrashlytics(); // Necessary else crashlytics crashes from not being initialized with fabric
        }

        // Load layout
        setContentView(R.layout.activity_main);

        // Get screen dimensions of device
        getScreenDimensions();

        // Log start of app and screen size
        flog("Activity Start: Screen:["+ pixelWidth + "p by " + pixelHeight + "p]");

        // Initialize miscellaneous objects
        runner = new Runner();
        myPreferences = new TPreferences(context);

        // Initialize variables
        allowColorInput = true;
        inGame = false;
        mainButtonEnabled = true;
        level = 1;
        startNextSequence = false;
        isSettingsScreenUp = false;
        popInRan = false;

        // Billing
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        // Initialize Ads
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId("ca-app-pub-9667393179892638/3352851301");
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                requestNewInterstitial();
            }
        });

        // For sound
        setVolumeControlStream(AudioManager.STREAM_MUSIC); // Makes the volume wheel control music (all game sounds grouped in music) by default
        AndroidSound.newSoundPool();
        AndroidSound.loadSounds(context);

        // Initialize Death Screen
        deathScreen = findViewById(R.id.deathScreen);
        txt_score = findViewById(R.id.score);
        txt_highscore = findViewById(R.id.highScore);
        txt_pressed = findViewById(R.id.pressed);
        txt_correct = findViewById(R.id.correct);
        deathScreen.setUp(this);

        // Initialize Settings Screen
        settingsScreen = findViewById(R.id.settingsScreen);
        settingsCloseButton = findViewById(R.id.settingsCloseButton);
        txt_speedText = findViewById(R.id.speedText);
        leftArrowButton = findViewById(R.id.leftArrow);
        rightArrowButton = findViewById(R.id.rightArrow);

        // Initialize Title and Instructions
        title = findViewById(R.id.title_logo);
        top_fade = findViewById(R.id.top_fade);
        txt_instructions = findViewById(R.id.instructions);
        title.flexSize();

        // Initialize buttons
        buttonBar = findViewById(R.id.button_bar);
        settingsButton = findViewById(R.id.settings);
        starButton = findViewById(R.id.star);
        noAdsButton = findViewById(R.id.noAds);
        moreGamesButton = findViewById(R.id.moreGames);
        mainButton = findViewById(R.id.mainButton);
        playSymbolButton = findViewById(R.id.playSymbolButton);

        greenButton = findViewById(R.id.greenButton);
        redButton = findViewById(R.id.redButton);
        yellowButton = findViewById(R.id.yellowButton);
        blueButton = findViewById(R.id.blueButton);
        colorButtons[0] = greenButton;
        colorButtons[1] = redButton;
        colorButtons[2] = yellowButton;
        colorButtons[3] = blueButton;

        // Setup color buttons
        greenButton.setUp(ContextCompat.getColor(context, R.color.green), sounds[0], 0);
        redButton.setUp(ContextCompat.getColor(context, R.color.red), sounds[1], 1);
        yellowButton.setUp(ContextCompat.getColor(context, R.color.yellow), sounds[2], 2);
        blueButton.setUp(ContextCompat.getColor(context, R.color.blue), sounds[3], 3);
        setButton(greenButton);
        setButton(redButton);
        setButton(yellowButton);
        setButton(blueButton);

        // Setup buttons
        setMainButton();
        setSettingsButton();
        setSettingsCloseButton();
        setLeftArrowButton();
        setRightArrowButton();
        setStarButton();
        setNoAdsButton();
        setMoreGamesButton();

        // Initialize speed
        setSpeed();

        // Initialize animations
        fadeOutTopFade = ObjectAnimator.ofFloat(top_fade, "alpha", 0.0f);
        fadeOutTopFade.setDuration(mainFadeDuration);

        fadeInTopFade = ObjectAnimator.ofFloat(top_fade, "alpha", 1.0f);
        fadeInTopFade.setDuration(mainFadeDuration);

        fadeOutPlaySymbol = ObjectAnimator.ofFloat(playSymbolButton, "alpha", 0.0f);
        fadeOutPlaySymbol.setDuration(mainFadeDuration);

        fadeOutButtonBar = ObjectAnimator.ofFloat(buttonBar, "alpha", 0.0f);
        fadeOutButtonBar.setDuration(mainFadeDuration);
        fadeOutButtonBar.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                moreGamesButton.setClickable(false);
                noAdsButton.setClickable(false);
                starButton.setClickable(false);
                settingsButton.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animator) {}

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        fadeInButtonBar = ObjectAnimator.ofFloat(buttonBar, "alpha", 1.0f);
        fadeInButtonBar.setDuration(mainFadeDuration);
        fadeInButtonBar.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                moreGamesButton.setClickable(true);
                noAdsButton.setClickable(true);
                starButton.setClickable(true);
                settingsButton.setClickable(true);
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        // Animate play symbol
        playSymbolButton.myAnimation();

        // Possibly redundant as this is declared in manifest, but just incase
        setRequestedOrientation(SCREEN_ORIENTATION_USER);
    }

    void startGame() {
        inGame = true;
        mainButtonEnabled = false;
        txt_instructions.fadeIn();

        allowColorInput = false;
        String level_text = "" + level;
        mainButton.setText(level_text);

        // Multi-screen orientation changes ignore this setting, but I guess that's okay
        setRequestedOrientation(SCREEN_ORIENTATION_LOCKED);

        for (ColorButton c: colorButtons) {
            c.returnToNormal();
        }

        startSequence();
    }

    // Fade in buttons and title
    void mainFadeInAnimation() {
        fadeInButtonBar.start();
        fadeInTopFade.start();
        title.fadeIn();
    }

    // Fade out buttons and title
    private void mainFadeOutAnimation() {
        fadeOutButtonBar.start();
        fadeOutPlaySymbol.start();
        fadeOutTopFade.start();
        title.fadeOut();
    }

    // Get screen dimensions; used for resizing of elements if and as needed.
    private void getScreenDimensions() {
        displayMetrics = getResources().getDisplayMetrics();
    }

    private void playColor(final ColorButton button) {
        button.press();

        // Wait a bit then return button to normal
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // All UI changes have to run on UI thread or errors occur
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button.returnToNormal();
                    }
                });
                // Checks to see if there's more colors to play
                if (sequenceTraveler + 1 < sequence.size()) {
                    sequenceTraveler++;
                    // After delay play next color
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Play next color, have to use the handler because only the original thread that created a view can touch it according to android
                                handler.sendEmptyMessage(sequence.get(sequenceTraveler));
                            }
                            catch(ArrayIndexOutOfBoundsException e) { TLogging.report(e);} // Something has gone wrong, this should never happen
                        }
                    }, milliSecondsDelay);
                }
                // else sequence is finished
                else {
                    sequenceTraveler = 0; // Reset
                    allowColorInput = true;
                }
            }
        }, milliSecondsToLight);
    }

    private void startSequence() {
        startNextSequence = false;
        allowColorInput = false;
        String level_text = "" + level;
        mainButton.setText(level_text);
        sequenceTraveler = 0;
        sequence.add(random.nextInt(4)); // Generate next square in sequence

        // Note, the 1 new value added is kept and retained, growing the sequence naturally per the rules of the game
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    handler.sendEmptyMessage(sequence.get(0)); // if statement needed for case of starting game then backing to main menu
                }
                catch(NullPointerException e) { TLogging.report("");} // Something has gone wrong, this should never happen
            }
        }, (int) (milliSecondsDelay * 8 + milliSecondsToLight)); // Weird balancing I've found to like across speed settings
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setMainButton() {
        mainButtonListener = new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                view.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
                double x = event.getX();
                double y = event.getY();

                // x and y are with respect to the origin at the top left corner,
                // I want coordinates with respect to the center of the circle (the center of the button)
                x -= bitmap.getWidth() / 2;
                y -= bitmap.getHeight() / 2;

                // The circle is sized by dp but java works in px, so we obtain px here
                int radius = bitmap.getWidth() / 2; // bitmap is square - divide by 2 for radius not diameter

                // If coordinates are inside circle
                if (x*x + y*y <= radius * radius) {
                    // When button is clicked
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (mainButtonEnabled && !inGame) { // Don't start the game if it has already started
                            startGame();

                            // Fade out buttons and title
                            mainFadeOutAnimation();
                        }
                    }
                    return true;
                }
                // else
                // Returning false is critical & causes the event to be passed to the next view behind this button
                return false;
            }
        };

        mainButton.setOnTouchListener(mainButtonListener);
    }

    private void setSettingsButton() {
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // We have to disable the mainButton while settings are up as a precaution
                mainButton.setOnTouchListener(null);
                settingsScreen.bringToFront();
                settingsScreen.setTranslationZ(999); // Fix for bringToFront not completely working on newer APIs with relativeLayout
                isSettingsScreenUp = true;
            }
        });
    }

    private void setRightArrowButton() {
        rightArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int speedSetting = myPreferences.getInt("speed", 0);
                if (speedSetting < 3) myPreferences.putInt("speed", ++speedSetting);
                setSpeed();
            }
        });
    }

    private void setLeftArrowButton() {
        leftArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int speedSetting = myPreferences.getInt("speed", 0);
                if (speedSetting > 0) myPreferences.putInt("speed", --speedSetting);
                setSpeed();
            }
        });
    }

    private void setSettingsCloseButton() {
        settingsCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Manual move to back
                final ViewGroup parent = (ViewGroup)settingsScreen.getParent();
                parent.removeView(settingsScreen);
                parent.addView(settingsScreen, 0);
                settingsScreen.setTranslationZ(0); // Bring elevation back to zero
                mainButton.setOnTouchListener(mainButtonListener);
                isSettingsScreenUp = false;
            }
        });
    }

    private void setStarButton() {
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TDialog.createAlertDialog(context, R.string.rate_the_app_title, R.string.rate_the_app_message, R.string.Rate, R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(rateGameIntent(getPackageName()));
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Dialog closes by default
                    }
                });
            }
        });
    }

    private void setMoreGamesButton() {
        moreGamesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TDialog.createAlertDialog(context, R.string.more_games_title, R.string.more_games_message, R.string.View, R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(moreGamesIntent());
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Dialog closes by default
                    }
                });
            }
        });
    }

    // For the 4 colored buttons, sets up touch listeners
    private void setButton(final ColorButton button) {
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (allowColorInput) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        button.press();

                        if (inGame) {
                            if (sequenceTraveler >= sequence.size()) onFailure(button.getNumber(), -1); // This occurs if user incorrectly inputs extra colors, conveniently also stops array out of bounds
                            else if (sequence.get(sequenceTraveler) == button.getNumber()) { // Success
                                sequenceTraveler++;
                                if (sequenceTraveler == sequence.size()) { // Sequence completed
                                    level++;
                                    startNextSequence = true; // Waits for user to lift b4 starting next sequence

                                    // Check to see if new highscore has been achieved
                                    int scoreNum = level - 1;
                                    int highScoreNum = myPreferences.getInt("highscore", 0);
                                    if (highScoreNum < scoreNum) {
                                        highScoreNum = scoreNum;
                                        myPreferences.putInt("highscore", highScoreNum);
                                    }
                                }
                                else if (sequenceTraveler > sequence.size()) TLogging.report("WTF: sT > s.s should never happen");
                            }

                            else onFailure(button.getNumber(), sequence.get(sequenceTraveler)); // Failure
                        }
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        button.returnToNormal();

                        if (startNextSequence) {
                            // Check to see if any are still lit, don't start next sequence until none are
                            boolean anyLit = false;
                            for (ColorButton b: colorButtons)
                                if (b.getPressed()) {
                                    anyLit = true;
                                    break;
                                }
                            if (!anyLit) {
                                startSequence();
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

    private void onFailure(int pressed, int correct) {
        inGame = false;
        startNextSequence = false;
        sequence.clear();

        // In case user fails so fast that animations haven't ended yet
        fadeOutPlaySymbol.end();
        txt_instructions.endAnimations();

        // Death sound
        sounds[4].play(1);

        // Check for Ad
        if (noAdsStatus == NOT_OWNED) {
            if (interstitialAd.isLoaded()) {
                int rollForAd = random.nextInt(3); // A 1/3 chance
                if (rollForAd == 0) {
                    if (!TESTING) interstitialAd.show();
                    else log("Ad not displayed because TESTING");
                }
            }
            // If NOT_OWNED but Ad never began loading (connection issues?), try loading Ad
            else if (!interstitialAd.isLoading()) requestNewInterstitial();
        }
        // If request failed, request again until success, don't show Ad unless sure NOT_OWNED
        else if (noAdsStatus == REQUEST_FAILED) {
            noAdsStatus = isNoAdsPurchased();
        }
        // else OWNED, don't roll for ads or send more requests

        int scoreNum = level - 1;
        level = 1;
        String scoreNum_text = "" + scoreNum;
        txt_score.setText(scoreNum_text);

        int highScoreNum = myPreferences.getInt("highscore", 0);
        String highScoreNum_text = "" + highScoreNum;
        txt_highscore.setText(highScoreNum_text);

        // Indicate what the pressed and correct buttons were
        String pressedString = "---"; // Default, shouldn't occur
        switch (pressed) {
            case(0): pressedString = "Green"; break;
            case(1): pressedString = "Red"; break;
            case(2): pressedString = "Yellow"; break;
            case(3): pressedString = "Blue"; break;
        }
        String correctString = "---"; // Default, occurs if user hits extra button and therefore there is no correct button
        switch (correct) {
            case(0): correctString = "Green"; break;
            case(1): correctString = "Red"; break;
            case(2): correctString = "Yellow"; break;
            case(3): correctString = "Blue"; break;
        }
        txt_pressed.setText(pressedString);
        txt_correct.setText(correctString);

        deathScreen.fadeIn();
        setRequestedOrientation(SCREEN_ORIENTATION_USER);
    }

    // End game in progress, reset variables, and return to main menu
    private void exitGameToMainMenu() {
        inGame = false;
        mainButtonEnabled = true;
        startNextSequence = false;
        level = 1;
        mainButton.setText("");

        // Remove scheduled tasks
        handler.removeCallbacksAndMessages(null);
        sequence.clear();

        // End animations incase still ongoing and return buttons to normal incase they are in middle of press by sequence
        fadeOutPlaySymbol.end();
        txt_instructions.endAnimations();
        for (ColorButton button: colorButtons) {
            button.returnToNormal();
        }

        playSymbolButton.setAlpha(1.0f);
        mainFadeInAnimation();
        allowColorInput = true;
        setRequestedOrientation(SCREEN_ORIENTATION_USER);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getScreenDimensions();
        title.flexSize();
    }

    @Override
    public void onBackPressed() {
        if (deathScreen.isDeathScreenUp) {
            deathScreen.performMainMenuClick();
        }
        else if (isSettingsScreenUp) {
            settingsCloseButton.performClick();
        }
        else if (inGame) {
            if (level > 2) { // 'level > 2' not worth bothering to ask if only just started the game
                TDialog.createAlertDialog(context, R.string.exit_title, R.string.exit_message, R.string.Exit_App, R.string.Menu, R.string.Cancel,
                        // Positive listener
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MainActivity.super.onBackPressed();
                            }
                        },
                        // Neutral listener
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                exitGameToMainMenu();
                            }
                        });
            }
            else { exitGameToMainMenu(); }
        }
        else {
            super.onBackPressed(); // Exits app
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unload Billing
        if (mService != null) unbindService(mServiceConn);

        // Unload sounds if necessary
        if (AndroidSound.soundPool != null) {
            AndroidSound.release();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Essential so buttons don't stay stuck if app is minimized mid-button-press
        // Done in onStart and not onResume in order to support window switches in multi-screen which call onResume but not onStart
        redButton.returnToNormal();
        yellowButton.returnToNormal();
        greenButton.returnToNormal();
        blueButton.returnToNormal();

        // Reload sounds if necessary
        if (AndroidSound.soundPool == null) {
            AndroidSound.newSoundPool();
            AndroidSound.loadSounds(context);
        }
    }

    @Override // Goes fullscreen
    public void onResume() {
        super.onResume();
        stopped = false;

        if (!popInRan) {
            title.popIn();
            popInRan = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopped = true;

        // Don't hog resources from other apps when not infront; don't leak memory and stop sound errors
        // We do this in onStop instead of onPause in order to support multi-screen, onPause is called but onStop isn't on multi-screen window change
        AndroidSound.release();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }

    // To be called on creation and every time speed setting is changed
    // Also fades left&right arrow buttons appropiately
    private void setSpeed() {
        int speed = myPreferences.getInt("speed", 0);
        if (speed == 0) { // Default normal speed
            milliSecondsToLight = 500;
            milliSecondsDelay = 90;
            txt_speedText.setText(R.string.Normal);

            // Fade out left arrow since no more settings to the left
            Drawable leftArrowDrawable = leftArrowButton.getBackground();
            leftArrowDrawable.setColorFilter(0x44ffffff, PorterDuff.Mode.MULTIPLY);
        }
        else if (speed == 1) { // Fast
            milliSecondsToLight = 300;
            milliSecondsDelay = 65;
            txt_speedText.setText(R.string.Fast);

            // Unfade left arrow
            Drawable leftArrowDrawable = leftArrowButton.getBackground();
            leftArrowDrawable.clearColorFilter();
        }
        else if (speed == 2) { // Extreme
            milliSecondsToLight = 150;
            milliSecondsDelay = 45;
            txt_speedText.setText(R.string.Extreme);

            // Unfade right arrow
            Drawable rightArrowDrawable = rightArrowButton.getBackground();
            rightArrowDrawable.clearColorFilter();
        }
        else if (speed == 3) { // Insane
            milliSecondsToLight = 75;
            milliSecondsDelay = 30;
            txt_speedText.setText(R.string.Insane);

            // Fade out right arrow since no more settings to the right
            Drawable rightArrowDrawable = rightArrowButton.getBackground();
            rightArrowDrawable.setColorFilter(0x44ffffff, PorterDuff.Mode.MULTIPLY);
        }
        else { // else should never happen
            report("Invalid speed setting: " + speed);
        }
    }

    // 1001 is arbitrary number, I could use any number for the code. Done to identify that responses with this code are for the purpose of billing
    public static final int BILLING_REQUEST_CODE = 1001;
    private void setNoAdsButton() {
        noAdsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TDialog.createAlertDialog(context, R.string.no_ads_title, R.string.no_ads_message, R.string.Purchase, R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            flog("Purchase flow started");
                            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "no_ads", "inapp", "Verified by me");
                            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                            if (pendingIntent != null) {
                                IntentSender intentSender = pendingIntent.getIntentSender();
                                if (intentSender != null)

                                    startIntentSenderForResult(intentSender, BILLING_REQUEST_CODE, new Intent(), 0, 0, 0);
                                else { flog("intentSender is null"); report(); } // This happens when item is already purchased? // TODO Test and Add dialog
                            } else {
                                flog("noAdsButton clicked: pendingIntent is null; suspect no Google Account logged into Android device");
                                AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
                                builder.setTitle(R.string.null_intent_sender_error_title).setMessage(R.string.null_intent_sender_error_message)
                                        .setNeutralButton(R.string.OK, null)
                                        .create()
                                        .show();
                            }
                        }
                        catch (RemoteException | IntentSender.SendIntentException e) { TLogging.report(e); }
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Dialog closes by default
                    }
                });
            }
        });
    }

    // This handles results from other activities, in particular, I'm using it to handle the results of requests to Google for purchases of my inapp products
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BILLING_REQUEST_CODE) {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            // Purchase successful
            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String productId = jo.getString("productId");
                    flog("You have bought the " + productId + " item!");
                }
                catch (JSONException e) {
                    flog("Failed to parse purchase data: " + purchaseData);
                    report(e);
                }
            }
            // Purchase Cancelled/Failed
            else { flog("Purchase RESULT_CANCELLED: " + resultCode); }
        }
        else {
            flog("Invalid requestCode: " + requestCode);
            report("Never supposed to get an invalid requestCode");
        }
    }

    // Query Google to see if no_ads has been purchased
    enum noAdsStatus {OWNED, NOT_OWNED, REQUEST_FAILED}
    private MainActivity.noAdsStatus isNoAdsPurchased() {
        // incase mService which only gets initialized on connection hasn't been initialized yet
        if (mService != null) {
            try {
                Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                int response = ownedItems.getInt("RESPONSE_CODE");
                // Request is successful
                if (response == 0) {
                    // Get pauseList of owned items - Warning, this only works for up to 700 owned items (Not a problem for me)
                    ArrayList<String> ownedProducts = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");

                    // ownedProducts should never be null, but in case it is
                    if (ownedProducts != null) {
                        for (int i = 0; i < ownedProducts.size(); ++i) {
                            String product = ownedProducts.get(i);
                            flog("Owned product: " + product);

                            if (product.equals("no_ads")) return OWNED;
                        }
                    }
                    else {
                        flog("Query for no_ads purchase failed: ownedProducts is null");
                        return REQUEST_FAILED;
                    }
                }
                // Request failed - maybe no internet connection
                else {
                    flog("Query for no_ads purchase failed: response is not 0");
                    return REQUEST_FAILED;
                }
            } catch (RemoteException e) {
                flog("Query for no_ads purchase failed: RemoteException");
                flog(mService == null ? "mService is null" : "mService is not null");
                report(e);
                return REQUEST_FAILED;
            }
            flog("no_ads product NOT OWNED");
            return NOT_OWNED;
        }
        flog("Query for no_ads purchase failed: mService is null");
        return REQUEST_FAILED;
    }
}
