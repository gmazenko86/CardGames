import java.util.ArrayList;

public class CardGame {
    public static void main(String... args){
        BJackGame bJackGame = new BJackGame();
        bJackGame.playGame();
    }

    enum CardFace {TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE }
    enum Suit { DIAMONDS, HEARTS, CLUBS, SPADES}

    void playGame(){
        Deck deck = new Deck();

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
    }
}
