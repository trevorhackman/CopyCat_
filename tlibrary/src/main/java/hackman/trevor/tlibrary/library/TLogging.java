package hackman.trevor.tlibrary.library;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.PrintWriter;
import java.io.StringWriter;

import static android.util.Log.ASSERT;
import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static android.util.Log.WARN;

public final class TLogging {
    private TLogging() {} // Private constructor to stop instances of this class, everything is static so instances are pointless

    public static final boolean TESTING = false; // TODO Make this false for release, keep true for testing
    private static int charTracker = 0;
    private static String lastLog = "Default";
    private static boolean crashlyticsEnabled = true;

    public static void log() {
        log(getTag());
    }

    private enum Priority {ASSERT, ERROR, WARN, INFO, DEBUG, VERBOSE}
    private static void log(String string, final int PRIORITY) {
        if (TESTING) {
            lastLog = string;
            switch (PRIORITY) {
                case ASSERT:
                    Log.wtf(getTag(), string);
                    break;
                case ERROR:
                    Log.e(getTag(), string);
                    break;
                case WARN:
                    Log.w(getTag(), string);
                    break;
                case INFO:
                    Log.i(getTag(), string);
                    break;
                case DEBUG:
                    Log.d(getTag(), string);
                    break;
                case VERBOSE:
                    Log.v(getTag(), string);
                    break;
            }
        }
    }

    private static String getTag() {
        // The TT_ is to make my logs filterable in the logcat by searching for TT, there's shitloads of other logs from other sources that I'm largely not interested in
        return "TT_" + TMath.intToExcelColName(++charTracker);
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

    // If testing logs to logcat, else logs to Firebase
    // Note: Firebase log is only recieved if there is an fatal crash or non-fatal exception
    public static void flog(String string) {
        lastLog = string;
        if (crashlyticsEnabled) {
            Crashlytics.log(string);
        }
        else log(string);
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

    public static void report() { report(lastLog); }

    // For when you catch an error and stop a crash from happening but still want to report it to firebase
    public static void report(Throwable e) {
        if (crashlyticsEnabled) {
            flog(e.toString());
            Crashlytics.logException(e);
        }
        else {
            // Method for getting stacktrace as string
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();

            log(exceptionAsString);
        }
    }

    public static void report(String string) {
        flog(string);
        report(new Exception(string));
    }

    public static void report(Throwable e, String string) {
        flog(string);
        report(e);
    }

    public static void disableCrashlytics() {
        crashlyticsEnabled = false;
        log("Crashlytics disabled");
    }
}
