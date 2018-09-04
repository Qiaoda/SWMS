package cn.jitmarketing.hot.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * 对门店来说，发货单在门店入货
 */
public class ReceiveSheet implements Serializable{

	@SerializedName(value="ReceiveSheetNo")
	public String receiveSheetNo;
	@SerializedName(value="LinkedOrderNo")
	public String linkedOrderNo;
	@SerializedName(value="OrderType")
	public String orderType;
	@SerializedName(value="CreateTime")
	public String createTime;
	@SerializedName(value="LastUpdateTime")
	public String updateTime;
	@SerializedName(value="Status")
	public int status;
	@SerializedName(value="SKUQty")
	public float skuQty;
//	@SerializedName(value="detailList")
//	public ArrayList<InStockSku> detailList;
	public int ResultStatus;
	@SerializedName(value="DisOrderNo")
	public String DisOrderNo;
	@SerializedName(value="OrderNo")
	public String OrderNo;
	@SerializedName(value="FromStoreName")
	public String FromStoreName;
	@SerializedName(value="FromRemark")
	public String FromRemark;
	@SerializedName(value="Qry")
	public String Qry;
	@SerializedName(value="DiffQry")
	public String DiffQry;
	@SerializedName(value="ConfirmTime")
	public String ConfirmTime;
	@SerializedName(value="SkuidList")
	public ArrayList<Diff> SkuidList; 
	
}

