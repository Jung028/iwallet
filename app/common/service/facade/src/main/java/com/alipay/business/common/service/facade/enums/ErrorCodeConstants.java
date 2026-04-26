package com.alipay.business.common.service.facade.enums;

// This class supposed to be in IPAY, the global result codes used across multiple modules
public interface ErrorCodeConstants {
    String PREFIX = "IPAY_RX_";
    String INFO = "1";
    String WARN = "2";
    String ERROR = "3";
    String FATAL = "4";
    String BIZ = "1";
    String SYS = "2";
    String THIRD_PARTY = "3";
}
