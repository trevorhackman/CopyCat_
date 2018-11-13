package hackman.trevor.tlibrary.library;

import android.graphics.Color;

// Exclusively color-related algorithms
// Parameter int color always refers to an rgb value
public enum TColor {;

    // Uses Color.HSVToColor to convert HSV color to RGB color
    // Convenience method
    public static int HSVToColor(float hue, float saturation, float value) {
        float[] hsv = {hue, saturation, value};
        return Color.HSVToColor(hsv);
    }

    // Shifts the hue of a color according to HSV
    // Hue is a value from 0 to 360 on the wheel that goes red=>yellow=>green=>cyan=>blue=>magenta=>red=>...
    public static int hueShift(int color, int shift) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[0] += shift;
        if (hsv[0] > 360) hsv[0] -= 360;

        return Color.HSVToColor(hsv);
    }

    // Shifts the saturation of a color according to HSV
    // Saturation is a value form 0=white to 1=color
    public static int saturationShift(int color, float shift) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] += shift;
        if (hsv[1] > 1) hsv[1] = 1;
        if (hsv[1] < 0) hsv[1] = 0;

        return Color.HSVToColor(hsv);
    }

    // Shifts the value of a color according to HSV
    // Value is a value from 0=black to 1=color
    public static int valueShift(int color, float shift) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] += shift;
        if (hsv[2] > 1) hsv[2] = 1;
        if (hsv[2] < 0) hsv[2] = 0;

        return Color.HSVToColor(hsv);
    }

    // Scales the saturation of a color according to HSV
    // Parameter float scale should be a value -1 to 1
    // Saturation is a value from 0=white to 1=color
    public static int saturationScale(int color, float scale) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (scale > 0) {
            hsv[1] += scale * (1 - hsv[1]);
        }
        else {
            hsv[1] += scale * hsv[1];
        }
        if (hsv[1] > 1) hsv[1] = 1;
        if (hsv[1] < 0) hsv[1] = 0;

        return Color.HSVToColor(hsv);
    }

    // Scales the value of a color according to HSV
    // Parameter float scale should be a value -1 to 1
    // Saturation is a value from 0=white to 1=color
    public static int valueScale(int color, float scale) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (scale > 0) {
            hsv[2] += scale * (1 - hsv[2]);
        }
        else {
            hsv[2] += scale * hsv[2];
        }
        if (hsv[2] > 1) hsv[2] = 1;
        if (hsv[2] < 0) hsv[2] = 0;

        return Color.HSVToColor(hsv);
    }

    // Opposite HSV color
    public static int complementaryColor(int color) {
        return hueShift(color, 180);
    }

    // Makes an rgb color darker by a given percentage
    // percentage must be a value in the range [0,1.0f]
    public static int darkenColor(int color, float percentage) {
        int a, r, g, b;
        a = Color.alpha(color);
        r = Color.red(color);
        g = Color.green(color);
        b = Color.blue(color);

        percentage = 1 - percentage;
        r *= percentage;
        g *= percentage;
        b *= percentage;

        return Color.argb(a, r, g, b);
    }

    // Makes an rgb color brighter by a given percentage
    // percentage must be a value in the range [0,1.0f]
    public static int brightenColor(int color, float percentage) {
        int a, r, g, b;
        a = Color.alpha(color);
        r = Color.red(color);
        g = Color.green(color);
        b = Color.blue(color);

        r += (int)((0xff - r) * percentage);
        g += (int)((0xff - g) * percentage);
        b += (int)((0xff - b) * percentage);

        return Color.argb(a, r, g, b);
    }

    // Overloads
    public static int saturationShift(int color, double shift) {
        return saturationShift(color, (float)shift);
    }

    public static int valueShift(int color, double shift) {
        return valueShift(color, (float)shift);
    }

    public static int saturationScale(int color, double scale) {
        return saturationScale(color, (float)scale);
    }

    public static int valueScale(int color, double scale) {
        return valueScale(color, (float)scale);
    }

    public static int darkenColor(int color, double percentage) {
        return darkenColor(color, (float)percentage);
    }

    public static int brightenColor(int color, double percentage) {
        return brightenColor(color, (float)percentage);
    }

    public static int HSVToColor(double hue, double saturation, double value) {
        return HSVToColor((float)hue, (float)saturation, (float)value);
    }
}
