package com.aplana.sbrf.taxaccounting.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import java.util.Set;

/**
 * Аспект валидации аргументов и результата методов
 *
 * @author Dmitriy Levykin
 */
@Aspect
public class ValidationAspect {
    private ValidatorFactory validatorFactory;

    public ValidationAspect() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    // Валидация возвращаемого результата
    @Around("execution(@javax.validation.constraints.* * * (..))")
    public Object afterReturning(ProceedingJoinPoint aJoinPoint) throws Throwable {
        Object theReturnValue = aJoinPoint.proceed();
        MethodSignature theSignature = (MethodSignature) aJoinPoint.getSignature();
        ExecutableValidator theValidator = validatorFactory.getValidator().forExecutables();
        Set<ConstraintViolation<Object>> theViolations = theValidator.validateReturnValue(aJoinPoint.getTarget(),
                theSignature.getMethod(), theReturnValue);
        if (theViolations.size() > 0) {
            throw new ConstraintViolationException(theViolations);
        }
        return theReturnValue;
    }

    // Валидация параметров
    @Around("execution(* * (.., @javax.validation.constraints.* (*), ..))")
    public Object pointcutMethodArgument(ProceedingJoinPoint aJoinPoint) throws Throwable {
        return validateInvocation(aJoinPoint);
    }

    // Валидация
    private Object validateInvocation(ProceedingJoinPoint aJoinPoint) throws Throwable {
        MethodSignature theSignature = (MethodSignature) aJoinPoint.getSignature();

        ExecutableValidator theValidator = validatorFactory.getValidator().forExecutables();
        Set<ConstraintViolation<Object>> theViolations = theValidator.validateParameters(aJoinPoint.getTarget(),
                theSignature.getMethod(), aJoinPoint.getArgs());
        if (theViolations.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();

            for (ConstraintViolation v : theViolations) {
                stringBuilder.append(String.format("%s#%s: %s; \r\n", v.getRootBeanClass().getName(),
                        v.getPropertyPath().toString(), v.getMessage()));
            }

            throw new ConstraintViolationException(stringBuilder.toString(), theViolations);
        }

        return aJoinPoint.proceed();
    }
}
