package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.alipay_plus.common.service.facade.baseresult.AccountBizResult;
import com.alipay.alipay_plus.common.service.facade.enums.TransactionStatusEnum;
import com.alipay.alipay_plus.common.service.facade.event.EcTransactionEvent;
import com.alipay.alipay_plus.common.service.facade.item.AccountInfoItem;
import com.alipay.alipay_plus.common.service.facade.item.TransactionHistoryItem;
import com.alipay.alipay_plus.common.service.facade.item.TransactionRecordItem;
import com.alipay.alipay_plus.common.service.facade.request.*;
import com.alipay.business.biz.service.impl.checker.BusinessRequestChecker;
import com.alipay.business.biz.service.impl.template.BusinessBizCallback;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.item.IdempotencyKeysItem;
import com.alipay.business.common.service.facade.money.MoneyUtil;
import com.alipay.business.common.service.facade.request.*;
import com.alipay.business.common.service.facade.request.TransferRequest;
import com.alipay.business.common.service.facade.result.*;
import com.alipay.business.core.model.converter.ItemConverter;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import com.alipay.business.core.model.enums.BusinessActionEnum;
import com.alipay.business.core.model.exception.BusinessException;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.item.UserInfoItem;
import com.alipay.usercenter.common.service.facade.request.OTPRequest;
import com.alipay.usercenter.common.service.facade.request.QueryUserInfoRequest;
import com.alipay.usercenter.common.service.facade.request.VerifyOtpRequest;
import io.jsonwebtoken.lang.Assert;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.core.KafkaTemplate;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Business service impl
 */
public class BusinessServiceImpl extends AbstractBusinessBizService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessServiceImpl.class);

    private KafkaTemplate<String, Object> kafkaTemplate;

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

                        // check payer != payee
                        AssertUtil.isTrue(request.getPayeeAccountNo().equals(request.getPayerAccountNo()), BusinessResultCode.PARAM_ILLEGAL.getCode(), "Cannot send to same account");

                        // query account info from account
                        QueryAccountInfoRequest queryAccountInfoRequest = new QueryAccountInfoRequest();
                        queryAccountInfoRequest.setAccountId(request.getPayeeAccountNo());
                        AccountBizResult<AccountInfoItem> payeeAccountInfo = accountServiceClient.queryAccountInfo(queryAccountInfoRequest);
                        queryAccountInfoRequest.setAccountId(request.getPayerAccountNo());
                        AccountBizResult<AccountInfoItem> payerAccountInfo = accountServiceClient.queryAccountInfo(queryAccountInfoRequest);

                        // validate both accounts exists
                        AssertUtil.notNull(payerAccountInfo, BusinessResultCode.ACCOUNT_NOT_FOUND.getCode(), "payer account not found");
                        AssertUtil.notNull(payeeAccountInfo, BusinessResultCode.ACCOUNT_NOT_FOUND.getCode(), "payee account not found");

                        // verify user is authorised
                        AssertUtil.isTrue(payerAccountInfo.getResult().getAccRelationId().equals(userId),
                                BusinessResultCode.INVALID_REQUEST.getCode(), "User is not authorised");

                        // validate balance is sufficient
                        CurrencyUnit payerAccountCurrent = Monetary.getCurrency(payerAccountInfo.getResult().getCurrency());
                        MonetaryAmount payerBalance = MoneyUtil.toMonetaryAmount(payerAccountInfo.getResult().getBalance(), payerAccountCurrent);
                        MonetaryAmount requestAmount = MoneyUtil.toMonetaryAmount(request.getAmount().getAmount(), request.getAmount().getCurrency());
                        if (payerBalance.isLessThan(requestAmount)){
                            throw new RuntimeException("insufficient balance");
                        }

                        InsertTransactionRecordRequest insertTransactionRecordRequest = new InsertTransactionRecordRequest();
                        insertTransactionRecordRequest.setPayeeAccountNo(request.getPayeeAccountNo());
                        insertTransactionRecordRequest.setPayerAccountNo(request.getPayerAccountNo());
                        insertTransactionRecordRequest.setAmount(requestAmount);
                        //check if amount over limit,
                        if (requestAmount.isGreaterThan(LIMIT)) {
                            // query user info to retrieve phone no.
                            QueryUserInfoRequest queryUserInfoRequest = new QueryUserInfoRequest();
                            queryUserInfoRequest.setUserId(request.getOperatorId());
                            UserBizResult<UserInfoItem> userInfoItem = userServiceClient.queryUserInfo(queryUserInfoRequest);
                            // call user center service, send OTP.
                            OTPRequest otpRequest = new OTPRequest();
                            otpRequest.setPhoneNo(userInfoItem.getResult().getPhoneNo());
                            userServiceClient.sendOTP(otpRequest);

                            // set status to OTP_OVER_LIMIT
                            insertTransactionRecordRequest.setStatus(TransactionStatusEnum.OTP_OVER_LIMIT);
                            //insert new record in transaction, status OTP_AMOUNT_OVER_LIMIT
                            AccountBizResult<String> result = accountServiceClient.insertTransactionRecord(insertTransactionRecordRequest);
                            if (result != null && result.getResult() != null) {
                                response.setResult(result.getResult());
                            }
                        } else {
                            insertTransactionRecordRequest.setStatus(TransactionStatusEnum.PENDING);
                            AccountBizResult<String> transactionRecord = accountServiceClient.insertTransactionRecord(insertTransactionRecordRequest);
                            BigDecimal amount = new BigDecimal(requestAmount.getNumber().doubleValue());
                            if (transactionRecord != null && transactionRecord.isSuccess()) {
                                // publish EC_TRANSACTION event code for transfer service to listen
                                EcTransactionEvent event = new EcTransactionEvent(
                                        insertTransactionRecordRequest.getTxnId(),
                                        insertTransactionRecordRequest.getPayeeAccountNo(),
                                        insertTransactionRecordRequest.getPayerAccountNo(),
                                        amount
                                );

                                // Use accountId as key → guarantees ordering per account
                                kafkaTemplate.send("EC_TRANSACTION", String.valueOf(event.getAmount()), event);
                            }
                        }
                    }
                });
    }

    private IdempotencyKeys checkOrInsertIdempotencyKey(TransferRequest request) {
        // insert or return idempotent record,
        try {
            idempotencyKeysRepository.insertIdempotencyKey(request.getUniqueRequestId(), request.getPayerAccountNo());
        } catch (DuplicateKeyException e) {
            // if the record is valid, return duplicate request result
            IdempotencyKeys idempotencyKeys = idempotencyKeysRepository.queryIdempotencyKeysByUniqueRequestId(request.getUniqueRequestId());
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
    public BusinessBizResult<String> transferConfirm(TransferConfirmRequest request, String userId) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.TRANSFER_CONFIRM_OVER_LIMIT,
                new BusinessBizCallback<>() {
                    @Override
                    protected BusinessBizResult<String> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(TransferConfirmRequest request) {
                        BusinessRequestChecker.checkTransferConfirmRequest(request);
                    }

                    @Override
                    protected void process(TransferConfirmRequest request, BusinessBizResult<String> response) {
                        // then in confirm, verifyOTP
                        // call user center first,
                        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest();
                        verifyOtpRequest.setOtp(request.getOtp());
                        verifyOtpRequest.setChallengeId(request.getChallengeId());
                        verifyOtpRequest.setSceneCode(request.getSceneCode());
                        UserBizResult<String> otpResult = userServiceClient.verifyOTP(verifyOtpRequest);

                        UpdateTransactionRecordRequest updateTransactionRecordRequest = new UpdateTransactionRecordRequest();
                        updateTransactionRecordRequest.setTxnId(request.getTxnId());

                        if (otpResult != null && otpResult.isSuccess()) {
                            updateTransactionRecordRequest.setStatus(TransactionStatusEnum.PENDING.getCode());
                            AccountBizResult<TransactionRecordItem> transactionRecord = accountServiceClient
                                    .updateTransactionRecord(updateTransactionRecordRequest);
                            // convert money to big decimal
                            BigDecimal amount = BigDecimal.valueOf(request.getTransferAmount().getAmount().doubleValue())
                                    .setScale(2, RoundingMode.HALF_UP);

                            if (transactionRecord != null && transactionRecord.isSuccess()) {
                                // publish EC_TRANSACTION event code for transfer service to listen
                                EcTransactionEvent event = new EcTransactionEvent(
                                        updateTransactionRecordRequest.getTxnId(),
                                        transactionRecord.getResult().getPayeeAccountId(),
                                        transactionRecord.getResult().getPayerAccountId(),
                                        amount
                                );

                                // Use accountId as key → guarantees ordering per account
                                kafkaTemplate.send("EC_TRANSACTION", event.getPayeeAccountNo(), event);
                            }
                        }
                    }
                });
    }


    @Override
    public BusinessBizResult<BusinessTransactionDetailsResult> queryTransactionDetails(BusinessTransactionRecordRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.QUERY_TRANSACTION_DETAILS,
                new BusinessBizCallback<>() {
                    @Override
                    protected BusinessBizResult<BusinessTransactionDetailsResult> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(BusinessTransactionRecordRequest request) {
                        BusinessRequestChecker.checkQueryTransactionDetailsRequest(request);
                    }

                    @Override
                    protected void process(BusinessTransactionRecordRequest request,
                                           BusinessBizResult<BusinessTransactionDetailsResult> result) {
                        QueryTransactionRecordRequest queryTransactionRecordRequest = new QueryTransactionRecordRequest();
                        queryTransactionRecordRequest.setAccountId(request.getAccountId());
                        queryTransactionRecordRequest.setTxnId(request.getTxnId());
                        AccountBizResult<TransactionRecordItem> accountBizResult = accountServiceClient.queryTransactionRecord(queryTransactionRecordRequest);
                        result.setResult(ItemConverter.convertToTxnDetails(accountBizResult));
                    }
                });

    }


    @Override
    public BusinessBizResult<BusinessTransactionHistoryResult> queryTransactionHistory(BusinessTransactionHistoryRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.QUERY_TRANSACTION_HISTORY,
                new BusinessBizCallback<>() {
                    @Override
                    protected BusinessBizResult<BusinessTransactionHistoryResult> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(BusinessTransactionHistoryRequest request) {
                        BusinessRequestChecker.checkQueryTransactionHistoryRequest(request);
                    }

                    @Override
                    protected void process(BusinessTransactionHistoryRequest request,
                                           BusinessBizResult<BusinessTransactionHistoryResult> response) {
                        QueryTransactionHistoryRequest queryTransactionHistoryRequest = new QueryTransactionHistoryRequest();
                        queryTransactionHistoryRequest.setAccountId(request.getAccountId());
                        queryTransactionHistoryRequest.setTxnId(request.getTxnId());
                        // query transaction history
                        AccountBizResult<List<TransactionHistoryItem>> result = accountServiceClient
                                .queryTransactionHistory(queryTransactionHistoryRequest);
                        //convert to normal before return
                        result.setResult(ItemConverter.convertToTxnHistory(result));
                    }
                });
    }

    @Override
    public BusinessBizResult<BusinessBalanceResult> queryBalance(BusinessBalanceRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.QUERY_BALANCE,
                new BusinessBizCallback<>() {

                    @Override
                    protected BusinessBizResult<BusinessBalanceResult> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(BusinessBalanceRequest request) {
                        BusinessRequestChecker.checkQueryBalanceRequest(request);
                    }

                    @Override
                    protected void process(BusinessBalanceRequest request, BusinessBizResult<BusinessBalanceResult> result) {
                        QueryAccountInfoRequest queryAccountInfoRequest = new QueryAccountInfoRequest();
                        queryAccountInfoRequest.setAccountId(request.getAccountId());
                        AccountBizResult<AccountInfoItem> accountInfo = accountServiceClient.queryAccountInfo(queryAccountInfoRequest);
                        result.setResult(ItemConverter.convertToBalanceResult(accountInfo));
                    }
                });
    }


    @Override
    public BusinessBizResult<UpdateIdempotencyKeysResult> updateIdempotencyKeys(UpdateIdempotencyKeysRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.UPDATE_IDEMPOTENCY_KEYS,
                new BusinessBizCallback<>() {


                    @Override
                    protected BusinessBizResult<UpdateIdempotencyKeysResult> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(UpdateIdempotencyKeysRequest request) {
                        BusinessRequestChecker.checkUpdateIdempotencyKeysRequest(request);
                    }

                    @Override
                    protected void process(UpdateIdempotencyKeysRequest request, BusinessBizResult<UpdateIdempotencyKeysResult> response) {
                        // update idempotency keys status to Error or finished after account center finish debit and credit accounts
                        IdempotencyKeys idempotencyKeys = idempotencyKeysRepository.updateIdempotencyKeys(request.getTxnId(), request.getStatus().getCode(), request.getRetryCount());
                        if(idempotencyKeys != null && idempotencyKeys.getErrorCode() != null) {
                            UpdateIdempotencyKeysResult result = new UpdateIdempotencyKeysResult();
                            result.setTxnId(idempotencyKeys.getTxnId());
                            result.setStatus(idempotencyKeys.getStatus().getCode());
                            response.setResult(result);
                        }
                    }
                });
    }

    @Override
    public BusinessBizResult<IdempotencyKeysItem> queryIdempotencyKeys(QueryIdempotencyKeysRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.QUERY_IDEMPOTENCY_KEYS,
                new BusinessBizCallback<>() {

                    @Override
                    protected BusinessBizResult<IdempotencyKeysItem> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(QueryIdempotencyKeysRequest request) {
                        BusinessRequestChecker.checkQueryIdempotencyKeysRequest(request);
                    }

                    @Override
                    protected void process(QueryIdempotencyKeysRequest request, BusinessBizResult<IdempotencyKeysItem> response) {
                        // update idempotency keys status to Error or finished after account center finish debit and credit accounts
                        IdempotencyKeys idempotencyKeys = idempotencyKeysRepository.queryIdempotencyKeysByTxnId(request.getTxnId());
                        if(idempotencyKeys != null) {
                            //convert to idempotency Keys item,
                            response.setResult(ItemConverter.convertToIdempotencyKeys(idempotencyKeys));
                        }
                    }
                });
    }


}



