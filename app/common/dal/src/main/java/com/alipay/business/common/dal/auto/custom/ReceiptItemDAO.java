package com.alipay.business.common.dal.auto.custom;

import com.alipay.business.common.dal.auto.dataobject.ReceiptItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReceiptItemDAO {

    int insertItemReceipt(ReceiptItemDO receiptItemDO);

    List<ReceiptItemDO> queryReceiptItemsByReceiptId(@Param("receiptId") String receiptId);

    ReceiptItemDO lockReceiptItemByItemId(@Param("itemId") String itemId);

    int updateReceiptItem(@Param("receiptItemId") String receiptItemId,
                          @Param("itemStatus") String itemStatus,
                          @Param("selectedBy") String selectedBy,
                          @Param("updatedAt") java.util.Date updatedAt);
}
