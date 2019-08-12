package hackman.trevor.copycat.ui;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdView;

import hackman.trevor.copycat.MainActivity;
import hackman.trevor.copycat.R;
import hackman.trevor.copycat.logic.Game;
import hackman.trevor.copycat.system.Ads;
import hackman.trevor.copycat.system.AndroidSound;
import hackman.trevor.copycat.system.Dialogs;
import hackman.trevor.copycat.system.Keys;
import hackman.trevor.copycat.system.drawables.Drawables;
import hackman.trevor.tlibrary.library.TColor;
import hackman.trevor.tlibrary.library.TDimensions;
import hackman.trevor.tlibrary.library.ui.GoodButton;
import hackman.trevor.tlibrary.library.ui.GoodTextView;
import hackman.trevor.tlibrary.library.ui.Llp;
import hackman.trevor.tlibrary.library.ui.Rlp;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER;
import static hackman.trevor.tlibrary.library.TLogging.flog;
import static hackman.trevor.tlibrary.library.TLogging.report;
import static hackman.trevor.tlibrary.library.ui.Rlp.MATCH;
import static hackman.trevor.tlibrary.library.ui.Rlp.WRAP;

public class GameScreen {
    private MainActivity main;
    public GameScreen(MainActivity main) {
        this.main = main;
    }

    // Layouts for landscape orientation and small phones
    private LinearLayout leftButtonColumn;
    private LinearLayout rightButtonColumn;

    // Variables
    public boolean allowColorInput; // Close input while sequence is playing, open when it's the player's turn to repeat
    public boolean inGame; // Keeps track of whether in game or not
    private boolean mainButtonEnabled; // Enables and disables main button, effectively tracks when game is allowed to start
    public boolean startGameAfterFadeOut; // If true, game will start after the death-screen finishes fading away. Set on playAgainButton click
    public boolean startNextSequence; // False until sequence is finished, true to continue to next level/sequence
    public boolean popInRan; // Title pop-in plays in onResume once per creation
    public long milliSecondsToLight; // The length of time a color is played (visual and sound) for during a sequence
    public long milliSecondsDelay; // Delay between lights
    public int level; // What level the user is on

    // Layouts
    private RelativeLayout root;
    public DeathMenu deathMenu;
    public SettingsMenu settingsMenu;
    public ModesMenu modesMenu;
    public LinearLayout buttonBar;

    // Button bar
    public Button moreGamesButton;
    public Button noAdsButton;
    public Button starButton;
    public Button settingsButton;
    public Button[] buttonBarArray;

    // Color buttons
    public ColorButton[] colorButtons = new ColorButton[4];
    public ColorButton greenButton;
    public ColorButton redButton;
    public ColorButton yellowButton;
    public ColorButton blueButton;

    public PlaySymbol playSymbol; // Inside the main button, displays play symbol
    public MainButton mainButton; // The circle, click on it to play, displays level number

    public Title title;
    private ImageView top_fade;
    private ImageView bottom_fade;
    public Instructions popUpInstructions;
    private AdView bannerAd;

    // Game modes
    private GoodButton modesButton; // Game modes button
    private GoodTextView modeDisplay; // Displays what mode is selected on game start

    // Animator Listeners
    public Runnable fadeInButtonBarListener;
    public Runnable fadeOutButtonBarListener;
    public static final int mainFadeDuration = 1000; // main animation duration in milliseconds

    // The logical game
    public Game game;

    public final static int GREEN = 0; // Highest
    public final static int RED = 1; // 2nd Highest
    public final static int YELLOW = 2; // 3rd Highest
    public final static int BLUE = 3; // 4th Highest

    private ColorButton getButton(int which) {
        switch (which) {
            case GREEN:
                return greenButton;
            case RED:
                return redButton;
            case YELLOW:
                return yellowButton;
            case BLUE:
                return blueButton;
        }
        throw new IllegalArgumentException("No such color: 135");
    }

    public void initialize() {
        // Initialize variables
        allowColorInput = true;
        inGame = false;
        mainButtonEnabled = true;
        level = 1;
        startNextSequence = false;
        popInRan = false;

        // Initialize Death Menu
        deathMenu = main.findViewById(R.id.deathScreen);
        deathMenu.setUp(main);

        // Initialize Settings Menu
        settingsMenu = main.findViewById(R.id.settingsScreen);
        settingsMenu.setUp(main);

        // Initialize Modes Menu
        modesMenu = new ModesMenu(main);

        // Initialize Title
        title = main.findViewById(R.id.title_logo);
        top_fade = main.findViewById(R.id.top_fade);
        bottom_fade = main.findViewById(R.id.bottom_fade);

        // Initialize buttons
        mainButton = main.findViewById(R.id.mainButton);
        buttonBar = main.findViewById(R.id.button_bar);
        settingsButton = main.findViewById(R.id.settings);
        starButton = main.findViewById(R.id.star);
        noAdsButton = main.findViewById(R.id.noAds);
        moreGamesButton = main.findViewById(R.id.moreGames);
        buttonBarArray = new Button[4];
        buttonBarArray[0] = moreGamesButton;
        buttonBarArray[1] = noAdsButton;
        buttonBarArray[2] = starButton;
        buttonBarArray[3] = settingsButton;

        greenButton = main.findViewById(R.id.greenButton);
        redButton = main.findViewById(R.id.redButton);
        yellowButton = main.findViewById(R.id.yellowButton);
        blueButton = main.findViewById(R.id.blueButton);
        colorButtons[0] = greenButton;
        colorButtons[1] = redButton;
        colorButtons[2] = yellowButton;
        colorButtons[3] = blueButton;

        // Setup color buttons
        greenButton.setUp(AndroidSound.chip1, 0);
        redButton.setUp(AndroidSound.chip2, 1);
        yellowButton.setUp(AndroidSound.chip3, 2);
        blueButton.setUp(AndroidSound.chip4, 3);
        setButton(greenButton);
        setButton(redButton);
        setButton(yellowButton);
        setButton(blueButton);

        // Setup buttons
        setMainButton();
        setSettingsButton();
        setStarButton();
        setNoAdsButton();
        setMoreGamesButton();

        // Initialize settings
        settingsMenu.getSettings();

        // Initialize animation listeners
        fadeInButtonBarListener = new Runnable() {
            @Override
            public void run() {
                moreGamesButton.setClickable(true);
                noAdsButton.setClickable(true);
                starButton.setClickable(true);
                settingsButton.setClickable(true);
                modesButton.setClickable(true);
            }
        };
        fadeOutButtonBarListener = new Runnable() {
            @Override
            public void run() {
                moreGamesButton.setClickable(false);
                noAdsButton.setClickable(false);
                starButton.setClickable(false);
                settingsButton.setClickable(false);
                modesButton.setClickable(false);
            }
        };

        // Game modes button
        modesButton = new GoodButton(main, Drawables.modesButton(40, 20)); // 40, 20 placeholders
        modesButton.setId(R.id.gameModesButton);
        modesButton.setText(main.getString(R.string.Game_Modes));
        modesButton.setTextSize(18); // 18 placeholder
        modesButton.setTextColor(Color.WHITE);
        setGameModesButton();

        Rlp modesLP = new Rlp();
        modesLP.centerHorizontal();
        modesLP.addRule(RelativeLayout.ABOVE, R.id.button_bar);
        modesButton.setLayoutParams(modesLP);

        // Get root, arbitrarily picked deathMenu to get it from
        root = (RelativeLayout)deathMenu.getParent();

        // Add game modes button
        root.addView(modesButton);

        // Instructions
        popUpInstructions = new Instructions(main);
        Rlp instructionsLp = new Rlp();
        instructionsLp.centerHorizontal();
        instructionsLp.addRule(RelativeLayout.ABOVE, R.id.gameModesButton);
        instructionsLp.bottomMargin = TDimensions.dpToPixel(2);
        popUpInstructions.setLayoutParams(instructionsLp);

        // Alternate center layout
        alternativePlayLayout = new LinearLayout(main);
        alternativePlayLayout.setOrientation(LinearLayout.VERTICAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alternativePlayLayout.setTranslationZ(99);
        }

        Rlp playSymbolLp = new Rlp();
        playSymbolLp.center();
        alternativePlayLayout.setLayoutParams(playSymbolLp);

        altModeText = new GoodTextView(main);
        altModeText.enableBold();
        altModeText.setAllCaps(true);
        Llp altTextLp = new Llp();
        altTextLp.gravity = Gravity.CENTER;
        altModeText.setLayoutParams(altTextLp);

        // Play Symbol Button
        playSymbol = new PlaySymbol(main);
        playSymbolSetUp();

        // Animate play symbol
        playSymbol.gyrate();

        // Landscape layouts
        leftButtonColumn = new LinearLayout(main);
        rightButtonColumn = new LinearLayout(main);
        leftButtonColumn.setOrientation(LinearLayout.VERTICAL);
        rightButtonColumn.setOrientation(LinearLayout.VERTICAL);

        Rlp leftLP = new Rlp(Rlp.WRAP, Rlp.MATCH);
        Rlp rightLP = new Rlp(Rlp.WRAP, Rlp.MATCH);
        leftLP.left();
        rightLP.right();

        leftButtonColumn.setGravity(Gravity.BOTTOM);
        rightButtonColumn.setGravity(Gravity.BOTTOM);
        leftButtonColumn.setLayoutParams(leftLP);
        rightButtonColumn.setLayoutParams(rightLP);

        root.addView(leftButtonColumn);
        root.addView(rightButtonColumn);
        root.addView(modesMenu, 0); // Add to back
        root.addView(popUpInstructions);

        // Conditionally initialize banner ad
        if (!main.tPreferences().getBoolean(Keys.isNoAdsOwned, false)) {
            bannerAd = Ads.getBannerAd();
            bannerAd.setLayoutParams(new Rlp(MATCH, WRAP).bottom());
            bannerAd.setBackgroundColor(TColor.Transparent);
        }
    }

    public void startGame() {
        game = new Game(Game.GameMode.valueOf(main.tPreferences().getString(Keys.gameMode, Game.GameMode.Classic.name())));
        mainButtonEnabled = false;
        allowColorInput = false;
        inGame = true;

        String level_text = "" + level;
        mainButton.setText(level_text);
        popUpInstructions.fadeIn();

        // Keep screen from rotating during game to prevent confusion
        // Multi-screen orientation changes ignore this setting, but I guess that's okay
        // Doesn't work VERSION < 18, but I guess that's okay too
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            main.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }

        for (ColorButton c: colorButtons) {
            c.returnToNormal();
        }

        flog("Game started");
        startSequence();
    }

    public void startSequence() {
        startNextSequence = false;
        allowColorInput = false;
        String level_text = "" + level;
        mainButton.setText(level_text);

        // Note, the 1 new value added is kept and retained, growing the sequence naturally per the rules of the game
        main.handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                main.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playColor(getButton(game.getGameMode().playBack()));
                    }
                });
            }
        }, (int) (milliSecondsDelay * 8 + milliSecondsToLight)); // Weird balancing I've found to like across speed settings
    }

    private void playColor(final ColorButton button) {
        button.press();

        // Wait a bit then return button to normal
        main.handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                button.returnToNormal();

                // Checks to see if there's more colors to play
                if (!game.getGameMode().checkAllowInput()) {
                    // After delay play next color
                    main.handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            main.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    playColor(getButton(game.getGameMode().playBack()));
                                }
                            });
                        }
                    }, milliSecondsDelay);
                }
                // else sequence is finished
                else {
                    allowColorInput = true;
                }
            }
        }, milliSecondsToLight);
    }

    // Fade in buttons and title
    public void mainFadeInAnimation() {
        buttonBar.animate().alpha(1.0f).setDuration(mainFadeDuration).withStartAction(fadeInButtonBarListener);
        leftButtonColumn.animate().alpha(1.0f).setDuration(mainFadeDuration);
        rightButtonColumn.animate().alpha(1.0f).setDuration(mainFadeDuration);
        top_fade.animate().alpha(1.0f).setDuration(mainFadeDuration);
        bottom_fade.animate().alpha(1.0f).setDuration(mainFadeDuration);
        modesButton.animate().alpha(1.0f).setDuration(mainFadeDuration);
        playSymbol.returnToNormal(); // Return playSymbol to normal
        mainButton.unshrink();
        title.fadeIn();

        if (alternativePlayLayout.getParent() != null) {
            alternativePlayLayout.animate().alpha(1.0f).setDuration(mainFadeDuration);
        }
    }

    // Fade out buttons and title
    private void mainFadeOutAnimation() {
        buttonBar.animate().alpha(0.0f).setDuration(mainFadeDuration).withStartAction(fadeOutButtonBarListener);
        leftButtonColumn.animate().alpha(0.0f).setDuration(mainFadeDuration);
        rightButtonColumn.animate().alpha(0.0f).setDuration(mainFadeDuration);
        top_fade.animate().alpha(0.0f).setDuration(mainFadeDuration);
        bottom_fade.animate().alpha(0.0f).setDuration(mainFadeDuration);
        modesButton.animate().alpha(0.0f).setDuration(mainFadeDuration);
        playSymbol.fadeOut();
        title.fadeOut();
        mainButton.shrink();

        if (alternativePlayLayout.getParent() != null) {
            alternativePlayLayout.animate().alpha(0.0f).setDuration(mainFadeDuration);
        }
    }

    // Makes the main-screen buttons fade out when the modes menu is opened
    // These buttons already get disabled
    final int forMenuOutDuration = 500;
    final int forMenuInDuration = 900;
    public void forMenuFadeOut() {
        buttonBar.animate().alpha(0.0f).setDuration(forMenuOutDuration);
        leftButtonColumn.animate().alpha(0.0f).setDuration(forMenuOutDuration);
        rightButtonColumn.animate().alpha(0.0f).setDuration(forMenuOutDuration);
        modesButton.animate().alpha(0.0f).setDuration(forMenuOutDuration);

        // Invisible buttons shouldn't block clicks
        moreGamesButton.setClickable(false);
        noAdsButton.setClickable(false);
        starButton.setClickable(false);
        settingsButton.setClickable(false);
        modesButton.setClickable(false);
        mainButtonEnabled = false;
    }

    public void forMenuFadeIn() {
        buttonBar.animate().alpha(1.0f).setDuration(forMenuInDuration);
        leftButtonColumn.animate().alpha(1.0f).setDuration(forMenuInDuration);
        rightButtonColumn.animate().alpha(1.0f).setDuration(forMenuInDuration);
        modesButton.animate().alpha(1.0f).setDuration(forMenuInDuration);

        // Re-clickable buttons
        moreGamesButton.setClickable(true);
        noAdsButton.setClickable(true);
        starButton.setClickable(true);
        settingsButton.setClickable(true);
        modesButton.setClickable(true);
        mainButtonEnabled = true;
    }

    // Flexible UI resizes according to screen dimensions
    public void flexAll() {
        int height = TDimensions.getHeightPixels();
        int width = TDimensions.getWidthPixels();

        title.flex(height, width);
        flexButtons(height, width);
        popUpInstructions.flex();
        deathMenu.flex(height, width);
        settingsMenu.flex(height, width);
        modesMenu.flex(height, width);

        if (playSymbolState == pss_CLASSIC) {
            flexMainClassic(height, width);
        }
        else if (playSymbolState == pss_ALTERNATE) {
            flexMainAlternate(height, width);
        }
    }

    private void flexButtons(int height, int width) {
        int minDimension = Math.min(height, width);
        int maxDimension = Math.max(height, width);
        int numButtons = buttonBarArray.length;

        // Determine margin
        int margin = (int)(.018 * minDimension);

        // Determine button size, minimum size is 60dp
        int minSize = (int)TDimensions.dpToPixel(60f);
        int size = (int)((minDimension - margin*numButtons*2)/numButtons * 0.95);
        size = Math.max(size, minSize);

        for (Button button: buttonBarArray) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,0); // 0,0 just placeholder

            params.height = size;
            params.width = size;

            params.setMargins(margin, margin, margin, 0);
            button.setLayoutParams(params);
        }

        // Give bottom margin to the bar, not to the buttons
        ((RelativeLayout.LayoutParams)buttonBar.getLayoutParams()).bottomMargin = margin;

        // Flex game modes button
        modesButton.setBackgroundDrawable(Drawables.modesButton(maxDimension * .05f, maxDimension * .007f));
        float minModesSize = TDimensions.dpToPixel(15);
        float modesSize = maxDimension * 0.035f;
        if (modesSize < minModesSize) modesSize = minModesSize;
        modesButton.setPixelTextSize(modesSize);

        // Landscape layout (And super small devices)
        if (width > height || TDimensions.pixelsToDp(height) < 420) {
            buttonBar.removeAllViewsInLayout();
            leftButtonColumn.removeAllViewsInLayout();
            rightButtonColumn.removeAllViewsInLayout();
            leftButtonColumn.addView(moreGamesButton);
            leftButtonColumn.addView(noAdsButton);
            rightButtonColumn.addView(starButton);
            rightButtonColumn.addView(settingsButton);

            ((RelativeLayout.LayoutParams)leftButtonColumn.getLayoutParams()).setMargins(margin, 0, 0, margin);
            ((RelativeLayout.LayoutParams)rightButtonColumn.getLayoutParams()).setMargins(0, 0, margin, margin);

            ((RelativeLayout.LayoutParams)modesButton.getLayoutParams()).bottomMargin = (int)(margin * 2.5f);
        }
        // Portrait layout
        else {
            buttonBar.removeAllViewsInLayout();
            leftButtonColumn.removeAllViewsInLayout();
            rightButtonColumn.removeAllViewsInLayout();
            buttonBar.addView(moreGamesButton);
            buttonBar.addView(noAdsButton);
            buttonBar.addView(starButton);
            buttonBar.addView(settingsButton);

            ((RelativeLayout.LayoutParams)modesButton.getLayoutParams()).bottomMargin = (int)(margin * 1.5f);
        }
    }

    private void setMainButton() {
        View.OnTouchListener mainButtonListener = new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                view.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
                double x = event.getX();
                double y = event.getY();

                // x and y are with respect to the origin at the top left corner,
                // I want coordinates with respect to the center of the circle (the center of the button)
                x -= bitmap.getWidth() / 2;
                y -= bitmap.getHeight() / 2;

                // The circle is sized by dp but java works in px, so we obtain px here
                int radius = bitmap.getWidth() / 2; // bitmap is square - divide by 2 for radius not diameter

                // If coordinates are inside circle
                if (x*x + y*y <= radius * radius) {
                    // When button is clicked
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (mainButtonEnabled && !inGame) { // Don't start the game if it has already started
                            startGame();

                            // Fade out buttons and title
                            mainFadeOutAnimation();
                        }
                    }
                    return true;
                }
                // else
                // Returning false is critical & causes the event to be passed to the next view behind this button
                return false;
            }
        };

        mainButton.setOnTouchListener(mainButtonListener);
    }

    private void setSettingsButton() {
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Display settings screen
                settingsMenu.open();

                // Conditionally add bannerAd
                if (!main.tPreferences().getBoolean(Keys.isNoAdsOwned, false)) {
                    if (bannerAd.getParent() == null) {
                        root.addView(bannerAd);
                    }
                    else {
                        report("bannerAd already added?");
                    }
                }

                // Play button sound
                AndroidSound.click.play(main);
            }
        });
    }

    private void setStarButton() {
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open dialog asking to rate the app
                Dialogs.rateTheApp(main);
                main.tPreferences().putBoolean(Keys.isRatingRequestDisplayed, true); // Don't do a rating request if people already found this button

                // Play button sound
                AndroidSound.click.play(main);
            }
        });
    }

    private void setMoreGamesButton() {
        moreGamesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialogs.viewMoreGames(main);

                // Play button sound
                AndroidSound.click.play(main);
            }
        });
    }

    private void setNoAdsButton() {
        noAdsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!main.tPreferences().getBoolean(Keys.isNoAdsOwned, false))
                    Dialogs.purchaseMenu(main);
                else {
                    Dialogs.noAdsAlreadyPurchased(main);
                }

                // Play button sound
                AndroidSound.click.play(main);
            }
        });
    }

    // For the 4 colored buttons, sets up touch listeners
    private void setButton(final ColorButton button) {
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (allowColorInput) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        button.press();

                        if (inGame) {
                            Game.InputData data = game.getGameMode().input(button.getNumber());
                            if (data.isSuccess) { // Success
                                if (!game.getGameMode().checkAllowInput()) { // Sequence completed
                                    level++;
                                    startNextSequence = true; // Waits for user to lift b4 starting next sequence

                                    // Check to see if new best for the mode has been achieved
                                    int scoreNum = level - 1;
                                    int highScoreNum = main.tPreferences().getInt(game.getGameMode().name() + Keys.modeBest, 0);
                                    if (highScoreNum < scoreNum) {
                                        highScoreNum = scoreNum;
                                        main.tPreferences().putInt(game.getGameMode().name() + Keys.modeBest, highScoreNum);
                                    }
                                }
                            }
                            else { // Failure
                                onFailure(button.getNumber(), data.correct);
                            }
                        }
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        button.performClick(); // To satisfy inspector
                        button.returnToNormal();

                        if (startNextSequence) {
                            // Check to see if any are still lit, don't start next sequence until none are
                            boolean anyLit = false;
                            for (ColorButton b: colorButtons)
                                if (b.getPressed()) {
                                    anyLit = true;
                                    break;
                                }
                            if (!anyLit) {
                                startSequence();
                            }
                        }
                    } // Happens if you press, and then an orientation change or some interruption occurs
                    else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        // Return buttons to normal - Fixes bug on portrait to reverse portrait orientation change which doesn't trigger onConfigurationChange (Why)
                        for (ColorButton button : colorButtons) {
                            button.returnToNormal();
                        }
                    }
                }
                return false;
            }
        });
    }

    private void onFailure(int pressed, int correct) {
        flog("onFailure called, score: " + (level - 1));

        inGame = false;
        startNextSequence = false;

        int scoreNum = level - 1;
        level = 1;

        // In case user fails so fast that animations haven't ended yet
        popUpInstructions.endAnimations();

        // Death sound
        AndroidSound.failure.play(main);

        // Track the number of games that have been completed
        int gamesCompleted = main.tPreferences().getInt(Keys.gamesCompleted, 0) + 1;
        main.tPreferences().putInt(Keys.gamesCompleted, gamesCompleted);

        // Request rating if never requested before and conditions are met
        boolean ratingRequestDisplayed = main.tPreferences().getBoolean(Keys.isRatingRequestDisplayed, false);

        if (!ratingRequestDisplayed && gamesCompleted > 10 && scoreNum > 10) {
            flog("Rating request displayed");
            Dialogs.rateTheApp(main);
            main.tPreferences().putBoolean(Keys.isRatingRequestDisplayed, true);
        }
        else {
            // Don't give bad initial ad experience
            // Don't continue if we remember that no_ads has been purchased
            if (gamesCompleted > 1 && !main.tPreferences().getBoolean(Keys.isNoAdsOwned, false)) {
                Ads.rollAdDisplay(.40, null); // If ad is loaded, random chance to display
            }
        }

        deathMenu.setValues(scoreNum, pressed, correct);
        deathMenu.animateIn();

        // Unlock screen orientation
        main.setRequestedOrientation(SCREEN_ORIENTATION_USER);
    }

    // Remove the banner ad if possible
    public void removeBannerAd() {
        if (bannerAd != null && bannerAd.getParent() != null) {
            root.removeView(bannerAd);
        }
    }

    // End game in progress, reset variables, and return to main menu
    public void exitGameToMainMenu() {
        flog("Exit to game menu");

        inGame = false;
        mainButtonEnabled = true;
        startNextSequence = false;
        level = 1;
        mainButton.setText("");

        // Remove scheduled tasks
        main.handler().removeCallbacksAndMessages(null);

        // End animations incase still ongoing and return buttons to normal incase they are in middle of press by sequence
        popUpInstructions.endAnimations();
        for (ColorButton button: colorButtons) {
            button.returnToNormal();
        }

        mainFadeInAnimation();
        allowColorInput = true;
        main.setRequestedOrientation(SCREEN_ORIENTATION_USER);
    }

    public void disableNonColorButtons() {
        moreGamesButton.setEnabled(false);
        noAdsButton.setEnabled(false);
        starButton.setEnabled(false);
        settingsButton.setEnabled(false);
        modesButton.setEnabled(false);
        mainButtonEnabled = false;
    }

    public void enableNonColorButtons() {
        moreGamesButton.setEnabled(true);
        noAdsButton.setEnabled(true);
        starButton.setEnabled(true);
        settingsButton.setEnabled(true);
        modesButton.setEnabled(true);
        mainButtonEnabled = true;
    }

    public void enableSettingsButton() {
        settingsButton.setEnabled(true);
    }

    public void enableModesButton() {
        modesButton.setEnabled(true);
    }

    // Play symbol has two different configurations depending on if playing classic or an alternative game mode
    private int playSymbolState = pss_START; // -1 for startup, 0 for classic config, 1 for alternative config
    private static int pss_START = -1;
    private static int pss_CLASSIC = 0;
    private static int pss_ALTERNATE = 1;
    private LinearLayout alternativePlayLayout;
    private GoodTextView altModeText;
    public void playSymbolSetUp() {
        Game.GameMode mode = Game.GameMode.valueOf(main.tPreferences().getString(Keys.gameMode, Game.GameMode.Classic.name()));

        // Classic layout
        if (mode == Game.GameMode.Classic) {
            if (playSymbolState != pss_CLASSIC) { // Don't repeat
                Rlp playSymbolLp = new Rlp();
                playSymbolLp.center();
                playSymbol.setLayoutParams(playSymbolLp);

                if (playSymbolState == pss_ALTERNATE) {
                    alternativePlayLayout.removeAllViews();
                    root.removeView(alternativePlayLayout);
                }

                // Gotta add behind the ModesMenu if old android
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && modesMenu.isModesMenuUp) {
                    root.addView(playSymbol, root.indexOfChild(modesMenu));
                }
                else {
                    root.addView(playSymbol);
                }

            }
            playSymbolState = pss_CLASSIC;
            flexMainClassic(TDimensions.getHeightPixels(), TDimensions.getWidthPixels());
        }
        // Alternative layout
        else {
            altModeText.setText(mode.displayName());
            if (playSymbolState != pss_ALTERNATE) { // Don't repeat
                if (playSymbolState == pss_CLASSIC) root.removeView(playSymbol);

                alternativePlayLayout.addView(playSymbol);
                alternativePlayLayout.addView(altModeText);

                // Gotta add behind the ModesMenu if old android
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && modesMenu.isModesMenuUp) {
                    root.addView(alternativePlayLayout, root.indexOfChild(modesMenu));
                }
                else {
                    root.addView(alternativePlayLayout);
                }

                playSymbolState = pss_ALTERNATE;
                flexMainAlternate(TDimensions.getHeightPixels(), TDimensions.getWidthPixels());
            }
        }
    }

    // Flex mainButton as well as playSymbol
    public void flexMainClassic(int height, int width) {
        // Minimum size 100dp
        float scale = 0.40f;
        float minDimension = Math.min(height, width);
        float dimensionSize = minDimension * scale;
        float minSize = TDimensions.dpToPixel(100);

        int size = (int)Math.max(minSize, dimensionSize);

        ViewGroup.LayoutParams mainLp = mainButton.getLayoutParams();
        mainLp.height = size;
        mainLp.width = size;

        mainButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size/2);

        int playSize = (int)(size / Math.pow(2, 0.5)); // divide Math.pow(2,0.5) to inscribe into MainButton circle
        ViewGroup.LayoutParams playLp = playSymbol.getLayoutParams();
        playLp.height = playSize;
        playLp.width = playSize;
    }

    public void flexMainAlternate(int height, int width) {
        // Minimum size 100dp
        float scale = 0.40f;
        float minDimension = Math.min(height, width);
        float dimensionSize = minDimension * scale;
        float minSize = TDimensions.dpToPixel(100);

        int size = (int)Math.max(minSize, dimensionSize);

        ViewGroup.LayoutParams mainLp = mainButton.getLayoutParams();
        mainLp.height = size;
        mainLp.width = size;

        mainButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, size/2);

        int playSize = (int)(0.85f * size / Math.pow(2, 0.5));
        ViewGroup.LayoutParams playLp = playSymbol.getLayoutParams();
        playLp.height = playSize;
        playLp.width = playSize;

        altModeText.setPixelTextSize(size * .1f);
    }

    private void setGameModesButton() {
        modesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If no_ads purchase has been made or have watched the rewarded video ad, then you can get to game modes menu
                if (main.tPreferences().getBoolean(Keys.isNoAdsOwned, false) || main.tPreferences().getBoolean(Keys.isRewardedGameModes, false)) {
                    modesMenu.open();
                    forMenuFadeOut();
                }
                else {
                    Dialogs.unlockGameModes(main, new Runnable() {
                        @Override
                        public void run() {
                            // On reward
                            main.tPreferences().putBoolean(Keys.isRewardedGameModes, true);
                            modesMenu.open();
                            forMenuFadeOut();
                        }
                    });
                }

                // Play button sound
                AndroidSound.click.play(main);
            }
        });
    }
}
