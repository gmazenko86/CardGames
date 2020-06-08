import java.util.ArrayList;

public class Player {
    // a player can sometimes play more than one hand
    ArrayList<Hand> hands;

    // in the constructor, instantiate the array list and populate it with one empty hand
    Player() {
        hands = new ArrayList<>();
        hands.add(new Hand());
    }

}
