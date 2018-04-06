package cpu.edu.ph.klondike;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

public class Card {
    private static final String TAG = Card.class.getSimpleName();

    private int value;  // 1 - 13
    private char suit; // enum is better
    private String displayValue;
    private String displaySuit;
    private int color;
    private boolean open;

    private int x; // top-left corner
    private int y;

    private static int width;
    private static int height;

    private boolean touched;

    private static final int MARGIN = 10; // same as above

    public static void setSizeOfAllCards(int screenWidth, int screenHeight) {
        Card.width = screenWidth / 10 - MARGIN;
        Card.height = screenHeight / 5;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public Card(int value, char suit, int x, int y) {
        this.value = value;
        this.suit = suit;

        displayValue = "" + value;

        switch (value) {
        case 1:
            displayValue = "A";
            break;
        case 11:
            displayValue = "J";
            break;
        case 12:
            displayValue = "Q";
            break;
        case 13:
            displayValue = "K";
            break;
        }

        switch (suit) {
        case 'C':
            displaySuit = "♣";
            break;
        case 'S':
            displaySuit = "♠";
            break;
        case 'H':
            displaySuit = "♥";
            break;
        case 'D':
            displaySuit = "♦";
            break;
        }

        color = suit == 'C' || suit == 'S' ? Color.BLACK : Color.RED;
        move(x, y);
    }

    public int getValue() {
        return value;
    }

    public int getColor() {
        return color;
    }

    public char getSuit() {
        return suit;
    }

    public void flip() {
        open = !open; // negates the boolean open
    }

    public void reveal() {
        open = true;
    }

    public void conceal() {
        open = false;
    }

    public boolean isOpen() {
        return open;
    }

    public void move(int x, int y) {
        // Log.d(TAG, "Moved to " + x +"," +y);
        this.x = x;
        this.y = y;
    }

    public void moveByDelta(int deltaX, int deltaY) {
        this.x += deltaX;
        this.y += deltaY;
    }

    public String toString() {
        return displayValue + displaySuit + " (" + x + ", " + y + ")";
    }

    public void setTouched(boolean touched) {
        this.touched = touched;
    }

    public boolean isTouched() {
        return touched;
    }

    public void handleTouch(int eventX, int eventY) {
        setTouched(false);

        if (eventX >= x && eventX <= x + width) {
            if (eventY >= y && eventY <= y + height) {
                setTouched(true);
            }
        }
    }

    public void draw(Canvas canvas) {
        int srcX = 0;
        int srcY = 0;

        switch (suit) {
        case 'C':
            srcY = 0;
            break;
        case 'H':
            srcY = getHeight();
            break;
        case 'S':
            srcY = getHeight() * 2;
            break;
        case 'D':
            srcY = getHeight() * 3;
            break;
        }

        Bitmap largePic = MainGameView.getPicture();
        int tileWidth = largePic.getWidth() / 13;
        int tileHeight = largePic.getHeight() / 5;

//        if (value == 1) {
//            srcX = (int) Math.round(largePic.getWidth() / 13.0 * 0);
//        }
//        else {
            srcX = (int) Math.round(largePic.getWidth() / 13.0 * (value - 1));
//        }

        if (!open) {
            srcX = (int) Math.round(0 /*/ 13 * largePic.getWidth()*/);
            srcY = tileHeight * 4;
        }

        Bitmap cardImage = Bitmap.createBitmap(
                largePic, srcX, srcY, tileWidth, tileHeight
        );
        cardImage = Bitmap.createScaledBitmap(
                cardImage, width, height, true
        );
        canvas.drawBitmap(
            cardImage, x, y, null
        );
    }
}
