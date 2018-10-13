package hackman.trevor.copycat;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

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

    void setUp(AndroidSound sound, int number) {
        this.sound = sound;
        this.number = number;
    }

    void setColor(int color) {
        this.color = color;
    }

    void playSound() {
        // Balancing the volume out some b/c the higher pitched notes 'sound' louder than lower pitched notes
        float volume = 1;
        switch(number) {
            case 0: volume = .655f; break;
            case 1: volume = .77f; break;
            case 2: volume = .885f; break;
            // case 3: volume = 1; break; // default makes this unnecessary
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

        if (xNew != 0 && yNew != 0) {
            int height = yNew;
            int width = xNew;

            int ratio = 15;
            float cornerRadius = height > width ? height / ratio : width / ratio; // Tertiary for consistency in both portrait & landscape

            drawable = new GradientDrawable();
            drawable.setColor(color);
            drawable.setCornerRadius(cornerRadius);

            pressedDrawable = new GradientDrawable();
            pressedDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            pressedDrawable.setGradientRadius(height / 2);

            int brightened = TMath.brightenColor(color, .5f);
            int[] colors = {brightened, color};
            pressedDrawable.setColors(colors);

            pressedDrawable.setCornerRadius(cornerRadius);
            pressedDrawable.setStroke((int)(Math.min(width, height) * .15), 0x00abcdef); // Transparent border for a simulated shrinkage or 'press' effect

            returnToNormal();
        }
    }

    void flex() {
        if (getHeight() != 0 && getWidth() != 0) {
            int height = getHeight();
            int width = getWidth();

            int ratio = 15;
            float cornerRadius = height > width ? height / ratio : width / ratio; // Tertiary for consistency in both portrait & landscape

            drawable = new GradientDrawable();
            drawable.setColor(color);
            drawable.setCornerRadius(cornerRadius);

            pressedDrawable = new GradientDrawable();
            pressedDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            pressedDrawable.setGradientRadius(height / 2);

            int brightened = TMath.brightenColor(color, .5f);
            int[] colors = {brightened, color};
            pressedDrawable.setColors(colors);

            pressedDrawable.setCornerRadius(cornerRadius);
            pressedDrawable.setStroke((int)(Math.min(width, height) * .15), 0x00abcdef); // Transparent border for a simulated shrinkage or 'press' effect

            returnToNormal();
        }
    }
}
