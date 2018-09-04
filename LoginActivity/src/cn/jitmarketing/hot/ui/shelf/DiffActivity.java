package cn.jitmarketing.hot.ui.shelf;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.ex.lib.core.utils.Ex;
import com.ex.lib.ext.xutils.annotation.ViewInject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import cn.jitmarketing.hot.BaseSwipeBackAcvitiy;
import cn.jitmarketing.hot.HotApplication;
import cn.jitmarketing.hot.HotConstants;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.adapter.DiffListAdapter;
import cn.jitmarketing.hot.entity.Diff;
import cn.jitmarketing.hot.entity.ReceiveSheet;
import cn.jitmarketing.hot.entity.ResultNet;
import cn.jitmarketing.hot.util.ExitAppUtils;
import cn.jitmarketing.hot.util.LogUtils;
import cn.jitmarketing.hot.view.TitleWidget;
/**
 * 差异
 * @author liushang1
 *
 */
public class DiffActivity extends BaseSwipeBackAcvitiy implements OnClickListener {
	
    private static final int WHAT_STOCK_DIFF_NET = 0x01;
    
	@ViewInject(R.id.check_title)
    TitleWidget check_title;
    @ViewInject(R.id.check_list)
    ListView check_list;
    @ViewInject(R.id.tv_confirm)
    TextView tv_confirm;

    private ArrayList<Diff> diffList;
    private String receiveSheetNo;
    private DiffListAdapter diffListAdapter;

    @Override
    protected void exInitAfter() {}

    @Override
    protected void exInitBundle() {
        initIble(this, this);
    }

    @Override
    protected String[] exInitReceiver() {
        return null;
    }

    @Override
    protected int exInitLayout() {
        return R.layout.activity_diff;
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
    protected void exInitView() {
        check_title.setText("差异");
        check_title.setOnLeftClickListner(this);
        tv_confirm.setOnClickListener(this);
        diffList = (ArrayList<Diff>) getIntent().getSerializableExtra("SkuidList");
        receiveSheetNo = getIntent().getStringExtra("ReceiveSheetNo");
        diffListAdapter = new DiffListAdapter(getLayoutInflater(), diffList);
        check_list.setAdapter(diffListAdapter);
    }
    
    @Override
	public Map<String, String> onStart(int what) {
		switch (what) {
			case WHAT_STOCK_DIFF_NET:
				return null;
		}
		return null;
	}
    
    @Override
	public void onError(int what, int e, String message) {
		// 操作日志
		LogUtils.logOnFile("入库差异失败->调整" + message);
		switch (what) {
		case WHAT_STOCK_DIFF_NET:
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
			case WHAT_STOCK_DIFF_NET:
				if(null == net.data){//{"Success":true,"Message":null,"Data":"签收成功!"}
					Ex.Toast(mActivity).showLong(net.message);
					diffList.clear();
					diffListAdapter.notifyDataSetChanged();
					return ;
				}
				break;

		}
	}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lv_left:
                this.finish();
                break;
            case R.id.tv_confirm:
            	if(diffList.isEmpty()){
    				Ex.Toast(this).showLong("提交数据为空");
    				return;
    			}
            	// 日志操作
    			LogUtils.logOnFile("差异->确定");
    			
    			JSONObject jsonObject = new JSONObject();
    			String postData = null;
    			try {
    				jsonObject.put("UnitID", HotApplication.getInstance().getUnitId());
    				jsonObject.put("UserID", HotApplication.getInstance().getUserId());
    				jsonObject.put("BillNo", receiveSheetNo);
    				jsonObject.put("AdjustType", "1");
    				jsonObject.put("Remark", "");
    				postData = jsonObject.toString();
    			} catch (JSONException e) {
    				LogUtils.logOnFile("确定->json 解析出错:"+ExceptionUtils.getStackTrace(e));
    			}
    			startJsonTask(HotConstants.Global.APP_URL_USER + HotConstants.Global.IN_STOCK_DIFF_CONFIRM, 
    					WHAT_STOCK_DIFF_NET, true, NET_METHOD_POST, postData, false);
                break;
        }
    }
}
