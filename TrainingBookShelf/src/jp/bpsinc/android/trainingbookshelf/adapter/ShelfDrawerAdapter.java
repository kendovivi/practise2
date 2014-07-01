
package jp.bpsinc.android.trainingbookshelf.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.bps.trainingbookshelf.R;
import java.util.ArrayList;
import java.util.List;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.trainingbookshelf.preferences.ShelfPreferences;

public class ShelfDrawerAdapter extends ArrayAdapter<String> {

    /** ドロワーコンテンツタイプ数 */
    private static final int DRAWER_TYPE_MAXCOUNT = 2;
    /** ドロワーコンテンツタイプ　ラベル */
    private static final int DRAWER_TYPE_SEPARATOR = 1;
    /** ドロワーコンテンツタイプ　項目 */
    private static final int DRAWER_TYPE_MENU = 2;

    /** ドロワー項目位置 */
    // private static final int DRAWER_MENU_LOGIN = 0;
    // private static final int DRAWER_MENU_BPSSHOP = 1;
    public static final int DRAWER_MENU_CHANGE_SHELF_TYPE = 3;
    // private static final int DRAWER_MENU_SETTING = 4;
    // private static final int DRAWER_MENU_MAIN_HONDANA = 6;
    // private static final int DRAWER_MENU_RECENTREAD = 7;
    // private static final int DRAWER_MENU_MYHONDANA_LIST = 8;

    private static final int DRAWER_SEPARATOR_SETTING = 2;
    private static final int DRAWER_SEPARATOR_HONDANA = 5;

    private static final String DRAWER_MENU_TEXT_CHANGE_TO_LIST = "リスト表示に切り替え";
    private static final String DRAWER_MENU_TEXT_CHANGE_TO_THUMBNAIL = "サムネイル表示に切り替え";

    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> mDrawerItemList;
    private ArrayList<Integer> mSeparatorList;
    private ItemInfo mItemInfo;

    public ShelfDrawerAdapter(Context context, int resource, List<String> itemList, int shelfType) {
        super(context, resource, itemList);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDrawerItemList = itemList;
        mSeparatorList = new ArrayList<Integer>();
        String text = shelfType == ShelfPreferences.SHELF_TYPE_THUMBNAIL ? DRAWER_MENU_TEXT_CHANGE_TO_LIST
                : DRAWER_MENU_TEXT_CHANGE_TO_THUMBNAIL;
        mDrawerItemList.set(DRAWER_MENU_CHANGE_SHELF_TYPE, text);
    }

    @Override
    public int getViewTypeCount() {
        // TODO: 何でtypecountは +1になるだろう。。2個しかないのに、後で調べる
        return DRAWER_TYPE_MAXCOUNT + 1;
    }

    @Override
    public int getItemViewType(int position) {
        mItemInfo = new ItemInfo(position);
        return mItemInfo.getViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        ViewHolder viewHolder;
        View view = convertView;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            switch (viewType) {
            // 分割線
                case DRAWER_TYPE_SEPARATOR:
                    view = mInflater.inflate(R.layout.drawer_separator, null);
                    viewHolder.textView = (TextView) view
                            .findViewById(R.id.drawer_separator);
                    setSeparatorText(position);
                    view.setOnClickListener(null);
                    break;
                // ドロワー項目
                case DRAWER_TYPE_MENU:
                    view = mInflater.inflate(R.layout.drawer_item, null);
                    viewHolder.textView = (TextView) view
                            .findViewById(R.id.drawer_item_text);
                    break;
                default:
                    break;
            }
            view.setTag(viewHolder);
        } else {
            LogUtil.i(" 【ドロワー項目】 -->> %s , 【viewType】 -->> %s", mDrawerItemList.get(position),
                    viewType);
            viewHolder = (ViewHolder) view.getTag();
        }
        // ドロワー項目テキスト設定
        viewHolder.textView.setText(mDrawerItemList.get(position));
        return view;
    }

    class ViewHolder {
        TextView textView;
    }

    /**
     * リストアイテムの情報をセット
     */
    class ItemInfo {
        int viewType;

        public ItemInfo(int position) {
            // 該当アイテムのviewTypeをセットする
            // separatorリストに入っている
            if (mSeparatorList.contains(position)) {
                viewType = DRAWER_TYPE_SEPARATOR;
            }
            // xmlから読み込んで、これからseparatorリストに入れる。
            if (mDrawerItemList.get(position).equals("separator")
                    && !mSeparatorList.contains(position)) {
                mSeparatorList.add(position);
                viewType = DRAWER_TYPE_SEPARATOR;
            }
            // ドロワーメニュー項目
            if (!mDrawerItemList.get(position).equals("separator")
                    && !mSeparatorList.contains(position)) {
                viewType = DRAWER_TYPE_MENU;
            }
        }

        public int getViewType() {
            return viewType;
        }
    }

    /**
     * ドロワーラベルテキスト設定
     * 
     * @param position
     */
    private void setSeparatorText(int position) {
        String text = "";
        switch (position) {
            case DRAWER_SEPARATOR_SETTING:
                text = mContext.getString(R.string.drawer_separator_setting_text);
                break;
            case DRAWER_SEPARATOR_HONDANA:
                text = mContext.getString(R.string.drawer_separator_hondana_text);
                break;
        }
        mDrawerItemList.set(position, text);
    }

}
