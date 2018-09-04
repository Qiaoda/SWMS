package cn.jitmarketing.hot.entity;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class OtherShopStockEntity implements Serializable{

	
	private static final long serialVersionUID = 1930577754036777949L;
	
	@SerializedName("Type")
	public int Type;
	@SerializedName("SkuCode")
	public String SkuCode;
	@SerializedName("StoreID")
	public String StoreID;
	@SerializedName("StoreName")
	public String StoreName;
	@SerializedName("Qry")
	public String Qry;

}
