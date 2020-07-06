import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

public class BJackGameUnitTest {

    final String configPath = "src/main/resources/config.txt";

    //TODO: unit test for block size creation in writeResultsDbase()
    //TODO: system level tests for integrity of stored tables (only with db connection)

    @Test
    void testgetSplitPairRec(){
        BJackGame game = new BJackGame(configPath);

        ArrayList<ArrayList<BJackHand>> arrayListofPairs = CardTestUtils.getAllStartingHands();
        for (ArrayList<BJackHand> handPair : arrayListofPairs) {
            if (handPair.get(0).havePair()) {
                assertTrue(checkSplitPairRec(handPair.get(0), handPair.get(1), game),
                        "Did not provide the right split recommendation" +
                                " for player: " + handPair.get(0).cards.get(0).getCardSignature() + ", " +
                                handPair.get(0).cards.get(1).getCardSignature() + "\n" + "with dealer showing " +
                                handPair.get(1).cards.get(0).getCardSignature());
            }
        }
    }

    boolean checkSplitPairRec(BJackHand playerHand, BJackHand dealerHand, BJackGame game){
        // array will use dealer up total as column, so Ace is treated as 11
        boolean[][] expected = {
                {true,true,true,true,true,true,false,false,false,false}, // twos
                {true,true,true,true,true,true,false,false,false,false}, // threes
                {false,false,false,true,false,false,false,false,false,false}, // fours
                {false,false,false,false,false,false,false,false,false,false}, // fives
                {true,true,true,true,true,true,false,false,false,false}, // sixes
                {true,true,true,true,true,true,true,false,false,false}, // sevens
                {true,true,true,true,true,true,true,true,true,true}, // eights
                {true,true,true,true,true,false,true,true,false,false}, // nines
                {false,false,false,false,false,false,false,false,false,false}, // tens, J, Q, K
                {true,true,true,true,true,true,true,true,true,true} // aces
        };

        game.dealerHand = dealerHand;
        boolean returned = game.getSplitPairRec(playerHand);
        int expectedRow = playerHand.pairCardValue() - 2;
        int expectedColumn = game.dealerUpCard().getCardValue() - 2;

        return (returned == expected[expectedRow][expectedColumn]);
    }


    @Test
    void testgetSoftHitRec(){
        BJackGame game = new BJackGame(configPath);

        ArrayList<ArrayList<BJackHand>> arrayListofPairs = CardTestUtils.getAllStartingHands();
        for (ArrayList<BJackHand> handPair : arrayListofPairs) {
            if (handPair.get(0).isSoftHand()) {
                assertTrue(checkSoftHitRec(handPair.get(0), handPair.get(1), game),
                        "Did not provide the right soft hit recommendation" +
                        " for player: " + handPair.get(0).cards.get(0).getCardSignature() + ", " +
                        handPair.get(0).cards.get(1).getCardSignature() + "\n" + "with dealer showing " +
                        handPair.get(1).cards.get(0).getCardSignature());
            }
        }
    }
    
    boolean checkSoftHitRec(BJackHand playerHand, BJackHand dealerHand, BJackGame game){
        // array will use dealer up total as column, so Ace is treated as 11
        boolean[][] expected = {
                {true,true,true,true,true,true,true,true,true,true}, // player has soft 12
                {true,true,true,true,true,true,true,true,true,true}, // player has soft 13
                {true,true,true,true,true,true,true,true,true,true}, // player has soft 14
                {true,true,true,true,true,true,true,true,true,true}, // player has soft 15
                {true,true,true,true,true,true,true,true,true,true}, // player has soft 16
                {true,true,true,true,true,true,true,true,true,true}, // player has soft 17
                {false,false,false,false,false,false,false,true,true,false}, // player has soft 18
                {false,false,false,false,false,false,false,false,false,false}, // player has soft 19
                {false,false,false,false,false,false,false,false,false,false}, // player has soft 20
                {false,false,false,false,false,false,false,false,false,false} // player has soft 21
                // 21 is test scenario only. In production code, 21 would be identified as Blackjack
        };
        game.dealerHand = dealerHand;
        boolean returned = game.getSoftHitRec(playerHand);
        int expectedRow = playerHand.getHandTotal() - 12;
        int expectedColumn = game.dealerUpCard().getCardValue() - 2;

        return (returned == expected[expectedRow][expectedColumn]);
    }

    @Test
    void testgetHardHitRec(){
        BJackGame game = new BJackGame(configPath);

        ArrayList<ArrayList<BJackHand>> arrayListofPairs = CardTestUtils.getAllStartingHands();
        for (ArrayList<BJackHand> handPair : arrayListofPairs) {
            if (!handPair.get(0).isSoftHand()) {
                assertTrue(checkHardHitRec(handPair.get(0), handPair.get(1), game),
                        "Did not provide the right hard hit recommendation" +
                                " for player: " + handPair.get(0).cards.get(0).getCardSignature() + ", " +
                                handPair.get(0).cards.get(1).getCardSignature() + "\n" + "with dealer showing " +
                                handPair.get(1).cards.get(0).getCardSignature());
            }
        }
    }

    boolean checkHardHitRec(BJackHand playerHand, BJackHand dealerHand, BJackGame game){
        // array will use dealer up total as column, so Ace is treated as 11
        boolean[][] expected = {
                {true,true,true,true,true,true,true,true,true,true}, // player has hard 4
                {true,true,true,true,true,true,true,true,true,true}, // player has hard 5
                {true,true,true,true,true,true,true,true,true,true}, // player has hard 6
                {true,true,true,true,true,true,true,true,true,true}, // player has hard 7
                {true,true,true,true,true,true,true,true,true,true}, // player has hard 8
                {true,true,true,true,true,true,true,true,true,true}, // player has hard 9
                {true,true,true,true,true,true,true,true,true,true}, // player has hard 10
                {true,true,true,true,true,true,true,true,true,true}, // player has hard 11
                {true,true,false,false,false,true,true,true,true,true}, // player has hard 12
                {false,false,false,false,false,true,true,true,true,true}, // player has hard 13
                {false,false,false,false,false,true,true,true,true,true}, // player has hard 14
                {false,false,false,false,false,true,true,true,true,true}, // player has hard 15
                {false,false,false,false,false,true,true,true,true,true}, // player has hard 16
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 17
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 18
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 19
                {false,false,false,false,false,false,false,false,false,false} // player has hard 20
        };
        game.dealerHand = dealerHand;
        boolean returned = game.getHardHitRec(playerHand);
        int expectedRow = playerHand.getHandTotal() - 4;
        int expectedColumn = game.dealerUpCard().getCardValue() - 2;

        return (returned == expected[expectedRow][expectedColumn]);
    }

    @Test
    void testgetHardDoubleRec(){
        BJackGame game = new BJackGame(configPath);

        ArrayList<ArrayList<BJackHand>> arrayListofPairs = CardTestUtils.getAllStartingHands();
        for (ArrayList<BJackHand> handPair : arrayListofPairs) {
            if (!handPair.get(0).isSoftHand()) {
                assertTrue(checkHardDoubleRec(handPair.get(0), handPair.get(1), game),
                        "Did not provide the right hard double recommendation" +
                                " for player: " + handPair.get(0).cards.get(0).getCardSignature() + ", " +
                                handPair.get(0).cards.get(1).getCardSignature() + "\n" + "with dealer showing " +
                                handPair.get(1).cards.get(0).getCardSignature());
            }
        }
    }

    boolean checkHardDoubleRec(BJackHand playerHand, BJackHand dealerHand, BJackGame game){
        // array will use dealer up total as column, so Ace is treated as 11
        boolean[][] expected = {
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 4
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 5
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 6
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 7
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 8
                {true,true,true,true,true,false,false,false,false,false}, // player has hard 9
                {true,true,true,true,true,true,true,true,false,false}, // player has hard 10
                {true,true,true,true,true,true,true,true,true,true}, // player has hard 11
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 12
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 13
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 14
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 15
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 16
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 17
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 18
                {false,false,false,false,false,false,false,false,false,false}, // player has hard 19
                {false,false,false,false,false,false,false,false,false,false} // player has hard 20
        };
        game.dealerHand = dealerHand;
        boolean returned = game.getHardDoubleRec(playerHand);
        int expectedRow = playerHand.getHandTotal() - 4;
        int expectedColumn = game.dealerUpCard().getCardValue() - 2;

        return (returned == expected[expectedRow][expectedColumn]);
    }

    @Test
    void testgetSoftDoubleRec(){
        BJackGame game = new BJackGame(configPath);

        ArrayList<ArrayList<BJackHand>> arrayListofPairs = CardTestUtils.getAllStartingHands();
        for (ArrayList<BJackHand> handPair : arrayListofPairs) {
            if (handPair.get(0).isSoftHand()) {
                assertTrue(checkSoftDoubleRec(handPair.get(0), handPair.get(1), game),
                        "Did not provide the right soft double recommendation" +
                                " for player: " + handPair.get(0).cards.get(0).getCardSignature() + ", " +
                                handPair.get(0).cards.get(1).getCardSignature() + "\n" + "with dealer showing " +
                                handPair.get(1).cards.get(0).getCardSignature());
            }
        }
    }

    boolean checkSoftDoubleRec(BJackHand playerHand, BJackHand dealerHand, BJackGame game){
        // array will use dealer up total as column, so Ace is treated as 11
        boolean[][] expected = {
                {false,false,false,true,true,false,false,false,false,false}, // player has soft 12
                {false,false,true,true,true,false,false,false,false,false}, // player has soft 13
                {false,false,true,true,true,false,false,false,false,false}, // player has soft 14
                {false,false,true,true,true,false,false,false,false,false}, // player has soft 15
                {false,false,true,true,true,false,false,false,false,false}, // player has soft 16
                {true,true,true,true,true,false,false,false,false,false}, // player has soft 17
                {false,true,true,true,true,false,false,false,false,false}, // player has soft 18
                {false,false,false,false,false,false,false,false,false,false}, // player has soft 19
                {false,false,false,false,false,false,false,false,false,false}, // player has soft 20
                {false,false,false,false,false,false,false,false,false,false} // player has soft 21
                // 21 is test scenario only. In production code, 21 would be identified as Blackjack
        };
        game.dealerHand = dealerHand;
        boolean returned = game.getSoftDoubleRec(playerHand);
        int expectedRow = playerHand.getHandTotal() - 12;
        int expectedColumn = game.dealerUpCard().getCardValue() - 2;

        return (returned == expected[expectedRow][expectedColumn]);
    }
    
    @Test
    void testbuildTableName(){

        DeckBySuit refDeck = new DeckBySuit();
        ArrayList<String> tableNames = new ArrayList<>();
        BJackGameSim game = new BJackGameSim(10, 32, configPath);
        game.preGameInit(1);

        for(int playerIndex1 = 0; playerIndex1 < 10; playerIndex1++){
            for(int playerIndex2 = playerIndex1 + 13; playerIndex2 < 23; playerIndex2++){
                for(int dealerUpIndex = 26; dealerUpIndex < 36; dealerUpIndex++){
                    TreeMap<Integer, Card> seedCards = new TreeMap<>();
                    CardTestUtils.fixDeckEntries(seedCards, 0, refDeck.cards.get(playerIndex1));
                    CardTestUtils.fixDeckEntries(seedCards, 1, refDeck.cards.get(dealerUpIndex));
                    CardTestUtils.fixDeckEntries(seedCards, 2, refDeck.cards.get(playerIndex2));
                    game.deck.shuffle();
                    CardTestUtils.fixDeck(game.deck, seedCards);
                    boolean goodDeck = CardTestUtils.checkDeckIntegrity(game.deck);
                    assertTrue(goodDeck, "Failed deck integrity check");
                    game.dealHands();
                    StringBuilder builder = new StringBuilder();
                    builder.append(game.dbMgr.buildTableName(game.dealerHand, game.players.get(0).hands.get(0)));
                    tableNames.add(builder.toString());
                    game.postHandReInit();
                }
            }
        }
        assertEquals(550, tableNames.size(), "Wrong number of table names from production code");
        ArrayList<String> expectedNames = getTestNamesList("src/test/resources/tablenames.txt");
        assertEquals(550, expectedNames.size(), "Wrong number of expected names from test code");
        boolean nameFound;
        for(String name : expectedNames){
            nameFound = tableNames.contains(name);
            assertTrue(nameFound, "Did not find expected table name " + name);
        }
        for(String name : tableNames){
            nameFound = expectedNames.contains(name);
            assertTrue(nameFound, "Did not expect to find table " + name);
        }
    }

    ArrayList<String> getTestNamesList(String filePath){
        return MyIOUtils.readLinesAsStrings(filePath);
    }
}
