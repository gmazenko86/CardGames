import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;

public class BJackGame extends CardGame {

    void playGame(){
        preDealInit(1);
        dealHands();

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

        displayActiveHands();

        boolean dealerBlackJack = checkDealerBjack();

        if(!dealerBlackJack) {
            for (BJackPlayer player : this.bJackPlayers) {
                playHands(player);
            }
        } else{
            displayAllHands();
        }

        System.out.println("end of demo message");
    }
    void initializePlayers(int numPlayers){
        // create 1 more than numPlayers the dealer is a player too
        for (int i = 0; i <= numPlayers; i++){
            BJackPlayer bJackPlayer = new BJackPlayer();
            bJackPlayers.add(bJackPlayer);
        }
    }

    void dealHands(){
        // deal 2 cards for each hand, including the dealer
        // need an array list with the dealer included as the last entry
        ArrayList<BJackPlayer> playersPlusDealer = new ArrayList<>(this.bJackPlayers);
        playersPlusDealer.add(this.dealer);

        for(int i = 0; i < 2; i++) {
            for (BJackPlayer BJackPlayer : playersPlusDealer) {
                for (Hand hand : BJackPlayer.hands) {
                    hand.drawCard(this.deck);
                }
            }
        }
        // set black jack flags
        for(BJackPlayer player : playersPlusDealer){
            for(BJackHand hand : player.hands){
                setBlackJackFlag(hand);
            }
        }
    }

    void setBlackJackFlag(BJackHand hand){
        if(hand.cards.size() == 2 && hand.getHandTotal() == 21){
            hand.handAttribute = BJackHand.HandAttribute.BLACKJACK;
        }
    }

    void displayActiveHands(){
        System.out.println("*".repeat(40));
        displayDealerUpCard(dealer.hands.get(0));
        for (BJackPlayer BJackPlayer : this.bJackPlayers) {
            for (BJackHand hand : BJackPlayer.hands) {
                if(hand.handAttribute == BJackHand.HandAttribute.NONE){
                    displayHand(hand);
                } else{
                    displayHandWithTotal(hand);
                }
            }
            System.out.println("");
        }
    }

    void displayAllHands(){
        System.out.println("*".repeat(40));
        displayHandWithTotal(dealer.hands.get(0));
        for(BJackPlayer player : bJackPlayers){
            for(BJackHand hand : player.hands){
                displayHandWithTotal(hand);
                System.out.println("");
            }
        }

    }

    void displayHand(BJackHand hand){
        for(Card card: hand.cards){
            card.displayCardSignature();
            System.out.print(" | ");
        }
        System.out.print("");
    }

    void displayHandWithTotal(BJackHand hand){
        displayHand(hand);
        System.out.print("Total is " + hand.getHandTotal());

        if(hand.handAttribute == BJackHand.HandAttribute.BUST){
            printRedText(" ::: BUST");
            System.out.println("");
        }
        if(hand.handAttribute == BJackHand.HandAttribute.BLACKJACK){
            printGreenText(" ::: BLACKJACK");
            System.out.println("");
            System.out.println("");
        }
    }

    void displayActiveHand(BJackHand hand){}

    void displayDealerUpCard(BJackHand hand){
        Card upCard = hand.cards.get(0);
        printYellowText(upCard.getCardSignature());
        printYellowText(" | ");
        printYellowText("X".repeat(10));
        printYellowText(" | ");
        printYellowText(" Dealer Showing " + upCard.getCardValue());
        System.out.println("");
        System.out.println("");
    }

    void displayInputRequest(){
        System.out.print("Enter 'h' to hit or 's' to stick ");
    }

    void playHands(BJackPlayer player){
        for(BJackHand hand: player.hands) {
            if(hand.handAttribute == BJackHand.HandAttribute.NONE ||
                hand.handAttribute == BJackHand.HandAttribute.SPLITHAND) {
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
                                displayActiveHands();
                                if (hand.getHandTotal() > 21) {
                                    hand.handAttribute = BJackHand.HandAttribute.BUST;
                                    displayActiveHands();
                                }
                                displayInputRequest();
                                break;
                            case 's':
                                hand.handAttribute = BJackHand.HandAttribute.STICK;
                                displayActiveHands();
                                break;
                            default:
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } while (inputChar != 's' && hand.handAttribute != BJackHand.HandAttribute.BUST);
            }
        }
    }

    void preDealInit(int numPlayers){
        initializePlayers(numPlayers);
    }

    boolean checkDealerBjack(){
        if (dealer.hands.get(0).handAttribute == BJackHand.HandAttribute.BLACKJACK){
            for(BJackPlayer player : this.bJackPlayers){
                for(BJackHand hand : player.hands){
                    if(hand.handAttribute == BJackHand.HandAttribute.NONE){
                        hand.handResult = BJackHand.HandResult.LOSE;
                    }
                    if(hand.handAttribute == BJackHand.HandAttribute.BLACKJACK){
                        hand.handResult = BJackHand.HandResult.PUSH;
                    }
                }
            }
            return true;
        }
        return false;
    }

    //TODO: DbaseExercises contains printlnBlue and printlnYellow. Build a reusable library
    public void printRedText(String str){
        System.out.print("\033[31m"); // This turns the text to red
        System.out.print(str);
        System.out.print("\033[0m"); // This resets the text back to default
    }

    public void printYellowText(String str){
        System.out.print("\033[33m"); // This turns the text to red
        System.out.print(str);
        System.out.print("\033[0m"); // This resets the text back to default
    }

    public void printGreenText(String str){
        System.out.print("\033[32m"); // This turns the text to red
        System.out.print(str);
        System.out.print("\033[0m"); // This resets the text back to default
    }
}
