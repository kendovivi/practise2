
package jp.bpsinc.android.chogazo.viewer.exception;

public class LoadImageException extends Exception {
    private static final long serialVersionUID = 1L;

    public LoadImageException(String message) {
        super(message);
    }

    public LoadImageException(String message, Throwable ex) {
        super(message, ex);
    }
}
