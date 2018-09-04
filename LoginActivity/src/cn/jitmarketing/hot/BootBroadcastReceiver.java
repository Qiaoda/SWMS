package cn.jitmarketing.hot;

import cn.jitmarketing.hot.service.BootService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context,BootService.class);
		context.startService(service);
	}

}
