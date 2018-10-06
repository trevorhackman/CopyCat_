package hackman.trevor.copycat;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import static hackman.trevor.tlibrary.library.TLogging.report;

/*
* Each instance of AndroidSound corresponds to one sound.
* All sounds are statically managed.
* */

public class AndroidSound {
    public static SoundPool soundPool;
    public static AndroidSound[] sounds;
    private final Context context;
    private final int soundId;

    // Beep volume level
    public static final float VOLUME_CLICK = 0.25f;

    // Private constructor
    private AndroidSound(Context context, int resource) {
        soundId = soundPool.load(context, resource, 1);
        this.context = context;
    }

    // Release all sound resources, to be called from onPause()
    public static void release() {
        try {
            unloadAll();
            soundPool.release();
            soundPool = null;
        } // May happen if soundPool already released
        catch (NullPointerException e) {
            report(e,"FALAL SOUND RELEASE");
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
    }

    // Call this second
    public static void loadSounds(Context context) {
        sounds = new AndroidSound[6];

        // Startup loaded first since it has to play first
        // Placeholder startup sound TODO find/create a good one,
        // sounds[6] = new AndroidSound(context, R.raw.startup_ogg);

        sounds[0] = new AndroidSound(context, R.raw.chip1);         // Sound 1
        sounds[1] = new AndroidSound(context, R.raw.chip2);         // Sound 2
        sounds[2] = new AndroidSound(context, R.raw.chip3);         // Sound 3
        sounds[3] = new AndroidSound(context, R.raw.chip4);         // Sound 4
        sounds[4] = new AndroidSound(context, R.raw.failure);       // Death sound
        sounds[5] = new AndroidSound(context, R.raw.click);         // Button click sound
    }

    // Return 1 for success, 0 for failure,
    public int play(float volume) {
        if (!MainActivity.stopped) {
            try {
                 int result = soundPool.play(soundId, volume, volume, 0, 0, 1);
                 return result != 0 ? 1 : 0; // 0 if failure, 1 if success
            } // May happen if soundPool is unloaded, but not reloaded
            catch (NullPointerException e) {
                // Report and attempt recovery of sounds
                report(e, "FALAL SOUND PLAY");
                newSoundPool();
                loadSounds(context);
                return 0;
            }
        }
        return -1;
    }

    // For playing starting sounds, don't want to wait
    public int playRegardless(float volume) {
        try {
            int result = soundPool.play(soundId, volume, volume, 0, 0, 1);
            return result != 0 ? 1 : 0; // 0 if failure, 1 if success
        } // May happen if soundPool is unloaded, but not reloaded
        catch (NullPointerException e) {
            // Report and attempt recovery of sounds
            report(e, "FALAL SOUND PLAY");
            newSoundPool();
            loadSounds(context);
            return 0;
        }
    }

    private void unload() {
        try {
            soundPool.unload(soundId);
        } // May happen if soundPool already released
        catch (NullPointerException e) {
            report(e,"FALAL SOUND UNLOAD");
        }
    }

    private static void unloadAll() {
        for (AndroidSound sound: sounds) {
            sound.unload();
        }
    }
}