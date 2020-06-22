import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;

/*
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.getDigits;
*/


public class CardGameTest {
    public static void main(String... args){
//        String fixDeckFilePath = "src/test/resources/fix65244.txt";
//        String fixDeckFilePath2 = "src/test/resources/fixA__J.txt";
//        BJackGameTest bJackGameTest = new BJackGameTest();
//        BJackGameTest bJackGameTest = new BJackGameTest(fixDeckFilePath);
//        BJackGameTest bJackGameTest = new BJackGameTest(fixDeckFilePath2);
//        bJackGameTest.playGame();
        
        BJackGameSim bJackGameSim = new BJackGameSim(5);
        bJackGameSim.playGame();


/*
        LinkedHashMap<Integer, LocalDateTime> hashMap = new LinkedHashMap<>();
        LocalDateTime dateTime;
        int hashCode;
        for(int i = 0; i < 10; i++){
            dateTime = LocalDateTime.now();
            hashCode = dateTime.hashCode();
            hashMap.put(hashCode,dateTime);
        }
        hashMap.forEach((k,v)->System.out.println(k + "="+ v));

*/

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
