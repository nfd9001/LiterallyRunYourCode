package BFModel;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-01-27.
 */
public class DecrementInstruction implements Instruction {
    @Override
    public Cell run(BFModel b) throws InvalidModelStateException{
        Cell c = b.getCurrentCell();

        //should never happen now; leaving this in case we change overflow behavior
        if (!c.decrementValue()){
            throw new InvalidModelStateException("Tried to decrement cell below minimum value.");
        }
        return c;
    }
}
