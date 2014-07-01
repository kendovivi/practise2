
package jp.bpsinc.android.chogazo.viewer.view;

public class OmfMode extends EbookMode {
    public OmfMode(boolean isLandscape, boolean isSpread) {
        super(isLandscape, isSpread);
    }

    @Override
    protected void setMode() {
        if (mIsLandscape) {
            if (mIsSpread) {
                mMode = Mode.LANDSCAPE_SPREAD;
            } else {
                mMode = Mode.LANDSCAPE_STANDARD;
            }
        } else {
            if (mIsSpread) {
                mMode = Mode.PORTRAIT_SPREAD;
            } else {
                mMode = Mode.PORTRAIT_STANDARD;
            }
        }
    }

    @Override
    protected void setIsVertical() {
        if (mMode == Mode.LANDSCAPE_STANDARD) {
            mIsVerticalScroll = true;
        } else {
            mIsVerticalScroll = false;
        }
    }

    @Override
    public ContentMode getContentMode() {
        return ContentMode.OMF;
    }
}
