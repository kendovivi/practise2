
package jp.bpsinc.android.chogazo.viewer.menu;

import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.activity.ViewerActivity;
import jp.bpsinc.android.chogazo.viewer.content.Book;
import jp.bpsinc.android.chogazo.viewer.view.EbookView;
import jp.bpsinc.android.chogazo.viewer.R;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class ViewerMenu implements OnTouchListener, OnClickListener {
    private final ViewerActivity mViewerActivity;
    private final EbookView mEbookView;
    private final Paint mPaint;
    private final ViewGroup mViewerLayout;
    private final View mMenu;
    private final SeekBar mFooterMenuPageSeekBar;
    private final TextView mFooterMenuPageSeekText;
    private final View mFooterMenuAutoPlay;
    private final View mFooterMenuBrightness;
    private final View mFooterMenuNav;
    private final View mFooterMenuBookmarkButton;
    private final View mFooterMenuSettingButton;
    private MenuItem mMenuItemAddBookmark;

    private String mPageCountStr;

    public ViewerMenu(ViewerActivity viewerActivity,
            EbookView ebookView, Book book, Menu actionBarMenu) {
        mViewerActivity = viewerActivity;
        mEbookView = ebookView;
        mPaint = new Paint();

        // アクションバーに書誌情報設定
        mViewerActivity.getSupportActionBar().setTitle(book.getTitle());
        mViewerActivity.getSupportActionBar().setSubtitle(book.getAuthor());

        // アクションバーのメニューオブジェクトを取得
        mMenuItemAddBookmark = actionBarMenu.findItem(R.id.cho_gazo_viewer_menu_add_bookmark);

        // 大本のレイアウト取得
        mViewerLayout = ((ViewGroup) mViewerActivity.findViewById(R.id.viewer_layout));
        // オプションメニュー用レイアウトを取得し、大本レイアウトに追加
        mMenu = mViewerActivity.getLayoutInflater().inflate(R.layout.cho_gazo_viewer_menu, null);
        mMenu.setOnTouchListener(this);
        mMenu.setOnClickListener(this);
        mViewerLayout.addView(mMenu);

        // フッタ部分のレイアウトを取得して必要なリスナー追加
        View footerMenuLayout = mViewerActivity.findViewById(R.id.viewer_footer_menu_layout);
        mFooterMenuAutoPlay = mViewerActivity.findViewById(R.id.viewer_footer_auto_play_button);
        mFooterMenuBrightness = mViewerActivity.findViewById(R.id.viewer_footer_brightness_button);
        mFooterMenuNav = mViewerActivity.findViewById(R.id.viewer_footer_nav_button);
        mFooterMenuBookmarkButton = mViewerActivity
                .findViewById(R.id.viewer_footer_bookmark_button);
        mFooterMenuSettingButton = mViewerActivity.findViewById(R.id.viewer_footer_setting_button);
        footerMenuLayout.setOnClickListener(this);
        mFooterMenuAutoPlay.setOnClickListener(this);
        mFooterMenuBrightness.setOnClickListener(this);
        mFooterMenuNav.setOnClickListener(this);
        mFooterMenuBookmarkButton.setOnClickListener(this);
        mFooterMenuSettingButton.setOnClickListener(this);

        // ページシークバー取得し、各種値設定
        mFooterMenuPageSeekBar = (SeekBar) mViewerActivity
                .findViewById(R.id.viewer_footer_seek_bar);
        mFooterMenuPageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // トラッキング開始時に呼び出されます
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                LogUtil.v("progress=%d", seekBar.getProgress());
            }

            // トラッキング中に呼び出されます
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                LogUtil.v("progress=%d, fromUser=%b", progress, fromUser);

                // 自動再生の経過秒数をリセット
                mViewerActivity.resetAutoPlayInterval();

                // オプションメニュー開いた時(setProgressした時)などに呼ばれた場合は何もしない
                if (fromUser) {
                    setSeekBarText(getSeekBarConvertProgress(progress));
                    mEbookView.jumpToPage(getSeekBarConvertProgress(progress));
                }
            }

            // トラッキング終了時に呼び出されます
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                LogUtil.v("progress=%d", seekBar.getProgress());
            }
        });
        // 本が右開きの場合と左開きの場合でページシークバーの向き(色の付き方など)を変更
        Resources res = mViewerActivity.getResources();
        if (mEbookView.getPageAccess().isRtl()) {
            mFooterMenuPageSeekBar.setProgressDrawable(
                    res.getDrawable(R.drawable.cho_gazo_seek_bar_rtl));
            mFooterMenuPageSeekBar.setIndeterminateDrawable(
                    res.getDrawable(R.drawable.cho_gazo_seek_bar_rtl));
        } else {
            mFooterMenuPageSeekBar.setProgressDrawable(
                    res.getDrawable(R.drawable.cho_gazo_seek_bar_ltr));
            mFooterMenuPageSeekBar.setIndeterminateDrawable(
                    res.getDrawable(R.drawable.cho_gazo_seek_bar_ltr));
        }
        mFooterMenuPageSeekText = (TextView) mViewerActivity
                .findViewById(R.id.viewer_footer_seek_text);

        // 目次がない場合、目次ボタンを無効化する
        if (!mViewerActivity.isNavExist()) {
            mFooterMenuNav.setEnabled(false);
        }

        // メニュー消去
        mMenu.setVisibility(View.GONE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int id = v.getId();
        if (id == R.id.viewer_footer_menu_layout) {
            // ヘッダやフッタ部分のタッチ無効化(EbookViewのonTouchEventが動かないようにする)
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.viewer_footer_auto_play_button) {
            // 自動再生ボタンを押したら自動再生のON/OFFを切り替える
            mViewerActivity.changeAutoPlay();
        } else if (id == R.id.viewer_footer_brightness_button) {
            // 画面の明るさ設定ボタンを押したら明るさ設定ダイアログを表示
            mViewerActivity.showBrightnessDialog();
        } else if (id == R.id.viewer_footer_nav_button) {
            // 目次ボタンを押したら目次画面表示
            mViewerActivity.showNavActivity();
        } else if (id == R.id.viewer_footer_bookmark_button) {
            // しおりボタン押したらしおり画面表示
            mViewerActivity.showBookmarkActivity();
        } else if (id == R.id.viewer_footer_setting_button) {
            // 設定ボタンを押したら設定画面表示
            mViewerActivity.showSettingActivity();
        } else if (id == R.id.viewer_menu_layout) {
            // ヘッダとフッタ以外の箇所だったらメニューを閉じる
            closeOptionMenu();
        }
    }

    public boolean isShowOptionMenu() {
        return mMenu.getVisibility() == View.VISIBLE;
    }

    public void showOptionMenu() {
        mMenu.setVisibility(View.VISIBLE);
        mViewerActivity.getSupportActionBar().show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mViewerActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        seekBarInit();
    }

    public void closeOptionMenu() {
        mMenu.setVisibility(View.GONE);
        mViewerActivity.getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mViewerActivity.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public void changeAutoPlayIcon(boolean isAutoPlay) {
        if (isAutoPlay) {
            mFooterMenuAutoPlay.setBackgroundResource(
                    R.drawable.cho_gazo_viewer_btn_footer_auto_pause);
            // 自動再生以外のボタンを無効化する
            mFooterMenuBrightness.setEnabled(false);
            mFooterMenuNav.setEnabled(false);
            mFooterMenuBookmarkButton.setEnabled(false);
            mFooterMenuSettingButton.setEnabled(false);
            mMenuItemAddBookmark.setEnabled(false);
        } else {
            mFooterMenuAutoPlay.setBackgroundResource(
                    R.drawable.cho_gazo_viewer_btn_footer_auto_play);
            // 自動再生以外のボタンを有効化する
            mFooterMenuBrightness.setEnabled(true);
            if (mViewerActivity.isNavExist()) {
                // 目次ボタンは目次がある場合だけ有効化
                mFooterMenuNav.setEnabled(true);
            }
            mFooterMenuBookmarkButton.setEnabled(true);
            mFooterMenuSettingButton.setEnabled(true);
            mMenuItemAddBookmark.setEnabled(true);
        }
    }

    /**
     * ページシークバー関連の各値を初期化
     */
    public void seekBarInit() {
        LinearLayout.LayoutParams linearLayoutParams;
        String pageCountStr = String.valueOf(mEbookView.getPageCount());
        mPageCountStr = " / " + pageCountStr;

        mPaint.setTextSize(mFooterMenuPageSeekText.getTextSize());
        linearLayoutParams = (LinearLayout.LayoutParams) mFooterMenuPageSeekText.getLayoutParams();
        linearLayoutParams.width = (int) mPaint.measureText(pageCountStr + mPageCountStr) + 1;
        mFooterMenuPageSeekText.setLayoutParams(linearLayoutParams);

        // ページシークバーの各種値を設定
        mFooterMenuPageSeekBar.setMax(mEbookView.getPageCount() - 1);
        // ページシークバー表示したまま端末回転した直後にオプションメニュー表示するとバーの表示がおかしいことがあるので一旦0にする
        // 原因としてはsetProgressの値が現在と同じ位置の場合に何も変化しない(onProgressChangedも呼ばれない)からだと思われる
        // (シークバー再取得しても端末回転前の状態が残ってて、シークバーの横幅が縦横で異なるからずれる？)
        mFooterMenuPageSeekBar.setProgress(0);
        mFooterMenuPageSeekBar.setProgress(
                getSeekBarConvertProgress(mEbookView.getCurrentPageIndex()));
        setSeekBarText(mEbookView.getCurrentPageIndex());
    }

    /**
     * 渡されたプログレスに1を足し(0始まりのため)、後ろに全体のページ数表示を付与してページシークバー横のテキストを更新する
     * 
     * @param progress ページシークバーのプログレス値、右開きの本の時は反転させる必要がある
     */
    private void setSeekBarText(int progress) {
        mFooterMenuPageSeekText.setText((progress + 1) + mPageCountStr);
    }

    /**
     * ページシークバーのプログレス値をLTRとRTLの場合で反転させる
     * 
     * @param progress 元のプログレス値
     * @return LTRの場合はprogress、RTLの場合は全ページ数 - (progress + 1)
     */
    private int getSeekBarConvertProgress(int progress) {
        int seekBarConvertIndex = progress;
        if (mEbookView.getPageAccess().isRtl()) {
            seekBarConvertIndex = mEbookView.getPageCount() - (seekBarConvertIndex + 1);
        }
        return seekBarConvertIndex;
    }
}
