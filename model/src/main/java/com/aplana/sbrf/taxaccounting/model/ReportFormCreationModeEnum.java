package com.aplana.sbrf.taxaccounting.model;

/**
 * Перечисление, задающее какие ОНФ нужно создавать
 */
public enum ReportFormCreationModeEnum {
    BY_ALL_DATA, // По всем данным
    BY_NEW_DATA, // По новым данным
    UNACCEPTED_BY_FNS // Для отчетных форм, не принятых ФНС
}
