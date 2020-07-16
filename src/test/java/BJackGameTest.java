import bjack.BJackGame;
import cards.Card;

import java.util.TreeMap;

public class BJackGameTest extends BJackGame {

    BJackGameTest(String dbaseConfigFilePath){
        // use this constructor if you want to use a normal fair deck
        super(dbaseConfigFilePath);
    }

    BJackGameTest(String dbaseConfigFilePath, String fixDeckInputPath){
        // use this constructor if you want to fix the deck for test and debug
        super(dbaseConfigFilePath);
        TreeMap<Integer, Card> seedCards = CardTestUtils.fixDeckEntries(fixDeckInputPath);
        CardTestUtils.fixDeck(this.deck, seedCards);
        boolean deckGood = CardTestUtils.checkDeckIntegrity(this.deck);
        assert (deckGood) : assertPrint("cards.Deck integrity check failed");
        System.out.println("cards.Deck integrity is good == " + deckGood);
    }

    @Override
    public void preGameInit(int numPlayers) {
        super.preGameInit(numPlayers);
        // temporarily give players additional hands for debug and test
/*
        bjack.BJackHand tempHand = new bjack.BJackHand();
        bjack.BJackHand tempHand1 = new bjack.BJackHand();
        bjack.BJackHand tempHand2 = new bjack.BJackHand();

        this.bJackPlayers.get(1).hands.add(tempHand);
        this.bJackPlayers.get(1).hands.add(tempHand1);
        this.bJackPlayers.get(1).hands.add(tempHand2);
*/
    }
}
