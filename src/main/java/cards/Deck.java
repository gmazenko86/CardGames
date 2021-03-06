package cards;

import java.util.ArrayList;

public class Deck {
    final public ArrayList<Card> cards;
    public int deckIndex;

    public Deck(){
        this.deckIndex = 0;
        this.cards = new ArrayList<>();
        Card.CardFace[] cardFace = Card.CardFace.values();
        Card.Suit[] cardSuit = Card.Suit.values();
        for(Card.CardFace face: cardFace){
            for (Card.Suit suit : cardSuit){
                Card newCard = new Card(face, suit);
                (this.cards).add(newCard);
            }
        }
    }

    public void shuffle(){
        int numCards = this.cards.size();
        ArrayList<Integer> sortOrder = CardGame.getIndexRandOrder(numCards);
        ArrayList<Card> tempDeck = new ArrayList<>(this.cards);
        this.cards.clear();
        for(Integer integer : sortOrder){
            this.cards.add(tempDeck.get(integer - 1));
        }
        this.deckIndex = 0;
    }

    public Card getNextCard(){
        // get the next card on the list
        Card returnCard = cards.get(this.deckIndex);
        // increment the deckIndex so we're ready to deal the next card
        this.deckIndex += 1;
        return returnCard;
    }

}
