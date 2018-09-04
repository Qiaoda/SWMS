package cn.jitmarketing.hot.choupan;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.ex.lib.ext.xutils.annotation.ViewInject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.jitmarketing.hot.BaseSwipeBackAcvitiy;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.adapter.ShelfDetailAdapter;
import cn.jitmarketing.hot.entity.InStockDetail;
import cn.jitmarketing.hot.ui.shelf.MainShelfAndActivity;
import cn.jitmarketing.hot.util.ChangeValueUtil;
import cn.jitmarketing.hot.util.ExitAppUtils;
import cn.jitmarketing.hot.util.LogUtils;
import cn.jitmarketing.hot.view.TitleWidget;

/**
 * 抽盘明细
 * @author liushang1
 *
 */
public class ChouPanDetailActivity extends BaseSwipeBackAcvitiy implements
        OnClickListener {
    @ViewInject(R.id.shelf_detail_title)
    TitleWidget shelf_detail_title;
    ListView detail_lv;

    private ArrayList<InStockDetail> detailList; // 明细list
    private Map<String, String> allSkuMap; // 所有带抽盘的sku

    private static final int FOR_RESULT_HAND = 0x12;
    ShelfDetailAdapter adapter;

    private boolean isNoValue;
    
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
        return R.layout.activity_shelf_detail;
    }

    @Override
    protected void exInitView() {
        shelf_detail_title.setText("明细");
        shelf_detail_title.setOnLeftClickListner(this);
        shelf_detail_title.setOnRightClickListner(this);
        detail_lv = (ListView) findViewById(R.id.detail_lv);

        detailList = (ArrayList<InStockDetail>) getIntent().getSerializableExtra("skuList");
        allSkuMap = (Map<String, String>) getIntent().getSerializableExtra("allSkuMap");

        adapter = new ShelfDetailAdapter(this, getLayoutInflater(), ChangeValueUtil.value(detailList));
        detail_lv.setAdapter(adapter);
        if (detailList.size() == 0) {//传入的时候就没有数据
            isNoValue = true;
        } else {
            isNoValue = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lv_left:
            	//日志操作
            	LogUtils.logOnFile("明细->返回");
                Intent intent1 = new Intent(ChouPanDetailActivity.this,
                        ChouPanActivity.class);
                //			intent1.putExtra("wList", detailList);
                intent1.putExtra("wList", ChangeValueUtil.reverseValue(adapter.getWareList()));
                intent1.putExtra("isNoValue", isNoValue);
                setResult(RESULT_OK, intent1);
                finish();
                break;
            case R.id.lv_right://添加
            	//日志操作
            	LogUtils.logOnFile("明细->添加");
                Intent intent = new Intent();
                intent.putExtra("allSkuMap", (HashMap<String, String>)allSkuMap);
                intent.setClass(ChouPanDetailActivity.this,
                        ChouPanDetailAddActivity.class);
                startActivityForResult(intent, FOR_RESULT_HAND);
                //关闭扫描服务
                //			if(serviceConnection!=null){
                //				unbindService(serviceConnection);
                //				stopService(new Intent(this,ScanService.class));
                //			}
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == FOR_RESULT_HAND) {
            String location = data.getStringExtra("location");
            String sku = data.getStringExtra("sku");
            String count = data.getStringExtra("count");
            InStockDetail ssb = new InStockDetail(sku, (float) (Integer.valueOf(count).intValue()),
                    location);
            push(ssb);
            adapter = new ShelfDetailAdapter(this, getLayoutInflater(),
                    ChangeValueUtil.value(detailList));
            detail_lv.setAdapter(adapter);
        }
    }

    private void push(InStockDetail item) {
        ArrayList<InStockDetail> list = detailList;
        for (int i = 0; i < list.size(); i++) {
            InStockDetail titem = list.get(i);
            if (titem.SKUCode.equals(item.SKUCode)
                    && titem.ShelfLocationCode.equals(item.ShelfLocationCode)) {
                titem.SKUCount += item.SKUCount;
                return;
            }
        }
        list.add(item);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent1 = new Intent(ChouPanDetailActivity.this,
                    MainShelfAndActivity.class);
            //			intent1.putExtra("wList", detailList);
            intent1.putExtra("wList", ChangeValueUtil.reverseValue(adapter.getWareList()));
            intent1.putExtra("isNoValue", isNoValue);
            setResult(RESULT_OK, intent1);
            finish();
        }
        return false;
    }
}
