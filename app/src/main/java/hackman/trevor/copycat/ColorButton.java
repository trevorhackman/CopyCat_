package hackman.trevor.copycat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;

import hackman.trevor.tlibrary.library.TColor;

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
        sound.play(getContext());
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
            createDrawables(yNew, xNew);
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

        int brightened = TColor.brightenColor(color, .5f);
        int[] colors = {brightened, color};
        layer1.setColors(colors);

        layer1.setCornerRadius(cornerRadius);
        layer1.setStroke((int)(min * .155), 0x00abcdef); // Transparent border for a simulated shrinkage or 'press' effect

        // A slight highlight to the button
        GradientDrawable layer0 = new GradientDrawable();
        layer0.setCornerRadius(cornerRadius);
        layer0.setStroke((int)(min * .145), 0x00abcdef);
        layer0.setColor(0xaaffffff); // Highlight

        Drawable layers[] = new Drawable[2];
        layers[0] = layer0;
        layers[1] = layer1;
        pressedDrawable = new LayerDrawable(layers);

        returnToNormal();
    }
}
