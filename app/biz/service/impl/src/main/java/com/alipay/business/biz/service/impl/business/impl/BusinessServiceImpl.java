package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.alipay_plus.common.service.facade.baseresult.AccountBizResult;
import com.alipay.alipay_plus.common.service.facade.item.AccountInfoItem;
import com.alipay.alipay_plus.common.service.facade.request.QueryAccountInfoRequest;
import com.alipay.business.biz.service.impl.checker.BusinessRequestChecker;
import com.alipay.business.biz.service.impl.template.BusinessBizCallback;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.money.MoneyUtil;
import com.alipay.business.common.service.facade.request.QueryBalanceRequest;
import com.alipay.business.common.service.facade.request.QueryTransactionDetailsRequest;
import com.alipay.business.common.service.facade.request.QueryTransactionHistoryRequest;
import com.alipay.business.common.service.facade.request.TransferRequest;
import com.alipay.business.common.service.facade.result.QueryBalanceResult;
import com.alipay.business.common.service.facade.result.QueryTransactionDetailsResult;
import com.alipay.business.common.service.facade.result.QueryTransactionHistoryResult;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import com.alipay.business.core.model.enums.BusinessActionEnum;
import com.alipay.business.core.model.exception.BusinessException;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.usercenter.common.service.facade.request.OTPRequest;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Currency;

/**
 * Business service impl
 */
public class BusinessServiceImpl extends AbstractBusinessBizService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessServiceImpl.class);

    private static final MonetaryAmount LIMIT =
            Money.of(new BigDecimal("200.00"), "MYR");

    @Override
    public BusinessBizResult<String> transfer(TransferRequest request, String userId) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.QUERY_TRANSACTION_DETAILS,
                new BusinessBizCallback<>() {
                    @Override
                    protected BusinessBizResult<String> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(TransferRequest request) {
                        BusinessRequestChecker.checkTransferRequest(request);
                    }

                    @Override
                    protected void process(TransferRequest request, BusinessBizResult<String> response) {

                        IdempotencyKeys existing = checkOrInsertIdempotencyKey(request);

                        if (existing != null) {
                            // Replay stored response
                            response.setResult(existing.getResponseSnapshot());
                            return;
                        }

                        // query account info from account
                        QueryAccountInfoRequest queryAccountInfoRequest = new QueryAccountInfoRequest();
                        queryAccountInfoRequest.setAccountId(request.getPayeeAccountNo());
                        AccountBizResult<AccountInfoItem> payeeAccountInfo = accountServiceClient.queryAccountInfo(queryAccountInfoRequest);
                        queryAccountInfoRequest.setAccountId(request.getPayerAccountNo());
                        AccountBizResult<AccountInfoItem> payerAccountInfo = accountServiceClient.queryAccountInfo(queryAccountInfoRequest);

                        // validate both accounts exists
                        AssertUtil.notNull(payerAccountInfo, BusinessResultCode.ACCOUNT_NOT_FOUND, "payer account not found");
                        AssertUtil.notNull(payeeAccountInfo, BusinessResultCode.ACCOUNT_NOT_FOUND, "payee account not found");

                        // verify user is authorised
                        AssertUtil.isTrue(payerAccountInfo.getResult().getAccRelationId().equals(userId),
                                BusinessResultCode.INVALID_REQUEST, "User is not authorised");

                        // validate balance is sufficient
                        CurrencyUnit payerAccountCurrent = Monetary.getCurrency(payerAccountInfo.getResult().getCurrency());
                        MonetaryAmount payerBalance = MoneyUtil.toMonetaryAmount(payerAccountInfo.getResult().getBalance(), payerAccountCurrent);
                        MonetaryAmount requestAmount = MoneyUtil.toMonetaryAmount(request.getAmount().getAmount(), request.getAmount().getCurrency());
                        if (payerBalance.isLessThan(requestAmount)){
                            throw new RuntimeException("insufficient balance");
                        }
                        // publish EC_TRANSACTION event code for transfer service to listen

                    }
                });
    }

    private IdempotencyKeys checkOrInsertIdempotencyKey(TransferRequest request) {
        // insert or return idempotent record,
        try {
            idempotencyKeysRepository.insertIdempotencyKey(request.getUniqueRequestId(), request.getPayerAccountNo());
        } catch (DuplicateKeyException e) {
            // if the record is valid, return duplicate request result
            IdempotencyKeys idempotencyKeys = idempotencyKeysRepository.queryIdempotencyKeys(request.getUniqueRequestId());
            if (idempotencyKeys == null) {
                throw new IllegalStateException("Idempotency record missing");
            }
            switch (idempotencyKeys.getStatus()) {
                case PENDING:
                    throw new BusinessException(BusinessResultCode.REPEATED_SUBMIT, "Request is processing");
                case SUCCESS, FAILED:
                    return idempotencyKeys;
                default:
                    throw new IllegalStateException("Unknown status");
            }
        }
        return null;
    }


    @Override
    public BusinessBizResult<QueryTransactionDetailsResult> queryTransactionDetails(QueryTransactionDetailsRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.QUERY_TRANSACTION_DETAILS,
                new BusinessBizCallback<>() {
                    @Override
                    protected BusinessBizResult<QueryTransactionDetailsResult> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(QueryTransactionDetailsRequest request) {

                    }

                    @Override
                    protected void process(QueryTransactionDetailsRequest request, BusinessBizResult<QueryTransactionDetailsResult> response) {

                    }
                });

    }

    @Override
    public BusinessBizResult<QueryTransactionHistoryResult> queryTransactionHistory(QueryTransactionHistoryRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.QUERY_TRANSACTION_HISTORY,
                new BusinessBizCallback<>() {
                    @Override
                    protected BusinessBizResult<QueryTransactionHistoryResult> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(QueryTransactionHistoryRequest request) {
                        BusinessRequestChecker.checkQueryTransactionHistoryRequest(request);
                    }

                    @Override
                    protected void process(QueryTransactionHistoryRequest request,
                                           BusinessBizResult<QueryTransactionHistoryResult> response) {

                    }
                });
    }

    @Override
    public BusinessBizResult<QueryBalanceResult> queryBalance(QueryBalanceRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.QUERY_BALANCE, new BusinessBizCallback<>() {

            @Override
            protected BusinessBizResult<QueryBalanceResult> createDefaultResponse() {
                return null;
            }

            @Override
            protected void checkParams(QueryBalanceRequest request) {

            }

            @Override
            protected void process(QueryBalanceRequest request, BusinessBizResult<QueryBalanceResult> response) {

            }
        });
    }
}



