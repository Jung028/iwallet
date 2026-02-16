package com.alipay.business.core.model.exception;


import com.alipay.business.common.service.facade.enums.BusinessResultCode;

public class BusinessException extends RuntimeException {

  private static final long seralVersionUID = 9187623791824214L;

  private BusinessResultCode resultCode;

  public BusinessException(BusinessResultCode resultCode, String message) {
    super(message);
    this.resultCode = resultCode;
  }

  public BusinessException(BusinessResultCode resultCode) {
    this(resultCode, resultCode.getDescription());
  }

  public BusinessResultCode getResultCode() {
    return resultCode;
  }

  public void setResultCode(BusinessResultCode resultCode) {
    this.resultCode = resultCode;
  }
}
