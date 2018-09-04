package cn.jitmarketing.hot.ui.sku;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ex.lib.core.utils.Ex;
import com.ex.lib.ext.xutils.annotation.ViewInject;
import com.example.scandemo.BaseSwipeOperationActivity;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.jitmarketing.hot.HotApplication;
import cn.jitmarketing.hot.HotConstants;
import cn.jitmarketing.hot.R;
import cn.jitmarketing.hot.adapter.ItemListAdapter;
import cn.jitmarketing.hot.adapter.PromotionListAdapter;
import cn.jitmarketing.hot.adapter.StockListAdapter;
import cn.jitmarketing.hot.entity.ItemEntity;
import cn.jitmarketing.hot.entity.PromotionEntity;
import cn.jitmarketing.hot.entity.ResultNet;
import cn.jitmarketing.hot.entity.SampleEntity;
import cn.jitmarketing.hot.entity.SearchSkuBean;
import cn.jitmarketing.hot.entity.SkuEntity;
import cn.jitmarketing.hot.net.SkuNet;
import cn.jitmarketing.hot.util.ConstValue;
import cn.jitmarketing.hot.util.ExitAppUtils;
import cn.jitmarketing.hot.util.FastDoubleClickUtil;
import cn.jitmarketing.hot.util.ImageUtil;
import cn.jitmarketing.hot.util.LogUtils;
import cn.jitmarketing.hot.util.SkuUtil;
import cn.jitmarketing.hot.view.BaseDialog;
import cn.jitmarketing.hot.view.ClearEditText;
import cn.jitmarketing.hot.view.TitleWidget;

/**
 * 库存查询
 */
public class CheckStockActivity extends BaseSwipeOperationActivity implements OnClickListener, OnItemClickListener {
	/**
     *
     */
	@ViewInject(R.id.frame_layout)
	FrameLayout frame_layout;
	/**
	 * sku整体布局
	 */
	@ViewInject(R.id.check_layout)
	LinearLayout check_layout;
	/**
	 * sku搜索页面布局
	 */
	@ViewInject(R.id.check_list_layout)
	LinearLayout check_list_layout;
	/**
	 * title控件
	 */
	@ViewInject(R.id.check_stock_title)
	TitleWidget check_stock_title;
	/**
	 * sku搜索点击框
	 */
	@ViewInject(R.id.check_stock_search_layout)
	LinearLayout checkSearchLayout;
	/**
	 * sku详细信息布局
	 */
	@ViewInject(R.id.layout_content)
	LinearLayout layout_content;
	/**
	 * sku列表
	 */
	@ViewInject(R.id.list_sku)
	ListView list_sku;
	/**
	 * 商品编码
	 */
	@ViewInject(R.id.stock_sku_code)
	TextView stock_sku_code;
	/**
	 * 商品名称
	 */
	@ViewInject(R.id.stock_sku_name)
	TextView stock_sku_name;
	/**
	 * 商品原价格
	 */
	@ViewInject(R.id.stock_sku_price)
	TextView stock_sku_price;
	/**
	 * 商品优惠价格
	 */
	@ViewInject(R.id.stock_sku_change_price)
	TextView stock_sku_change_price;
	/**
	 * 商品数量
	 */
	@ViewInject(R.id.stock_sku_num)
	TextView stock_sku_num;
	/**
	 * 库存
	 */
	@ViewInject(R.id.stock_sku_stock)
	TextView stock_sku_stock;
	/**
	 * 商品图标
	 */
	@ViewInject(R.id.stock_sku_icon)
	ImageView stock_sku_icon;

	/**
	 * 取新
	 */
	@ViewInject(R.id.get_new)
	TextView get_new;
	/**
	 * 另一只
	 */
	@ViewInject(R.id.get_the_other)
	TextView get_the_other;
	/**
	 * 出样
	 */
	@ViewInject(R.id.get_the_sample)
	TextView get_the_sample;

	/**
	 * 下次调价时间
	 */
	@ViewInject(R.id.tv_next_sale_price_date)
	TextView nextSalePriceDate;

	/**
	 * 下次调价价格
	 */
	@ViewInject(R.id.tv_next_sale_price)
	TextView nextSalePrice;

	/**
	 * sku搜索列表
	 */
	@ViewInject(R.id.allocationnoticesearch_list)
	ListView searchListView;
	/**
	 * sku搜索输入框
	 */
	@ViewInject(R.id.check_stock_search_edt)
	ClearEditText searchEditText;
	/**
	 * sku搜索取消按钮
	 */
	@ViewInject(R.id.check_storck_cancel)
	TextView storckCancelButton;
	
	ListView list_promotion;
	private AlertDialog promotionDialog;
	

	private static final int WHAT_NET_GET_STOCK = 0x10;
	private static final int WHAT_NET_GET_SIGNLE_SKU = 0x154;
	private static final int WHAT_NET_GET_NEW = 0x11;
	private static final int WHAT_NET_GET_STOCK_FUZZY = 0x13;
	private static final int WHAT_NET_GET_STOCK1 = 0x14;
	private static final int WHAT_NET_GET_E3_STOCK = 0x141;
	private static final int WHAT_GET_WECHAT_TOKEN = 0x111;
	private static final int WHAT_GET_WECHAT_ALARM = 0x112;
	private String skuCodeStr = "";
	private StockListAdapter stockListAdapter;
	private ArrayList<SkuEntity> skulist = new ArrayList<SkuEntity>();
	private PromotionListAdapter promotionListAdapter;
	private List<PromotionEntity> promotionList = new ArrayList<>();
	private SkuEntity selectedSku;
	private ItemEntity itemEntity;
	// private String skuInfo;
	private SearchSkuBean searchSkuBean;// sku搜索结果对象
	private ItemListAdapter itemListAdapter;
	private List<SearchSkuBean> searchSkuList = new ArrayList<SearchSkuBean>();
	private String searchCondition;
	private String globalToken;
	private int retryCount=0;

	private boolean showSearch;
	HotApplication ap;
	public static boolean sampleSuccess;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (showSearch) {
				hideSearchView();// 隐藏搜索框
			} else {
				finish();
			}
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
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
	protected void exInitAfter() {
	}

	@Override
	protected void exInitBundle() {
		initIble(this, this);
	}

	@Override
	protected int exInitLayout() {
		return R.layout.activity_check_stock;
	}

	@Override
	protected void exInitView() {
		ap = (HotApplication) getApplication();
		
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.promotion_dialog, null);
		//TextView tv_micro_mall = (TextView) view.findViewById(R.id.tv_micro_mall);
		/*tv_micro_mall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO 执行请求查询
				ImageView imageView = new ImageView(CheckStockActivity.this);
				int wpx = Des.dip2px(CheckStockActivity.this, 16);
				int hpx = Des.dip2px(CheckStockActivity.this, 5);
				int width = ConstValue.SCREEN_WIDTH - wpx;//屏幕三等分， 去掉中间间隔（10dp）
				int height = ConstValue.SCREEN_WIDTH - hpx;//屏幕三等分， 去掉中间间隔（10dp）
				Bitmap bitmap = QRCodeUtil.generateBitmap("http://www.baidu.com", width, height);
				imageView.setImageBitmap(bitmap);
				Dialog dialog = new Dialog(CheckStockActivity.this, R.style.transparent_dialog);
				dialog.setCancelable(true);
				dialog.setContentView(imageView);
				dialog.show();
				
			}
		});*/
		
		list_promotion = (ListView) view.findViewById(R.id.list_promotion);
		promotionListAdapter = new PromotionListAdapter(this, promotionList);
		list_promotion.setAdapter(promotionListAdapter);
		
		promotionDialog = new AlertDialog.Builder(this).create();
		promotionDialog.setView(view, 0, 0, 0, 0);
		
		get_new.setOnClickListener(this);
		get_the_other.setOnClickListener(this);
		get_the_sample.setOnClickListener(this);
		check_stock_title.setOnLeftClickListner(this);
		check_stock_title.setOnRightClickListner(this);
		stock_sku_stock.setOnClickListener(this);
		checkSearchLayout.setOnClickListener(this);
		searchEditText.addTextChangedListener(textWatcher);
		storckCancelButton.setOnClickListener(this);
		searchListView.setOnItemClickListener(this);
		searchEditText.setOnKeyListener(onKeyListener);
		hideSoftKeyBoard(this, searchEditText);
		check_list_layout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		// 从扫描动作进入的（主界面扫描直接跳转）
		String skuCode = getIntent().getStringExtra("skuCode");
		if (skuCode != null) {
			skuCodeStr = skuCode;
			stock_sku_code.setText(skuCode);
			/* 根据skuCode查询商品信息 */
			startTask(HotConstants.Global.APP_URL_USER + HotConstants.SKU.GetStoreForSKU, WHAT_NET_GET_STOCK, NET_METHOD_POST, false);
			layout_content.setVisibility(View.VISIBLE);
		}
		
		list_sku.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				//SkuEntity selectedSku = skulist.get(position);
				HotConstants.SELECTED = position;
				stockListAdapter.notifyDataSetChanged();
				selectedSku = skulist.get(position);
				//点击sku时，请求后台获取sku对应的促销信息
				JSONObject postData = new JSONObject();
				try {
					postData.put("SKUCode", selectedSku.SKUCode);
					startJsonTask(HotConstants.Global.APP_URL_USER + HotConstants.SKU.GetSkuPromotionPriceInfo, WHAT_NET_GET_SIGNLE_SKU, true, NET_METHOD_POST, postData.toString(), false);
				} catch (JSONException e) {
					LogUtils.logOnFile("创建json对象失败:"+ExceptionUtils.getStackTrace(e));
				}
				return false;
			}
		});
		
		createImageDialog();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 出样，换样，撤样成功后，返回该界面刷新
		// if(sampleSuccess) {
		// startTask(HotConstants.Global.APP_URL_USER
		// + HotConstants.SKU.GetStoreForSKU, WHAT_NET_GET_STOCK,
		// NET_METHOD_POST, false);
		// sampleSuccess = false;
		// }
	}

	@Override
	public Map<String, String> onStart(int what) {
		switch (what) {
		case WHAT_NET_GET_STOCK:// 获取商品信息
			return SkuNet.getStoreForSKU(skuCodeStr);
		case WHAT_NET_GET_STOCK1:
			return SkuNet.getStoreForSKU(skuCodeStr);
		case WHAT_NET_GET_NEW:// 取新
			return SkuNet.getNew(skuCodeStr, mRecordid);
		case WHAT_NET_GET_STOCK_FUZZY:// 搜索提示
			return SkuNet.getStoreForSKUFuzzy(searchCondition);
		}
		return null;
	}

	@Override
	public void onReceiver(Intent intent) {
		// byte[] barcode = intent.getByteArrayExtra("barocode");
		// int barocodelen = intent.getIntExtra("length", 0);
		// if(barcode != null) {
		// skuCodeStr = new String(barcode, 0,
		// barocodelen).toUpperCase().trim();
		// dealBarCode(skuCodeStr);
		// } else {
		// ap.getsoundPool(ap.Sound_error);
		// return;
		// }
	}

	private void dealBarCode(String skuCodeStr) {
		if (null != popupWindow) {
			popupWindow.dismiss();
			popupWindow = null;
		}
		if (!showSearch) {
			if (!SkuUtil.isWarehouse(skuCodeStr)) {
				ap.getsoundPool(ap.Sound_sku);
				stock_sku_code.setText(skuCodeStr);
				/* 获取商品信息 */
				startTask(HotConstants.Global.APP_URL_USER + HotConstants.SKU.GetStoreForSKU, WHAT_NET_GET_STOCK, NET_METHOD_POST, false);
			} else {
				ap.getsoundPool(ap.Sound_location);
			}
		}
	}

	@Override
	public void onError(int what, int e, String message) {
		sampleSuccess = false;
		Toast.makeText(this, "网络请求失败", Toast.LENGTH_LONG).show();
	}
	
	/**
	 * 显示图片
	 */
    private BaseDialog imageDialog;
    private ImageView imageView;
	private void createImageDialog() {
		View view = LayoutInflater.from(this).inflate(R.layout.dialog_goods_image, null);
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(null != imageDialog && imageDialog.isShowing()){
					imageDialog.dismiss();
				}
				
			}
		});
		imageDialog = new BaseDialog(this, view);
		imageView = (ImageView) view.findViewById(R.id.iv_image);
		
	}
	
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	
	private void getImage(String url, String storeId, String skuId, String skuCount){
		LogUtils.logOnFile("开始获取小程序图片...");
		//final String cacheImage = storeId+"_"+skuId+".jpeg";
		
		try {
			JSONObject data = new JSONObject();
			final String scene = storeId+":"+skuId+":"+skuCount;
			final String page = "pages/index/index";
			data.put("scene", scene);//+":"+skuCount
			data.put("page", page);
			OkHttpClient client = new OkHttpClient.Builder().readTimeout(10, TimeUnit.SECONDS).build();
			RequestBody body = RequestBody.create(JSON, data.toString());
			Request request = new Request.Builder()
			      .url(url)
			      .post(body)
			      .build();
			Call call = client.newCall(request);
			call.enqueue(new okhttp3.Callback() {
				
				@Override
				public void onResponse(Call arg0, Response response) throws IOException {
					LogUtils.logOnFile("解析小程序url："+response);
					ResponseBody body = response.body();
					//将响应数据转化为输入流数据
			          InputStream inputStream=body.byteStream();
			          
			          //将输入流数据转化为Bitmap位图数据
			          final Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
			          if(null == bitmap){
			        	  LogUtils.logOnFile("生成二维码失败，bitmap为空："+body.string());
			        	  failedHandler(scene, page);
			        	  return;
			          }
			          /*FileOutputStream cacheStream = new FileOutputStream(new File("/sdcard", cacheImage));
			          bitmap.compress(Bitmap.CompressFormat.JPEG, 100, cacheStream);
			          cacheStream.flush();
			          cacheStream.close();*/
			          LogUtils.logOnFile("解析小程序url成功");
			          runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							LogUtils.logOnFile("设置小程序图片成功");
							imageView.setImageBitmap(bitmap);
							if(imageDialog != null)
								imageDialog.show();
						}
					});
				}
				
				@Override
				public void onFailure(Call arg0, IOException e) {
					LogUtils.logOnFile("解析微信小程序图片失败:"+ExceptionUtils.getStackTrace(e));
					failedHandler(scene, page);
				}
			});
		} catch (Exception e) {
			LogUtils.logOnFile("创建json对象失败:"+ExceptionUtils.getStackTrace(e));
			 runOnUiThread(new Runnable() {
					
				@Override
				public void run() {
					Ex.Toast(mContext).showShort("获取二维码出错");
				}
			});
		}
		
	}
	
	private void failedHandler(final String scene, final String page){
		LogUtils.logOnFile("解析小程序图片失败,进入失败处理流程");
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				StringBuffer urlBuffer = new StringBuffer(HotConstants.Global.ORG_QR_CODE_URL+globalToken);
				try {
					urlBuffer.append("&scene=")
						.append(URLEncoder.encode(scene, "utf-8"))
						.append("&page=")
						.append(URLEncoder.encode(page, "utf-8"));
					LogUtils.logOnFile("请求公司地址，进行转发:"+urlBuffer.toString());
					ImageUtil.uploadImageCache(CheckStockActivity.this, urlBuffer.toString(), imageView, new Callback(){

						@Override
						public void onError() {
							
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									Ex.Toast(mContext).showShort("获取二维码出错");
									LogUtils.logOnFile("请求公司地址，获取二维码出错");
								}
							});
						}

						@Override
						public void onSuccess() {
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									LogUtils.logOnFile("请求公司地址，获取二维码成功");
									if(imageDialog != null)
										imageDialog.show();
								}
							});
							
						}
						
					});
				} catch (Exception e) {
					LogUtils.logOnFile("请求机构地址出错："+ExceptionUtils.getStackTrace(e));
					Ex.Toast(mContext).showShort("获取二维码出错");
				}
				
			}
		});
	}
	
	private void wechatAlarm(){
		LogUtils.logOnFile("微信报警开始");
		JSONObject params = new JSONObject();
        try {
			params.put("templateId", "GNtD6i0B1AQoOCfJSqNdw3_SI-tGaEh1WwmevtNsFT4");
			JSONArray jsonArray = new JSONArray();
			jsonArray.put("ofQWXjm49Ys2_EMNfMVQO2xv2HEo");
			params.put("openList", jsonArray);
			params.put("templateContextFirst", "微信二维码报警");
			params.put("templateContextRemark", "获取二维码失败");
			startJsonTask(HotConstants.Global.WECHAT_ALARM, WHAT_GET_WECHAT_ALARM, false, NET_METHOD_POST, params.toString(), false);
		} catch (JSONException e) {
			LogUtils.logOnFile("微信报警json出错"+ExceptionUtils.getStackTrace(e));
		}
	}

	private String mRecordid="";
	@Override
	public void onSuccess(int what, String result, boolean isCache) {
		LogUtils.logOnFile("onSuccess: what:"+what+result);
		sampleSuccess = false;
		ResultNet<?> net = Ex.T().getString2Cls(result, ResultNet.class);
		if (!net.isSuccess) {
			if(what == WHAT_NET_GET_E3_STOCK){
				try {
					JSONObject jsonObject = new JSONObject(result);
					LogUtils.logOnFile("->解析E3结果:\n"+jsonObject.toString());
					int rspCode = Integer.parseInt(jsonObject.get("rspCode")+"");
					String rspMsg = jsonObject.getString("rspMsg");
					if(rspCode == 1){
						JSONObject data = jsonObject.getJSONObject("data");
						if(null != data){
							int stockNum = data.getInt("stockNum");
							//E3有库存则生成小程序码，否则什么都不做
							if(stockNum > 0){
								//获取小程序token生成小程序码
								LogUtils.logOnFile("->获取小程序Token");
								startTask(HotConstants.Global.WECHAT_TOKEN, WHAT_GET_WECHAT_TOKEN, true, NET_METHOD_GET, false);
							}else{
								Ex.Toast(mContext).showLong("大仓无库存");
							}
						}
						
					}else{
						Ex.Toast(mContext).showLong(StringUtils.isBlank(rspMsg)?"查询E3接口出错":rspMsg);
						return;
					}
					
				} catch (JSONException e) {
					LogUtils.logOnFile("->解析E3结果出错:\n"+ExceptionUtils.getStackTrace(e));
				}
				return;
			}else if(what == WHAT_GET_WECHAT_TOKEN){
				try {
					JSONObject jsonObject = new JSONObject(result);
					String token = jsonObject.getString("token");
					globalToken = token;
					LogUtils.logOnFile("获取 token:"+token);
					getImage(HotConstants.Global.WECHAT_GENERATE_IMAGE+token, 
							HotApplication.getInstance().getUnitId(), selectedSku.SKUCode, selectedSku.SKUCount);
				} catch (JSONException e) {
					LogUtils.logOnFile("->解析token结果出错:\n"+ExceptionUtils.getStackTrace(e));
				}
				return;
			}
			
			if(StringUtils.isNoneBlank(net.message))
				Ex.Toast(mContext).showLong(net.message);
			return;
		}
		switch (what) {
		case WHAT_NET_GET_STOCK1:
		case WHAT_NET_GET_STOCK:// 获取商品信息
			if (null == net.data) {
				Ex.Toast(mActivity).show(net.message);
				layout_content.setVisibility(View.GONE);
				return;
			} else {
				if(StringUtils.isNoneBlank(net.message))
					Ex.Toast(mActivity).show(net.message);
				layout_content.setVisibility(View.VISIBLE);
			}
			try {
				JSONObject dataJson = new JSONObject(mGson.toJson(net.data));
				if(dataJson.has("recordid")){
					mRecordid = dataJson.getString("recordid");
				}
				String skulistStr = dataJson.getString("skus");
				String itemStr = dataJson.getString("item");
				itemEntity = mGson.fromJson(itemStr, ItemEntity.class);
				skulist.clear();
				skulist.addAll((ArrayList<SkuEntity>) mGson.fromJson(skulistStr, new TypeToken<List<SkuEntity>>() {
				}.getType()));
				/* 在列表中找出当前商品并选中 */
				HotConstants.SELECTED = 0;
				selectedSku = skulist.get(0);
				//默认显示第一个选中的sku的促销信息
				setPromotionInfo(selectedSku.promotionInfo4SwmsList);
				for (int i = 0; i < skulist.size(); i++) {
					if (skuCodeStr.equals(skulist.get(i).SKUCode)) {
						HotConstants.SELECTED = i;
						selectedSku = skulist.get(i);
					}
				}
				/* 显示商品信息 */
				setSkuData(itemEntity);
				/* 为取新存值 */
				setValue();
				String url = null;
				if (selectedSku.ColorID == null || selectedSku.ColorID.equals("")) {
					url = HotConstants.Global.APP_URL_USER + "ItemImgs/" + selectedSku.SKUCode + ".jpg";
				} else {
					url = HotConstants.Global.APP_URL_USER + "ItemImgs/" + itemEntity.ItemID + "$" + selectedSku.ColorID + ".jpg";
				}
				/* 显示图片 */
				ImageUtil.uploadImage(this, url, stock_sku_icon);

				if (showSearch) {
					hideSearchView();
					layout_content.setVisibility(View.VISIBLE);
				}
				// 判断是否启用小商品版
				if (HotConstants.Global.isStartXSPVersion) {
					if (itemEntity.AttDId.equals("3")) {// 小商品
						get_new.setEnabled(false);
						get_new.setClickable(false);
						get_new.setVisibility(View.GONE);
						get_the_sample.setEnabled(false);
						get_the_sample.setClickable(false);
						get_the_sample.setVisibility(View.GONE);
					} else {
						get_new.setEnabled(true);
						get_new.setClickable(true);
						get_new.setVisibility(View.VISIBLE);
						get_the_sample.setEnabled(true);
						get_the_sample.setClickable(true);
						get_the_sample.setVisibility(View.VISIBLE);
					}
				}
				//get_the_sample.setVisibility(View.GONE);
			} catch (JSONException e) {
				LogUtils.logOnFile("->解析搜索结果出错:\n"+ExceptionUtils.getStackTrace(e));
			}
			break;
		case WHAT_NET_GET_NEW:// 取新
			// 日志打印
			LogUtils.logOnFile("->取新成功");
		case WHAT_NET_GET_STOCK_FUZZY:
			if (null == net.data) {
				Ex.Toast(mActivity).show(net.message);
				return;
			}
			String itemStr = mGson.toJson(net.data);
			
			searchSkuList.clear();
			searchSkuList.addAll((List<SearchSkuBean>) mGson.fromJson(itemStr, new TypeToken<List<SearchSkuBean>>() {
			}.getType()));
			if (itemListAdapter == null) {
				itemListAdapter = new ItemListAdapter(this, searchSkuList);
				searchListView.setAdapter(itemListAdapter);
			} else {
				itemListAdapter.notifyDataSetChanged();
			}
			check_list_layout.setBackgroundColor(getResources().getColor(R.color.color_bg));
			break;
		case WHAT_NET_GET_SIGNLE_SKU:
			if (null == net.data) {
				Ex.Toast(mActivity).show(net.message);
				return;
			} 
			
			try {
				JSONObject dataJson = new JSONObject(mGson.toJson(net.data));
				String sku = dataJson.getString("sku");
				String changeprice = dataJson.getString("changeprice");
				String promotionInfo4SwmsList = dataJson.getString("promotionInfo4SwmsList");
				List<PromotionEntity> proList = mGson.fromJson(promotionInfo4SwmsList, new TypeToken<List<PromotionEntity>>() {}.getType());
				//显示促销信息
				setPromotionInfo(proList);
				//设置价格规则
				SkuEntity entity = new SkuEntity();
				entity.Price = selectedSku.Price;
				entity.ChangePrice = changeprice;
				showPrice(entity);
				promotionDialog.show();
			} catch (JSONException e) {
				Ex.Toast(mActivity).show("获取促销信息失败");
				LogUtils.logOnFile("解析促销信息数据失败:"+ExceptionUtils.getStackTrace(e));
			}
			break;
		}
	}

	private void setPromotionInfo(List<PromotionEntity> proList) {
		promotionList.clear();
		promotionList.addAll(proList);
		list_promotion.setSelection(0);
		promotionListAdapter.notifyDataSetChanged();
	}

	TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			if (s != null && !s.toString().trim().equals("")) {
				searchCondition = s.toString();
				/* 搜素提示请求 */
				startTask(HotConstants.Global.APP_URL_USER + HotConstants.SKU.GetStoreForSKUFuzzy, WHAT_NET_GET_STOCK_FUZZY, false, NET_METHOD_POST, false);
			} else {
				if (searchSkuList != null && itemListAdapter != null) {
					searchSkuList.clear();
					itemListAdapter.notifyDataSetChanged();
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
		}
	};

	/**
	 * 把获取到的商品信息数据 赋值给控件
	 * 
	 * @param itemEntity
	 */
	private void setSkuData(ItemEntity itemEntity) {
		stock_sku_name.setText(itemEntity.ItemName);
		// ImageUtil.getBitmapByBase64(itemEntity.ItemImage, stock_sku_icon);
		if (stockListAdapter == null) {
			stockListAdapter = new StockListAdapter(this, skulist);
			list_sku.setAdapter(stockListAdapter);
			list_sku.setOnItemClickListener(this);
		} else {
			stockListAdapter.notifyDataSetChanged();
		}
		list_sku.setSelection(HotConstants.SELECTED);
		if (itemEntity.IsSomeSampling) {// 鞋子
			get_the_other.setVisibility(View.VISIBLE);
		} else {
			get_the_other.setVisibility(View.GONE);
		}

		showPrice(selectedSku);
	}
	
	// 显示当前商品价格
	private void showPrice(SkuEntity selectedSku) {
		stock_sku_price.setText("￥" + SkuUtil.formatPrice(selectedSku.Price)); 
		if(StringUtils.isNoneBlank(selectedSku.nextSalePrice)){
			nextSalePrice.setText("￥" + selectedSku.nextSalePrice);
		}
		nextSalePriceDate.setText(selectedSku.nextSaleDate);
		if (StringUtils.isNoneBlank(selectedSku.ChangePrice)) {
			if (!selectedSku.ChangePrice.equals(selectedSku.Price)) {
				stock_sku_change_price.setText(SkuUtil.formatPrice(selectedSku.ChangePrice));
				stock_sku_price.setTextColor(getResources().getColor(R.color.color_gray_text));
				stock_sku_price.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG); // 设置中划线并加清晰
				stock_sku_price.setTextSize(12);
			} else {
				stock_sku_change_price.setText("");
				stock_sku_price.setTextSize(14);
				stock_sku_price.setTextColor(getResources().getColor(R.color.color_green));
				stock_sku_price.getPaint().setFlags(0); // 取消设置的的划线
			}
		} else {
			stock_sku_change_price.setText("");
			stock_sku_price.setTextSize(14);
			stock_sku_price.setTextColor(getResources().getColor(R.color.color_green));
			stock_sku_price.getPaint().setFlags(0); // 取消设置的的划线
		}
	}

	@Override
	public void onItemClick(AdapterView<?> listview, View view, int position, long id) {
		if (listview == list_sku) {
			
			HotConstants.SELECTED = position;
			stockListAdapter.notifyDataSetChanged();
			selectedSku = skulist.get(position);
			
			skuCodeStr = selectedSku.SKUCode;
			/* 根据skuCode查询商品信息 */
			startTask(HotConstants.Global.APP_URL_USER + HotConstants.SKU.GetStoreForSKU, WHAT_NET_GET_STOCK, NET_METHOD_POST, false);
			
			//小程序码
			JSONObject data = new JSONObject();
			try {
				data.put("storeId", HotApplication.getInstance().getUnitId());
				data.put("skuId", selectedSku.SKUCode);
			} catch (JSONException e) {
				Ex.Toast(mActivity).show("获取E3库存失败");
				LogUtils.logOnFile("获取E3库存失败:"+ExceptionUtils.getStackTrace(e));
			}
			//库存没货查询E3
			//1001899 直接查询E3
			//LogUtils.logOnFile("查询E3库存");
			startJsonTask(HotConstants.Global.E3_STOCK_QUERY_INFO, WHAT_NET_GET_E3_STOCK, true, NET_METHOD_POST, data.toString(), false);
			
			
			showPrice(selectedSku);
			setValue();
			String url = null;
			if (selectedSku.ColorID == null || selectedSku.ColorID.equals("")) {
				url = HotConstants.Global.APP_URL_USER + "ItemImgs/" + selectedSku.SKUCode + ".jpg";
			} else {
				url = HotConstants.Global.APP_URL_USER + "ItemImgs/" + itemEntity.ItemID + "$" + selectedSku.ColorID + ".jpg";
			}
			ImageUtil.uploadImage(this, url, stock_sku_icon);
		} else if (listview == searchListView) {
			searchSkuBean = searchSkuList.get(position);
			skuCodeStr = searchSkuBean.SKUCode;
			startTask(HotConstants.Global.APP_URL_USER + HotConstants.SKU.GetStoreForSKU, WHAT_NET_GET_STOCK, NET_METHOD_POST, false);
		}
	}

	private void setValue() {
		stock_sku_code.setText(selectedSku.SKUCode);
		stock_sku_num.setText(selectedSku.SKUCount);
		stock_sku_stock.setText(selectedSku.StockSKUShelfLocationString);
		skuCodeStr = selectedSku.SKUCode;
		// skuInfo = "商品编码:  " + selectedSku.SKUCode + "\n\r" + "颜色:  " +
		// selectedSku.ColorName + "\n\r"
		// + "尺寸:  " + selectedSku.SizeName;
		if (!itemEntity.AttDId.equals("3")) {
			if (selectedSku.IsHasSample) {// 取另一只
				get_the_other.setEnabled(true);
				get_the_other.setClickable(true);
			} else {
				get_the_other.setEnabled(false);
				get_the_other.setClickable(false);
			}
			if (selectedSku.SKUCount.equals("0")) {
				get_new.setEnabled(false);
				get_new.setClickable(false);
				get_the_sample.setEnabled(false);
				get_the_sample.setClickable(false);
			} else {
				get_new.setEnabled(true);
				get_new.setClickable(true);
				get_the_sample.setEnabled(true);
				get_the_sample.setClickable(true);
			}
		}
	}

	/**
	 * 显示搜索界面
	 */
	private void showSearchView() {
		showSearch = true;
		// check_list_layout.setBackgroundResource(R.drawable.common_img_mask);
		check_list_layout.setVisibility(View.VISIBLE);
		rotateyAnimRun(checkSearchLayout, 1f, 0f, 500);// 从有到无
		verticalRun(checkSearchLayout, 0, -check_stock_title.getHeight(), 500);// 往上位移
		popSoftKeyBoard(this, searchEditText);
		searchEditText.requestFocus();
	}

	/**
	 * 隐藏搜索界面
	 */
	private void hideSearchView() {
		showSearch = false;
		check_list_layout.setVisibility(View.GONE);
		rotateyAnimRun(checkSearchLayout, 0f, 1f, 500);// 从无到有
		verticalRun(checkSearchLayout, -check_stock_title.getHeight(), 0, 500);// 往下位移
		hideSoftKeyBoard(this, searchEditText);
	}

	/**
	 * 点击出样按钮显示的列表
	 */
	private ArrayList<SampleEntity> getSampleList(ItemEntity item, SkuEntity sku, boolean isGetOther) {
		ArrayList<SampleEntity> arraylist = new ArrayList<SampleEntity>();
		if (isGetOther) {// 取另一只
			if (sku.SampleList == null)
				return arraylist;
			for (SampleEntity sam : sku.SampleList) {
				SampleEntity three = sam.copy(sam);
				three.isCanBatch = false;
				if (sam.EndQty.equals("1"))
					three.Label = "空鞋盒";
				else
					three.Label = "鞋盒(一只)";
				three.Type = "1";
				arraylist.add(three);
			}
			return arraylist;
		} else {// 出样，撤样，换样
			if (item.IsSomeSampling) {// 是鞋子
				SampleEntity one = new SampleEntity();
				one.isCanBatch = false;
				one.Label = "出样:  一双";
				one.EndQty = "1";
				one.Type = "2";
				SampleEntity two = new SampleEntity();
				two.isCanBatch = false;
				two.Label = "出样:  一只";
				two.EndQty = "0.5";
				two.Type = "2";
				arraylist.add(one);
				arraylist.add(two);
				if (sku.IsHasSample) {// 出样
					if (sku.SampleList.size() == 1) {
						for (SampleEntity sam : sku.SampleList) {
							SampleEntity three = sam.copy(sam);
							SampleEntity four = sam.copy(sam);
							three.isCanBatch = false;
							three.Label = "换样:  " + sam.Label;
							three.Type = "3";
							four.isCanBatch = false;
							four.Label = "撤样:  " + sam.Label;
							four.Type = "4";
							arraylist.add(three);
							arraylist.add(four);
						}
					} else {
						SampleEntity three = sku.SampleList.get(0).copy(sku.SampleList.get(0));
						SampleEntity four = sku.SampleList.get(1).copy(sku.SampleList.get(1));
						three.isCanBatch = false;
						three.Label = "换样:  " + sku.SampleList.get(0).Label;
						three.Type = "3";
						four.isCanBatch = false;
						four.Label = "换样:  " + sku.SampleList.get(1).Label;
						four.Type = "3";
						arraylist.add(three);
						arraylist.add(four);
						SampleEntity five = sku.SampleList.get(0).copy(sku.SampleList.get(0));
						SampleEntity six = sku.SampleList.get(1).copy(sku.SampleList.get(1));
						five.isCanBatch = false;
						five.Label = "撤样:  " + sku.SampleList.get(0).Label;
						five.Type = "4";
						six.isCanBatch = false;
						six.Label = "撤样:  " + sku.SampleList.get(1).Label;
						six.Type = "4";
						arraylist.add(five);
						arraylist.add(six);
					}
				}
			} else {
				SampleEntity one = new SampleEntity();
				one.isCanBatch = true;
				// one.Label = "出样:  一" + item.ItemUnit;
				one.Label = "出样";
				one.EndQty = "1";
				one.Type = "2";
				arraylist.add(one);
				if (sku.IsHasSample) {// 出样
					if (sku.SampleList.size() > 0) {
						for (SampleEntity sam : sku.SampleList) {
							SampleEntity three = sam.copy(sam);
							SampleEntity four = sam.copy(sam);
							three.isCanBatch = false;
							// three.Label = "换样:  " + sam.Label;
							three.Label = "换样";
							three.Type = "3";
							four.isCanBatch = true;
							// four.Label = "撤样:  " + sam.Label;
							four.Label = "撤样";
							four.Type = "4";
							arraylist.add(three);
							arraylist.add(four);
						}
					}
				}
			}
		}
		return arraylist;
	}

	@Override
	public void onClick(View v) {
		if (FastDoubleClickUtil.isFastDoubleClick()) {
			Log.i("fast", "fast");
			return;
		}
		switch (v.getId()) {
		case R.id.lv_left:
			this.finish();
			break;
		case R.id.lv_right:
			// 日志操作
			LogUtils.logOnFile("搜索");
			showSearchView();
			break;
		case R.id.get_new:
			// 取新
			// 日志操作
			LogUtils.logOnFile("->取新" + skuCodeStr);
			startTask(HotConstants.Global.APP_URL_USER + HotConstants.SKU.GetNew, WHAT_NET_GET_NEW, NET_METHOD_POST, false);
			break;
		case R.id.get_the_other:
			// 取鞋盒
			// 日志操作
			if (selectedSku == null) {
				Ex.Toast(mActivity).show("请选择一条记录");
				return;
			}
			LogUtils.logOnFile("->取鞋盒" + skuCodeStr);
			Bundle bundle = new Bundle();
			bundle.putString("skuCodeStr", skuCodeStr);
			bundle.putSerializable("sku", selectedSku);
			bundle.putSerializable("sampleList", getSampleList(itemEntity, selectedSku, true));
			Intent intent = new Intent(CheckStockActivity.this, SomeSampleDialog.class);
			intent.putExtra("bundle", bundle);
			startActivity(intent);
			break;
		case R.id.get_the_sample:
			// 出样
			if (selectedSku == null) {
				Ex.Toast(mActivity).show("此商品暂无库存");
				return;
			}
			if (selectedSku.SKUCount.equals("0")) {
				Ex.Toast(mActivity).show("此商品暂无库存");
				return;
			}
			Bundle bundle1 = new Bundle();
			bundle1.putString("skuCodeStr", skuCodeStr);
			// bundle1.putString("skuInfo", skuInfo);
			bundle1.putSerializable("sku", selectedSku);
			bundle1.putSerializable("sampleList", getSampleList(itemEntity, selectedSku, false));
			Intent intent1 = new Intent(CheckStockActivity.this, SomeSampleDialog.class);
			intent1.putExtra("bundle", bundle1);
			startActivity(intent1);
			break;
		case R.id.check_stock_search_layout:
			showSearchView();
			break;
		case R.id.check_storck_cancel:
			hideSearchView();
			break;
		case R.id.stock_sku_stock:
			if (selectedSku.StockSKUShelfLocationString.length() > 1) {
				getPopupWindow();
				// 这里是位置显示方式,在屏幕的左侧
				popupWindow.showAtLocation(v, Gravity.CENTER, 0, -6);
			}
			break;
		}

	}

	PopupWindow popupWindow;

	/**
	 * 创建PopupWindow
	 */
	protected void initPopuptWindow() {
		// 获取自定义布局文件activity_popupwindow_left.xml的视图
		View popupWindow_view = getLayoutInflater().inflate(R.layout.activity_popupwindow_left, null, false);
		TextView txt = (TextView) popupWindow_view.findViewById(R.id.pop_txt);
		txt.setText(selectedSku.StockSKUShelfLocationString);
		// 创建PopupWindow实例,200,LayoutParams.MATCH_PARENT分别是宽度和高度
		popupWindow = new PopupWindow(popupWindow_view, ConstValue.SCREEN_WIDTH - 24, LayoutParams.WRAP_CONTENT, true);
		// 设置动画效果
		popupWindow.setAnimationStyle(R.style.AnimationFade);
		// 点击其他地方消失
		popupWindow_view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (popupWindow != null && popupWindow.isShowing()) {
					popupWindow.dismiss();
					popupWindow = null;
				}
				return false;
			}
		});
	}

	/***
	 * 获取PopupWindow实例
	 */
	private void getPopupWindow() {
		if (null != popupWindow) {
			popupWindow.dismiss();
			return;
		} else {
			initPopuptWindow();
		}
	}

	OnKeyListener onKeyListener = new OnKeyListener() {// 输入完后按键盘上的搜索键【回车键改为了搜索键】

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {// 修改回车键功能
				if (searchEditText.getText() == null || searchEditText.getText().toString().equals("")) {
					return false;
				}
				searchCondition = searchEditText.getText().toString();
				startTask(HotConstants.Global.APP_URL_USER + HotConstants.SKU.GetStoreForSKUFuzzy, WHAT_NET_GET_STOCK_FUZZY, false, NET_METHOD_POST, false);
			}
			return false;
		}
	};

	/**
	 * 缩放动画
	 * 
	 * @param view
	 * @param start
	 * @param end
	 * @param duration
	 */
	public void rotateyAnimRun(final View view, float start, float end, long duration) {
		ObjectAnimator anim = ObjectAnimator//
				.ofFloat(view, "zhy", start, end)//
				.setDuration(duration);//
		anim.start();
		anim.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float cVal = (Float) animation.getAnimatedValue();
				view.setAlpha(cVal);
				view.setScaleX(cVal);
				view.setScaleY(cVal);
			}
		});
	}

	/**
	 * 位移动画
	 * 
	 * @param view
	 * @param start
	 * @param end
	 * @param duration
	 */
	public void verticalRun(View view, float start, float end, long duration) {
		ValueAnimator animator = ValueAnimator.ofFloat(start, end);
		animator.setTarget(frame_layout);
		animator.setDuration(duration).start();
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				frame_layout.setTranslationY((Float) animation.getAnimatedValue());
			}
		});
	}

	/**
	 * 弹出软键盘
	 * 
	 * @param context
	 * @param view
	 */
	public void popSoftKeyBoard(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, InputMethodManager.RESULT_SHOWN);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	}

	/**
	 * 隐藏软键盘
	 */
	public void hideSoftKeyBoard(Context context, View editText) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	@Override
	public void fillCode(String code) {
		this.skuCodeStr = code;
		if (code != null) {
			dealBarCode(code);
			// 日志操作
			LogUtils.logOnFile("扫码->" + code);
		} else {
			ap.getsoundPool(ap.Sound_error);
			return;
		}
	}
}
