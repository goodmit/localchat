package exceptions;

/**
 * Created by Дмитрий on 02.04.2017.
 */
public class JoinFailException extends Exception {
    private int reason = 0;

    public JoinFailException () {
    }

    public JoinFailException (int reason) {
        this.reason = reason;
    }

    public int getReasonStatus() {
        return reason;
    }
}
