package com.alipay.business.common.dal.auto.custom;

import com.alipay.business.common.dal.auto.dataobject.TransactionReceiptItemRelDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface TransactionReceiptItemRelDAO {
    int insertTransactionReceiptItemRel(TransactionReceiptItemRelDO transactionReceiptItemRel);

    List<TransactionReceiptItemRelDO> queryTransactionReceiptItemRel(@Param("transactionId") String transactionId);

    Integer queryTotalPaidQuantityByReceiptItemId(@Param("receiptItemId") UUID receiptItemId);
}
