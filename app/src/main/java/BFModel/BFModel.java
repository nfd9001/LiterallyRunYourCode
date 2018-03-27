package BFModel;

import android.util.SparseArray;

import java.util.*;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-01-27.
 * Represents and provides access and controls to an entire instance of a BF virtual machine
 */
public class BFModel {
    private static final int MAX_NUMBER_CELLS = 255;
    private List<Cell> cells = new ArrayList<>();
    private Cell currentCell;
    private Stack<Integer> loopHeadStack = new Stack<>();
    private Queue<Integer> inputQueue;
    private char[] instructions;
    private int numberOfInstructions;
    private int currentInstructionIndex = 0;
    private int stepCount = 0;
    private static SparseArray<Instruction> instructionMap;
    private static List<Character> legalChars = Arrays.asList(',', '.', '[',']','<','>','+', '-');
    private String output = "";
    private boolean halted = false;

    /**
     * Constructs a brainfuck debugger for the given code and input.
     *
     * @param instructions Instructions for the debugger to execute. This should already strip all non-instructive
     *                    characters.
     * @param input An input for the program in the debugger to parse.
     * @throws InvalidModelStateException When the guest code violates the boundaries of the VM (here, mismatched brackets).
     */
    public BFModel(String instructions, String input) throws InvalidModelStateException{
        //TODO: strip invalid chars
        if (!validateInstructions(instructions)){
            throw new InvalidModelStateException("Set of instructions contained an illegal op (unclosed loop, bad character).");
        }
        inputQueue = new LinkedList<>();
        char[] in = input.toCharArray();
        for(char i : in){
            if ((int) i > 255){
                throw new InvalidModelStateException("Input contained non-ASCII characters.");
            }
            inputQueue.add((int)i);

        }
        initInstructionMap();

        numberOfInstructions = instructions.length();
        this.instructions = instructions.toCharArray();
        currentCell = new Cell();
        cells.add(currentCell);

    }

    char[] getInstructions() {
        return instructions;
    }

    /**
     * Gets the current instruction index.
     * @return The current instruction index.
     */
    public int getCurrentInstructionIndex() {
        return currentInstructionIndex;
    }

    void setCurrentInstructionIndex(int currentInstructionIndex) {
        this.currentInstructionIndex = currentInstructionIndex;
    }

    /**
     * Gets the current Cell.
     * @return The current Cell.
     */
    public Cell getCurrentCell() {
        return currentCell;
    }

    void setCurrentCell(Cell currentCell) {
        this.currentCell = currentCell;
    }

    boolean prependCell(){
        if (cells.size() == MAX_NUMBER_CELLS){return false;}
        Cell c = new Cell(null, cells.get(0));
        cells.get(0).setPrev(c);
        cells.add(0, c);
        return true;
    }
    boolean appendCell(){
        if (cells.size() == MAX_NUMBER_CELLS){return false;}
        Cell c = new Cell(cells.get(cells.size() - 1), null);
        cells.get(cells.size() - 1).setNext(c);
        cells.add(c);
        return true;
    }

    /**
     * Gets a list of all Cells at the machine's current state.
     * @return The machine's list of Cells.
     */
    public List<Cell> getCells() {
        return cells;
    }

    Stack<Integer> getLoopHeadStack() {
        return loopHeadStack;
    }

    private static void initInstructionMap(){
        instructionMap = new SparseArray<>();
        instructionMap.put((int)'+', new IncrementInstruction());
        instructionMap.put((int)'-', new DecrementInstruction());
        instructionMap.put((int)'>', new NextInstruction());
        instructionMap.put((int)'<', new PrevInstruction());
        instructionMap.put((int)'.', new OutputInstruction());
        instructionMap.put((int)',', new InputInstruction());
        instructionMap.put((int)'[', new LoopHeadInstruction());
        instructionMap.put((int)']', new LoopGuardInstruction());
    }
    int getNextInput(){
        if (inputQueue.isEmpty()){
            return 0;
        }
        return inputQueue.remove();
    }

    public String getOutput() {
        return output;
    }

    void appendToOutput(char c){
        output = output + c;
    }

    private boolean validateInstructions(String instructions){
        int unclosedLeftBrackets = 0;
        for (char c : instructions.toCharArray()){
            if(!legalChars.contains(c)){
                return false;
            }
            if (c == '['){
                unclosedLeftBrackets++;
            }else if (c == ']'){
                unclosedLeftBrackets--;
            }else{
                continue;
            }
            if (unclosedLeftBrackets < 0){
                return false;
            }
        }
        return (unclosedLeftBrackets == 0);
    }

    public int getStepCount() {
        return stepCount;
    }

    /**
     * Steps through one instruction in the model.
     * @return True if the machine is halted (in which case, you shouldn't call step again); otherwise, false.
     * @throws InvalidModelStateException Thrown if this step would cause one of the machine's boundaries to no longer hold.
     */
    public boolean step() throws InvalidModelStateException {
        if (halted){
            throw new InvalidModelStateException("Tried to step on a halted program.");
        }
        if (currentInstructionIndex >= numberOfInstructions){
            halted = true;
        }
        else {
            setCurrentCell(instructionMap.get(instructions[currentInstructionIndex]).run(this));
            currentInstructionIndex++;
            stepCount++;
        }
        return halted;
    }

    int getNumberOfInstructions() {
        return numberOfInstructions;
    }

    /**
     * Tells you if the machine is halted.
     * @return True if the machine is halted, false otherwise.
     */
    public boolean isHalted(){
        return halted;
    }

    /**
     * Strips comment characters.
     * @param s String to strip.
     * @return String stripped of comments.
     */
    public static String stripComments(String s){
        return s.replaceAll("[^+\\-\\[\\]<>,.]" , "");
    }
    public int getCurrentCellPosition(){
        return cells.indexOf(currentCell);
    }
}
