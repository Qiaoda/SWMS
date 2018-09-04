package cn.jitmarketing.hot.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import org.apache.commons.lang3.exception.ExceptionUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.ex.lib.core.callback.ExRequestCallback;
import com.ex.lib.core.exception.ExException;
import com.ex.lib.core.utils.Ex;
import com.ex.lib.core.utils.mgr.MgrPerference;
import com.google.gson.JsonObject;
import cn.jitmarketing.hot.HotConstants;
import cn.jitmarketing.hot.LoginActivity;
import cn.jitmarketing.hot.entity.ResultNet;
import cn.jitmarketing.hot.util.LogUtils;
import cn.jitmarketing.hot.util.MacUtils;
import cn.jitmarketing.hot.util.MobileInfoUtil;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

@SuppressLint("NewApi")
public class AlarmService extends Service {

	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		requestHttp();
		
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 00);
        calendar.set(Calendar.MILLISECOND, 0);
        Intent i = new Intent(this, AlarmService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //参数2是开始时间、参数3是允许系统延迟的时间
        	manager.setWindow(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + 1000*60*60*24, 0, pi);
        } 
		return super.onStartCommand(intent, flags, startId);
	}
		
	private void requestHttp() {
		LogUtils.logOnFile("AlarmService.开始上传设备信息");
		HashMap<String, String> map = new HashMap<String, String>();
		//map.put("UserID", MgrPerference.getInstance(this).getString(HotConstants.User.SHARE_USERID));
		map.put("UnitID", MgrPerference.getInstance(this).getString(HotConstants.Unit.UNIT_ID));
		map.put("MACAddress", MacUtils.getMobileMAC(this));
		map.put("DeviceNo", MobileInfoUtil.getIMEI(AlarmService.this));
		map.put("DeviceBrand", android.os.Build.BRAND);
		map.put("DeviceModel", android.os.Build.MODEL);
		map.put("OSVersion", android.os.Build.VERSION.RELEASE);
		
		String url = MgrPerference.getInstance(this).getString("CACHE_APP_URL") + HotConstants.Global.UPLOAD_DEVICE_INFO;
		LogUtils.logOnFile("AlarmService.url="+url);
		if(url.indexOf("http") != -1)
			Ex.Net(this).sendAsyncPost(url, map, requestCallback);
	}
	
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	ExRequestCallback requestCallback = new ExRequestCallback() {

		@Override
		public void onSuccess(InputStream inStream, HashMap<String, String> cookies) {
			// 请求结果
			String result = "";
			// 判断结果流是否为空
			if (inStream != null) {
				// 转换流对象
				result = Ex.T().getInStream2Str(inStream);
				Log.e("result--", result);
				result = result.replaceAll("\\s+","");
				ResultNet<?> net = Ex.T().getString2Cls(result, ResultNet.class);
				if (net.isSuccess) {
					LogUtils.logOnFile("AlarmService.上传设备信息成功");
				}else{
					LogUtils.logOnFile("AlarmService.上传设备信息失败:"+net.message);
				}
			}
		}

		@Override
		public void onError(int statusCode, ExException e) {
			final JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("UnitID", MgrPerference.getInstance(AlarmService.this).getString(HotConstants.Unit.UNIT_ID));
			jsonObject.addProperty("EX_error", ExceptionUtils.getStackTrace(e));
			try {
				OkHttpClient client = new OkHttpClient();
				
				RequestBody body = RequestBody.create(JSON, jsonObject.toString());
				Request request = new Request.Builder()
				      .url(MgrPerference.getInstance(AlarmService.this).getString("CACHE_APP_URL") + HotConstants.Global.UPLOAD_ERROR_URL)
				      .post(body)
				      .build();
				
				Response response = client.newCall(request).execute();
				LogUtils.logOnFile(response.body().string());
				
			} catch (IOException e1) {
				LogUtils.logOnFile(e1.getMessage());
			}
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
