
package jp.bpsinc.android.trainingbookshelf.view;

import android.content.Context;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import com.bps.trainingbookshelf.R;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;

public class ShelfDrawerLayout extends DrawerLayout {
    private ActionBarActivity mActivity;
    private ShelfDrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private static ActionBarDrawerToggle sToggle;
    /** ドロワーを開くフラグ */
    private boolean mToOpenDrawer;
    /** ドロワーのドラッグ方向 */
    private boolean mIsDrawerMoveToRight;
    /** アクションバーアイコンを表示するフラグ */
    private static boolean sIsActionBarVisible = true;
    /** ドロワー直前offsetを記録用 */
    private float mLastOffset;

    public ShelfDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (ActionBarActivity) context;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mDrawerLayout = (ShelfDrawerLayout) mActivity.findViewById(R.id.drawer_layout);
        mDrawerListView = (ListView) mActivity.findViewById(R.id.drawer_listview);

        sToggle = new ActionBarDrawerToggle(mActivity, mDrawerLayout, R.drawable.ic_drawer,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                switch (newState) {
                    case DrawerLayout.STATE_DRAGGING:
                        sIsActionBarVisible = false;
                        break;
                    // 完全に閉じるか開く状態
                    case DrawerLayout.STATE_IDLE:
                        if (!mDrawerLayout.isDrawerVisible(mDrawerListView)) {
                            sIsActionBarVisible = true;
                        }
                        break;
                    // アプリアイコンをタップする時、アクションバーアイコンを消す処理
                    // アプリアイコンをタップ動作は、STATE_DRAGGINGに入らない。この状態に入る。
                    case DrawerLayout.STATE_SETTLING:
                        // これから、drawerが見えるようになるので、actionbarを消す。
                        if (!mDrawerLayout.isDrawerVisible(mDrawerListView)) {
                            sIsActionBarVisible = false;
                        }
                        break;
                }
                LogUtil.i("newState -->> %d, isDrawerVisible -->> %b, isDrawerOpen -->> %b",
                        newState, mDrawerLayout.isDrawerVisible(mDrawerListView),
                        mDrawerLayout.isDrawerOpen(mDrawerListView));
                mActivity.supportInvalidateOptionsMenu();

            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                mToOpenDrawer = slideOffset > 0.1F ? true : false;
                mIsDrawerMoveToRight = (slideOffset > mLastOffset) ? true : false;
                mLastOffset = slideOffset;
            }

        };
        this.setDrawerListener(sToggle);
        mActivity.supportInvalidateOptionsMenu();
        sToggle.syncState();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                // ドロワーoffsetが一定以上、ドロワーのドラッグ方向が右向きの場合ドロワーを開く
                if (mToOpenDrawer && mIsDrawerMoveToRight) {
                    openDrawer(mDrawerListView);
                    return true;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    public static boolean getActionBarVisibility() {
        return sIsActionBarVisible;
    }

    public static ActionBarDrawerToggle getToggle() {
        return sToggle;
    }

}
