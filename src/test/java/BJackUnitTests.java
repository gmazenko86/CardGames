import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BJackUnitTests {

    @Test
    void testFaceCount(){
        Deck deck = new Deck();
        Hand hand = new Hand();
        for (int i = 0; i < deck.cards.size(); i++){
            hand.drawCard(deck);
        }
        deck.shuffle();
        assertEquals(4, hand.faceCount(Card.CardFace.TWO), "Should have 4 2s");
        assertEquals(4, hand.faceCount(Card.CardFace.THREE), "Should have 4 3s");
        assertEquals(4, hand.faceCount(Card.CardFace.FOUR), "Should have 4 4s");
        assertEquals(4, hand.faceCount(Card.CardFace.FIVE), "Should have 4 5s");
        assertEquals(4, hand.faceCount(Card.CardFace.SIX), "Should have 4 6s");
        assertEquals(4, hand.faceCount(Card.CardFace.SEVEN), "Should have 4 7s");
        assertEquals(4, hand.faceCount(Card.CardFace.EIGHT), "Should have 4 8s");
        assertEquals(4, hand.faceCount(Card.CardFace.NINE), "Should have 4 9s");
        assertEquals(4, hand.faceCount(Card.CardFace.TEN), "Should have 4 10s");
        assertEquals(4, hand.faceCount(Card.CardFace.JACK), "Should have 4 Jacks");
        assertEquals(4, hand.faceCount(Card.CardFace.QUEEN), "Should have 4 Queens");
        assertEquals(4, hand.faceCount(Card.CardFace.KING), "Should have 4 Kings");
        assertEquals(4, hand.faceCount(Card.CardFace.ACE), "Should have 4 Aces");
    }

    @Test
    void testDrawCard(){
        Deck deck = new Deck();
        Hand hand = new Hand();
        Card refCard = new Card();
        int numCards = 50;
        for (int i = 0; i < numCards; i++){
            hand.drawCard(deck);
        }
        assertEquals(numCards, deck.deckIndex, "Should have drawn" + numCards + "cards");

        boolean allCards = true;
        for(int i = 1; i < numCards; i++){
            if (!(hand.cards.get(i).getClass() == refCard.getClass())){
                allCards = false;
                break;
            }
        }
        assertTrue(allCards, "Not all objects are " + refCard.getClass());
    }
}
