
package jp.bpsinc.android.chogazo.viewer.exception;

public class EpubParseException extends ContentsParseException {
    private static final long serialVersionUID = 1L;

    public EpubParseException(String message) {
        super(message);
    }

    public EpubParseException(String message, Throwable ex) {
        super(message, ex);
    }
}
