package com.alipay.business.biz.service.impl.util;

import com.alipay.business.common.service.facade.enums.ReceiptItemStatus;
import com.alipay.business.core.model.domain.Receipt;
import com.alipay.business.core.model.domain.ReceiptItemDomain;
import com.alipay.business.core.service.ReceiptItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author adam
 * @date 13/7/2026 11:51 PM
 */
@Component
public class CalculateTotalPaidUtil {

    @Autowired
    private ReceiptItemRepository receiptItemRepository;

    public double calculateTotalPaid(Receipt receipt) {
        List<ReceiptItemDomain> allItems =
                receiptItemRepository.queryReceiptItemsByReceiptId(
                        receipt.getReceiptId().toString()
                );

        return allItems.stream()
                .filter(item -> ReceiptItemStatus.PAID.getCode().equals(item.getStatus()))
                .map(item -> {
                    BigDecimal price = item.getTotalPrice() != null
                            ? item.getTotalPrice()
                            : BigDecimal.ZERO;

                    BigDecimal tax = item.getTotalTaxAmount() != null
                            ? item.getTotalTaxAmount()
                            : BigDecimal.ZERO;

                    return price.add(tax);
                })
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
    }

}