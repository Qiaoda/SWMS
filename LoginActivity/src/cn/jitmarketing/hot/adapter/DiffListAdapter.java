package cn.jitmarketing.hot.adapter;

import java.util.List;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.R.color;
import cn.jitmarketing.hot.R.drawable;
import cn.jitmarketing.hot.adapter.CGYScanSkuAdapter.ViewHolder;
import cn.jitmarketing.hot.entity.CheckBean;
import cn.jitmarketing.hot.entity.Diff;
import cn.jitmarketing.hot.entity.InStockDetail;
import cn.jitmarketing.hot.entity.InStockSku;

/** 核对信息的Adapter */
public class DiffListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<Diff> skuList;

	public DiffListAdapter(LayoutInflater inflater,
			List<Diff> skuList) {
		this.inflater = inflater;
		this.skuList = skuList;
	}

	@Override
	public int getCount() {
		return skuList.size();
	}

	@Override
	public Object getItem(int position) {

		return skuList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder=null;
		if(convertView==null){
			convertView = inflater.inflate(R.layout.diff_item_layout, null);
			holder=new ViewHolder();
			holder.check_sku_code = (TextView) convertView
					.findViewById(R.id.check_sku_code);
			holder.sys_count = (TextView) convertView
					.findViewById(R.id.sys_count);
			holder.diff_count = (TextView) convertView
					.findViewById(R.id.diff_count);
			holder.lin_check=(LinearLayout) convertView.findViewById(R.id.lin_check);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		Diff item = skuList.get(position);
		holder.check_sku_code.setText("编码:  " + item.skuid);
		holder.sys_count.setText(""+(int)item.SkuQry);
		holder.diff_count.setText(String.valueOf(item.SkuQry-item.SkuDiffQry));
		//holder.diff_count.setText(""+(int)item.ConfirmQry);
		return convertView;
	}
	class ViewHolder{
		LinearLayout lin_check;
		TextView check_sku_code;
		TextView sys_count;
		TextView diff_count;
	}
}
