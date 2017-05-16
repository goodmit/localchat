package server;

/**
 * Created by d.gusev on 04.04.2017.
 */
public class ErrorTypes {

    static final int PASS_INCORRECT = 11;

    static final int JOIN_OK = 100;
    static final int JOIN_LOGIN_BUSY = 101;
    static final int JOIN_LOGIN_SHORT = 102;
    static final int JOIN_LOGIN_LONG = 103;
    static final int JOIN_LOGIN_INCORRECT = 104;
    static final int JOIN_PASS_SHORT = 105;
    static final int JOIN_PASS_LONG = 106;
    static final int JOIN_NICK_SHORT = 107;
    static final int JOIN_NICK_LONG = 108;
    static final int JOIN_NICK_INCORRECT = 109;

    static final int AUTH_OK = 200;
    static final int AUTH_LOGIN_INCORRECT = 201;
    static final int AUTH_INCORRECT = 202;

    static final int DB_OK = 300;
    static final int DB_BUSY = 301;
    static final int DB_DATA_INCORRECT = 302;
    static final int DB_REQUEST_INCORRECT = 303;
}
