package exceptions;

/**
 * Created by Tim on 13.09.2016.
 */
public class AuthFailException extends Exception {
    private int reason = 0;

    public AuthFailException () {
    }

    public AuthFailException (int reason) {
        this.reason = reason;
    }

    public int getReasonStatus() {
        return reason;
    }
}
