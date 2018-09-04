package cn.jitmarketing.hot.choupan;

import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ex.lib.core.utils.Ex;
import com.ex.lib.ext.xutils.annotation.ViewInject;
import com.example.scandemo.BaseSwipeOperationActivity;
import cn.jitmarketing.hot.HotApplication;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.util.ConstValue;
import cn.jitmarketing.hot.util.ExitAppUtils;
import cn.jitmarketing.hot.util.LogUtils;
import cn.jitmarketing.hot.util.SkuUtil;
import cn.jitmarketing.hot.view.ClearEditText;
import cn.jitmarketing.hot.view.SkuEditText;

public class ChouPanDetailAddActivity extends BaseSwipeOperationActivity implements
		OnClickListener {
	@ViewInject(R.id.hand_shelf_layout)
	LinearLayout hand_shelf_layout;
	@ViewInject(R.id.hand_shelf_location)
	SkuEditText hand_shelf_location;
	@ViewInject(R.id.hand_shelf_sku)
	ClearEditText hand_shelf_sku;
	@ViewInject(R.id.hand_shelf_count)
	ClearEditText hand_shelf_count;
	@ViewInject(R.id.hand_shelf_cancle)
	TextView hand_shelf_cancle;
	@ViewInject(R.id.hand_shelf_sure)
	TextView hand_shelf_sure;
	private HotApplication ap;
	
	private Map<String, String> allSkuMap;//所有待抽盘的Sku
	
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
		setFinishOnTouchOutside(false);
		return R.layout.activity_hand_shelf;
	}

	@Override
	public void onReceiver(Intent intent) {
	}

	@Override
	protected void exInitView() {
		ap = (HotApplication) getApplication();
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				(int) (ConstValue.SCREEN_WIDTH * 0.9),
				(int) (ConstValue.SCREEN_HEIGHT * 0.6));
		hand_shelf_layout.setLayoutParams(params);
		hand_shelf_cancle.setOnClickListener(this);
		hand_shelf_sure.setOnClickListener(this);

		allSkuMap = (Map<String, String>) getIntent().getSerializableExtra("allSkuMap");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.hand_shelf_cancle:
			//日志操作
        	LogUtils.logOnFile("~->添加->取消");
			this.finish();
			break;
		case R.id.hand_shelf_sure:

			if (!hand_shelf_location.invalid()) {
				Ex.Toast(mContext).show(
						getResources().getString(R.string.validat_sku));
				return;
			}
			String location = hand_shelf_location.getText(this).toString()
					.toUpperCase();
			String sku = hand_shelf_sku.getText().toString().toUpperCase();
			if(allSkuMap.get(sku) == null){
				Ex.Toast(mActivity).show("SKU["+sku+"]不在抽盘列表中");
				return;
			}
			String count = hand_shelf_count.getText().toString();
			if (location.equals("") || sku.equals("") || count.equals("")
					|| count.equals("0")) {
				Ex.Toast(mActivity).show("输入框内容不要为空或0");
			}
			//日志操作
        	LogUtils.logOnFile("~->添加->SKU:"+sku+"数量："+count+"库位："+location);
			// 抽盘-明细-添加
			Intent intent = new Intent();
			intent.putExtra("location", location);
			intent.putExtra("sku", sku);
			intent.putExtra("count", count);
			setResult(RESULT_OK, intent);
			this.finish();
			break;
		}
	}

	@Override
	public void fillCode(String code) {
		if (!SkuUtil.isWarehouse(code)) {
            
			hand_shelf_sku.setText(code);
			ap.getsoundPool(ap.Sound_sku);
		} else {
			ap.getsoundPool(ap.Sound_location);
			hand_shelf_location.setText(code);
		}
		//日志操作
    	LogUtils.logOnFile("~->添加->扫码："+code);
	}
	
}
