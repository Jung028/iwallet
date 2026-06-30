package com.alipay.business.common.service.facade.api;

import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import com.alipay.business.common.service.facade.request.QueryReceiptsHistoryRequest;
import com.alipay.business.common.service.facade.request.QueryQrCodesRequest;
import com.alipay.business.common.service.facade.request.ToggleQrRequest;
import com.alipay.business.common.service.facade.result.QueryReceiptsHistoryResult;
import com.alipay.business.common.service.facade.result.QueryQrCodesResult;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author adam
 * @date 25/4/2026 2:11 PM
 */
@Path("/qrCodeService")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface QrCodeService {

    /**
     * generate qr code
     * @param request
     * @return
     */
    BusinessBizResult<String> generateQrCode(GenerateQrCodeRequest request);

    /**
     * query merchant qrds
     * @param request
     * @return
     */
    BusinessBizResult<QueryQrCodesResult> queryQrCodes(QueryQrCodesRequest request);

    /**
     * disable qr
     * @param request
     * @return
     */
    BusinessBizResult<String> toggleQrCode(ToggleQrRequest request);

    /**
     * queryReceiptsHistory
     * @param request
     * @return
     */
    BusinessBizResult<QueryReceiptsHistoryResult> queryReceiptsHistory(QueryReceiptsHistoryRequest request);

}