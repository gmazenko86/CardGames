/*
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.getDigits;
*/
public class CardGameTest {
    public static void main(String... args){
        String fixDeckFilePath = "src/test/resources/fix65244.txt";
        String fixDeckFilePath2 = "src/test/resources/fixA__J.txt";
        BJackGameTest bJackGameTest = new BJackGameTest();
//        BJackGameTest bJackGameTest = new BJackGameTest(fixDeckFilePath);
//        BJackGameTest bJackGameTest = new BJackGameTest(fixDeckFilePath2);
        bJackGameTest.playGame();

/*
        boolean goodDeck;
        Deck testdeck = new Deck();
        goodDeck = CardTestUtils.checkDeckIntegrity(testdeck);
        System.out.println("The deck is good = " + goodDeck);

        testdeck.cards.remove(15);
        goodDeck = CardTestUtils.checkDeckIntegrity(testdeck);
        System.out.println("The deck is good = " + goodDeck);

        Deck testdeck2 = new Deck();
        goodDeck = CardTestUtils.checkDeckIntegrity(testdeck2);
        System.out.println("The deck is good = " + goodDeck);

        Card card = CardTestUtils.getCard(Card.CardFace.EIGHT, Card.Suit.HEARTS);
        testdeck2.cards.add(16, card);
        testdeck2.cards.add(card);
        goodDeck = CardTestUtils.checkDeckIntegrity(testdeck2);
        System.out.println("The deck is good = " + goodDeck);
*/
    }
}
