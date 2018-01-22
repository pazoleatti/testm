package com.aplana.sbrf.taxaccounting.permissions.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Служит для обозначения методов для которых необходимо запустить цепочку проверок
 * {@link com.aplana.sbrf.taxaccounting.permissions.logging.units.CheckUnit}
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface LoggingPreauthorize {
    LoggingPreauthorizeType preauthorizeType();
}
