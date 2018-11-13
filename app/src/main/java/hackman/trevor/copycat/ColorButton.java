package hackman.trevor.copycat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;

import hackman.trevor.tlibrary.library.TColor;
import hackman.trevor.tlibrary.library.TMath;

public class ColorButton extends android.support.v7.widget.AppCompatImageButton {
    private AndroidSound sound;
    private GradientDrawable drawable;
    private LayerDrawable pressedDrawable;
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

            createDrawables(height, width);
        }
    }

    void flex() {
        if (getHeight() != 0 && getWidth() != 0) {
            int height = getHeight();
            int width = getWidth();

            createDrawables(height, width);
        }
    }

    private void createDrawables(int height, int width) {
        int ratio = 15;
        float cornerRadius = height > width ? height / ratio : width / ratio; // Tertiary for consistency in both portrait & landscape
        int min = Math.min(width, height);

        drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(cornerRadius);

        // The shrunk button with radial gradient glow
        GradientDrawable layer1 = new GradientDrawable();
        layer1.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        layer1.setGradientRadius(height / 2);

        int brightened = TMath.brightenColor(color, .5f);
        int[] colors = {brightened, color};
        layer1.setColors(colors);

        layer1.setCornerRadius(cornerRadius);
        layer1.setStroke((int)(min * .155), 0x00abcdef); // Transparent border for a simulated shrinkage or 'press' effect

        // A slight highlight to the button
        GradientDrawable layer0 = new GradientDrawable();
        layer0.setCornerRadius(cornerRadius);
        layer0.setStroke((int)(min * .145), 0x00abcdef);
        layer0.setColor(TColor.saturationShift(color, -0.5));

        Drawable layers[] = new Drawable[2];
        layers[0] = layer0;
        layers[1] = layer1;
        pressedDrawable = new LayerDrawable(layers);

        returnToNormal();
    }
}
