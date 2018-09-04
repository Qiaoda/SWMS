package cn.jitmarketing.hot.ui.shelf;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.ex.lib.core.utils.Ex;
import com.ex.lib.ext.xutils.annotation.ViewInject;
import com.example.scandemo.BaseSwipeOperationActivity;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import cn.jitmarketing.hot.HotApplication;
import cn.jitmarketing.hot.HotConstants;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.adapter.SignOrderNumberListAdapter;
import cn.jitmarketing.hot.entity.ReceiveSheet;
import cn.jitmarketing.hot.entity.ResultNet;
import cn.jitmarketing.hot.util.ExitAppUtils;
import cn.jitmarketing.hot.util.LogUtils;
import cn.jitmarketing.hot.view.BaseDialog;
import cn.jitmarketing.hot.view.ClearEditText;
import cn.jitmarketing.hot.view.TitleWidget;

/**
 * 签收扫码界面
 */
public class SignActivity extends BaseSwipeOperationActivity implements OnClickListener{

	/**
	 * 获取在途单号记录
	 */
	private static final int WHAT_IN_STOCK_SIGN_NET = 0x01;
	
	/**确定签收*/
	private static final int WHAT_STOCK_SIGN_NET = 0x02;

	@ViewInject(R.id.and_title)
	private TitleWidget and_title;
	@ViewInject(R.id.and_list)
	private ListView listView;
	@ViewInject(R.id.tv_confirm)
	private TextView tv_confirm; // 确定
	
	private SignOrderNumberListAdapter listAdapter;
	private List<String> newList = new ArrayList<>();
	private List<String> receiveSheetList = new ArrayList<>();
	private List<String> disOrderList = new ArrayList<>();
	private List<String> orderNoList = new ArrayList<>();

	private HotApplication ap;
	private boolean canReceive;
	private BaseDialog addDialog;
	private ClearEditText dialogOrder;
	private TextView dialogSure;
	private TextView dialogCancel;
	// 防止双击
	private long lastClickTime = 0;


	@Override
	protected void exInitBundle() {
		initIble(this, this);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*if(getIntent() != null){
			String ddCode = getIntent().getStringExtra("ddCode");
			if(StringUtils.isNoneBlank(ddCode)){
				newList.add(ddCode);
				listAdapter.notifyDataSetChanged();
			}
		}*/
		
		ExitAppUtils.getInstance().addActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ExitAppUtils.getInstance().delActivity(this);
	}

	@Override
	protected int exInitLayout() {
		return R.layout.activity_sign;
	}

	@Override
	protected String[] exInitReceiver() {
		return super.exInitReceiver();
	}

	@Override
	protected void exInitView() {
		ap = (HotApplication) getApplication();
		and_title.setOnLeftClickListner(this);
		and_title.setOnRightClickListner(this);
		and_title.setText("入库签收");
		tv_confirm.setOnClickListener(this);
		createDialog();
		
		listAdapter = new SignOrderNumberListAdapter(getLayoutInflater(), newList, this);
		listView.setAdapter(listAdapter);
		startTask(HotConstants.Global.APP_URL_USER + HotConstants.Global.IN_STOCK_SIGN, WHAT_IN_STOCK_SIGN_NET, NET_METHOD_POST, false);
		
	}

	@Override
	public Map<String, String> onStart(int what) {
		switch (what) {
			case WHAT_IN_STOCK_SIGN_NET:
				return null;
		}
		return null;
	}

	@Override
	public void onError(int what, int e, String message) {
		// 操作日志
		LogUtils.logOnFile("入库签收失败->签收" + message);
		switch (what) {
		case WHAT_IN_STOCK_SIGN_NET:
		case WHAT_STOCK_SIGN_NET:
			Ex.Toast(this).showLong(R.string.urlError);
		
		}
	}

	@Override
	public void onSuccess(int what, String result, boolean hashCache) {
		// 操作日志
		LogUtils.logOnFile("入库签收->签收" + result);
		ResultNet<?> net = Ex.T().getString2Cls(result, ResultNet.class);
		if (!net.isSuccess) {
			Ex.Toast(mContext).showLong(net.message);
			return;
		}
		switch (what) {
			case WHAT_IN_STOCK_SIGN_NET:
				if(null == net.data){
					Ex.Toast(mActivity).showLong(net.message);
					return ;
				}
			
				try {
					JSONObject jsonObject = new JSONObject(mGson.toJson(net.data));
					String str = jsonObject.getString("List");
					List<ReceiveSheet> results = mGson.fromJson(str, new TypeToken<List<ReceiveSheet>>() {
					}.getType());
					for(ReceiveSheet receiveSheet : results){
						receiveSheetList.add(String.valueOf(receiveSheet.receiveSheetNo).toUpperCase());
						disOrderList.add(String.valueOf(receiveSheet.DisOrderNo).toUpperCase());
						orderNoList.add(String.valueOf(receiveSheet.OrderNo).toUpperCase());
					}
					break;
				} catch (JSONException e) {
					LogUtils.logOnFile("入库签收->解析json出错" + ExceptionUtils.getStackTrace(e));
				}
			case WHAT_STOCK_SIGN_NET:
				if(null == net.data){//{"Success":true,"Message":null,"Data":"签收成功!"}
					Ex.Toast(mActivity).showLong(net.message);
					newList.clear();
					listAdapter.notifyDataSetChanged();
					return ;
				}
				break;

		}
	}

	@Override
	protected void onResume() {
		canReceive = true;
		super.onResume();
		// 日志操作
		LogUtils.logOnFile("进入->签收界面");
	}

	@Override
	protected void onPause() {
		canReceive = false;
		super.onPause();
	}

	@Override
	protected void onStop() {
		canReceive = false;
		super.onStop();
	}
	
	private void back(){
		if(!newList.isEmpty()){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder
				.setTitle("此次签收操作未保存，是否放弃？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
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
			
		}else{
			finish();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lv_left:// 返回按钮
			back();
			break;
		case R.id.tv_confirm:// 确定
			// 日志操作
			LogUtils.logOnFile("签收->确定");
			if(newList.isEmpty()){
				Ex.Toast(this).showLong("提交数据为空");
				return;
			}
			JSONObject jsonObject = new JSONObject();
			String postData = null;
			try {
				JSONArray array = new JSONArray(newList);
				jsonObject.put("unitid", HotApplication.getInstance().getUnitId());
				jsonObject.put("Data", array);
				postData = jsonObject.toString();
			} catch (JSONException e) {
				LogUtils.logOnFile("签收->json 解析出错:"+ExceptionUtils.getStackTrace(e));
			}
			startJsonTask(HotConstants.Global.APP_URL_USER + HotConstants.Global.IN_STOCK_SIGN_CONFIRM, 
					WHAT_STOCK_SIGN_NET, true, NET_METHOD_POST, postData, false);
			break;
		case R.id.lv_right:
			addDialog.show();
			break;
		case R.id.dialog_sure:
			String code =  dialogOrder.getText().toString();
			if(StringUtils.isNoneBlank(code)){
				if(receiveSheetList.contains(code.toUpperCase()) || disOrderList.contains(code.toUpperCase())|| orderNoList.contains(code.toUpperCase())){
					newList.add(code.toUpperCase());
					listAdapter.notifyDataSetChanged();
					addDialog.dismiss();
					listAdapter.notifyDataSetChanged();
				}else{
					Ex.Toast(mContext).showShort("入库单号不存在");
				}
				
			}
			break;
		case R.id.dialog_cancel:
			addDialog.dismiss();
			break;
		
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		super.onActivityResult(requestCode, resultCode, data);
		
	}

	@Override
	public void onBackPressed() {
		back();
	}

	@Override
	public void fillCode(String code) {
		ap.getsoundPool(ap.Sound_sku);
		String temp = code;
		/*if (addDialog.isShowing()) {
			dialogOrder.setText(code);
		}else{
			dealBarCode(code);
			listAdapter.notifyDataSetChanged();
		}*/
		if(StringUtils.isNoneBlank(code)){
			code = code.toUpperCase();
		}
		if(receiveSheetList.contains(code) || disOrderList.contains(code) || orderNoList.contains(code) ){
			dealBarCode(temp);
			listAdapter.notifyDataSetChanged();
		}else{
			Ex.Toast(mContext).showShort("入库单号不存在");
		}
		
		LogUtils.logOnFile("签收->扫码：" + code);
	}

	private void dealBarCode(String barcode) {
		if (canReceive) {
			if(!newList.contains(barcode)){
				newList.add(barcode);
			}
		}
	}

	@Override
	public void onReceiver(Intent intent) {
		
	}

	/**
	 * 手动添加弹窗
	 */
	private void createDialog() {
		View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_sign_ordernumber_add, null);
		addDialog = new BaseDialog(mActivity, view);
		addDialog.setCanceledOnTouchOutside(false);
		dialogOrder = (ClearEditText) view.findViewById(R.id.dialog_order);
		dialogSure = (TextView) view.findViewById(R.id.dialog_sure);
		dialogCancel = (TextView) view.findViewById(R.id.dialog_cancel);
		dialogSure.setOnClickListener(this);
		dialogCancel.setOnClickListener(this);
	}

}
