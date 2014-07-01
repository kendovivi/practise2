
package jp.bpsinc.android.chogazo.viewer.exception;

public class DrmException extends Exception {
    private static final long serialVersionUID = 1L;

    public DrmException(String message) {
        super(message);
    }

    public DrmException(String message, Throwable ex) {
        super(message, ex);
    }
}
