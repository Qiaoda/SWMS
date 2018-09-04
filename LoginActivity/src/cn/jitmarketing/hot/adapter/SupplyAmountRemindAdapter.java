package cn.jitmarketing.hot.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.jitmarketing.hot.HotConstants;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.entity.RemindSampleBean;

public class SupplyAmountRemindAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<RemindSampleBean> skuList;
    private Activity activity;

    public SupplyAmountRemindAdapter(Activity activity, List<RemindSampleBean> skuList) {
        this.activity = activity;
        this.skuList = skuList;
        inflater = LayoutInflater.from(activity);
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
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.supply_amount_remind_item_layout, null);
            holder = new ViewHolder();
            holder.tv_sku = (TextView) convertView.findViewById(R.id.tv_sku);
            holder.tv_qty = (TextView) convertView.findViewById(R.id.tv_qty);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RemindSampleBean item = skuList.get(position);
        holder.tv_sku.setText(item.SKUID);
        holder.tv_qty.setText(item.EndQty);
        holder.tv_sku.setSelected(true);
        //在这里加个判断，若为选中项，则改变背景图片和背景色
        if (HotConstants.SELECTED == position) {
            convertView.setBackgroundResource(R.drawable.shape_green_frame_sharp);
        } else {
            convertView.setBackgroundResource(R.drawable.shape_gray_frame_sharp);
        }
        return convertView;
    }

    public class ViewHolder {
        public TextView tv_sku;
        public TextView tv_qty;
    }

}
