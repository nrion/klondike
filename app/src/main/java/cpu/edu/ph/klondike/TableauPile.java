package cpu.edu.ph.klondike;

import android.graphics.Canvas;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TableauPile {
    private static final String TAG = TableauPile.class.getSimpleName();

    private LinkedList<Card> cards = new LinkedList<>();
    private int x;

    public TableauPile(int x) {
        this.x = x;
    }

    public void addCard(Card card) {
        int y;

        if (cards.isEmpty()) {
            y = Card.getHeight() / 4;
        }
        else {
            Card topCard = getTopmostCard();
            y = topCard.getY() + Card.getHeight() / 4;
        }

        card.move(this.x, y);
        cards.add(card);
    }

    public void addCards(List<Card> cards) {
        for (Card card : cards) {
            addCard(card);
        }
    }

    public void removeCards(int howMany) {
        int j = 1;
        for (Iterator i = cards.descendingIterator(); j <= howMany; j++) {
            i.next();
            i.remove();
        } // safe way to remove list items inside a loop
    }

    public void revealLast() {
        if (!cards.isEmpty()) {
            cards.get(cards.size() - 1).reveal();
        }
    }

    public LinkedList<Card> getCards() {
        return cards;
    }

    public Card getTopmostCard() {
        if (cards.isEmpty()) {
            return null;
        }

        return cards.get(cards.size() - 1);
    }

    public void draw(Canvas canvas) {
        for (Card card : new LinkedList<>(cards)) {
            card.draw(canvas);
        }
    }

    public int getDistance(Card draggedCard) {
        int y = Card.getHeight() / 4;

        if (!cards.isEmpty()) {
            Card topCard = getTopmostCard();

            return (int) Math.sqrt(
                    Math.pow(draggedCard.getX() - topCard.getX(), 2) +
                            Math.pow(draggedCard.getY() - topCard.getY(), 2)
            );
        }
        return (int) Math.sqrt(
                Math.pow(draggedCard.getX() - x, 2) +
                        Math.pow(draggedCard.getY() - y, 2)
        );
    }

    public boolean canAccept(Card draggedCard) {
        if (!cards.isEmpty()) {
            Card topCard = getTopmostCard();
            int difference = topCard.getValue() - draggedCard.getValue();

            return draggedCard.getColor() != topCard.getColor()
                && difference == 1;
        }
        return draggedCard.getValue() == 13;
    }
}
