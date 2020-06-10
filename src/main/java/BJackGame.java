import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;

public class BJackGame extends CardGame {

    void playGame(){
        initializePlayers();
        // temporarily give the first player 2 hands for debug and test
/*
        BJackHand tempHand = new BJackHand();
        BJackHand tempHand1 = new BJackHand();
        BJackHand tempHand2 = new BJackHand();

        this.bJackPlayers.get(1).hands.add(tempHand);
        this.bJackPlayers.get(1).hands.add(tempHand1);
        this.bJackPlayers.get(1).hands.add(tempHand2);
*/
        initializeDeck();
        initializeHands();

        // need an array list with the dealer removed
        ArrayList<BJackPlayer> playersNoDealer = new ArrayList<>(this.bJackPlayers);
        playersNoDealer.remove(0);

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

        for(BJackPlayer player : playersNoDealer){
            playHands(player);
        }

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
                System.out.println("Total is " + hand.getHandTotal());
            }
            System.out.print("\n");
        }
    }

    void displayInputRequest(){
        System.out.print("Enter 'h' to hit or 's' to stick ");
    }

    void playHands(BJackPlayer player){
        for(BJackHand hand: player.hands) {
            char inputChar = 0;
            displayInputRequest();
            do {
                try {
                    byte[] bytes = new byte[1];
                    System.in.read(bytes);
                    inputChar = (char) bytes[0];
                    switch (inputChar) {
                        case 'h':
                            hand.drawCard(deck);
                            displayAllHands();
                            displayInputRequest();
                        case 's' :
                        default:
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (inputChar != 's');
        }
    }
}
