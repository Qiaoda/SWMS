package cn.jitmarketing.hot.adapter;

import cn.jitmarketing.hot.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class RandomCheckScanAdapter extends BaseAdapter {

	private Context mContext;
	
	public RandomCheckScanAdapter(Context mContext) {
	
		this.mContext = mContext;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 20;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder=null;
		if (convertView==null) {
			viewHolder=new ViewHolder();
			convertView=LayoutInflater.from(mContext).inflate(R.layout.adapter_random_check_scan, null);
		}else{
			viewHolder=(ViewHolder) convertView.getTag();
		}
		
		return convertView;
	}
	
	private static class ViewHolder{
		
	}

}
