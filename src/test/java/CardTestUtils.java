import org.apache.commons.lang3.RegExUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardTestUtils {
    static public boolean fixDeck(Deck deck, TreeMap<Integer, Card> seedCards) {
        // first, remove the seed cards from the existing deck so they are not duplicated
            // note that the cards in seedCards != the cards in deck since they are different
            // objects and have different addresses. Have to check for equal contents and
            // remove the corresponding card from the deck using the correct reference

        ArrayList<Card> swapArray = new ArrayList<>(deck.cards);
        seedCards.forEach((k, v)-> {
            for (Card card : deck.cards) {
                if(v.cardFace == card.cardFace && v.suit == card.suit){
                    swapArray.remove(card);
                }
            }
        });
        deck.cards.clear();
        deck.cards.addAll(swapArray);

        // now add the seed cards to the deck in the spots defined by the TreeMap keys
        seedCards.forEach((k, v) -> deck.cards.add(k, v));
        return checkDeckIntegrity(deck);
    }

    static public TreeMap<Integer, Card> fixDeckEntries(String filePath) {

        ArrayList<String> inputLines = CardTestUtils.readLinesAsStrings(filePath);
        TreeMap<Integer, Card> treeMap = new TreeMap<>();

        // first find an integer, which represents the position in the deck
        Pattern pattern1 = Pattern.compile("[0-9]+");
        // then find the card face and suit. Same regex works for both
        Pattern pattern2 = Pattern.compile("[A-Z]+");

        for(String inputLine : inputLines){
            Deck refDeck = new Deck();
            Matcher matcher1 = pattern1.matcher(inputLine);
            if(matcher1.find()){
                String token1 = matcher1.group();
                String substring1 = RegExUtils.removeFirst(inputLine, pattern1);
                Matcher matcher2 = pattern2.matcher(substring1);
                if(matcher2.find()){
                    String token2 = matcher2.group();
                    String substring2 = RegExUtils.removeFirst(substring1,pattern2);
                    Matcher matcher3 = pattern2.matcher(substring2);
                    if(matcher3.find()){
                        String token3 = matcher3.group();
                        Integer treeMapKey = Integer.valueOf(token1);
                        for(Card card : refDeck.cards){
                            if(card.cardFace.name().equals(token2) &&
                                card.suit.name().equals(token3)){
                                Card saveCard = getCard(card.cardFace, card.suit);
                                treeMap.put(treeMapKey, saveCard);
                            }
                        }
                    }
                }
            }
        }
        return treeMap;
    }

    static public Card getCard(Card.CardFace face, Card.Suit suit) {
        Card card = new Card();
        card.cardFace = face;
        card.suit = suit;
        return card;
    }

    static public boolean checkDeckIntegrity(Deck deck) {
        ArrayList<Card> swapList = new ArrayList<>(deck.cards);

        Deck refDeck = new Deck();
        // compare the deck sent as a parameter to the newly created refDeck
        // refDeck is assumed to be good since it was just created with the constructor
        for(Card card : refDeck.cards) {
            boolean foundCard = false;
            for (Card findCard : deck.cards) {
                if (findCard.cardFace == card.cardFace && findCard.suit == card.suit) {
                    foundCard = swapList.remove(findCard);
                    // once you find one instance, stop looking so that duplicates
                    // remain in the deck and will be detected as errors later
                    break;
                }
            }
            if (!foundCard) { return false; }
        }
        // list will be empty if each card was in the deck 1 and only 1 time
        return swapList.isEmpty();
    }

    //TODO: this is duplicated from DbaseDemo.java. Build a reusable library
    public static ArrayList<String> readLinesAsStrings(String inputPath){
        try (BufferedReader br = new BufferedReader(new FileReader(inputPath))) {
            ArrayList<String> arrayList = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                arrayList.add(line);
            }
            return arrayList;
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
