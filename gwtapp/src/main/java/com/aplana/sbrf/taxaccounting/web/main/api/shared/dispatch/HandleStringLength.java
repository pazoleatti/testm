package com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * используется аспектом QueryStringLengthAspect для того чтобы обрезать указанные строковые поля в массиве fieldNames,
 * до размера указанного в переменной stringLength
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HandleStringLength {
    String[] fieldNames();
    int stringLength() default 1000;
}
