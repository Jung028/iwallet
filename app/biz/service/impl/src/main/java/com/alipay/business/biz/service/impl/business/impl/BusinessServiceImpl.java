package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.enums.TransactionStatusEnum;
import com.alipay.account_center.common.service.facade.enums.TransactionTypeEnum;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.item.TransactionRecordItem;
import com.alipay.account_center.common.service.facade.request.*;
import com.alipay.business.biz.service.impl.auth.JwtUtil;
import com.alipay.business.biz.service.impl.auth.TransferTokenPayload;
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
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
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
import java.util.Date;
import java.util.UUID;

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

                        AssertUtil.isTrue(
                                !request.getPayeeAccountNo().equals(request.getPayerAccountNo()),
                                BusinessResultCode.PARAM_ILLEGAL, "Cannot send to same account");

                        QueryAccountInfoRequest queryAccountInfoRequest = new QueryAccountInfoRequest();
                        queryAccountInfoRequest.setAccountId(request.getPayerAccountNo());
                        AccountBizResult<AccountInfoItem> payerAccountInfo =
                                accountServiceClient.queryAccountInfo(queryAccountInfoRequest);

                        queryAccountInfoRequest.setAccountId(request.getPayeeAccountNo());
                        AccountBizResult<AccountInfoItem> payeeAccountInfo =
                                accountServiceClient.queryAccountInfo(queryAccountInfoRequest);

                        AssertUtil.notNull(payerAccountInfo.getResult(),
                                BusinessResultCode.ACCOUNT_NOT_FOUND, "Payer account not found");
                        AssertUtil.notNull(payeeAccountInfo.getResult(),
                                BusinessResultCode.ACCOUNT_NOT_FOUND, "Payee account not found");

                        AssertUtil.isTrue(
                                payerAccountInfo.getResult().getAccountRelationId().equals(userId),
                                BusinessResultCode.INVALID_REQUEST, "User is not authorised");

                        CurrencyUnit payerCurrency =
                                Monetary.getCurrency(payerAccountInfo.getResult().getCurrency());
                        MonetaryAmount payerBalance = MoneyUtil.toMonetaryAmount(
                                payerAccountInfo.getResult().getBalance(), payerCurrency);
                        MonetaryAmount requestAmount = MoneyUtil.toMonetaryAmount(
                                request.getAmount().getAmount(), request.getAmount().getCurrency());

                        AssertUtil.isTrue(!payerBalance.isLessThan(requestAmount),
                                BusinessResultCode.INSUFFICIENT_BALANCE, "Insufficient balance");

                        boolean requiresOtp = requestAmount.isGreaterThan(LIMIT);

                        String transferToken = transferTokenService.issue(
                                request.getUniqueRequestId(),
                                request.getPayerAccountNo(),
                                request.getPayeeAccountNo(),
                                request.getAmount().getAmount(),
                                request.getAmount().getCurrency().getCurrencyCode(),
                                requiresOtp
                        );

                        String msg = requiresOtp
                                ? "Requires OTP confirmation"
                                : BusinessActionEnum.TRANSFER.getDesc();

                        ResponseBuilder.success(response, transferToken,
                                BusinessActionEnum.TRANSFER.getCode(), msg);
                    }
                });
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
                        AssertUtil.notBlank(request.getTransferToken(),
                                BusinessResultCode.PARAM_ILLEGAL, "Transfer token is required");
                    }

                    @Override
                    protected void process(TransferConfirmRequest request, BusinessBizResult<String> response) {

                        // ── Step 1: verify token ───────────────────────────────
                        TransferTokenPayload payload =
                                transferTokenService.verify(request.getTransferToken());

                        AssertUtil.notNull(payload,
                                BusinessResultCode.INVALID_REQUEST,
                                "Transfer session expired or invalid, please start again");

                        // ── Step 2: OTP (verified before txn exists) ───────────
                        if (payload.isRequiresOtp()) {
                            AssertUtil.isTrue(
                                    request.getAuthTypeEnum().equals(AuthTypeEnum.OTP),
                                    BusinessResultCode.ILLEGAL_STATUS,
                                    "This transfer requires OTP verification");

                            VerifyVerifiedTokenRequest verifyVerifiedTokenRequest =
                                    new VerifyVerifiedTokenRequest();
                            verifyVerifiedTokenRequest.setVerifiedToken(request.getVerifiedToken());
                            userServiceClient.verifyVerifiedToken(verifyVerifiedTokenRequest);
                        }

                        // ── Step 3: idempotency replay check ───────────────────
                        // If processTransfer already ran successfully, replay result.
                        // If it's still in flight (PENDING/PROCESSING), return txnId.
                        IdempotencyKeys existingKey = idempotencyKeysRepository
                                .queryIdempotencyKeysByIdempotencyKey(payload.getUniqueRequestId());

                        if (existingKey != null && existingKey.getTxnId() != null) {
                            if (existingKey.getStatus().equals(IdempotencyKeysStatusEnum.SUCCESS)) {
                                response.setResult(existingKey.getResponseSnapshot());
                                return;
                            }
                            if (existingKey.getStatus().equals(IdempotencyKeysStatusEnum.PENDING) ||
                                    existingKey.getStatus().equals(IdempotencyKeysStatusEnum.PROCESSING)) {
                                ResponseBuilder.success(response, existingKey.getTxnId(),
                                        BusinessActionEnum.TRANSFER.getCode(),
                                        "Transfer is already in progress");
                                return;
                            }
                        }

                        // ── Step 4: PIN verification with retry lockout ─────────
                        VerifyUserAuthRequest verifyUserAuthRequest = new VerifyUserAuthRequest();
                        verifyUserAuthRequest.setUserId(userId);
                        verifyUserAuthRequest.setCredential(request.getPassword());
                        UserBizResult<String> authInfo =
                                userServiceClient.verifyUserAuth(verifyUserAuthRequest);

                        if (!authInfo.isSuccess()) {
                            handleFailedPinAttempt(payload.getUniqueRequestId(), response);
                            return;
                        }

                        // ── Steps 5 + 6: re-validate balance + create txn ──────
                        // Done atomically — balance check and txn insert in one
                        // DB transaction so no race condition between the two.
                        transactionTemplate.execute(status -> {

                            // re-validate balance (may have changed in the 10-min window)
                            QueryAccountInfoRequest queryAccountInfoRequest =
                                    new QueryAccountInfoRequest();
                            queryAccountInfoRequest.setAccountId(payload.getPayerAccountNo());
                            AccountBizResult<AccountInfoItem> payerAccountInfo =
                                    accountServiceClient.queryAccountInfo(queryAccountInfoRequest);

                            AssertUtil.notNull(payerAccountInfo.getResult(),
                                    BusinessResultCode.ACCOUNT_NOT_FOUND, "Payer account not found");

                            CurrencyUnit payerCurrency =
                                    Monetary.getCurrency(payerAccountInfo.getResult().getCurrency());
                            MonetaryAmount freshBalance = MoneyUtil.toMonetaryAmount(
                                    payerAccountInfo.getResult().getBalance(), payerCurrency);
                            CurrencyUnit currencyUnit = Monetary.getCurrency(payload.getCurrency());
                            MonetaryAmount requestAmount = MoneyUtil.toMonetaryAmount(
                                    payload.getAmount(), currencyUnit);

                            AssertUtil.isTrue(!freshBalance.isLessThan(requestAmount),
                                    BusinessResultCode.INSUFFICIENT_BALANCE,
                                    "Insufficient balance, please check your account and try again");

                            // create the transaction record — first and only DB write
                            // the txn is always born as PENDING (OTP was already verified above)
                            InsertTransactionRecordRequest insertRequest =
                                    new InsertTransactionRecordRequest();
                            insertRequest.setPayerAccountNo(payload.getPayerAccountNo());
                            insertRequest.setPayeeAccountNo(payload.getPayeeAccountNo());
                            insertRequest.setAmount(payload.getAmount());
                            insertRequest.setCurrency(payload.getCurrency());
                            insertRequest.setTxnType(TransactionTypeEnum.TRANSFER);
                            insertRequest.setStatus(TransactionStatusEnum.PENDING);

                            AccountBizResult<TransactionRecordItem> transactionRecord =
                                    accountServiceClient.insertTransactionRecord(insertRequest);

                            AssertUtil.isTrue(
                                    transactionRecord != null && transactionRecord.isSuccess(),
                                    BusinessResultCode.SYSTEM_EXCEPTION,
                                    "Failed to create transaction record");

                            String txnId = transactionRecord.getResult().getTxnId();

                            // write idempotency key as PENDING
                            // processTransfer owns the rest: PENDING → PROCESSING → SUCCESS | FAILED
                            IdempotencyKeys idempotencyKeys = new IdempotencyKeys();
                            idempotencyKeys.setIdempotencyKey(payload.getUniqueRequestId());
                            idempotencyKeys.setUserId(Long.valueOf(userId));
                            try {
                                idempotencyKeys.setRequestHash(HashUtil.generateIdempotentRequestHash(freshBalance, payload.getPayerAccountNo(), payload.getPayeeAccountNo()));
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                            idempotencyKeys.setTxnId(txnId);
                            idempotencyKeys.setStatus(IdempotencyKeysStatusEnum.PENDING);
                            idempotencyKeys.setRetryCount(0);
                            idempotencyKeys.setCreatedAt(new Date());
                            idempotencyKeys.setUpdatedAt(new Date());

                            try {
                                idempotencyKeysRepository.insertIdempotencyKey(idempotencyKeys);
                            } catch (DuplicateKeyException e) {
                                // concurrent confirm race — other request won, replay its result
                                IdempotencyKeys existing = idempotencyKeysRepository
                                        .queryIdempotencyKeysByIdempotencyKey(payload.getUniqueRequestId());
                                response.setResult(existing.getResponseSnapshot());
                                return null;
                            }

                            ResponseBuilder.success(response, txnId,
                                    BusinessActionEnum.TRANSFER.getCode(),
                                    BusinessActionEnum.TRANSFER.getDesc());

                            return null;
                        });

                        // ── Step 7: publish to Kafka ───────────────────────────
                        // Only runs if the transaction block above succeeded
                        if (response.isSuccess() && response.getResult() != null) {
                            PublishTransferRequest publishRequest = new PublishTransferRequest();
                            publishRequest.setAccountId(payload.getPayerAccountNo());
                            publishRequest.setTxnId(response.getResult());
                            accountServiceClient.publishTransfer(publishRequest);
                        }
                    }
                });
    }


    // ── retry / lockout helper ─────────────────────────────────────────────────
    // Keyed on uniqueRequestId since no txnId exists at PIN entry time.
    // Uses the idempotency table's retryCount field for storage.
    private void handleFailedPinAttempt(String uniqueRequestId, BusinessBizResult<String> response) {
        IdempotencyKeys idempotencyKeys = idempotencyKeysRepository
                .queryIdempotencyKeysByIdempotencyKey(uniqueRequestId);

        // insert a lightweight tracking row if this is the first wrong attempt
        if (idempotencyKeys == null) {
            idempotencyKeys = new IdempotencyKeys();
            idempotencyKeys.setIdempotencyKey(uniqueRequestId);
            idempotencyKeys.setStatus(IdempotencyKeysStatusEnum.INIT);
            idempotencyKeys.setRetryCount(0);
            idempotencyKeys.setCreatedAt(new Date());
            idempotencyKeys.setUpdatedAt(new Date());
            idempotencyKeysRepository.insertIdempotencyKey(idempotencyKeys);
        }

        int newRetryCount = idempotencyKeys.getRetryCount() + 1;

        IdempotencyKeys update = new IdempotencyKeys();
        update.setIdempotencyKey(uniqueRequestId);
        update.setRetryCount(newRetryCount);

        if (newRetryCount >= 5) {
            update.setStatus(IdempotencyKeysStatusEnum.PERMANENT_LOCKOUT);
            idempotencyKeysRepository.updateIdempotencyKeys(update);
            ResponseBuilder.fail(response, BusinessActionEnum.CONFIRM_TRANSFER.getCode(),
                    "Account permanently locked, please contact support");

        } else if (newRetryCount >= 3) {
            update.setStatus(IdempotencyKeysStatusEnum.TIMED_LOCKOUT);
            update.setLockedUntil(new Date(System.currentTimeMillis() + 30 * 60 * 1000L));
            idempotencyKeysRepository.updateIdempotencyKeys(update);
            ResponseBuilder.fail(response, BusinessActionEnum.CONFIRM_TRANSFER.getCode(),
                    "Too many attempts, locked for 30 minutes");

        } else {
            idempotencyKeysRepository.updateIdempotencyKeys(update);
            ResponseBuilder.fail(response, BusinessActionEnum.CONFIRM_TRANSFER.getCode(),
                    "Incorrect password, " + (5 - newRetryCount) + " attempts remaining");
        }
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



