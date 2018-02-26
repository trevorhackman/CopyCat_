package hackman.trevor.copycat;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import hackman.trevor.tlibrary.library.TDialog;
import hackman.trevor.tlibrary.library.TLogging;
import hackman.trevor.tlibrary.library.TPreferences;

import static hackman.trevor.tlibrary.library.TLogging.flog;
import static hackman.trevor.tlibrary.library.TLogging.report;

public class MainActivity extends AppCompatActivity {
    // For Ads
    final boolean PLAYADS = true; // TODO Make this true for release, keep false when developing/testing unless testing ads
    InterstitialAd interstitialAd;

    // Saved Data
    TPreferences myPreferences;

    // For Billing
    IInAppBillingService mService;
    final ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            flog("mService connected");

            noAdsStatus = isNoAdsPurchased(); // We have to wait for connection else nullReferenceError
            if (noAdsStatus == NOT_OWNED)
                requestNewInterstitial();

            // Query for items available for purchase once connected
            // Commented out b/c I make no use of it within this app, only used it for testing
            /*
            ArrayList<String> responseList; // List of app products and their information sorted by keys
            ArrayList<String> skuList = new ArrayList<>();
            skuList.add("no_ads"); // You need to add the ids for all of your app products
            Bundle querySkus = new Bundle();
            querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
            Bundle skuDetails = null;
            try {
                skuDetails = mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);

                // Get results
                if (skuDetails != null) {
                    int response = skuDetails.getInt("REPONSE_CODE");
                    if (response == 0) {
                        responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                        log("Number of products found: " + responseList.size());
                        for (String thisResponse : responseList) {
                            log(thisResponse);
                            // How to acquire each element of the product by key
                            JSONObject object = new JSONObject(thisResponse);
                            String productId = object.getString("productId");
                            String price = object.getString("price");
                            log("Product ID: " + productId + "\nPrice: " + price);
                        }
                    }
                    else flog("Wrong Response: " + response);
                }
                else flog("skuDetails still null");
            }
            catch (RemoteException | JSONException e) {
                flog("Error getting details of inapp purchases");
                report(e);
            }
            */
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
        }
    };

    LinearLayout buttonBar;
    LinearLayout deathScreen;
    LinearLayout settingsScreen;

    Button mainButton; // The circle, click on it to play, displays level number
    Button symbolButton; // Inside the main button, displays play symbol
    Button mainMenuButton;
    Button playAgainButton;
    Button settingsButton;
    Button settingsCloseButton;
    Button leftArrowButton;
    Button rightArrowButton;
    Button starButton;
    Button noAdsButton;
    Button moreGamesButton;

    ColorButton[] colorButtons = new ColorButton[4];
    ColorButton greenButton;
    ColorButton redButton;
    ColorButton yellowButton;
    ColorButton blueButton;

    ImageView title;
    ImageView title_background;

    TextView txt_score;
    TextView txt_highscore;
    TextView txt_pressed;
    TextView txt_correct;
    TextView txt_instructions;
    TextView txt_speedText;

    View.OnTouchListener mainButtonListener;

    // Static Integers
    final static int GREEN = 0; // Highest
    final static int RED = 1; // 2nd Highest
    final static int YELLOW = 2; // 3rd Highest
    final static int BLUE = 3; // 4th Highest

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
    final Timer timer = new Timer();

    // Animations - Main screen fade out and in
    ObjectAnimator fadeOutTitle;
    ObjectAnimator fadeOutTitleBackground;
    ObjectAnimator fadeOutButtonBar;
    ObjectAnimator fadeOutPlaySymbol;
    ObjectAnimator fadeInTitle;
    ObjectAnimator fadeInTitleBackground;
    ObjectAnimator fadeInButtonBar;
    AnimatorSet mainFadeOutAnimatorSet;
    AnimatorSet mainFadeInAnimatorSet;
    final int mainFadeDuration = 1000; // In milliseconds

    // Other Animations
    ObjectAnimator fadeOutInstructions;
    ObjectAnimator fadeInInstructions;
    ObjectAnimator fadeInDeathScreen;
    ObjectAnimator fadeOutDeathScreen;
    ScaleAnimation animatePlaySymbol;
    Animator titleScaleY;
    Animator titleScaleX;
    AnimatorSet titleAnimatorSet;



    // Sounds
    final int sound1 = R.raw.chip1;
    final int sound2 = R.raw.chip2;
    final int sound3 = R.raw.chip3;
    final int sound4 = R.raw.chip4;
    final int soundDeath = R.raw.chip5_2;
    private SoundPool soundPool;
    private int soundId;

    // Declare variables
    boolean allowColorInput; // Close input while sequence is playing, open when it's the player's turn to repeat
    boolean isDeathScreenUp; // Keeps track of whether death screen is fully faded in or not
    boolean isStart; // Keeps track of whether the game has started yet or not
    boolean isSettingsScreenUp; // Keeps track of whether settings screen is open or not
    boolean startGameAfterFadeOut; // If true, game will start after the deathscreen finishes fading away. Set on playAgainButton click
    boolean startNextSequence; // False until sequence is finished, true to continue to next level/sequence
    long milliSecondsToLight; // The length of time a color is played (visual and sound) for during a sequence
    long milliSecondsDelay; // Delay between lights
    int level; // What level the user is on
    int sequenceTraveler; // Iterates as the sequence and player plays
    int noAdsStatus; // Tracks the response from isNoAdsPurchased()

    // Screen size, obtained in onCreate
    int pixelHeight;
    int pixelWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get screen dimensions of device
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        pixelHeight = size.y;
        pixelWidth = size.x;

        // Log start of app and screen size
        flog("Activity Start: Screen:["+ pixelWidth + "p by " + pixelHeight + "p]");

        // Initialize miscellaneous objects
        runner = new Runner();
        myPreferences = new TPreferences(context);

        // Initialize variables
        allowColorInput = true;
        isDeathScreenUp = false;
        isStart = false;
        level = 1;
        startNextSequence = false;
        isSettingsScreenUp = false;

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
        soundPool = new SoundPool.Builder().build();
        soundId = soundPool.load(context, soundDeath, 1);

        // Initialize Death Screen
        deathScreen = findViewById(R.id.deathScreen);
        txt_score = findViewById(R.id.score);
        txt_highscore = findViewById(R.id.highScore);
        txt_pressed = findViewById(R.id.pressed);
        txt_correct = findViewById(R.id.correct);

        // Initialize Settings Screen
        settingsScreen = findViewById(R.id.settingsScreen);
        settingsCloseButton = findViewById(R.id.settingsCloseButton);
        txt_speedText = findViewById(R.id.speedText);
        leftArrowButton = findViewById(R.id.leftArrow);
        rightArrowButton = findViewById(R.id.rightArrow);

        // Initialize Title and Instructions
        title = findViewById(R.id.title_logo);
        title_background = findViewById(R.id.title_background);
        txt_instructions = findViewById(R.id.instructions);

        // Initialize buttons
        buttonBar = findViewById(R.id.button_bar);
        settingsButton = findViewById(R.id.settings);
        starButton = findViewById(R.id.star);
        noAdsButton = findViewById(R.id.noAds);
        moreGamesButton = findViewById(R.id.moreGames);
        mainButton = findViewById(R.id.mainButton);
        symbolButton = findViewById(R.id.symbolButton);
        mainMenuButton = findViewById(R.id.mainMenuButton);
        playAgainButton = findViewById(R.id.playAgainButton);

        greenButton = findViewById(R.id.greenButton);
        redButton = findViewById(R.id.redButton);
        yellowButton = findViewById(R.id.yellowButton);
        blueButton = findViewById(R.id.blueButton);
        colorButtons[0] = greenButton;
        colorButtons[1] = redButton;
        colorButtons[2] = yellowButton;
        colorButtons[3] = blueButton;

        // Setup color buttons
        greenButton.setUp(ContextCompat.getColor(context, R.color.green), sound1, 0);
        redButton.setUp(ContextCompat.getColor(context, R.color.red), sound2, 1);
        yellowButton.setUp(ContextCompat.getColor(context, R.color.yellow), sound3, 2);
        blueButton.setUp(ContextCompat.getColor(context, R.color.blue), sound4, 3);
        setButton(greenButton);
        setButton(redButton);
        setButton(yellowButton);
        setButton(blueButton);

        // Setup buttons
        setMainButton();
        setMainMenuButton();
        setPlayAgainButton();
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
        fadeOutTitle = ObjectAnimator.ofFloat(title, "alpha", 0.0f);
        fadeOutTitle.setDuration(mainFadeDuration);

        fadeInTitle = ObjectAnimator.ofFloat(title, "alpha", 1.0f);
        fadeInTitle.setDuration(mainFadeDuration);

        fadeOutTitleBackground = ObjectAnimator.ofFloat(title_background, "alpha", 0.0f);
        fadeOutTitleBackground.setDuration(mainFadeDuration);

        fadeInTitleBackground = ObjectAnimator.ofFloat(title_background, "alpha", 1.0f);
        fadeInTitleBackground.setDuration(mainFadeDuration);

        fadeOutPlaySymbol = ObjectAnimator.ofFloat(symbolButton, "alpha", 0.0f);
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

        // Combine animations into sets
        mainFadeInAnimatorSet = new AnimatorSet();
        mainFadeOutAnimatorSet = new AnimatorSet();
        mainFadeInAnimatorSet.play(fadeInButtonBar).with(fadeInTitle).with(fadeInTitleBackground);
        mainFadeOutAnimatorSet.play(fadeOutButtonBar).with(fadeOutPlaySymbol).with(fadeOutTitle).with(fadeOutTitleBackground);

        fadeInInstructions = ObjectAnimator.ofFloat(txt_instructions, "alpha", 1.0f);
        fadeInInstructions.setDuration(500);
        fadeInInstructions.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                fadeOutInstructions.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        fadeOutInstructions = ObjectAnimator.ofFloat(txt_instructions, "alpha", 0.0f);
        fadeOutInstructions.setDuration(1000);
        fadeOutInstructions.setStartDelay(2000);

        fadeInDeathScreen = ObjectAnimator.ofFloat(deathScreen, "alpha", 1.0f);
        fadeInDeathScreen.setDuration(500);
        fadeInDeathScreen.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                deathScreen.bringToFront();
                deathScreen.setTranslationZ(999); // Fix for newer APIs handling bringToFront differently with relativeLayout
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isDeathScreenUp = true;

                // Make deathscreen and buttons clickable once completely faded in
                playAgainButton.setClickable(true);
                mainMenuButton.setClickable(true);

                if (!isStart) {
                    mainButton.setText("");
                    symbolButton.setAlpha(1.0f);
                    playAgainButton.setClickable(true);
                    mainMenuButton.setClickable(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        fadeOutDeathScreen = ObjectAnimator.ofFloat(deathScreen, "alpha", 0.0f);
        fadeOutDeathScreen.setDuration(500);
        fadeOutDeathScreen.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Don't want to be clickable while fading out, also stops multiclicks
                playAgainButton.setClickable(false);
                mainMenuButton.setClickable(false);

                isDeathScreenUp = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (startGameAfterFadeOut) {
                    startGameAfterFadeOut = false;
                    startGame();
                }

                // Manual bring to back
                final ViewGroup parent = (ViewGroup)deathScreen.getParent();
                parent.removeView(deathScreen);
                parent.addView(deathScreen, 0);
                deathScreen.setTranslationZ(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        animatePlaySymbol = new ScaleAnimation(1f, 1.08f, 1f, 1.08f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animatePlaySymbol.setDuration(750);
        animatePlaySymbol.setRepeatCount(-1);
        animatePlaySymbol.setRepeatMode(Animation.REVERSE);
        symbolButton.setAnimation(animatePlaySymbol);
        animatePlaySymbol.start();

        // Animate title on startup
        title.setScaleY(0);
        title.setScaleX(0);
        int animationDuration = 1000;
        titleScaleX = ObjectAnimator.ofFloat(title, "scaleX", 1.0f);
        titleScaleX.setDuration(animationDuration);
        titleScaleY = ObjectAnimator.ofFloat(title, "scaleY", 1.0f);
        titleScaleY.setDuration(animationDuration);
        titleAnimatorSet = new AnimatorSet();
        titleAnimatorSet.play(titleScaleY).with(titleScaleX);
        titleAnimatorSet.setStartDelay(0);
        titleAnimatorSet.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float v) {
                return -2*v*v + 3*v; // Allows me two animations in one! Peaks at v = 0.75 then down to (1,1)
            }
        });

        titleAnimatorSet.start();
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    private void playColor(final ColorButton button) {
        button.press();

        // Wait a bit then return button to normal
        timer.schedule(new TimerTask() {
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
                    timer.schedule(new TimerTask() {
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
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    handler.sendEmptyMessage(sequence.get(0));
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
                        if (!isStart) { // Don't start the game if it has already started
                            startGame();

                            // Fade out buttons and title
                            mainFadeOutAnimatorSet.start();
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

    private void startGame() {
        isStart = true;
        fadeInInstructions.start();

        startSequence();
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

    public void setRightArrowButton() {
        rightArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int speedSetting = myPreferences.getInt("speed", 0);
                if (speedSetting < 3) myPreferences.putInt("speed", ++speedSetting);
                setSpeed();
            }
        });
    }

    public void setLeftArrowButton() {
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
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=" + getPackageName())); // package name is hackman.trevor.copycat
                        startActivity(intent);
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
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://search?q=pub:Hackman"));
                        startActivity(intent);
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

    private void setMainMenuButton() {
        mainMenuButton.setOnClickListener(new View.OnClickListener() {
            // On Continue Click
            public void onClick(View view) {

                fadeOutDeathScreen.start();

                // Fade in buttons and title
//                  fadeInSettings.start();
//                  fadeInStar.start();
//                  fadeInNoAds.start();
//                  fadeInMoreGames.start();
                mainFadeInAnimatorSet.start();
            }
        });
    }

    private void setPlayAgainButton() {
        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                symbolButton.setAlpha(0.0f);

                startGameAfterFadeOut = true;
                mainButton.setText("1"); // Starting level is always 1
                fadeOutDeathScreen.start();
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

                        if (isStart) {
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
                // For the case of pressing a button, then starting the game, then lifting the button up while sequence is playing - Fixes Bug 13: Buttons stuck lit
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    button.returnToNormal();
                }
                return false;
            }
        });
    }

    private void onFailure(int pressed, int correct) {
        isStart = false;
        startNextSequence = false;
        sequence.clear();

        // In case user fails so fast that fadeOutPlaySymbol hasn't ended yet
        fadeOutPlaySymbol.end();

        // Death sound
        soundPool.play(soundId, 1, 1, 1, 0, 1f);

        // Check for Ad
        if (noAdsStatus == NOT_OWNED) {
            if (interstitialAd.isLoaded()) {
                int rollForAd = random.nextInt(3); // A 1/3 chance
                if (PLAYADS && rollForAd == 0) interstitialAd.show();
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

        fadeInDeathScreen.start();
    }

    @Override
    public void onBackPressed() {
        if (isDeathScreenUp) {
            mainMenuButton.performClick();
        }
        else if (isSettingsScreenUp) {
            settingsCloseButton.performClick();
        }
        else { super.onBackPressed(); }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Billing
        if (mService != null) unbindService(mServiceConn);

        // Releasing these soundPools seems to be key in fixing a bug where the sounds would get corrupted
        soundPool.release();
        greenButton.destroy();
        redButton.destroy();
        blueButton.destroy();
        yellowButton.destroy();
    }

    @Override // Goes fullscreen
    public void onResume() {
        super.onResume();
        // goFullScreen();

        // This code fixes bug 2.8
        redButton.returnToNormal();
        yellowButton.returnToNormal();
        greenButton.returnToNormal();
        blueButton.returnToNormal();
    }

    @Override // Goes fullscreen
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // if (hasFocus) goFullScreen();

        // Quick fix to starting buttons with the graphics specified in returnToNormal instead of what is specified in xml
        greenButton.returnToNormal();
        redButton.returnToNormal();
        yellowButton.returnToNormal();
        blueButton.returnToNormal();
    }

    // Old way to go fullscreen, replaced by line in styles.xml, keeping in case needed again in the future
    /*@TargetApi(19) // Doesn't seem to break anything on sub-19 API lvl
    private void goFullScreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }*/

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
            TLogging.flog("Invalid speed setting: " + speed);
        }
    }

    private void setNoAdsButton() {
        noAdsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TDialog.createAlertDialog(context, R.string.no_ads_title, R.string.no_ads_message, R.string.Purchase, R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "no_ads", "inapp", "Verified by me");
                            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                            if (pendingIntent != null) {
                                IntentSender intentSender = pendingIntent.getIntentSender();
                                if (intentSender != null)
                                    // 1001 is arbitrary code, I could use any code. Done to identify that responses with this code are for the purpose of billing
                                    startIntentSenderForResult(intentSender, 1001, new Intent(), 0, 0, 0);
                                else { flog("intentSender is null"); report(); }; // This happens when item is already purchased? // TODO Test and Add dialog
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
        // I use 1001 for billing
        if (requestCode == 1001) {
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
    final static int OWNED = 0;
    final static int NOT_OWNED = 1;
    final static int REQUEST_FAILED = 2;
    private int isNoAdsPurchased() {
        // incase mService which only gets initialized on connection hasn't been initialized yet
        if (mService != null) {
            try {
                Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);
                int response = ownedItems.getInt("RESPONSE_CODE");
                // Request is successful
                if (response == 0) {
                    // Get list of owned items - Warning, this only works for up to 700 owned items (Not a problem for me)
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
