import java.util.ArrayList;

public class Hand {
    ArrayList<Card> cards;

    Hand(){
        this.cards = new ArrayList<>();
    }

    void drawCard(Deck deck){
        cards.add(deck.getCard());
    }
}
