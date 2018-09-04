package cn.jitmarketing.hot.choupan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.ex.lib.core.utils.Ex;
import com.ex.lib.ext.xutils.annotation.ViewInject;
import com.example.scandemo.BaseSwipeOperationActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import u.aly.ba;

import cn.jitmarketing.hot.HotApplication;
import cn.jitmarketing.hot.HotConstants;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.adapter.SkuShelvesOneListAdapter;
import cn.jitmarketing.hot.entity.InStockDetail;
import cn.jitmarketing.hot.entity.ResultNet;
import cn.jitmarketing.hot.util.ExitAppUtils;
import cn.jitmarketing.hot.util.LogUtils;
import cn.jitmarketing.hot.util.SaveListUtil;
import cn.jitmarketing.hot.util.SkuUtil;
import cn.jitmarketing.hot.view.BaseDialog;
import cn.jitmarketing.hot.view.ClearEditText;
import cn.jitmarketing.hot.view.SkuEditText;
import cn.jitmarketing.hot.view.TitleWidget;

/**
 * 抽盘界面
 */
public class ChouPanActivity extends BaseSwipeOperationActivity implements OnClickListener{

	/**
	 * 确定
	 */
	private static final int WHAT_NET_CHOUPAN_CONFIRM = 0x10;
	/**
	 * 所有待抽盘的sku
	 */
	private static final int WHAT_NET_CHOUPAN_ALL_SKU = 0x09;

	private static final int FOR_RESULT_DETAIL = 0x12;

	@ViewInject(R.id.and_title)
	private TitleWidget and_title;
	@ViewInject(R.id.and_list)
	private ListView listView;
	@ViewInject(R.id.tv_confirm)
	private TextView tv_confirm; // 确定
	@ViewInject(R.id.tv_clear)
	private TextView tv_clear; // 清楚所有数据

	private SkuEditText sku_shelf; // 库位
	private TextView sku_really_all_count; // 已扫数量
	private TextView sku_scan_detail; // 明细

	private String skuShelfCode; // 库位码（显示在库位位置）
	private ArrayList<String> stringList; // 存储扫码记录

	private HotApplication ap;
	private SkuShelvesOneListAdapter listAdapter;
	private ArrayList<InStockDetail> confirmList = new ArrayList<InStockDetail>();
	private ArrayList<InStockDetail> skuList = new ArrayList<InStockDetail>();
	private ArrayList<InStockDetail> newList = new ArrayList<InStockDetail>();// 显示三条数据的
																				// 集合
	private Map<String, String> allSkuMap = new HashMap<>();
	private boolean canReceive;

	// 防止双击
	private long lastClickTime = 0;

	private BaseDialog addDialog;
	private ClearEditText dialogSku;
	private ClearEditText dialogCount;
	private SkuEditText dialogStock;
	private TextView dialogSure;
	private TextView dialogCancel;
	
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
	protected void exInitBundle() {
		initIble(this, this);
	}

	@Override
	protected int exInitLayout() {
		return R.layout.activity_choupan;
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
		and_title.setText("抽盘");
		tv_confirm.setOnClickListener(this);
		tv_clear.setOnClickListener(this);
		stringList = new ArrayList<String>();

		listAdapter = new SkuShelvesOneListAdapter(getLayoutInflater(), newList);

		View footView = getLayoutInflater().inflate(R.layout.sku_shelves_foot, null);
		sku_shelf = (SkuEditText) footView.findViewById(R.id.sku_shelf);
		sku_shelf.stopEdit();
		sku_really_all_count = (TextView) footView.findViewById(R.id.sku_really_all_count);
		sku_scan_detail = (TextView) footView.findViewById(R.id.sku_scan_detail);
		sku_scan_detail.setOnClickListener(this);

		View headView = getLayoutInflater().inflate(R.layout.sku_shelves_item_layout, null);
		listView.addHeaderView(headView);
		listView.addFooterView(footView);
		listView.setAdapter(listAdapter);

		createDialog();
		
		requestAllSku();

	}
	
	private void requestAllSku(){
		//获取所有待抽盘的sku
		JSONObject object = new JSONObject();
		try {
			object.put("unitid", HotApplication.getInstance().getUnitId());
		} catch (JSONException e) {
			LogUtils.logOnFile("获取所有抽盘SKU->"+ExceptionUtils.getStackTrace(e));
		}
		startJsonTask(HotConstants.Global.APP_URL_USER + HotConstants.RandomCheck.CHOUPAN_ALL_SKU, WHAT_NET_CHOUPAN_ALL_SKU, true, NET_METHOD_POST, object.toString(), false);
	}

	@Override
	public Map<String, String> onStart(int what) {
		switch (what) {
		case WHAT_NET_CHOUPAN_CONFIRM:
			return null;
		}
		return null;
	}

	@Override
	public void onError(int what, int e, String message) {
		// 操作日志
		LogUtils.logOnFile("抽盘->" + message);
		switch (what) {
		case WHAT_NET_CHOUPAN_CONFIRM:
			Ex.Toast(this).showLong("提交出错,请重试");
			break;
		case WHAT_NET_CHOUPAN_ALL_SKU:
			Ex.Toast(this).showLong("获取SKU失败");
			break;
		}
	}

	@Override
	public void onSuccess(int what, String result, boolean hashCache) {
		// 操作日志
		LogUtils.logOnFile("抽盘->" + result);
		ResultNet<?> net = Ex.T().getString2Cls(result, ResultNet.class);
		if (!net.isSuccess) {
			Ex.Toast(mContext).showLong(net.message);
			return;
		}
		switch (what) {
		case WHAT_NET_CHOUPAN_CONFIRM:
			if (null == net.data) {
				Ex.Toast(mActivity).showLong(net.message);
				return;
			}
			if((Boolean) net.data){
				Ex.Toast(mContext).showLong("提交成功");
				//提交成功,清除页面保存数据
				cacheClear();
			}else{
				Ex.Toast(mContext).showLong("提交失败");
			}
			break;
		case WHAT_NET_CHOUPAN_ALL_SKU://所有待抽盘的sku
			List<String> allSkuList = (List<String>) net.data;//mGson.fromJson((String)net.data, new TypeToken<List<String>>(){}.getType());
			if(allSkuList.size() > 0){
				for(String sku : allSkuList){
					allSkuMap.put(sku, sku);
				}
			}
			break;
		}
	}

	@Override
	protected void onResume() {
		canReceive = true;
		if(newList.size() == 0 && confirmList.size() == 0){
			List<InStockDetail> tempNewList = (ArrayList<InStockDetail>) readLocalData("newList");
			confirmList = (ArrayList<InStockDetail>) readLocalData("confirmList");
			
			newList.addAll(tempNewList);
			if(newList.size() > 0){
				sku_really_all_count.setText("" + SkuUtil.getnewSkuCount(confirmList));
				sku_shelf.setText(tempNewList.get(0).ShelfLocationCode==null?"":tempNewList.get(0).ShelfLocationCode);
				listAdapter.notifyDataSetChanged();
			}
		}
		
		super.onResume();
		// 日志操作
		LogUtils.logOnFile("进入->上架界面");
	}

	@Override
	protected void onPause() {
		canReceive = false;
		//保存数据
		cacheData();
		super.onPause();
	}

	@Override
	protected void onStop() {
		canReceive = false;
		//保存数据
		cacheData();
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lv_left:// 返回按钮
			//缓存页面数据
			cacheData();
			finish();
			break;
		case R.id.sku_scan_detail:// 明细
			// 日志操作
			LogUtils.logOnFile("抽盘->明细");
			Intent intent = new Intent(mActivity, ChouPanDetailActivity.class);
			intent.putExtra("skuList", confirmList == null ? new ArrayList<InStockDetail>() : confirmList);
			intent.putExtra("allSkuMap", (HashMap<String, String>)allSkuMap);
			startActivityForResult(intent, FOR_RESULT_DETAIL);
			break;
		case R.id.tv_confirm:// 确定
			Log.i("TAG", newList.toString());
			// 日志操作
			LogUtils.logOnFile("抽盘->确定");
			
			new AlertDialog.Builder(this).setTitle(R.string.noteTitle).setMessage("确定提交抽盘数据?").setNegativeButton(R.string.cancelTitle, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 日志操作
					LogUtils.logOnFile("抽盘->弹窗->取消");
					dialog.dismiss();
				}
			}).setPositiveButton(R.string.sureTitle, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					// 日志操作
					LogUtils.logOnFile("抽盘->弹窗->确定");
					// 防止双击
					long currentTime = System.currentTimeMillis();
					if (currentTime - lastClickTime > 1000) {
						lastClickTime = currentTime;
						// 操作事件
						if (confirmList.size() > 0) {
							startJsonTask(HotConstants.Global.APP_URL_USER + HotConstants.RandomCheck.CHOUPAN_CONFIRM, WHAT_NET_CHOUPAN_CONFIRM, true, NET_METHOD_POST, SaveListUtil.chouPanConfirm(confirmList), false);
						} else {
							Ex.Toast(mActivity).show("无数据，不能提交");
						}
					}

				}
			}).show();
			break;
		case R.id.tv_clear:
			//清楚所有未提交数据
			boolean clear = cacheClear();
			if(clear)
				Ex.Toast(mActivity).show("数据已清除");
			else
				Ex.Toast(mActivity).show("清除失败");
			break;
		case R.id.lv_right:
			addDialog.show();
			break;
		case R.id.dialog_sure:
			handAddSku();
			break;
		case R.id.dialog_cancel:
			clearDialogDate();
			addDialog.dismiss();
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == FOR_RESULT_DETAIL) {
			confirmList.clear();
			List<InStockDetail> result = (List<InStockDetail>) data.getSerializableExtra("wList");
			confirmList.addAll(result);
			sku_really_all_count.setText("" + SkuUtil.getnewSkuCount(confirmList));
			boolean isNoValue = data.getBooleanExtra("isNoValue", false);
			if (!isNoValue) {
				ArrayList<InStockDetail> ddList = new ArrayList<InStockDetail>();// 显示的列表
				for (InStockDetail dd : confirmList) {
					if (dd.ShelfLocationCode.equals(sku_shelf.getText(this))) {
						ddList.add(dd);
					}
				}
				if(ddList.size() > 0)
					SkuUtil.skunewShowList(ddList, newList);
				listAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onBackPressed() {
		//保存数据
		cacheData();
		super.onBackPressed();
	}

	@Override
	public void fillCode(String code) {
		if (!addDialog.isShowing()) {
			dealBarCode(code);
		}
		LogUtils.logOnFile("抽盘->扫码：" + code);
	}

	private boolean dealBarCode(String barcode) {
		if (canReceive) {
			skuShelfCode = sku_shelf.getText(this);
			// 如果扫的是商品
			if ((!SkuUtil.isWarehouse(barcode))) {
				if(allSkuMap.get(barcode) == null){
					Ex.Toast(mActivity).show("SKU["+barcode+"]不在抽盘列表中");
					return false;
				}
				ap.getsoundPool(ap.Sound_sku);
				if (skuShelfCode.length() > 0) {
					skuList.clear();
					sku_shelf.setText("");
				}

				stringList.add(barcode);
				InStockDetail ssb = new InStockDetail(barcode, 1);
				SkuUtil.newcheck(skuList, ssb);
				SkuUtil.skunewShowList(skuList, newList);
				listAdapter.notifyDataSetChanged();
			}
			// 如果扫的是库位
			else if (SkuUtil.isWarehouse(barcode) && stringList.size() > 0) {
				ap.getsoundPool(ap.Sound_location);
				sku_shelf.setText(barcode);
				// 遍历寻找库位是否存在
				for (int i = 0; i < skuList.size(); i++) {
					skuList.get(i).ShelfLocationCode = barcode;
					push(skuList.get(i));
				}
				sku_really_all_count.setText(SkuUtil.getnewSkuCount(skuList) + "");
				stringList.clear();
			}
		}
		return true;
	}
	
	private void push(InStockDetail item) {
		for (int i = 0; i < confirmList.size(); i++) {
			InStockDetail tItem = confirmList.get(i);
			if (tItem.SKUCode.equals(item.SKUCode) && tItem.ShelfLocationCode.equals(item.ShelfLocationCode)) {
				tItem.SKUCount += item.SKUCount;
				return;
			}
		}
		confirmList.add(item);
	}

	@Override
	public void onReceiver(Intent intent) {
		
	}

	/**
	 * 手动添加弹窗
	 */
	private void createDialog() {
		View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_hand_add, null);
		addDialog = new BaseDialog(mActivity, view);
		addDialog.setCanceledOnTouchOutside(false);
		dialogSku = (ClearEditText) view.findViewById(R.id.dialog_sku);
		dialogCount = (ClearEditText) view.findViewById(R.id.dialog_count);
		dialogStock = (SkuEditText) view.findViewById(R.id.dialog_stock);
		dialogSure = (TextView) view.findViewById(R.id.dialog_sure);
		dialogCancel = (TextView) view.findViewById(R.id.dialog_cancel);
		dialogSure.setOnClickListener(this);
		dialogCancel.setOnClickListener(this);
	}

	/**
	 * 手动添加商品
	 */
	private void handAddSku() {
		// 输入框空数据验证
		if (TextUtils.isEmpty(dialogSku.getText().toString())) {
			Toast.makeText(this, "请输入SKU", Toast.LENGTH_LONG).show();
			return;
		}
		if (TextUtils.isEmpty(dialogCount.getText().toString())) {
			Toast.makeText(this, "请输入数量", Toast.LENGTH_LONG).show();
			return;
		}
		if (TextUtils.isEmpty(dialogStock.getText(mActivity))) {
			Toast.makeText(this, "请输入库位码", Toast.LENGTH_LONG).show();
			return;
		}
		
		
		// 添加数据到列表
		boolean locationResult = true;
		boolean skuResult = true;
		for (int i = 0; i < Integer.valueOf(dialogCount.getText().toString()); i++) {
			skuResult = dealBarCode(dialogSku.getText().toString().toUpperCase());
		}
		locationResult = dealBarCode(dialogStock.getText(mActivity));
		// 清除数据
		if(locationResult && skuResult){
			clearDialogDate();
			addDialog.dismiss();
		}
	}

	/**
	 * 清除弹框数据
	 */
	private void clearDialogDate() {
		dialogSku.setText("");
		dialogCount.setText("");
		dialogStock.setText("");
	}
	
	/**
	 * 缓存数据
	 */
	private void cacheData(){
		writeData2Local(newList, "newList");
		writeData2Local(confirmList, "confirmList");
	}
	
	/**
	 * 清除缓存
	 */
	private boolean cacheClear(){
		sku_shelf.setText("");
		sku_really_all_count.setText("");
		skuList.clear();
		newList.clear();
		confirmList.clear();
		listAdapter.notifyDataSetChanged();
		if(deleteLocalData("newList") && deleteLocalData("confirmList"))
			return true;
		else
			return false;
	}
	
	/**
	 * 缓存数据到本地
	 * @param list
	 * @param key
	 */
	private void writeData2Local(List<InStockDetail> list, String key){
		if(list == null || list.size() == 0)
			return;
		
		LogUtils.logOnFile("抽盘->保存抽盘数据：" + key);
		File file = new File(mContext.getFilesDir(), key+".cache");
		
		try {
			file.delete();
			file.createNewFile();
			Gson gson = new Gson();
			String json = gson.toJson(list);
			FileUtils.writeStringToFile(file, json, "utf-8");
		} catch (Exception e) {
			LogUtils.logOnFile("抽盘->保存抽盘数据：" + ExceptionUtils.getStackTrace(e));
		}
			
	}
	
	/**
	 * 读取本地缓存数据
	 * @param key
	 * @return
	 */
	private List<InStockDetail> readLocalData(String key){
		LogUtils.logOnFile("抽盘->读取抽盘数据：" + key);
		File file = new File(mContext.getFilesDir(), key+".cache");
		if(file.exists()){
			Gson gson = new Gson();
			try {
				String json = FileUtils.readFileToString(file, "utf-8");
				List<InStockDetail> fromJson = gson.fromJson(json, new TypeToken<List<InStockDetail>>(){}.getType());
				return fromJson;
			} catch (Exception e) {
				LogUtils.logOnFile("抽盘->读取抽盘数据：" + ExceptionUtils.getStackTrace(e));
			}
		}
		return new ArrayList<>();
	}
	
	/**
	 * 删除本地缓存数据
	 * @param key
	 */
	private boolean deleteLocalData(String key){
		LogUtils.logOnFile("抽盘->删除抽盘数据：" + key);
		File file = new File(mContext.getFilesDir(), key+".cache");
		if(file.exists()){
			return file.delete();
		}
		return true;
	}
}
