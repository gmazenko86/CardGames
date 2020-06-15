import java.io.Console;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;

public class BJackGame extends CardGame {

    final ArrayList<BJackPlayer> players;
    final BJackPlayer dealer;
    BJackHand dealerHand;

    BJackGame(){
        super();
        this.players = new ArrayList<>();
        this.dealer = new BJackPlayer();
        this.dealerHand = dealer.hands.get(0);
    }

    void playGame(){
        preGameInit(1);

        boolean playAnotherHand = true;
        while(playAnotherHand) {
            dealHands();

            // console object will be null if program is run from IDE
            Console console = System.console();

            if (Objects.nonNull(console)) {
                console.printf("Print console object using console.printf " + console.toString() + "\n");
                System.out.println("Print console object using System.out.println " + console.toString());
                PrintWriter writer = console.writer();
                writer.println("Print console object using PrintWriter.println " + console.toString());
            }

            // need an array list of players plus the dealer
            // update hand attributes based on blackjacks
            // TODO: should consolidate the following 2 enhanced for loops into functions
            ArrayList<BJackPlayer> playersPlusDealer = getPlayersPlusDealer();
            for (BJackPlayer player : playersPlusDealer) {
                for (BJackHand hand : player.hands) {
                    hand.setBlackJackFlag();
                }
            }
            // update the hand results based on blackjacks
            for (BJackPlayer player : players) {
                for (BJackHand hand : player.hands) {
                    hand.setResultPerBJacks(dealerHand);
                }
            }

            boolean dealerBlackJack = dealerHasBJack();

            if (!dealerBlackJack) {
                for (BJackPlayer player : this.players) {
                    playHands(player);
                }
            } else {
                setAllPlayerHandResults(dealerHand);
                displayAllHands();
            }

            // now play the dealer hand if there are any active player hands
            if (anyActivePlayerHands()) {
                playDealerHand();
            }

            setAllPlayerHandResults(dealerHand);
            displayResults();
            payAndCollect();
            displayPlayerBankrolls();
            System.out.println("deckindex = " + deck.deckIndex);

            // reinitialize all hands by getting new instances
            // for now, each player gets one hand
            for (BJackPlayer player : playersPlusDealer) {
                player.hands.removeAll(player.hands);
                BJackHand hand = new BJackHand();
                player.hands.add(hand);
            }
            // reset dealerHand variable to the first hand of the last player
            dealerHand = dealer.hands.get(0);
            if(deck.deckIndex > 26){
                deck.shuffle();
                System.out.println("Deck has been shuffled");
            }
            playAnotherHand = playAnotherHand();
        }
        displayFinalResults();
    }

    void setAllPlayerHandResults(BJackHand dealerHand){
        for(BJackPlayer player : players){
            player.setPlayerHandResults(dealerHand);
        }
    }

    void displayFinalResults(){
        for(BJackPlayer player : players){
            System.out.println(player.toString() + " finished with $" + player.bankroll);
        }
    }

    boolean playAnotherHand(){
        Character inputChar = ioMgr.getApprovedInputChar(
                "Enter 'p' to play another hand or 'q' to quit ",
                'p', 'q');
        if(inputChar == 'p'){
            return true;
        }
        if(inputChar == 'q'){
            return false;
        }
        assert(false) : assertPrint("Corrupted input in playAnotherHand()");
        return false;
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
                if(hand.handAttribute == BJackHand.HandAttribute.NONE ||
                    hand.handAttribute == BJackHand.HandAttribute.SPLITHAND){
                    hand.displayHand();
                    System.out.println();
                } else{
                    hand.displayHandWithTotal(false);
                }
            }
        }
    }

    void displayAllHands(){
        System.out.println("*".repeat(40));
        dealerHand.displayHandWithTotal(false);
        for(BJackPlayer player : players){
            for(BJackHand hand : player.hands){
                hand.displayHandWithTotal(false);
            }
        }
    }

    void displayResults(){
        System.out.println("*".repeat(40));
        dealerHand.displayHandWithTotal(false);
        for(BJackPlayer player : players){
            for(BJackHand hand: player.hands){
                hand.displayHandWithTotal(true);
            }
        }
    }

    void displayDealerUpCard(BJackHand hand){
        Card upCard = hand.cards.get(0);
        IOManager.printYellowText(upCard.getCardSignature());
        IOManager.printYellowText(" | ");
        IOManager.printYellowText("X".repeat(10));
        IOManager.printYellowText(" | ");
        IOManager.printYellowText(" Dealer Showing " + upCard.getCardValue());
        System.out.println();
        System.out.println();
    }

    boolean splitPair(){
        displayActiveHands();
        Character inputChar = ioMgr.getApprovedInputChar("Do you want to split the pair?" +
                " 'y' for yes or 'n' for no ", 'y', 'n');
        switch(inputChar) {
            case 'y':
                return true;
            case 'n':
            default : return false;
        }
    }

    void handleSplit(BJackPlayer player, BJackHand hand){
        int handIndex = player.hands.indexOf(hand);
        BJackHand newHand = new BJackHand();
        Card pairCard =  hand.cards.get(1);

        boolean pairAces = hand.pairAces();
        hand.cards.remove(pairCard);
        //TODO: add logic to allow only 1 draw when aces are split
        hand.drawCard(deck);
        hand.handAttribute = BJackHand.HandAttribute.SPLITHAND;
        player.hands.add(handIndex + 1, newHand);
        newHand.cards.add(pairCard);
        newHand.drawCard(deck);
        newHand.handAttribute = BJackHand.HandAttribute.SPLITHAND;
    }

    boolean doubleDown(){
        Character inputChar = ioMgr.getApprovedInputChar("Do you want to double down? " +
                " 'y' for yes or 'n' for no ", 'y', 'n');
        switch(inputChar) {
            case 'y':
                return true;
            case 'n':
            default : return false;
        }
    }

    void playHands(BJackPlayer player){
        for(BJackHand hand: player.hands) {
            if(hand.handResult == BJackHand.HandResult.PENDING &&
                hand.handAttribute != BJackHand.HandAttribute.BLACKJACK) {
                hand.playingThis = true;
                boolean havePair = hand.havePair();
                System.out.println("have pair = " + havePair);
                if(hand.handAttribute == BJackHand.HandAttribute.NONE ||
                    hand.handAttribute == BJackHand.HandAttribute.SPLITHAND){
                    if(havePair){
                        if(splitPair()) {
                            handleSplit(player, hand);
                            // have to start over now that the pair has been split
                            playHands(player);
                            // return once above recursive call completes execution,
                            // since all hands have been played
                            return;
                        }
                    }
                    displayActiveHands();
                    if(hand.checkDoubleDown()){
                        if(doubleDown()){
                            hand.handleDoubleDown(deck);
                        }
                    }
                    if(hand.canHit()) {
                        char inputChar;
                        do {
                            inputChar = ioMgr.getApprovedInputChar(
                                    "Enter 'h' to hit or 's' to stick ", 'h', 's');
                            switch (inputChar) {
                                case 'h':
                                    hand.drawCard(deck);
                                    // use displayActiveHands() instead of displayAllHands when the dealer
                                    // hole card should not yet be shown
                                    displayActiveHands();
                                    if (hand.getHandTotal() > 21) {
                                        hand.handAttribute = BJackHand.HandAttribute.BUST;
                                        hand.setLoseForBust();
                                        displayActiveHands();
                                    }
                                    break;
                                case 's':
                                    hand.handAttribute = BJackHand.HandAttribute.STICK;
                                    displayActiveHands();
                                    break;
                                default:
                                    break;
                            }
                        } while (inputChar != 's' && hand.handAttribute != BJackHand.HandAttribute.BUST);
                    }
                }
                hand.playingThis = false;
            }
        }
    }

    void preGameInit(int numPlayers){
        initializePlayers(numPlayers);
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
            if(player.anyActiveHands()){
                return true;
            }
        }
        return false;
    }

    boolean dealerHasBJack(){
        return (dealerHand.handAttribute == BJackHand.HandAttribute.BLACKJACK);
    }

    void payAndCollect(){
        for(BJackPlayer player : players){
            for(BJackHand hand : player.hands){
                assert(hand.handResult != BJackHand.HandResult.PENDING) :
                        assertPrint("handResult should not still be PENDING");
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

    boolean assertPrint(String string){
        System.out.println(string);
        return true;
    }
}
