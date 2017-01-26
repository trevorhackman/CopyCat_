package hackman.trevor.library;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public final class MyDialog {
    private MyDialog() {}

    // ID is of the form R.string... It is the proper way of hardcoding strings in the res/strings.xml
    public static void createAlertDialog(Context context, int titleID, int messageID, int positiveStringID, int negativeStringID
            , DialogInterface.OnClickListener positiveButtonListener, DialogInterface.OnClickListener negativeButtonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(titleID).setMessage(messageID)
                .setPositiveButton(positiveStringID, positiveButtonListener)
                .setNegativeButton(negativeStringID, negativeButtonListener)
                .create()
                .show();
    }

    // Empty negative button listener, by default dialog closes which is enough much of the time
    public static void createAlertDialog(Context context, int titleID, int messageID, int positiveStringID, int negativeStringID
            , DialogInterface.OnClickListener positiveButtonListener) {
        createAlertDialog(context, titleID, messageID, positiveStringID, negativeStringID, positiveButtonListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });
    }
}