public class BJackHand extends Hand{

    BJackHand(){
        super();
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
