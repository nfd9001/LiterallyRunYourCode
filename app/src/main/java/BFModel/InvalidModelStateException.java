package BFModel;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-01-27.
 * Exception used by BFModel to indicate some error
 */
public class InvalidModelStateException extends Exception {
    public InvalidModelStateException(){
        super();
    }
    public InvalidModelStateException(String s){
        super(s);
    }
}
