import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static java.lang.Math.pow;

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

            //TODO: have to remove entries with duplicate hashIDs in dealerResults before writing to database

            // probably use a HashSet somehow. consistently get duplicates when running 100K hands

            // numThreads has to be an even power of 2 for the method to work
            int numThreads = 32;

            int threadsPerPlayer = numThreads/2;
            // calculate log base 2 to determine how many times the player
            // ArrayList will have to be split
            int splits = (int)(Math.log(threadsPerPlayer)/Math.log(2.));

            // each of these local objects will have their own dbase connection
            ArrayList<DBMgrSim> dealerDBMgrs = new ArrayList<>();
            for(int i = 0; i < threadsPerPlayer; i++){
                DBMgrSim dbMgrSim= new DBMgrSim(this.configFilePath);
                dealerDBMgrs.add(dbMgrSim);
            }
            ArrayList<DBMgrSim> playerDBMgrs = new ArrayList<>();
            for(int i = 0; i < threadsPerPlayer; i++){
                DBMgrSim dbMgrSim = new DBMgrSim(this.configFilePath);
                playerDBMgrs.add(dbMgrSim);
            }
            // create the results arrays
            ArrayList<ArrayList<ResultsEntry>> dealerArrays = createResultsLists(threadsPerPlayer);
            ArrayList<ArrayList<ResultsEntry>> playerArrays = createResultsLists(threadsPerPlayer);

            // cut the results arrays in halves
            dealerArrays.get(0).addAll(dealerResults);
            playerArrays.get(0).addAll(playerResults);
            divideArrayList(dealerArrays, splits);
            divideArrayList(playerArrays, splits);

            // these objects are the runnable tasks
            ArrayList<WriteEntriesToDb > taskList = new ArrayList<>();
            // first add the dealer tasks
            for(int i = 0; i < threadsPerPlayer; i++){
                WriteEntriesToDb task = new WriteEntriesToDb("dealerhands",
                        dealerArrays.get(i), dealerDBMgrs.get(i));
                taskList.add(task);
            }
            // now add the player tasks
            for(int i = 0; i < threadsPerPlayer; i++){
                WriteEntriesToDb task = new WriteEntriesToDb("playerhands",
                        playerArrays.get(i), playerDBMgrs.get(i));
                taskList.add(task);
            }

            // now start the threads
            for(int i = 0; i < numThreads; i++){
                Thread thread = new Thread(taskList.get(i));
                thread.start();
            }
        }

        void divideArrayList(ArrayList<ArrayList<ResultsEntry>> arrayLists, int splits){
            for(int i = 0; i < splits; i++){
                int arrayStart = (int) Math.pow(2., i) - 1;
                for(int j = 0; j < (int) Math.pow(2., i); j++){
                    int arrayIndex = arrayStart + j;
                    bisectArrayList(
                            arrayLists.get(2 * arrayIndex + 1),
                            arrayLists.get(2 * arrayIndex + 2),
                            arrayLists.get(arrayIndex));
                }
            }
            // only want to keep the last half of the Array Lists. Keep the last 2^splits
            int arrayIndex = arrayLists.size() - 1;
            ArrayList<ArrayList<ResultsEntry>> swapList = new ArrayList<>();
            for(int i = 0; i < (int)Math.pow(2., splits); i++){
                swapList.add(arrayLists.get(arrayIndex));
                arrayIndex -= 1;
            }
            arrayLists.clear();
            arrayLists.addAll(swapList);
            return;
        }

        ArrayList<ArrayList<ResultsEntry>> createResultsLists(int threadsPerPlayer){
            ArrayList<ArrayList<ResultsEntry>> returnArray = new ArrayList<>();
            for(int i = 0; i < threadsPerPlayer * 2 - 1; i++){
                ArrayList<ResultsEntry> resultsArray = new ArrayList<>();
                returnArray.add(resultsArray);
            }
            return returnArray;
        }

        // this function takes an original ArrayList and populates 2 others with
        // the respective halves of the original
        void bisectArrayList(ArrayList<ResultsEntry> firstHalf,
                             ArrayList<ResultsEntry> secondHalf,
                             ArrayList<ResultsEntry> original){
            int originalSize = original.size();
            firstHalf.clear();
            firstHalf.addAll(original);
            secondHalf.clear();
            secondHalf.addAll(original);

            // remove the 2nd half of the arrayList
            for(int i = originalSize - 1; i >= originalSize/2; i--){
                firstHalf.remove(i);
            }
            // now keep the 2nd half by removing the first half
            secondHalf.removeAll(firstHalf);
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
