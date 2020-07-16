package bjack;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import cards.Card;
import cards.CardGame;
import cards.Deck;
import cards.Hand;
import myioutils.MyIOUtils;
import mypostgre.MyPostGreSqlClass;

//TODO: confirm running in a simulation environment with no display (player seems to be doing too well)
//TODO: system level testing
//TODO: consider using NullPrintStream in MyPostGreSqlClass to suppress stack trace from ConnectException
//TODO: write graphics front end


public class BJackGame extends CardGame {

    public final ArrayList<BJackPlayer> players;
    final BJackPlayer dealer;
    public BJackHand dealerHand;
    final ArrayList<ResultsEntry> dealerResults;
    final ArrayList<ResultsEntry> playerResults;
    final String dbConfigPath;
    IOMgr iom;
    public DBMgr dbMgr;
    protected final boolean validDbConnection;

    public BJackGame(String dbConfigPath){
        super();
        this.players = new ArrayList<>();
        this.dealer = new BJackPlayer();
        this.dealerHand = dealer.hands.get(0);
        this.dealerResults = new ArrayList<>();
        this.playerResults = new ArrayList<>();
        this.dbConfigPath = dbConfigPath;
        this.iom = new IOMgr();
        this.dbMgr = new DBMgr(dbConfigPath);
        if (Objects.isNull(this.dbMgr.conn)){
            this.validDbConnection = false;
        }
        else this.validDbConnection = true;
        if(validDbConnection){
            this.dbMgr = new DBMgrRO(dbConfigPath);
        }
    }

    public void playGameWrapper(){
        playGame();
    }

    public void playGame(){
        LocalDateTime timeStamp = LocalDateTime.now();
        System.out.println(timeStamp + " = start of program");
        preGameInit(1);

        boolean playAnotherHand = true;
        int hashCode;
        HashSet<Integer> hashCodes = new HashSet<>();
        boolean notDuplicate;
        LocalDateTime dateTime;
        while(playAnotherHand) {
            dealHands();

            // need an array list of players plus the dealer
            // update hand attributes based on blackjacks
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

            // check for a duplicate hashcode. If not dup, then log the results
            notDuplicate = hashCodes.add(hashCode);
            // check to make sure the max hand length is <= 8
            int maxCards = 0;
            for(BJackPlayer player : playersPlusDealer){
                for(BJackHand hand : player.hands){
                    if(hand.cards.size() > maxCards){
                        maxCards = hand.cards.size();
                    }
                }
            }

            if(notDuplicate && (maxCards <= 8)){
                logResults(hashCode);
            }

            // reinitialize all hands by getting new instances
            postHandReInit();

            playAnotherHand = playAnotherHand();
        }
        timeStamp = LocalDateTime.now();
        System.out.println(timeStamp + " = done playing hands");
        iom.displayFinalResults();


//        System.out.println("Dealer Results");
//        displayResultsArray(dealerResults);
//        System.out.println("cards.Player Results");
//        displayResultsArray(playerResults);
        dbMgr.writeResultsDbase();
        // clear the results arrays in case we're doing iterations of iterations
        clearResultsArrays();
    }

    void clearResultsArrays(){
        dealerResults.clear();
        playerResults.clear();
    }

    public void postHandReInit(){
        ArrayList<BJackPlayer> playersPlusDealer = getPlayersPlusDealer();
        for (BJackPlayer player : playersPlusDealer) {
            player.reinitHands();
        }
        // reset dealerHand variable to the first hand of the last player
        dealerHand = dealer.hands.get(0);
        if(deck.deckIndex > 26){
            deck.shuffle();
        }
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
        int playersNeeded;
        int currentNumPlayers = players.size();
        playersNeeded = numPlayers - currentNumPlayers;
        for (int i = 0; i < playersNeeded; i++){
            BJackPlayer bJackPlayer = new BJackPlayer();
            players.add(bJackPlayer);
        }
    }

    public void dealHands(){
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

    public void preGameInit(int numPlayers){
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
        char inputChar;

        inputChar = iom.getApprovedInputChar(
                "Enter 'h' to hit or 's' to stick or 'a' for advice ", 'h', 's', 'a');

        if(inputChar == 'a'){
            boolean recFlag;
            if(hand.isSoftHand()){
              recFlag = getSoftHitRec(hand);
            } else{
                recFlag = getHardHitRec(hand);
            }
            if(recFlag){
                iom.displayAdvice("Recommendation: Hit");
            } else{
                iom.displayAdvice("Recommendation: Stick");
            }
            // provide outcome probabilities if deciding based on first 2 cards
            if(hand.cards.size() == 2){
                iom.displayProbabilities(hand);
            }
            // have to recursively call hitHand() until user chooses 'h' or 's'
            // have to return hitHand() so call stack is properly unwound
            return hitHand(hand);
        }
        if(inputChar == 'h'){
            return true;
        } else if(inputChar == 's'){
            return false;
        } else{
            assert (false);
            return false;
        }
    }

    boolean doubleDown(BJackHand hand){
        Character inputChar = iom.getApprovedInputChar("Do you want to double down? " +
                " 'y' for yes or 'n' for no or 'a' for advice ", 'y', 'n', 'a');
        switch(inputChar) {
            case 'a': {
                boolean recFlag;
                if(hand.isSoftHand()){
                    recFlag = getSoftDoubleRec(hand);
                } else{
                    recFlag = getHardDoubleRec(hand);
                }
                if(recFlag){
                    iom.displayAdvice("Recommendation: Double Down");
                } else{
                    iom.displayAdvice("Recommendation: Do NOT double down");
                }
                if(hand.cards.size() == 2){
                    iom.displayProbabilities(hand);
                }
                // have to recursively call doubleDown() until user chooses 'y' or 'n'
                // have to return doubleDown() so call stack is properly unwound
                return doubleDown(hand);
            }
            case 'y':
                return true;
            case 'n':
            default : return false;
        }
    }

    boolean splitPair(BJackHand hand){
        Character inputChar = iom.getApprovedInputChar("Do you want to split the pair?" +
                " 'y' for yes or 'n' for no or 'a' for advice ", 'y', 'n', 'a');
        switch(inputChar) {
            case 'a': {
                boolean recFlag;
                assert (hand.havePair()) : assertPrint("Should not ask for split advice with no pair");
                recFlag = getSplitPairRec(hand);
                if(recFlag){
                    iom.displayAdvice("Recommendation: Split the pair");
                } else{
                    iom.displayAdvice("Recommendation: Do NOT split the pair");
                }
                if(hand.cards.size() == 2){
                    iom.displayProbabilities(hand);
                }
                // have to recursively call splitPair() until user chooses 'y' or 'n'
                // have to return splitPair() so call stack is properly unwound
                return splitPair(hand);
            }            case 'y':
                return true;
            case 'n':
            default : return false;
        }
    }

    public Card dealerUpCard(){
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

    public boolean getHardHitRec(BJackHand hand){
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

    public boolean getSoftHitRec(BJackHand hand){
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

    public boolean getHardDoubleRec(BJackHand hand){
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

    public boolean getSoftDoubleRec(BJackHand hand){
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
                case 4: if(13 <= playerTotal && playerTotal <= 18){returnFlag = true;}
                    break;
                case 5:
                case 6: if(12 <= playerTotal && playerTotal <= 18){returnFlag = true;}
                    break;
                default:
                    break;
            }
        }
        return returnFlag;
    }

    public boolean getSplitPairRec(BJackHand hand){
        Card dealerCard =  dealerUpCard();
        int upValue = dealerCard.getCardValue();
        int pairValue = hand.pairCardValue();
        boolean returnFlag = false;

        if(hand.havePair()) {
            switch(pairValue){
                case 11:
                case 8:
                    returnFlag = true;
                    break;
                case 9:
                    if(2 <= upValue && upValue <= 9 && upValue != 7){
                        returnFlag = true;
                    }
                    break;
                case 7:
                    if(2 <= upValue && upValue <= 8){ returnFlag = true;}
                    break;
                case 6:
                case 3:
                case 2:
                    if(2 <= upValue && upValue <= 7){ returnFlag = true;}
                    break;
                case 4:
                    if(upValue == 5){returnFlag = true;}
                    break;
                default:
                    break;
            }
        }
        return returnFlag;
    }

    void payAndCollect(){
        for(BJackPlayer player : players){
            for(BJackHand hand : player.hands){
                assert(!hand.resultPending()) :
                        assertPrint("handResult should not still be PENDING");
                if(hand.isLose()){
                    player.adjustBankroll(-hand.getBet());
                }
                if(hand.isWin()){
                    if(hand.haveBJack()){
                        player.adjustBankroll(hand.getBet() * 1.5);
                    } else{
                        player.adjustBankroll(hand.getBet());
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

    public boolean assertPrint(String string){
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
                if (hand.getPlaying()){
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
                    MyIOUtils.printGreenText(" -----cards.Player result = WIN");
                } else if(hand.isLose()){
                    MyIOUtils.printRedText(" -----cards.Player result = LOSE");
                } else if(hand.isPush()){
                    MyIOUtils.printBlueText(" -----cards.Player result = PUSH");
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

        void displayAdvice(String adMsg){
            System.out.println(adMsg);
        }

        void displayProbabilities(BJackHand hand){
            ProbabilityStruct ps = dbMgr.getProb(hand);
            if(Objects.isNull(ps)){
                System.out.println("Probabilities not available");
                return;
            }

            double winProb = 100. * ps.wins/ps.total;
            double pushProb = 100. * ps.pushes/ps.total;
            double lossProb = 100. * ps.losses/ps.total;
            String sf1 = String.format("Probabilities: Win=%4.1f" + "%% ", winProb);
            String sf2 = String.format("Push=%4.1f" + "%% ", pushProb);
            String sf3 = String.format("Lose=%4.1f" + "%% ", lossProb);
            System.out.println(sf1 + sf2 + sf3);
        }
    }

    public class DBMgr extends MyPostGreSqlClass {
        DBMgr(String configFilePath) {
            super(configFilePath);
        }

        void writeResultsDbase(){
        }

        public String buildTableName(BJackHand dealerHand, BJackHand playerHand){
            int dealerValue = getTableNameInt(dealerHand.cards.get(0));
            // table names contain the value of the smaller card first
            int temp1 = getTableNameInt(playerHand.cards.get(0));
            int temp2 = getTableNameInt(playerHand.cards.get(1));
            int playerVal1 = Math.min(temp1, temp2);
            int playerVal2 = Math.max(temp1, temp2);


            return "d" + dealerValue +
                    "p" + playerVal1 + "_" + playerVal2;
        }

        int getTableNameInt(Card card){
            // want to use '1' for aces when creating table names;
            int cardValue = card.getCardValue();
            if(cardValue == 11){
                cardValue = 1;
            }
            return cardValue;
        }

        ProbabilityStruct getAdviceData(String tableName){
            return null;
        }

        ProbabilityStruct getProb(BJackHand hand) {
            String tableName = buildTableName(dealerHand, hand);
            return getAdviceData(tableName);
        }
    }

    class DBMgrRO extends DBMgr{
        DBMgrRO (String configFilePath) {
            super(configFilePath);
        }

        @Override
        ProbabilityStruct getAdviceData(String tableName) {

            String sqlString = "select count(hashid), 'total' as desc from " + tableName +
            " where dattrib != 'BLACKJACK'\n" +
            "union\n" +
            "select count(pattrib), 'wins' as desc from " + tableName +
            " where presult = 'WIN' and pattrib != 'BLACKJACK'\n" +
            "union\n" +
            "select count(pattrib), 'pushes' as desc from " + tableName+ " where presult = 'PUSH'\n" +
            "union\n" +
            "select count(pattrib), 'losses' as desc from " + tableName +
            " where presult = 'LOSE' and dattrib != 'BLACKJACK';";

            ProbabilityStruct probStruct = new ProbabilityStruct();

            try(Statement statement = getStatementScrollable()){
                ResultSet resultSet = statement.executeQuery(sqlString);
                // "the first call to the method next makes the first row the current row"
                while (resultSet.next()){
                    StringBuilder label = new StringBuilder();
                    label.append(resultSet.getString("desc"));
                    int count = resultSet.getInt("count");
                    switch(label.toString()){
                        case "total" :
                            probStruct.total = count;
                            break;
                        case "losses" :
                            probStruct.losses = count;
                            break;
                        case "wins" :
                            probStruct.wins = count;
                            break;
                        case "pushes" :
                            probStruct.pushes = count;
                        default:
                            break;
                    }
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
            return probStruct;
        }
    }
}

