package BFModel;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-01-27.
 * Implementation for instruction +.
 */
public class IncrementInstruction implements Instruction {
    @Override
    public Cell run(BFModel b) throws InvalidModelStateException{
        Cell c = b.getCurrentCell();
        if (!c.incrementValue()){
            throw new InvalidModelStateException("Tried to increment a cell past the maximum value.");
        }
        return c;
    }
}
