package hackman.trevor.tlibrary.library;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import hackman.trevor.tlibrary.R;

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
            webLink.setData(Uri.parse("https://play.google.com/store/search?q=pub:Hackman&c=apps")); //&c=apps is necessary

            // Check that there's an activity that can handle a webLink (there should be for 99% of phones, but you never know)
            if (webLink.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(webLink);
            }
            else { // Should create an empty chooser with message "No apps can perform this action"
                context.startActivity(Intent.createChooser(webLink, null));
            }
        }
    }

    // If app is ever offered on other app stores, may need to modify
    public static void startRateGameIntent(Context context, String packageName) {
        Intent marketLink = new Intent(Intent.ACTION_VIEW);
        marketLink.setData(Uri.parse("market://details?id=" + packageName));

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

    public static void submitFeedbackIntent(Context context) {
        String email = "hackman.developer@gmail.com";
        String subject = context.getString(R.string.app_name) + " Feedback";
        // String body = "<Type Here>"; // I think this is unnecessary
        String chooserTitle = "Send Email";

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        // emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        context.startActivity(Intent.createChooser(emailIntent, chooserTitle));
    }
}
