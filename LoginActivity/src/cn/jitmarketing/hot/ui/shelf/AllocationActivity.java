package cn.jitmarketing.hot.ui.shelf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

import com.ex.lib.core.utils.Ex;
import com.ex.lib.core.utils.mgr.MgrPerference;
import com.ex.lib.ext.xutils.annotation.ViewInject;
import com.google.gson.reflect.TypeToken;

import cn.jitmarketing.hot.BaseSwipeBackAcvitiy;
import cn.jitmarketing.hot.HotConstants;
import cn.jitmarketing.hot.HotConstants.Shelf;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.adapter.AllocationListAdapter;
import cn.jitmarketing.hot.adapter.AllocationNoticeListAdapter;
import cn.jitmarketing.hot.entity.AllocationBean;
import cn.jitmarketing.hot.entity.AllocationNoticeBean;
import cn.jitmarketing.hot.entity.ResultNet;
import cn.jitmarketing.hot.net.WarehouseNet;
import cn.jitmarketing.hot.util.ExitAppUtils;
import cn.jitmarketing.hot.util.LogUtils;
import cn.jitmarketing.hot.view.ClearEditText;
import cn.jitmarketing.hot.view.TitleWidget;

public class AllocationActivity extends BaseSwipeBackAcvitiy implements OnClickListener, OnItemClickListener {

	@ViewInject(R.id.this_allocationnotice_title)
	TitleWidget allocation_title;
	@ViewInject(R.id.allocationnoticesearch_edit)
	ClearEditText searchEdt;
	@ViewInject(R.id.allocationnoticesearch_list)
	ListView alloction_list;
	private static final int WHAT_NET_GET_ALLOCATION_LIST = 0x10;
	private static final int TEMP_DELETE = 0x11;

	private List<AllocationNoticeBean> allocationNoticeList;
	private List<AllocationNoticeBean> searchList = new ArrayList<AllocationNoticeBean>();
	private AllocationNoticeListAdapter adapter;
	public static boolean allocation = false;
	private String allocationOrderID;

	@Override
	protected void onResume() {
		super.onResume();
		if (allocation) {
			allocation = false;
			startTask(HotConstants.Global.APP_URL_USER + Shelf.ALLOCATION_NOTICE, WHAT_NET_GET_ALLOCATION_LIST, NET_METHOD_POST, false);
		}
		// 日志操作
		LogUtils.logOnFile("进入->调拨通知单");
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
	}

	@Override
	protected void exInitAfter() {
		super.exInitAfter();
	}

	@Override
	protected void exInitBundle() {
		initIble(this, this);
	}

	@Override
	protected int exInitLayout() {
		return R.layout.activity_allocationnotice;
	}

	@Override
	protected void exInitView() {
		allocation = false;
		///allocation_title.setOnLeftClickListner(this);
		///allocation_title.setOnRightClickListner(this);
		alloction_list.setOnItemClickListener(this);
		searchEdt.addTextChangedListener(textWatcher);
		searchEdt.setOnEditorActionListener(onEditorActionListener);
		startTask(HotConstants.Global.APP_URL_USER + Shelf.ALLOCATION_NOTICE, WHAT_NET_GET_ALLOCATION_LIST, NET_METHOD_POST, false);
	}

	@Override
	public void onSuccess(int what, String result, boolean hashCache) {
		ResultNet<?> net = Ex.T().getString2Cls(result, ResultNet.class);
		if (!net.isSuccess) {
			Ex.Toast(mContext).showLong(net.message);
			return;
		}
		switch (what) {
		case WHAT_NET_GET_ALLOCATION_LIST:
			String str = mGson.toJson(net.data);
			
			try {
				JSONObject jsonObject = new JSONObject(str);
				allocationNoticeList = mGson.fromJson(jsonObject.getString("list"), new TypeToken<List<AllocationNoticeBean>>() {
				}.getType());
				searchList.clear();
				searchList.addAll(allocationNoticeList);
				adapter = new AllocationNoticeListAdapter(this, allocationNoticeList);
				alloction_list.setAdapter(adapter);
			} catch (JSONException e) {
				
			}
			
			break;
		case TEMP_DELETE:
			startTask(HotConstants.Global.APP_URL_USER + Shelf.ALLOCATION_NOTICE, WHAT_NET_GET_ALLOCATION_LIST, NET_METHOD_POST, false);
			break;
		}
	}

	public void delete(String allocationOrderID) {
		this.allocationOrderID = allocationOrderID;
		startTask(HotConstants.Global.APP_URL_USER + Shelf.ALLOCATION_TEMP_DELETE, TEMP_DELETE, NET_METHOD_POST, false);
	}

	@Override
	public Map<String, String> onStart(int what) {
		switch (what) {
		case WHAT_NET_GET_ALLOCATION_LIST:
			return WarehouseNet.allocationNoticeParams(MgrPerference.getInstance(this).getString(HotConstants.Unit.UNIT_ID),"",0,1);
		case TEMP_DELETE:
			return WarehouseNet.tempDelete(allocationOrderID);
		}
		return null;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Bundle bundle = new Bundle();
		bundle.putString("AllocationOrderID", allocationNoticeList.get(position).allocationOrderCode);
		bundle.putString("AllocationBatchNo", allocationNoticeList.get(position).allocationBatchNo);
		bundle.putString("ToUnitName", allocationNoticeList.get(position).toUnitName);
		bundle.putString("StartDateTime", allocationNoticeList.get(position).startDateTime);
		bundle.putString("EndDateTime", allocationNoticeList.get(position).endDateTime);
		
		bundle.putInt("Qty", allocationNoticeList.get(position).qty);
		bundle.putInt("LeftQty",allocationNoticeList.get(position).leftQty);
		bundle.putString("CreateTime", allocationNoticeList.get(position).createTime);
		if (allocationNoticeList.get(position).Status == 0) {// 暂存的
			// 日志操作
			LogUtils.logOnFile("调拨单->暂存->单号：" + allocationNoticeList.get(position).allocationOrderCode);
			Ex.Activity(mActivity).start(AllocationCreateActivity.class, bundle);
			/*LogUtils.logOnFile("调拨单->创建调拨单");
			Intent intent = new Intent();
			intent.setClass(this, AllocationCreateActivity.class);
			startActivity(intent);*/
		} else {
			LogUtils.logOnFile("调拨单->调拨记录->单号：" + allocationNoticeList.get(position).allocationOrderCode);
			Ex.Activity(mActivity).start(AllocationCreateActivity.class, bundle);
			LogUtils.logOnFile("调拨单->创建调拨单");
			Intent intent = new Intent();
			intent.setClass(this, AllocationCreateActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lv_left:
			finish();
			break;
		case R.id.lv_right:
			// 日志操作
			LogUtils.logOnFile("调拨单->创建调拨单");
			Intent intent = new Intent();
			intent.setClass(this, AllocationCreateActivity.class);
			startActivity(intent);
			break;
		}
	}

	/**
	 * 搜索框文字监听器
	 */
	TextWatcher textWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.equals("")) {
				allocationNoticeList.clear();
				allocationNoticeList.addAll(searchList);
				adapter.notifyDataSetChanged();
			} else {
				allocationNoticeList.clear();
				for (int i = 0; i < searchList.size(); i++) {
					if (searchList.get(i).allocationBatchNo.contains(s.toString())) {
						allocationNoticeList.add(searchList.get(i));
					}
				}
				adapter.notifyDataSetChanged();
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	};

	OnEditorActionListener onEditorActionListener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
				// 先隐藏键盘
				hideSoftKeyBoard(mActivity, searchEdt);
			}
			return true;
		}
	};

	/**
	 * 隐藏软键盘
	 */
	public void hideSoftKeyBoard(Context context, View editText) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
}