package com.alipay.business.core.model.exception;


import com.alipay.business.common.service.facade.enums.ResultCode;

public class BusinessException extends RuntimeException {

  private static final long seralVersionUID = 9187623791824214L;

  private ResultCode resultCode;

  public BusinessException(ResultCode resultCode, String message) {
    super(message);
    this.resultCode = resultCode;
  }

  public BusinessException(ResultCode resultCode) {
    this(resultCode, resultCode.getDescription());
  }

  public ResultCode getResultCode() {
    return resultCode;
  }

  public void setResultCode(ResultCode resultCode) {
    this.resultCode = resultCode;
  }
}
