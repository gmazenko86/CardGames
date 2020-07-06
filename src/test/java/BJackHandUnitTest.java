import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BJackHandUnitTest {

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
        newHand.cards.addAll(deck.cards);
        assertEquals(340, newHand.getHandTotal(), "Cards not added correctly");
    }
}
