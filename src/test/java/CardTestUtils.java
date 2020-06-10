import java.util.TreeMap;

public class CardTestUtils {
    static public void fixDeck(Deck deck,  TreeMap<Integer, Card> seedCards){
        // first, remove the seed cards from the existing deck so they are not duplicated
        seedCards.forEach((k,v)->deck.cards.remove(v));

        // now add the seed cards to the deck in the spots defined by the TreeMap keys
        seedCards.forEach((k,v)-> deck.cards.add(k, v));
    }

    static public TreeMap<Integer, Card> fixDeckEntries(){
        Card card1 = getCard(Card.CardFace.ACE, Card.Suit.SPADES);
        Integer position1 = 1;
        Card card2 = getCard(Card.CardFace.JACK, Card.Suit.CLUBS);
        Integer position2 = 3;

        TreeMap<Integer, Card> treeMap = new TreeMap<>();
        treeMap.put(position1, card1);
        treeMap.put(position2, card2);

        return treeMap;
    }

    static public Card getCard(Card.CardFace face, Card.Suit suit){
        Card card = new Card();
        card.cardFace = face;
        card.suit = suit;
        return card;
    }
}
