import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BJackUnitTest {

    @Test
    void testfaceCount(){
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
    void testdrawCard(){
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

    @Test
    void testgetHandTotal(){
        // first test that it handles Aces correctly
        Card aceSpades = new Card(Card.CardFace.ACE, Card.Suit.SPADES);
        Card aceClubs = new Card(Card.CardFace.ACE, Card.Suit.CLUBS);
        Card aceHearts = new Card(Card.CardFace.ACE, Card.Suit.HEARTS);
        Card aceDiamonds = new Card(Card.CardFace.ACE, Card.Suit.DIAMONDS);

        BJackHand hand = new BJackHand();

        // create a fictitious hand with 25 aces
        for (int i = 0; i < 5; i++){
            hand.cards.add(aceSpades);
            hand.cards.add(aceClubs);
            hand.cards.add(aceHearts);
            hand.cards.add(aceDiamonds);
        }
        BJackHand swapHand = new BJackHand();
        // make sure it handles the hard and soft totals correctly
        int total;
        int expected = 0;
        int numCards = 0;
        for(Card card : hand.cards){
            numCards += 1;
            swapHand.cards.add(card);
            total = swapHand.getHandTotal();
            if(numCards <= 11){expected = 10 + numCards;}
            if(12 <= numCards){expected = numCards;}
            assertEquals(expected, total, "function did not add Aces correctly");
        }
        // now count the whole deck and make sure total = 85 * 4 = 340
        BJackHand newHand = new BJackHand();
        Deck deck = new Deck();
        deck.shuffle();
        for(Card card : deck.cards){
            newHand.cards.add(card);
        }
        assertEquals(340, newHand.getHandTotal(), "Cards not added correctly");
    }
}
