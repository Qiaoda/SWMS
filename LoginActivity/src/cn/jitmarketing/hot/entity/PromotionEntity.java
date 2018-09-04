package cn.jitmarketing.hot.entity;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class PromotionEntity implements Serializable{
	
	//promotionInfo4SwmsList
	@SerializedName(value = "promotionName")
	public String promotionName;
	@SerializedName(value = "startDate")
	public String startDate;
	@SerializedName(value = "endDate")
	public String endDate;
	@SerializedName(value = "startTime")
	public String startTime;
	@SerializedName(value = "endTime")
	public String endTime;

}
