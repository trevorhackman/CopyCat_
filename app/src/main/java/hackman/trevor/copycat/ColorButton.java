package hackman.trevor.copycat;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class ColorButton extends ImageButton {
    private final Context context;
    private int color;
    private int color_lit;
    private int number;
    private SoundPool soundPool;
    private int soundId;

    public ColorButton(Context context) {
        super(context);
        this.context = context;
    }

    // Is this necessary?
    public ColorButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void setUp(int color, int color_lit, int number, int sound) {
        this.color = color;
        this.color_lit = color_lit;
        this.number = number;

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load(context, sound, 1);
    }

    public void light() {
        setBackgroundColor(color_lit);
    }

    public void darken() {
        setBackgroundColor(color);
    }

    public int getNumber() {
        return number;
    }

    public void playSound() {
        soundPool.play(soundId, 1, 1, 1, 0, 1f);
    }

    public void destroy() {
        soundPool.release();
    }
}
