package cn.jitmarketing.hot.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import cn.jitmarketing.hot.HotApplication;
import cn.jitmarketing.hot.HotConstants;
import cn.jitmarketing.hot.MainActivity;
import cn.jitmarketing.hot.service.UploadLogService;

import com.google.gson.JsonObject;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * 捕获android程序崩溃日志<br>
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序.
 * 
 * @author PMTOAM
 * 
 */
@SuppressLint("SimpleDateFormat")
public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = CrashHandler.class.getCanonicalName();

	// 系统默认的UncaughtException处理类
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandler实例
	private static CrashHandler INSTANCE = new CrashHandler();
	// 程序的Context对象
	private Context mContext;
	private Application mApplication;
	// 用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<String, String>();

	// 用于格式化日期,作为日志文件名的一部分
	private DateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");

	/**
	 * 保证只有一个实例 
	 */
	private CrashHandler() {
	}

	/**
	 * 获取实例 ，单例模式
	 */
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context, Application application) {
		mContext = context;
		mApplication = application;
		// 获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Log.e(TAG, "error : ", e);
			}

			// 退出程序
			//android.os.Process.killProcess(android.os.Process.myPid());
			//System.exit(0);
			ExitAppUtils.getInstance().exit();

		}
		
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}

		// 使用Toast来显示异常信息
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				Toast.makeText(mContext, "很抱歉，程序出现异常,即将退出!", Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}.start();
		
		// 收集设备参数信息
		collectDeviceInfo(mContext);
		
		// 保存日志文件
//		String str = saveCrashInfo2File(ex);
//		Log.e(TAG, str);
		errorLog(ex);
		return true;
	}

	/**
	 * 收集设备参数信息
	 * 
	 * @param ctx
	 */
	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}

		} catch (NameNotFoundException e) {
			Log.e(TAG, "an error occured when collect package info", e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				Log.d(TAG, field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				Log.e(TAG, "an error occured when collect crash info", e);
			}
		}
	}
	
	/**
	 * 错误日志输出到LogUtils
	 */
	private void errorLog(Throwable ex) {
		
		StringBuffer sb = new StringBuffer();
		String msg = getStackTraceString(ex);
		sb.append("\n" + msg);

		// 输出到操作日志
		LogUtils.logOnFile(sb.toString());
		Log.d(TAG+"_errorLog", sb.toString());
		//发送错误堆栈到服务器
		if(!HotConstants.Global.APP_URL_USER.equals("192.168.1.10:80/"))
			sendCrashError2Server(msg);
	}
	
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private void sendCrashError2Server(String msg){
		LogUtils.logOnFile("-----start sendCrashError2Server");
		LogUtils.logOnFile("-----msg:"+msg);
		final JsonObject jsonObject = new JsonObject();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		jsonObject.addProperty("UnitID", ((HotApplication)mApplication).getUnitId());
		jsonObject.addProperty("EX_TIME", format.format(new Date(Long.valueOf(infos.get("TIME")))));
		jsonObject.addProperty("EX_FINGERPRINT", infos.get("FINGERPRINT"));
		jsonObject.addProperty("EX_HARDWARE", infos.get("HARDWARE"));
		jsonObject.addProperty("EX_UNKNOWN", infos.get("UNKNOWN"));
		jsonObject.addProperty("EX_RADIO", infos.get("RADIO"));
		jsonObject.addProperty("EX_BOARD", infos.get("BOARD"));
		jsonObject.addProperty("EX_versionCode", infos.get("versionCode"));
		jsonObject.addProperty("EX_PRODUCT", infos.get("PRODUCT"));
		jsonObject.addProperty("EX_versionName", infos.get("versionName"));
		jsonObject.addProperty("EX_DISPLAY", infos.get("DISPLAY"));
		jsonObject.addProperty("EX_USER", infos.get("USER"));
		jsonObject.addProperty("EX_HOST", infos.get("HOST"));
		jsonObject.addProperty("EX_DEVICE", infos.get("DEVICE"));
		jsonObject.addProperty("EX_TAGS", infos.get("TAGS"));
		jsonObject.addProperty("EX_MODEL", infos.get("MODEL"));
		jsonObject.addProperty("EX_BOOTLOADER", infos.get("BOOTLOADER"));
		jsonObject.addProperty("EX_CPU_ABI", infos.get("CPU_ABI"));
		jsonObject.addProperty("EX_CPU_ABI2", infos.get("CPU_ABI2"));
		jsonObject.addProperty("EX_IS_DEBUGGABLE", infos.get("IS_DEBUGGABLE"));
		jsonObject.addProperty("EX_ID", infos.get("ID"));
		jsonObject.addProperty("EX_SERIAL", infos.get("SERIAL"));
		jsonObject.addProperty("EX_MANUFACTURER", infos.get("MANUFACTURER"));
		jsonObject.addProperty("EX_BRAND", infos.get("BRAND"));
		jsonObject.addProperty("EX_TYPE", infos.get("TYPE"));
		jsonObject.addProperty("EX_error", msg);
		
		Thread logThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					OkHttpClient client = new OkHttpClient();
					
					RequestBody body = RequestBody.create(JSON, jsonObject.toString());
					Request request = new Request.Builder()
					      .url(HotConstants.Global.APP_URL_USER + HotConstants.Global.UPLOAD_ERROR_URL)
					      .post(body)
					      .build();
					
					Response response = client.newCall(request).execute();
					LogUtils.logOnFile(response.body().string());
					
				} catch (IOException e) {
					LogUtils.logOnFile(e.getMessage());
				}
			}
		});
		logThread.start();
		try {
			logThread.join();
		} catch (InterruptedException e) {
			LogUtils.logOnFile(e.getMessage());
		}
		
		LogUtils.logOnFile("-----end sendCrashError2Server");
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 * @return 返回文件名称,便于将文件传送到服务器
	 */
	private String saveCrashInfo2File(Throwable ex) {

		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append("[" + key + ", " + value + "]\n");
		}

		sb.append("\n" + getStackTraceString(ex));

		// 输出到操作日志
		LogUtils.logOnFile(sb.toString());

		try {
			String time = formatter.format(new Date());

			TelephonyManager mTelephonyMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			String imei = mTelephonyMgr.getDeviceId();
			if (TextUtils.isEmpty(imei)) {
				imei = "unknownimei";
			}

			String fileName = "CRS_" + time + "_" + imei + ".txt";

			File sdDir = null;

			if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
				sdDir = Environment.getExternalStorageDirectory();

			File cacheDir = new File(sdDir + File.separator + "dPhoneLog");
			if (!cacheDir.exists()) 
				cacheDir.mkdir();

			File filePath = new File(cacheDir + File.separator + fileName);

			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(sb.toString().getBytes());
			fos.close();

			return fileName;
		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing file...", e);
		}
		return null;
	}

	/**
	 * 获取捕捉到的异常的字符串
	 */
	public static String getStackTraceString(Throwable tr) {
		if (tr == null) {
			return "";
		}

		Throwable t = tr;
		while (t != null) {
			if (t instanceof UnknownHostException) {
				return "";
			}
			t = t.getCause();
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		tr.printStackTrace(pw);
		return sw.toString();
	}
}
