package hackman.trevor.tlibrary.library;

import android.util.Log;

import androidx.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/*
 * Three Functions
 * 1) Logging to logcat while testing on android device/emulator
 * 2) Logging with System.out.println() when testing with non-android environment
 * 3) Logging to Firebase when released on users' devices in the event of report: being called
 *
 * log:
 * logs to logcat with Log.e: only if TESTING
 * If log fails (non-android environment) uses System.out.println:
 *
 * flog:
 * If testing logs to logcat, else logs to Firebase with Crashlytics.log:
 * Firebase logs are only received if there is an fatal crash or report while not testing
 *
 * report:
 * If not testing, reports to Firebase with Crashlytics.logException:
 * If testing, just logs the report
 */

public enum TLogging {;
    public static final boolean TESTING = true; // TODO Make this false for release, keep true for testing
    private static int charTracker = 0; // For getTag:

    // synchronized to be thread-safe just in case
    private synchronized static String getTag() {
        // The TT_ is to make my logs filterable in the logcat by searching for TT, there's shit-loads of other logs from other sources that I'm largely not interested in
        return "TT_" + TMath.intToExcelColName(++charTracker);
    }

    // Logs to logcat, uses Log.ERROR by default
    public static void log(String string) {
        if (TESTING) {
            try {
                Log.e(getTag(), string);
            } catch (RuntimeException | NoClassDefFoundError e) { // Not ideal, but detecting non-android environment with this
                System.out.println(string);
            }
        }
    }

    public static void log(String[] strings) {
        StringBuilder s = new StringBuilder();
        for (String string : strings) s.append(string).append("\n");
        log(s.toString());
    }

    public static void log() {
        log(getTag());
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

    public static void log(float[] floatArray) {
        log(Arrays.toString(floatArray));
    }

    public static void log(@Nullable Object object) {
        if (object == null) log("null");
        else log(object.toString());
    }

    public static void log(Object[] objects) {
        log(Arrays.toString(objects));
    }

    // If testing logs to logcat, else logs to Firebase
    public static void flog(String string) {
        if (!TESTING) {
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

    // For when you catch an error and stop a crash from happening but still want to report it to firebase
    public static void report(Throwable e) {
        if (!TESTING) {
            flog(e.toString());
            Crashlytics.logException(e);
        }
        else {
            // Method for getting stacktrace as string
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw)); // Writes stacktrace to sw
            String exceptionAsString = sw.toString();

            log(exceptionAsString);
        }
    }

    public static void report(String string) {
        report(new Exception(string));
    }

    public static void report(Throwable e, String string) {
        flog(string);
        report(e);
    }
}
