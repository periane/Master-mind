

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	
	    // Parse values from any existing arguments
        Boolean enableDuplicates = args.length >= 1 ? Boolean.parseBoolean(args[0]) : null;
        Integer numberOfGuesses = args.length >= 2 ? Integer.parseInt(args[1]) : null;
        Integer secretCodeLength = args.length >= 3 ? Integer.parseInt(args[2]) : null;
        Integer minCodeValue = null, maxCodeValue = null;
        if (args.length == 5) {
            minCodeValue = Integer.parseInt(args[3]);
            maxCodeValue = Integer.parseInt(args[4]);
        }

        // Instantiate the game with any provided settings
        Mastermind game;
        switch (args.length) {
            case 5:
                game = new Mastermind(numberOfGuesses, secretCodeLength, minCodeValue, maxCodeValue, enableDuplicates);
                break;
            case 3:
                game = new Mastermind(numberOfGuesses, secretCodeLength, enableDuplicates);
                break;
            case 2:
                game = new Mastermind(numberOfGuesses, enableDuplicates);
                break;
            case 1:
                game = new Mastermind(enableDuplicates);
                break;
            case 0:
                game = new Mastermind();
                break;
            default:
                System.out.println("Invalid number of arguments provided!");
                return;
        }

        // Let's play!
        game.play();
    }
}
