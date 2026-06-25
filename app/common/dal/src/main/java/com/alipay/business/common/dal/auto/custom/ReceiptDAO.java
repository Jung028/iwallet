package com.alipay.business.common.dal.auto.custom;

import com.alipay.business.common.dal.auto.dataobject.ReceiptDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReceiptDAO {

    int insertReceipt(ReceiptDO receiptDO);
}
