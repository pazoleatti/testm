package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Получение в скрипте признака возможности формирования отчетов для декларации
 * @author LHaziev
 */
public class DeclarationShowReport implements Serializable {
    private static final long serialVersionUID = -215843658318486124L;

    /** Признак возможности формирования отчетов для декларации */
    private boolean isShowReport = true;

    public boolean isShowReport() {
        return isShowReport;
    }

    public void setShowReport(boolean isShowReport) {
        this.isShowReport = isShowReport;
    }
}
