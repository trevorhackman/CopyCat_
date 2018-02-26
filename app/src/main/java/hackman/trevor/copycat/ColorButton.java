package hackman.trevor.copycat;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.media.SoundPool;
import android.util.AttributeSet;

public class ColorButton extends android.support.v7.widget.AppCompatImageButton {
    private final Context context;
    private SoundPool soundPool;
    private boolean pressed;
    private int color;
    private int number;
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

    public void setUp(int color, int sound, int number) {
        this.color = color;
        this.number = number;

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .build();
        soundId = soundPool.load(context, sound, 1);
    }

    public void press() {
        pressed = true;
        pressedEffect();
        playSound();
    }

    public void pressedEffect() {
        GradientDrawable gd = new GradientDrawable();

        int brightened = hackman.trevor.tlibrary.library.TMath.brightenColor(color, .5f);

//        gd.setColor(MyMath.darkenColor(color, .60f)); // Instead of brightening the color, darkening is an interesting alternative
//        gd.setColor(MyMath.brightenColor(color, .75f)); // Old, simply brighten

        gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        gd.setGradientRadius(getHeight());
        int[] colors = {brightened, color};
        gd.setColors(colors);
        gd.setCornerRadius(getHeight()/20);
        gd.setStroke(25, 0x00abcdef); // Transparent border for a simulated shrinkage or 'press' effect

        setBackground(gd);
    }

    public void playSound() {
        // Balancing the volume out some b/c the higher pitched notes 'sound' louder than lower pitched notes
        float volume = 1;
        switch(number) {
            case 0: volume = .7f; break;
            case 1: volume = .8f; break;
            case 2: volume = .9f; break;
            // case 3: volume = 1; break; // default makes this unneceessary
        }
        soundPool.play(soundId, volume, volume, 1, 0, 1f);
    }

    public void returnToNormal() {
        pressed = false;

        // setBackgroundColor(color); // Old
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(getHeight()/20);
        setBackground(gd);
    }

    public boolean getPressed() {
        return pressed;
    }

    public int getNumber() {
        return number;
    }

    public void destroy() {
        soundPool.release();
    }
}
