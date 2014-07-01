
package jp.bpsinc.android.chogazo.viewer.exception;

public class ContentsOtherException extends Exception {
    private static final long serialVersionUID = 1L;

    public ContentsOtherException(String message) {
        super(message);
    }

    public ContentsOtherException(String message, Throwable ex) {
        super(message, ex);
    }
}
