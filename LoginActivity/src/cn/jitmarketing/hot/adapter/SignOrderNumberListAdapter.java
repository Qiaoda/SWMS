package cn.jitmarketing.hot.adapter;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import cn.jitmarketing.hot.R;

public class SignOrderNumberListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<String> newList;
	private Context context;
	
	public SignOrderNumberListAdapter(LayoutInflater inflater, List<String> newList, Context context){
		this.inflater = inflater;
		this.newList = newList;
		this.context = context;
	}
	
	
	
	public List<String> getNewList() {
		return newList;
	}



	public void setNewList(List<String> newList) {
		this.newList = newList;
	}



	@Override
	public int getCount() {
		return newList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder=null;
		if(convertView==null){
			convertView = inflater.inflate(R.layout.sign_ordernumber_item_layout, null);
			holder=new ViewHolder();
			holder.tv_order_number = (TextView) convertView.findViewById(R.id.tv_order_number);
			holder.tv_order = (TextView) convertView.findViewById(R.id.tv_order);
			holder.btn_del = (Button) convertView.findViewById(R.id.btn_del);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tv_order.setText(String.valueOf(position+1));
		holder.tv_order_number.setText(newList.get(position));
		holder.btn_del.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder
					.setTitle("是否删除此条记录？")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							newList.remove(position);
							SignOrderNumberListAdapter.this.notifyDataSetChanged();
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					})
					.setCancelable(false)
					.create()
					.show();
				
			}
		});
		
		return convertView;
	}
	class ViewHolder{
		TextView tv_order;
		TextView tv_order_number;
		Button btn_del;
	}
}
