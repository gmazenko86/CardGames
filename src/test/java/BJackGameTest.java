import java.util.TreeMap;

public class BJackGameTest extends BJackGame{

    BJackGameTest(){
        // use this constructor if you want to use a normal fair deck
        super();
    }

    BJackGameTest(String inputPath){
        // use this constructor if you want to fix the deck for test and debug
        super();
        TreeMap<Integer, Card> seedCards = CardTestUtils.fixDeckEntries(inputPath);
        CardTestUtils.fixDeck(this.deck, seedCards);
        boolean deckGood = CardTestUtils.checkDeckIntegrity(this.deck);
        assert (deckGood) : assertPrint("Deck integrity check failed");
        System.out.println("Deck integrity is good == " + deckGood);
    }

    boolean assertPrint(String string){
        System.out.println(string);
        return true;
    }

    @Override
    void preDealInit(int numPlayers) {
        super.preDealInit(numPlayers);
        // temporarily give players additional hands for debug and test
/*
        BJackHand tempHand = new BJackHand();
        BJackHand tempHand1 = new BJackHand();
        BJackHand tempHand2 = new BJackHand();

        this.bJackPlayers.get(1).hands.add(tempHand);
        this.bJackPlayers.get(1).hands.add(tempHand1);
        this.bJackPlayers.get(1).hands.add(tempHand2);
*/
    }
}
