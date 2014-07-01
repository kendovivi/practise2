
package jp.bpsinc.android.chogazo.viewer.adapter;

import java.util.List;

import jp.bpsinc.android.chogazo.viewer.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NavListAdapter extends ArrayAdapter<NavInfo> {
    private class NavListViewHolder {
        TextView navTitle;
        TextView navPage;
    }

    private final LayoutInflater mInflater;

    public NavListAdapter(Context context, List<NavInfo> list) {
        super(context, 0, list);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NavListViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.cho_gazo_viewer_nav_row, null);

            holder = new NavListViewHolder();
            holder.navTitle = (TextView) convertView.
                    findViewById(R.id.cho_gazo_viewer_nav_row_title);
            holder.navPage = (TextView) convertView.
                    findViewById(R.id.cho_gazo_viewer_nav_row_page);

            convertView.setTag(holder);
        } else {
            holder = (NavListViewHolder) convertView.getTag();
        }
        NavInfo info = getItem(position);
        holder.navTitle.setText(info.getTitle());
        holder.navPage.setText(String.valueOf(info.getSinglePageIndex() + 1) + "ページ");
        return convertView;
    }
}
