package hackman.trevor.tlibrary.library;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.Window;

// Class for various tidbits, if enough tidbits of any common type accumulate I will agglomerate them into a new library class
public enum TMiscellaneous {;

    // This will need to be accordingly adjusted if I ever change my developer name
    public static void startMoreGamesIntent(Context context) {
        Intent marketLink = new Intent(Intent.ACTION_VIEW);
        marketLink.setData(Uri.parse("market://search?q=pub:Hackman"));

        // Market link will fail if Google Play Store is not installed
        if (marketLink.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(marketLink);
        }
        else {
            Intent webLink = new Intent(Intent.ACTION_VIEW);
            webLink.setData(Uri.parse("https://play.google.com/store/search?q=pub:Hackman"));

            // Check that there's an activity that can handle a webLink (there should be for 99% of phones, but you never know)
            if (webLink.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(webLink);
            }
            else { // Should create an empty chooser with message "No apps can perform this action"
                context.startActivity(Intent.createChooser(webLink, null));
            }
        }
    }

    public static void startRateGameIntent(Context context, String packageName) {
        Intent marketLink = new Intent(Intent.ACTION_VIEW);
        marketLink.setData(Uri.parse("market://details?id=" + packageName)); // package name is hackman.trevor.copycat

        // Market link will fail if Google Play Store is not installed
        if (marketLink.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(marketLink);
        }
        else {
            Intent webLink = new Intent(Intent.ACTION_VIEW);
            webLink.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));

            // Check that there's an activity that can handle a webLink (there should be for 99% of phones, but you never know)
            if (webLink.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(webLink);
            }
            else { // Should create an empty chooser with message "No apps can perform this action"
                context.startActivity(Intent.createChooser(webLink, null));
            }
        }
    }

    // To call from activity (onResume and onWindowFocusChanged), getWindow() for parameter, making activity fullscreen
    // OUTDATED - Add line "<item name="android:windowFullscreen">true</item>' to @style/AppTheme for easier, better solution
    // Still useful if I want to add a setting for fullscreen vs not
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void goFullScreen(Window window) {
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    // Create a ripple drawable for buttons - adds ripple effect onPress and onClick to a normal drawable
    // Ripple drawables can wrap around other kinds of drawables
    // @param background is not altered and is the appearance of the resulting RippleDrawable
    // @param pressedColor is the color of the ripple effect
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static RippleDrawable createRippleDrawable(Drawable background, int pressedColor) {
        ColorStateList csl = new ColorStateList(
                new int[][]
                        {
                                new int[]{}
                        },
                new int[]
                        {
                                pressedColor
                        }
        );
        return new RippleDrawable(csl, background, null);
    }
}
