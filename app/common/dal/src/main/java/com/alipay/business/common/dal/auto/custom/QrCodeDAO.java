package com.alipay.business.common.dal.auto.custom;

import com.alipay.business.common.dal.auto.dataobject.QrCodeDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author adam
 * @date 24/4/2026 12:40 AM
 */
@Mapper
public interface QrCodeDAO {

    int insertQrCode(QrCodeDO qrCodeDO);
}