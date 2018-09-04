package cn.jitmarketing.hot.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cn.jitmarketing.hot.HotConstants;

public class PermissionUtil {
	
	private static void do_exec(String cmd) {
		DataOutputStream os = null;
        try {  
            Process proc = Runtime.getRuntime().exec("/system/xbin/su");
            os = new DataOutputStream(proc.getOutputStream());
            os.writeBytes("setprop persist.sys.key_passwd_enable false \n");
            //os.flush();
            
            os.writeBytes(" exit \n");
            os.flush();
            proc.waitFor();
        } catch (Exception e) {  
        	Log.e("PermissionUtil", ExceptionUtils.getStackTrace(e));
        }finally{
        	if(os != null){
        		try {
					os.close();
				} catch (IOException e) {
					Log.e("PermissionUtil", ExceptionUtils.getStackTrace(e)); 
				}
        	}
        }
    }  
	
	public static boolean getRoot(String cmd){
		DataOutputStream stream = null;
		try {

			Process process = Runtime.getRuntime().exec("su");
			stream = new DataOutputStream(process.getOutputStream());
			stream.writeBytes(cmd + " \n");
			stream.writeBytes(" exit \n");
			stream.flush();
			return process.waitFor()==0;
		}  catch (Exception e) {
			System.out.println(e);
			return false;
		}finally{
			if(stream != null){
				try {
					stream.close();
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		}
	}
	

	/**0.不允许,1.允许*/
	public static void installAppPermission(Context context, String model, int app){
		LogUtils.logOnFile("安装app权限->"+model+"->"+app);
		if(StringUtils.isBlank(model))
			return;
		if(model.equals(HotConstants.Global.GIONEELY72_KK)){
			Intent _intent = new Intent(HotConstants.Global.SYS_PERMISSION);
			
			_intent.putExtra("Timestamp", new Date().getTime());
			_intent.putExtra("Cmd", 0x000F);
			_intent.putExtra("Mode", app); 
			
			context.sendBroadcast(_intent);
		}else if(model.contains(HotConstants.Global.JIEBAO_HT380)){
			/*String adb = "setprop persist.sys.usb.config mass_storage,acm"+(app==0?"":",adb");
			Log.d("PermissionUtil", adb);
			do_exec(adb);
			String pwd = "setprop persist.sys.key_passwd_enable "+(app==0?true:false);
			Log.d("PermissionUtil", pwd);
			do_exec(pwd);*/
		}
			
	}
		
	/**0.允许,1.不允许*/
	public static void uninstallAppPermission(Context context, String model, int app){
		
		LogUtils.logOnFile("卸载app权限->"+model+"->"+app);
		if(StringUtils.isBlank(model))
			return;
		
		if(model.equals(HotConstants.Global.GIONEELY72_KK)){
			Intent _intent = new Intent(HotConstants.Global.SYS_PERMISSION);

	        _intent.putExtra("Timestamp", new Date().getTime());
	        _intent.putExtra("Cmd", 0x0014);
	        _intent.putExtra("Enable", app);

	        context.sendBroadcast(_intent);
		}
		
	}
}
