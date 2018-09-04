package cn.jitmarketing.hot.view.sortlistview;

import java.util.Comparator;

import cn.jitmarketing.hot.entity.StockTakingShopownerEntity;

/**
 * 
 * @author liushang1
 *
 */
public class ESOComparator implements Comparator<StockTakingShopownerEntity> {

	public int compare(StockTakingShopownerEntity lhs, StockTakingShopownerEntity rhs) {
		String llocationCode = lhs.ShelfLocationCode;
		String rlocationCode = rhs.ShelfLocationCode;
		if((llocationCode.startsWith("E") || llocationCode.startsWith("S") || llocationCode.startsWith("O")) &&
				(rlocationCode.startsWith("E") || rlocationCode.startsWith("S") || rlocationCode.startsWith("O"))){
			if(llocationCode.startsWith("O") && (rlocationCode.startsWith("S") || rlocationCode.startsWith("E"))){
				return 1;
			}else if(llocationCode.startsWith("S") && rlocationCode.startsWith("O")){
				return -1;
			}else if(llocationCode.startsWith("S")){
				return 1;
			}else{
				return -1;
			}
		}else{
			return 1;
		}
	}

}
