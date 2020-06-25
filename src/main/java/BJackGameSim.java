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
            int beginIndex;
            int endIndex;

            WriteEntriesToDb(String tableName, ArrayList<ResultsEntry> resultsEntries, DBMgrSim dbMgrSim,
                             int beginIndex, int endIndex){
                this.tableName = tableName;
                this.resultsEntries = resultsEntries;
                this.dbMgrSim = dbMgrSim;
                this.beginIndex = beginIndex;
                this.endIndex = endIndex;
            }

            void writeEntries(){
                LocalDateTime timeStamp1;
                LocalDateTime timeStamp2;
                timeStamp1 = LocalDateTime.now();

                for(int i = beginIndex; i < endIndex; i++){
                    writeEntryToDb(tableName, resultsEntries.get(i), dbMgrSim);
                }

                timeStamp2 = LocalDateTime.now();
                System.out.print(timeStamp2 + " ");
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

            LocalDateTime timeStamp = LocalDateTime.now();
            System.out.println(timeStamp + " = start of writeResultsDbase()");

            // numThreads has to be an even power of 2 for the method to work
            int numThreads = 32;

            int threadsPerPlayer = numThreads / 2;
            // calculate log base 2 to determine how many times the player
            // ArrayList will have to be split
            int splits = (int) (Math.log(threadsPerPlayer) / Math.log(2.));

            timeStamp = LocalDateTime.now();
            System.out.println(timeStamp + " = ready to get the dbase connections");

            // each of these local objects will have their own dbase connection
            ArrayList<DBMgrSim> dealerDBMgrs = new ArrayList<>();
            for (int i = 0; i < threadsPerPlayer; i++) {
                DBMgrSim dbMgrSim = new DBMgrSim(this.configFilePath);
                dealerDBMgrs.add(dbMgrSim);
            }
            ArrayList<DBMgrSim> playerDBMgrs = new ArrayList<>();
            for (int i = 0; i < threadsPerPlayer; i++) {
                DBMgrSim dbMgrSim = new DBMgrSim(this.configFilePath);
                playerDBMgrs.add(dbMgrSim);
            }

            timeStamp = LocalDateTime.now();
            System.out.println(timeStamp + " = ready to create the task lists");

            // determine results array indices to pass to each thread

            int dealerBlockSize = dealerResults.size() / threadsPerPlayer;
            int dealerExtras = dealerResults.size() % threadsPerPlayer;
            int playerBlockSize = playerResults.size() / threadsPerPlayer;
            int playerExtras = playerResults.size() % threadsPerPlayer;

            // these objects are the runnable tasks
            ArrayList<WriteEntriesToDb > taskList = new ArrayList<>();
            addTasks(taskList, dealerResults, dealerDBMgrs,"dealerhands",
                    threadsPerPlayer, dealerBlockSize, dealerExtras);
            addTasks(taskList, playerResults, playerDBMgrs,"playerhands",
                    threadsPerPlayer, playerBlockSize, playerExtras);

            // now start the threads
            timeStamp = LocalDateTime.now();
            MyIOUtils.printlnBlueText(timeStamp + " = ready to start the threads");
            for(int i = 0; i < numThreads; i++){
                Thread thread = new Thread(taskList.get(i));
                thread.start();
            }
        }

        void addTasks(ArrayList<WriteEntriesToDb > taskList,
                      ArrayList<ResultsEntry> resultsEntries,
                      ArrayList<DBMgrSim> dbMgrSims,
                      String tableName, int threadsPerPlayer, int blockSize, int extras){
            int beginIndex;
            int endIndex;
            for(int i = 0; i < threadsPerPlayer; i++){
                beginIndex = i * blockSize;
                if(i == (threadsPerPlayer - 1)){
                    endIndex = (i+1)*blockSize + extras;
                } else{
                    endIndex = (i+1)*blockSize;
                }
                WriteEntriesToDb task = new WriteEntriesToDb(tableName,
                        resultsEntries, dbMgrSims.get(i), beginIndex, endIndex);
                taskList.add(task);
            }
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
}
