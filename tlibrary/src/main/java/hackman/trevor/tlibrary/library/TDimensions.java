package hackman.trevor.tlibrary.library;

import android.content.Context;
import android.util.DisplayMetrics;

public enum TDimensions {;
    public static DisplayMetrics metrics;
    public static final int WP = 360; // Width Pixels
    public static final int HP = 640; // Height Pixels. 640 = 360 * 16/9 ; So WP=HP on 16:9 Portrait
    public static final int MD = 360; // Minimum Dimension Pixels. Equivalent to WP in Portrait.

    public static void setUp(Context context) {
        metrics = context.getResources().getDisplayMetrics();
    }

    /**
     * This method returns the number of pixels for the height of the device
     * @return An int value: The number of px height
     */
    public static int getHeightPixels() {
        return metrics.heightPixels;
    }

    /**
     * This method returns the number of pixels for the width of the device
     * @return An int value: The number of px width
     */
    public static int getWidthPixels() {
        return metrics.widthPixels;
    }

    /**
     * This method reports whether effective orientation is portrait (width <= height) or landscape
     * @return A boolean value: true if portrait, false if landscape
     */
    public static boolean isPortrait() {
        return metrics.widthPixels <= metrics.heightPixels;
    }

    /**
     * This method converts md unit to equivalent pixels, depending on minimum dimension
     *
     * @param md A value in md (custom minimum-dimension-dependent pixels) unit. 1md = 1/100 Minimum Dimension
     * md is the same length regardless of orientation (portrait vs landscape) unlike wp and hp\
     * @return A rounded int value to represent px equivalent to md depending on device height
     */
    public static int mdToPixels(float md) {
        int minDimension = Math.min(metrics.heightPixels, metrics.widthPixels);
        float px = md * minDimension / MD;
        return Math.round(px);
    }

    /**
     * This method converts wp unit to equivalent pixels, depending on width of device
     *
     * @param wp A value in wp (custom width-dependent pixels) unit. 1wp = 1/360 Screen Width
     * @return A rounded int value to represent px equivalent to wp depending on device width
     */
    public static int wpToPixel(float wp) {
        float px = wp * metrics.widthPixels / WP;
        return Math.round(px);
    }

    /**
     * This method converts hp unit to equivalent pixels, depending on height of device
     *
     * @param hp A value in hp (custom height-dependent pixels) unit. 1hp = 1/640 Screen Height
     * hp is equivalent to wp on 16:9 aspect ratio displays\
     * @return A rounded int value to represent px equivalent to hp depending on device height
     */
    public static int hpToPixel(float hp) {
        float px = hp * metrics.heightPixels / HP;
        return Math.round(px);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A rounded int value to represent px equivalent to dp depending on device density
     */
    public static int dpToPixel(float dp) {
        float px = dp * (metrics.xdpi / (float)metrics.densityDpi);
        return Math.round(px);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public static float pixelsToDp(float px){
        float dp = px / (metrics.xdpi / (float)metrics.densityDpi);
        return dp;
    }

    // Overloads
    public static int mdToPixels(double md) { return mdToPixels((float)md); }
    public static int wpToPixel(double wp) { return wpToPixel((float)wp); }
    public static int hpToPixel(double hp) { return hpToPixel((float)hp); }
    public static int dpToPixel(double dp) { return dpToPixel((float)dp); }
    public static float pixelsToDp(double px){ return pixelsToDp((float)px); }
}
