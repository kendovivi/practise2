
package jp.bpsinc.android.chogazo.viewer.exception;

public class UnexpectedException extends Exception {
    private static final long serialVersionUID = 1L;

    public UnexpectedException(String message) {
        super(message);
    }

    public UnexpectedException(String message, Throwable ex) {
        super(message, ex);
    }
}
