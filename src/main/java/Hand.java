import java.util.ArrayList;

public class Hand {
    final ArrayList<Card> cards;

    Hand(){
        this.cards = new ArrayList<>();
    }

    void drawCard(Deck deck){
        cards.add(deck.getCard());
    }

    int faceCount(Card.CardFace face){
        int faceCount = 0;
        for(Card card : this.cards){
            if(card.cardFace == face){
                faceCount += 1;
            }
        }
        return faceCount;
    }
}
