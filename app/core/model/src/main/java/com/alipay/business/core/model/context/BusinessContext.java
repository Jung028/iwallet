package com.alipay.business.core.model.context;


import com.alipay.business.core.model.enums.BusinessActionEnum;

import java.io.Serializable;
import java.util.Date;

public class BusinessContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private Date time;
    private BusinessActionEnum action;
    private String operatorId;
    private String operatorName;


    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public BusinessActionEnum getAction() {
        return action;
    }

    public void setAction(BusinessActionEnum action) {
        this.action = action;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public BusinessContext(BusinessActionEnum action, Date time, String operatorId, String operatorName) {
        this.action = action;
        this.time = time;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
    }
}
