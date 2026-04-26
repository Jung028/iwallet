package com.alipay.business.common.service.facade.baseresult;

/**
 * @author adam
 * @date 15/3/2026 12:34 AM
 */
public class BusinessBasePageRequest extends BusinessBaseRequest {
    private int pageNo = 1;
    private int pageSize = 10;

    public int getPageNo() { return pageNo; }
    public void setPageNo(int pageNo) { this.pageNo = pageNo; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    // calculated offset, no need to set manually
    public int getOffset() { return (pageNo - 1) * pageSize; }
}