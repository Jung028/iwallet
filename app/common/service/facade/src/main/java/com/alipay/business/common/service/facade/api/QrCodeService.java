package com.alipay.business.common.service.facade.api;

import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.request.GenerateQrCodeRequest;
import com.alipay.business.common.service.facade.result.GenerateQrCodeResult;

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
    BusinessBizResult<GenerateQrCodeResult> generateQrCode(GenerateQrCodeRequest request);
}