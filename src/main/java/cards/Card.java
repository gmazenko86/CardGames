package cards;

public class Card {
    public enum CardFace {ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING}
    public enum Suit { DIAMONDS, HEARTS, CLUBS, SPADES}

    public CardFace cardFace;
    public Suit suit;

    public Card(){}

    public Card(CardFace face, Suit suit){
        this.cardFace = face;
        this.suit =suit;
    }

    public String getCardSignature(){
        return this.cardFace.name() + " " + this.suit.name();
    }

    public int getCardValue(){
        switch (cardFace){
            case ACE: return 11;
            case KING:
            case QUEEN:
            case JACK:
            case TEN:
                return 10;
            case NINE: return 9;
            case EIGHT: return 8;
            case SEVEN: return 7;
            case SIX: return 6;
            case FIVE: return 5;
            case FOUR: return 4;
            case THREE: return 3;
            case TWO: return 2;
            default: return -1; // invalid case
        }
    }
}
