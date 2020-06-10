import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Scanner;

public class BJackGame extends CardGame {

    void playGame(){
        initializePlayers();
        initializeDeck();
        initializeHands();


        // console object will be null if program is run from IDE
        Console console = System.console();

        if(Objects.nonNull(console)){
            console.printf ("Print console object using console.printf " + console.toString() + "\n");
            System.out.println ("Print console object using System.out.println " + console.toString());
            PrintWriter writer = console.writer();
            writer.println("Print console object using PrintWriter.println " + console.toString());
        }

        for(Card card : deck.cards){
            card.displayCardSignature();
            System.out.print("| ");
        }
        System.out.println("\n");

        displayAllHands();

        BJackPlayer greg = this.bJackPlayers.get(1);
        BJackHand gregshand = greg.hands.get(0);

        char inputChar = 0;
        System.out.print("Enter 'h' to hit or 's' to stick ");
        do{
            try {
                byte[] bytes = new byte[1];
                System.in.read(bytes);
                inputChar = (char) bytes[0];
                switch(inputChar) {
                    case 'h':
                        gregshand.drawCard(deck);
                        displayAllHands();
                        System.out.print("Enter 'h' to hit or 's' to stick ");
                    default:
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (inputChar != 's');
        System.out.println("end of demo message");
    }
    void initializePlayers(){
        BJackPlayer dealer = new BJackPlayer();
        bJackPlayers.add(dealer);
        BJackPlayer greg = new BJackPlayer();
        bJackPlayers.add(greg);
//        BJackPlayer carolyn = new BJackPlayer();
//        bJackPlayers.add(carolyn);
    }

    void initializeHands(){
        // deal 2 cards for each hand, including the dealer
        for(int i = 0; i < 2; i++) {
            for (BJackPlayer BJackPlayer : this.bJackPlayers) {
                for (Hand hand : BJackPlayer.hands) {
                    hand.drawCard(this.deck);
                }
            }
        }
    }

    void displayAllHands(){
        System.out.println("*".repeat(40));
        for (BJackPlayer BJackPlayer : this.bJackPlayers) {
            for (BJackHand hand : BJackPlayer.hands) {
                for(Card card: hand.cards){
                    card.displayCardSignature();
                    System.out.print(" | ");
                }
                System.out.println("Total is " + hand.getHardTotal());
            }
            System.out.print("\n");
        }
    }

//    void playHands(BJackPlayer player){
//    }
}
