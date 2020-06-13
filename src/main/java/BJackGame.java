import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;

public class BJackGame extends CardGame {

    ArrayList<BJackPlayer> players;
    BJackPlayer dealer;
    BJackHand dealerHand;

    BJackGame(){
        super();
        this.players = new ArrayList<>();
        this.dealer = new BJackPlayer();
        this.dealerHand = dealer.hands.get(0);
    }

    void playGame(){
        preGameInit(3);
        while(deck.deckIndex < 26) {
            dealHands();

            // console object will be null if program is run from IDE
            Console console = System.console();

            if (Objects.nonNull(console)) {
                console.printf("Print console object using console.printf " + console.toString() + "\n");
                System.out.println("Print console object using System.out.println " + console.toString());
                PrintWriter writer = console.writer();
                writer.println("Print console object using PrintWriter.println " + console.toString());
            }

            for (Card card : deck.cards) {
                card.displayCardSignature();
                System.out.print("| ");
            }
            System.out.println("\n");

            // need an array list of players plus the dealer
            ArrayList<BJackPlayer> playersPlusDealer = getPlayersPlusDealer();
            for (BJackPlayer player : playersPlusDealer) {
                for (BJackHand hand : player.hands) {
                    hand.setBlackJackFlag();
                }
            }
            // use displayActiveHands() instead of displayAllHands when the dealer
            // hole card has not yet been shown
            displayActiveHands();

            boolean dealerBlackJack = dealerHasBjack();

            if (!dealerBlackJack) {
                for (BJackPlayer player : this.players) {
                    playHands(player);
                }
            } else {
                setPlayerHandResults();
                displayAllHands();
            }

            // now play the dealer hand if there are any active player hands
            if (anyActivePlayerHands()) {
                playDealerHand();
            }

            setPlayerHandResults();
            displayResults();
            payAndCollect();
            displayPlayerBankrolls();
            System.out.println("deckindex = " + deck.deckIndex);

            // reinitialize all hands by getting new instances
            // for now, each player gets one hand
            for(BJackPlayer player : playersPlusDealer){
                player.hands.removeAll(player.hands);
                BJackHand hand = new BJackHand();
                player.hands.add(hand);
            }
        }
        deck.shuffle();
        deck.deckIndex = 0;

        System.out.println("end of demo message");
    }

    void initializePlayers(int numPlayers){
        for (int i = 0; i < numPlayers; i++){
            BJackPlayer bJackPlayer = new BJackPlayer();
            players.add(bJackPlayer);
        }
    }

    void dealHands(){
        // deal 2 cards for each hand, including the dealer
        // need an array list with the dealer included as the last entry
        ArrayList<BJackPlayer> playersPlusDealer = getPlayersPlusDealer();

        for(int i = 0; i < 2; i++) {
            for (BJackPlayer BJackPlayer : playersPlusDealer) {
                for (Hand hand : BJackPlayer.hands) {
                    hand.drawCard(this.deck);
                }
            }
        }
    }

    ArrayList<BJackPlayer> getPlayersPlusDealer(){
        ArrayList<BJackPlayer> playersPlusDealer = new ArrayList<>(this.players);
        playersPlusDealer.add(this.dealer);
        return  playersPlusDealer;
    }

    void displayActiveHands(){
        System.out.println("*".repeat(40));
        displayDealerUpCard(dealerHand);
        for (BJackPlayer BJackPlayer : this.players) {
            for (BJackHand hand : BJackPlayer.hands) {
                if(hand.handAttribute == BJackHand.HandAttribute.NONE){
                    displayHand(hand);
                    System.out.println();
                } else{
                    displayHandWithTotal(hand, false);
                }
            }
        }
    }

    void displayAllHands(){
        System.out.println("*".repeat(40));
        displayHandWithTotal(dealerHand, false);
        for(BJackPlayer player : players){
            for(BJackHand hand : player.hands){
                displayHandWithTotal(hand, false);
            }
        }
    }

    void displayResults(){
        System.out.println("*".repeat(40));
        displayHandWithTotal(dealerHand, false);
        for(BJackPlayer player : players){
            for(BJackHand hand: player.hands){
                displayHandWithTotal(hand, true);
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

    void displayHandWithTotal(BJackHand hand, boolean printResults){
        displayHand(hand);
        System.out.print("Total is " + hand.getHandTotal());

        if(hand.handAttribute == BJackHand.HandAttribute.BUST){
            printRedText(" ::: BUST");
        }
        if(hand.handAttribute == BJackHand.HandAttribute.BLACKJACK){
            printGreenText(" ::: BLACKJACK");
        }
        if(printResults){
            System.out.print(" -----Player result = " + hand.handResult.name());
        }
        System.out.println();
    }

    void displayDealerUpCard(BJackHand hand){
        Card upCard = hand.cards.get(0);
        printYellowText(upCard.getCardSignature());
        printYellowText(" | ");
        printYellowText("X".repeat(10));
        printYellowText(" | ");
        printYellowText(" Dealer Showing " + upCard.getCardValue());
        System.out.println();
        System.out.println();
    }

    void displayInputRequest(){
        System.out.print("Enter 'h' to hit or 's' to stick ");
    }

    void playHands(BJackPlayer player){
        for(BJackHand hand: player.hands) {
            if(hand.handResult == BJackHand.HandResult.PENDING &&
                hand.handAttribute != BJackHand.HandAttribute.BLACKJACK) {
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
                                } else {
                                    displayInputRequest();
                                }
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

    void preGameInit(int numPlayers){
        initializePlayers(numPlayers);
    }

    boolean checkDealerBJack(){
        if (dealerHand.handAttribute == BJackHand.HandAttribute.BLACKJACK){
            for(BJackPlayer player : this.players){
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

    void playDealerHand(){
        while(dealerHand.getHandTotal() < 17){
            dealerHand.drawCard(this.deck);
        }
        if(dealerHand.getHandTotal() > 21){
            dealerHand.handAttribute = BJackHand.HandAttribute.BUST;
        }
        displayAllHands();
    }

    boolean anyActivePlayerHands(){
        for(BJackPlayer player : players) {
            for (BJackHand hand : player.hands) {
                if (hand.handResult == BJackHand.HandResult.PENDING) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean dealerHasBjack(){
        return (dealerHand.handAttribute == BJackHand.HandAttribute.BLACKJACK);
    }

    void setPlayerHandResults(){
        for(BJackPlayer player : players){
            for(BJackHand hand : player.hands){
                setLoseToDealerBJack(hand);
                setWinForPlayerBJack(hand, dealerHand);
                setLoseForBust(hand);
                setWinForDealerBust(hand, dealerHand);
                setHandResultPerTotals(hand);
            }
        }
    }

    void setWinForPlayerBJack(BJackHand playerHand, BJackHand dealerHand){
        if(playerHand.handAttribute == BJackHand.HandAttribute.BLACKJACK &&
            dealerHand.handAttribute != BJackHand.HandAttribute.BLACKJACK){
            playerHand.handResult = BJackHand.HandResult.WIN;
        }
    }

    void setLoseForBust(BJackHand hand){
        if(hand.handAttribute == BJackHand.HandAttribute.BUST){
            hand.handResult = BJackHand.HandResult.LOSE;
        }
    }

    void setWinForDealerBust(BJackHand playerHand, BJackHand dealerHand){
        if(dealerHand.handAttribute == BJackHand.HandAttribute.BUST &&
            playerHand.handResult == BJackHand.HandResult.PENDING){
            playerHand.handResult = BJackHand.HandResult.WIN;
        }
    }

    void setHandResultPerTotals(BJackHand hand){
        int dealerTotal = dealerHand.getHandTotal();
        int playerTotal = hand.getHandTotal();
        if(hand.handResult == BJackHand.HandResult.PENDING) {
            if (playerTotal == dealerTotal) {
                hand.handResult = BJackHand.HandResult.PUSH;
            } else if (playerTotal > dealerTotal) {
                hand.handResult = BJackHand.HandResult.WIN;
            } else {
                hand.handResult = BJackHand.HandResult.LOSE;
            }
        }
    }

    void setLoseToDealerBJack(BJackHand playerHand){
        if(dealerHand.handAttribute == BJackHand.HandAttribute.BLACKJACK){
            if(playerHand.handAttribute == BJackHand.HandAttribute.BLACKJACK){
                playerHand.handResult = BJackHand.HandResult.PUSH;
            } else {
                playerHand.handResult = BJackHand.HandResult.LOSE;
            }
        }
    }

    void payAndCollect(){
        for(BJackPlayer player : players){
            for(BJackHand hand : player.hands){
                assert(hand.handResult != BJackHand.HandResult.PENDING);
                if(hand.handResult == BJackHand.HandResult.LOSE){
                    player.bankroll -= hand.bet;
                }
                if(hand.handResult == BJackHand.HandResult.WIN){
                    if(hand.handAttribute == BJackHand.HandAttribute.BLACKJACK){
                        player.bankroll += (hand.bet * 1.5);
                    } else{
                        player.bankroll += hand.bet;
                    }
                }
            }
        }
    }

    void displayPlayerBankrolls(){
        for (BJackPlayer player : players){
            System.out.println(player.toString() + " bankroll = " + player.bankroll);
        }
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
