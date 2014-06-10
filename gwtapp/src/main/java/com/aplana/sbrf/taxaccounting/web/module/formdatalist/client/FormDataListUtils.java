package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.FormDataSearchOrdering;

/**
 * Вспомогательные методы и поля для представления "Список налоговых форм"
 *
 * @author aivanov
 * @since 10.06.2014
 */
public class FormDataListUtils {

    public static final String FORM_DATA_KIND_TITLE = "Тип налоговой формы";
    public static final String FORM_DATA_KIND_TITLE_D = "Тип формы";
    public static final String FORM_DATA_TYPE_TITLE = "Вид налоговой формы";
    public static final String FORM_DATA_TYPE_TITLE_D = "Вид формы";
    public static final String DEPARTMENT_TITLE = "Подразделение";
    public static final String PERIOD_YEAR_TITLE = "Год";
    public static final String REPORT_PERIOD_TITLE = "Период";
    public static final String PERIOD_MONTH_TITLE = "Месяц";
    public static final String FORM_DATA_STATE_TITLE = "Состояние";
    public static final String FORM_DATA_RETURN_TITLE = "Признак возрата";

    public static final String FORM_DATA_CREATE = "Создать налоговую форму...";
    public static final String FORM_DATA_CREATE_D = "Создать форму...";
    public static final String FORM_DATA_CREATE_TITLE = "Создание налоговой формы";
    public static final String FORM_DATA_CREATE_TITLE_D = "Создание формы";


    public static FormDataSearchOrdering getSortByColumn(String sortByColumn){
        return FormDataSearchOrdering.valueOf(sortByColumn);
    }
}
