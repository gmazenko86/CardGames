package bjack;

import cards.Player;

import java.util.ArrayList;

public class BJackPlayer extends Player {
    // a player can sometimes play more than one hand
    public final ArrayList<BJackHand> hands;

    // in the constructor, instantiate the array list and populate it with one empty hand
    BJackPlayer() {
        hands = new ArrayList<>();
        hands.add(new BJackHand());
        this.bankroll = 2000.;
    }

    void setPlayerHandResults(BJackHand dealerHand){
        for(BJackHand hand : this.hands){
            hand.setLoseForBust();
            hand.setWinForDealerBust(dealerHand);
            hand.setResultPerTotals(dealerHand);
        }
    }

    boolean anyActiveHands(){
        for (BJackHand hand : this.hands) {
            if (hand.handResult == BJackHand.HandResult.PENDING) {
                return true;
            }
        }
        return false;
    }

    void reinitHands(){
        // remove all hands from the player's array list of hands
        this.hands.clear();
        // TODO: each player gets one hand for now. will eventually
        //  have to recreate the number they started with once a user option
        //  to choose multiple player hands is added
        BJackHand newHand = new BJackHand();
        this.hands.add(newHand);
    }

}
