package cn.jitmarketing.hot.adapter;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.adapter.AllocationListAdapter.ViewHolder;
import cn.jitmarketing.hot.entity.AllocationBean;
import cn.jitmarketing.hot.entity.AllocationNoticeBean;
import cn.jitmarketing.hot.ui.shelf.AllocationActivity;

public class AllocationNoticeListAdapter extends BaseAdapter {
	// /调拨通知单列表
	private List<AllocationNoticeBean> allocationNoticeList;
	private LayoutInflater inflater;
	private AllocationActivity activity;

	public AllocationNoticeListAdapter(AllocationActivity activity,
			List<AllocationNoticeBean> allocationNoticeList) {
		this.allocationNoticeList = allocationNoticeList;
		this.activity = activity;
		inflater = LayoutInflater.from(activity);
	}

	@Override
	public int getCount() {
		return allocationNoticeList.size();
	}

	@Override
	public Object getItem(int position) {
		return allocationNoticeList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.allocationnotice_item_layout, null);  
			holder = new ViewHolder();
			holder.allocationNotice_code = (TextView) convertView
					.findViewById(R.id.allocationNotice_code);
			holder.allcationNotice_come = (TextView) convertView
					.findViewById(R.id.allcationNotice_come);
		
			holder.allocationNotice_time = (TextView) convertView
					.findViewById(R.id.allocationNotice_time);
			holder.allocationNotice_type = (TextView) convertView
					.findViewById(R.id.allcationNotice_type);
			holder.comeNotice_layout = (LinearLayout) convertView
					.findViewById(R.id.comeNotice_layout);
			holder.allocationNotice_BeginDate=(TextView)convertView.findViewById(R.id.allocationNotice_BeginDate);
			holder.allocationNotice_EndDate=(TextView)convertView.findViewById(R.id.allocationNotice_EndDate);
			holder.allocationNotice_Qty = (TextView)convertView.findViewById(R.id.allocationNotice_Qty);
			holder.allocationNotice_LeftQty = (TextView)convertView.findViewById(R.id.allocationNotice_LeftQty);
			
			///holder.allcationNotice_delete = (Button) convertView.findViewById(R.id.allcation_delete);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.allocationNotice_code.setText(allocationNoticeList.get(position).allocationBatchNo);
		String iddd=allocationNoticeList.get(position).toUnitID;
		holder.allcationNotice_come.setText(allocationNoticeList.get(position).toUnitName+"("+allocationNoticeList.get(position).toUnitID+")");
		holder.allocationNotice_time.setText(allocationNoticeList.get(position).createTime);
		holder.allocationNotice_BeginDate.setText(allocationNoticeList.get(position).startDateTime);
		holder.allocationNotice_EndDate.setText(allocationNoticeList.get(position).endDateTime);
		holder.allocationNotice_Qty.setText(allocationNoticeList.get(position).qty+"");
		holder.allocationNotice_LeftQty.setText(allocationNoticeList.get(position).leftQty+"");
		///holder.allocationNotice_operater_txt.setText(allocationNoticeList.get(position).OperPerson);
		if(allocationNoticeList.get(position).toUnitName == null || allocationNoticeList.get(position).toUnitName.equals("")) {
			holder.comeNotice_layout.setVisibility(View.GONE);
		} else {	
			holder.comeNotice_layout.setVisibility(View.VISIBLE);
		}
		if(allocationNoticeList.get(position).Status == 0) {
			holder.allocationNotice_type.setText("暂存中");
			holder.allocationNotice_type.setVisibility(View.VISIBLE);
			holder.allcationNotice_delete.setVisibility(View.VISIBLE);
			holder.allocationNotice_type.setTextColor(activity.getResources().getColor(R.color.color_red_text));
		} else {
			holder.allocationNotice_type.setText("");
			/*if(allocationNoticeList.get(position).OrderType == 1) {
				holder.allcationnotice_type.setText("调拨");
				holder.allcationnotice_type.setTextColor(activity.getResources().getColor(R.color.diaobo));
			} else if(allocationNoticeList.get(position).OrderType == 2) {
				holder.allcationnotice_type.setText("退次");
				holder.allcationnotice_type.setTextColor(activity.getResources().getColor(R.color.tuici));
			} else if(allocationNoticeList.get(position).OrderType == 3) {
				holder.allcationnotice_type.setText("退仓");
				holder.allcationnotice_type.setTextColor(activity.getResources().getColor(R.color.tuicang));
			} else {
				holder.allcationnotice_type.setText("");
				holder.allcationnotice_type.setTextColor(activity.getResources().getColor(R.color.color_red_text));
			}*/
			holder.allocationNotice_type.setText("调拨通知");
			holder.allocationNotice_type.setTextColor(activity.getResources().getColor(R.color.diaobo));
			holder.allocationNotice_type.setVisibility(View.VISIBLE);
			///holder.allcationNotice_delete.setVisibility(View.GONE);
		}
		/*holder.allcationNotice_delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(activity)
				.setMessage("是否清除暂存数据?")
				.setPositiveButton(R.string.sureTitle,
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int whichButton) {
						activity.delete(allocationNoticeList.get(position).allocationBatchNo);
					}
				})
				.setNegativeButton(R.string.cancelTitle,
						new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
	
					}
				}).show();
			}
		});*/
		return convertView;
	}

	class ViewHolder {
		TextView allocationNotice_code;
		TextView allocationNotice_status;
		TextView allcationNotice_come;
		TextView allocationNotice_time;
		TextView allocationNotice_type;
		TextView allocationNotice_BeginDate;
		TextView allocationNotice_EndDate;
		TextView allocationNotice_Qty;
		TextView allocationNotice_LeftQty;
		LinearLayout comeNotice_layout;
		TextView allocationNotice_operater_txt;
		Button allcationNotice_delete;
	}
	
}
