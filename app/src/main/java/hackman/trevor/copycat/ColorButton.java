package hackman.trevor.copycat;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;

import hackman.trevor.library.MyMath;

public class ColorButton extends android.support.v7.widget.AppCompatImageButton {
    private final Context context;
    private int color;
    private int number;
    private SoundPool soundPool;
    private int soundId;

    // Is this necessary?
    public ColorButton(Context context) {
        super(context);
        this.context = context;
    }

    public ColorButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void setUp(int color, int number, int sound) {
        this.color = color;
        this.number = number;

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load(context, sound, 1);
    }

    public void light() {
        GradientDrawable gd = new GradientDrawable();

//        gd.setColor(MyMath.darkenColor(color, .60f)); // Instead of brightening the color, darkening is an interesting alternative
        gd.setColor(MyMath.brightenColor(color, .75f));

        gd.setStroke(15, 0x00abcdef); // Transparent border for a simulated shrinkage or 'press' effect
        setBackground(gd);
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
