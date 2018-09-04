package cn.jitmarketing.hot.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.entity.ReceiveSheet;

public class InStockDiffListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<ReceiveSheet> receList;
	private Activity activity;
	int selectItem;

	public InStockDiffListAdapter(Activity activity, LayoutInflater inflater, List<ReceiveSheet> receList) {
		this.inflater = inflater;
		this.receList = receList;
		this.activity = activity;
	}

	@Override
	public int getCount() {
		return receList.size();
	}

	@Override
	public Object getItem(int position) {
		return receList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.instock_sign_list_item_layout, null);
			holder = new ViewHolder();
			holder.self_sku_layout = (LinearLayout) convertView.findViewById(R.id.self_sku_layout);
			holder.orign_order_num = (TextView) convertView.findViewById(R.id.orign_order_num);
			holder.order_num = (TextView) convertView.findViewById(R.id.order_num);
			holder.sys_count = (TextView) convertView.findViewById(R.id.sys_count);
			holder.diff_count = (TextView) convertView.findViewById(R.id.diff_count);
			holder.confirm_time = (TextView) convertView.findViewById(R.id.confirm_time);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		ReceiveSheet item = receList.get(position);
		
		holder.orign_order_num.setText(item.receiveSheetNo);
		holder.order_num.setText(item.DisOrderNo);
		holder.sys_count.setText(item.Qry);
		holder.diff_count.setText(item.DiffQry);
		holder.confirm_time.setText(item.ConfirmTime);
		
		if (position == selectItem) {
			holder.self_sku_layout.setBackgroundResource(R.drawable.shadow_red_bg);
		} else {
			holder.self_sku_layout.setBackgroundResource(R.drawable.shadow_bg);
		}

		return convertView;
	}

	public void setSelectItem(int selectItem) {
		this.selectItem = selectItem;
	}

	class ViewHolder {
		LinearLayout self_sku_layout;
		TextView orign_order_num;
		TextView order_num;
		TextView sys_count;
		TextView diff_count;
		TextView confirm_time;
	}

}
