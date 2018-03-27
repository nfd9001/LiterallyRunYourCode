package BFModel;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-01-28.
 * Implements the instruction [.
 */
public class LoopHeadInstruction implements Instruction {
    @Override
    public Cell run(BFModel b) throws InvalidModelStateException {
        Cell c = b.getCurrentCell();
        if (c.getValue() == 0){
            int j = b.getNumberOfInstructions();
            char[] instructions = b.getInstructions();
            int i = b.getCurrentInstructionIndex();
            while (instructions[i] != ']'){
                if (i == j - 1){
                    //should never happen
                    throw new InvalidModelStateException("Skipped through a [ without a corresponding ].");
                }
                i++;
            }
            b.setCurrentInstructionIndex(i - 1); //since outer loop increments counter
        } else{
            b.getLoopHeadStack().push(b.getCurrentInstructionIndex()); //again, outer loop increments counter
        }
        return c;
    }
}
