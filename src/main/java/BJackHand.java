public class BJackHand extends Hand{

    BJackHand(){
        super();
    }

    public int getHardTotal(){
        int hardTotal = 0;
        for(Card card: this.cards){
            hardTotal += card.getCardValue();
        }
        return hardTotal;
    }
}
