import java.util.ArrayList;

public class BJackPlayer {
    // a player can sometimes play more than one hand
    ArrayList<BJackHand> hands;
    double bankroll;

    // in the constructor, instantiate the array list and populate it with one empty hand
    BJackPlayer() {
        hands = new ArrayList<>();
        hands.add(new BJackHand());
        this.bankroll = 2000.;
    }

}
