import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

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

        byte[] bytes = new byte[1];
        byte toPrint = 0;
        while (inputChar != 's'){
            System.out.print ("Enter 'h' to hit or 's' to stick ");
            try{
                System.in.read(bytes);
                toPrint = bytes[0];
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputChar = (char)toPrint;
            if(inputChar == 'h'){
                gregshand.drawCard(deck);
                displayAllHands();
            }
        }
        System.out.println("end of demo message");
    }
    void initializePlayers(){
        BJackPlayer dealer = new BJackPlayer();
        bJackPlayers.add(dealer);
        BJackPlayer greg = new BJackPlayer();
        bJackPlayers.add(greg);
//        Player carolyn = new Player();
//        players.add(carolyn);
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
        for (BJackPlayer BJackPlayer : this.bJackPlayers) {
            for (Hand hand : BJackPlayer.hands) {
                for(Card card: hand.cards){
                    card.displayCardSignature();
                    System.out.print(" | ");
                }
            }
            System.out.println("\n");
        }
    }
}
