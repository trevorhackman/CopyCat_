package hackman.trevor.copycat;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import hackman.trevor.library.MyDialog;
import hackman.trevor.library.MyPreferences;

import static hackman.trevor.library.Logging.flog;
import static hackman.trevor.library.Logging.log;
import static hackman.trevor.library.Logging.report;

public class MainActivity extends AppCompatActivity {
    // Saved Data
    MyPreferences myPreferences;

    // Ad
    InterstitialAd interstitialAd;

    // For Billing
    IInAppBillingService mService;
    final ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
            log("mService connected");

            noAdsStatus = isNoAdsPurchased(); // We have to wait for connection else nullReferenceError
            if (noAdsStatus == NOT_OWNED)
                requestNewInterstitial();

            // Query for items available for purchase once connected
            // Commented out b/c I actually make no use of it with this app, only used it for testing
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

    LinearLayout deathScreen;
    LinearLayout settingsScreen;

    Button mainButton;
    Button symbolButton;
    Button continueButton;
    Button settingsButton;
    Button settingsCloseButton;
    Button leftArrowButton;
    Button rightArrowButton;
    Button starButton;
    Button noAdsButton;
    Button moreGamesButton;

    ColorButton greenButton;
    ColorButton redButton;
    ColorButton yellowButton;
    ColorButton blueButton;

    TextView score;
    TextView highScore;
    TextView instructions;
    TextView speedText;

    View.OnTouchListener mainButtonListener;

    // Static Integers
    final static int PLAY_GREEN = 0; // Highest
    final static int PLAY_RED = 1; // 2nd Highest
    final static int PLAY_YELLOW = 2; // 3rd Highest
    final static int PLAY_BLUE = 3; // 4th Highest

    // This is the handler along with my own method I've come up with to stop the
    // potential memory leaks w/o a static trail making a mess of the entire class
    static Runner runner;
    private class Runner { // I wish to make this a singleton, but that'd defeat the purpose of removing the static trail
        private void playColor(int key) {
            switch (key) {
                case PLAY_GREEN:
                    MainActivity.this.playColor(greenButton);
                    break;
                case PLAY_RED:
                    MainActivity.this.playColor(redButton);
                    break;
                case PLAY_YELLOW:
                    MainActivity.this.playColor(yellowButton);
                    break;
                case PLAY_BLUE:
                    MainActivity.this.playColor(blueButton);
                    break;
            }
        }
    }
    final static MyHandler handler = new MyHandler();
    private static class MyHandler extends Handler {
        public void handleMessage(Message message) {
            final int what = message.what;
            switch (what) {
                case PLAY_GREEN:
                    runner.playColor(PLAY_GREEN);
                    break;
                case PLAY_RED:
                    runner.playColor(PLAY_RED);
                    break;
                case PLAY_YELLOW:
                    runner.playColor(PLAY_YELLOW);
                    break;
                case PLAY_BLUE:
                    runner.playColor(PLAY_BLUE);
                    break;
            }
        }
    }

    // Final objects
    final Context context = this;
    final ArrayList<Integer> sequence = new ArrayList<>(); // The sequence of colors to be played
    final Random random = new Random();
    final Timer timer = new Timer();

    // Animators
    ObjectAnimator fadeOutPlaySymbol;
    ObjectAnimator fadeInDeathScreen;
    ObjectAnimator fadeOutDeathScreen;
    ObjectAnimator fadeInInstructions;
    ObjectAnimator fadeOutInstructions;
    ObjectAnimator fadeInSettings;
    ObjectAnimator fadeOutSettings;
    ObjectAnimator fadeInStar;
    ObjectAnimator fadeOutStar;
    ObjectAnimator fadeInNoAds;
    ObjectAnimator fadeOutNoAds;
    ObjectAnimator fadeInMoreGames;
    ObjectAnimator fadeOutMoreGames;

    // Sounds
    final int sound1 = R.raw.chip1;
    final int sound2 = R.raw.chip2;
    final int sound3 = R.raw.chip3;
    final int sound4 = R.raw.chip4;
    final int soundDeath = R.raw.chip5_2;
    private SoundPool soundPool;
    private int soundId;

    // Declare variables
    boolean[] colorsLit; // Keeps track of what colors are lit - wait until all false to start next sequence
    boolean allowColorInput; // Close input while sequence is playing, open when it's the player's turn to repeat
    boolean fadeOutDeathScreenRan; // Keeps track of whether animation has run once yet per death and keeps track of whether dead or not
    boolean isStart; // Keeps track of whether the game has started yet or not
    boolean isSettingsScreenUp; // Keeps track of whether settings screen is open or not
    boolean startNextSequence; // False until sequence is finished, true to continue to next level/sequence
    long milliSecondsToLight; // The length of time a color is played (visual and sound) for during a sequence
    long milliSecondsDelay; // Delay between lights
    int level; // What level the user is on
    int sequenceTraveler; // Iterates as the sequence and player plays
    int noAdsStatus; // Tracks the response from isNoAdsPurchased()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flog("Activity Start"); // Seems useful to log start of app for crash reports

        // Initialize miscellaneous objects
        runner = new Runner();
        myPreferences = new MyPreferences(context);

        // Initialize variables
        allowColorInput = true;
        colorsLit = new boolean[4];
        fadeOutDeathScreenRan = true;
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
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load(context, soundDeath, 1);

        // Initialize Death Screen
        deathScreen = findViewById(R.id.deathScreen);
        score = findViewById(R.id.score);
        highScore = findViewById(R.id.highScore);

        // Initialize Settings Screen
        settingsScreen = findViewById(R.id.settingsScreen);
        settingsCloseButton = findViewById(R.id.settingsCloseButton);
        speedText = findViewById(R.id.speedText);
        leftArrowButton = findViewById(R.id.leftArrow);
        rightArrowButton = findViewById(R.id.rightArrow);

        // Initialize Instructions
        instructions = findViewById(R.id.instructions);
        instructions.setAlpha(0.0f);

        // Initialize buttons
        mainButton = findViewById(R.id.mainButton);
        symbolButton = findViewById(R.id.symbolButton);
        continueButton = findViewById(R.id.continueButton);
        settingsButton = findViewById(R.id.settings);
        starButton = findViewById(R.id.star);
        noAdsButton = findViewById(R.id.noAds);
        moreGamesButton = findViewById(R.id.moreGames);

        greenButton = findViewById(R.id.greenButton);
        redButton = findViewById(R.id.redButton);
        yellowButton = findViewById(R.id.yellowButton);
        blueButton = findViewById(R.id.blueButton);

        // Setup buttons
        setMainButton();
        setContinueButton();
        setSettingsButton();
        setSettingsCloseButton();
        setLeftArrowButton();
        setRightArrowButton();
        setStarButton();
        setNoAdsButton();
        setMoreGamesButton();
        // These should be merged together, setButtons should all systemized and done under the ColorButton class, not here
        greenButton.setUp(ContextCompat.getColor(context, R.color.green), 0, sound1);
        redButton.setUp(ContextCompat.getColor(context, R.color.red), 1, sound2);
        yellowButton.setUp(ContextCompat.getColor(context, R.color.yellow), 2, sound3);
        blueButton.setUp(ContextCompat.getColor(context, R.color.blue), 3, sound4);
        setButton(greenButton);
        setButton(redButton);
        setButton(yellowButton);
        setButton(blueButton);

        // Initialize speed
        setSpeed();

        // Initialize animations
        fadeOutPlaySymbol = ObjectAnimator.ofFloat(symbolButton, "alpha", 0.0f);
        fadeOutPlaySymbol.setDuration(1000);

        fadeInDeathScreen = ObjectAnimator.ofFloat(deathScreen, "alpha", 1.0f);
        fadeInDeathScreen.setDuration(500);
        fadeInDeathScreen.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                mainButton.setText("");
                symbolButton.setAlpha(1.0f);
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
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                // Move to back - no built in method for it like bringToFront()
                final ViewGroup parent = (ViewGroup)deathScreen.getParent();
                parent.removeView(deathScreen);
                parent.addView(deathScreen, 0);

                deathScreen.setClickable(true);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        fadeInInstructions = ObjectAnimator.ofFloat(instructions, "alpha", 0.9f);
        fadeInInstructions.setDuration(1000);
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

        fadeOutInstructions = ObjectAnimator.ofFloat(instructions, "alpha", 0.0f);
        fadeOutInstructions.setDuration(1000);
        fadeOutInstructions.setStartDelay(2000);

        fadeInSettings = ObjectAnimator.ofFloat(settingsButton, "alpha", 1.0f);
        fadeInSettings.setDuration(1000);
        fadeInSettings.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                settingsButton.setClickable(true);
            }

            @Override
            public void onAnimationEnd(Animator animator) {}

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        fadeOutSettings = ObjectAnimator.ofFloat(settingsButton, "alpha", 0.0f);
        fadeOutSettings.setDuration(1000);
        fadeOutSettings.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                settingsButton.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animator) {}

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        fadeInStar = ObjectAnimator.ofFloat(starButton, "alpha", 1.0f);
        fadeInStar.setDuration(1000);
        fadeInStar.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                starButton.setClickable(true);
            }

            @Override
            public void onAnimationEnd(Animator animator) {}

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        fadeOutStar = ObjectAnimator.ofFloat(starButton, "alpha", 0.0f);
        fadeOutStar.setDuration(1000);
        fadeOutStar.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                starButton.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animator) {}

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        fadeInNoAds = ObjectAnimator.ofFloat(noAdsButton, "alpha", 1.0f);
        fadeInNoAds.setDuration(1000);
        fadeInNoAds.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                noAdsButton.setClickable(true);
            }

            @Override
            public void onAnimationEnd(Animator animator) {}

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        fadeOutNoAds = ObjectAnimator.ofFloat(noAdsButton, "alpha", 0.0f);
        fadeOutNoAds.setDuration(1000);
        fadeOutNoAds.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                noAdsButton.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animator) {}

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        fadeInMoreGames = ObjectAnimator.ofFloat(moreGamesButton, "alpha", 1.0f);
        fadeInMoreGames.setDuration(1000);
        fadeInMoreGames.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                moreGamesButton.setClickable(true);
            }

            @Override
            public void onAnimationEnd(Animator animator) {}

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        fadeOutMoreGames = ObjectAnimator.ofFloat(moreGamesButton, "alpha", 0.0f);
        fadeOutMoreGames.setDuration(1000);
        fadeOutMoreGames.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                moreGamesButton.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animator) {}

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
    }

    private void playColor(final ColorButton button) {
        button.light(); // Light Button
        button.playSound();

        // Wait a bit then darken button
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // All UI changes have to run on UI thread or errors occur
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button.darken();
                    }
                });
                // Checks to see if there's more colors to play
                if (sequenceTraveler + 1 < sequence.size()) {
                    sequenceTraveler++;
                    // After delay play next color
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(sequence.get(sequenceTraveler)); // Play next color
                        }
                    }, milliSecondsDelay);
                }
                // Else sequence is over - allow player input again
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
                // A Firebase crash report shows my app somehow crashing from Exception java.lang.IndexOutOfBoundsException: Invalid index 0, size is 0 from the line 'handler.sendEmptyMessage(sequence.get(0));'
                // I can't figure out how to reproduce this rare crash (Is it from nanosecond gap abuse?), but this if statement should at least stop the crash
                if (sequence.size() > 0)
                    handler.sendEmptyMessage(sequence.get(0));
            }
        }, (int) (milliSecondsDelay * 8 + milliSecondsToLight)); // Weird balancing I've found to like across speed settings
    }

    private void setMainButton() {
        mainButtonListener = new View.OnTouchListener() {
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
                        // Game starts
                        if (!isStart) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                            isStart = true;
                            fadeInInstructions.start();

                            // Fade out buttons
                            fadeOutPlaySymbol.start();
                            fadeOutSettings.start();
                            fadeOutStar.start();
                            fadeOutNoAds.start();
                            fadeOutMoreGames.start();

                            startSequence();
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
                // We have to disable the mainButton while settings are up (currently it still shows b/c of the small size of the settings screen
                mainButton.setOnTouchListener(null);
                settingsScreen.bringToFront();
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
                mainButton.setOnTouchListener(mainButtonListener);
                isSettingsScreenUp = false;
            }
        });
    }

    private void setStarButton() {
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyDialog.createAlertDialog(context, R.string.rate_the_app_title, R.string.rate_the_app_message, R.string.OK, R.string.Cancel, new DialogInterface.OnClickListener() {
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
                MyDialog.createAlertDialog(context, R.string.more_games_title, R.string.more_games_message, R.string.OK, R.string.Cancel, new DialogInterface.OnClickListener() {
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

    private void setContinueButton() {
        continueButton.setOnClickListener(new View.OnClickListener() {
            // On Continue Click
            public void onClick(View view) {
                if (!fadeOutDeathScreenRan) {
                    fadeOutDeathScreenRan = true;
                    fadeOutDeathScreen.start();
                    deathScreen.setClickable(false);

                    // Fade in corner buttons
                    fadeInSettings.start();
                    fadeInStar.start();
                    fadeInNoAds.start();
                    fadeInMoreGames.start();
                }
            }
        });
    }

    // For the 4 colored buttons, sets up touch listeners
    private void setButton(final ColorButton button) {
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (allowColorInput || colorsLit[button.getNumber()]) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        button.light(); // Light button when pressed.
                        button.playSound();
                        colorsLit[button.getNumber()] = true;

                        if (isStart) {
                            if (sequenceTraveler >= sequence.size()) onFailure(); // This occurs if user incorrectly inputs extra colors, conveniently also stops array out of bounds
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
                                else if (sequenceTraveler > sequence.size()) report("WTF: sT > s.s should never happen");
                            }

                            else onFailure(); // Failure
                        }
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        v.performClick();
                        colorsLit[button.getNumber()] = false;

                        if (startNextSequence) {
                            // Check to see if any are still lit, don't start next sequence until none are
                            boolean anyLit = false;
                            for (boolean b: colorsLit)
                                if (b) {
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

    private void onFailure() {
        isStart = false;
        startNextSequence = false;
        sequence.clear();

        // In case user fails so fast fade animations are still going
        fadeOutPlaySymbol.end();
        fadeOutInstructions.end();

        // Death sound
        soundPool.play(soundId, 1, 1, 1, 0, 1f);

        // Check for Ad
        if (noAdsStatus == NOT_OWNED) {
            if (interstitialAd.isLoaded()) {
                int rollForAd = random.nextInt(2); // A 1/2 chance
                if (rollForAd == 0) interstitialAd.show();
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
        score.setText(scoreNum_text);

        int highScoreNum = myPreferences.getInt("highscore", 0);
        String highScoreNum_text = "" + highScoreNum;
        highScore.setText(highScoreNum_text);

        fadeOutDeathScreenRan = false;
        deathScreen.bringToFront();
        fadeInDeathScreen.start();
    }

    @Override
    public void onBackPressed() {
        if (!fadeOutDeathScreenRan) {
            continueButton.performClick();
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
        goFullScreen();

        // This code fixes bug 2.8
        redButton.darken();
        yellowButton.darken();
        greenButton.darken();
        blueButton.darken();
        for (int i = 0; i < colorsLit.length; ++i) {
            colorsLit[i] = false;
        }
    }

    @Override // Goes fullscreen
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) goFullScreen();
    }

    @TargetApi(19) // Doesn't seem to break anything on sub-19 API lvl
    private void goFullScreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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
            speedText.setText(R.string.Normal);

            // Fade out left arrow since no more settings to the left
            Drawable leftArrowDrawable = leftArrowButton.getBackground();
            leftArrowDrawable.setColorFilter(0x44ffffff, PorterDuff.Mode.MULTIPLY);
        }
        else if (speed == 1) { // Fast
            milliSecondsToLight = 300;
            milliSecondsDelay = 65;
            speedText.setText(R.string.Fast);

            // Unfade left arrow
            Drawable leftArrowDrawable = leftArrowButton.getBackground();
            leftArrowDrawable.clearColorFilter();
        }
        else if (speed == 2) { // Extreme
            milliSecondsToLight = 150;
            milliSecondsDelay = 45;
            speedText.setText(R.string.Extreme);

            // Unfade right arrow
            Drawable rightArrowDrawable = rightArrowButton.getBackground();
            rightArrowDrawable.clearColorFilter();
        }
        else if (speed == 3) { // Insane
            milliSecondsToLight = 75;
            milliSecondsDelay = 30;
            speedText.setText(R.string.Insane);

            // Fade out right arrow since no more settings to the right
            Drawable rightArrowDrawable = rightArrowButton.getBackground();
            rightArrowDrawable.setColorFilter(0x44ffffff, PorterDuff.Mode.MULTIPLY);
        }
        else { // else should never happen
            log("Invalid speed setting: " + speed);
        }
    }

    private void setNoAdsButton() {
        noAdsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "no_ads", "inapp", "Verified by me");
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                    if (pendingIntent != null) {
                        IntentSender intentSender = pendingIntent.getIntentSender();
                        if (intentSender != null)
                            // 1001 is arbitrary code, I could use any code. Done to identify that responses with this code are for the purpose of billing
                            startIntentSenderForResult(intentSender, 1001, new Intent(), 0, 0, 0);
                        else ; // This happens when item is already purchased? // TODO Test and Add dialog
                    } else {
                        flog("noAdsButton clicked: pendingIntent is null; suspect no Google Account logged into Android device");
                        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
                        builder.setTitle(R.string.null_intent_sender_error_title).setMessage(R.string.null_intent_sender_error_message)
                                .setNeutralButton(R.string.OK, null)
                                .create()
                                .show();
                    }
                }
                catch (RemoteException | IntentSender.SendIntentException e) { report(e); }
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
                    String productID = jo.getString("productID");
                    flog("You have bought the " + productID + " item!");
                }
                catch (JSONException e) {
                    flog("Failed to parse purchase data.");
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
