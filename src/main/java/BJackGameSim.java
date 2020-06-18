public class BJackGameSim extends BJackGame{
    int iterations;
    int gamesPlayed;

    BJackGameSim(int iterations){
        super();
        IOMgrSim ioMgrSim = new IOMgrSim();
        this.iom = (IOMgr) ioMgrSim;
        this.iterations = iterations;
        gamesPlayed = 0;
    }

    // this extends the nested class IOMgr from the parent class BJackGame
    // to use the override functions below, this.iom has to be
    // assigned to the ioMgrSim instance created in the constructor
    class IOMgrSim extends IOMgr {
        @Override
        void displayActiveHands() {
        }

        @Override
        void displayAllHands() {
        }

        @Override
        void displayResults() {
        }

    }

    @Override
    boolean playAnotherHand() {
        gamesPlayed += 1;
        return gamesPlayed < iterations;
    }

    @Override
    boolean hitHand(BJackHand hand) {
        boolean hitFlag = false;

        if(hand.isSoftHand()){
            if(getSoftHitRec(hand)){
                hitFlag = true;
            }
        } else if(getHardHitRec(hand)){
             hitFlag = true;
        }
        return hitFlag;
    }

    @Override
    boolean doubleDown(BJackHand hand) {

        boolean returnFlag = false;
        // first handle hard totals
        if(!hand.isSoftHand()) {
            if (getHardDoubleRec(hand)){
                returnFlag = true;
            }
        } else{
            if(getSoftDoubleRec(hand)){
                returnFlag = true;
            }
        }
        return  returnFlag;
    }

    @Override
    boolean splitPair(BJackHand hand) {
        boolean recommendation;
        recommendation = getSplitPairRec(hand);
        return recommendation;
    }

    @Override
    void displayPlayerBankrolls() {
    }


}
