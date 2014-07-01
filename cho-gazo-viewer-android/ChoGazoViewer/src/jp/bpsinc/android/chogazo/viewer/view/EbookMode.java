
package jp.bpsinc.android.chogazo.viewer.view;

/**
 * ビューアの各種設定を保持する<br>
 * このクラスのインスタンスをビューアに1つ作成し、全ての箇所で使用できるようにクラス間で引き回す<br>
 * 複数のインスタンスを作成したり、このクラス内に保持する情報(端末の向きや見開き情報など)を別のクラス上に分散して保持してはいけない<br>
 * ビューアに1つだけ存在するこのクラスのインスタンスの設定値を1回変更すれば、それを使用している全ての箇所に反映されるように使用すること
 */
public abstract class EbookMode {
    private static final TapAction DEFAULT_TAP_ACTION = TapAction.NORMAL;
    private static final PageAnimation DEFAULT_PAGE_ANIMATION = PageAnimation.SLIDE;
    private static final int DEFAULT_AUTO_PLAY_INTERVAL = 5;
    private static final boolean DEFAULT_LOOP_PLAY = false;
    private static final AutoPlayDirection DEFAULT_AUTO_PLAY_DIRECTION = AutoPlayDirection.FORWARD;

    /** 画面の表示モード */
    public enum Mode {
        /** 画面の向き：縦、ページ：単一 */
        PORTRAIT_STANDARD,
        /** 画面の向き：縦、ページ：見開き */
        PORTRAIT_SPREAD,
        /** 画面の向き：横、ページ：単一 */
        LANDSCAPE_STANDARD,
        /** 画面の向き：横、ページ：見開き */
        LANDSCAPE_SPREAD,
    }

    /** コンテンツごとの動作モード */
    public enum ContentMode {
        /** Fixed-Layout */
        FXL,
        /** OpenMangaFormat */
        OMF,
    }

    /** 画像表示直後の表示方法(ここで指定したフィットモードに対応する初期サイズが最低縮小率になる) */
    public enum FitMode {
        /** 画像を画面にアスペクトフィットさせる */
        ASPECT_FIT,
        /** 画像の横幅を画面の横幅にフィットさせる */
        WIDTH_FIT,
        /** 画像の縦幅を画面の縦幅にフィットさせる */
        HEIGHT_FIT,
    }

    /** タップ時の動作設定 */
    public enum TapAction {
        /** 左右領域タップでタップした方向へページ移動 */
        NORMAL,
        /** 左右領域どちらをタップしても次のページへ移動 */
        NEXT,
        /** 左右領域はタップしても何も動作しない */
        NONE;

        public static TapAction menuValueOf(String name) {
            TapAction tapAction;
            try {
                tapAction = TapAction.valueOf(name);
            } catch (Exception e) {
                tapAction = DEFAULT_TAP_ACTION;
            }
            return tapAction;
        }
    }

    /** ページめくり時のアニメーション効果設定 */
    public enum PageAnimation {
        /** ページアニメーション：スライド */
        SLIDE,
        /** ページアニメーション：なし */
        NONE;

        public static PageAnimation menuValueOf(String name) {
            PageAnimation animation;
            try {
                animation = PageAnimation.valueOf(name);
            } catch (Exception e) {
                animation = DEFAULT_PAGE_ANIMATION;
            }
            return animation;
        }
    }

    /** 自動再生方向設定 */
    public enum AutoPlayDirection {
        /** 自動再生方向：順方向 */
        FORWARD,
        /** 自動再生方向：逆方向 */
        REVERSE,
        /** 自動再生方向：ランダム */
        RANDOM;

        public static AutoPlayDirection menuValueOf(String name) {
            AutoPlayDirection direction;
            try {
                direction = AutoPlayDirection.valueOf(name);
            } catch (Exception e) {
                direction = DEFAULT_AUTO_PLAY_DIRECTION;
            }
            return direction;
        }
    }

    /** 画面の表示モード */
    protected Mode mMode;
    /** 画像表示直後の表示方法 */
    protected FitMode mFitMode;
    /** 端末が横向きか */
    protected boolean mIsLandscape;
    /** 現在の設定が見開きか */
    protected boolean mIsSpread;
    /** 現在のモードが縦スクロールか */
    protected boolean mIsVerticalScroll;

    /** 見開き時のカレントページのデフォルトポジションを後続ページにする場合true */
    private boolean mIsSubsequentPagePositionFlag;
    /** 現在のタップ動作設定 */
    private TapAction mTapAction;
    /** 現在のページアニメーション設定 */
    private PageAnimation mPageAnimation;
    /** 自動再生間隔設定 */
    private int mAutoPlayInterval;
    /** ループ再生設定 */
    private boolean mLoopPlay;
    /** 自動再生方向設定 */
    private AutoPlayDirection mAutoPlayDirection;

    public EbookMode(boolean isLandscape, boolean isSpread) {
        mIsLandscape = isLandscape;
        mIsSpread = isSpread;
        resetAllMode();

        mIsSubsequentPagePositionFlag = false;
        mTapAction = DEFAULT_TAP_ACTION;
        mPageAnimation = DEFAULT_PAGE_ANIMATION;
        mAutoPlayInterval = DEFAULT_AUTO_PLAY_INTERVAL;
        mLoopPlay = DEFAULT_LOOP_PLAY;
        mAutoPlayDirection = DEFAULT_AUTO_PLAY_DIRECTION;
    }

    /**
     * セットした端末の向きと見開き設定を元に、Viewの表示モードを取得する
     * 
     * @return Viewの表示モード
     */
    public Mode getMode() {
        return mMode;
    }

    private void setFitMode() {
        switch (mMode) {
            case PORTRAIT_STANDARD:
                mFitMode = FitMode.ASPECT_FIT;
                break;
            case PORTRAIT_SPREAD:
                mFitMode = FitMode.HEIGHT_FIT;
                break;
            case LANDSCAPE_STANDARD:
                mFitMode = FitMode.WIDTH_FIT;
                break;
            case LANDSCAPE_SPREAD:
                mFitMode = FitMode.ASPECT_FIT;
                break;
        }
    }

    /**
     * セットした端末の向きと見開き設定を元に、画像の表示モードを取得する
     * 
     * @return 画像の表示モード
     */
    public FitMode getFitMode() {
        return mFitMode;
    }

    public void setIsLandscape(boolean isLandscape) {
        mIsLandscape = isLandscape;
        resetAllMode();
    }

    /**
     * 設定した端末の向きを取得
     * 
     * @return 横向きに設定されている場合true、縦向きに設定されている場合false
     */
    public boolean isLandscape() {
        return mIsLandscape;
    }

    public void setIsSpread(boolean isSpread) {
        mIsSpread = isSpread;
        resetAllMode();
    }

    /**
     * 設定した見開き状態を取得
     * 
     * @return 見開きならtrue、単ページならfalse
     */
    public boolean isSpread() {
        return mIsSpread;
    }

    /**
     * セットしたコンテンツの種類、端末の向きと見開き設定を元に、ビューのスクロール方向を取得する
     * 
     * @return 縦スクロールの場合true、横スクロールの場合false
     */
    public boolean isVerticalScroll() {
        return mIsVerticalScroll;
    }

    /**
     * 見開き時のカレントページのデフォルトポジションを後続ページにするかどうか
     * 
     * @return デフォルトポジションを後続ページにする場合はtrue、それ以外ならfalse
     */
    public boolean isSubsequentPagePositionFlag() {
        return mIsSubsequentPagePositionFlag;
    }

    public void setIsSubsequentPagePositionFlag(boolean isSubsequentPagePositionFlag) {
        mIsSubsequentPagePositionFlag = isSubsequentPagePositionFlag;
    }

    /**
     * 現在のページアニメーション設定を取得
     * 
     * @return ページアニメーション設定
     */
    public PageAnimation getPageAnimation() {
        return mPageAnimation;
    }

    public void setPageAnimation(PageAnimation pageAnimation) {
        this.mPageAnimation = pageAnimation;
    }

    /**
     * 現在のタップ動作設定を取得
     * 
     * @return タップ動作設定
     */
    public TapAction getTapAction() {
        return mTapAction;
    }

    public void setTapAction(TapAction tapAction) {
        this.mTapAction = tapAction;
    }

    /**
     * 現在の自動再生間隔設定を取得
     * 
     * @return 現在の自動再生間隔設定
     */
    public int getAutoPlayInterval() {
        return mAutoPlayInterval;
    }

    public void setAutoPlayInterval(int autoPlayInterval) {
        this.mAutoPlayInterval = autoPlayInterval;
    }

    /**
     * 現在のループ再生設定を取得
     * 
     * @return 現在のループ再生設定
     */
    public boolean isLoopPlay() {
        return mLoopPlay;
    }

    public void setLoopPlay(boolean loopPlay) {
        this.mLoopPlay = loopPlay;
    }

    /**
     * 現在の自動再生方向設定を取得
     * 
     * @return 現在の自動再生方向設定
     */
    public AutoPlayDirection getAutoPlayDirection() {
        return mAutoPlayDirection;
    }

    public void setAutoPlayDirection(AutoPlayDirection autoPlayDirection) {
        this.mAutoPlayDirection = autoPlayDirection;
    }

    /**
     * 設定されている画面の表示モードが見開き表示か判定
     * 
     * @return 見開きページリストを使う場合はtrue、単ページリストを使う場合はfalse
     */
    public boolean isSpreadView() {
        switch (mMode) {
            case PORTRAIT_SPREAD:
            case LANDSCAPE_SPREAD:
                return true;
            case PORTRAIT_STANDARD:
            case LANDSCAPE_STANDARD:
            default:
                return false;
        }
    }

    private void resetAllMode() {
        // 各種設定を再設定、Modeを一番最初に設定する必要あり
        setMode();
        setFitMode();
        setIsVertical();
    }

    /** 現在の各種設定に従い、コンテンツのビューア動作モードごとの画面の表示モード設定を行う */
    protected abstract void setMode();
    /** 現在の各種設定に従い、コンテンツのビューア動作モードごとの縦スクロール設定を行う */
    protected abstract void setIsVertical();
    /** コンテンツのビューア動作モードを取得する */
    public abstract ContentMode getContentMode();
}
