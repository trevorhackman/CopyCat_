package hackman.trevor.tlibrary.library;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;

import static hackman.trevor.tlibrary.library.TLogging.log;
import static hackman.trevor.tlibrary.library.TLogging.report;

// An instance-less enum is a quick workaround to making the class both final and abstract,
// both non-inheritable and non-instantiable, ie. non-object-oriented,
// an illegal keyword combination in object-oriented java.
// Alternative is to make the class final with a private constructor that throws an error if called
public enum TMath {;

    /**
     * This method converts md unit to equivalent pixels, depending on minimum dimension
     *
     * @param md A value in md (custom minimum-dimension-dependent pixels) unit. 1md = 1/360 Minimum Dimension
     * md is equivalent to wp on portrait landscape
     * @param context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to md depending on device height
     */
    public static float convertMdToPixel(float md, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int minDimension = Math.min(metrics.heightPixels, metrics.widthPixels);
        float px = md * minDimension / 360;
        return px;
    }

    /**
     * This method converts hp unit to equivalent pixels, depending on height of device
     *
     * @param hp A value in hp (custom height-dependent pixels) unit. 1hp = 1/640 Screen Height
     * hp is equivalent to wp on 16:9 aspect ratio displays
     * @param context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to hp depending on device height
     */
    public static float convertHpToPixel(float hp, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = hp * metrics.heightPixels / 640;
        return px;
    }

    /**
     * This method converts wp unit to equivalent pixels, depending on width of device
     *
     * @param wp A value in wp (custom width-dependent pixels) unit. 1wp = 1/360 Screen Width
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to wp depending on device width
     */
    public static float convertWpToPixel(float wp, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = wp * metrics.widthPixels / 360;
        return px;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = dp * (metrics.xdpi / (float)metrics.densityDpi);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float dp = px / (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    // Turns an int into its corresponding 'excel column' form. That is 1, 2, 3... into A, B, C, ... Z, AA, AB, AC, ... AZ, BA, BB, ... ZZ, AAA, ...
    // Only takes integers that are 1 or greater
    public static String intToExcelColName(int integer) {
        // Should never happen, unless int.max overflow?
        if (integer < 1) {
            report("Invalid argument, integer can't be less than 1");
            return "ERROR";
        }

        StringBuilder result = new StringBuilder();
        int modulo;

        while (integer > 0) {
            modulo = integer % 26;
            if (modulo == 0) {
                modulo = 26;
            }
            result.insert(0, (char) ('@' + modulo)); // '@' is character before 'A'
            integer = (integer - 1) / 26;
        }

        return result.toString();
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

    // Opposite HSV color
    public static int complementaryColor(int color) {
        return hueShift(color, 180);
    }

    // Makes a color (int) darker by a given percentage
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

    // Makes a color (int) brighter by a given percentage
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

    public static int roundDouble (double d) {
        if (d < ((int)d) + 0.5) return (int)d;
        return ((int)d) + 1;
    }

    public static double makeAngle0To360(double angle) {
        if (angle > 360) return angle - 360 * ((int)(angle / 360));
        if (angle < 0) return angle + 360 * ((int)(angle / -360) + 1);
        return angle;
    }

    // Calculates sin with angle input
    public static double sin(double angle) {
        // angle *= Math.PI / 180;
        return Math.sin(angle * Math.PI / 180);
    }

    // Calculates cos with angle input
    public static double cos(double angle) {
        // angle *= Math.PI / 180;
        return Math.cos(angle * Math.PI / 180);
    }

    // Calculates tan with angle input
    public static double tan(double angle) {
        // angle *= Math.PI / 180;
        return Math.tan(angle * Math.PI / 180);
    }

    // Returns arcsin in terms of angle
    // Note that arcsin's output is limited in range to the closed interval [-90,90]
    public static double arcsin(double ratio) {
        return Math.asin(ratio) * 180 / Math.PI;
    }

    // Returns arccos in terms of angle
    // Note that arccos's output is limited in range to the closed interval [-90,90]
    public static double arccos(double ratio) {
        return Math.acos(ratio) * 180 / Math.PI;
    }

    // Returns arctan in terms of angle
    // Note that arctan's output is limited in range to the open interval (-90,90)
    public static double arctan(double ratio) {
        return Math.atan(ratio) * 180 / Math.PI;
    }

    // Linear Interpolation or lerp for short
    public static float lerp(float previous, float current, float interpolation) {
        return previous + interpolation * (current - previous);
    }
}
