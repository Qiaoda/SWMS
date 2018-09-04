package cn.jitmarketing.hot.entity;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class SingleSkuEntity implements Serializable{
	
	@SerializedName(value = "sku")
	public String sku;
	@SerializedName(value = "changeprice")
	public String changeprice;
	@SerializedName(value = "enable")
	public String enable;
	@SerializedName(value = "promotionInfo4SwmsList")
	public List<PromotionEntity> promotionInfo4SwmsList;
}
