package com.aplana.sbrf.taxaccounting.web.spring.json;

import java.lang.annotation.*;

/**
 * С помощью данной аннотации можно задать набор фильтров, которые будет применяться в json серилизации в рамках
 * данного запроса
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonFilterDescription {

    /**
     * id фильтра
     */
    public String name();

    /**
     * Описание фильтра (список полей, которые он пропустит в json-объект)
     */
    public String[] fields() default {};

    /**
     * Предустановленный фильтр
     */
    public JsonPredefinedFilter filter() default JsonPredefinedFilter.NONE;

}
