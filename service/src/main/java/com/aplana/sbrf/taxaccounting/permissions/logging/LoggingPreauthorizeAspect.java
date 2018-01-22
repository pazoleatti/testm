package com.aplana.sbrf.taxaccounting.permissions.logging;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Аспект который на основании результата {@link LoggingPermissionChecker} принимает решение о наличии прав доступа
 * над вызываемой операцией
 */
@Aspect
@Configurable
public class LoggingPreauthorizeAspect {

    @Autowired
    private LoggingPermissionCheckerFactory loggingPermissionCheckerFactory;

    @Around(value = "execution (* com.aplana.sbrf.taxaccounting.service.impl..*(..)) && @annotation(com.aplana.sbrf.taxaccounting.permissions.logging.LoggingPreauthorize)")
    public Object preAuthorize(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        Object[] args = joinPoint.getArgs();

        Logger logger = null;
        DeclarationData declarationData = null;
        TAUserInfo userInfo = null;
        for (Object arg : args) {
            if (arg instanceof Logger) {
                logger = (Logger) arg;
            } else if (arg instanceof TAUserInfo) {
                userInfo = (TAUserInfo) arg;
            } else if (arg instanceof DeclarationData) {
                declarationData = (DeclarationData) arg;
            }
        }

        LoggingPreauthorizeType type = methodSignature.getMethod().getAnnotation(LoggingPreauthorize.class).preauthorizeType();

        LoggingPermissionChecker loggingPermissionChecker = loggingPermissionCheckerFactory.getLoggingPermissionChecker(type);

        if (loggingPermissionChecker.check(logger, userInfo, declarationData)) {
            return joinPoint.proceed();
        } else {
            throw new ServiceException("");
        }
    }
}
