package hackman.trevor.copycat;

import android.app.AlertDialog;
import android.content.Context;

public enum Dialogs {;
    public static void successfulNoAdsPurchase(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.Purchase_Successful)
                .setMessage(R.string.purchase_success_message)
                .setNeutralButton(R.string.OK, null)
                .create()
                .show();
    }

    public static void noAdsAlreadyPurchased(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.Already_Purchased)
                .setMessage(R.string.already_purchased_message)
                .setNeutralButton(R.string.OK, null)
                .create()
                .show();
    }

    public static void nullIntentError(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.Unknown_Error)
                .setMessage(R.string.unknown_null_intent_data)
                .setNeutralButton(R.string.OK, null)
                .create()
                .show();
    }

    public static void nullmServiceError(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.Error).setMessage(R.string.null_mService_error_message)
                .setNeutralButton(R.string.OK, null)
                .create()
                .show();
    }

    public static void nullPendingIntentError(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.Error).setMessage(R.string.null_pending_intent_error_message)
                .setNeutralButton(R.string.OK, null)
                .create()
                .show();
    }

    public static void remoteException(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.Unknown_Error)
                .setMessage(R.string.unknown_remote_exception)
                .setNeutralButton(R.string.OK, null)
                .create()
                .show();
    }

    public static void nullIntentSenderError(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.Unknown_Error)
                .setNeutralButton(R.string.OK, null)
                .setMessage(R.string.unknown_null_intent_sender)
                .create()
                .show();
    }
}
