import java.util.TreeMap;

public class BJackGameTest extends BJackGame{

    BJackGameTest(){
        super();
        //TODO: create a new constructor that takes the filePath as a parameter
        String filePath = "src/test/resources/fix65244.txt";
        TreeMap<Integer, Card> seedCards = CardTestUtils.fixDeckEntries(filePath);
        CardTestUtils.fixDeck(this.deck, seedCards);
        boolean deckGood = CardTestUtils.checkDeckIntegrity(this.deck);
        System.out.println("Deck integrity is good = " + deckGood);
    }


}
