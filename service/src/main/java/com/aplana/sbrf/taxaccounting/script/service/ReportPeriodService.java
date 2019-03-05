package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.Calendar;

@ScriptExposed
public interface ReportPeriodService {

    /**
     * Получение {@link ReportPeriod} по идентификатору
     *
     * @param reportPeriodId идентификатор
     * @return объект {@link ReportPeriod} или
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если периода с заданным идентификатором не существует
     */
    ReportPeriod get(int reportPeriodId);

    /**
     * Получение даты начала отчетного периода
     *
     * @param reportPeriodId идентификатор отчетного периода
     * @return объект {@link Calendar} или null
     */
    Calendar getStartDate(int reportPeriodId);

    /**
     * Возвращает календарную дату начала отчетного периода
     *
     * @param reportPeriodId идентификатор отчетного периода
     * @return объект {@link Calendar} или null
     */
    Calendar getCalendarStartDate(int reportPeriodId);

    /**
     * Возвращает дату конца отчетного периода
     *
     * @param reportPeriodId идентификатор отчетного периода
     * @return объект {@link Calendar} или null
     */
    Calendar getEndDate(int reportPeriodId);

    /**
     * Получение отчетного периода по коду записи справочника "Коды, определяющие налоговый (отчётный) период" и году
     *
     * @param code код записи справочника "Коды, определяющие налоговый (отчётный) период"
     * @param year год отчетного периода
     * @return объект {@link ReportPeriod} или null
     */
    ReportPeriod getByTaxTypedCodeYear(TaxType taxType, String code, int year);

    /**
     * Получение записи справочника "Коды, определяющие налоговый (отчётный) период" по идентификатору.
     */
    ReportPeriodType getReportPeriodTypeById(Long id);
}
