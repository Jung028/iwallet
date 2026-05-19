package com.alipay.business.common.service.facade.result;

import com.alipay.business.common.service.facade.baseresult.BusinessBasePageResult;
import com.alipay.business.common.service.facade.item.QrCodeItem;

import java.util.List;

/**
 * @author adam
 * @date 19/5/2026 5:45 PM
 */
public class QueryQrCodesResult extends BusinessBasePageResult {
    private List<QrCodeItem> qrCodes;

    public List<QrCodeItem> getQrCodes() { return qrCodes; }
    public void setQrCodes(List<QrCodeItem> qrCodes) { this.qrCodes = qrCodes; }
}
