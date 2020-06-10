import java.util.TreeMap;

public class BJackGameTest extends BJackGame{

    @Override
    void initializeDeck() {
        super.initializeDeck();
        TreeMap<Integer,Card> deckEntries = CardTestUtils.fixDeckEntries();
        CardTestUtils.fixDeck(this.deck, deckEntries);
    }
}
