package com.aplana.sbrf.taxaccounting.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для обработки задач планировщика
 * Помечается метод, который нужно выполнить по указанному расписанию
 * @author dloshkarev
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AplanaScheduled {
    /**
     * Код строки из таблицы CONFIGURATION, в которой содержится расписание в формате CRON для выполнения задачи планировщика
     */
    String settingCode();
}
