package hackman.trevor.copycat.system;

// Keys for TPreferences (key,value) save data
public enum Keys {;
    public final static String isNoAdsOwned = "noAdsOwned"; // Whether or not noAds has been purchased. Don't display ads if this is true.
    public final static String isRewardedGameModes = "rewardedGameModes"; // Whether or not extra game modes have been unlocked
    public final static String isRatingRequestDisplayed = "ratingRequestDisplayed"; // Whether or not rating dialog has been displayed before
    public final static String gamesCompleted = "gamesCompleted";
    public final static String speed = "speed"; // Remember the game speed selected in the settings
    public final static String colors = "colors"; // Remember the color theme selected in the settings

    // Remembers the name of the game mode last selected
    // Typical usage: Game.GameMode mode = Game.GameMode.valueOf(main.tPreferences.getString(Keys.gameMode, Game.GameMode.Classic.name()));
    public final static String gameMode = "gameMode";

    // NOT to be used by itself. Remembers the best score achieved in the prior specified game mode.
    // Typical usage: int highScoreNum = main.tPreferences.getInt(game.getGameMode().name() + Keys.modeBest, 0);
    public final static String modeBest = "Best";
}
