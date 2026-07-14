package com.alipay.business.common.dal.auto.custom;

import com.alipay.business.common.dal.auto.dataobject.ReceiptDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ReceiptDAO {

    int insertReceipt(ReceiptDO receiptDO);

    List<ReceiptDO> queryReceiptsHistory(@Param("userId") String userId,
                                         @Param("receiptId") String receiptId,
                                         @Param("pageSize") int pageSize,
                                         @Param("pageNo") int pageNo);

    ReceiptDO queryReceiptByReceiptId(@Param("receiptId") String receiptId);

    int updateReceipt(@Param("receiptId") String receiptId,
                      @Param("totalAmountPaid") BigDecimal totalAmountPaid,
                      @Param("status") String status);

    int updateReceiptReferenceId(@Param("receiptId") String receiptId,
                                 @Param("referenceId") String referenceId);
}
