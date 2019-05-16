package hackman.trevor.copycat.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    private GameMode gameMode;
    public GameMode getGameMode() {
        return gameMode;
    }

    public Game(GameMode gameMode) {
        this.gameMode = gameMode;
        GameMode.initialize(gameMode);
    }

    public enum GameMode {
        Classic("Classic") {
            @Override
            public InputData input(int pressed) {
                if (allowInput) {
                    // Success
                    if (sequence.get(index) == pressed) {
                        // Last one
                        if (index == sequence.size() - 1) {
                            incrementSequence();
                            allowInput = false;
                            index = 0;
                        }
                        // Not last
                        else {
                            index += 1;
                        }
                        return new InputData(true);
                    }
                    return new InputData(false, sequence.get(index));
                }
                return new InputData(false);
            }
            @Override
            public void incrementSequence() {
                sequence.add(random.nextInt(4));
            }
            @Override
            public int playBack() {
                if (!allowInput) {
                    int toReturn = index;
                    index += 1;
                    if (index >= sequence.size()) {
                        index = 0;
                        allowInput = true;
                    }
                    return sequence.get(toReturn);
                }
                return -1;
            }
        },
        Reverse("Reverse") {
            @Override
            public InputData input(int pressed) {
                if (allowInput) {
                    // Success
                    if (sequence.get(sequence.size() - 1 - index) == pressed) {
                        // Last one
                        if (index == sequence.size() - 1) {
                            incrementSequence();
                            allowInput = false;
                            index = 0;
                        }
                        // Not last
                        else {
                            index += 1;
                        }
                        return new InputData(true);
                    }
                    return new InputData(false, sequence.get(sequence.size() - 1 - index));
                }
                return new InputData(false);
            }
            @Override
            public void incrementSequence() {
                sequence.add(random.nextInt(4));
            }
            @Override
            public int playBack() {
                if (!allowInput) {
                    int toReturn = index;
                    index += 1;
                    if (index >= sequence.size()) {
                        index = 0;
                        allowInput = true;
                    }
                    return sequence.get(toReturn);
                }
                return -1;
            }
        },
        Chaos("Chaos") {
            @Override
            public InputData input(int pressed) {
                if (allowInput) {
                    // Success
                    if (sequence.get(index) == pressed) {
                        // Last one
                        if (index == sequence.size() - 1) {
                            incrementSequence();
                            allowInput = false;
                            index = 0;
                        }
                        // Not last
                        else {
                            index += 1;
                        }
                        return new InputData(true);
                    }
                    return new InputData(false, sequence.get(index));
                }
                return new InputData(false);
            }
            @Override
            public void incrementSequence() {
                for (int i = 0; i < sequence.size(); i++) {
                    sequence.set(i, random.nextInt(4));
                }
                sequence.add(random.nextInt(4));
            }
            @Override
            public int playBack() {
                if (!allowInput) {
                    int toReturn = index;
                    index += 1;
                    if (index >= sequence.size()) {
                        index = 0;
                        allowInput = true;
                    }
                    return sequence.get(toReturn);
                }
                return -1;
            }
        },
        NoRepeat("Single") {
            @Override
            public InputData input(int pressed) {
                if (allowInput) {
                    // Success
                    if (sequence.get(index) == pressed) {
                        // Last one
                        if (index == sequence.size() - 1) {
                            incrementSequence();
                            allowInput = false;
                            index = 0;
                        }
                        // Not last
                        else {
                            index += 1;
                        }
                        return new InputData(true);
                    }
                    // Failure
                    return new InputData(false, sequence.get(index));
                }
                return new InputData(false);
            }
            @Override
            public void incrementSequence() {
                sequence.add(random.nextInt(4));
            }
            @Override
            public int playBack() {
                if (!allowInput) {
                    index = 0;
                    allowInput = true;
                    return sequence.get(sequence.size() - 1);
                }
                return -1;
            }
        };
        private static Random random;
        private static List<Integer> sequence;
        private static int index;
        private static boolean allowInput;

        public abstract InputData input(int pressed); // Only do anything if (allowInput). Return true if success. Return false if failed.
        public abstract void incrementSequence(); // Extend the sequence
        public abstract int playBack(); // Only do anything if (!allowInput). Returns -1 if (allowInput).

        public boolean checkAllowInput() {
            return allowInput;
        }

        // Initialize the game
        private static void initialize(GameMode gameMode) {
            random = new Random();
            sequence = new ArrayList<>();
            index = 0;
            allowInput = false;

            gameMode.incrementSequence();
        }

        // The display name of each mode
        private String name;
        GameMode(String name) {
            this.name = name;
        }
        public String displayName() {
            return name;
        }
    }

    public static class InputData {
        public boolean isSuccess; // True if success. False if failed.
        public int correct; // The correct button that should've been pressed if failed. -1 if success.

        private InputData(boolean isSuccess) {
            this(isSuccess, -1);
        }

        private InputData(boolean isSuccess, int correct) {
            this.isSuccess = isSuccess;
            this.correct = correct;
        }
    }
}
