package cn.jitmarketing.hot.entity;

import java.io.Serializable;
import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

public class AllocationNoticeBean {

	@SerializedName(value="AllocationBatchNo")
	public String allocationBatchNo;
	@SerializedName(value="AllocationOrderCode")
	public String allocationOrderCode;
	@SerializedName(value="ToUnitID")
	public String toUnitID;
	@SerializedName(value="ToUnitName")
	public String toUnitName;
	@SerializedName(value="StartDateTime")
	public String startDateTime;
	@SerializedName(value="EndDateTime")
	public String endDateTime;
	@SerializedName(value="Status")
	public int Status;
	@SerializedName(value="Qty")
	public int qty;
	@SerializedName(value="LeftQty")
	public int leftQty;
	@SerializedName(value="CreateTime")
	public String createTime;
	@SerializedName(value="LastUpdatetime")
	public String lastUpdatetime;
	
}
