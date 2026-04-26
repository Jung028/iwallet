package com.alipay.business.common.service.facade.enums;

// This class supposed to be in IPAY, the global result codes used across multiple modules
public class GlobalResultCodes {
    static String EXECUTE_SUCCESS = "IPAY_RS_100000200";
    String IDEMPOTENT_SUCCESS = "IPAY_RS_100000201";
    static String SYSTEM_EXCEPTION = "IPAY_RS_200000000";
    static String PARAM_ILLEGAL = "IPAY_RS_200000100";
    static String REPEATED_SUBMIT = "IPAY_RS_200000101";
    String TNT_INST_ID_IS_NULL = "IPAY_RS_200000102";
    String TNT_INST_ID_NOT_MATCH = "IPAY_RS_200000103";
    String IDEMPOTENT_FAIL = "IPAY_RS_200000104";
    String NOT_CURRENT_REGION_DATA = "IPAY_RS_200000105";
}
