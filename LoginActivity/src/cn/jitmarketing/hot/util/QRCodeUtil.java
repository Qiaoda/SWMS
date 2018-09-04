package cn.jitmarketing.hot.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.graphics.Bitmap;

public class QRCodeUtil {
	
	public static Bitmap generateBitmap(String content,int width, int height) {  
	    QRCodeWriter qrCodeWriter = new QRCodeWriter();  
	    Map<EncodeHintType, String> hints = new HashMap<>();  
	    hints.put(EncodeHintType.CHARACTER_SET, "utf-8");  
	    try {  
	        BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);  
	        int[] pixels = new int[width * height];  
	        for (int i = 0; i < height; i++) {  
	            for (int j = 0; j < width; j++) {  
	                if (encode.get(j, i)) {  
	                    pixels[i * width + j] = 0x00000000;  
	                } else {  
	                    pixels[i * width + j] = 0xffffffff;  
	                }  
	            }  
	        }  
	        return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);  
	    } catch (WriterException e) {  
	    	LogUtils.logOnFile("->生成二维码出错:\n"+ExceptionUtils.getStackTrace(e));
	    }  
	    return null;  
	}  
	
}
