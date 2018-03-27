package BFModel;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-01-27.
 * Data structure for a single cell, utility methods
 */
public class Cell {
    private static final int MAX_CELL_VALUE = 255;
    private static final int MIN_CELL_VALUE = 0;
    private int value;

    private Cell prev;
    private Cell next;

    Cell(){
        value = 0;
        prev = null;
        next = null;
    }
    Cell(Cell prev, Cell next){
        value = 0;
        this.prev = prev;
        this.next = next;
    }

    Cell getNext() {
        return next;
    }

    //these aren't intended to rearrange a list; they're only setters for the ends
    void setNext(Cell next) {
        if (this.next == null) {
            this.next = next;
        }
    }

    Cell getPrev() {
        return prev;
    }

    void setPrev(Cell prev) {
        if (this.prev == null) {
            this.prev = prev;
        }
    }

    public int getValue() {
        return value;
    }

    void setValue(int value) {
        this.value = value;
    }

    public char getValueChar(){
        if (value >= 32 && value < 256){
            return (char) value;
        }
        else{
            return '\0';
        }
    }

    boolean incrementValue(){
        if (value < MAX_CELL_VALUE){
            value++;
            return true;
        }
        value = MIN_CELL_VALUE;
        return true;
    }

    boolean decrementValue(){
        if (value > MIN_CELL_VALUE){
            value--;
            return true;
        }
        value = MAX_CELL_VALUE;
        return true;
    }
    public String toString(){
        return getValue() + "\n" + getValueChar();
    }
}
