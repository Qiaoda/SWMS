package cn.jitmarketing.hot.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadUtil {

	
	public static void uploadLog(File file, String url, Callback callback){
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
	      .url(url)//HotConstants.Global.APP_URL_USER+HotConstants.Global.LOG_UPLOAD_URL
	      .post(requestBody)
	      .build();
	 
	    Call call = client.newCall(request);
	    call.enqueue(callback);
	}
	
	public static void uploadLog(File file, final String url){
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
	      .url(url)//http://10.1.4.110:8080/upload/uploadLog
	      .post(requestBody)
	      .build();
	 
	    Call call = client.newCall(request);
	    call.enqueue(new Callback() {
			
			@Override
			public void onResponse(Call arg0, Response arg1) throws IOException {
				LogUtils.logOnFile("日志上传成功:"+url);
				
			}
			
			@Override
			public void onFailure(Call arg0, IOException arg1) {
				LogUtils.logOnFile("日志上传失败:"+url);
				
			}
		});
	}
}
