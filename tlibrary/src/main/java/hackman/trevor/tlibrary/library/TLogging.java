package hackman.trevor.tlibrary.library;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public final class TLogging {
    private TLogging() {} // Private constructor to stop instances of this class, everything is static so instances are pointless

    public static final boolean TESTING = true; // TODO Make this false for release, keep true for testing

    private static int charTracker = 0;
    private static String lastLog = "Default";
    private static boolean crashlyticsEnabled = true; // Gets disabled if (TESTING). When enabled, logcat logging is not allowed

    public static void log() {
        log(getTag());

    }

    // synchronized to be thread-safe just in case
    private synchronized static String getTag() {
        // The TT_ is to make my logs filterable in the logcat by searching for TT, there's shitloads of other logs from other sources that I'm largely not interested in
        return "TT_" + TMath.intToExcelColName(++charTracker);
    }

    // Logs to logcat, uses Log.ERROR by default
    public static void log(String string) {
        if (TESTING) Log.e(getTag(), string);
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

    public static void log(boolean[] boolArray) {
        log(Arrays.toString(boolArray));
    }

    public static void log(Object object) {
        log(object.toString());
    }

    // If testing logs to logcat, else logs to Firebase
    // Note: Firebase log is only recieved if there is an fatal crash or non-fatal exception
    public static void flog(String string) {
        lastLog = string;
        if (crashlyticsEnabled) {
            Crashlytics.log(string);
            // Crashlytics.log(ERROR, "TT_", string); // Uncomment this if I want logging in release mode
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
