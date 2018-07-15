package hackman.trevor.copycat;

import android.app.AlertDialog;
import android.content.Context;

public enum Dialogs {;
    public static void openNullIntentError(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.Unknown_Error)
                .setMessage(R.string.unknown_null_intent_data)
                .setNeutralButton(R.string.OK, null)
                .create()
                .show();
    }
}
