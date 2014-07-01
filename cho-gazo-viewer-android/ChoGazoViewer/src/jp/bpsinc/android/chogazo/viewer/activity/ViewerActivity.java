
package jp.bpsinc.android.chogazo.viewer.activity;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsParseException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsUnzipException;
import jp.bpsinc.android.chogazo.viewer.exception.DrmException;
import jp.bpsinc.android.chogazo.viewer.exception.UnexpectedException;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.util.OrientationUtil;
import jp.bpsinc.android.chogazo.viewer.util.StringUtil;
import jp.bpsinc.android.chogazo.viewer.util.WindowUtil;
import jp.bpsinc.android.chogazo.viewer.adapter.BookmarkInfo;
import jp.bpsinc.android.chogazo.viewer.content.Book;
import jp.bpsinc.android.chogazo.viewer.content.ContentsReader;
import jp.bpsinc.android.chogazo.viewer.content.NavListItem;
import jp.bpsinc.android.chogazo.viewer.content.PageAccess;
import jp.bpsinc.android.chogazo.viewer.content.PageInfo;
import jp.bpsinc.android.chogazo.viewer.content.ViewerContents;
import jp.bpsinc.android.chogazo.viewer.db.AbstractRow;
import jp.bpsinc.android.chogazo.viewer.db.AutoBookmarkTable;
import jp.bpsinc.android.chogazo.viewer.db.BookmarkTable;
import jp.bpsinc.android.chogazo.viewer.dialog.BrightnessDialog;
import jp.bpsinc.android.chogazo.viewer.dialog.SimpleYesNoDialog;
import jp.bpsinc.android.chogazo.viewer.dialog.ViewerErrorDialog;
import jp.bpsinc.android.chogazo.viewer.dialog.SimpleYesNoDialog.YesNoListener;
import jp.bpsinc.android.chogazo.viewer.menu.ViewerMenu;
import jp.bpsinc.android.chogazo.viewer.view.EbookView;
import jp.bpsinc.android.chogazo.viewer.view.EbookMode;
import jp.bpsinc.android.chogazo.viewer.view.EbookMode.AutoPlayDirection;
import jp.bpsinc.android.chogazo.viewer.view.EbookMode.PageAnimation;
import jp.bpsinc.android.chogazo.viewer.view.EbookMode.TapAction;
import jp.bpsinc.android.chogazo.viewer.R;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public abstract class ViewerActivity extends ActionBarActivity implements YesNoListener {
    /** ハードウェアアクセラレーション有効化用フラグ、APIレベル11未満用に定義 */
    private static final int FLAG_HARDWARE_ACCELERATED = 0x01000000;
    /** 入力値：本棚から受け渡されるコンテンツの情報 */
    public static final String INTENT_KEY_CONTENTS = "INTENT_KEY_CONTENTS";
    /** リクエストコード：設定画面 */
    public static final int REQUEST_CODE_SETTING = 1;
    /** リクエストコード：ブックマーク画面 */
    public static final int REQUEST_CODE_BOOKMARK = 2;
    /** リクエストコード：目次画面 */
    public static final int REQUEST_CODE_NAV = 3;
    /** リクエストID:しおりを付けるボタン */
    private static final int REQUEST_ID_ADD_BOOKMARK_BUTTON = 1;
    /** ダイアログフラグメントの引数として渡すバンドルキー */
    public static final String BUNDLE_KEY_INT_1 = "INT_1";

    /** 本棚から受け渡されるデータ */
    protected ViewerContents mViewerContents;
    /** 解析したコンテンツの各種情報 */
    protected Book mBook;
    /** ビューアの各種設定の取得などに使用 */
    protected EbookMode mEbookMode;
    /** コンテンツのページへアクセスを行うオブジェクト */
    protected PageAccess mPageAccess;
    /** 画像を描画してスクロールなどするためのビュー */
    protected EbookView mEbookView;
    /** 自動しおり機能 */
    protected AutoBookmarkTable mAutoBookmarkTable;
    /** 非UIスレッドからの処理受け付け用ハンドラ */
    private Handler mHandler;
    /** 自動再生タイマー */
    private CountDownTimer mAutoPlayTimer;
    /** 自動再生のランダム設定用 */
    private Random mAutoRandomPlay;

    /** オプションメニュー表示用のクラス */
    protected ViewerMenu mViewerMenu;
    /** アクションバーに表示するメニューオブジェクト */
    private Menu mActionBarMenu;
    /** 前回の端末の向きを保存 */
    private int mSaveOrientation;
    /** コンテンツ読み込み済み判定フラグ */
    private boolean mContentsLoaded;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.v();
        super.onCreate(savedInstanceState);

        // アクションバーオーバーレイ設定、アクションバーとステータスバー非表示、ハードウェアアクセラレーション有効化
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.cho_gazo_viewer_btn_header_back);
        actionBar.setHomeButtonEnabled(true);
        actionBar.hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(FLAG_HARDWARE_ACCELERATED, FLAG_HARDWARE_ACCELERATED);

        setContentView(R.layout.cho_gazo_viewer);

        mHandler = new Handler();
        mAutoRandomPlay = new Random();
        mSaveOrientation = getResources().getConfiguration().orientation;

        // 画面の明るさ設定を反映
        WindowUtil.setViewerBtightness(this);

        try {
            // 本棚からの情報を取得
            mViewerContents = (ViewerContents) getIntent()
                    .getSerializableExtra(INTENT_KEY_CONTENTS);
            bookshelfArgumentCheck(mViewerContents);

            if (mViewerContents.getContentsKey() == null) {
                // コンテンツキーが指定されていなかったら代わりにパスを使用する
                mViewerContents.setContentsKey(mViewerContents.getPath());
            }
            // 自動しおり初期化
            mAutoBookmarkTable = new AutoBookmarkTable(
                    getApplicationContext(), mViewerContents.getContentsKey());
        } catch (RuntimeException e) {
            LogUtil.e("runtime error", e);
            ViewerErrorDialog.show(getSupportFragmentManager(),
                    ViewerErrorDialog.ID_UNEXPECTED_ERR);
        }
    }

    @Override
    protected void onStart() {
        LogUtil.v();
        super.onStart();
    }

    @Override
    protected void onResume() {
        LogUtil.v();
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogUtil.v();
        if (mEbookView != null) {
            mAutoBookmarkTable.setAutoBookmark(mEbookView.getCurrentSinglePageIndex());
        }
        // 自動再生停止
        stopAutoPlay();
        super.onPause();
    }

    @Override
    protected void onStop() {
        LogUtil.v();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        LogUtil.v();

        // onDestroyの一番始めにスレッド停止する、ここでスレッド消えるまで待機が発生
        if (mEbookView != null) {
            mEbookView.stopAllPageLoadAndReset();
        }
        if (mViewerMenu != null) {
            mViewerMenu.closeOptionMenu();
        }
        if (mPageAccess != null) {
            mPageAccess.close();
        }

        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        LogUtil.v();
        super.onWindowFocusChanged(hasFocus);

        // 初回のみ、コンテンツ読み込み処理を行う
        // onCreateOptionsMenu実行後に行いたいため、onCreateではなくここでやる
        if (!mContentsLoaded) {
            mContentsLoaded = true;
            try {
                loadContents();
            } catch (RuntimeException e) {
                LogUtil.e("runtime error", e);
                ViewerErrorDialog.show(getSupportFragmentManager(),
                        ViewerErrorDialog.ID_UNEXPECTED_ERR);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LogUtil.v("oldOrientation=%d, newOrientation=%d",  mSaveOrientation, newConfig.orientation);
        super.onConfigurationChanged(newConfig);

        // 全オブジェクト生成前にエラーダイアログ出た場合などを考慮し、onCreateの最後に生成するオブジェクトのnull判定も行う
        if (mViewerMenu != null && mSaveOrientation != newConfig.orientation) {
            mSaveOrientation = newConfig.orientation;

            // 自動再生の経過秒数をリセット
            resetAutoPlayInterval();

            // スクロールも停止(これを行わないと、computeScrollが動き続けておかしなことになる)
            mEbookView.scrollerAbortAnimation();
            // ビューアごとに可変な処理を行う
            orientationChange();
            // 端末の向き設定を変更してページリストを再設定
            mEbookMode.setIsLandscape(!mEbookMode.isLandscape());
            mEbookView.setPageList();

            // 端末回転したらメニュー表示初期化(見開き切り替えでページ数変わることに対する対処)
            mViewerMenu.seekBarInit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.v();

        switch (requestCode) {
            case REQUEST_CODE_SETTING:
                // 設定画面から戻ってきた時
                if (resultCode == RESULT_OK) {
                    applySetting();
                }
                mViewerMenu.closeOptionMenu();
                break;
            case REQUEST_CODE_BOOKMARK:
                // しおり画面から戻ってきた時
                if (resultCode == RESULT_OK) {
                    // 縦見開き時にメモリを保持したままのページヘ飛んだ場合に後方ページヘ寄せる処理などがうまく働かないことがあるため、ページを一旦破棄する
                    mEbookView.stopAllPageLoadAndReset();

                    // しおり画面で端末回転してる場合、onLayout前にonActivityResultが動くので(色んな端末で試したわけではない)
                    // jumpToPageでカレントページだけ更新される、その後onLayoutが動き、通常通りの流れで画像読み込みが行われる
                    // 試した限りでは無かったが、onLayoutの後にonActivityResultが動いた場合、ページシークバーで移動した時と同じ動作となるのでこちらも問題なし
                    int bookmarkPage = data.getIntExtra(
                            BookmarkActivity.INTENT_DATA_KEY_BOOKMARK_PAGE, 0);
                    mEbookView.jumpToPage(pageCountCorrection(bookmarkPage));
                }
                // しおり画面から戻ったらオプションメニュー閉じる(ここの仕様変える場合、変わりにページシークバー更新しないといけない)
                mViewerMenu.closeOptionMenu();
                break;
            case REQUEST_CODE_NAV:
                // 目次画面から戻ってきた時
                if (resultCode == RESULT_OK) {
                    // 基本的にしおりと同じ処理でページ移動、INTENTからのページ数取得でキーが異なる部分のみ違う
                    mEbookView.stopAllPageLoadAndReset();
                    int navPage = data.getIntExtra(NavActivity.INTENT_DATA_KEY_NAV_PAGE, 0);
                    mEbookView.jumpToPage(pageCountCorrection(navPage));
                }
                // 目次画面から戻ったらオプションメニュー閉じる(ここの仕様変える場合、変わりにページシークバー更新しないといけない)
                mViewerMenu.closeOptionMenu();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        LogUtil.v();
        getMenuInflater().inflate(R.menu.cho_gazo_viewer_action_bar_menu, menu);

        // メニューオブジェクト保存、この処理が走るまでmActionBarMenuはnullなので注意
        mActionBarMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        LogUtil.v();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtil.v();
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.cho_gazo_viewer_menu_add_bookmark) {
            // しおりを付けるボタンを押したらダイアログを表示
            showAddBookmarkDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        LogUtil.v();
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtil.v();
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            openMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void openMenu() {
        if (mViewerMenu != null) {
            if (mViewerMenu.isShowOptionMenu()) {
                mViewerMenu.closeOptionMenu();
            } else {
                mViewerMenu.showOptionMenu();
            }
        }
    }

    @Override
    public void onBackPressed() {
        LogUtil.v();
        if (mViewerMenu != null && mViewerMenu.isShowOptionMenu()) {
            mViewerMenu.closeOptionMenu();
        } else {
            super.onBackPressed();
        }
    }

    private void loadContents() {
        // コンテンツ読み込みはDRM解除などで通信が入る可能性があるため、UIスレッドでは動かさない
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // コンテンツを開く
                    ContentsReader reader = (ContentsReader) newInstanceFromString(
                            mViewerContents.getReaderClassName());
                    reader.open(ViewerActivity.this,
                            mViewerContents.getPath(), mViewerContents.getReaderOption());

                    // コンテンツ解析
                    mBook = (Book) newInstanceFromString(mViewerContents.getBookClassName());
                    mBook.parse(reader);

                    // 本棚から渡された書誌情報があればbookにコピー
                    bibliographicCopyFromBookshelf(mBook, mViewerContents);

                    mPageAccess = new PageAccess(reader, mBook) {
                        @SuppressWarnings("unchecked")
                        @Override
                        public ArrayList<PageInfo> getSpreadPageInfoList() {
                            ArrayList<PageInfo> list = mSpreadPageInfo;
                            if (isRtl() == false) {
                                // LTRならページリストをシャローコピーして逆順にしたオブジェクトを返す
                                list = (ArrayList<PageInfo>) list.clone();
                                Collections.reverse(list);
                            }
                            return list;
                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        public ArrayList<PageInfo> getSinglePageInfoList() {
                            ArrayList<PageInfo> list = mSinglePageInfo;
                            if (isReversePage()) {
                                // LTRかつ縦向きならページリストをシャローコピーして逆順にしたオブジェクトを返す
                                list = (ArrayList<PageInfo>) list.clone();
                                Collections.reverse(list);
                            }
                            return list;
                        }
                    };
                    startShow();
                } catch (ClassCastException e) {
                    LogUtil.e("illegal argument error", e);
                    ViewerErrorDialog.show(getSupportFragmentManager(),
                            ViewerErrorDialog.ID_ILLEGAL_ARG_ERR);
                } catch (IllegalArgumentException e) {
                    LogUtil.e("illegal argument error", e);
                    ViewerErrorDialog.show(getSupportFragmentManager(),
                            ViewerErrorDialog.ID_ILLEGAL_ARG_ERR);
                } catch (FileNotFoundException e) {
                    LogUtil.e("specified path not found error", e);
                    ViewerErrorDialog.show(getSupportFragmentManager(),
                            ViewerErrorDialog.ID_PATH_NOTFOUND_ERR);
                } catch (DrmException e) {
                    LogUtil.e("drm release error", e);
                    ViewerErrorDialog.show(getSupportFragmentManager(),
                            ViewerErrorDialog.ID_DRM_RELEASE_ERR);
                } catch (ContentsUnzipException e) {
                    LogUtil.e("contents unzip error", e);
                    ViewerErrorDialog.show(getSupportFragmentManager(),
                            ViewerErrorDialog.ID_CONTENTS_UNZIP_ERR);
                } catch (ContentsParseException e) {
                    LogUtil.e("contents parse error", e);
                    ViewerErrorDialog.show(getSupportFragmentManager(),
                            ViewerErrorDialog.ID_CONTENTS_PARSE_ERR);
                } catch (ContentsOtherException e) {
                    LogUtil.e("contents file error", e);
                    ViewerErrorDialog.show(getSupportFragmentManager(),
                            ViewerErrorDialog.ID_CONTENTS_OTHER_ERR);
                } catch (UnexpectedException e) {
                    LogUtil.e("unexpected error", e);
                    ViewerErrorDialog.show(getSupportFragmentManager(),
                            ViewerErrorDialog.ID_UNEXPECTED_ERR);
                }
            }
        }).start();
    }

    private void startShow() throws ContentsOtherException {
        // 設定変更やレイアウト関連などはUIスレッドで処理する
        mHandler.post(new Runnable() {
            @SuppressLint("NewApi")
            @Override
            public void run() {
                // 設定適用、基本的にウィンドウやmEbookModeに設定するのでここでやる、
                // EbookView作成後だと見開き状態などが変わり読み込みが2度走る可能性がある、この処理を移動する場合色々考慮すること
                mEbookMode = createEbookMode();
                applySetting();

                mEbookView = new EbookView(ViewerActivity.this,
                        mPageAccess, mEbookMode, getAutoBookmarkPageIndex());
                // Android 4.1 以降の場合はステータスバーをオーバーレイ表示するようにする
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mEbookView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                }

                // コンテンツ読み込みが終わったらプログレスを消す
                findViewById(R.id.cho_gazo_viewer_layout_progress).setVisibility(View.GONE);

                // ビューアのメインレイアウトに書籍描画用のビューを設定
                ((ViewGroup) findViewById(R.id.viewer_layout)).addView(mEbookView);
                // メニューレイアウトを追加
                mViewerMenu = new ViewerMenu(ViewerActivity.this,
                        mEbookView, mBook, mActionBarMenu);
            }
        });
    }

    public ViewerMenu getViewerMenu() {
        return mViewerMenu;
    }

    private void bookshelfArgumentCheck(ViewerContents viewerContents) {
        if (viewerContents == null) {
            throw new IllegalArgumentException("ViewerContents is null");
        }
        String filePath = viewerContents.getPath();
        LogUtil.v("read file = %s", filePath);
        if (filePath == null) {
            throw new IllegalArgumentException("contents file path is null");
        }
        String readerClassName = viewerContents.getReaderClassName();
        LogUtil.v("readerClassName = %s", readerClassName);
        if (readerClassName == null) {
            throw new IllegalArgumentException("readerClassName is null");
        }
        String bookClassName = viewerContents.getBookClassName();
        LogUtil.v("bookClassName = %s", bookClassName);
        if (bookClassName == null) {
            throw new IllegalArgumentException("bookClassName is null");
        }
    }

    private Object newInstanceFromString(String className) {
        Object object;
        try {
            Class<?> clazz = Class.forName(className);
            object = clazz.newInstance();
        } catch (ClassNotFoundException e) {
            LogUtil.e(e);
            throw new IllegalArgumentException("");
        } catch (InstantiationException e) {
            LogUtil.e(e);
            throw new IllegalArgumentException("");
        } catch (IllegalAccessException e) {
            LogUtil.e(e);
            throw new IllegalArgumentException("");
        }
        return object;
    }

    private void bibliographicCopyFromBookshelf(Book book, ViewerContents viewerContents) {
        if (viewerContents.getTitle() != null) {
            book.setTitle(viewerContents.getTitle());
        }
        if (viewerContents.getTitleKana() != null) {
            book.setTitleKana(viewerContents.getTitleKana());
        }
        if (viewerContents.getAuthor() != null) {
            book.setAuthor(viewerContents.getAuthor());
        }
        if (viewerContents.getAuthorKana() != null) {
            book.setAuthorKana(viewerContents.getAuthorKana());
        }
    }

    /**
     * ページ数を逆転する必要があるか判定する
     * 
     * @return LTR、かつ、横スクロールならtrue、それ以外ならfalse
     */
    public boolean isReversePage() {
        return mPageAccess.isRtl() == false && mEbookMode.isVerticalScroll() == false;
    }

    public void postShowDialog(final int id) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ViewerErrorDialog.show(getSupportFragmentManager(), id);
            }
        });
    }

    /**
     * 自動しおりが存在する場合、その値を習得して現在のビュー設定に合わせた値に補正したページインデックスを返す
     * 
     * @return 自動しおりを見開き設定に合わせた値に補正したページインデックス、自動しおりが存在しない場合は0
     */
    private int getAutoBookmarkPageIndex() {
        int currentPageIndex = 0;
        if (mAutoBookmarkTable != null) {
            AutoBookmarkTable.Row row = mAutoBookmarkTable.getAutoBookmark();
            if (row != null) {
                currentPageIndex = pageCountCorrection(row.getLastPage());
            }
        }
        return currentPageIndex;
    }

    /**
     * 画面の明るさ設定ダイアログを表示する
     */
    public void showBrightnessDialog() {
        BrightnessDialog.show(getSupportFragmentManager());
    }

    /**
     * しおりを付けるダイアログを表示する
     */
    public void showAddBookmarkDialog() {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_KEY_INT_1, mEbookView.getCurrentSinglePageIndex());
        SimpleYesNoDialog.show(REQUEST_ID_ADD_BOOKMARK_BUTTON, getSupportFragmentManager(),
                null,
                getString(R.string.viewer_dlg_add_bookmark_text),
                getString(R.string.viewer_dlg_btn_positive),
                getString(R.string.viewer_dlg_btn_negative),
                args, this);
    }

    /**
     * しおり画面を表示
     */
    public void showBookmarkActivity() {
        Intent intent = new Intent(this, BookmarkActivity.class);
        BookmarkInfo bookmarkInfo = new BookmarkInfo(mViewerContents.getContentsKey(),
                mEbookView.getCurrentSinglePageIndex());
        intent.putExtra(BookmarkActivity.INTENT_KEY_BOOKMARK_INFO, bookmarkInfo);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, REQUEST_CODE_BOOKMARK);
    }

    /**
     * 目次画面を表示
     */
    public void showNavActivity() {
        List<NavListItem> list = mBook.getNavList();
        int size = list.size();
        String[] titleList = new String[size];
        int[] pageIndexList = new int[size];

        for (int i = 0; i < size; i++) {
            NavListItem item = list.get(i);
            titleList[i] = item.getTitle();
            pageIndexList[i] = item.getSinglePageIndex();
        }

        Intent intent = new Intent(this, NavActivity.class);
        intent.putExtra(NavActivity.INTENT_KEY_NAV_TITLE, titleList);
        intent.putExtra(NavActivity.INTENT_KEY_NAV_PAGE, pageIndexList);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, REQUEST_CODE_NAV);
    }

    /**
     * 目次情報が1件以上あるか判定する
     * 
     * @return 目次情報が1件以上ある場合true、0件の場合false
     */
    public boolean isNavExist() {
        return !mBook.getNavList().isEmpty();
    }

    /**
     * 自動再生の開始と停止を切り替える
     */
    public void changeAutoPlay() {
        if (mAutoPlayTimer == null) {
            // 自動再生開始
            mViewerMenu.changeAutoPlayIcon(true);

            int interval = mEbookMode.getAutoPlayInterval() * 1000;
            mAutoPlayTimer = new CountDownTimer(interval, interval) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // 何もしない
                }
                @Override
                public void onFinish() {
                    switch (mEbookMode.getAutoPlayDirection()) {
                        case FORWARD:
                            if (mEbookMode.isLoopPlay() && mEbookView.isLastPage()
                                    && !mEbookView.isScrollableForward()) {
                                // ループ再生ONの時に最終ページにいたら先頭ページへ遷移
                                mEbookView.jumpToPage(0);
                            } else {
                                mEbookView.pageScrollOrNextPage();
                            }
                            break;
                        case REVERSE:
                            if (mEbookMode.isLoopPlay() && mEbookView.isFirstPage()
                                    && !mEbookView.isScrollableBackward()) {
                                // ループ再生ONの時に先頭ページにいたら最終ページへ遷移
                                mEbookView.jumpToPage(mEbookView.getPageCount() - 1);
                            } else {
                                mEbookView.pageScrollOrPrevPage();
                            }
                            break;
                        case RANDOM:
                            int randomJumpPage;
                            do {
                                // ランダムページ数が現在のページと同じだったら再取得
                                randomJumpPage = mAutoRandomPlay
                                        .nextInt(mEbookView.getPageCount() - 1);
                            } while (randomJumpPage == mEbookView.getCurrentPageIndex());
                            mEbookView.jumpToPage(randomJumpPage);
                            break;
                    }
                    mViewerMenu.seekBarInit();
                    mAutoPlayTimer.start();
                }
            };
            mAutoPlayTimer.start();
        } else {
            // 自動再生停止
            stopAutoPlay();
        }
    }

    /**
     * 自動再生停止、自動再生中でなければ何もしない
     */
    private void stopAutoPlay() {
        if (mAutoPlayTimer != null) {
            mViewerMenu.changeAutoPlayIcon(false);
            mAutoPlayTimer.cancel();
            mAutoPlayTimer = null;
        }
    }

    /**
     * 自動再生の経過秒数をリセットする、自動再生中でなければ何もしない
     */
    public void resetAutoPlayInterval() {
        if (mAutoPlayTimer != null) {
            mAutoPlayTimer.cancel();
            mAutoPlayTimer.start();
        }
    }

    /**
     * 単ページ表示のページインデックスを渡し、現在の見開き設定に合ったページインデックスに補正する<br>
     * 見開き設定の場合、singlePageIndexが見開き時の後続ページだったらEbookModeの後続ページポジションフラグをtrueにする<br>
     * 値が範囲外の場合は一番近い正常値に補正する
     * 
     * @param singlePageIndex 単ページ表示のページインデックス
     * @return 見開き設定に合ったページインデックス、範囲外の場合は一番近い正常値
     */
    private int pageCountCorrection(int singlePageIndex) {
        int currentPageIndex = singlePageIndex;
        if (mEbookMode.isSpreadView()) {
            // 保存されているページ数は単ページのもののため、見開き設定なら変換かける
            currentPageIndex = mPageAccess.getSpreadIndexFromSingleIndex(currentPageIndex);
            spreadSubsequentPageJudgment(singlePageIndex, currentPageIndex);
        } else {
            // 自動しおりに保存されてた値が範囲外だったら補正する
            if (currentPageIndex < 0) {
                currentPageIndex = 0;
            } else if (currentPageIndex >= mPageAccess.getSinglePageInfoList().size()) {
                currentPageIndex = mPageAccess.getSinglePageInfoList().size() - 1;
            }
        }
        return currentPageIndex;
    }

    /**
     * 単ページインデックスが見開きページの後続ページの場合、EbookModeの後続ページポジションフラグをtrueにする
     * 
     * @param singlePageIndex 単ページインデックス
     * @param spreadPageIndex 見開きページインデックス
     */
    public void spreadSubsequentPageJudgment(int singlePageIndex, int spreadPageIndex) {
        if (mPageAccess.isRtl() == false) {
            // LTR時はgetSpreadPageListの中身が反転してるのでインデックスを補正
            spreadPageIndex = mPageAccess.getSpreadPageInfoList().size() - spreadPageIndex - 1;
        }
        if (singlePageIndex > mPageAccess.getSpreadPageInfoList().get(spreadPageIndex)
                .getThisPage()) {
            mEbookMode.setIsSubsequentPagePositionFlag(true);
        }
    }

    @Override
    public void onDialogPositiveClick(int id, Bundle args) {
        switch (id) {
            case REQUEST_ID_ADD_BOOKMARK_BUTTON:
                // しおりを付ける
                BookmarkTable bookmarkTable = new BookmarkTable(getApplicationContext());
                int bookmarkPage = args.getInt(BUNDLE_KEY_INT_1);
                List<AbstractRow> bookmarkList = bookmarkTable.getBookmark(
                        mViewerContents.getContentsKey(), bookmarkPage);
                if (bookmarkList.size() == 0) {
                    String label = StringUtil.getCurrentTime();
                    bookmarkTable.insertBookmark(mViewerContents.getContentsKey(),
                            bookmarkPage, label, label);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onDialogNegativeClick(int id, Bundle args) {
        switch (id) {
            case REQUEST_ID_ADD_BOOKMARK_BUTTON:
                // 何もしない
                break;
            default:
                break;
        }
    }

    protected void applySettingOrientation(SharedPreferences pref) {
        OrientationUtil.setOrientation(this, pref.getString(
                getString(R.string.pre_key_viewer_orientation),
                getString(R.string.pre_default_viewer_orientation)));
    }

    protected void applySettingSpread(SharedPreferences pref) {
        mEbookMode.setIsSpread(pref.getBoolean(
                getString(R.string.pre_key_viewer_spread),
                getResources().getBoolean(R.bool.pre_default_viewer_spread)));
        if (mEbookView != null) {
            // 見開き設定切り替わってるかもしれないのでページリストを設定し直す
            mEbookView.setPageList();
        }
    }

    protected void applySettingTapAction(SharedPreferences pref) {
        String tapAction = pref.getString(
                getString(R.string.pre_key_viewer_tap_action),
                getString(R.string.pre_default_viewer_tap_action));
        mEbookMode.setTapAction(TapAction.menuValueOf(tapAction));
    }

    protected void applySettingPageAnimation(SharedPreferences pref) {
        String pageAnimation = pref.getString(
                getString(R.string.pre_key_viewer_animation),
                getString(R.string.pre_default_viewer_animation));
        mEbookMode.setPageAnimation(PageAnimation.menuValueOf(pageAnimation));
    }

    protected void applySettingAutoPlayInterval(SharedPreferences pref) {
        mEbookMode.setAutoPlayInterval(pref.getInt(
                getString(R.string.pre_key_viewer_auto_play_interval),
                getResources().getInteger(R.integer.pre_default_viewer_auto_play_interval)));
    }

    protected void applySettingLoopPlay(SharedPreferences pref) {
        mEbookMode.setLoopPlay(pref.getBoolean(
                getString(R.string.pre_key_viewer_loop_play),
                getResources().getBoolean(R.bool.pre_default_viewer_loop_play)));
    }

    protected void applySettingPlayDirection(SharedPreferences pref) {
        String autoPlayDirection = pref.getString(
                getString(R.string.pre_key_viewer_auto_play_direction),
                getString(R.string.pre_key_viewer_auto_play_direction));
        mEbookMode.setAutoPlayDirection(AutoPlayDirection.menuValueOf(autoPlayDirection));
    }

    /**
     * ビューア動作モードに対応する設定画面を呼び出す
     */
    public void showSettingActivity() {
        Intent intent;
        switch (mEbookMode.getContentMode()) {
            case FXL:
                intent = new Intent(this, FxlViewerPreferenceActivity.class);
                break;
            case OMF:
                intent = new Intent(this, OmfViewerPreferenceActivity.class);
                break;
            default:
                // デフォルトは適当にFXLを設定しておく
                intent = new Intent(this, FxlViewerPreferenceActivity.class);
                break;
        }
        intent.putExtra(ViewerPreferenceActivity.EXTRA_CONTENT_TITLE,
                mBook.getTitle());
        intent.putExtra(ViewerPreferenceActivity.EXTRA_CONTENT_TITLE_KANA,
                mBook.getTitleKana());
        intent.putExtra(ViewerPreferenceActivity.EXTRA_CONTENT_AUTHOR,
                mBook.getAuthor());
        intent.putExtra(ViewerPreferenceActivity.EXTRA_CONTENT_AUTHOR_KANA,
                mBook.getAuthorKana());
        intent.putExtra(ViewerPreferenceActivity.EXTRA_CONTENT_FILE_PATH,
                mViewerContents.getPath());

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, REQUEST_CODE_SETTING);
    }

    /** 実装クラスごとの設定を行ったEbookModeを生成する */
    protected abstract EbookMode createEbookMode();

    /** 実装クラスごとの設定適用をする */
    protected abstract void applySetting();

    /** 実装クラスごとの端末回転処理を行う */
    protected abstract void orientationChange();
}
