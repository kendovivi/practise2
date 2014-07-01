
package jp.bpsinc.android.chogazo.viewer.activity;

import java.util.ArrayList;
import java.util.List;

import jp.bpsinc.android.chogazo.viewer.R;
import jp.bpsinc.android.chogazo.viewer.adapter.NavInfo;
import jp.bpsinc.android.chogazo.viewer.adapter.NavListAdapter;
import jp.bpsinc.android.chogazo.viewer.util.LayoutUtil;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class NavActivity extends FragmentActivity implements OnItemClickListener {
    /** 入力値：ビューアから渡される目次タイトルリスト */
    public static final String INTENT_KEY_NAV_TITLE = "INTENT_KEY_NAV_TITLE";
    /** 入力値：ビューアから渡される目次ページインデックスリスト */
    public static final String INTENT_KEY_NAV_PAGE = "INTENT_KEY_NAV_PAGE";
    /** 戻り値：ビューアに返す目次ページインデックス */
    public static final String INTENT_DATA_KEY_NAV_PAGE = "INTENT_DATA_KEY_NAV_PAGE";

    /** 目次情報を表示するリストビュー */
    private ListView mListView;
    /** アダプター生成に使う目次情報リスト */
    private List<NavInfo> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cho_gazo_viewer_nav);
        LayoutUtil.setupTitleBar(this, R.string.viewer_nav_head_text);

        Intent intent = getIntent();
        String[] titleList = intent.getStringArrayExtra(INTENT_KEY_NAV_TITLE);
        int[] pageIndexList = intent.getIntArrayExtra(INTENT_KEY_NAV_PAGE);
        mList = new ArrayList<NavInfo>();
        for (int i = 0; i < titleList.length; i++) {
            mList.add(new NavInfo(titleList[i], pageIndexList[i]));
        }
        mListView = (ListView) findViewById(R.id.cho_gazo_viewer_nav_list);
        mListView.setAdapter(new NavListAdapter(getApplicationContext(), mList));
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        LogUtil.v("position=%d, id=%d", position, id);

        NavInfo info = (NavInfo) mListView.getItemAtPosition(position);
        Intent intent = new Intent();
        intent.putExtra(INTENT_DATA_KEY_NAV_PAGE, info.getSinglePageIndex());
        setResult(RESULT_OK, intent);
        finish();
    }
}
