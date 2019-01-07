package hackman.trevor.copycat;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import static hackman.trevor.tlibrary.library.TLogging.flog;
import static hackman.trevor.tlibrary.library.TLogging.report;

/*
* Each instance of AndroidSound corresponds to one sound.
* All sounds are statically managed.
* */

public class AndroidSound {
    public static SoundPool soundPool;
    public static AndroidSound[] sounds;
    private static boolean allSoundsLoaded = false;

    private final Context context;
    private final int soundId;

    // Play ids
    public static int chip1 = 0;
    public static int chip2 = 1;
    public static int chip3 = 2;
    public static int chip4 = 3;
    public static int failure = 4;
    public static int click = 5;

    // Beep volume level
    public static final float VOLUME_CLICK = 0.3f;

    // Private constructor
    private AndroidSound(Context context, int resource) {
        soundId = soundPool.load(context, resource, 1);
        this.context = context;
    }

    // Release all sound resources, to be called from onStop()
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

    // Call this first
    public static void newSoundPool() {
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
            // loadID is a number 1 to n, where n is how many sounds I have, numbered in order of creation, currently equivalent to soundId
            // status = 0 is success. What other values can it have? No idea, android documentation is extremely lacking.
            public void onLoadComplete(SoundPool soundPool, int loadID, int status) {
                if (status != 0) report("Sound failed to load. Status: " + status + ", id: " + loadID);
                if (loadID == 6 && status == 0) {
                    allSoundsLoaded = true;
                    flog("All sounds loaded"); // All sounds successfully loaded
                }

                // TODO This is a test, remove if I don't receive any reports. I should never receive any
                if (loadID > 6) {
                    report("loadID greater than 6 : 84");
                }
            }
        });
    }

    // Call this second
    public static void loadSounds(Context context) {
        sounds = new AndroidSound[6];

        // Startup loaded first since it has to play first
        // TODO find/create a good startup sound
        // sounds[6] = new AndroidSound(context, R.raw.startup_ogg);

        sounds[chip1] = new AndroidSound(context, R.raw.chip1);         // Sound 1 (Highest)
        sounds[chip2] = new AndroidSound(context, R.raw.chip2);         // Sound 2
        sounds[chip3] = new AndroidSound(context, R.raw.chip3);         // Sound 3
        sounds[chip4] = new AndroidSound(context, R.raw.chip4);         // Sound 4 (Lowest)
        sounds[click] = new AndroidSound(context, R.raw.click);         // Button click sound
        sounds[failure] = new AndroidSound(context, R.raw.failure);     // Death sound
    }

    // Return 1 for success, 0 for failure,
    public int play(float volume) {
        if (!MainActivity.stopped) {
            if (allSoundsLoaded) {
                try {
                    int result = soundPool.play(soundId, volume, volume, 0, 0, 1);
                    return result != 0 ? 1 : 0; // 0 if failure, 1 if success
                } // May happen if soundPool is unloaded, but not reloaded
                catch (NullPointerException e) {
                    // Report and attempt recovery of sounds
                    report(e, "FALAL SOUND PLAY : 100");
                    newSoundPool();
                    loadSounds(context);
                    return 0;
                }
            } // else // prior returns
            // Attempt recovery of sounds
            newSoundPool();
            loadSounds(context);
            return 0;
        }
        return -1;
    }

    // For playing starting sounds, don't want to wait for MainActivity.stopped
    /*public int playRegardless(float volume) {
        try {
            int result = soundPool.play(soundId, volume, volume, 0, 0, 1);
            return result != 0 ? 1 : 0; // 0 if failure, 1 if success
        } // May happen if soundPool is unloaded, but not reloaded
        catch (NullPointerException e) {
            // Report and attempt recovery of sounds
            report(e, "FALAL SOUND PLAY REGARDLESS 117");
            newSoundPool();
            loadSounds(context);
            return 0;
        }
    }*/

    private void unload() {
        try {
            soundPool.unload(soundId);
        } // May happen if soundPool already released
        catch (NullPointerException e) {
            report(e,"FALAL SOUND UNLOAD 129");
        }
    }

    private static void unloadAll() {
        allSoundsLoaded = false;
        for (AndroidSound sound: sounds) {
            sound.unload();
        }
    }
}