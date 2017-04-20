package hackman.trevor.library;

import android.graphics.Color;

import static hackman.trevor.library.Logging.report;

public class MyMath {

    // Turns an int into its corresponding 'excel column' form. That is 1, 2, 3... into A, B, C, ... Z, AA, AB, AC, ... AZ, BA, BB, ... ZZ, AAA, ...
    // Only takes integers that are 1 or greater
    public static String intToExcelColName(int integer) {
        // Should never happen, unless int.max overflow?
        if (integer < 1) {
            report("Invalid argument, integer can't be less than 1");
            return "ERROR";
        }

        String result = "";
        int modulo;

        while (integer > 0) {
            modulo = integer % 26;
            if (modulo == 0) {
                modulo = 26;
            }
            result = (char)('@' + modulo) + result; // '@' is character before 'A'
            integer = (integer - 1) / 26;
        }

        return result;
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
}
