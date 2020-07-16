import myioutils.MyIOUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BJackGameSim extends BJackGame{
    int iterations;
    int gamesPlayed;
    int numThreads;
    ArrayList<DBMgrSim> dealerDBMgrs;
    ArrayList<DBMgrSim> playerDBMgrs;

    BJackGameSim(int iterations, int numThreads, String dbConfigPath){
        super(dbConfigPath);
        this.iom = new IOMgrSim();
        // if db connection is valid, instantiate a DBMgrSim to handle the db writes
        // otherwise, the dbMgr instantiated by the parent will remain (write function does nothing)
        if(this.validDbConnection){
            this.dbMgr = new DBMgrSim(dbConfigPath);
        }
        this.numThreads = numThreads;
        this.dealerDBMgrs = new ArrayList<>();
        this.playerDBMgrs = new ArrayList<>();
        this.iterations = iterations;
        gamesPlayed = 0;
    }

    @Override
    void playGameWrapper() {
        prePlayGameInit();
        playGame();
    }

    void prePlayGameInit(){
        LocalDateTime timeStamp = LocalDateTime.now();
        int threadsPerPlayer = numThreads/2;
        System.out.println(timeStamp + " = ready to get the dbase connections");
        // each of these objects will have their own dbase connection
        populateDbmgrArray(dealerDBMgrs, threadsPerPlayer);
        populateDbmgrArray(playerDBMgrs, threadsPerPlayer);
    }

    void populateDbmgrArray(ArrayList<DBMgrSim> dbMgrSims, int threadsPerPlayer){
        for (int i = 0; i < threadsPerPlayer; i++) {
            DBMgrSim dbMgrSim = new DBMgrSim(this.dbConfigPath);
            dbMgrSims.add(dbMgrSim);
        }
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

        final String configFilePath;

        DBMgrSim(String configFilePath){
            super(configFilePath);
            this.configFilePath = configFilePath;
        }

        public class WriteEntriesToDb implements Runnable, Callable<String > {
            final String tableName;
            final ArrayList<ResultsEntry> resultsEntries;
            final DBMgrSim dbMgrSim;
            final int beginIndex;
            final int endIndex;

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


            // this override enables use of Runnable objects as threads
            @Override
            public void run() {
                writeEntries();
            }

            // Override call() to enable use of a thread pool
            // returns an Object by default. Return a potentially useful debug string instead
            // would return a Result if results of the thread were needed
            @Override
            public String call() {
                String str = "invoked call() method";
                writeEntries();
                return str;
            }
        }

        @Override
        void writeResultsDbase() {

            LocalDateTime timeStamp = LocalDateTime.now();
            System.out.println(timeStamp + " = start of writeResultsDbase()");

            // numThreads has to be an even power of 2 for the method to work
            // numThreads is set in the constructor
            // each thread will have its own dbase connection
            // 128 threads will usually generate exceptions due to too many dbase connections

            int threadsPerPlayer = numThreads / 2;

            timeStamp = LocalDateTime.now();
            System.out.println(timeStamp + " = ready to create the task lists");

            // determine results array indices to pass to each thread

            int dealerBlockSize = getBlockSize(dealerResults, threadsPerPlayer);
            int playerBlockSize = getBlockSize(playerResults, threadsPerPlayer);

            // these objects are the callable tasks
            ArrayList<Callable<String> > taskList = new ArrayList<>();
            addTasks(taskList, dealerResults, dealerDBMgrs,"dealerhands",
                    threadsPerPlayer, dealerBlockSize);
            addTasks(taskList, playerResults, playerDBMgrs,"playerhands",
                    threadsPerPlayer, playerBlockSize);

            // now start the threads
            timeStamp = LocalDateTime.now();
            MyIOUtils.printlnBlueText(timeStamp + " = ready to start the threads");

            ExecutorService pool = Executors.newFixedThreadPool(numThreads);
            try{
                pool.invokeAll(taskList);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                pool.shutdown();
            }
        }

        int getBlockSize(ArrayList<ResultsEntry> resultsList, int threadsPerPlayer){
            return resultsList.size() / threadsPerPlayer;
        }

        int getBeginIndex(int threadNo, int blockSize){
            return threadNo * blockSize;
        }

        int getEndIndex(int threadNo, int blockSize, int threadsPerPlayer, int arraySize){
            int beginIndex = getBeginIndex(threadNo, blockSize);
            int endIndex;
            if(threadNo == threadsPerPlayer - 1){
                // have to add any extras into the last bucket
                endIndex = arraySize;
            } else{
                endIndex = beginIndex + blockSize;
            }
            return endIndex;
        }

        void addTasks(ArrayList<Callable<String>> taskList,
                      ArrayList<ResultsEntry> resultsEntries,
                      ArrayList<DBMgrSim> dbMgrSims,
                      String tableName, int threadsPerPlayer, int blockSize){
            int beginIndex;
            int endIndex;
            int arraySize = resultsEntries.size();
            for(int threadNo = 0; threadNo < threadsPerPlayer; threadNo++){
                beginIndex = getBeginIndex(threadNo, blockSize);
                endIndex = getEndIndex(threadNo, blockSize, threadsPerPlayer, arraySize);

                Callable<String> task = new WriteEntriesToDb(tableName,
                        resultsEntries, dbMgrSims.get(threadNo), beginIndex, endIndex);
                taskList.add(task);
            }
        }

        void writeEntryToDb(String tableName, ResultsEntry entry, DBMgrSim dbMgrSim){

            String statestr = getPsSqlString(tableName);

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
                for(int loopIndex = i ; loopIndex <= 12; loopIndex++){
                    ps.setNull(loopIndex, Types.NULL);
                }
                ps.execute();
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }

        String getPsSqlString(String tableName){
            return "insert into " + tableName +
                    "(\n" +
                    "hashid, total, attribute, result,\n" +
                    "card1,card2,card3,card4,card5,card6,card7,card8)\n" +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?);";
        }
    }

    @Override
    boolean playAnotherHand() {
        gamesPlayed += 1;
        if(gamesPlayed < iterations){
            return true;
        } else{
            gamesPlayed = 0;
            return false;
        }
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
