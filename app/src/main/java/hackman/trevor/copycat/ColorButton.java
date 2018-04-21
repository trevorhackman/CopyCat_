package hackman.trevor.copycat;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import hackman.trevor.tlibrary.library.TMath;

public class ColorButton extends android.support.v7.widget.AppCompatImageButton {
    private AndroidSound sound;
    private GradientDrawable drawable;
    private GradientDrawable pressedDrawable;
    private boolean pressed;
    private int color;
    private int number;

    public ColorButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void setUp(int color, AndroidSound sound, int number) {
        this.color = color;
        this.sound = sound;
        this.number = number;
    }

    void playSound() {
        // Balancing the volume out some b/c the higher pitched notes 'sound' louder than lower pitched notes
        float volume = 1;
        switch(number) {
            case 0: volume = .7f; break;
            case 1: volume = .8f; break;
            case 2: volume = .9f; break;
            // case 3: volume = 1; break; // default makes this unneceessary
        }
        sound.play(volume);
    }

    void press() {
        pressed = true;
        setBackground(pressedDrawable);
        playSound();
    }

    void returnToNormal() {
        pressed = false;
        setBackground(drawable);
    }

    boolean getPressed() {
        return pressed;
    }

    int getNumber() {
        return number;
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        int width = xNew;
        int height = yNew;

        int ratio = 15;
        float cornerRadius = height > width ? height/ratio : width/ratio;

        drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(cornerRadius); // Tertiary for consistency in both portrait & landscape


        pressedDrawable = new GradientDrawable();

        pressedDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        pressedDrawable.setGradientRadius(height/2);

        int brightened = TMath.brightenColor(color, .5f);
        int[] colors = {brightened, color};
        pressedDrawable.setColors(colors);

        pressedDrawable.setCornerRadius(cornerRadius);
        pressedDrawable.setStroke(Math.min(width, height)/6, 0x00abcdef); // Transparent border for a simulated shrinkage or 'press' effect

        returnToNormal();
    }
}
