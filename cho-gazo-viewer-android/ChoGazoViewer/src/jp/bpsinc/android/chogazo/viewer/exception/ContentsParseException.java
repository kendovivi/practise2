
package jp.bpsinc.android.chogazo.viewer.exception;

public class ContentsParseException extends Exception {
    private static final long serialVersionUID = 1L;

    public ContentsParseException(String message) {
        super(message);
    }

    public ContentsParseException(String message, Throwable ex) {
        super(message, ex);
    }
}
