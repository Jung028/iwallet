package com.alipay.business.web;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("(execution(* com.alipay.usercenter..controller..*.*(..)) || " +
              "execution(* com.alipay.usercenter..service..*.*(..)) || " +
              "execution(* com.alipay.usercenter..biz..*.*(..))) && " +
              "!within(jakarta.servlet.Filter+) && " +
              "!within(org.springframework.web.filter.GenericFilterBean+) && " +
              "!@within(org.springframework.context.annotation.Configuration)")
    public void allMethods() {}

    @Around("allMethods()")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        
        // Log entry
        logger.info("Entering: {}", methodName);
        
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;
            
            // Log exit
            logger.info("Exiting: {} ({}ms)", methodName, executionTime);
            return result;
        } catch (Throwable e) {
            long executionTime = System.currentTimeMillis() - start;
            // Log error
            logger.error("Error in: {} ({}ms). Message: {}", methodName, executionTime, e.getMessage());
            throw e;
        }
    }
}