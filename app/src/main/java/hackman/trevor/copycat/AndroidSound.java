package hackman.trevor.copycat;

import android.content.Context;
import android.media.SoundPool;

import static hackman.trevor.tlibrary.library.TLogging.log;
import static hackman.trevor.tlibrary.library.TLogging.report;

/*
* Each instance of AndroidSound corresponds to one sound.
* All sounds are statically managed.
* */

public class AndroidSound {
    static SoundPool soundPool;
    static AndroidSound[] sounds;
    private final Context context;
    private final int soundId;

    static void release() {
        try {
            unloadAll();
            soundPool.release();
            soundPool = null;
        } // May happen if soundPool already released
        catch (NullPointerException e) {
            report(e,"FALAL SOUND RELEASE");
        }
    }

    static void newSoundPool() {
        soundPool = new SoundPool.Builder()
                .setMaxStreams(20)
                .build();
    }

    static void loadSounds(Context context) {
        sounds = new AndroidSound[5];
        sounds[0] = new AndroidSound(context, R.raw.chip1);
        sounds[1] = new AndroidSound(context, R.raw.chip2);
        sounds[2] = new AndroidSound(context, R.raw.chip3);
        sounds[3] = new AndroidSound(context, R.raw.chip4);
        sounds[4] = new AndroidSound(context, R.raw.chip5_2);
    }

    static void unloadAll() {
        for (AndroidSound sound: sounds) {
            sound.unload();
        }
    }

    private AndroidSound(Context context, int resource) {
        soundId = soundPool.load(context, resource, 1);
        this.context = context;
    }

    void play(float volume) {
        if (!MainActivity.stopped) {
            try {
                soundPool.play(soundId, volume, volume, 0, 0, 1);
            } // May happen if soundPool is unloaded, but not reloaded
            catch (NullPointerException e) {
                // Report and attempt recovery of sounds
                report(e, "FALAL SOUND PLAY");
                newSoundPool();
                loadSounds(context);
            }
        }
    }

    void unload() {
        try {
            soundPool.unload(soundId);
        } // May happen if soundPool already released
        catch (NullPointerException e) {
            report(e,"FALAL SOUND UNLOAD");
        }
    }
}