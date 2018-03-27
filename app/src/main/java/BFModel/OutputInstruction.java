package BFModel;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-01-28.
 * Implements the instruction .
 */
public class OutputInstruction  implements Instruction{
    @Override
    public Cell run(BFModel b) {
        Cell c = b.getCurrentCell();
        b.appendToOutput(c.getValueChar());
        return c;
    }
}
