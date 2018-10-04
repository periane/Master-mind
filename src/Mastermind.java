
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The game Mastermind
 * A player is given some number of tries to guess a secret code of some length created by the computer.
 */
public class Mastermind {
    private boolean _duplicatesAllowed;
    private int _numberOfGuesses;
    private int _codeLength;
    private int _minCodeValue;
    private int _maxCodeValue;
    private int _currentGuess;

   
    private HashMap<Integer, HashSet<Integer>> _code;

    public Mastermind() {
        _codeLength = 4;
        _minCodeValue = 0;
        _maxCodeValue = 9;
        _numberOfGuesses = 8;
        _duplicatesAllowed = false;
        _currentGuess = 0;
    }

    
    public Mastermind(boolean duplicatesAllowed) {
        this();
        _duplicatesAllowed = duplicatesAllowed;
    }

    public Mastermind(int numberOfGuesses) {
        this();
        if (numberOfGuesses < 1) {
            throw new IllegalArgumentException("At least one guess is required!");
        }
        _numberOfGuesses = numberOfGuesses;
    }

    
   public Mastermind(int numberOfGuesses, boolean duplicatesAllowed) {
        this(numberOfGuesses);
        _duplicatesAllowed = duplicatesAllowed;
    }

    
    public Mastermind(int numberOfGuesses, int codeLength) {
        this(numberOfGuesses);
        if (codeLength < 1) {
            throw new IllegalArgumentException("Secret code must contain at least one number!");
        }
        _codeLength = codeLength;
    }

   
    public Mastermind(int numberOfGuesses, int codeLength, boolean duplicatesAllowed) {
        this(numberOfGuesses, codeLength);
        _duplicatesAllowed = duplicatesAllowed;
    }

    
    public Mastermind(int numberOfGuesses, int codeLength, int minCodeValue, int maxCodeValue) {
        this(numberOfGuesses, codeLength);
        if (!isValidCodeNumber(minCodeValue) || !isValidCodeNumber(maxCodeValue)) {
            throw new IllegalArgumentException("Secret code values must be between 0 and 9!");
        }
        _minCodeValue = minCodeValue;
        _maxCodeValue = maxCodeValue;
    }

   
    public Mastermind(int numberOfGuesses, int codeLength, int minCodeValue, int maxCodeValue, boolean duplicatesAllowed) {
        this(numberOfGuesses, codeLength, minCodeValue, maxCodeValue);
        _duplicatesAllowed = duplicatesAllowed;
    }

    /**
     * Starts the game loop
     */
    public void play() {
        generateCode();

        System.out.println("Welcome to Mastermind!");
        System.out.printf("You should enter numbers between %d and %d.\n", _minCodeValue, _maxCodeValue);
       // System.out.printf("Duplicate values are%sallowed.\n", (_duplicatesAllowed ? " ": " not "));
        System.out.printf("Can you break the code in just %d guesses?\n", _numberOfGuesses);

        boolean winner = false;
        Scanner input = new Scanner(System.in);
        while (_currentGuess < _numberOfGuesses) {
            System.out.printf("Guess %d: ", _currentGuess + 1);
            String guess = input.nextLine();

            // Check the current guess, and exit if it is a perfect match
            GuessResult result = submitGuess(guess);
            if (result.isPerfectGuess()) {
                winner = true;
                break;
            }

            // Output the score, or a message, if either were provided
            if (!result.getScore().isEmpty()) {
                System.out.println(result.getScore());
            } else if (!result.getMessage().isEmpty()) {
                System.out.println(result.getMessage());
            }
        }

        String endGameMessage = winner ? "You solved it!" : "Sorry you lost";
        System.out.println(endGameMessage);
        
        
    }
    
   

    /**
     * Generates the secret code for the user to guess
     */
    private void generateCode() {
        if (!_duplicatesAllowed) {
            int codeRangeSize = _maxCodeValue - _minCodeValue;
            if (codeRangeSize < _codeLength) {
                throw new RuntimeException("Code value range must be larger than code length! .");
            }
        }

        _code = new HashMap<>();
        for (int i = 0; i < _codeLength; i++) {
            int number = ThreadLocalRandom.current().nextInt(_minCodeValue, _maxCodeValue + 1);
            if (!_duplicatesAllowed) {
                // Recalculate number for current position if it already exists to prevent duplicates
                while (_code.containsKey(number)) {
                    number = ThreadLocalRandom.current().nextInt(_minCodeValue, _maxCodeValue + 1);
                }
            }

            HashSet<Integer> indices = _code.getOrDefault(number, new HashSet<>());
            indices.add(i);
            _code.put(number, indices);
        }
    }


    private GuessResult submitGuess(String guess) {
        _currentGuess++;

        if (guess.length() != _codeLength) {
            String errorMessage = String.format("Guess must be %d numbers long!", _codeLength);
            return new GuessResult(errorMessage);
        }

        try {
            return getScore(guess);
        } catch (IllegalArgumentException e) {
            String errorMessage = String.format("Invalid Guess: %s", e.getMessage());
            return new GuessResult(errorMessage);
        }
    }

  
    private GuessResult getScore(String guess) {
        int scorePluses = 0;
        int scoreMinuses = 0;
        boolean perfectGuess = true;

        // Track the number of matches for each value, to prevent double scoring
        HashMap<Integer, Integer> matches = new HashMap<>();
        for (int i = 0; i < guess.length(); i++) {
            char guessValue = guess.charAt(i);
            int number = Character.getNumericValue(guessValue);
            if (isValidGuessNumber(number)) {
                // Skip current value if we have exhausted value's possible matches
                int numberMatchCount = matches.getOrDefault(number, 0);
                int actualNumberCount = _code.getOrDefault(number, new HashSet<>()).size();
                if (actualNumberCount > 0 && numberMatchCount == actualNumberCount) {
                    perfectGuess = false;
                    continue;
                }

                // Check the current guess position and update scores accordingly
                switch (checkGuessValue(number, i)) {
                    case '+':
                        scorePluses++;
                        matches.put(number, matches.getOrDefault(number, 0) + 1);
                        break;
                    case '-':
                        scoreMinuses++;
                        matches.put(number, matches.getOrDefault(number, 0) + 1);
                        
                    default:
                        perfectGuess = false;
                }
            } else {
                throw new IllegalArgumentException(String.format("Guess values must be numbers between %d and %d!", _minCodeValue, _maxCodeValue));
            }
        }

        return new GuessResult(getScoreString(scorePluses, scoreMinuses), perfectGuess);
    }

   
    private boolean isValidCodeNumber(int number) {
        return number >= 0 && number <= 9;
    }

    
    private boolean isValidGuessNumber(int number) {
        return number >= _minCodeValue && number <= _maxCodeValue;
    }

    /**
     * Determine if the given guess was a match
     * @param number Number to check
     * @param index Index to check
     * @return Null char if no match, '-' for number match only, and '+' for an exact (number and index) match
     */
    private char checkGuessValue(int number, int index) {
        char score = '\0';

        if (_code.containsKey(number)) {
            HashSet<Integer> indices = _code.get(number);
            if (indices.contains(index)) {
                score = '+';
            } else {
                score = '-';
            }
        }

        return score;
    }
    
   
    /**
     * Constructs a score string composed of the given number of pluses and minuses
     * @param pluses Number of pluses to include in the string
     * @param minuses Number of minuses to include in the string
     * @return A score string of '#' and '-' symbols
     */
    private String getScoreString(int pluses, int minuses) {
        char[] score = new char[pluses + minuses];
        Arrays.fill(score, 0, pluses, '+');
        Arrays.fill(score, pluses, score.length, '-');
        return new String(score);
    }

    /**
     * Internal class used for handling the results of guess checks
     */
    private class GuessResult {
        private String _scoreValue;
        private boolean _perfectGuess;
        private String _message;

   
        public GuessResult() {
            _scoreValue = "";
            _perfectGuess = false;
            _message = "";
        }

        public GuessResult(String message) {
            this();
            _message = message;
        }

   
        public GuessResult(String scoreValue, boolean perfectGuess) {
            this();
            _scoreValue = scoreValue;
            _perfectGuess = perfectGuess;
        }

       
        public GuessResult(String scoreValue, boolean perfectGuess, String message) {
            this(scoreValue, perfectGuess);
            _message = message;
        }

       
        public String getScore() {
            return _scoreValue;
        }

        
        public boolean isPerfectGuess() {
            return _perfectGuess;
        }

       
        public String getMessage() {
            return _message;
        }
      
    }
}
