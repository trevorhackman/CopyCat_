package hackman.trevor.copycat.standard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import hackman.trevor.copycat.MainActivity;
import hackman.trevor.copycat.R;

import static hackman.trevor.copycat.MainActivity.noAdsStatus.NOT_OWNED;
import static hackman.trevor.copycat.MainActivity.noAdsStatus.OWNED;
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
                        main.gameScreen.exitGameToMainMenu();
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
                        main.purchaseFlow();
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
                        if (main.noAdsStatusResult == NOT_OWNED) {
                            boolean loaded = Ads.playRewardedVideoAd(onReward);
                            if (!loaded) {
                                videoAdNotLoaded(main);
                            }
                        }
                        else if (main.noAdsStatusResult != OWNED) {
                            main.noAdsStatusResult = main.isNoAdsPurchased(); // Query again
                            // Attempt to play video ad anyways
                            // Don't want to deny game modes to people who can't connect to Google Billing (for example if Google Play Store is disabled or not installed) to check no_ads purchase
                            // This does mean in very exceptional cases someone that paid to remove ads might have to watch an ad
                            boolean loaded = Ads.playRewardedVideoAd(onReward);
                            if (!loaded) {
                                videoAdNotLoaded(main);
                            }
                        }
                        // else OWNED
                        else {
                            report("Shouldn't happen : no_ads owned when we don't remember it being owned : 177");
                            main.tPreferences.putBoolean("noAdsOwned", true);
                        }
                    }
                })
                .setNegativeButton(R.string.Cancel, null)
                .create()
                .show();
    }
}