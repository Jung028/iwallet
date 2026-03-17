package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.enums.TransactionStatusEnum;
import com.alipay.account_center.common.service.facade.enums.TransactionTypeEnum;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.item.TransactionRecordItem;
import com.alipay.account_center.common.service.facade.request.*;
import com.alipay.business.biz.service.impl.checker.BusinessRequestChecker;
import com.alipay.business.biz.service.impl.helper.ResponseBuilder;
import com.alipay.business.biz.service.impl.template.BusinessBizCallback;
import com.alipay.business.common.service.facade.api.BusinessService;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.enums.AuthTypeEnum;
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
import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.item.OtpVerifiedClaims;
import com.alipay.usercenter.common.service.facade.request.VerifyUserAuthRequest;
import com.alipay.usercenter.common.service.facade.request.VerifyVerifiedTokenRequest;
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
import java.util.ArrayList;
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
    public BusinessBizResult<String> transferInit(TransferRequest request, String userId) {
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

                        if (existing != null && existing.getTxnId() != null) {
                            if (existing.getStatus().equals(IdempotencyKeysStatusEnum.SUCCESS) ||
                                    existing.getStatus().equals(IdempotencyKeysStatusEnum.FAILED)) {
                                // Replay stored response
                                response.setResult(existing.getResponseSnapshot());
                                return;
                            } else if (existing.getStatus().equals(IdempotencyKeysStatusEnum.PENDING) ||
                                    existing.getStatus().equals(IdempotencyKeysStatusEnum.INIT) ||
                                    existing.getStatus().equals(IdempotencyKeysStatusEnum.OTP_OVER_LIMIT)) {
                                // Return the existing txnId to try again for case where balance insufficient
                                ResponseBuilder.success(response, existing.getTxnId(),
                                        BusinessActionEnum.TRANSFER.getCode(), "Please complete your existing transaction first");
                                return;
                            }
                        }

                        // check if there exist a transaction that status is not FINISH, if there is,
                        // ask user to complete first before making a new one
                        QueryTransactionRecordRequest transactionRecordRequest = new QueryTransactionRecordRequest();
                        transactionRecordRequest.setAccountId(request.getPayerAccountNo());
                        List<String> statusList = new ArrayList<>();
                        statusList.add(TransactionStatusEnum.PENDING.getCode());
                        statusList.add(TransactionStatusEnum.OTP_OVER_LIMIT.getCode());
                        transactionRecordRequest.setTxnStatusList(statusList);
                        AccountBizResult<TransactionRecordItem> transaction = accountServiceClient.queryTransactionByStatus(transactionRecordRequest);

                        if (transaction.isSuccess() && transaction.getResult() != null && transaction.getResult().getTxnId() != null) {
                            ResponseBuilder.success(response, transaction.getResult().getTxnId(),
                                    BusinessActionEnum.TRANSFER.getCode(), "Please complete your existing transaction first");
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

                            // validate both accounts exists
                            AssertUtil.notNull(payerAccountInfo.getResult(), BusinessResultCode.ACCOUNT_NOT_FOUND, "payer account not found");
                            AssertUtil.notNull(payeeAccountInfo.getResult(), BusinessResultCode.ACCOUNT_NOT_FOUND, "payee account not found");

                            // verify user is authorised
                            AssertUtil.isTrue(payerAccountInfo.getResult().getAccountRelationId().equals(userId),
                                    BusinessResultCode.INVALID_REQUEST, "User is not authorised");

                            // validate balance is sufficient
                            CurrencyUnit payerAccountCurrency = Monetary.getCurrency(payerAccountInfo.getResult().getCurrency());
                            MonetaryAmount payerBalance = MoneyUtil.toMonetaryAmount(payerAccountInfo.getResult().getBalance(), payerAccountCurrency);
                            System.out.println(request.getAmount().getAmount());
                            MonetaryAmount requestAmount = MoneyUtil.toMonetaryAmount(request.getAmount().getAmount(), request.getAmount().getCurrency());
                            if (payerBalance.isLessThan(requestAmount)){
                                throw new RuntimeException("insufficient balance");
                            }

                            InsertTransactionRecordRequest insertTransactionRecordRequest = new InsertTransactionRecordRequest();
                            insertTransactionRecordRequest.setPayeeAccountNo(request.getPayeeAccountNo());
                            insertTransactionRecordRequest.setPayerAccountNo(request.getPayerAccountNo());
                            insertTransactionRecordRequest.setAmount(request.getAmount().getAmount());
                            insertTransactionRecordRequest.setCurrency(request.getAmount().getCurrency().getCurrencyCode());
                            insertTransactionRecordRequest.setTxnType(TransactionTypeEnum.TRANSFER);

                            // set initial status for transaction record
                            TransactionStatusEnum txnStatus = requestAmount.isGreaterThan(LIMIT) ?
                                    TransactionStatusEnum.OTP_OVER_LIMIT : TransactionStatusEnum.PENDING;
                            insertTransactionRecordRequest.setStatus(txnStatus);

                            // insert new record in account center transaction table
                            AccountBizResult<TransactionRecordItem> transactionRecord = accountServiceClient.insertTransactionRecord(insertTransactionRecordRequest);

                            if (transactionRecord != null && transactionRecord.isSuccess()) {
                                // after success insert new transaction record, then update the idempotency keys with the transaction id,
                                // update to PENDING or OTP_OVER_LIMIT for account center to check
                                IdempotencyKeys idempotencyKeys = new IdempotencyKeys();
                                idempotencyKeys.setTxnId(transactionRecord.getResult().getTxnId());
                                idempotencyKeys.setIdempotencyKey(request.getUniqueRequestId());

                                IdempotencyKeysStatusEnum idemStatus;
                                if (txnStatus == TransactionStatusEnum.OTP_OVER_LIMIT) {
                                    idemStatus = IdempotencyKeysStatusEnum.OTP_OVER_LIMIT;
                                } else {
                                    idemStatus = IdempotencyKeysStatusEnum.PENDING;
                                }

                                idempotencyKeys.setStatus(idemStatus);

                                int rows = idempotencyKeysRepository.updateIdempotencyKeys(idempotencyKeys);

                                // check idempotency keys success
                                if (rows > 0) {
                                    String msg;
                                    if (txnStatus == TransactionStatusEnum.OTP_OVER_LIMIT) {
                                        msg = "requires OTP transfer over limit confirmation";
                                    } else {
                                        msg = BusinessActionEnum.TRANSFER.getDesc();
                                    }
                                    // return txnId to frontend to perform transferConfirm authentication
                                    ResponseBuilder.success(response, transactionRecord.getResult().getTxnId(), BusinessActionEnum.TRANSFER.getCode(), msg);
                                } else {
                                    ResponseBuilder.fail(response, BusinessActionEnum.TRANSFER.getCode(), "Update idempotency keys failed");
                                }
                            }

                            return null;
                        });

                    }
                });
    }

    private IdempotencyKeys checkOrInsertIdempotencyKey(TransferRequest request, String userId) {
        try {
            // checks for duplicate requests by hash first, but only for ACTIVE ones to allow repeating transfers once finished
            String requestHash = HashUtil.generateIdempotentRequestHash(request.getAmount(), request.getPayerAccountNo(), request.getPayeeAccountNo());
            IdempotencyKeys activeByHash = idempotencyKeysRepository.queryActiveIdempotencyKeyByHash(requestHash, Long.valueOf(userId));

            if (activeByHash != null) {
                return activeByHash;
            }

            // if there is no active existing hash, we will insert a new idempotencyKey record
            IdempotencyKeys idempotencyKeys = new IdempotencyKeys();
            idempotencyKeys.setIdempotencyKey(request.getUniqueRequestId());
            idempotencyKeys.setUserId(Long.valueOf(userId));
            idempotencyKeys.setRequestHash(requestHash);
            idempotencyKeys.setTxnId(null);
            idempotencyKeys.setStatus(IdempotencyKeysStatusEnum.INIT);
            idempotencyKeys.setCreatedAt(new Date());
            idempotencyKeys.setUpdatedAt(new Date());
            idempotencyKeysRepository.insertIdempotencyKey(idempotencyKeys);
        } catch (DuplicateKeyException e) {
            // if the idempotencyKey (uniqueRequestId) already exists, return the record to handle replay or status check
            IdempotencyKeys idempotencyKeys = idempotencyKeysRepository.queryIdempotencyKeysByIdempotencyKey(request.getUniqueRequestId());
            System.out.println(idempotencyKeys.getIdempotencyKey());
            if (idempotencyKeys == null) {
                throw new IllegalStateException("Idempotency record missing");
            }
            System.out.println(idempotencyKeys.getIdempotencyKey());
            return idempotencyKeys;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    @Override
    public BusinessBizResult<String> transferConfirm(TransferConfirmRequest request, String userId) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.CONFIRM_TRANSFER,
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
                        // must check the jwttoken, if the txn is for otp_over_limit.
                        if (request.getAuthTypeEnum().equals(AuthTypeEnum.OTP)) {
                            VerifyVerifiedTokenRequest verifyVerifiedTokenRequest = new VerifyVerifiedTokenRequest();
                            verifyVerifiedTokenRequest.setVerifiedToken(request.getVerifiedToken());
                            userServiceClient.verifyVerifiedToken(verifyVerifiedTokenRequest);
                        }

                        QueryTransactionRecordRequest queryRequest = new QueryTransactionRecordRequest();
                        queryRequest.setTxnId(request.getTxnId());
                        queryRequest.setAccountId(request.getAccountId());
                        AccountBizResult<TransactionRecordItem> txnRecord = accountServiceClient.queryTransactionRecord(queryRequest);

                        // Fail early if the user is sending the wrong AuthType for this specific transaction
                        if (txnRecord.getResult().getTxnStatus().equals(TransactionStatusEnum.OTP_OVER_LIMIT)) {
                            AssertUtil.isTrue(request.getAuthTypeEnum().equals(AuthTypeEnum.OTP),
                                    BusinessResultCode.ILLEGAL_STATUS, "This transaction requires OTP verification");
                        }

                        // because now there could be two use case, one for view anything sensitive such as view balance, we need
                        // verification of password such as face id
                        VerifyUserAuthRequest verifyUserAuthRequest = new VerifyUserAuthRequest();
                        verifyUserAuthRequest.setUserId(userId);
                        verifyUserAuthRequest.setCredential(request.getPassword());
                        UserBizResult<String> authInfo = userServiceClient.verifyUserAuth(verifyUserAuthRequest);

                        if (!authInfo.isSuccess()) {
                            IdempotencyKeys idempotencyKeys = idempotencyKeysRepository.queryIdempotencyKeysByTxnId(request.getTxnId());

                            // increment FIRST before checking
                            int newRetryCount = idempotencyKeys.getRetryCount() + 1;

                            IdempotencyKeys updateIdempotencyKeys = new IdempotencyKeys();
                            updateIdempotencyKeys.setIdempotencyKey(idempotencyKeys.getIdempotencyKey()); // needed for WHERE clause
                            updateIdempotencyKeys.setRetryCount(newRetryCount);

                            if (newRetryCount >= 5) {
                                // permanent lockout
                                updateIdempotencyKeys.setStatus(IdempotencyKeysStatusEnum.PERMANENT_LOCKOUT);
                                idempotencyKeysRepository.updateIdempotencyKeys(updateIdempotencyKeys);
                                ResponseBuilder.fail(response, BusinessActionEnum.CONFIRM_TRANSFER.getCode(),
                                        "Account permanently locked, please contact support");

                            } else if (newRetryCount >= 3) {
                                // timed lockout — lock for 30 mins
                                updateIdempotencyKeys.setStatus(IdempotencyKeysStatusEnum.TIMED_LOCKOUT);
                                updateIdempotencyKeys.setLockedUntil(new Date(System.currentTimeMillis() + 30 * 60 * 1000));
                                idempotencyKeysRepository.updateIdempotencyKeys(updateIdempotencyKeys);
                                ResponseBuilder.fail(response, BusinessActionEnum.CONFIRM_TRANSFER.getCode(),
                                        "Too many attempts, locked for 30 minutes");

                            } else {
                                // just increment, show attempts remaining
                                idempotencyKeysRepository.updateIdempotencyKeys(updateIdempotencyKeys);
                                ResponseBuilder.fail(response, BusinessActionEnum.CONFIRM_TRANSFER.getCode(),
                                        "Incorrect password, " + (5 - newRetryCount) + " attempts remaining");
                            }
                            return;
                        }
                        // if exceeded, we need to lockout any type of transfer request for 30 minutes. then after second is 60 minutes.
                        // then 3rd is change your password
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
                        queryTransactionHistoryRequest.setPageNo(request.getPageNo());
                        queryTransactionHistoryRequest.setPageSize(request.getPageSize());

                        // query transaction history
                        AccountBizResult<QueryTransactionHistoryResult> result = accountServiceClient
                                .queryTransactionHistory(queryTransactionHistoryRequest);

                        BusinessTransactionHistoryResult businessTransactionHistoryResult = new BusinessTransactionHistoryResult();
                        businessTransactionHistoryResult.setTransactions(ItemConverter.convertToTxnHistory(result));
                        businessTransactionHistoryResult.setTotalCount(result.getResult().getTotalCount());

                        //convert to normal before return
                        ResponseBuilder.success(response, businessTransactionHistoryResult, BusinessActionEnum.QUERY_TRANSACTION_HISTORY.getCode(),
                                BusinessActionEnum.QUERY_TRANSACTION_HISTORY.getDesc());
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
                        ResponseBuilder.success(result, ItemConverter.convertToBalanceResult(accountInfo), BusinessActionEnum.QUERY_BALANCE.getCode(),
                                BusinessActionEnum.QUERY_BALANCE.getDesc());
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
                        IdempotencyKeys updateIdempotencyKeys = new IdempotencyKeys();
                        updateIdempotencyKeys.setTxnId(request.getTxnId());
                        updateIdempotencyKeys.setStatus(request.getStatus());
                        updateIdempotencyKeys.setRetryCount(request.getRetryCount());
                        int rows = idempotencyKeysRepository.updateIdempotencyKeysByTxnId(updateIdempotencyKeys);

                        IdempotencyKeys idempotencyKeys = idempotencyKeysRepository.queryIdempotencyKeysByTxnId(request.getTxnId());
                        if (rows > 0) {
                            UpdateIdempotencyKeysResult updateIdempotencyKeysResult = new UpdateIdempotencyKeysResult();
                            updateIdempotencyKeysResult.setTxnId(idempotencyKeys.getTxnId());
                            updateIdempotencyKeysResult.setStatus(idempotencyKeys.getStatus().getCode());
                            updateIdempotencyKeysResult.setRetryCount(idempotencyKeys.getRetryCount());

                            ResponseBuilder.success(response, updateIdempotencyKeysResult,
                                    BusinessActionEnum.UPDATE_IDEMPOTENCY_KEYS.getCode(),
                                    BusinessActionEnum.UPDATE_IDEMPOTENCY_KEYS.getDesc());
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



