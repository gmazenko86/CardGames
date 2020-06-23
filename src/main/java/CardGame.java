import java.util.*;
import java.util.stream.LongStream;

public class CardGame {
    final Deck deck;

    CardGame(){
        this.deck = new Deck();
        this.deck.shuffle();
    }

    public static void main(String... args){
        String dbConfigFilePath = "/home/greg/PersonalCodingExercises/" +
                "DbaseExercises/src/main/resources/config.txt";
        BJackGame bJackGame = new BJackGame(dbConfigFilePath);
        bJackGame.playGame();
    }

    static ArrayList<Integer> getIndexRandOrder(long numIntegers){
        Random random = new Random();
        LongStream longStream = random.longs(numIntegers);
        LongStream distinctStream = longStream.distinct();

        long[] randomLongs = distinctStream.toArray();

        // make sure there are no duplicate values. If so, regenerate the stream
        while(randomLongs.length != numIntegers){
            longStream = random.longs(numIntegers);
            distinctStream = longStream.distinct();
            randomLongs = distinctStream.toArray();
        }

        // create a map of random longs to integers
        int index = 0;
        HashMap<Long, Integer> unsortedHmap = new HashMap<>();
        for(long longs : randomLongs){
            index += 1;
            unsortedHmap.put(longs, index);
        }
        // treemap ordered based on the natural order of the keys
        TreeMap<Long, Integer> treeMap = new TreeMap<>(unsortedHmap);

        ArrayList<Integer> integerArrayList= new ArrayList<>();
        treeMap.forEach((k, v) -> integerArrayList.add(v));

        return integerArrayList;
    }

}
