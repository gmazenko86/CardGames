import java.sql.*;
import java.time.LocalDateTime;

public class BJackGameSim extends BJackGame{
    int iterations;
    int gamesPlayed;

    BJackGameSim(int iterations){
        super();
        IOMgrSim ioMgrSim = new IOMgrSim();
        DBMgrSim dbMgrSim = new DBMgrSim();
        this.iom = (IOMgr) ioMgrSim;
        this.dbMgr = (DBMgr) dbMgrSim;
        this.iterations = iterations;
        gamesPlayed = 0;
    }

    // this extends the nested class IOMgr from the parent class BJackGame
    // to use the override functions below, this.iom has to be
    // assigned to the ioMgrSim instance created in the constructor
    class IOMgrSim extends IOMgr {
        @Override
        void displayActiveHands() {
        }

        @Override
        void displayAllHands() {
        }

        @Override
        void displayResults() {
        }

    }

    // this extends the nested class DBMgr from the parent class BJackGame
    // to use the override functions below, this.dbMgr has to be
    // assigned to the dbMgrSim instance created in the constructor
    class DBMgrSim extends DBMgr{


        @Override
        void writeResultsDbase() {

            LocalDateTime timeStamp;
            timeStamp = LocalDateTime.now();
            System.out.println(timeStamp);

            MyPostGreSqlClass dbmgr = new MyPostGreSqlClass("/home/greg/PersonalCodingExercises/" +
                    "DbaseExercises/src/main/resources/config.txt");

            for(ResultsEntry entry : dealerResults){
                writeEntryToDb("dealertable", entry, dbmgr);
            }

            timeStamp = LocalDateTime.now();
            System.out.println(timeStamp);
        }

        void writeEntryToDb(String tableName, ResultsEntry entry, MyPostGreSqlClass dbmgr){

            String statestr = buildPsSqlString("dealerhands");

            try(PreparedStatement ps = dbmgr.getPreparedScrollable(statestr)){
                ps.setInt(1, entry.handHashId);
                ps.setInt(2, entry.handTotal);
                ps.setString(3, entry.handAttribute.name());
                ps.setString(4, entry.handResult.name());
                int i = 5;
                for(Card card : entry.cards){
                    ps.setString(i, card.cardFace.name());
                    i++;
                }
                for(int loopIndex = i ; loopIndex <= 16; loopIndex++){
                    ps.setNull(loopIndex, Types.NULL);
                }
                ps.execute();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }

        String buildPsSqlString(String tableName){
            StringBuilder builder = new StringBuilder("insert into ");
            builder.append(tableName);
            builder.append("(\n");
            builder.append("hashid, total, attribute, result,\n");
            builder.append("card1,card2,card3,card4,card5,card6,card7,card8,card9,card10,card11,card12)\n");
            builder.append("values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
            return builder.toString();
        }
    }

    @Override
    boolean playAnotherHand() {
        gamesPlayed += 1;
        return gamesPlayed < iterations;
    }

    @Override
    boolean hitHand(BJackHand hand) {
        boolean hitFlag = false;

        if(hand.isSoftHand()){
            if(getSoftHitRec(hand)){
                hitFlag = true;
            }
        } else if(getHardHitRec(hand)){
             hitFlag = true;
        }
        return hitFlag;
    }

    @Override
    boolean doubleDown(BJackHand hand) {

        boolean returnFlag = false;
        // first handle hard totals
        if(!hand.isSoftHand()) {
            if (getHardDoubleRec(hand)){
                returnFlag = true;
            }
        } else{
            if(getSoftDoubleRec(hand)){
                returnFlag = true;
            }
        }
        return  returnFlag;
    }

    @Override
    boolean splitPair(BJackHand hand) {
        boolean recommendation;
        recommendation = getSplitPairRec(hand);
        return recommendation;
    }

    @Override
    void displayPlayerBankrolls() {
    }

/*
    // don't need this since it was a bit faster to use a java PreparedStatement
    protected String buildSqlString(String tableName, ResultsEntry entry){
        StringBuilder builder = new StringBuilder("insert into ");
        builder.append(tableName);
        builder.append("(\n");

        builder.append("hashid, total, attribute, result");
        int i= 0;
        for(Card card : entry.cards){
            i++;
            Integer index = i;
            builder.append(", ");
            builder.append("card");
            builder.append(index.toString());
        }
        builder.append(")\n");
        builder.append("values(");
        builder.append(((Integer)(entry.handHashId)).toString());
        builder.append(", ");
        builder.append(((Integer)(entry.handTotal)).toString());
        builder.append(", '");
        builder.append(entry.handAttribute.name());
        builder.append("', '");
        builder.append(entry.handResult.name());
        for(Card card : entry.cards){
            builder.append("','");
            builder.append(card.cardFace.name());
        }
        builder.append("');");
        return builder.toString();
    }
*/

}
