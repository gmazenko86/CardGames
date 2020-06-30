import java.util.ArrayList;

public class DeckBySuit extends Deck {
    DeckBySuit(){
        super();
        this.deckIndex = 0;
        this.cards.clear();
        Card.Suit[] cardSuit = Card.Suit.values();
        Card.CardFace[] cardFace = Card.CardFace.values();
        for(Card.Suit suit : cardSuit){
            for (Card.CardFace face : cardFace){
                Card newCard = new Card(face, suit);
                (this.cards).add(newCard);
            }
        }
    }
}
