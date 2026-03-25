package com.alipay.business.biz.service.impl.business.impl;

import com.alipay.account_center.common.service.facade.baseresult.AccountBizResult;
import com.alipay.account_center.common.service.facade.enums.TransactionStatusEnum;
import com.alipay.account_center.common.service.facade.enums.TransactionTypeEnum;
import com.alipay.account_center.common.service.facade.item.AccountInfoItem;
import com.alipay.account_center.common.service.facade.item.TransactionRecordItem;
import com.alipay.account_center.common.service.facade.request.*;
import com.alipay.business.biz.service.impl.auth.TransferTokenPayload;
import com.alipay.business.biz.service.impl.checker.BusinessRequestChecker;
import com.alipay.business.biz.service.impl.helper.ResponseBuilder;
import com.alipay.business.biz.service.impl.template.BusinessBizCallback;
import com.alipay.business.common.service.facade.api.BusinessService;
import com.alipay.business.common.service.facade.baseresult.BusinessBizResult;
import com.alipay.business.common.service.facade.enums.AuthTypeEnum;
import com.alipay.business.common.service.facade.enums.BusinessResultCode;
import com.alipay.business.common.service.facade.enums.IdempotencyKeysStatusEnum;
import com.alipay.business.common.service.facade.event.EcTopUpEvent;
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
import com.alipay.sofa.rpc.common.utils.JSONUtils;
import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import com.alipay.usercenter.common.service.facade.baseresult.UserBizResult;
import com.alipay.usercenter.common.service.facade.config.ExtInfo;
import com.alipay.usercenter.common.service.facade.enums.CardIssuer;
import com.alipay.usercenter.common.service.facade.enums.CardStatus;
import com.alipay.usercenter.common.service.facade.enums.CardType;
import com.alipay.usercenter.common.service.facade.enums.Provider;
import com.alipay.usercenter.common.service.facade.item.UserCardDetailItem;
import com.alipay.usercenter.common.service.facade.item.UserInfoItem;
import com.alipay.usercenter.common.service.facade.request.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.CardException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import okhttp3.Response;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

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

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

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
                    }

                    @Override
                    protected void process(TransferConfirmRequest request, BusinessBizResult<String> response) {

                        TransferTokenPayload payload =
                                transferTokenService.verify(request.getTransferToken());

                        AssertUtil.notNull(payload,
                                BusinessResultCode.INVALID_REQUEST,
                                "Transfer session expired or invalid, please start again");

                        if (payload.isRequiresOtp()) {
                            AssertUtil.isTrue(
                                    request.getAuthTypeEnum().equals(AuthTypeEnum.OTP),
                                    BusinessResultCode.ILLEGAL_STATUS,
                                    "This transfer requires OTP verification");
                            AssertUtil.notBlank(request.getVerifiedToken(), BusinessResultCode.PARAM_ILLEGAL,
                                    "verified token is required");

                            VerifyVerifiedTokenRequest verifyVerifiedTokenRequest =
                                    new VerifyVerifiedTokenRequest();
                            verifyVerifiedTokenRequest.setVerifiedToken(request.getVerifiedToken());
                            userServiceClient.verifyVerifiedToken(verifyVerifiedTokenRequest);
                        }

                        // check if there is existing idempotency record
                        IdempotencyKeys existingKey = idempotencyKeysRepository
                                .queryIdempotencyKeysByIdempotencyKey(payload.getUniqueRequestId());

                        if (existingKey != null && existingKey.getReferenceId() != null) {
                            if (existingKey.getStatus().equals(IdempotencyKeysStatusEnum.SUCCESS)) {
                                response.setResult(existingKey.getResponseSnapshot());
                                System.out.println("Already processed");
                                return;
                            }
                            if (existingKey.getStatus().equals(IdempotencyKeysStatusEnum.PENDING) ||
                                    existingKey.getStatus().equals(IdempotencyKeysStatusEnum.PROCESSING)) {
                                ResponseBuilder.success(response, existingKey.getReferenceId(),
                                        BusinessActionEnum.TRANSFER.getCode(),
                                        "Transfer is already in progress");
                                return;
                            }
                        }

                        // verify user password
                        VerifyUserAuthRequest verifyUserAuthRequest = new VerifyUserAuthRequest();
                        verifyUserAuthRequest.setUserId(userId);
                        verifyUserAuthRequest.setCredential(request.getPassword());
                        UserBizResult<String> authInfo =
                                userServiceClient.verifyUserAuth(verifyUserAuthRequest);

                        if (!authInfo.isSuccess()) {
                            handleFailedPinAttempt(payload.getUniqueRequestId(), response);
                            return;
                        }

                        transactionTemplate.execute(status -> {

                            // insert idempotency record of pending status
                            IdempotencyKeys idempotencyKeys = new IdempotencyKeys();
                            idempotencyKeys.setIdempotencyKey(payload.getUniqueRequestId());
                            idempotencyKeys.setUserId(Long.valueOf(userId));
                            try {
                                idempotencyKeys.setRequestHash(HashUtil.
                                        generateIdempotentRequestHash(payload.getAmount(), payload.getCurrency(),
                                                payload.getPayerAccountNo(), payload.getPayeeAccountNo()));
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
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

                            // TODO: we will check if the auto reload is triggered, if its not we assert balance insufficient
                            //  else, we will call chargeSavedCard() which will call the stripejs deduct and return response
                            //  and we will add the balance and continue the publishTransfer below
                            AssertUtil.isTrue(!freshBalance.isLessThan(requestAmount),
                                    BusinessResultCode.INSUFFICIENT_BALANCE,
                                    "Insufficient balance, please check your account and try again");

                            // insert transaction record of pending status
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

                            String referenceId = transactionRecord.getResult().getTxnId();

                            idempotencyKeysRepository.updateReferenceId(payload.getUniqueRequestId(), referenceId);

                            ResponseBuilder.success(response, referenceId,
                                    BusinessActionEnum.TRANSFER.getCode(),
                                    BusinessActionEnum.TRANSFER.getDesc());

                            return null;
                        });

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

    /**
     * handle failed pin attempts
     * @param uniqueRequestId
     * @param response
     */
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
            idempotencyKeysRepository.updateFailedAttempts(update);
            ResponseBuilder.fail(response, BusinessActionEnum.CONFIRM_TRANSFER.getCode(),
                    "Account permanently locked, please contact support");

        } else if (newRetryCount >= 3) {
            update.setStatus(IdempotencyKeysStatusEnum.TIMED_LOCKOUT);
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
                        updateIdempotencyKeys.setReferenceId(request.getReferenceId());
                        updateIdempotencyKeys.setStatus(request.getStatus());
                        updateIdempotencyKeys.setRetryCount(request.getRetryCount());
                        int rows = idempotencyKeysRepository.updateIdempotencyKeysByReferenceId(updateIdempotencyKeys);

                        IdempotencyKeys idempotencyKeys = idempotencyKeysRepository.queryIdempotencyKeysByReferenceId(request.getReferenceId());
                        if (rows > 0) {
                            UpdateIdempotencyKeysResult updateIdempotencyKeysResult = new UpdateIdempotencyKeysResult();
                            updateIdempotencyKeysResult.setReferenceId(idempotencyKeys.getReferenceId());
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
                        try {
                            // getOrCreateStripeCustomer
                            String stripeCustomerId = getOrCreateStripeCustomer(userId);

                            // then build payment intent params
                            PaymentIntentCreateParams.Builder paramsBuilder =
                                    PaymentIntentCreateParams.builder()
                                            .setAmount(request.getAmount()
                                                    .multiply(BigDecimal.valueOf(100)).longValue())
                                            .setCurrency(request.getCurrency().toLowerCase())
                                            .setCustomer(stripeCustomerId)
                                            .putMetadata("userId", userId)
                                            .putMetadata("saveCard", String.valueOf(request.isSaveCard()));

                            // if the user wants to save the card
                            if (request.isSaveCard()) {
                                paramsBuilder.setSetupFutureUsage(
                                        PaymentIntentCreateParams.SetupFutureUsage.OFF_SESSION);
                            }

                            // then we build paymentIntent which is a stripe constructor for creating a PaymentIntent
                            PaymentIntent paymentIntent;
                            paymentIntent = PaymentIntent.create(paramsBuilder.build());

                            // then we pass the client secret which is result of create and return to frontend to
                            // pass into payment using the card
                            TopUpResult result = new TopUpResult();
                            result.setSecretClient(paymentIntent.getClientSecret());
                            result.setPaymentIntentId(paymentIntent.getId());

                            ResponseBuilder.success(response, result,
                                    BusinessActionEnum.CREATE_TOP_UP_INTENT.getCode(),
                                    BusinessActionEnum.CREATE_TOP_UP_INTENT.getDesc());


                        } catch (StripeException | JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    @Override
    public BusinessBizResult<String> publishTopUp(PublishTopUpRequest request) {
        return businessServiceTemplate.execute(request, BusinessActionEnum.PUBLISH_TOP_UP,
                new BusinessBizCallback<>() {

            @Override
            protected BusinessBizResult<String> createDefaultResponse() {
                return new BusinessBizResult<>();
            }

            @Override
            protected void checkParams(PublishTopUpRequest request) {

            }

            @Override
            protected void process(PublishTopUpRequest request,
                                   BusinessBizResult<String> response) {

                // 1 verify the signature from the topUpInit result, using the webhook and signature
                Event event;
                try {
                    event = Webhook.constructEvent(request.getPayload(), request.getStripeSignature(),
                            webhookSecret);
                } catch (SignatureVerificationException e) {
                    logger.warn("Invalid Stripe webhook signature: {}",
                            e.getMessage());
                    ResponseBuilder.fail(response,
                            BusinessActionEnum.PUBLISH_TOP_UP.getCode(),
                            "Invalid signature");
                    return;
                }

                // ensure payment intent is payment intent success.
                if (!"payment_intent.succeeded".equals(event.getType())) {
                    ResponseBuilder.success(response, "ignored",
                            BusinessActionEnum.PUBLISH_TOP_UP.getCode(),
                            "Event not handled");
                    return;
                }

                PaymentIntent intent = (PaymentIntent) event
                        .getDataObjectDeserializer().getObject()
                        .orElseThrow(() -> new BusinessException(BusinessResultCode.SYSTEM_EXCEPTION,
                        "failed to deserialize payment_intent"));

                String userId = intent.getMetadata().get("userId");
                boolean saveCard = Boolean.parseBoolean(
                        intent.getMetadata().get("saveCard"));
                BigDecimal amount = BigDecimal.valueOf(intent.getAmount())
                        .divide(BigDecimal.valueOf(100));
                String currency = intent.getCurrency().toUpperCase();

                // 3. Idempotency guard — Stripe fires webhooks at least once,
                //    sometimes twice. Also handles Flow B sync credit duplicate.
                // can we set the first few numbers to idemptify that tis apymentIntentId, because other idempotencyKey is nroamlly frontedn generated
                if (idempotencyKeysRepository
                        .existsByPaymentIntentId(intent.getId())) {
                    logger.info("Duplicate webhook for pi={}, skipping",
                            intent.getId());
                    ResponseBuilder.success(response, "duplicate",
                            BusinessActionEnum.PUBLISH_TOP_UP.getCode(),
                            "Already processed");
                    return;
                }

                // 4. Save card token if user requested it
                if (saveCard && intent.getPaymentMethod() != null) {
                    try {
                        PaymentMethod pm = PaymentMethod.retrieve(intent.getPaymentMethod());

                        InsertNewCardRequest insertNewCardRequest = new InsertNewCardRequest();

                        insertNewCardRequest.setUserId(userId);
                        insertNewCardRequest.setGmtCreate(new Date());
                        insertNewCardRequest.setGmtModified(new Date());

                        // Provider (always Stripe in this flow)
                        insertNewCardRequest.setProvider(Provider.STRIPE);

                        // Token + customer mapping
                        insertNewCardRequest.setProviderToken(intent.getPaymentMethod());
                        insertNewCardRequest.setStripeCustomerId(intent.getCustomer());

                        insertNewCardRequest.setLast4(pm.getCard().getLast4());
                        insertNewCardRequest.setCardIssuer(
                                CardIssuer.valueOf(pm.getCard().getBrand().toUpperCase())
                        );
                        insertNewCardRequest.setExpiryMonth(pm.getCard().getExpMonth().intValue());
                        insertNewCardRequest.setExpiryYear(pm.getCard().getExpYear().intValue());
                        insertNewCardRequest.setCardHolderName(pm.getBillingDetails() != null
                                ? pm.getBillingDetails().getName()
                                : null);
                        if (pm.getCard().getFunding() != null) {
                            switch (pm.getCard().getFunding()) {
                                case "DEBIT":
                                    insertNewCardRequest.setCardType(CardType.DEBIT);
                                    break;
                                case "CREDIT":
                                    insertNewCardRequest.setCardType(CardType.CREDIT);
                                    break;
                                default:
                                    throw new IllegalStateException("Illegal card type");
                            }
                        } else {
                            insertNewCardRequest.setCardType(CardType.CREDIT);
                        }
                        insertNewCardRequest.setCardStatus(CardStatus.ACTIVE);
                        insertNewCardRequest.setDefault(true);
                        userServiceClient.insertNewCard(insertNewCardRequest);
                    } catch (StripeException e) {
                        // non-fatal — top-up still proceeds
                        logger.warn("Failed to save card for userId={}", userId, e);
                    }
                }

                // 5. Mark idempotency before publishing — prevents double credit
                IdempotencyKeys idempotencyKeys = new IdempotencyKeys();
                idempotencyKeys.setIdempotencyKey(intent.getId());
                idempotencyKeys.setUserId(Long.valueOf(userId));
                try {
                    idempotencyKeys.setRequestHash(HashUtil.
                            generateIdempotentRequestHash(intent.getAmount(), intent.getCurrency(),
                                    intent.getCustomer()));
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
                idempotencyKeys.setStatus(IdempotencyKeysStatusEnum.PENDING);
                idempotencyKeys.setRetryCount(0);
                idempotencyKeys.setCreatedAt(new Date());
                idempotencyKeys.setUpdatedAt(new Date());
                idempotencyKeysRepository.insertIdempotencyKey(idempotencyKeys);

                // 6. Publish EC_TOPUP_RECEIVED → AccountCenter credits balance
                //    Same Kafka pipeline as EC_TRANSACTION_RESULT
                EcTopUpEvent topUpEvent = new EcTopUpEvent();
                topUpEvent.setUserId(userId);
                topUpEvent.setAmount(amount);
                topUpEvent.setCurrency(currency);
                topUpEvent.setPaymentIntentId(intent.getId());
                topUpEvent.setGmtTaskOccur(System.currentTimeMillis());

                kafkaTemplate.send("EC_TOPUP_RECEIVED", topUpEvent);

                logger.info("EC_TOPUP_RECEIVED published userId={} amount={} {}",
                        userId, amount, currency);

                ResponseBuilder.success(response, "received",
                        BusinessActionEnum.PUBLISH_TOP_UP.getCode(),
                        BusinessActionEnum.PUBLISH_TOP_UP.getDesc());

                }
        });
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
                                            .setCustomer(card.getStripeCustomerId())
                                            .setPaymentMethod(card.getProviderToken()) // pm_xxx
                                            .setConfirm(true)       // charge immediately
                                            .setOffSession(true)    // user NOT present
                                            .putMetadata("userId", userId)
                                            .putMetadata("saveCard", "false")
                                            .putMetadata("type", "AUTO_RELOAD")
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

    /**
     * return or insert new stripe customer id
     * @param userId
     * @return
     * @throws StripeException
     * @throws JsonProcessingException
     */
    private String getOrCreateStripeCustomer(String userId) throws StripeException, JsonProcessingException {
        // Check local DB first — one Stripe Customer per user, never duplicate
        QueryUserInfoRequest queryUserInfoRequest = new QueryUserInfoRequest();
        queryUserInfoRequest.setUserId(userId);
        UserBizResult<UserInfoItem> userInfo = userServiceClient.queryUserInfo(queryUserInfoRequest);
        String customerId = null;
        ObjectMapper objectMapper = new ObjectMapper();

        if (userInfo.getResult() != null && userInfo.getResult().getExtInfo() != null) {
            // parse extInfo JSON to get stripeCustomerId
            ExtInfo extInfo = objectMapper.readValue(
                    userInfo.getResult().getExtInfo(), ExtInfo.class);
            customerId = extInfo.getStripeCustomerId();
        }

        if (customerId == null) {
            // first time — create on Stripe and save back to extInfo
            Customer customer = Customer.create(
                    CustomerCreateParams.builder()
                            .putMetadata("userId", userId)
                            .build());

            ExtInfo extInfo = userInfo.getResult().getExtInfo() != null
                    ? objectMapper.readValue(userInfo.getResult().getExtInfo(), ExtInfo.class)
                    : new ExtInfo();

            extInfo.setStripeCustomerId(customer.getId());

            UpdateUserInfoRequest updateRequest = new UpdateUserInfoRequest();
            updateRequest.setUserId(userId);
            updateRequest.setExtInfo(objectMapper.writeValueAsString(extInfo));
            userServiceClient.updateExtInfo(updateRequest);

            customerId = customer.getId();
        }

        return customerId;
    }


}



