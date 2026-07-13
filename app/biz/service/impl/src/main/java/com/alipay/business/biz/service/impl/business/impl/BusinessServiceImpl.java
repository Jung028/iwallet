package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.enums.*;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.item.TransactionRecordItem;
import com.alipay.account_center.common.service.facade.request.*;
import com.alipay.business.biz.service.impl.auth.QrTokenPayload;
import com.alipay.business.biz.service.impl.auth.TransferTokenPayload;
import com.alipay.business.biz.service.impl.checker.BusinessRequestChecker;
import com.alipay.business.biz.service.impl.helper.ResponseBuilder;
import com.alipay.business.biz.service.impl.receipt.ReceiptSessionData;
import com.alipay.business.biz.service.impl.template.BusinessBizCallback;
import com.alipay.business.common.service.facade.api.BusinessService;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.enums.*;
import com.alipay.business.common.service.facade.event.EcAutoReloadEvent;
import com.alipay.business.common.service.facade.item.IdempotencyKeysItem;
import com.alipay.business.common.service.facade.item.ReceiptSubItem;
import com.alipay.business.common.service.facade.money.MoneyUtil;
import com.alipay.business.common.service.facade.request.*;
import com.alipay.business.common.service.facade.request.TransferRequest;
import com.alipay.business.common.service.facade.result.*;
import com.alipay.business.common.util.requesthash.HashUtil;
import com.alipay.business.core.model.converter.ItemConverter;
import com.alipay.business.core.model.domain.IdempotencyKeys;
import com.alipay.business.core.model.domain.TransactionReceiptItemRel;
import com.alipay.business.core.model.enums.BusinessActionEnum;
import com.alipay.business.core.model.util.AssertUtil;
import com.alipay.merchant.common.service.facade.baseresult.MerchantBizResult;
import com.alipay.merchant.common.service.facade.item.MerchantInfoItem;
import com.alipay.merchant.common.service.facade.result.QueryMerchantInfoRequest;
import com.alipay.riskops.common.service.facade.baseresult.RiskOpsBizResult;
import com.alipay.riskops.common.service.facade.enums.RiskDecisionOutcome;
import com.alipay.riskops.common.service.facade.request.RiskDecisionRequest;
import com.alipay.riskops.common.service.facade.result.RiskDecisionResult;
import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.enums.AuthType;
import com.alipay.usercenter.common.service.facade.enums.Provider;
import com.alipay.usercenter.common.service.facade.item.*;
import com.alipay.usercenter.common.service.facade.request.*;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.alipay.business.biz.service.impl.constant.GlobalBizConstants.*;

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

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

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
                        request.setPayeeAccountNo("2db5cc27-3fa0-47c6-93b9-720e15ec7089");
                        QueryAccountInfoRequest queryAccountInfoRequest = new QueryAccountInfoRequest();
                        BigDecimal amount;
                        //et the qrid and the payload and signature here.
                        if (request.getTransferType().equals(TransferType.QR.getCode())) {
                            AssertUtil.notBlank(request.getQrToken(), BusinessResultCode.PARAM_ILLEGAL,
                                    "Qr token cannot be blank");
                            // verify the token, then return the payload
                            QrTokenPayload qrTokenPayload = qrTokenService.verifyQrToken(request.getQrToken());

                            // get payee account id, where payee is the QR code owner id account
                            queryAccountInfoRequest.setUserId(qrTokenPayload.getOwnerId());
                            AccountBizResult<AccountInfoItem> payeeAccountInfo =
                                    accountServiceClient.queryAccountInfoByUserId(queryAccountInfoRequest);
                            AssertUtil.notNull(payeeAccountInfo, BusinessResultCode.ACCOUNT_NOT_FOUND, "account not found");

                            // get payer account id
                            queryAccountInfoRequest.setUserId(userId);
                            AccountBizResult<AccountInfoItem> payerAccountInfo =
                                    accountServiceClient.queryAccountInfoByUserId(queryAccountInfoRequest);
                            AssertUtil.notNull(payerAccountInfo, BusinessResultCode.ACCOUNT_NOT_FOUND, "account not found");

                            // override the request which are passed into the
                            request.setPayeeAccountNo(payeeAccountInfo.getResult().getAccountId());
                            request.setPayerAccountNo(payerAccountInfo.getResult().getAccountId());
                            // the idempotency key unique request id stores the qrId.
                            request.setUniqueRequestId(qrTokenPayload.getQrId());
                            // convert string to big decimal
                            amount = BigDecimal.valueOf(qrTokenPayload.getAmount());
                        } else {
                            // else set the Money amount to request amount
                            amount = request.getAmount().getAmount();
                        }
                        AssertUtil.isTrue(
                                !request.getPayeeAccountNo().equals(request.getPayerAccountNo()),
                                BusinessResultCode.PARAM_ILLEGAL, "Cannot send to same account");

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

                        String transferToken = transferTokenService.issueTransferToken(
                                request.getUniqueRequestId(),
                                request.getReferenceId(),
                                request.getReferenceType(),
                                request.getPayerAccountNo(),
                                request.getPayeeAccountNo(),
                                amount,
                                request.getAmount().getCurrency().getCurrencyCode(),
                                requiresOtp,
                                request.getTxnCategory()
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
                    }

                    @Override
                    protected void process(TransferConfirmRequest request, BusinessBizResult<String> response) {

                        System.out.println("userId: " + userId);
                        TransferTokenPayload payload =
                                transferTokenService.verifyTransferToken(request.getTransferToken());

                        AssertUtil.notNull(payload,
                                BusinessResultCode.INVALID_REQUEST,
                                "Transfer session expired or invalid, please start again");

                        // TODO: we do not need to do anything to the transaction if it has failed because it was not updated to PROCESSING, meaning
                        //  the trasnsaction has not started to deduct your money, so if it failed in business Center, we do not need to
                        //  re-handle it. just add a scheduler to check where the GMT create is more than a day and status is PENDING.
                        if (payload.isRequiresOtp()) {
                            AssertUtil.isTrue(
                                    request.getTransferType().equals(TransferType.OTP.getCode()),
                                    BusinessResultCode.ILLEGAL_STATUS,
                                    "This transfer requires OTP verification");
                            AssertUtil.notBlank(request.getVerifiedToken(), BusinessResultCode.PARAM_ILLEGAL,
                                    "verified token is required");

                            VerifyVerifiedTokenRequest verifyVerifiedTokenRequest =
                                    new VerifyVerifiedTokenRequest();
                            verifyVerifiedTokenRequest.setVerifiedToken(request.getVerifiedToken());
                            userServiceClient.verifyVerifiedToken(verifyVerifiedTokenRequest);
                        }

                        // check if there is existing frontend request
                        IdempotencyKeys existingKey = idempotencyKeysRepository
                                .queryIdempotencyKeysByIdempotencyKey(payload.getUniqueRequestId());

                        if (existingKey != null && existingKey.getReferenceId() != null) {
                            if (existingKey.getStatus().equals(IdempotencyKeysStatusEnum.SUCCESS.getCode())) {
                                response.setResult(existingKey.getResponseSnapshot());
                                System.out.println("Already processed");
                                return;
                            }
                            if (existingKey.getStatus().equals(IdempotencyKeysStatusEnum.PENDING.getCode()) ||
                                    existingKey.getStatus().equals(IdempotencyKeysStatusEnum.PROCESSING.getCode())) {
                                ResponseBuilder.success(response, existingKey.getReferenceId(),
                                        BusinessActionEnum.TRANSFER.getCode(),
                                        "Transfer is already in progress");
                                return;
                            }
                        }

                        VerifyUserAuthRequest verifyUserAuthRequest = new VerifyUserAuthRequest();
                        verifyUserAuthRequest.setUserId(userId);
                        verifyUserAuthRequest.setAuthType(String.valueOf(AuthType.TRANSFER_PIN));
                        verifyUserAuthRequest.setCredential(request.getPassword());
                        UserBizResult<String> authInfo =
                                userServiceClient.verifyUserAuth(verifyUserAuthRequest);

                        if (!authInfo.isSuccess() || authInfo.getResult() == null) {
                            // Insert a idempotency record in INIT status to track incorrect attempts
                            handleFailedPinAttempt(payload.getUniqueRequestId(), response, userId);
                            return;
                        }

                        // ownerType is static — safe to check before the transaction
                        QueryAccountInfoRequest feeCheckRequest = new QueryAccountInfoRequest();
                        feeCheckRequest.setAccountId(payload.getPayerAccountNo());
                        AccountBizResult<AccountInfoItem> feeCheckAccount =
                                accountServiceClient.queryAccountInfo(feeCheckRequest);
                        boolean feeIsActive = feeCheckAccount.getResult() != null &&
                                OwnerType.MERCHANT.getCode().equals(feeCheckAccount.getResult().getOwnerType());

                        String referenceId = transactionTemplate.execute(status -> {

                            List<ReceiptSubItem> selectedReceiptItems = null;
                            // if this transaction category is a group receipt,
                            if (payload.getTransactionCategory().equals(TransactionCategory.GROUP_RECEIPT)) {
                                //get the receipt session from cache
                                ReceiptSessionData receiptSession =
                                        receiptSessionService.getReceiptSession(payload.getReferenceId());
                                System.out.println("HERE" +receiptSession.getReceiptId());

                                // get the selected receipt items
                                selectedReceiptItems =
                                        receiptSession.getItems()
                                                .stream()
                                                .filter(item -> item.getClaims().containsKey(userId))
                                                .map(item -> {
                                                    ReceiptSubItem relation = new ReceiptSubItem();
                                                    relation.setItemId(item.getItemId());
                                                    relation.setQuantity(item.getQuantity());
                                                    return relation;
                                                }).toList();

                            }

                            // insert idempotency record of pending status
                            IdempotencyKeys idempotencyKeys = new IdempotencyKeys();
                            idempotencyKeys.setIdempotencyKey(payload.getUniqueRequestId());
                            idempotencyKeys.setUserId(Long.valueOf(userId));
                            idempotencyKeys.setIdempotencyType(IdempotencyTypeEnum.TRANSFER.getCode());;

                            try {
                                idempotencyKeys.setRequestHash(HashUtil.
                                        generateTransferRequestHash(payload.getAmount(), payload.getCurrency(),
                                                payload.getPayerAccountNo(), payload.getPayeeAccountNo()));
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                            idempotencyKeys.setStatus(String.valueOf(IdempotencyKeysStatusEnum.PENDING));
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

                            // re-validate balance (may have changed in the 10-min window)
                            QueryAccountInfoRequest queryAccountInfoRequest =
                                    new QueryAccountInfoRequest();
                            queryAccountInfoRequest.setAccountId(payload.getPayerAccountNo());
                            System.out.println("payer accountID" + queryAccountInfoRequest.getAccountId());
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

                            QueryAutoReloadConfigRequest queryAutoReloadConfigRequest = new QueryAutoReloadConfigRequest();
                            queryAutoReloadConfigRequest.setUserId(userId);
                            UserBizResult<AutoReloadConfigItem> autoReloadConfig =
                                    userServiceClient.queryAutoReloadConfig(queryAutoReloadConfigRequest);
                            // we should not throw an error saying that the user should add the config,
                            if (autoReloadConfig.getResult() != null &&
                                    autoReloadConfig.getResult().getIsActive().equals(true)
                                    && freshBalance.isLessThan(requestAmount)) {
                                // publish auto reload event
                                EcAutoReloadEvent ecAutoReloadEvent = new EcAutoReloadEvent();
                                ecAutoReloadEvent.setAmount(payload.getAmount());
                                ecAutoReloadEvent.setCurrency(payload.getCurrency());
                                ecAutoReloadEvent.setUserId(userId);
                                kafkaTemplate.send("EC_AUTO_RELOAD", ecAutoReloadEvent);
                            } else {
                                AssertUtil.isTrue(!freshBalance.isLessThan(requestAmount),
                                        BusinessResultCode.INSUFFICIENT_BALANCE,
                                        "Insufficient balance, please check your account and try again");
                            }

                            // determine transaction category
                            TransactionCategory category = TransactionCategory.TRANSFER;
                            // Check if merchant account

                            if (payerAccountInfo.getResult().getOwnerType().equals(OwnerType.MERCHANT.getCode())) {
                                // then query the merchant category, then use this
                                QueryMerchantInfoRequest queryMerchantInfoRequest = new QueryMerchantInfoRequest();
                                queryMerchantInfoRequest.setMerchantId(payerAccountInfo.getResult().getAccountRelationId());
                                MerchantBizResult<MerchantInfoItem> merchantInfo = merchantServiceClient.queryMerchantInfo(queryMerchantInfoRequest);
                                category = TransactionCategory.valueOf(merchantInfo.getResult().getMerchantCategory());
                            }

                            // check if the transfer category is a group receipt, then update
                            if (payload.getTransactionCategory().equals(TransactionCategory.GROUP_RECEIPT)) {
                                // set category to group receipt
                                category = payload.getTransactionCategory();
                            }

                            // insert transaction record of pending status
                            InsertTransactionRecordRequest insertRequest =
                                    new InsertTransactionRecordRequest();
                            insertRequest.setPayerAccountNo(payload.getPayerAccountNo());
                            insertRequest.setPayeeAccountNo(payload.getPayeeAccountNo());
                            // Set transaction category
                            insertRequest.setCategory(category);
                            insertRequest.setAmount(payload.getAmount());
                            insertRequest.setCurrency(payload.getCurrency());
                            insertRequest.setTxnType(TransactionType.TRANSFER);
                            insertRequest.setStatus(TransactionStatusEnum.PENDING);

                            // only if its receipt item, then we set the reference id for transaction for consumer
                            if (payload.getReferenceType() != null && payload.getReferenceId() != null &&
                                    payload.getReferenceType().equals(ReferenceType.RECEIPT_ITEM.name())) {
                                insertRequest.setReferenceType(payload.getReferenceType());
                                insertRequest.setReferenceId(payload.getReferenceId());
                            }

                            AccountBizResult<TransactionRecordItem> transactionRecord =
                                    accountServiceClient.insertTransactionRecord(insertRequest);

                            AssertUtil.isTrue(
                                    transactionRecord != null
                                            && transactionRecord.isSuccess()
                                            && transactionRecord.getResult() != null,
                                    BusinessResultCode.SYSTEM_EXCEPTION,
                                    "Failed to create transaction record");

                            // only after a transaction record is confirmed to insert, we update the reference id for idempotent record
                            String txnId = transactionRecord.getResult().getTxnId();

                            // Then update the transaction_receipt_item_rel table for the items
                            if (selectedReceiptItems != null) {
                                for (ReceiptSubItem receiptSubItem : selectedReceiptItems) {
                                    TransactionReceiptItemRel transactionReceiptItemRel = new TransactionReceiptItemRel();
                                    transactionReceiptItemRel.setReceiptItemId(UUID.fromString(receiptSubItem.getItemId().toString()));
                                    transactionReceiptItemRel.setReceiptItemQuantity(Integer.parseInt(String.valueOf(receiptSubItem.getQuantity())));
                                    transactionReceiptItemRel.setTxnId(txnId);
                                    transactionReceiptItemRel.setGmtCreate(new Date());
                                    transactionReceiptItemRel.setGmtModified(new Date());

                                    transactionReceiptItemRepository.insertTransactionReceiptItemRel(transactionReceiptItemRel);
                                }
                            }

                            idempotencyKeysRepository.updateReferenceId(payload.getUniqueRequestId(), txnId);

                            ResponseBuilder.success(response, txnId,
                                    BusinessActionEnum.TRANSFER.getCode(),
                                    BusinessActionEnum.TRANSFER.getDesc());

                            return txnId;
                        });

                        IdempotencyKeys idempotencyKeys = new IdempotencyKeys();
                        idempotencyKeys.setIdempotencyKey(payload.getUniqueRequestId());
                        // Only runs if the transaction block above succeeded
                        if (response.isSuccess() && response.getResult() != null) {

                            // Evaluate transfer
                            RiskOpsBizResult<RiskDecisionResult> riskDecision = riskOpsServiceClient.evaluateTransferRisk
                                    (buildTransferRiskRequest(payload, referenceId, request, userId));

                            // increment redis into the cache the moment that the transfer is completed? only completed transfers reifght?
                            // publish the decision to inotify center
                            publishRiskDecision(riskDecision, idempotencyKeys, TxnEventType.TRANSFER);

                            // publish transfer payload
                            transactionService.publishTransfer(payload.getPayerAccountNo(), referenceId,
                                    TxnEventType.TRANSFER.getCode(), feeIsActive);
                        }
                    }
                });
    }

    private RiskDecisionRequest buildTransferRiskRequest(
                                                         TransferTokenPayload payload,
                                                         String referenceId,
                                                         TransferConfirmRequest request,
                                                         String userId
    ) {
        RiskDecisionRequest riskRequest = new RiskDecisionRequest();

        riskRequest.setBusinessId(referenceId); // txnId
        riskRequest.setBusinessType("TRANSFER");

        riskRequest.setUserId(userId);

        riskRequest.setPayerAccountNo(payload.getPayerAccountNo());
        riskRequest.setPayeeAccountNo(payload.getPayeeAccountNo());

        riskRequest.setAmount(payload.getAmount());
        riskRequest.setCurrency(payload.getCurrency());

        riskRequest.setUniqueRequestId(payload.getUniqueRequestId());
        riskRequest.setOccurredAt(new Date());

        return riskRequest;
    }

    /**
     * handle failed pin attempts
     * @param uniqueRequestId
     * @param response
     */
    private void handleFailedPinAttempt(String uniqueRequestId, BusinessBizResult<String> response, String userId) {
        IdempotencyKeys idempotencyKeys = idempotencyKeysRepository
                .queryIdempotencyKeysByIdempotencyKey(uniqueRequestId);

        // insert a lightweight tracking row if this is the first wrong attempt
        if (idempotencyKeys == null) {
            idempotencyKeys = new IdempotencyKeys();
            idempotencyKeys.setUserId(Long.valueOf(userId));
            idempotencyKeys.setIdempotencyKey(uniqueRequestId);
            idempotencyKeys.setStatus(String.valueOf(IdempotencyKeysStatusEnum.INIT));
            idempotencyKeys.setIdempotencyType(IdempotencyTypeEnum.TRANSFER_INCORRECT_PIN.getCode());
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
            update.setStatus(String.valueOf(IdempotencyKeysStatusEnum.PERMANENT_LOCKOUT));
            idempotencyKeysRepository.updateFailedAttempts(update);
            ResponseBuilder.fail(response, BusinessActionEnum.CONFIRM_TRANSFER.getCode(),
                    "Account permanently locked, please contact support");

        } else if (newRetryCount >= 3) {
            update.setStatus(String.valueOf(IdempotencyKeysStatusEnum.TIMED_LOCKOUT));
            update.setLockedUntil(new Date(System.currentTimeMillis() + 30 * 60 * 1000L));
            idempotencyKeysRepository.updateFailedAttempts(update);
            ResponseBuilder.fail(response, BusinessActionEnum.CONFIRM_TRANSFER.getCode(),
                    "Too many attempts, locked for 30 minutes");

        } else {
            idempotencyKeysRepository.updateFailedAttempts(update);
            ResponseBuilder.fail(response, BusinessActionEnum.CONFIRM_TRANSFER.getCode(),
                    "Incorrect password, " + (5 - newRetryCount) + " attempts remaining");
        }
    }

    private void handleFailedPinAttemptTopUp(String uniqueRequestId, BusinessBizResult<TopUpResult> response) {
        IdempotencyKeys idempotencyKeys = idempotencyKeysRepository
                .queryIdempotencyKeysByIdempotencyKey(uniqueRequestId);

        // insert a lightweight tracking row if this is the first wrong attempt
        if (idempotencyKeys == null) {
            idempotencyKeys = new IdempotencyKeys();
            idempotencyKeys.setIdempotencyKey(uniqueRequestId);
            idempotencyKeys.setStatus(String.valueOf(IdempotencyKeysStatusEnum.INIT));
            idempotencyKeys.setIdempotencyType(IdempotencyTypeEnum.TRANSFER_INCORRECT_PIN.getCode());
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
            update.setStatus(String.valueOf(IdempotencyKeysStatusEnum.PERMANENT_LOCKOUT));
            idempotencyKeysRepository.updateFailedAttempts(update);
            ResponseBuilder.fail(response, BusinessActionEnum.CONFIRM_TRANSFER.getCode(),
                    "Account permanently locked, please contact support");

        } else if (newRetryCount >= 3) {
            update.setStatus(String.valueOf(IdempotencyKeysStatusEnum.TIMED_LOCKOUT));
            update.setLockedUntil(new Date(System.currentTimeMillis() + 30 * 60 * 1000L));
            idempotencyKeysRepository.updateFailedAttempts(update);
            ResponseBuilder.fail(response, BusinessActionEnum.CONFIRM_TRANSFER.getCode(),
                    "Too many attempts, locked for 30 minutes" +
                            (5 - newRetryCount) + " attempts remaining");

        } else {
            idempotencyKeysRepository.updateFailedAttempts(update);
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
                        queryTransactionHistoryRequest.setPayerAccountId(request.getPayerAccountId());
                        queryTransactionHistoryRequest.setTxnCategory(request.getTxnCategory());
                        queryTransactionHistoryRequest.setAmountMin(request.getAmountMin());
                        queryTransactionHistoryRequest.setAmountMax(request.getAmountMax());
                        queryTransactionHistoryRequest.setTxnType(request.getTxnType());
                        queryTransactionHistoryRequest.setTxnStatus(request.getTxnStatus());
                        queryTransactionHistoryRequest.setPageNo(request.getPageNo());
                        queryTransactionHistoryRequest.setPageSize(request.getPageSize());
                        queryTransactionHistoryRequest.setGmtCreate(request.getGmtCreate());


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
                        updateIdempotencyKeys.setReferenceId(request.getReferenceId());
                        updateIdempotencyKeys.setStatus(String.valueOf(request.getStatus()));
                        updateIdempotencyKeys.setRetryCount(request.getRetryCount());
                        int rows = idempotencyKeysRepository.updateIdempotencyKeysByReferenceId(updateIdempotencyKeys);

                        IdempotencyKeys idempotencyKeys = idempotencyKeysRepository.queryIdempotencyKeysByReferenceId(request.getReferenceId());
                        if (rows > 0) {
                            UpdateIdempotencyKeysResult updateIdempotencyKeysResult = new UpdateIdempotencyKeysResult();
                            updateIdempotencyKeysResult.setReferenceId(idempotencyKeys.getReferenceId());
                            updateIdempotencyKeysResult.setStatus(idempotencyKeys.getStatus());
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
                        IdempotencyKeys idempotencyKeys = idempotencyKeysRepository.queryIdempotencyKeysByReferenceId(request.getReferenceId());
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

    @Override
    public BusinessBizResult<TopUpResult> createTopUpIntent(TopUpRequest request, String userId) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.CREATE_TOP_UP_INTENT,
                new BusinessBizCallback<>() {

                    @Override
                    protected BusinessBizResult<TopUpResult> createDefaultResponse() {
                        return new BusinessBizResult<>() {
                        };
                    }

                    @Override
                    protected void checkParams(TopUpRequest request) {
                        BusinessRequestChecker.checkCreateTopUpIntentRequest(request);
                    }

                    @Override
                    protected void process(TopUpRequest request, BusinessBizResult<TopUpResult> response) {
                        // verify password pin
                        VerifyUserAuthRequest verifyUserAuthRequest = new VerifyUserAuthRequest();
                        verifyUserAuthRequest.setUserId(userId);
                        verifyUserAuthRequest.setCredential(request.getPasswordPin());
                        verifyUserAuthRequest.setAuthType(AuthType.TRANSFER_PIN.getCode());
                        UserBizResult<String> userAuthItem = userServiceClient.verifyUserAuth(verifyUserAuthRequest);
                        AssertUtil.isTrue(userAuthItem.isSuccess(), BusinessResultCode.ILLEGAL_STATUS, "Password incorrect");

                        if (!userAuthItem.isSuccess()) {
                            handleFailedPinAttemptTopUp(request.getUniqueRequestId(), response);
                            return;
                        }

                        // get the stripeCustomerId from user_card_provider
                        QueryUserCardProviderRequest queryUserCardProviderRequest = new QueryUserCardProviderRequest();
                        queryUserCardProviderRequest.setUserId(userId);
                        queryUserCardProviderRequest.setProvider(Provider.STRIPE);
                        UserBizResult<UserCardProviderItem> result = userServiceClient.queryUserCardProvider(queryUserCardProviderRequest);
                        String providerCustomerId = result.getResult().getProviderCustomerId();
                        // ensure not null
                        AssertUtil.notBlank(providerCustomerId, BusinessResultCode.PARAM_ILLEGAL,
                                "Stripe customer not found for userId=" + userId);

                        // retrieve user name
                        QueryUserInfoRequest queryUserInfoRequest = new QueryUserInfoRequest();
                        queryUserInfoRequest.setUserId(userId);
                        UserBizResult<UserInfoItem> userInfo = userServiceClient.queryUserInfoByUserId(queryUserInfoRequest);

                        // then build payment intent params
                        PaymentIntentCreateParams.Builder paramsBuilder =
                                PaymentIntentCreateParams.builder()
                                        .setAmount(request.getAmount()
                                                .multiply(BigDecimal.valueOf(100)).longValue())
                                        .setCurrency(request.getCurrency().toLowerCase())
                                        .setCustomer(providerCustomerId)
                                        .putMetadata(USER_ID, userId)
                                        .putMetadata(SAVE_CARD, String.valueOf(request.isSaveCard()))
                                        .putMetadata(USER_NAME, userInfo.getResult().getUserName());

                        // if the user wants to save the card
                        if (request.isSaveCard()) {
                            paramsBuilder.setSetupFutureUsage(
                                    PaymentIntentCreateParams.SetupFutureUsage.OFF_SESSION);
                        }

                        // query idempotency for the request id if not exist then only continue below,
                        IdempotencyKeys existingKey = idempotencyKeysRepository.queryIdempotencyKeysByIdempotencyKey(
                                request.getUniqueRequestId()
                        );

                        if (existingKey != null && existingKey.getReferenceId() != null) {
                            if (existingKey.getStatus().equals(IdempotencyKeysStatusEnum.SUCCESS.getCode())) {
                                TopUpResult topUpResult = new TopUpResult();
                                topUpResult.setPaymentIntentId(existingKey.getReferenceId());
                                ResponseBuilder.success(response, topUpResult, BusinessActionEnum.CREATE_TOP_UP_INTENT.getCode(),
                                        "Already processed this top up intent");
                                return;
                            }
                            if (existingKey.getStatus().equals(IdempotencyKeysStatusEnum.INIT.getCode()) ||
                                    existingKey.getStatus().equals(IdempotencyKeysStatusEnum.PENDING.getCode()) ||
                                    existingKey.getStatus().equals(IdempotencyKeysStatusEnum.PROCESSING.getCode())) {
                                TopUpResult topUpResult = new TopUpResult();
                                PaymentIntent existingIntent = null;
                                try {
                                    existingIntent = PaymentIntent.retrieve(existingKey.getReferenceId());
                                } catch (StripeException e) {
                                    throw new RuntimeException(e);
                                }

                                topUpResult.setSecretClient(existingIntent.getClientSecret());
                                topUpResult.setPaymentIntentId(existingIntent.getId());
                                ResponseBuilder.success(response, topUpResult,
                                        BusinessActionEnum.CREATE_TOP_UP_INTENT.getCode(),
                                        "Transfer is already in progress");
                                return;
                            }
                        }

                        //surround in a transactional template
                        TopUpResult topUpResult = transactionTemplate.execute(status -> {

                            // geet the payer account id, insert a new transaction of the STRIPE_ACCOUNT
                            QueryAccountInfoRequest queryAccountInfoRequest = new QueryAccountInfoRequest();
                            queryAccountInfoRequest.setUserId(userId);
                            AccountBizResult<AccountInfoItem> accountInfo = accountServiceClient.queryAccountInfoByUserId(queryAccountInfoRequest);

                            // insert transaction record of pending status
                            InsertTransactionRecordRequest insertRequest =
                                    new InsertTransactionRecordRequest();
                            insertRequest.setPayerAccountNo(STRIPE_CLEARING_ACCOUNT);
                            insertRequest.setPayeeAccountNo(accountInfo.getResult().getAccountId());
                            insertRequest.setAmount(request.getAmount());
                            insertRequest.setCurrency(request.getCurrency());
                            insertRequest.setTxnType(TransactionType.TOP_UP);
                            insertRequest.setStatus(TransactionStatusEnum.PENDING);
                            // Add category
                            insertRequest.setCategory(TransactionCategory.TOP_UP);

                            AccountBizResult<TransactionRecordItem> transactionRecord =
                                    accountServiceClient.insertTransactionRecord(insertRequest);
                            AssertUtil.notNull(transactionRecord, BusinessResultCode.PARAM_ILLEGAL,
                                    "transaction record is null");

                            // ensure the txnId is set so that publishTopUp can pass in the txnId into the publishTransfer
                            paramsBuilder.putMetadata(TXN_ID, transactionRecord.getResult().getTxnId());

                            // set the idempotency key
                            RequestOptions options = RequestOptions.builder()
                                    .setIdempotencyKey(request.getUniqueRequestId())
                                    .build();
                            // then we build paymentIntent which is a stripe constructor for creating a PaymentIntent
                            PaymentIntent paymentIntent;
                            try {
                                paymentIntent = PaymentIntent.create(paramsBuilder.build(), options);
                            } catch (StripeException e) {
                                throw new RuntimeException(e);
                            }

                            // then we insert a new idempotency record for the paymentIntentId for publishTopUp to
                            // prevent processing same paymentIntent
                            IdempotencyKeys idempotencyKeys = new IdempotencyKeys();
                            idempotencyKeys.setIdempotencyKey(request.getUniqueRequestId());
                            idempotencyKeys.setIdempotencyType(IdempotencyTypeEnum.TOP_UP.getCode());
                            idempotencyKeys.setUserId(Long.valueOf(userId));
                            // set idempotency reference Id to the txnId
                            idempotencyKeys.setReferenceId(transactionRecord.getResult().getTxnId());
                            try {
                                idempotencyKeys.setRequestHash(HashUtil.
                                        generateTopUpRequestHash(request.getAmount(), request.getCurrency(),
                                                String.valueOf(request.getCardType()), request.getUniqueRequestId()));
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                            idempotencyKeys.setStatus(String.valueOf(IdempotencyKeysStatusEnum.INIT));
                            idempotencyKeys.setRetryCount(0);
                            idempotencyKeys.setCreatedAt(new Date());
                            idempotencyKeys.setUpdatedAt(new Date());
                            try {
                                idempotencyKeysRepository.insertIdempotencyKey(idempotencyKeys);
                            } catch (DuplicateKeyException e) {
                                // concurrent confirm race — other request won, replay its result
                                TopUpResult topResult = new TopUpResult();

                                IdempotencyKeys existing = idempotencyKeysRepository
                                        .queryIdempotencyKeysByIdempotencyKey(request.getUniqueRequestId());
                                try {
                                    paymentIntent = PaymentIntent.retrieve(existing.getReferenceId());
                                } catch (StripeException ex) {
                                    throw new RuntimeException(ex);
                                }
                                AssertUtil.notNull(paymentIntent, BusinessResultCode.PARAM_ILLEGAL, "payment intent is null");
                                topResult.setSecretClient(paymentIntent.getClientSecret());
                                topResult.setPaymentIntentId(paymentIntent.getId());
                                ResponseBuilder.success(response, topResult,
                                        BusinessActionEnum.CREATE_TOP_UP_INTENT.getCode(),
                                        "Transfer is already in progress");
                            }
                            // then we pass the client secret which is result of create and return to frontend to
                            // pass into payment using the card
                            RiskOpsBizResult<RiskDecisionResult> riskDecision = riskOpsServiceClient.evaluateTransferRisk
                                    (buildTopUpRiskRequest(request, userId));

                            publishRiskDecision(riskDecision, idempotencyKeys, TxnEventType.TOP_UP);

                            // --- build TopUpResult to return both ---
                            TopUpResult finalResult = new TopUpResult();
                            finalResult.setPaymentIntentId(paymentIntent.getId());
                            finalResult.setSecretClient(paymentIntent.getClientSecret());
                            finalResult.setTxnId(transactionRecord.getResult().getTxnId());

                            return finalResult;
                        });

                        // TODO :  cant I publish directly from iriskops ? sinc e the logic is the same, if the result is blocked,
                        // check whether tis a transfer or a top up, then publish specific email.



                        AssertUtil.notNull(topUpResult, BusinessResultCode.PARAM_ILLEGAL, "payment intent is null");
                        AssertUtil.notNull(topUpResult.getTxnId(), BusinessResultCode.PARAM_ILLEGAL, "txnId is null");
                        AssertUtil.notNull(topUpResult.getPaymentIntentId(), BusinessResultCode.PARAM_ILLEGAL, "payment intent is null");
                        AssertUtil.notNull(topUpResult.getSecretClient(), BusinessResultCode.PARAM_ILLEGAL, "secret client is null");

                        logger.info("TopUpIntent created: requestId={}, paymentIntentId={}",
                                request.getUniqueRequestId(), topUpResult.getPaymentIntentId());

                        ResponseBuilder.success(response, topUpResult,
                                BusinessActionEnum.CREATE_TOP_UP_INTENT.getCode(),
                                BusinessActionEnum.CREATE_TOP_UP_INTENT.getDesc());

                    }
                });
    }

    private void publishRiskDecision(RiskOpsBizResult<RiskDecisionResult> riskDecision,
                                     IdempotencyKeys idempotencyKeys,
                                     TxnEventType txnEventType
    ) {
        // if is block
        String eventType = txnEventType.getCode();

        UpdateTransactionRecordRequest updateRequest = new UpdateTransactionRecordRequest();
        if (riskDecision.getResult().getOutcome().equals(RiskDecisionOutcome.BLOCK.getCode())) {
            // mark the transaction as BLOCKED, and idempotency result FAILED with reason. then return
            updateRequest.setTxnId(idempotencyKeys.getReferenceId());
            updateRequest.setStatus(TransactionStatusEnum.FAILED.getCode());
            accountServiceClient.updateTransactionRecord(updateRequest);

            idempotencyKeys.setReferenceId(idempotencyKeys.getReferenceId());
            idempotencyKeys.setStatus(IdempotencyKeysStatusEnum.FAILED.getCode());
            idempotencyKeysRepository.updateIdempotencyKeys(idempotencyKeys);
            if (txnEventType.equals(TxnEventType.TRANSFER)) {
                kafkaTemplate.send("EC_TRANSFER_BLOCKED", riskDecision.getResult().getUserId(), eventType);
            } else if (txnEventType.equals(TxnEventType.TOP_UP)) {
                kafkaTemplate.send("EC_TRANSFER_BLOCKED", riskDecision.getResult().getUserId(), eventType);
            }

        }
        // if is step up
        if (riskDecision.getResult().getOutcome().equals(RiskDecisionOutcome.STEP_UP.getCode())) {
            //TODO: in notify, its just sending an email saying suspicious behaviour, to contact bank if not him.
            // Add userId, accountId, to send notification, we need to the user gmail or phone no.
            if (txnEventType.equals(TxnEventType.TRANSFER)) {
                kafkaTemplate.send("EC_TRANSFER_STEP_UP_REQUIRED", riskDecision.getResult().getUserId(), eventType);
            } else if (txnEventType.equals(TxnEventType.TOP_UP)) {
                kafkaTemplate.send("EC_TRANSFER_STEP_UP_REQUIRED", riskDecision.getResult().getUserId(), eventType);
            }
        }
    }

    private RiskDecisionRequest buildTopUpRiskRequest(TopUpRequest request, String userId) {
        RiskDecisionRequest riskRequest = new RiskDecisionRequest();

        riskRequest.setBusinessId(request.getUniqueRequestId());
        riskRequest.setBusinessType("TOP_UP");
        riskRequest.setUserId(userId);

        QueryAccountInfoRequest queryAccountInfoRequest = new QueryAccountInfoRequest();
        queryAccountInfoRequest.setUserId(userId);
        AccountBizResult<AccountInfoItem> accountInfo = accountServiceClient.queryAccountInfoByUserId(queryAccountInfoRequest);
        if (accountInfo != null && accountInfo.getResult() != null) {
            riskRequest.setAccountNo(accountInfo.getResult().getAccountId());
            riskRequest.setPayeeAccountNo(accountInfo.getResult().getAccountId());
        }
        riskRequest.setPayerAccountNo(STRIPE_CLEARING_ACCOUNT);

        riskRequest.setAmount(request.getAmount());
        riskRequest.setCurrency(request.getCurrency());
        riskRequest.setUniqueRequestId(request.getUniqueRequestId());

        riskRequest.setTraceId(MDC.get("traceId"));
        riskRequest.setOccurredAt(new Date());

        if (request.getCardType() != null) {
            riskRequest.getAttributes().put("cardType", request.getCardType().name());
        }

        return riskRequest;
    }

    @Override
    public BusinessBizResult<String> chargeCard(ChargeCardRequest request, String userId) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.CHARGE_CARD,
                new BusinessBizCallback<>() {

                    @Override
                    protected BusinessBizResult<String> createDefaultResponse() {
                        return new BusinessBizResult<>();
                    }

                    @Override
                    protected void checkParams(ChargeCardRequest request) {

                    }

                    @Override
                    protected void process(ChargeCardRequest request,
                                           BusinessBizResult<String> response) {

                        // 1. Fetch saved card token from UserCenter
                        QueryDefaultCardRequest cardRequest = new QueryDefaultCardRequest();
                        cardRequest.setUserId(userId);
                        UserBizResult<UserCardDetailItem> cardResult =
                                topUpServiceClient.queryDefaultCard(cardRequest);

                        AssertUtil.isTrue(
                                cardResult.isSuccess() && cardResult.getResult() != null,
                                BusinessResultCode.CARD_NOT_FOUND,
                                "No default card found for userId=" + userId);

                        UserCardDetailItem card = cardResult.getResult();

                        //    Stripe uses saved mandate from initial confirmCardPayment
                        try {
                            PaymentIntent intent = PaymentIntent.create(
                                    PaymentIntentCreateParams.builder()
                                            .setAmount(request.getAmount()
                                                    .multiply(BigDecimal.valueOf(100)).longValue())
                                            .setCurrency(request.getCurrency().toLowerCase())
                                            .setCustomer(card.getProviderCustomerId())
                                            .setPaymentMethod(card.getProviderToken()) // pm_xxx
                                            .setConfirm(true)       // charge immediately
                                            .setOffSession(true)    // user NOT present
                                            .putMetadata(USER_ID, userId)
                                            .putMetadata(SAVE_CARD, FALSE)
                                            .putMetadata(TYPE, AUTO_RELOAD)
                                            .build()
                            );

                            // Stripe webhook fires async → EC_TOPUP_RECEIVED
                            logger.info("Auto-reload initiated userId={} pi={}",
                                    userId, intent.getId());

                            ResponseBuilder.success(response,
                                    intent.getId(),
                                    BusinessActionEnum.CHARGE_CARD.getCode(),
                                    "Auto-reload initiated");

                        } catch (CardException e) {
                            // Card declined — disable auto-reload, notify user
                            logger.warn("Auto-reload card declined userId={}: {}",
                                    userId, e.getMessage());
                            ResponseBuilder.fail(response,
                                    BusinessActionEnum.CHARGE_CARD.getCode(),
                                    "Card declined: " + e.getMessage());

                        } catch (StripeException e) {
                            logger.error("Stripe error during auto-reload userId={}",
                                    userId, e);
                            ResponseBuilder.fail(response,
                                    BusinessActionEnum.CHARGE_CARD.getCode(),
                                    "Payment provider error");
                        }
                    }
                });
    }




}


