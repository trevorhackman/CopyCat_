package hackman.trevor.tlibrary.library;

import android.graphics.Color;

import static hackman.trevor.tlibrary.library.TLogging.report;

public class TMath {

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
