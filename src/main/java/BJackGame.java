import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

//TODO: confirm running in a simulation environment with no display (player seems to be doing too well)
//TODO: enable logging of results to a dbase - determine dbase strategy
//TODO: unit testing framework and test cases
//TODO: system level testing
//TODO: write graphics front end


public class BJackGame extends CardGame {

    final ArrayList<BJackPlayer> players;
    final BJackPlayer dealer;
    BJackHand dealerHand;
    ArrayList<ResultsEntry> dealerResults;
    ArrayList<ResultsEntry> playerResults;
    IOMgr iom;

    BJackGame(){
        super();
        this.players = new ArrayList<>();
        this.dealer = new BJackPlayer();
        this.dealerHand = dealer.hands.get(0);
        this.dealerResults = new ArrayList<>();
        this.playerResults = new ArrayList<>();
        this.iom = new IOMgr();
    }

    void playGame(){
        preGameInit(1);

        boolean playAnotherHand = true;
        int hashCode;
        LocalDateTime dateTime;
        while(playAnotherHand) {
            dealHands();
/*
            // console object will be null if program is run from IDE
            Console console = System.console();

            if (Objects.nonNull(console)) {
                console.printf("Print console object using console.printf " + console.toString() + "\n");
                System.out.println("Print console object using System.out.println " + console.toString());
                PrintWriter writer = console.writer();
                writer.println("Print console object using PrintWriter.println " + console.toString());
            }
*/
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
                iom.displayAllHands();
            }

            // now play the dealer hand if there are any active player hands
            if (anyActivePlayerHands()) {
                playDealerHand();
            }

            setAllPlayerHandResults(dealerHand);
            iom.displayResults();
            payAndCollect();
            displayPlayerBankrolls();
            dateTime = LocalDateTime.now();
            hashCode = dateTime.hashCode();
            logResults(hashCode);

            // reinitialize all hands by getting new instances
            for (BJackPlayer player : playersPlusDealer) {
                player.reinitHands();
            }
            // reset dealerHand variable to the first hand of the last player
            dealerHand = dealer.hands.get(0);
            if(deck.deckIndex > 26){
                deck.shuffle();
            }
            playAnotherHand = playAnotherHand();
        }
        iom.displayFinalResults();
//        System.out.println("Dealer Results");
//        displayResultsArray(dealerResults);
//        System.out.println("Player Results");
//        displayResultsArray(playerResults);
    }

    void setAllPlayerHandResults(BJackHand dealerHand){
        for(BJackPlayer player : players){
            player.setPlayerHandResults(dealerHand);
        }
    }

    boolean playAnotherHand(){
        Character inputChar = iom.getApprovedInputChar(
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

    void handleSplit(BJackPlayer player, BJackHand hand){
        int handIndex = player.hands.indexOf(hand);
        BJackHand newHand = new BJackHand();
        Card pairCard =  hand.cards.get(1);

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
                        iom.displayActiveHands();
                        if(splitPair(hand)) {
                            handleSplit(player, hand);
                            // have to start over now that the pair has been split
                            playHands(player);
                            // return once above recursive call completes execution,
                            // since all hands have been played
                            return;
                        }
                    }
                    iom.displayActiveHands();
                    if(hand.checkDoubleDown()){
                        if(doubleDown(hand)){
                            hand.handleDoubleDown(deck);
                        }
                    }
                    if(hand.canHit()) {
                        boolean hitHand;
                        do {
                            hitHand = hitHand(hand);
                            if(hitHand) {
                                hand.drawCard(deck);
                                // use displayActiveHands() instead of displayAllHands when the dealer
                                // hole card should not yet be shown
                                iom.displayActiveHands();
                                if (hand.getHandTotal() > 21) {
                                    hand.setBust();
                                    hand.setLoseForBust();
                                    iom.displayActiveHands();
                                }
                            } else{
                                hand.setStick();
                                iom.displayActiveHands();
                            }
                        } while (hitHand && (!hand.isBust()));
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
        iom.displayAllHands();
    }

    boolean hitHand(BJackHand hand){
        char inputChar = iom.getApprovedInputChar(
                "Enter 'h' to hit or 's' to stick ", 'h', 's');
        if(inputChar == 'h'){return true;}
        if(inputChar == 's'){return false;}
        assert(false);
        return false;
    }

    boolean doubleDown(BJackHand hand){
        Character inputChar = iom.getApprovedInputChar("Do you want to double down? " +
                " 'y' for yes or 'n' for no ", 'y', 'n');
        switch(inputChar) {
            case 'y':
                return true;
            case 'n':
            default : return false;
        }
    }

    boolean splitPair(BJackHand hand){
        Character inputChar = iom.getApprovedInputChar("Do you want to split the pair?" +
                " 'y' for yes or 'n' for no ", 'y', 'n');
        switch(inputChar) {
            case 'y':
                return true;
            case 'n':
            default : return false;
        }
    }

    Card dealerUpCard(){
        return dealerHand.cards.get(0);
    }

    boolean anyActivePlayerHands(){
        for(BJackPlayer player : players) {
            if(player.anyActiveHands()){
                return true;
            }
        }
        return false;
    }

    int getHardStandTot(int dealerShows){
        switch (dealerShows){
            case 11:
            case 10:
            case 9:
            case 8:
            case 7:
                return 17;
            case 6:
            case 5:
            case 4:
                return 12;
            case 3:
            case 2:
                return 13;
            default:
                assert(false);
        }
        return -1;
    }

    int getSoftStandTotal(int dealerShows){
        if(dealerShows == 9 || dealerShows == 10){
            return 19;
        }
        return  18;
    }

    boolean getHardHitRec(BJackHand hand){
        Card dealerCard =  dealerUpCard();
        int upValue = dealerCard.getCardValue();
        int playerTotal = hand.getHandTotal();
        int hardStandTotal = getHardStandTot(upValue);
        boolean hitFlag = false;

        if(!hand.isSoftHand()){
            if(playerTotal < hardStandTotal){
                hitFlag = true;
            }
        }
        return hitFlag;
    }

    boolean getSoftHitRec(BJackHand hand){
        Card dealerCard =  dealerUpCard();
        int upValue = dealerCard.getCardValue();
        int playerTotal = hand.getHandTotal();
        int softStandTotal = getSoftStandTotal(upValue);
        boolean hitFlag = false;

        if(hand.isSoftHand()) {
            if (playerTotal < softStandTotal) {
                hitFlag = true;
            }
        }
        return hitFlag;
    }

    boolean getHardDoubleRec(BJackHand hand){
        Card dealerCard =  dealerUpCard();
        int upValue = dealerCard.getCardValue();
        int playerTotal = hand.getHandTotal();
        boolean returnFlag = false;
        if(!hand.isSoftHand()) {
            if (playerTotal == 11 ||
                playerTotal == 10 && upValue < 10 ||
                playerTotal == 9 && (2 <= upValue && upValue <= 6)){
                returnFlag = true;
            }
        }
        return returnFlag;
    }

    boolean getSoftDoubleRec(BJackHand hand){
        Card dealerCard =  dealerUpCard();
        int upValue = dealerCard.getCardValue();
        int playerTotal = hand.getHandTotal();
        boolean returnFlag = false;
        if(hand.isSoftHand()) {
            switch (upValue){
                case 2:
                    if(playerTotal == 17){returnFlag = true;}
                    break;
                case 3:
                    if(playerTotal == 17 || playerTotal == 18){returnFlag = true;}
                    break;
                case 4:
                case 5:
                case 6: if(13 <= playerTotal && playerTotal <= 18){returnFlag = true;}
                    break;
                default:
                    break;
            }
        }
        return returnFlag;
    }

    boolean getSplitPairRec(BJackHand hand){
        Card dealerCard =  dealerUpCard();
        int upValue = dealerCard.getCardValue();
        int pairValue = hand.pairCardValue();
        boolean returnFlag = false;

        if(hand.havePair()) {
            switch(pairValue){
                case 11:
                case 8:
                    returnFlag = true;
                case 9:
                    if(2 <= upValue && upValue <= 9 && upValue != 7){returnFlag = true;}
                case 7:
                    if(2 <= upValue && upValue <= 8){ returnFlag = true;}
                case 6:
                case 3:
                case 2:
                    if(2 <= upValue && upValue <= 7){ returnFlag = true;}
                case 4:
                    if(upValue == 5){returnFlag = true;}
                default:
                    break;
            }
        }
        return returnFlag;
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

    class IOMgr{
        Character getApprovedInputChar(String inputString, char... array){
            System.out.print(inputString);

            boolean foundChar;
            Character returnChar = null;

            do {
                try {
                    byte[] bytes = new byte[256];
                    System.in.read(bytes);
                    // only interested in the first character input by the user
                    returnChar = Character.valueOf((char) bytes[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                foundChar = arrayContains(returnChar, array);
                if (!foundChar) {
                    System.out.print("Invalid input: " + inputString);
                }
            } while(!foundChar);
            assert (Objects.nonNull(returnChar));
            return returnChar;
        }

        boolean arrayContains(char toCheck, char[] array){
            for(char character : array){
                if(character == toCheck){
                    return true;
                }
            }
            return false;
        }

        void displayFinalResults(){
            for(BJackPlayer player : players){
                System.out.println(player.toString() + " finished with $" + player.bankroll);
            }
        }

        void displayActiveHands(){
            System.out.println("*".repeat(40));
            displayDealerUpCard(dealerHand);
            for (BJackPlayer BJackPlayer : players) {
                for (BJackHand hand : BJackPlayer.hands) {
                    if(hand.notPlayed() || hand.isSplitHand()){
                        displayHand(hand);
                        System.out.println();
                    } else{
                        displayHandWithTotal(hand,false);
                    }
                }
            }
        }

        void displayAllHands(){
            System.out.println("*".repeat(40));
            displayHandWithTotal(dealerHand, false);
            for(BJackPlayer player : players){
                for(BJackHand hand : player.hands){
                    displayHandWithTotal(hand,false);
                }
            }
        }

        void displayResults(){
            System.out.println("*".repeat(40));
            displayHandWithTotal(dealerHand, false);
            for(BJackPlayer player : players){
                for(BJackHand hand: player.hands){
                    displayHandWithTotal(hand,true);
                }
            }
        }

        protected void displayHand(BJackHand hand){
            for(Card card: hand.cards){
                if (hand.playingThis){
                    MyIOUtils.printBlueText(card.getCardSignature());
                    MyIOUtils.printBlueText(" | ");
                } else{
                    displayCardSignature(card);
                    System.out.print(" | ");
                }
            }
            System.out.print("");
        }

        protected void displayHandWithTotal(BJackHand hand, boolean printResults){
            displayHand(hand);
            System.out.print("Total is " + hand.getHandTotal());

            if(hand.isBust()){
                MyIOUtils.printRedText(" ::: BUST");
            }
            if(hand.haveBJack()){
                MyIOUtils.printGreenText(" ::: BLACKJACK");
            }
            if(printResults){
                if(hand.isWin()){
                    MyIOUtils.printGreenText(" -----Player result = WIN");
                } else if(hand.isLose()){
                    MyIOUtils.printRedText(" -----Player result = LOSE");
                } else if(hand.isPush()){
                    MyIOUtils.printBlueText(" -----Player result = PUSH");
                }
                else{ assert(false);}
            }
            System.out.println();
        }

        protected void displayDealerUpCard(BJackHand dealerHand){
            Card upCard = dealerHand.cards.get(0);
            MyIOUtils.printYellowText(upCard.getCardSignature());
            MyIOUtils.printYellowText(" | ");
            MyIOUtils.printYellowText("X".repeat(10));
            MyIOUtils.printYellowText(" | ");
            MyIOUtils.printYellowText(" Dealer Showing " + upCard.getCardValue());
            System.out.println();
            System.out.println();
        }

        protected void displayCardSignature(Card card){
            System.out.print(card.getCardSignature());
        }

        void displayDeck(Deck deck){
            for (Card card : deck.cards) {
                displayCardSignature(card);
                System.out.print("| ");
            }
            System.out.println("\n");
        }
    }
}
