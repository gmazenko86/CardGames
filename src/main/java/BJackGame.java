import java.io.Console;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

//TODO: create a mechanism for logging results of each hand
//TODO: enable player decision logic/recommendation (this will be needed for simulation)
//TODO: enable running in a simulation environment with no display
//TODO: enable logging of results to a dbase


public class BJackGame extends CardGame {

    final ArrayList<BJackPlayer> players;
    final BJackPlayer dealer;
    BJackHand dealerHand;
    ArrayList<ResultsEntry> dealerResults;
    ArrayList<ResultsEntry> playerResults;

    BJackGame(){
        super();
        this.players = new ArrayList<>();
        this.dealer = new BJackPlayer();
        this.dealerHand = dealer.hands.get(0);
        this.dealerResults = new ArrayList<>();
        this.playerResults = new ArrayList<>();
    }

    void playGame(){
        preGameInit(1);

        boolean playAnotherHand = true;
        int hashCode;
        LocalDateTime dateTime;
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

            boolean dealerBlackJack = dealerHand.haveBJack();

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
            dateTime = LocalDateTime.now();
            hashCode = dateTime.hashCode();
            logResults(hashCode);

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
        System.out.println("Dealer Results");
        displayResultsArray(dealerResults);
        System.out.println("Player Results");
        displayResultsArray(playerResults);
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
        dealerHand.displayDealerUpCard();
        for (BJackPlayer BJackPlayer : this.players) {
            for (BJackHand hand : BJackPlayer.hands) {
                if(hand.notPlayed() || hand.isSplitHand()){
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

    void handleSplit(BJackPlayer player, BJackHand hand){
        int handIndex = player.hands.indexOf(hand);
        BJackHand newHand = new BJackHand();
        Card pairCard =  hand.cards.get(1);

        boolean pairAces = hand.pairAces();
        hand.cards.remove(pairCard);
        hand.drawCard(deck);
        hand.setSplit();
        player.hands.add(handIndex + 1, newHand);
        newHand.cards.add(pairCard);
        newHand.drawCard(deck);
        newHand.setSplit();
    }

    void playHands(BJackPlayer player){
        for(BJackHand hand: player.hands) {
            if(hand.resultPending() && (!hand.haveBJack())){
                hand.setPlaying(true);
                if(hand.notPlayed() || hand.isSplitHand()){
                    if(hand.havePair()){
                        displayActiveHands();
                        if(hand.splitPair()) {
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
                        if(hand.doubleDown()){
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
                                        hand.setBust();
                                        hand.setLoseForBust();
                                        displayActiveHands();
                                    }
                                    break;
                                case 's':
                                    hand.setStick();
                                    displayActiveHands();
                                    break;
                                default:
                                    break;
                            }
                        } while (inputChar != 's' && (!hand.isBust()));
                    }
                }
                hand.setPlaying(false);
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

    //TODO: refactor this so it does not directly access hand enums
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

    void logResults(int hashCode){
        ResultsEntry dealerEntry = dealerHand.getResultsEntry(hashCode);
        dealerResults.add(dealerEntry);
        for(BJackPlayer player : players){
            for(BJackHand hand : player.hands){
                ResultsEntry playerEntry = hand.getResultsEntry(hashCode);
                playerResults.add(playerEntry);
            }
        }
    }

    void displayResultsArray(ArrayList<ResultsEntry> resultsArray){
        for(ResultsEntry entry : resultsArray){
            System.out.format("%-12.12s", entry.handHashId);
            System.out.format("%-4.4s", entry.handTotal);
            System.out.format("%-12.12s", entry.handAttribute.name());
            System.out.format("%-8.8s", entry.handResult.name());

            for(Card card : entry.cards){
                System.out.format("%-7.7s", card.cardFace.name());
            }
            System.out.print("\n");
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
