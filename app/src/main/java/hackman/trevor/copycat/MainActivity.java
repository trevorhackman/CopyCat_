package hackman.trevor.copycat;

import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;

import hackman.trevor.copycat.system.Ads;
import hackman.trevor.copycat.system.AndroidSound;
import hackman.trevor.copycat.system.Billing;
import hackman.trevor.copycat.system.Dialogs;
import hackman.trevor.copycat.system.Keys;
import hackman.trevor.copycat.ui.GameScreen;
import hackman.trevor.tlibrary.library.TDimensions;
import hackman.trevor.tlibrary.library.TLogging;
import hackman.trevor.tlibrary.library.TPreferences;
import io.fabric.sdk.android.Fabric;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER;
import static hackman.trevor.tlibrary.library.TLogging.flog;
import static hackman.trevor.tlibrary.library.TLogging.log;

public class MainActivity extends AppCompatActivity {
    // Saved Data
    private TPreferences tPreferences;
    public TPreferences tPreferences() { return tPreferences; }

    // The app's one and only handler
    private Handler handler;
    public Handler handler() { return handler; }

    // The one and only screen
    private GameScreen gameScreen;
    public GameScreen gameScreen() { return gameScreen; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Crashlytics disabled by default, automatically enable it here if not testing
        if (!TLogging.TESTING) {
            Fabric.with(this, new Crashlytics()); // Enable crashlytics
            Log.e("TT_", "Release Mode");
        }
        else log("TESTING");

        // Initialize
        TDimensions.setUp(this);

        // Log start of app and screen size
        flog("Activity Create: Screen:["+ TDimensions.getWidthPixels() + "(width) by " + TDimensions.getHeightPixels() + "(height)]");

        // Initialize save system
        tPreferences = new TPreferences(this);

        // Disable rewarded video unlock for now - need to rethink things
        tPreferences().putBoolean(Keys.isRewardedGameModes, true);

        // Ads
        if (!tPreferences().getBoolean(Keys.isNoAdsOwned, false)) {
            Ads.initializeMobileAds(this);
            Ads.initializeInterstitial(this);
            Ads.initializeBanner(this);
            if (!tPreferences().getBoolean(Keys.isRewardedGameModes, false)) {
                Ads.initializeRewarded(this);
            }
        }

        // For sound
        setVolumeControlStream(AudioManager.STREAM_MUSIC); // Makes the volume wheel control music (all game sounds grouped in music) by default
        AndroidSound.initializeSounds(this);

        // Possibly redundant as this is declared in manifest, but just incase
        setRequestedOrientation(SCREEN_ORIENTATION_USER);

        // Load layout
        setContentView(R.layout.activity_main);

        // Initialize screen
        handler = new Handler();
        gameScreen = new GameScreen(this);
        gameScreen.initialize();
        gameScreen.flexAll();

        // Try to connect to billing
        Billing.startConnection(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Vital to reinitialize when change in screen dimensions
        TDimensions.setUp(this);

        // Resize UI
        gameScreen.flexAll();
    }

    @Override
    public void onBackPressed() {
        if (gameScreen.deathMenu.isDeathScreenUp) {
            gameScreen.deathMenu.performMainMenuClick();
        }
        else //noinspection StatementWithEmptyBody
            if (gameScreen.deathMenu.isDeathScreenComing) {
            // Intentionally do nothing
        }
        else if (gameScreen.settingsMenu.isSettingsScreenUp) {
            gameScreen.settingsMenu.close();
        }
        else if (gameScreen.modesMenu.isModesMenuUp) {
                gameScreen.modesMenu.close();
        }
        else if (gameScreen.inGame) {
            if (gameScreen.level > 2) { // 'level > 2' not worth bothering to ask if only just started the game
                Dialogs.leaveCurrentGame(this, this);
            }
            else { gameScreen.exitGameToMainMenu(); }
        }
        else {
            super.onBackPressed(); // Exits app
        }
    }

    // Needed for use by Dialog
    public void superOnBackPress() {
        super.onBackPressed();
    }

    @Override
    public void onStart() {
        super.onStart();
        flog("Activity Start");

        // Essential so buttons don't stay stuck if app is minimized mid-button-press
        // Done in onStart and not onResume in order to support window switches in multi-screen which call onResume but not onStart
        gameScreen.redButton.returnToNormal();
        gameScreen.yellowButton.returnToNormal();
        gameScreen.greenButton.returnToNormal();
        gameScreen.blueButton.returnToNormal();

        // Load sounds if necessary
        if (AndroidSound.soundPool == null) {
            AndroidSound.initializeSounds(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        flog("Activity Resume");

        // Fixes bug, in the case sequence is completed but before finger is lifted app becomes paused, never detecting finger lift
        if (gameScreen.startNextSequence) gameScreen.startSequence();

        // Load sounds if necessary
        if (AndroidSound.soundPool == null) {
            AndroidSound.initializeSounds(this);
        }
    }

    @Override // Better indication of when the activity becomes visible than onResume
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            if (!gameScreen.popInRan) {
                gameScreen.title.popIn();
                gameScreen.popInRan = true;
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

        // Don't hog resources from other apps when not in front; don't leak memory and stop sound errors
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

        // Remove scheduled tasks
        handler().removeCallbacksAndMessages(null);

        // Unload sounds if necessary
        if (AndroidSound.soundPool != null) {
            AndroidSound.release();
        }

        // Log Destruction
        flog("Activity Destroyed");
    }
}
