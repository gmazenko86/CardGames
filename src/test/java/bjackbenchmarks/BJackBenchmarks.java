package bjackbenchmarks;

import bjack.BJackGameSim;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1)
@Warmup(iterations = 3, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 1, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
public class BJackBenchmarks {

    public static void main(String... args) throws IOException, RunnerException {
        org.openjdk.jmh.Main.main(args);
    }

    // 2 benchmarks below compare writing to the db with 2 threads vs 8
    @Benchmark
    public void bmark2threads(){
        String dbConfigFilePath = "src/main/resources/config.txt";

        BJackGameSim bJackGameSim = new BJackGameSim(5000, 2, dbConfigFilePath);
        bJackGameSim.playGameWrapper();
        bJackGameSim.dbMgr.truncateTable("dealerhands");
        bJackGameSim.dbMgr.truncateTable("playerhands");
    }

    @Benchmark
    public void bmark8threads(){
        String dbConfigFilePath = "src/main/resources/config.txt";

        BJackGameSim bJackGameSim = new BJackGameSim(5000, 8, dbConfigFilePath);
        bJackGameSim.playGameWrapper();
        bJackGameSim.dbMgr.truncateTable("dealerhands");
        bJackGameSim.dbMgr.truncateTable("playerhands");
    }
}
