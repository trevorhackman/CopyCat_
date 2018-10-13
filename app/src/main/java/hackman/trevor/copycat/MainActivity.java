package hackman.trevor.copycat;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.vending.billing.IInAppBillingService;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import hackman.trevor.tlibrary.library.TLogging;
import hackman.trevor.tlibrary.library.TMath;
import hackman.trevor.tlibrary.library.TPreferences;
import io.fabric.sdk.android.Fabric;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER;
import static hackman.trevor.copycat.AndroidSound.sounds;
import static hackman.trevor.copycat.MainActivity.noAdsStatus.INITIAL;
import static hackman.trevor.copycat.MainActivity.noAdsStatus.NOT_OWNED;
import static hackman.trevor.copycat.MainActivity.noAdsStatus.OWNED;
import static hackman.trevor.copycat.MainActivity.noAdsStatus.REQUEST_FAILED;
import static hackman.trevor.tlibrary.library.TLogging.flog;
import static hackman.trevor.tlibrary.library.TLogging.log;
import static hackman.trevor.tlibrary.library.TLogging.report;

public class MainActivity extends AppCompatActivity {
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

            noAdsStatusResult = isNoAdsPurchased(); // We have to wait for connection else nullReferenceError
            if (noAdsStatusResult == NOT_OWNED)
                requestNewInterstitial();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            flog("mServiceConn disconnected");
        }
    };
    private void bindBillingService() {
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    DeathScreen deathScreen;
    SettingsScreen settingsScreen;
    LinearLayout buttonBar;

    PlaySymbol playSymbolButton; // Inside the main button, displays play symbol
    MainButton mainButton; // The circle, click on it to play, displays level number

    // Button bar
    Button moreGamesButton;
    Button noAdsButton;
    Button starButton;
    Button settingsButton;
    Button[] buttonBarArray;

    ColorButton[] colorButtons = new ColorButton[4];
    ColorButton greenButton;
    ColorButton redButton;
    ColorButton yellowButton;
    ColorButton blueButton;

    Title title;
    ImageView top_fade;

    Instructions txt_instructions;

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
        throw new IllegalArgumentException("No such color: 135");
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

    // Animator Listeners
    Animator.AnimatorListener fadeInButtonBarListener;
    Animator.AnimatorListener fadeOutButtonBarListener;
    static final int mainFadeDuration = 1000; // main animation duration in milliseconds

    // Screen size, obtained in onCreate
    static DisplayMetrics displayMetrics = new DisplayMetrics();

    // Declare variables
    MainActivity.noAdsStatus noAdsStatusResult = INITIAL; // Tracks the response from isNoAdsPurchased()
    boolean allowColorInput; // Close input while sequence is playing, open when it's the player's turn to repeat
    boolean inGame; // Keeps track of whether in game or not
    boolean mainButtonEnabled; // Enables and disables main button, effectively tracks when game is allowed to start
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
        if (!TLogging.TESTING) Fabric.with(this, new Crashlytics());
        else TLogging.disableCrashlytics(); // Necessary else crashlytics crashes from not being initialized with fabric

        // Load layout
        setContentView(R.layout.activity_main);

        // Get screen dimensions of device
        getScreenDimensions();

        // Log start of app and screen size
        flog("Activity Create: Screen:["+ displayMetrics.heightPixels + "p by " + displayMetrics.widthPixels + "p]");

        // Initialize miscellaneous objects
        runner = new Runner();
        myPreferences = new TPreferences(context);

        // Initialize variables
        allowColorInput = true;
        inGame = false;
        mainButtonEnabled = true;
        level = 1;
        startNextSequence = false;
        popInRan = false;

        // Billing
        bindBillingService();

        // Unsure of purpose of this line, things seem to work fine w/o it. AdMob guide recommends it but doesn't explain what it does in the slightest
        MobileAds.initialize(this, "ca-app-pub-9667393179892638~7004321704");
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
        deathScreen.setUp(this);

        // Initialize Settings Screen
        settingsScreen = findViewById(R.id.settingsScreen);
        settingsScreen.setUp(this);

        // Initialize Title and Instructions
        title = findViewById(R.id.title_logo);
        top_fade = findViewById(R.id.top_fade);
        txt_instructions = findViewById(R.id.instructions);

        // Initialize buttons
        mainButton = findViewById(R.id.mainButton);
        playSymbolButton = findViewById(R.id.playSymbolButton);
        buttonBar = findViewById(R.id.button_bar);
        settingsButton = findViewById(R.id.settings);
        starButton = findViewById(R.id.star);
        noAdsButton = findViewById(R.id.noAds);
        moreGamesButton = findViewById(R.id.moreGames);
        buttonBarArray = new Button[4];
        buttonBarArray[0] = moreGamesButton;
        buttonBarArray[1] = noAdsButton;
        buttonBarArray[2] = starButton;
        buttonBarArray[3] = settingsButton;

        greenButton = findViewById(R.id.greenButton);
        redButton = findViewById(R.id.redButton);
        yellowButton = findViewById(R.id.yellowButton);
        blueButton = findViewById(R.id.blueButton);
        colorButtons[0] = greenButton;
        colorButtons[1] = redButton;
        colorButtons[2] = yellowButton;
        colorButtons[3] = blueButton;

        // Setup color buttons
        greenButton.setUp(sounds[AndroidSound.chip1], 0);
        redButton.setUp(sounds[AndroidSound.chip2], 1);
        yellowButton.setUp(sounds[AndroidSound.chip3], 2);
        blueButton.setUp(sounds[AndroidSound.chip4], 3);
        setButton(greenButton);
        setButton(redButton);
        setButton(yellowButton);
        setButton(blueButton);

        // Setup buttons
        setMainButton();
        setSettingsButton();
        setStarButton();
        setNoAdsButton();
        setMoreGamesButton();

        // Initialize settings
        settingsScreen.getSettings();

        // Initialize animation listeners
        fadeInButtonBarListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                moreGamesButton.setClickable(true);
                noAdsButton.setClickable(true);
                starButton.setClickable(true);
                settingsButton.setClickable(true);
            }

            @Override public void onAnimationEnd(Animator animator) { }
            @Override public void onAnimationCancel(Animator animator) { }
            @Override public void onAnimationRepeat(Animator animator) { }
        };
        fadeOutButtonBarListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                moreGamesButton.setClickable(false);
                noAdsButton.setClickable(false);
                starButton.setClickable(false);
                settingsButton.setClickable(false);
            }

            @Override public void onAnimationEnd(Animator animator) {}
            @Override public void onAnimationCancel(Animator animator) {}
            @Override public void onAnimationRepeat(Animator animator) {}
        };

        // Animate play symbol
        playSymbolButton.gyrate();

        // Possibly redundant as this is declared in manifest, but just incase
        setRequestedOrientation(SCREEN_ORIENTATION_USER);

        flexAll();

        // Try putting in onWindowFocusChanged
        // Loading issue: Turns out sounds don't load instantly, and potentially variably, so difficulties ensure with playing sound right on startup
        // Play start sound
        // playStartSound();
    }

    void startGame() {
        flog("Game started");

        inGame = true;
        mainButtonEnabled = false;
        txt_instructions.fadeIn();

        allowColorInput = false;
        String level_text = "" + level;
        mainButton.setText(level_text);

        // Keep screen from rotating during game to prevent confusion
        // Multi-screen orientation changes ignore this setting, but I guess that's okay
        // Doesn't work VERSION < 18, but I guess that's okay too
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }

        for (ColorButton c: colorButtons) {
            c.returnToNormal();
        }

        startSequence();
    }

    // Fade in buttons and title
    void mainFadeInAnimation() {
        buttonBar.animate().alpha(1.0f).setDuration(mainFadeDuration).setListener(fadeInButtonBarListener);
        top_fade.animate().alpha(1.0f).setDuration(mainFadeDuration);
        title.fadeIn();
    }

    // Fade out buttons and title
    private void mainFadeOutAnimation() {
        buttonBar.animate().alpha(0.0f).setDuration(mainFadeDuration).setListener(fadeOutButtonBarListener);
        top_fade.animate().alpha(0.0f).setDuration(mainFadeDuration);
        playSymbolButton.fadeOut();
        title.fadeOut();
    }

    // Get screen dimensions; used for resizing of elements if and as needed.
    private void getScreenDimensions() {
        displayMetrics = getResources().getDisplayMetrics();
    }

    // Flexible UI resizes according to screen dimensions
    private void flexAll() {
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        title.flexSize(height, width);
        mainButton.flexSize(height, width);
        playSymbolButton.flexSize(height, width);
        flexButtons(height, width);
        txt_instructions.flex();
        deathScreen.flex(height, width);
        settingsScreen.flex(height, width);
    }

    private void flexButtons(int height, int width) {
        int minDimension = Math.min(height, width); // For consistent size in both portrait & landscape
        int numButtons = buttonBarArray.length;
        for (Button button: buttonBarArray) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,0); // 0,0 just placeholder

            // Determine margin, minimum margin is 6dp
            int margin = (int)(.015 * minDimension);
            margin = Math.max((int)TMath.convertDpToPixel(4, this), margin);

            // Determine button size, minimum* size is 60dp, but if screen is super small, let the buttons go smaller
            int maxSize = (minDimension - margin*numButtons*2)/numButtons; // Max size that will let the buttons still fit on screen
            int minSize = (int)TMath.convertDpToPixel(60f, this);

            int size = (int)(maxSize * 0.9);

            if (maxSize < minSize) size = maxSize; // For super small devices
            else if (size < minSize) size = minSize; // For small devices

            params.height = size;
            params.width = size;

            int bottomMargin;
            if (height < width) bottomMargin = margin;
            else bottomMargin = 2 * margin;
            params.setMargins(margin, margin, margin, bottomMargin);

            button.setLayoutParams(params);
        }
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

    private void setMainButton() {
        View.OnTouchListener mainButtonListener = new View.OnTouchListener() {
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
                if (!settingsScreen.isSettingsScreenUp()) {
                    // Display settings screen
                    settingsScreen.display();

                    // Play button sound
                    AndroidSound.sounds[AndroidSound.click].play(AndroidSound.VOLUME_CLICK);
                }
            }
        });
    }

    private void setStarButton() {
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open dialog asking to rate the app
                Dialogs.rateTheApp(context, MainActivity.this);

                // Play button sound
                AndroidSound.sounds[AndroidSound.click].play(AndroidSound.VOLUME_CLICK);
            }
        });
    }

    private void setMoreGamesButton() {
        moreGamesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialogs.viewMoreGames(context);

                // Play button sound
                AndroidSound.sounds[AndroidSound.click].play(AndroidSound.VOLUME_CLICK);
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
                        button.performClick(); // To satisfy inspector
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
        flog("onFailure called, score: " + (level - 1));

        inGame = false;
        startNextSequence = false;
        sequence.clear();

        int scoreNum = level - 1;
        level = 1;

        // In case user fails so fast that animations haven't ended yet
        playSymbolButton.endAnimations();
        txt_instructions.endAnimations();

        // Death sound
        sounds[AndroidSound.failure].play(1);

        // Track the number of games that have been completed
        int gamesCompleted = myPreferences.getInt("gamesCompleted", 0) + 1;
        myPreferences.putInt("gamesCompleted", gamesCompleted);

        // Check for Ad
        // Don't display ad on the very first play
        if (noAdsStatusResult == NOT_OWNED && gamesCompleted > 1) {
            if (interstitialAd.isLoaded()) {
                double rollForAd = random.nextDouble(); // Double in range [0.0, 1.0)
                if (rollForAd < 0.385) { // 38.5% chance for ad
                    if (!TLogging.TESTING) {
                        flog("Ad shown");
                        interstitialAd.show();
                    }
                    else log("Ad not displayed because TESTING");
                }
            }
            // If NOT_OWNED but Ad never began loading (connection issues?), try loading Ad
            else if (!interstitialAd.isLoading()) requestNewInterstitial();
        }
        // If request failed, request again until success, don't show Ad unless sure NOT_OWNED
        else if (noAdsStatusResult == REQUEST_FAILED) {
            noAdsStatusResult = isNoAdsPurchased();
        }
        // else OWNED, don't roll for ads or send more requests

        deathScreen.setValues(scoreNum, pressed, correct);
        deathScreen.animateIn();

        // Unlock screen orientation
        setRequestedOrientation(SCREEN_ORIENTATION_USER);
    }

    // End game in progress, reset variables, and return to main menu
    public void exitGameToMainMenu() {
        flog("Exit to game menu");

        inGame = false;
        mainButtonEnabled = true;
        startNextSequence = false;
        level = 1;
        mainButton.setText("");

        // Remove scheduled tasks
        handler.removeCallbacksAndMessages(null);
        sequence.clear();

        // End animations incase still ongoing and return buttons to normal incase they are in middle of press by sequence
        playSymbolButton.endAnimations();
        txt_instructions.endAnimations();
        for (ColorButton button: colorButtons) {
            button.returnToNormal();
        }

        playSymbolButton.reset();
        mainFadeInAnimation();
        allowColorInput = true;
        setRequestedOrientation(SCREEN_ORIENTATION_USER);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getScreenDimensions();
        flexAll();
    }

    @Override
    public void onBackPressed() {
        if (deathScreen.isDeathScreenUp) {
            deathScreen.performMainMenuClick();
        }
        else if (settingsScreen.isSettingsScreenUp()) {
            settingsScreen.close();
        }
        else if (inGame) {
            if (level > 2) { // 'level > 2' not worth bothering to ask if only just started the game
                Dialogs.leaveCurrentGame(context, this);
            }
            else { exitGameToMainMenu(); }
        }
        else {
            super.onBackPressed(); // Exits app
        }
    }

    // Needed for use by Dialog
    public void superOnBackPress() {
        super.onBackPressed();
    }

    // Change this to use OnLoadListener
    // In case of failure, retry every 100ms until success
    /*private void playStartSound() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Play startup sound
                int x = AndroidSound.sounds[0].playRegardless(0.5f);
                log("ATTEMPT: " + x);

                // A return of 1 means success, if failure, try again in 100 milliseconds
                if (x != 1) playStartSound();
            }
        }, 100);
    }*/

    @Override
    public void onStart() {
        super.onStart();
        flog("Activity Start");

        // Essential so buttons don't stay stuck if app is minimized mid-button-press
        // Done in onStart and not onResume in order to support window switches in multi-screen which call onResume but not onStart
        redButton.returnToNormal();
        yellowButton.returnToNormal();
        greenButton.returnToNormal();
        blueButton.returnToNormal();

        // Load sounds if necessary
        if (AndroidSound.soundPool == null) {
            AndroidSound.newSoundPool();
            AndroidSound.loadSounds(context);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        flog("Activity Resume");
        stopped = false;

        // Fixes bug, in the case sequence is completed but before finger is lifted app becomes paused, never detecting finger lift
        if (startNextSequence) startSequence();
    }

    @Override // Better indication of when the activity becomes visible than onResume
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            if (!popInRan) {
                title.popIn();
                popInRan = true;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        flog("Activity Pause");
    }

    @Override
    public void onStop() {
        super.onStop();
        flog("Activity Stop");
        stopped = true;

        // Don't hog resources from other apps when not infront; don't leak memory and stop sound errors
        // We do this in onStop instead of onPause in order to support multi-screen, onPause is called but onStop isn't on multi-screen window change
        if (AndroidSound.soundPool != null) { // Bug, unsure of the cause, that causes release to be called when things are already released
            AndroidSound.release();
        }
        else {
            flog("SOUNDPOOL ALREADY NULL 856");
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

        // Log Destruction
        flog("Activity Destroyed");
    }


    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }

    public void noAdsOnClick() {
        try {
            flog("Purchase flow started");
            Bundle buyIntentBundle = null;

            if (mService != null) {
                buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "no_ads", "inapp", "Verified by me");
            } else {
                flog("mService is null, purchase flow ended");
                Dialogs.nullmServiceError(context);
                // Attempt to re-establish billing connection
                bindBillingService();
            }

            if (buyIntentBundle != null) {
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                if (pendingIntent != null) {
                    IntentSender intentSender = pendingIntent.getIntentSender();
                    if (intentSender != null)
                        startIntentSenderForResult(intentSender, BILLING_REQUEST_CODE, new Intent(), 0, 0, 0);
                    else {
                        report("intentSender is null");
                        Dialogs.nullIntentSenderError(context);
                    }
                } else { // I believe this happens when item is already purchased // TODO test
                    report("noAdsButton clicked: pendingIntent is null; already purchased? No Google Account logged in? Other Reason?");
                    Dialogs.nullPendingIntentError(context);
                }
            }
        }
        // DeadObjectException (A type of RemoteException) happens when (but uncertain if limited to) mService is not null, but mServiceConn has disconnected
        catch (RemoteException | IntentSender.SendIntentException e) {
            TLogging.report(e);
            Dialogs.remoteException(context);
        }
    }


    // 2002 is arbitrary number, I could use any number for the code. Done to identify that responses with this code are for the purpose of billing
    public static final int BILLING_REQUEST_CODE = 2002;
    private void setNoAdsButton() {
        noAdsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Null check
                if (noAdsStatusResult == null) { report("noAdsStatusResult should never be null 836"); }

                // We know product is already owned
                else if (noAdsStatusResult == OWNED) {
                    Dialogs.noAdsAlreadyPurchased(context);
                }
                else {
                    if (noAdsStatusResult != NOT_OWNED) noAdsStatusResult = isNoAdsPurchased(); // Query again if necessary

                    // Show purchase menu (This might show even if already purchased if query failed or query was never made, unsure how to resolve)
                    Dialogs.purchaseMenu(context, MainActivity.this);
                }

                // Play button sound
                AndroidSound.sounds[AndroidSound.click].play(AndroidSound.VOLUME_CLICK);
            }
        });
    }

    // This handles results from other activities, in particular, I'm using it to handle the results of requests to Google for purchases of my inapp products
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BILLING_REQUEST_CODE) {
            if (data != null) {
                String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

                // Purchase successful
                if (resultCode == RESULT_OK) {
                    try {
                        JSONObject jo = new JSONObject(purchaseData);
                        String productId = jo.getString("productId");
                        flog("You have bought the " + productId + " item!");
                        Dialogs.successfulNoAdsPurchase(context);
                    } catch (JSONException e) {
                        flog("Failed to parse purchase data: " + purchaseData);
                        report(e);
                    }
                }
                // Purchase Cancelled/Failed
                else {
                    flog("Purchase RESULT_CANCELLED: " + resultCode);
                }
            }
            else {
                flog(resultCode + " : " + (resultCode==RESULT_OK));
                report("Null intent data");
                Dialogs.nullIntentError(context);
            }
        }
        else {
            flog("Invalid requestCode: " + requestCode);
            report("Never supposed to get an invalid requestCode");
        }
    }

    // Query Google to see if no_ads has been purchased
    enum noAdsStatus {INITIAL, OWNED, NOT_OWNED, REQUEST_FAILED}
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
                        report("Null ownedProducts 971");
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
