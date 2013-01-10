package com.aplana.sbrf.taxaccounting.util;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Используется для пометки бинов, реализации которых должны инжектиться в groovy-скрипты
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScriptExposed {
	/**
	 * Типы налогов, для которых доступен данный бин.
	 */
	TaxType[] taxTypes() default {};

	/**
	 * События форм, для скриптов которых, доступен данный бин.
	 */
	FormDataEvent[] formDataEvents() default {};
}
