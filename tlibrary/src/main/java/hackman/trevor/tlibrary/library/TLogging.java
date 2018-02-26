package hackman.trevor.tlibrary.library;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

public final class TLogging {
    private TLogging() {} // Private constructor to stop instances of this class, everything is static so instances are pointless

    private static int charTracker = 0;
    private static String lastLog = "Default";
    private final static boolean logCatLoggingEnabled = true;

    public static void log() {
        log(TMath.intToExcelColName(++charTracker));
    }

    private final static int ASSERT = 0;
    private final static int ERROR = 1;
    private final static int WARN = 2;
    private final static int INFO = 3;
    private final static int DEBUG = 4;
    private final static int VERBOSE = 5;
    private static void log(String string, final int PRIORITY) {
        if (logCatLoggingEnabled) {
            lastLog = string;
            switch (PRIORITY) {
                case (ASSERT):
                    Log.wtf(TMath.intToExcelColName(++charTracker), string);
                    break;
                case (ERROR):
                    Log.e(TMath.intToExcelColName(++charTracker), string);
                    break;
                case (WARN):
                    Log.w(TMath.intToExcelColName(++charTracker), string);
                    break;
                case (INFO):
                    Log.i(TMath.intToExcelColName(++charTracker), string);
                    break;
                case (DEBUG):
                    Log.d(TMath.intToExcelColName(++charTracker), string);
                    break;
                case VERBOSE:
                    Log.v(TMath.intToExcelColName(++charTracker), string);
                    break;
            }
        }
    }

    // Logs to logcat, uses Log.ERROR by default
    public static void log(String string) {
        log(string, ERROR);
    }

    public static void log(int integer) {
        log("" + integer);
    }

    public static void log(double d) {
        log("" + d);
    }

    public static void log(boolean bool) {
        log("" + bool);
    }

    public static void logw(String string) {
        log(string, WARN);
    }

    public static void logi(String string) {
        log(string, INFO);
    }

    public static void logd(String string) {
        log(string, DEBUG);
    }

    public static void logv(String string) {
        log(string, VERBOSE);
    }

    // Logs to logcat and to firebase
    // Note: Firebase log is only recieved if there is an unhandled exception or report
    public static void flog(String string) {
        lastLog = string;
        try {
            FirebaseCrash.logcat(Log.ERROR, TMath.intToExcelColName(++charTracker), string);
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

    // For when you catch an error and stop a crash from happening but still want to report it to firebase
    public static void report(Exception exception) {
        FirebaseCrash.report(exception);
    }

    public static void report (String string) {
        flog(string);
        report(new Exception(string));
    }

    public static void report() { report(lastLog); }
}
