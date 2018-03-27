package BFModel;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-01-27.
 * Interface for instructions
 */
public interface Instruction {
    /**
     *
     * @param b BFModel on which we're operating
     * @return The current cell after the instruction has completed.
     * @throws InvalidModelStateException Thrown when calling code violates the model's limitations.
     */
    Cell run(BFModel b) throws InvalidModelStateException;
}
