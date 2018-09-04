package cn.jitmarketing.hot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.ex.lib.core.utils.Ex;
import com.ex.lib.core.utils.mgr.MgrPerference;
import com.ex.lib.ext.xutils.annotation.ViewInject;
import com.google.gson.JsonObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import cn.jitmarketing.hot.HotConstants.Global;
import cn.jitmarketing.hot.entity.ResultNet;
import cn.jitmarketing.hot.entity.UpDataBean;
import cn.jitmarketing.hot.net.WarehouseNet;
import cn.jitmarketing.hot.service.BootService;
import cn.jitmarketing.hot.service.NoticeService;
import cn.jitmarketing.hot.service.UpdateService;
import cn.jitmarketing.hot.ui.shelf.AmendPasswordActivity;
import cn.jitmarketing.hot.util.ExitAppUtils;
import cn.jitmarketing.hot.util.LogUtils;
import cn.jitmarketing.hot.view.BaseDialog;
import cn.jitmarketing.hot.view.TitleWidget;
import cn.jitmarketing.hot.util.PermissionUtil;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class CopyOfSettingActivity extends BaseSwipeBackAcvitiy {
	private static final int WHAT_NET_UPDATE = 0x11;
	/**
	 * 标题
	 */
	@ViewInject(R.id.setting_title)
	TitleWidget titleLayout;
	/**
	 * 修改密码
	 */
	@ViewInject(R.id.setting_modify_pwd_txt)
	TextView modifyPwdBtn;
	/**
	 * 注销按钮
	 */
	@ViewInject(R.id.setting_logout_btn)
	Button logoutBtn;
	@ViewInject(R.id.setting_version_txt)
	TextView setting_version_txt;
	/**
	 * 检查更新
	 */
	@ViewInject(R.id.setting_check_update_txt)
	TextView setting_check_update_txt;
	/**
	 * 通知设置
	 */
	@ViewInject(R.id.notice_txt)
	TextView notice_txt;

	/**
	 * 日志上传
	 */
	@ViewInject(R.id.log_upload)
	TextView log_upload;
	
	@ViewInject(R.id.ll_app_permission)
	LinearLayout ll_app_permission;
	/**启用安装*/
	@ViewInject(R.id.btn_allow_install_app)
	Button btn_allow_install_app;
	/**禁用安装*/
	@ViewInject(R.id.btn_not_allow_install_app)
	Button btn_not_allow_install_app;
	/**启用卸载*/
	@ViewInject(R.id.btn_allow_uninstall_app)
	Button btn_allow_uninstall_app;
	/**禁用卸载*/
	@ViewInject(R.id.btn_not_allow_uninstall_app)
	Button btn_not_allow_uninstall_app;
	HotApplication ap;
	/* 版本更新对话框 */
	private BaseDialog upDataDialog;
	private ProgressBar progressBar;
	private TextView progress;
	private TextView updata_content;
	@Override
	protected int exInitLayout() {
		EventBus.getDefault().register(this);
		return R.layout.activity_setting;
	}

	@Override
	protected void exInitBundle() {
		initIble(this, this);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ExitAppUtils.getInstance().addActivity(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
		ExitAppUtils.getInstance().delActivity(this);
	}

	@Override
	protected void exInitView() {
		ap = (HotApplication) getApplication();
		setting_version_txt.setOnClickListener(clickListener);
		ll_app_permission.setVisibility(View.GONE);
		btn_allow_install_app.setOnClickListener(clickListener);
		btn_not_allow_install_app.setOnClickListener(clickListener);
		btn_allow_uninstall_app.setOnClickListener(clickListener);
		btn_not_allow_uninstall_app.setOnClickListener(clickListener);
		log_upload.setOnClickListener(clickListener);
		titleLayout.setOnLeftClickListner(clickListener);
		logoutBtn.setOnClickListener(clickListener);
		modifyPwdBtn.setOnClickListener(clickListener);
		setting_check_update_txt.setOnClickListener(clickListener);
		notice_txt.setOnClickListener(clickListener);
		/* 创建更新dialog */
		createUpDataDialog();
		try {
			setting_version_txt.setText("当前版本号：" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} // 设置本地版本号
		initGlobal();

	}
	
	/* 强制更新对话框 */
	private void createUpDataDialog() {
		String msg = "";
		View view = LayoutInflater.from(this).inflate(R.layout.dialog_updata, null);
		upDataDialog = new BaseDialog(this, view);
		progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		progress = (TextView) view.findViewById(R.id.progress);
		updata_content = (TextView) view.findViewById(R.id.updata_content);
		updata_content.setText(msg);
		upDataDialog.setCancelable(false);
		upDataDialog.setCanceledOnTouchOutside(false);
	}
	
	@Subscribe(threadMode = ThreadMode.MainThread)
	public void getUpData(UpDataBean upDataBean) {
		progressBar.setProgress(upDataBean.getProgressNum());
		progress.setText(upDataBean.getProgres());
		if (upDataBean.isFailed()) {
			upDataDialog.dismiss();
			Toast.makeText(CopyOfSettingActivity.this, "更新失败，请重新启动", Toast.LENGTH_SHORT).show();
		}
		if (upDataBean.isFinish()) {
			upDataDialog.dismiss();
		}
	}

	/**
	 * 初始化全局变量 实际工作中这个方法中serverVersion从服务器端获取，最好在启动画面的activity中执行
	 */
	public void initGlobal() {
		try {
			Global.localVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode; // 设置本地版本号
			Global.localVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName; // 设置本地版本名称
			// Global.serverVersion = 1;//假定服务器版本为2，本地版本默认是1
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 检查更新版本
	 */
	public void checkVersion() {
		boolean isUpdate = false;
		if (Global.localVersionName != null && Global.serverVersionName != null) {
			String[] localArray = Global.localVersionName.split("\\.");//2.0.6
			String[] serverArray = Global.serverVersionName.split("\\.");//2.0.10
			if (Integer.valueOf(serverArray[0]) > Integer.valueOf(localArray[0])) {
				isUpdate = true;
			} else {
				if (Integer.valueOf(serverArray[0]) < Integer.valueOf(localArray[0])) {
					return;
				}
				if (Integer.valueOf(serverArray[1]) > Integer.valueOf(localArray[1])) {
					isUpdate = true;
				} else {
					if (Integer.valueOf(serverArray[1]) < Integer.valueOf(localArray[1])) {
						return;
					}
					if (Integer.valueOf(serverArray[2]) > Integer.valueOf(localArray[2])) {
						isUpdate = true;
					} else {
						isUpdate = false;
					}
				}
			}
		}
		if (isUpdate) {
			String msg = "发现新版本,建议立即更新使用\n 1.修改了盘点功能";
			upDataDialog.show();
			Intent updateIntent = new Intent(CopyOfSettingActivity.this, UpdateService.class);
			updateIntent.putExtra("titleId", R.string.app_name);
			updateIntent.putExtra("downUrl", Global.servreDownloadUrl);
			startService(updateIntent);
			// 发现新版本，提示用户更新
//			AlertDialog.Builder alert = new AlertDialog.Builder(this);
//			alert.setTitle("软件升级").setMessage(msg).setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					// 开启更新服务UpdateService
//					// 这里为了把update更好模块化，可以传一些updateService依赖的值
//					// 如布局ID，资源ID，动态获取的标题,这里以app_name为例
//					Intent updateIntent = new Intent(SettingActivity.this, UpdateService.class);
//					updateIntent.putExtra("titleId", R.string.app_name);
//					updateIntent.putExtra("downUrl", Global.servreDownloadUrl);
//					startService(updateIntent);
//				}
//			});
//			alert.create().show();
		} else {
			// 清理工作，略去
			// cheanUpdateFile();
			Ex.Toast(mContext).showShort("已经是最新版本了亲");
		}
	}

	@Override
	public Map<String, String> onStart(int what) {
		switch (what) {
		case WHAT_NET_UPDATE:
			return WarehouseNet.updateApp(Global.localVersionName);
		}
		return null;
	}

	@Override
	public void onError(int what, int e, String message) {
		super.onError(what, e, message);
	}

	@Override
	public void onSuccess(int what, String result, boolean hashCache) {
		ResultNet<?> net = Ex.T().getString2Cls(result, ResultNet.class);
		if (!net.isSuccess) {
			Ex.Toast(mContext).showLong(net.message);
			return;
		}
		switch (what) {
		case WHAT_NET_UPDATE:
			String str = mGson.toJson(net.data);
			try {
				JSONObject ob = new JSONObject(str);
				Global.servreDownloadUrl = ob.getString("Url");
				Global.serverVersionName = ob.getString("Version");
				checkVersion();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			break;
		}
	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.lv_left:
				CopyOfSettingActivity.this.finish();
				break;
			case R.id.setting_modify_pwd_txt:
				Intent intent = new Intent();
				intent.setClass(CopyOfSettingActivity.this, AmendPasswordActivity.class);
				startActivity(intent);
				break; 
			case R.id.setting_logout_btn:// 注销按钮
				new AlertDialog.Builder(CopyOfSettingActivity.this).setTitle(R.string.noteTitle).setMessage(getString(R.string.LoginOut)).setPositiveButton(R.string.sureTitle, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						stopService(new Intent(CopyOfSettingActivity.this, BootService.class));
						MgrPerference.getInstance(CopyOfSettingActivity.this).putString(HotConstants.User.SHARE_PASSWORD, "");
						Ex.Activity(mActivity).finishAll();
						stopService(new Intent(CopyOfSettingActivity.this, NoticeService.class));
						Intent intent = new Intent(CopyOfSettingActivity.this, LoginActivity.class);
						startActivity(intent);
					}
				}).setNegativeButton(R.string.cancelTitle, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
				break;
			case R.id.setting_check_update_txt:
				startTask(HotConstants.Global.APP_URL_USER + Global.updateUrl, WHAT_NET_UPDATE, NET_METHOD_POST, false);
				break;
			case R.id.notice_txt:
				Intent intent1 = new Intent();
				intent1.setClass(CopyOfSettingActivity.this, NoticeSettingActivity.class);
				startActivity(intent1);
				break;
			case R.id.setting_version_txt:
				 System.arraycopy(mHints, 1, mHints, 0, mHints.length - 1);
				 mHints[mHints.length - 1] = SystemClock.uptimeMillis();
				 if (SystemClock.uptimeMillis() - mHints[0] <= 1000){
					 ll_app_permission.setVisibility(View.VISIBLE);
				 }
				 break;
			case R.id.btn_allow_install_app:
				PermissionUtil.installAppPermission(CopyOfSettingActivity.this, ap.model, 1);
				Ex.Toast(mContext).showShort("已启用APP安装");
				break;
			case R.id.btn_not_allow_install_app:
				PermissionUtil.installAppPermission(CopyOfSettingActivity.this, ap.model, 0);
				Ex.Toast(mContext).showShort("已禁用APP安装");
				break;
			case R.id.btn_allow_uninstall_app:
				PermissionUtil.uninstallAppPermission(CopyOfSettingActivity.this, ap.model, 0);
				Ex.Toast(mContext).showShort("已启用APP卸载");
				break;
			case R.id.btn_not_allow_uninstall_app:
				PermissionUtil.uninstallAppPermission(CopyOfSettingActivity.this, ap.model, 1);
				Ex.Toast(mContext).showShort("已禁用APP卸载");
				break;
			case R.id.log_upload:
				uploadLog();
				break;
			default:
				break;
			}
		}
	};
	
	private void uploadLog(){
		Ex.Dialog(this).showProgressDialog("", "日志上传中");
		LogUtils.logOnFile("开始上传日志");
		SimpleDateFormat pathFormat = new SimpleDateFormat("yyyy-MM-dd");
		File file = new File("/sdcard", "hotlog."+pathFormat.format(new Date())+".txt");
		if(!file.exists()){
			Ex.Toast(mContext).showShort("日志文件不存在");
		}
		RequestBody requestBody = new MultipartBody.Builder()
	      .setType(MultipartBody.FORM)
	      .addFormDataPart("LogFileName", file.getName())
	      .addFormDataPart("file", file.getName(),
	        RequestBody.create(MediaType.parse("application/octet-stream"),file))
	      .build();
		OkHttpClient client = new OkHttpClient.Builder()
			.readTimeout(5, TimeUnit.MINUTES)
			.build();
	    Request request = new Request.Builder()
	      .url("http://10.1.4.110:8080/upload/uploadLog")//HotConstants.Global.APP_URL_USER+HotConstants.Global.LOG_UPLOAD_URL
	      .post(requestBody)
	      .build();
	 
	    Call call = client.newCall(request);
	    call.enqueue(new okhttp3.Callback() {
				
				@Override
				public void onResponse(Call call, Response response) throws IOException {
					Ex.Dialog(CopyOfSettingActivity.this).dismissProgressDialog();
					String result = response.body().string();
					LogUtils.logOnFile("日志上传结果:"+result);
					try {
						final JSONObject jsonObject = new JSONObject(result);
						boolean success = jsonObject.getBoolean("Success");
						final String msg = jsonObject.getString("Message");
						if(success){
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									Ex.Toast(mContext).showShort("上传日志成功");
									
								}
							});
						}else{
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									Ex.Toast(mContext).showShort(msg);
								}
							});
							
						}
					} catch (JSONException e) {
						LogUtils.logOnFile("解析json失败:"+ExceptionUtils.getStackTrace(e));
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								Ex.Toast(mContext).showShort("上传日志失败");
							}
						});
						
					}
					
				}
				
				@Override
				public void onFailure(Call arg0, IOException e) {
					Ex.Dialog(CopyOfSettingActivity.this).dismissProgressDialog();
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Ex.Toast(mContext).showShort("上传日志失败");
						}
					});
					
					LogUtils.logOnFile("上传日志失败:"+ExceptionUtils.getStackTrace(e));
				}
			});
	}
	
	private long[] mHints = new long[5];

}
