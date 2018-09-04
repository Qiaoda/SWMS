package cn.jitmarketing.hot.ui.sample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.ex.lib.core.utils.Ex;
import com.ex.lib.ext.xutils.annotation.ViewInject;
import com.example.scandemo.BaseSwipeOperationActivity;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import cn.jitmarketing.hot.HotApplication;
import cn.jitmarketing.hot.HotConstants;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.adapter.SupplyAmountRemindAdapter;
import cn.jitmarketing.hot.entity.RemindSampleBean;
import cn.jitmarketing.hot.entity.ResultNet;
import cn.jitmarketing.hot.net.SkuNet;
import cn.jitmarketing.hot.util.ExitAppUtils;
import cn.jitmarketing.hot.util.LogUtils;
import cn.jitmarketing.hot.util.SkuUtil;
import cn.jitmarketing.hot.view.ClearEditText;
import cn.jitmarketing.hot.view.TitleWidget;

/**
 * 补量提醒 Created by fgy on 2016/4/11.
 */
public class SupplyAmountRemindActivity extends BaseSwipeOperationActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

	private static final int WHAT_NET_GET_SUPPLY_REMIND_SAMPLE_LIST = 0;
	private static final int WHAT_NET_GET_SUPPLY_REMIND_SAMPLE_ONT = 1;
	private static final int WHAT_NET_ADD_SUPPLY_REMIND_NOTICE = 2;

	@ViewInject(R.id.allocation_title)
	private TitleWidget allocation_title;
	@ViewInject(R.id.check_stock_search_edt)
	private ClearEditText check_stock_search_edt;
	@ViewInject(R.id.tv_no_data)
	private TextView tv_no_data;
	@ViewInject(R.id.ll_loadMore)
	private LinearLayout ll_loadMore;
	@ViewInject(R.id.list_sku)
	private ListView list_sku;

	@ViewInject(R.id.sp_type)
	private Spinner sp_type;

	private static final int PAGE_SIZE = 10000;
	private static final String TAG = "SupplyAmountRemindActivity";
	private int curPageIndex = 0;
	private boolean isLoading = false;

	private HotApplication ap;
	private String scanSkuCodeStr = "";
	private ArrayList<RemindSampleBean> mList;
	private SupplyAmountRemindAdapter adapter;
	
	private int[] bigTypes = new int[]{1,2,4,5,6,7,0};
	private int selectItem = 6;

	@Override
	protected int exInitLayout() {
		return R.layout.activity_supply_amount_remind;
	}

	@Override
	protected void exInitBundle() {
		initIble(this, this);
		mList = new ArrayList<RemindSampleBean>();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitAppUtils.getInstance().addActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ExitAppUtils.getInstance().delActivity(this);
		mList.clear();
		//mList = null;
		//adapter = null;
	}

	@Override
	protected void exInitView() {
		ap = (HotApplication) getApplication();
		allocation_title.setOnLeftClickListner(this);
		check_stock_search_edt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					hideSoftKeyBoard(mActivity, check_stock_search_edt);
					if(StringUtils.isNotBlank(textView.getText()))
						fillCode(textView.getText().toString());
					else
						Ex.Toast(mContext).showShort("商品编号不能为空");
				}
				return false;
			}
		});
		check_stock_search_edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				/*
				 * if (hasFocus) btn_custom_search.setVisibility(View.VISIBLE);
				 * else btn_custom_search.setVisibility(View.GONE);
				 */
			}
		});
		check_stock_search_edt.addTextChangedListener(new TextWatcher() {
		
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(StringUtils.isBlank(s)){
					scanSkuCodeStr = "";
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		list_sku.setOnScrollListener(onScrollListener);

		adapter = new SupplyAmountRemindAdapter(mActivity, mList);
		list_sku.setAdapter(adapter);
		list_sku.setOnItemClickListener(this);
		list_sku.setEmptyView(tv_no_data);

		String[] mItems = getResources().getStringArray(R.array.bigtypesnosmall);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mItems);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//绑定 Adapter到控件
		sp_type.setAdapter(adapter);
		sp_type.setSelection(6, true);
		sp_type.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				selectItem = position;
				curPageIndex=0;
				getList();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		getList();
	}

	private AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView absListView, int scrollState) {
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			// 滚到底部了
			if (totalItemCount == firstVisibleItem + visibleItemCount) {
				ll_loadMore.setVisibility(View.VISIBLE);

				if (!isLoading) {
					 getList();
				}
			}
		}
	};

	@Override
	public Map<String, String> onStart(int what) {
		switch (what) {
		case WHAT_NET_GET_SUPPLY_REMIND_SAMPLE_LIST:
		case WHAT_NET_GET_SUPPLY_REMIND_SAMPLE_ONT:
			return SkuNet.getRemindSampleList(scanSkuCodeStr, curPageIndex, PAGE_SIZE, bigTypes[selectItem]);
		}
		return null;
	}

	@Override
	public void onSuccess(int what, String result, boolean hashCache) {
		isLoading = false;
		check_stock_search_edt.setFocusableInTouchMode(true);
		curPageIndex++;
		if (ll_loadMore.isShown())
			ll_loadMore.setVisibility(View.GONE);

		ResultNet<?> net = Ex.T().getString2Cls(result, ResultNet.class);
		if (!net.isSuccess) {
			Ex.Toast(mContext).showLong(net.message);
			return;
		}
		try {
			switch (what) {
			case WHAT_NET_GET_SUPPLY_REMIND_SAMPLE_LIST:
			case WHAT_NET_GET_SUPPLY_REMIND_SAMPLE_ONT:
				String skuListStr = new JSONArray(mGson.toJson(net.data)).toString();
				List<RemindSampleBean> resultList = mGson.fromJson(skuListStr, new TypeToken<List<RemindSampleBean>>() {
				}.getType());
				if (resultList.size() < PAGE_SIZE) {
					list_sku.setOnScrollListener(null);
				}
				mList.clear();
				mList.addAll(resultList);
				adapter.notifyDataSetChanged();
				break;
			case WHAT_NET_ADD_SUPPLY_REMIND_NOTICE:
				if (net.data == null) {
					Ex.Toast(mContext).showLong(net.message);
					return;
				}
				break;
			}
		} catch (JSONException e) {
			LogUtils.logOnFile(TAG+"-解析json失败:" + ExceptionUtils.getStackTrace(e));
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onError(int what, int e, String message) {
		isLoading = false;
		Toast.makeText(this, "网络请求失败", Toast.LENGTH_LONG).show();
	}

	private void getList() {
		isLoading = true;
		startTask(HotConstants.Global.APP_URL_USER + HotConstants.SKU.GET_SUPPLY_SAMPLE_REMIND, WHAT_NET_GET_SUPPLY_REMIND_SAMPLE_LIST, true, NET_METHOD_POST, false);
		check_stock_search_edt.setFocusable(false);
		check_stock_search_edt.setFocusableInTouchMode(false);
	}

	private void getListForOne() {
		isLoading = true;
		startTask(HotConstants.Global.APP_URL_USER + HotConstants.SKU.GET_SUPPLY_SAMPLE_REMIND, WHAT_NET_GET_SUPPLY_REMIND_SAMPLE_ONT, true, NET_METHOD_POST, false);
	}

	@Override
	public void fillCode(String code) {
		if (code != null) {
			if (!SkuUtil.isWarehouse(code)) {
				curPageIndex = 0;
				ap.getsoundPool(ap.Sound_sku);
				this.scanSkuCodeStr = code;
				check_stock_search_edt.setText(scanSkuCodeStr);
				getListForOne();
			} else {
				ap.getsoundPool(ap.Sound_location);
			}
		} else {
			ap.getsoundPool(ap.Sound_error);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lv_left:
			finish();
			break;

		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
			.setTitle("是否通过补量提醒生成补样通知？")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					HotConstants.SELECTED = position;
					RemindSampleBean bean = mList.get(position);
					JSONObject data = new JSONObject();
					try {
						data.put("unitid", ap.getUnitId());
						data.put("qty", bean.EndQty);
						data.put("skuid", bean.SKUID);
						data.put("billNo", bean.BillNo);
						startJsonTask(HotConstants.Global.APP_URL_USER + HotConstants.SKU.ADD_SUPPLY_REMIND_NOTICE, WHAT_NET_ADD_SUPPLY_REMIND_NOTICE, true, NET_METHOD_POST, data.toString(), false);
					} catch (JSONException e) {
						LogUtils.logOnFile(TAG+"-解析json失败:" + ExceptionUtils.getStackTrace(e));
						throw new RuntimeException(e);
					}
					
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

	/**
	 * 隐藏软键盘
	 */
	private void hideSoftKeyBoard(Context context, View editText) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
}
