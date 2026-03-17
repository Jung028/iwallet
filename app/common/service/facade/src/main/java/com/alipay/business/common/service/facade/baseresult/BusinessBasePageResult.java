package com.alipay.business.common.service.facade.baseresult;

/**
 * @author adam
 * @date 15/3/2026 12:49 AM
 */
public class BusinessBasePageResult extends BusinessBaseResult {
    private int totalCount;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}