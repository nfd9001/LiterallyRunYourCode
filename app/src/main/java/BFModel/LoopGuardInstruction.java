package BFModel;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-01-28.
 * Implements the instruction ].
 */
public class LoopGuardInstruction implements Instruction{
    @Override
    public Cell run(BFModel b) {
        Cell c = b.getCurrentCell();
        if (c.getValue() != 0){
            b.setCurrentInstructionIndex(b.getLoopHeadStack().peek());
        } else{
            b.getLoopHeadStack().pop();
        }
        return c;
    }
}
