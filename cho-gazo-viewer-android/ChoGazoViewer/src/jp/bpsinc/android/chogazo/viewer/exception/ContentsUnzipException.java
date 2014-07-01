
package jp.bpsinc.android.chogazo.viewer.exception;

public class ContentsUnzipException extends Exception {
    private static final long serialVersionUID = 1L;

    public ContentsUnzipException(String message) {
        super(message);
    }

    public ContentsUnzipException(String message, Throwable ex) {
        super(message, ex);
    }
}
