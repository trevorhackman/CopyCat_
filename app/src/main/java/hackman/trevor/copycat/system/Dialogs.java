package hackman.trevor.copycat.system;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import hackman.trevor.copycat.MainActivity;
import hackman.trevor.copycat.R;

import static hackman.trevor.tlibrary.library.TLogging.report;
import static hackman.trevor.tlibrary.library.TMiscellaneous.startMoreGamesIntent;
import static hackman.trevor.tlibrary.library.TMiscellaneous.startRateGameIntent;

public enum Dialogs {;
    public static void leaveCurrentGame(Context context, final MainActivity main) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.exit_title)
                .setMessage(R.string.exit_message)
                .setPositiveButton(R.string.Exit_App, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        main.superOnBackPress();
                    }
                })
                .setNeutralButton(R.string.Menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        main.gameScreen().exitGameToMainMenu();
                    }
                })
                .setNegativeButton(R.string.Cancel, null)
                .create()
                .show();
    }

    public static void viewMoreGames(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.more_games_title)
                .setMessage(R.string.more_games_message)
                .setPositiveButton(R.string.View, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startMoreGamesIntent(context);
                    }
                })
                .setNegativeButton(R.string.Cancel, null)
                .create()
                .show();
    }

    public static void rateTheApp(final MainActivity main) {
        AlertDialog.Builder builder = new AlertDialog.Builder(main, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.rate_the_app_title)
                .setMessage(R.string.rate_the_app_message)
                .setPositiveButton(R.string.Rate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRateGameIntent(main, main.getPackageName());
                    }
                })
                .setNegativeButton(R.string.Cancel, null)
                .create()
                .show();
    }

    public static void purchaseMenu(final MainActivity main) {
        AlertDialog.Builder builder = new AlertDialog.Builder(main, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.no_ads_title)
                .setMessage(R.string.no_ads_message)
                .setPositiveButton(R.string.Purchase, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Billing.purchaseFlow(main);
                    }
                })
                .setNegativeButton(R.string.Cancel, null)
                .create()
                .show();
    }

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

    public static void videoAdNotLoaded(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.video_not_loaded_title).setMessage(R.string.video_not_loaded_message)
                .setNeutralButton(R.string.OK, null)
                .create()
                .show();
    }

    public static void unlockGameModes(final MainActivity main, final Runnable onReward) {
        AlertDialog.Builder builder = new AlertDialog.Builder(main, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.unlock_modes_title)
                .setMessage(R.string.unlock_modes_message)
                .setPositiveButton(R.string.Watch, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If we don't remember, play an ad
                        // In exceptional circumstances someone that already payed may not be remembered (data cleared & yet to make successful query to remember), but oh well
                        if (main.tPreferences().getBoolean(Keys.isNoAdsOwned, false)) {
                            boolean loaded = Ads.playRewardedVideoAd(onReward);
                            if (!loaded) {
                                videoAdNotLoaded(main);
                            }
                        }
                        // else OWNED
                        else {
                            report("Shouldn't happen : no_ads owned when we don't remember it being owned : 177");
                            main.tPreferences().putBoolean(Keys.isNoAdsOwned, true);
                        }
                    }
                })
                .setNegativeButton(R.string.Cancel, null)
                .create()
                .show();
    }

    public static void failedNetwork(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.failed_title)
                .setMessage(R.string.failed_message)
                .setNeutralButton(R.string.OK, null)
                .create()
                .show();
    }

    public static void unknownError(Context context, String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.unknown_title)
                .setMessage(errorMessage)
                .setNeutralButton(R.string.OK, null)
                .create()
                .show();
    }
}
