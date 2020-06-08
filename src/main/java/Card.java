class Card {
    enum CardFace {TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE}
    enum Suit { DIAMONDS, HEARTS, CLUBS, SPADES}

    CardFace cardFace;
    Suit suit;

    String getCardSignature(){
        return this.cardFace.name() + " " + this.suit.name();
    }

    void displayCardSignature(){
        System.out.print(getCardSignature());
    }
}
