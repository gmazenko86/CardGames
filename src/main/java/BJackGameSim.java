public class BJackGameSim extends BJackGame{
    int iterations;
    int gamesPlayed;

    BJackGameSim(int iterations){
        super();
        this.iterations = iterations;
        gamesPlayed = 0;
    }
/*
    @Override
    void displayActiveHands() {
    }

    @Override
    void displayAllHands() {
    }

    @Override
    void displayResults() {
    }
*/
    @Override
    boolean playAnotherHand() {
        gamesPlayed += 1;
        return gamesPlayed < iterations;
    }

    @Override
    boolean hitHand(BJackHand hand) {
        Card dealerCard =  dealerUpCard();
        int upValue = dealerCard.getCardValue();
        int playerTotal = hand.getHandTotal();
        int hardStandTotal = getHardStandTot(upValue);
        int softStandTotal = getSoftStandTotal(upValue);
        boolean hitFlag = false;
        if(isSoftHand(hand)){
            if(playerTotal < softStandTotal){
                hitFlag = true;
            }
        } else if(playerTotal < hardStandTotal){
             hitFlag = true;
        }
        if(hitFlag){
            System.out.println("Hit hand");
        } else{
            System.out.println("Stick");
        }
        return hitFlag;
    }

    @Override
    boolean doubleDown(BJackHand hand) {
        Card dealerCard =  dealerUpCard();
        int upValue = dealerCard.getCardValue();
        int playerTotal = hand.getHandTotal();
        boolean returnFlag = false;
        // first handle hard totals
        if(hand.faceCount(Card.CardFace.ACE) == 0) {
            if (playerTotal == 11) {
                returnFlag = true;
            }
            if (playerTotal == 10 && upValue < 10) {
                returnFlag = true;
            }
            if (playerTotal == 9 && (2 <= upValue && upValue <= 6)) {
                returnFlag = true;
            }
            if (returnFlag) {
                System.out.println("Double down");
            }
        }
        //TODO: handle soft totals
        return  returnFlag;
    }

    @Override
    boolean splitPair() {
        return true;
    }

    @Override
    void displayPlayerBankrolls() {
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

    boolean isSoftHand(BJackHand hand){
        int sumCardValues = 0;
        int numAces = hand.faceCount(Card.CardFace.ACE);
        for(Card card : hand.cards){
            sumCardValues += card.getCardValue();
        }
        if (numAces > 0 &&
                // next condition means that an ACE is used as an 11
                (hand.getHandTotal() != (sumCardValues - numAces*10))){
            return true;
        }
        return false;
    }
}
