package BFModel;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-01-28.
 * Implementation for instruction ,.
 */
public class InputInstruction implements Instruction {

    @Override
    public Cell run(BFModel b) {
        Cell c = b.getCurrentCell();
        c.setValue(b.getNextInput());
        return c;
    }
}
