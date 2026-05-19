package com.alipay.business.common.dal.auto.custom;

import com.alipay.business.common.dal.auto.dataobject.QrCodeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author adam
 * @date 24/4/2026 12:40 AM
 */
@Mapper
public interface QrCodeDAO {

    int insertQrCode(QrCodeDO qrCodeDO);

    int toggleQrCode(@Param("qrId") String qrId, @Param("toggleQr") boolean toggleQr);

    List<QrCodeDO> queryQrCodes(@Param("merchantId") String merchantId,
                                @Param("pageSize") int pageSize,
                                @Param("offset") int offset);

    int countQrCodes(@Param("merchantId") String merchantId);
}