
package jp.bpsinc.android.trainingbookshelf.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.bps.trainingbookshelf.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jp.bpsinc.android.trainingbookshelf.adapter.ShelfDrawerAdapter;
import jp.bpsinc.android.trainingbookshelf.content.ContentInfo;
import jp.bpsinc.android.trainingbookshelf.content.ContentManager;
import jp.bpsinc.android.trainingbookshelf.fragment.ListTypeFragment;
import jp.bpsinc.android.trainingbookshelf.fragment.ThumbTypeFragment;
import jp.bpsinc.android.trainingbookshelf.preferences.ShelfPreferences;
import jp.bpsinc.android.trainingbookshelf.util.ThumbnailLruCacheTools;
import jp.bpsinc.android.trainingbookshelf.view.ShelfDrawerLayout;

public class BookShelfMainActivity extends ActionBarActivity implements OnItemClickListener {

    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private Fragment mShelfFragment;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private ShelfDrawerAdapter mDrawerAdapter;
    private List<String> mDrawerItemList;
    private int mShelfType;
    private ShelfPreferences mShelfPreferences;
    private ArrayList<ContentInfo> mContentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookshelf_main);

        mShelfPreferences = new ShelfPreferences(this);

        // 本棚表示タイプ取得
        mShelfType = mShelfPreferences.getShelfType();

        // ドロワー関連
        mDrawerLayout = (ShelfDrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListView = (ListView) findViewById(R.id.drawer_listview);
        // ドロワー配列をリストに変更
        mDrawerItemList = (List<String>) Arrays.asList(getResources().getStringArray(
                R.array.drawer_items));
        mDrawerAdapter = new ShelfDrawerAdapter(this, R.id.drawer_listview, mDrawerItemList,
                mShelfType);
        mDrawerListView.setAdapter(mDrawerAdapter);
        mDrawerListView.setOnItemClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // 本棚fragment
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();

        if (isThumb()) {
            mShelfFragment = new ThumbTypeFragment();
        } else {
            mShelfFragment = new ListTypeFragment();
        }
        mFragmentTransaction.add(R.id.shelf_fragment_container,
                mShelfFragment);
        mFragmentTransaction.commit();
        ContentManager.loadEpubFile(mShelfFragment, mShelfType, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * ドロワーの閉じる開けるイベント中のInvalidateOptionsMenu()メソッドに呼ばれる、ドロワーのステータスにより、
     * actionBarのボタン配置を更新
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_edit).setVisible(ShelfDrawerLayout.getActionBarVisibility());
        menu.findItem(R.id.action_search).setVisible(ShelfDrawerLayout.getActionBarVisibility());
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * アクションバー各項目イベント
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // アプリアイコンイベント、ドロワーを呼び出すまたは閉じる
        if (ShelfDrawerLayout.getToggle().onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
        // TODO: actionBar イベント

        }
        return super.onOptionsItemSelected(item);
    }

    // TODO Drawer各項目イベント
    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
        mDrawerLayout.closeDrawers();
        TextView textView = (TextView) view.findViewById(R.id.drawer_item_text);
        switch (position) {
            case ShelfDrawerAdapter.DRAWER_MENU_CHANGE_SHELF_TYPE:
                // どの本棚タイプに変更する
                int changeToType = 0;
                // ドロワー項目テキスト
                String text = "";
                if (isList()) {
                    changeToType = ShelfPreferences.SHELF_TYPE_THUMBNAIL;
                    text = "リスト表示に切り替え";
                    mShelfFragment = new ThumbTypeFragment();
                } else {
                    changeToType = ShelfPreferences.SHELF_TYPE_LIST;
                    text = "サムネイル表示に切り替え";
                    mShelfFragment = new ListTypeFragment();
                }
                mShelfType = changeToType;
                textView.setText(text);
                // ここで、adapterのgetviewを呼ぶ、更新したテキストを表示させる　(ここで強制的に呼ばないと、4系は呼ばない、2系は呼ぶ)
                mDrawerItemList.set(position, text);
                mDrawerAdapter.notifyDataSetChanged();
                // TODO Fragmentのスタックや回収について、後でback keyのチケットを処理するとき調べて入れる
                FragmentTransaction ft = mFragmentManager.beginTransaction();
                ft.replace(R.id.shelf_fragment_container, mShelfFragment);
                ft.commit();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() != 0) {
            // fragmentスタックにまたendではない、バックキーで終了させない。 一番上のスタックを表示する。
            mFragmentManager.popBackStack();
        } else {
            mShelfPreferences.saveShelfType(mShelfType);
            super.onBackPressed();
        }
    }

    /**
     * Homeキー押した時点で本棚表示タイプを保存
     */
    @Override
    protected void onStop() {
        super.onStop();
        mShelfPreferences.saveShelfType(mShelfType);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ThumbnailLruCacheTools.getInstance().clear();
        mContentList = null;
    }

    private boolean isThumb() {
        return mShelfType == ShelfPreferences.SHELF_TYPE_THUMBNAIL;
    }

    private boolean isList() {
        return mShelfType == ShelfPreferences.SHELF_TYPE_LIST;
    }

    /**
     * fragmentにコンテンツ変数を渡す
     */
    public ArrayList<ContentInfo> getContentList() {
        return mContentList;
    }

    /**
     * EPUBファイル読み込み処理が終わったら、コンテンツリストを設定
     */
    public void setContentList(ArrayList<ContentInfo> list) {
        mContentList = list;
    }

}
