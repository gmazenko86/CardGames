import com.sun.jdi.Value;

import javax.swing.text.Element;
import java.security.Key;
import java.util.*;
import java.util.stream.LongStream;

public class CardGame {
    public static void main(String... args){
        BJackGame bJackGame = new BJackGame();
        bJackGame.playGame();
    }

    enum CardFace {TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE }
    enum Suit { DIAMONDS, HEARTS, CLUBS, SPADES}

    void playGame(){
        Deck deck = new Deck();

        deck.shuffle();

        for(Card card : deck.cards){
            System.out.println(card.getCardSignature());
        }
        System.out.println("Generic message from parent. Object = " + this.toString());
    }
    protected static class Card{
        CardFace cardFace;
        Suit suit;

        String getCardSignature(){
            return this.cardFace.name() + " " + this.suit.name();
        }
    }

    protected class Deck {
        ArrayList<Card> cards;

        Deck(){
            this.cards = new ArrayList<>();
            CardFace[] cardFace = CardFace.values();
            Suit[] cardSuit = Suit.values();
            for(CardFace face: cardFace){
                for (Suit suit : cardSuit){
                    Card newCard = new Card();
                    newCard.cardFace = face;
                    newCard.suit = suit;
                    (this.cards).add(newCard);
                }
            }

        }

        void shuffle(){
            int numCards = this.cards.size();
            getIndexRandOrder(numCards);
        }

        int[] getIndexRandOrder(long numInts){
            Random random = new Random();
            LongStream longStream = random.longs(numInts);
            LongStream distinctStream = longStream.distinct();

            long[] randomLongs = distinctStream.toArray();

            // make sure there are no duplicate values. If so, regenerate the stream
            while(randomLongs.length != numInts){
                longStream = random.longs(numInts);
                distinctStream = longStream.distinct();
                randomLongs = distinctStream.toArray();
            }

            // create a map of random longs to integers
            int index = 0;
            HashMap<Long, Integer> unsortedHmap = new HashMap();
            for(long longs : randomLongs){
                index += 1;
                unsortedHmap.put(longs, index);
            }
            // treemap ordered based on the natural order of the keys
            TreeMap<Long, Integer> treeMap = new TreeMap();
            treeMap.putAll(unsortedHmap);

            ArrayList<Integer> integerArrayList= new ArrayList<>();

            treeMap.forEach((k, v) -> integerArrayList.add(v));

            System.out.println("Ready to sort the hashmap");

            return new int[0];
        }
    }
}
