package cards;

import java.util.ArrayList;

public class Hand {
    final public ArrayList<Card> cards;

    public Hand(){
        this.cards = new ArrayList<>();
    }

    public void drawCard(Deck deck){
        cards.add(deck.getNextCard());
    }

    public int faceCount(Card.CardFace face){
        int faceCount = 0;
        for(Card card : this.cards){
            if(card.cardFace == face){
                faceCount += 1;
            }
        }
        return faceCount;
    }
}
