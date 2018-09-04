package cn.jitmarketing.hot.entity;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public class Diff implements Serializable{
	@SerializedName(value="skuid")
	public String skuid;
	@SerializedName(value="SkuQry")
	public int SkuQry;
	@SerializedName(value="SkuDiffQry")
	public int SkuDiffQry;
	@SerializedName(value="ConfirmQry")
	public int ConfirmQry;
}
