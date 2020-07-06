import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.TreeMap;

public class BJackTableWriter extends BJackGameSim{
    int dealerUpIndex;
    int playerIndex1;
    int playerIndex2;

    BJackHand lastDealerStartHand;
    BJackHand lastPlayerStartHand;

    BJackTableWriter(int iterations, String dbConfigPath){
        super(iterations, dbConfigPath);
        this.lastDealerStartHand = new BJackHand();
        this.lastPlayerStartHand = new BJackHand();
    }

    @Override
    void playGameWrapper() {
        prePlayGameInit();
        generateTables();
    }

    void generateTables(){

        int tableCount = 0;
        for(playerIndex1 = 0; playerIndex1 < 10; playerIndex1++){
            for(playerIndex2 = playerIndex1 + 13; playerIndex2 < 23; playerIndex2++){
                for(dealerUpIndex = 26; dealerUpIndex < 36; dealerUpIndex++){
                    // next lines
                    fixDeckForTableWrites();
                    playGame();
                    StringBuilder tableNameBuilder = new StringBuilder();
                    tableNameBuilder.append(dbMgr.buildTableName(lastDealerStartHand, lastPlayerStartHand));
                    StringBuilder sqlBuilder = new StringBuilder();
                    sqlBuilder.append(getSqlCreateTable(tableNameBuilder.toString()));
                    if(validDbConnection){
                        tableCount += 1;
                        MyIOUtils.printlnRedText("attempting to create table " + tableNameBuilder.toString()
                                + " number = " + tableCount);
                        createResultsTable(sqlBuilder.toString());
                        truncateTable("dealerhands");
                        truncateTable("playerhands");
                    }
                }
            }
        }
    }

    //TODO: decide if some of these functions should be moved into dbMgr or MyPostGreSqlClass
    void createResultsTable(String sqlString){
        try(Statement statement = dbMgr.getStatementScrollable()){
            statement.execute(sqlString);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    //TODO: probably move this to MyPostGreSqlClass
    void truncateTable(String tableName){
        String sqlString = "truncate " + tableName + ";";
        try(Statement statement = dbMgr.getStatementScrollable()){
            statement.execute(sqlString);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    void postHandReInit() {
        super.postHandReInit();
        deck.shuffle();
        fixDeckForTableWrites();
    }

    void fixDeckForTableWrites(){
        DeckBySuit refDeck = new DeckBySuit();
        TreeMap<Integer, Card> seedCards = new TreeMap<>();

        CardTestUtils.fixDeckEntries(seedCards, 0, refDeck.cards.get(playerIndex1));
        CardTestUtils.fixDeckEntries(seedCards, 1, refDeck.cards.get(dealerUpIndex));
        CardTestUtils.fixDeckEntries(seedCards, 2, refDeck.cards.get(playerIndex2));
        deck.shuffle();
        CardTestUtils.fixDeck(deck, seedCards);
    }

    @Override
    void dealHands() {
        super.dealHands();
        // have to save the last dealer and player hands
        lastDealerStartHand.cards.clear();
        lastDealerStartHand.cards.addAll(dealerHand.cards);
        lastPlayerStartHand.cards.clear();
        lastPlayerStartHand.cards.addAll(players.get(0).hands.get(0).cards);
    }

    String getSqlCreateTable(String tableName){
        return "select dealerhands.hashid as hashid, dealerhands.total as dtot,\n" +
            "dealerhands.attribute as dattrib, dealerhands.result as dresult,\n" +
            "dealerhands.card1 as dcard1, dealerhands.card2 as dcard2, \n" +
            "dealerhands.card3 as dcard3, dealerhands.card4 as dcard4, \n" +
            "dealerhands.card5 as dcard5, dealerhands.card6 as dcard6, \n" +
            "dealerhands.card7 as dcard7, dealerhands.card8 as dcard8, \n" +
            "playerhands.total as ptot, \n" +
            "playerhands.attribute as pattrib, playerhands.result as presult,\n" +
            "playerhands.card1 as pcard1, playerhands.card2 as pcard2, \n" +
            "playerhands.card3 as pcard3, playerhands.card4 as pcard4, \n" +
            "playerhands.card5 as pcard5, playerhands.card6 as pcard6, \n" +
            "playerhands.card7 as pcard7, playerhands.card8 as pcard8 \n" +
            "into " + tableName + "\n" +
            "from dealerhands left join playerhands using(hashid);";
    }
}
