package BFModel;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-01-28.
 * Implements the instruction >.
 */
public class NextInstruction implements Instruction{
    @Override
    public Cell run(BFModel b) throws InvalidModelStateException {
        Cell c = b.getCurrentCell();
        if (c.getNext() == null){
            if (!b.appendCell()){
                throw new InvalidModelStateException("Code generated over the maximum supported number of cells.");
            }
        }
        return c.getNext();
    }
}
