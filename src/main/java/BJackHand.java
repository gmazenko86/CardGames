public class BJackHand extends Hand{

    // these attributes drive special handling
    enum HandAttribute {NONE, STICK, BLACKJACK, BUST, SPLITHAND, SURRENDER}
    // HandResult represents the final disposition of a player hand and determines payments
    enum  HandResult {PENDING, WIN, LOSE, PUSH}

    HandAttribute handAttribute;
    HandResult handResult;

    BJackHand(){
        super();
        handAttribute = HandAttribute.NONE;
        handResult = HandResult.PENDING;
    }

    public int getHandTotal(){
        int total = 0;
        int aceCount = 0;
        for(Card card: this.cards){
            if(card.cardFace == Card.CardFace.ACE){
                aceCount += 1;
            }
            total += card.getCardValue();
        }
        while((aceCount > 0) && (total > 21)){
            total -= 10;
            aceCount -= 1;
        }
        return total;
    }
}
