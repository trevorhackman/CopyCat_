package hackman.trevor.library;

import static hackman.trevor.library.Logging.report;

public class MyMath {

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
}
