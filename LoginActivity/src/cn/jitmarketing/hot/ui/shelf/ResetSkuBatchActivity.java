package cn.jitmarketing.hot.ui.shelf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import cn.jitmarketing.hot.BaseSwipeBackAcvitiy;
import cn.jitmarketing.hot.HotApplication;
import cn.jitmarketing.hot.HotConstants;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.adapter.ResetSkuBatchAdapter;
import cn.jitmarketing.hot.entity.InStockDetail;
import cn.jitmarketing.hot.entity.ResultNet;
import cn.jitmarketing.hot.util.ExitAppUtils;
import cn.jitmarketing.hot.util.SaveListUtil;
import cn.jitmarketing.hot.util.SkuUtil;
import cn.jitmarketing.hot.view.SkuEditText;
import cn.jitmarketing.hot.view.TitleWidget;

import com.ex.lib.core.utils.Ex;
import com.ex.lib.ext.xutils.annotation.ViewInject;
import com.example.scandemo.BaseSwipeOperationActivity;

/**拿货*/
public class ResetSkuBatchActivity extends BaseSwipeOperationActivity implements OnClickListener {

	@ViewInject(R.id.take_goods_batch_title)
	TitleWidget take_goods_batch_title;
	@ViewInject(R.id.batch_take_goods_listview)
	ListView batch_take_goods_listview;
	@ViewInject(R.id.sku_txt)
	TextView sku_txt;
	@ViewInject(R.id.hand_btn)
	Button hand_btn;
	@ViewInject(R.id.confirm_btn)
	Button confirm_btn;
	@ViewInject(R.id.sku_shelf)
	SkuEditText sku_shelf;
	@ViewInject(R.id.scan_time_txt)
	TextView scan_time_txt;
	
	private static final int WHAT_NET_RESET_SKU_BATCH = 0x10;
	private static final int FOR_RESULT_and = 1;
	
	private boolean canRecevice = true;
	private HotApplication ap;
	private List<InStockDetail> detailList = new ArrayList<InStockDetail>();
	private String skuCode;
	private String factId;
	private int count;
	private ResetSkuBatchAdapter adapter;
	private int scanTime;
	private boolean scanSku;
	
	@Override
	protected void onResume() {
		super.onResume();
		hideSoftKeyBoard(this, sku_shelf);
		canRecevice = true;
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
	protected void onStop() {
		super.onStop();
		canRecevice = false;
	};
	
	@Override
	protected void onPause() {
		super.onPause();
		canRecevice = false;
	};
	
	@Override
	protected void exInitAfter() {
		ap = (HotApplication) getApplication();
	}

	@Override
	protected void exInitBundle() {
		initIble(this, this);
	}

	@Override
	protected int exInitLayout() {
		return R.layout.activity_take_goods_batch;
	}
	

	protected void exInitView() {
		sku_shelf.setEnable(false);
		take_goods_batch_title.setText("批量复位");
		take_goods_batch_title.setOnLeftClickListner(this);
		confirm_btn.setOnClickListener(this);
		hand_btn.setOnClickListener(this);
		skuCode = getIntent().getExtras().getString("skuCode");
		factId = getIntent().getExtras().getString("factId");
		count = getIntent().getExtras().getInt("count");
		adapter = new ResetSkuBatchAdapter(this, detailList);
		batch_take_goods_listview.setAdapter(adapter);
		sku_txt.setText(skuCode);
	}
	/**
	 * 先扫描sku码,再扫描库位，完成一次提交
	 */
	@Override
	public void onReceiver(Intent intent) {
//		if(canRecevice){
//			byte[] code = intent.getByteArrayExtra("barocode");
//			int codelen = intent.getIntExtra("length", 0);
//			String barcode = new String(code, 0, codelen).toUpperCase().trim();
//			dealBarCode(barcode);
//		}
	}
	
//	@Override
//	public void fillCode(String code) {
//		dealBarCode(code);
//	}
	
	private void dealBarCode(String barcode) {
		if(sku_shelf.getText(this).equals("")) {
			if(SkuUtil.isWarehouse(barcode) && !scanSku) {
				ap.getsoundPool(ap.Sound_error);
				Ex.Toast(mContext).showLong("先扫描商品编码");
				return;
			}
		}
		if(!SkuUtil.isWarehouse(barcode)) {
			if(!skuCode.equals(barcode)) {
				ap.getsoundPool(ap.Sound_error);
				sku_txt.setText(barcode);
				Ex.Toast(mContext).showLong("非本次操作商品");
				return;
			}
			ap.getsoundPool(ap.Sound_sku);
			scanTime++;
			scan_time_txt.setText(String.valueOf(scanTime));
			sku_shelf.setText("");
			scanSku = true;
		} else {
			ap.getsoundPool(ap.Sound_location);
			sku_shelf.setText(barcode);
			boolean isExist = false;
			for(int i=0; i<detailList.size(); i++) {
				if(detailList.get(i).ShelfLocationCode != null && detailList.get(i).ShelfLocationCode.equals(barcode)) {
					detailList.get(i).SKUCount += scanTime;
					isExist = true;
				}
			} 
			if(!isExist) {
				InStockDetail detail = new InStockDetail(barcode, scanTime, barcode);
				detailList.add(detail);
			}
			adapter.notifyDataSetChanged();
			scanTime = 0;
			scanSku = false;
		}
//		if(SkuUtil.isWarehouse(barcode)) {//先扫描库位码
//			ap.getsoundPool(ap.Sound_location);
//			sku_shelf.setText(barcode);
//			sku_txt.setText("");
//			currentShelfCode = barcode;
//		} else {
//			if(!skuCode.equals(barcode)) {
//				ap.getsoundPool(ap.Sound_error);
//				sku_txt.setText(barcode);
//				Ex.Toast(mContext).showLong("非本次操作商品");
//				return;
//			}
//			ap.getsoundPool(ap.Sound_sku);
//			sku_txt.setText(barcode);
//			boolean isExist = false;
//			for(int i =0; i<detailList.size(); i++) {
//				if(detailList.get(i).ShelfLocationCode.equals(currentShelfCode)) {
//					detailList.get(i).SKUCount +=1;
//					isExist = true;
//				}
//			} 
//			if(!isExist) {
//				InStockDetail detail = new InStockDetail(barcode, 1, currentShelfCode);
//				detailList.add(detail);
//			}
//			adapter.notifyDataSetChanged();
//		}
	}

	@Override
	public void onError(int what, int e, String message) {
          switch (what) {
		case WHAT_NET_RESET_SKU_BATCH:
			Ex.Toast(this).show("你的网速不太好，提交失败，请稍后重试");
			break;
		}
	}

	@Override
	public Map<String, String> onStart(int what) {
		return null;
	}

	@Override
	public void onSuccess(int what, String result, boolean hashCache) {
		ResultNet<?> net = Ex.T().getString2Cls(result, ResultNet.class);
		if (!net.isSuccess) {
			if(what == WHAT_NET_RESET_SKU_BATCH) {
			}
			Ex.Toast(mContext).showLong(net.message);
			return;
		}
		switch (what) {
		case WHAT_NET_RESET_SKU_BATCH:
			Ex.Toast(mContext).showLong("批量复位成功");
			ResetSkuActivity.isRefreshList = true;
			finish();
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lv_left:
			this.finish();
			break;
		case R.id.confirm_btn:
			int countTotal = 0;
			for(InStockDetail detail : detailList) {
				countTotal += detail.SKUCount;
			}
			if(count < countTotal) {
				Ex.Toast(mContext).showLong("复位数量多于申请数量!" + "申请数量是" + count + ",复位数量是" + ((int)countTotal));
				return;
			}
			startJsonTask(HotConstants.Global.APP_URL_USER
					+ HotConstants.SKU.ResetBatchSample, WHAT_NET_RESET_SKU_BATCH, true,
					NET_METHOD_POST, SaveListUtil.takeGoodsBatch(detailList, factId), false);
			break;
		case R.id.hand_btn:
			Intent intent = new Intent(this, HandResetSkuBatchActivity.class);
			startActivityForResult(intent, FOR_RESULT_and);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == FOR_RESULT_and) {
			Bundle bd = data.getExtras();
			String shelf = bd.getString("shelfCode");
			int count = bd.getInt("count");
			boolean isExist = false;
			for(int i =0; i<detailList.size(); i++) {
				if(detailList.get(i).ShelfLocationCode.equals(shelf)) {
					detailList.get(i).SKUCount += count;
					isExist = true;
				}
			} 
			if(!isExist) {
				InStockDetail detail = new InStockDetail(skuCode, count, shelf);
				detailList.add(detail);
			}
			adapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * 隐藏软键盘
	 */
	public void hideSoftKeyBoard(Context context, View editText) {
		InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	@Override
	public void fillCode(String code) {
		// TODO Auto-generated method stub
		dealBarCode(code);
	}
}
