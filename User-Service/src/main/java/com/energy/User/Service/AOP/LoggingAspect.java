package com.energy.User.Service.AOP;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;

/* Aspect-oriented programming (AOP) means putting behavior shared by many methods into a separate component
   and applying it automatically. */
@Aspect
@Configuration
@Slf4j
public class LoggingAspect {
    @Pointcut("execution(* com.energy.User.Service.service.*.*(..))") // All Methods in any class in service folder
    public void serviceMethods() {}

    @Before("serviceMethods()") // At the beginning of every method
    public void logBefore(JoinPoint joinPoint) {
        log.info("Called service method: {} with arguments: {}",
                joinPoint.getSignature().getName(), joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) { // At the end of every method
        log.info("Service method: {}, returned: {}",
                joinPoint.getSignature().getName(), result);
    }
}

