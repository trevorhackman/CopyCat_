package hackman.trevor.library;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

import static hackman.trevor.library.MyMath.intToExcelColName;

public final class Logging {
    private Logging() {} // Private constructor to stop instances of this class, everything is static so instances are pointless

    private static int charTracker = 0;
    private static String lastLog = "Default";

    public static void log() {
        log(intToExcelColName(++charTracker));
    }

    // Logs to logcat, I just use Log.ERROR as my default. I don't currently see a need for other message levels
    public static void log(String string) {
        lastLog = string;
        Log.e(intToExcelColName(++charTracker), string);
    }

    public static void log(int integer) {
        log("" + integer);
    }

    public static void log(double d) {
        log("" + d);
    }

    public static void log(boolean bool) {
        if (bool) log("True");
        else log("False");
    }

    // Logs to logcat and to firebase
    // Note: Firebase log is only recieved if there is an unhandled exception or report
    public static void flog(String string) {
        lastLog = string;
        try {
            FirebaseCrash.logcat(Log.ERROR, intToExcelColName(++charTracker), string);
        } catch (NoClassDefFoundError e) {
            log("Weird NoClassDefFoundError on FirebaseCrash logging - enable MultiDex in manifest and gradle.app to fix");
        }
    }

    public static void flog(int integer) {
        flog("" + integer);
    }

    public static void flog(double d) {
        flog("" + d);
    }

    public static void flog(boolean bool) {
        if (bool) flog("True");
        else flog("False");
    }

    public static void report(Exception exception) {
        FirebaseCrash.report(exception);
    }

    public static void report (String string) {
        flog(string);
        report(new Exception(string));
    }

    public static void report() { report(lastLog); }
}
