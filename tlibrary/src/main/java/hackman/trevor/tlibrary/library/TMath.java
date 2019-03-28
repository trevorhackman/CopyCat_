package hackman.trevor.tlibrary.library;

import static hackman.trevor.tlibrary.library.TLogging.report;

// An instance-less enum is a quick workaround to making the class both final and abstract,
// both non-inheritable and non-instantiable, ie. non-object-oriented,
// an illegal keyword combination in object-oriented java.
// Alternative is to make the class final with a private constructor that throws an error if called
public enum TMath {;

    public static final double BILLION = 1000000000.0;

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
