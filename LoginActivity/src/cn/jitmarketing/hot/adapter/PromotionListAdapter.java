package cn.jitmarketing.hot.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.jitmarketing.hot.HotConstants;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.entity.PromotionEntity;

public class PromotionListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<PromotionEntity> promotionList;
	private Activity activity;
	
	public PromotionListAdapter(Activity activity, List<PromotionEntity> promotionList){
		this.activity = activity;
		this.promotionList = promotionList;
		inflater = LayoutInflater.from(activity);
	}
	
	@Override
	public int getCount() {
		return promotionList.size();
	}

	@Override
	public Object getItem(int position) {
		
		return promotionList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.promotion_layout, null);  
			holder = new ViewHolder();
			holder.tv_promotion_name = (TextView) convertView.findViewById(R.id.tv_promotion_name);
			holder.tv_promotion_date = (TextView) convertView.findViewById(R.id.tv_promotion_date);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		PromotionEntity item = promotionList.get(position);
		holder.tv_promotion_name.setText(item.promotionName);
		holder.tv_promotion_date.setText("促销日期: " + item.startDate.replaceAll("-", "/") + " - " + item.endDate.replaceAll("-", "/"));
		 
		return convertView;
	}
	
	public class ViewHolder {
		public TextView tv_promotion_name;
		public TextView tv_promotion_date;
	}

}
