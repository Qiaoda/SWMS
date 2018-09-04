package cn.jitmarketing.hot.ui.sku;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.ex.lib.core.callback.ExRequestCallback;
import com.ex.lib.core.exception.ExException;
import com.ex.lib.core.utils.Ex;
import com.ex.lib.ext.xutils.annotation.ViewInject;
import com.example.scandemo.BaseSwipeOperationActivity;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cn.jitmarketing.hot.HotApplication;
import cn.jitmarketing.hot.HotConstants;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.entity.OtherShopStockEntity;
import cn.jitmarketing.hot.entity.ResultNet;
import cn.jitmarketing.hot.util.ExitAppUtils;
import cn.jitmarketing.hot.util.FastDoubleClickUtil;
import cn.jitmarketing.hot.util.LogUtils;
import cn.jitmarketing.hot.view.AutoListView;
import cn.jitmarketing.hot.view.TitleWidget;
import cn.jitmarketing.hot.view.AutoListView.OnLoadListener;
import cn.jitmarketing.hot.view.AutoListView.OnRefreshListener;

/**
 * 库存查询
 */
public class OtherShopStockActivity extends BaseSwipeOperationActivity implements OnClickListener,OnRefreshListener, OnLoadListener{
	
	/**
	 * title控件
	 */
	@ViewInject(R.id.othershop_stock_title)
	TitleWidget othershopStockTitle;

	//@ViewInject(R.id.ll_header)
	//LinearLayout ll_header;

	@ViewInject(R.id.lv_stock)
	private AutoListView lv_stock;
	//@ViewInject(R.id.detail_list)
	//AutoListView only_list;
	//private LinearLayout headerParent; 
	@ViewInject(R.id.ll_header)
	private LinearLayout ll_header;
	//private LinearLayout footerParent;
	//private LinearLayout ll_footer;
	
	@ViewInject(R.id.tv_info)
	private TextView tv_info;
	
	private StockAdapter adapter;
	
	private AlertDialog queryDialog;
	private TextView tv_query;
	private EditText et_shop_id;
	private EditText et_sku_item;
	
	private TextView total_stock;
	
	private List<OtherShopStockEntity> lists = new ArrayList<>();
	private List<OtherShopStockEntity> newLists = new ArrayList<>();
	
	HotApplication ap;
	private String skuCodeStr;
	private int sumQry;
	private int pageIndex = 1;
	private final int pageSize = 20;
	private boolean isRefresh;
	private static final int WHAT_NET_GET_STOCK = 0x10;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitAppUtils.getInstance().addActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ExitAppUtils.getInstance().delActivity(this);
	}

	@Override
	protected void exInitAfter() {
	}

	@Override
	protected void exInitBundle() {
		initIble(this, this);
	}

	@Override
	protected int exInitLayout() {
		return R.layout.activity_othershop_stock;
	}

	@Override
	protected void exInitView() {
		ap = (HotApplication) getApplication();
		othershopStockTitle.setOnLeftClickListner(this);
		othershopStockTitle.setOnRightClickListner(this);
		
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.query_othershop_stock_dialog, null);
		tv_query = (TextView)view.findViewById(R.id.tv_query);
		tv_query.setOnClickListener(this);
		et_shop_id = (EditText)view.findViewById(R.id.et_shop_id);
		//et_shop_id.setOnClickListener(this);
		et_sku_item = (EditText)view.findViewById(R.id.et_sku_item);
		//et_sku_item.setOnClickListener(this);
		
		queryDialog = new AlertDialog.Builder(this).create();
		queryDialog.setView(view, 0, 0, 0, 0);

		//headerParent = (LinearLayout) inflater.inflate(R.layout.othershop_stock_item_header, null);
		//ll_header = (LinearLayout) headerParent.findViewById(R.id.ll_header);
		//lv_stock.addHeaderView(headerParent);
		//ll_header.setVisibility(View.GONE);
		total_stock = (TextView) ll_header.findViewById(R.id.total_stock);
		
		//footerParent = (LinearLayout) inflater.inflate(R.layout.othershop_stock_item_footer, null);
		//ll_footer = (LinearLayout) footerParent.findViewById(R.id.ll_footer);
		//lv_stock.addFooterView(footerParent);
		//ll_footer.setVisibility(View.GONE);
		lv_stock.setOnRefreshListener(this);
		lv_stock.setOnLoadListener(this);
		lv_stock.setPageSize(pageSize);
		lv_stock.onLoadComplete();
		adapter = new StockAdapter(this, lists);
		lv_stock.setAdapter(adapter);
		hideView();
	}
	
	private void hideView(){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				lv_stock.setVisibility(View.GONE);
				ll_header.setVisibility(View.GONE);
				
			}
		});
	}
	
	private void shoView(){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				lv_stock.setVisibility(View.VISIBLE);
				ll_header.setVisibility(View.VISIBLE);
				
			}
		});
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public Map<String, String> onStart(int what) {
		return null;
	}


	@Override
	public void onError(int what, int e, String message) {
		Toast.makeText(this, "网络请求失败", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onSuccess(int what, String result, boolean isCache) {
		ResultNet<?> net = Ex.T().getString2Cls(result, ResultNet.class);
		if (!net.isSuccess) {
			Ex.Toast(mContext).showLong(net.message);
			return;
		}
		switch (what) {
		case WHAT_NET_GET_STOCK:// 获取商品信息
			if (null == net.data) {
				Ex.Toast(mActivity).show(net.message);
				return;
			} 
			try {
				JSONObject dataJson = new JSONObject(mGson.toJson(net.data));
				sumQry = dataJson.getInt("SumQry");
				total_stock.setText("(总:"+sumQry+")");
				lists.clear();
				lists.addAll((ArrayList<OtherShopStockEntity>)mGson.fromJson(dataJson.getString("List"), new TypeToken<List<OtherShopStockEntity>>() {
				}.getType()));
				
				lv_stock.setResultSize(lists.size());
				if(lists.size() > 0){
					shoView();
					tv_info.setVisibility(View.GONE);
				}
				queryDialog.dismiss();
				adapter.notifyDataSetChanged();
				//pageIndex++;
			} catch (JSONException e) {
				LogUtils.logOnFile("->解析搜索结果出错:\n"+ExceptionUtils.getStackTrace(e));
			}
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (FastDoubleClickUtil.isFastDoubleClick()) {
			Log.i("fast", "fast");
			return;
		}
		switch (v.getId()) {
		case R.id.lv_left:
			this.finish();
			break;
		case R.id.lv_right:
			queryDialog.show();
			break;
		case R.id.tv_query:
			if(TextUtils.isEmpty(et_sku_item.getText().toString())){
				Toast.makeText(OtherShopStockActivity.this, "SKU/款不能为空", Toast.LENGTH_SHORT).show();
				return;
			}
			pageIndex = 1;
			try {
				JSONObject postData = new JSONObject();
				postData.put("SkuCode", et_sku_item.getText().toString());
				postData.put("StoreID", et_shop_id.getText().toString());
				postData.put("_PageIndex", "0");
				postData.put("_PageSize", String.valueOf(pageSize));
				startJsonTask(HotConstants.Global.APP_URL_USER + HotConstants.Global.OTHER_SHOP_STOCK, WHAT_NET_GET_STOCK, true, NET_METHOD_POST, postData.toString(), false);
			} catch (JSONException e) {
				LogUtils.logOnFile(OtherShopStockActivity.class.getSimpleName()+"解析JSON错误:"+ExceptionUtils.getStackTrace(e));
			}
			
			break;
		}

	}
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what==1) {
				lv_stock.setResultSize(newLists.size());
				if(isRefresh) {
					newLists.clear();
					lv_stock.onRefreshComplete();
					Ex.Toast(mContext).showLong("刷新成功");
				} else {
					lv_stock.onLoadComplete();
				}
				lists.addAll(newLists);
				adapter.notifyDataSetChanged();
			} else if(msg.what==2){
				if(isRefresh) {
					lv_stock.onRefreshComplete();
					Ex.Toast(mContext).showLong("刷新失败");
				} else {
					lv_stock.onLoadComplete();
					Ex.Toast(mContext).showLong("加载失败");
				}
			}
		}
	};
	
	ExRequestCallback requestCallback = new ExRequestCallback() {
		@Override
		public void onSuccess(InputStream inStream,
				HashMap<String, String> cookies) {
			// 请求结果
			String result = "";
			// 判断结果流是否为空
			if (inStream != null) {
				// 转换流对象
				result = Ex.T().getInStream2Str(inStream);
				ResultNet<?> net = Ex.T().getString2Cls(result,
						ResultNet.class);
				if (!net.isSuccess) {
					Ex.Toast(mContext).showLong(net.message);
					return;
				}
				
				try {
					JSONObject dataJson = new JSONObject(mGson.toJson(net.data));
					List<OtherShopStockEntity> results = mGson.fromJson(dataJson.getString("List"),
							new TypeToken<List<OtherShopStockEntity>>() {
							}.getType());
					newLists.clear();
					newLists.addAll(results);
					//adapter.notifyDataSetChanged();
				} catch (JSONException e) {
					LogUtils.logOnFile(OtherShopStockActivity.class.getSimpleName()+"解析JSON错误:"+ExceptionUtils.getStackTrace(e));
					handler.sendEmptyMessage(2);
				}
				
				handler.sendEmptyMessage(1);
			}
		}
		@Override
		public void onError(int statusCode, ExException e) {
			handler.sendEmptyMessage(2);
		}
	};

	@Override
	public void onLoad() {
		pageIndex++;
		try {
			JSONObject postData = new JSONObject();
			postData.put("SkuCode", et_sku_item.getText().toString());
			postData.put("StoreID", et_shop_id.getText().toString());
			postData.put("_PageIndex", String.valueOf(pageIndex));
			postData.put("_PageSize", String.valueOf(pageSize));
			lv_stock.onRefreshComplete();
			isRefresh = false;
			Ex.Net(mContext).sendJsonAsyncPost(HotConstants.Global.APP_URL_USER + HotConstants.Global.OTHER_SHOP_STOCK, postData.toString(), requestCallback);
		} catch (JSONException e) {
			LogUtils.logOnFile(OtherShopStockActivity.class.getSimpleName()+"解析JSON错误:"+ExceptionUtils.getStackTrace(e));
		}
	}

	@Override
	public void onRefresh() {
		pageIndex = 1;
		JSONObject postData = new JSONObject();
		try {
			postData.put("SkuCode", et_sku_item.getText().toString());
			postData.put("StoreID", et_shop_id.getText().toString());
			postData.put("_PageIndex", "0");
			postData.put("_PageSize", String.valueOf(pageSize));
			lv_stock.onLoadComplete();
			isRefresh = true;
			Ex.Net(mContext).sendJsonAsyncPost(HotConstants.Global.APP_URL_USER + HotConstants.Global.OTHER_SHOP_STOCK, postData.toString(), requestCallback);	
		} catch (JSONException e) {
			LogUtils.logOnFile(OtherShopStockActivity.class.getSimpleName()+"解析JSON错误:"+ExceptionUtils.getStackTrace(e));
		}
		
	}


	@Override
	public void fillCode(String code) {
		this.skuCodeStr = code;
		if (code != null) {
			if(!queryDialog.isShowing())
				queryDialog.show();
			et_sku_item.setText(skuCodeStr);
		} else {
			ap.getsoundPool(ap.Sound_error);
			return;
		}
	}
	
	class StockAdapter extends BaseAdapter{
		
		Context context;
		List<OtherShopStockEntity> lists;
		LayoutInflater inflater;
		
		public StockAdapter(Context context, List<OtherShopStockEntity> lists) {
			this.context = context;
			this.lists = lists;
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return lists.size();
		}

		@Override
		public Object getItem(int position) {
			return lists.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.othershop_stock_item, null);
				holder = new ViewHolder();
				//holder.tv_num = (TextView) convertView.findViewById(R.id.tv_num);
				//holder.tv_shop_id = (TextView) convertView.findViewById(R.id.tv_shop_id);
				holder.tv_shop_id_name = (TextView) convertView.findViewById(R.id.tv_shop_id_name);
				holder.tv_sku = (TextView) convertView.findViewById(R.id.tv_sku);
				holder.tv_stock = (TextView) convertView.findViewById(R.id.tv_stock);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			//holder.tv_num.setText(lists.get(position).num+"");
			//holder.tv_shop_id.setText(lists.get(position).shop_id);
			OtherShopStockEntity stockEntity = lists.get(position);
			int type = stockEntity.Type;
			String qry = stockEntity.Qry;
			if(type == 1){
				if(Double.parseDouble(qry) > 0){
					qry = "有";
				}else{
					qry = "无";
				}
			}
			holder.tv_shop_id_name.setText(lists.get(position).StoreID + "\n" +lists.get(position).StoreName);
			holder.tv_sku.setText(lists.get(position).SkuCode);
			holder.tv_stock.setText(qry);

			return convertView;
		}
		
		class ViewHolder{
			//TextView tv_num;
			//TextView tv_shop_id;
			TextView tv_shop_id_name;
			TextView tv_sku;
			TextView tv_stock;
		}
	}

}
