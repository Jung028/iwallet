package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.enums.TransactionStatusEnum;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.item.TransactionHistoryItem;
import com.alipay.account_center.common.service.facade.item.TransactionRecordItem;
import com.alipay.account_center.common.service.facade.request.*;
import com.alipay.business.biz.service.impl.checker.BusinessRequestChecker;
import com.alipay.business.biz.service.impl.helper.ResponseBuilder;
import com.alipay.business.biz.service.impl.template.BusinessBizCallback;
import com.alipay.business.common.service.facade.api.BusinessService;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.IdempotencyKeysStatusEnum;
import com.alipay.business.common.service.facade.item.IdempotencyKeysItem;
import com.alipay.business.common.service.facade.money.MoneyUtil;
import com.alipay.business.common.service.facade.request.*;
import com.alipay.business.common.service.facade.request.TransferRequest;
import com.alipay.business.common.service.facade.result.*;
import com.alipay.business.common.util.requesthash.HashUtil;
import com.alipay.business.core.model.converter.ItemConverter;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import com.alipay.business.core.model.enums.BusinessActionEnum;
import com.alipay.business.core.model.exception.BusinessException;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.sofa.runtime.api.annotation.SofaReferenceBinding;
import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.request.VerifyUserAuthRequest;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

/**
 * Business service impl
 */
@SofaService(
        interfaceType = BusinessService.class,
        bindings = {
                @SofaServiceBinding(bindingType = "rest"),
                @SofaServiceBinding(bindingType = "bolt")
        }
)
@Service
public class BusinessServiceImpl extends AbstractBusinessBizService implements BusinessService{

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

                        IdempotencyKeys existing = checkOrInsertIdempotencyKey(request, userId);

                        if (existing != null && !existing.getStatus().equals(IdempotencyKeysStatusEnum.INIT)) {
                            // Replay stored response
                            response.setResult(existing.getResponseSnapshot());
                            return;
                        }

                        transactionTemplate.execute(status -> {
                            // check payer != payee
                            AssertUtil.isTrue(!request.getPayeeAccountNo().equals(request.getPayerAccountNo()), BusinessResultCode.PARAM_ILLEGAL, "Cannot send to same account");

                            // query account info from account
                            QueryAccountInfoRequest queryAccountInfoRequest = new QueryAccountInfoRequest();
                            queryAccountInfoRequest.setAccountId(request.getPayeeAccountNo());
                            System.out.println(request.getPayeeAccountNo());
                            AccountBizResult<AccountInfoItem> payeeAccountInfo = accountServiceClient.queryAccountInfo(queryAccountInfoRequest);
                            queryAccountInfoRequest.setAccountId(request.getPayerAccountNo());
                            AccountBizResult<AccountInfoItem> payerAccountInfo = accountServiceClient.queryAccountInfo(queryAccountInfoRequest);

                            // check user is the account's owner.
                            AssertUtil.isTrue(userId.equals(payerAccountInfo.getResult().getAccountRelationId()),
                                    BusinessResultCode.INVALID_REQUEST, "User not authorized to perform this transfer");

                            // validate both accounts exists
                            AssertUtil.notNull(payerAccountInfo, BusinessResultCode.ACCOUNT_NOT_FOUND, "payer account not found");
                            AssertUtil.notNull(payeeAccountInfo, BusinessResultCode.ACCOUNT_NOT_FOUND, "payee account not found");

                            // verify user is authorised
                            AssertUtil.isTrue(payerAccountInfo.getResult().getAccountRelationId().equals(userId),
                                    BusinessResultCode.INVALID_REQUEST, "User is not authorised");

                            // validate balance is sufficient
                            CurrencyUnit payerAccountCurrent = Monetary.getCurrency(payerAccountInfo.getResult().getCurrency());
                            MonetaryAmount payerBalance = MoneyUtil.toMonetaryAmount(payerAccountInfo.getResult().getBalance(), payerAccountCurrent);
                            System.out.println(request.getAmount().getAmount());
                            MonetaryAmount requestAmount = MoneyUtil.toMonetaryAmount(request.getAmount().getAmount(), request.getAmount().getCurrency());
                            if (payerBalance.isLessThan(requestAmount)){
                                throw new RuntimeException("insufficient balance");
                            }

                            InsertTransactionRecordRequest insertTransactionRecordRequest = new InsertTransactionRecordRequest();
                            insertTransactionRecordRequest.setPayeeAccountNo(request.getPayeeAccountNo());
                            insertTransactionRecordRequest.setPayerAccountNo(request.getPayerAccountNo());
                            insertTransactionRecordRequest.setAmount(request.getAmount().getAmount());
                            insertTransactionRecordRequest.setCurrency(request.getAmount().getCurrency());
                            //check if amount over limit,
                            if (requestAmount.isGreaterThan(LIMIT)) {
                                // set status to OTP_OVER_LIMIT
                                insertTransactionRecordRequest.setStatus(TransactionStatusEnum.OTP_OVER_LIMIT);
                                //insert new record in transaction, status OTP_AMOUNT_OVER_LIMIT
                                AccountBizResult<TransactionRecordItem> result = accountServiceClient.insertTransactionRecord(insertTransactionRecordRequest);
                                if (result != null && result.getResult() != null) {
                                    ResponseBuilder.success(response, null, BusinessActionEnum.TRANSFER.getCode(), BusinessActionEnum.TRANSFER.getDesc());
                                }
                            } else {
                                insertTransactionRecordRequest.setStatus(TransactionStatusEnum.PENDING);
                                AccountBizResult<TransactionRecordItem> transactionRecord = accountServiceClient.insertTransactionRecord(insertTransactionRecordRequest);

                                if (transactionRecord != null && transactionRecord.isSuccess()) {
                                    // after success insert new transaction record, then update the idempotency keys with the transaction id,
                                    // update to PENDING for account center to check
                                    int idempotencyKeys = idempotencyKeysRepository.updateIdempotencyKeys(transactionRecord.getResult().getTxnId(),
                                            IdempotencyKeysStatusEnum.PENDING.getCode(), 0);

                                    // check idempotency keys success
                                    if (idempotencyKeys > 0) {
                                        // return txnId to frontend to perform transferConfirm authentication
                                        ResponseBuilder.success(response, transactionRecord.getResult().getTxnId(), BusinessActionEnum.TRANSFER.getCode(), BusinessActionEnum.TRANSFER.getDesc());
                                    } else {
                                        ResponseBuilder.fail(response, BusinessActionEnum.TRANSFER.getCode(), "Update idempotency keys failed");
                                    }
                                }
                            }

                            return null;
                        });

                    }
                });
    }

    private IdempotencyKeys checkOrInsertIdempotencyKey(TransferRequest request, String userId) {
        try {
            IdempotencyKeys idempotencyKeys = new IdempotencyKeys();
            idempotencyKeys.setIdempotencyKey(request.getUniqueRequestId());
            idempotencyKeys.setUserId(Long.valueOf(userId));
            idempotencyKeys.setRequestHash(HashUtil.generateIdempotentRequestHash
                    (request.getAmount(), request.getPayerAccountNo(), request.getPayeeAccountNo()));
            idempotencyKeys.setTxnId(null);
            System.out.println(idempotencyKeys.getIdempotencyKey());
            System.out.println(idempotencyKeys.getUserId());
            System.out.println(idempotencyKeys.getRequestHash());
            idempotencyKeys.setStatus(IdempotencyKeysStatusEnum.INIT);
            idempotencyKeys.setCreatedAt(new Date());
            idempotencyKeys.setUpdatedAt(new Date());
            idempotencyKeysRepository.insertIdempotencyKey(idempotencyKeys);
        } catch (DuplicateKeyException e) {
            // if the record is valid, return duplicate request result
            IdempotencyKeys idempotencyKeys = idempotencyKeysRepository.queryIdempotencyKeysByUniqueRequestId(request.getUniqueRequestId());
            if (idempotencyKeys == null) {
                throw new IllegalStateException("Idempotency record missing");
            }
            return switch (idempotencyKeys.getStatus()) {
                case PENDING ->
                        throw new BusinessException(BusinessResultCode.REPEATED_SUBMIT, "Request is processing");
                case SUCCESS, FAILED->
                    throw new BusinessException(BusinessResultCode.REPEATED_SUBMIT, "Screenshot: " + idempotencyKeys.getResponseSnapshot());
                case INIT -> idempotencyKeys;
                default -> throw new IllegalStateException("Unknown status");
            };
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    @Override
    public BusinessBizResult<String> transferConfirm(TransferConfirmRequest request, String userId) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.PASSWORD_CONFIRM,
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
                        // because now there could be two use case, one for view anything sensitive such as view balance, we need
                        // verification of password such as face id
                        VerifyUserAuthRequest verifyUserAuthRequest = new VerifyUserAuthRequest();
                        verifyUserAuthRequest.setUserId(userId);
                        verifyUserAuthRequest.setCredential(request.getPassword());
                        UserBizResult<String> authInfo = userServiceClient.verifyUserAuth(verifyUserAuthRequest);

                        AssertUtil.isTrue(authInfo.isSuccess(), BusinessResultCode.PASSWORD_INCORRECT, "Invalid password");

                        PublishTransferRequest publishTransferRequest = new PublishTransferRequest();
                        publishTransferRequest.setAccountId(request.getAccountId());
                        publishTransferRequest.setTxnId(request.getTxnId());
                        AccountBizResult<String> accountBizResult = accountServiceClient.publishTransfer(publishTransferRequest);
                        if (accountBizResult.isSuccess()) {
                            ResponseBuilder.success(response, null, BusinessActionEnum.TRANSFER.getCode(), BusinessActionEnum.TRANSFER.getDesc());
                        } else {
                            ResponseBuilder.fail(response, BusinessActionEnum.TRANSFER.getCode(), BusinessActionEnum.TRANSFER.getDesc());
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
                        ResponseBuilder.success(result, ItemConverter.convertToTxnDetails(accountBizResult), BusinessActionEnum.QUERY_TRANSACTION_DETAILS.getCode(),
                                BusinessActionEnum.QUERY_TRANSACTION_DETAILS.getDesc());
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
                        int rows = idempotencyKeysRepository.updateIdempotencyKeys(
                                request.getTxnId(),
                                request.getStatus().getCode(),
                                request.getRetryCount()
                        );

                        IdempotencyKeys idempotencyKeys = idempotencyKeysRepository.queryIdempotencyKeysByTxnId(request.getTxnId());
                        if (rows > 0) {
                            UpdateIdempotencyKeysResult updateIdempotencyKeysResult = new UpdateIdempotencyKeysResult();
                            updateIdempotencyKeysResult.setTxnId(idempotencyKeys.getTxnId());
                            updateIdempotencyKeysResult.setStatus(idempotencyKeys.getStatus().getCode());
                            updateIdempotencyKeysResult.setRetryCount(idempotencyKeys.getRetryCount());

                            ResponseBuilder.success(response, updateIdempotencyKeysResult,
                                    BusinessActionEnum.QUERY_IDEMPOTENCY_KEYS.getCode(),
                                    BusinessActionEnum.QUERY_IDEMPOTENCY_KEYS.getDesc());
                        } else {
                            ResponseBuilder.fail(response,
                                    BusinessActionEnum.QUERY_IDEMPOTENCY_KEYS.getCode(),
                                    "No idempotency record updated");
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
                        if (idempotencyKeys != null) {
                            //convert to idempotency Keys item,
                            ResponseBuilder.success(response, ItemConverter.convertToIdempotencyKeys(idempotencyKeys), BusinessActionEnum.QUERY_IDEMPOTENCY_KEYS.getCode(),
                                    BusinessActionEnum.QUERY_IDEMPOTENCY_KEYS.getDesc());
                        } else {
                            ResponseBuilder.fail(response, BusinessActionEnum.QUERY_IDEMPOTENCY_KEYS.getCode(),
                                    BusinessActionEnum.QUERY_IDEMPOTENCY_KEYS.getDesc());

                        }
                    }
                });
    }


}



