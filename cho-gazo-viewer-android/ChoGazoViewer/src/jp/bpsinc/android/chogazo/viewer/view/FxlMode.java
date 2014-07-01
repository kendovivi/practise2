
package jp.bpsinc.android.chogazo.viewer.view;

public class FxlMode extends EbookMode {
    public FxlMode(boolean isLandscape, boolean isSpread) {
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
            mMode = Mode.PORTRAIT_STANDARD;
        }
    }

    @Override
    protected void setIsVertical() {
        mIsVerticalScroll = false;
    }

    @Override
    public ContentMode getContentMode() {
        return ContentMode.FXL;
    }
}
