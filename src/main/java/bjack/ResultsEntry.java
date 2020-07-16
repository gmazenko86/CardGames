package bjack;

import cards.Card;

import java.util.ArrayList;

public class ResultsEntry {
    int handHashId;
    int handTotal;
    BJackHand.HandAttribute handAttribute;
    BJackHand.HandResult handResult;
    final ArrayList<Card> cards;

    ResultsEntry(){
        this.cards = new ArrayList<>();
    }

}
