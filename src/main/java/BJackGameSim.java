import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class BJackGameSim extends BJackGame{
    int iterations;
    int gamesPlayed;

    BJackGameSim(int iterations, String dbConfigPath){
        super(dbConfigPath);
        IOMgrSim ioMgrSim = new IOMgrSim();
        DBMgrSim dbMgrSim = new DBMgrSim(dbConfigPath);
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

        String configFilePath;

        DBMgrSim(String configFilePath){
            super(configFilePath);
            this.configFilePath = configFilePath;
        }

        class WriteEntriesToDb implements Runnable {
            String tableName;
            ArrayList<ResultsEntry> resultsEntries;
            DBMgrSim dbMgrSim;

            WriteEntriesToDb(String tableName, ArrayList<ResultsEntry> resultsEntries, DBMgrSim dbMgrSim){
                this.tableName = tableName;
                this.resultsEntries = resultsEntries;
                this.dbMgrSim = dbMgrSim;
            }

            void writeEntries(){
                LocalDateTime timeStamp1;
                LocalDateTime timeStamp2;
                timeStamp1 = LocalDateTime.now();
                System.out.println(Thread.currentThread().getName() + " " +timeStamp1);

                for(ResultsEntry entry : resultsEntries){
                    writeEntryToDb(tableName, entry, dbMgrSim);
                }

                timeStamp2 = LocalDateTime.now();
                long time1MicroSec = (timeStamp1.getMinute()*60 + timeStamp1.getSecond()) * 1000000
                        + timeStamp1.getNano()/1000;
                long time2MicroSec = (timeStamp2.getMinute()*60 + timeStamp2.getSecond()) * 1000000
                        + timeStamp2.getNano()/1000;
                long elapsed = time2MicroSec - time1MicroSec;
                double elapsedDouble = (double)elapsed/1000000.;
                System.out.println(Thread.currentThread().getName() + " " + elapsedDouble + " seconds elapsed");
            }

            @Override
            public void run() {
                writeEntries();
            }
        }

        @Override
        void writeResultsDbase() {

            // a little faster if done with 2 threads, 1 for dealer results and 1 for player results
            // each of these local objects will have their own dbase connection
            DBMgrSim dbMgrSim1 = new DBMgrSim(this.configFilePath);
            DBMgrSim dbMgrSim2 = new DBMgrSim(this.configFilePath);
            DBMgrSim dbMgrSim3 = new DBMgrSim(this.configFilePath);
            DBMgrSim dbMgrSim4 = new DBMgrSim(this.configFilePath);

            // cut the results arrays in halves
            int dealerSize = dealerResults.size();
            int playerSize = playerResults.size();

            ArrayList<ResultsEntry> dealer1 = new ArrayList<>(dealerResults);
            ArrayList<ResultsEntry> dealer2 = new ArrayList<>(dealerResults);
            // remove the 2nd half of the arrayList
            for(int i = dealerSize - 1; i >= dealerSize/2; i--){
                dealer1.remove(i);
            }
            // now keep the 2nd half by removing the first half
            dealer2.removeAll(dealer1);

            ArrayList<ResultsEntry> player1 = new ArrayList<>(playerResults);
            ArrayList<ResultsEntry> player2 = new ArrayList<>(playerResults);
            // remove the 2nd half of the arrayList
            for(int i = playerSize - 1; i >= playerSize/2; i--){
                player1.remove(i);
            }
            // now keep the 2nd half by removing the first half
            player2.removeAll(player1);

            // these objects are the runnable tasks
            WriteEntriesToDb task1 = new WriteEntriesToDb("dealerhands", dealer1, dbMgrSim1);
            WriteEntriesToDb task2 = new WriteEntriesToDb("dealerhands", dealer2, dbMgrSim2);
            WriteEntriesToDb task3 = new WriteEntriesToDb("playerhands", player1, dbMgrSim3);
            WriteEntriesToDb task4 = new WriteEntriesToDb("playerhands", player2, dbMgrSim4);

            Thread thread1 = new Thread(task1);
            Thread thread2 = new Thread(task2);
            Thread thread3 = new Thread(task3);
            Thread thread4 = new Thread(task4);
            thread1.start();
            thread2.start();
            thread3.start();
            thread4.start();

        }

        void writeEntryToDb(String tableName, ResultsEntry entry, DBMgrSim dbMgrSim){

            String statestr = buildPsSqlString(tableName);

            try(PreparedStatement ps = dbMgrSim.getPreparedScrollable(statestr)){
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
