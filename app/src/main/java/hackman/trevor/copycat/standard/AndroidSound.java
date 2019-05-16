package hackman.trevor.copycat.standard;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import hackman.trevor.copycat.MainActivity;
import hackman.trevor.copycat.R;

import static hackman.trevor.tlibrary.library.TLogging.flog;
import static hackman.trevor.tlibrary.library.TLogging.report;

/*
* Each instance of AndroidSound corresponds to one sound.
* All sounds are statically managed.
* */

public class AndroidSound {
    public static SoundPool soundPool;
    private final int soundId;
    private final float volume;

    // True allows the sound to play without waiting for activity lifecycle, needed for a sound to start asap
    // Should be true for only one sound at most
    private final boolean startUpSound;

    public static AndroidSound chip1;   // Sound 1 (Highest)
    public static AndroidSound chip2;   // Sound 2
    public static AndroidSound chip3;   // Sound 3
    public static AndroidSound chip4;   // Sound 4 (Lowest)
    public static AndroidSound failure; // Death sound
    public static AndroidSound click;   // Button click sound

    // Volume levels
    public static final float VOLUME_CHIP1 =    0.655f;
    public static final float VOLUME_CHIP2 =    0.77f;
    public static final float VOLUME_CHIP3 =    0.885f;
    public static final float VOLUME_CHIP4 =    1.0f;
    public static final float VOLUME_FAILURE =  1.0f;
    public static final float VOLUME_CLICK =    0.3f;


    // Private constructor
    private AndroidSound(Context context, int resource, float volume, boolean startUpSound) {
        soundId = soundPool.load(context, resource, 1);
        this.volume = volume;
        this.startUpSound = startUpSound;
    }

    // Return 1 for success, 0 for failure, -1 for activity stopped (don't want to play sounds when app is in background)
    public int play(Context context) {
        if (!MainActivity.stopped || startUpSound) {
            try {
                int result = soundPool.play(soundId, volume, volume, 0, 0, 1);
                return result != 0 ? 1 : 0; // 0 if failure, 1 if success
            } // May happen if soundPool is unloaded, but not reloaded
            catch (NullPointerException e) {
                // Report and attempt recovery of sounds
                report(e, "FALAL SOUND PLAY 100");
                initializeSounds(context);
                return 0;
            }
        }
        return -1;
    }

    // Call to load sounds in onCreate, onStart, and onResume (be thorough)
    public static void initializeSounds(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(20)
                    .build();
        }
        else {
            soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
        }

        // soundPool onLoadCompleteListener listens to the loading of individual sounds
        // Currently only using this for logging
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            // loadID seems to be a number 1 to n, where n is how many sounds I have, numbered in order of creation, not any different it seems to soundId
            // status = 0 is success. What other values can it have? No idea, android documentation is extremely lacking.
            public void onLoadComplete(SoundPool soundPool, int loadID, int status) {
                // TODO Improve this - Retry to load, if repeated failure report
                if (status != 0) flog("Sound failed to load. Status: " + status + ", id: " + loadID);
            }
        });


        // Balancing the volume out some b/c the higher pitched notes 'sound' louder than lower pitched notes
        chip1 = new AndroidSound(context, R.raw.chip1, VOLUME_CHIP1, false);
        chip2 = new AndroidSound(context, R.raw.chip2, VOLUME_CHIP2, false);
        chip3 = new AndroidSound(context, R.raw.chip3, VOLUME_CHIP3, false);
        chip4 = new AndroidSound(context, R.raw.chip4, VOLUME_CHIP4, false);
        failure = new AndroidSound(context, R.raw.failure, VOLUME_FAILURE, false);
        click = new AndroidSound(context, R.raw.click, VOLUME_CLICK, false);
    }

    // Release all sound resources, to be called from onStop()
    // Don't hog resources from other apps when not in front; don't leak memory and stop sound errors
    // We do this in onStop instead of onPause in order to support multi-screen, onPause is called but onStop isn't on multi-screen window change
    public static void release() {
        try {
            unloadAll();
            soundPool.release();
            soundPool = null;
        } // May happen if soundPool already released
        catch (NullPointerException e) {
            report(e,"FALAL SOUND RELEASE 47");

            if (soundPool != null) {
                soundPool.release();
                soundPool = null;
            }
        }
    }

    private static void unloadAll() {
        click.unload();
    }

    private void unload() {
        try {
            soundPool.unload(soundId);
        } // May happen if soundPool already released
        catch (NullPointerException e) {
            report(e,"FALAL SOUND UNLOAD 129");
        }
    }
}