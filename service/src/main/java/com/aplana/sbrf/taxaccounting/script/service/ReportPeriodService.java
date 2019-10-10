package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodType;
import com.aplana.sbrf.taxaccounting.model.log.LogLevelType;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.Calendar;
import java.util.List;

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
     * Получение записи справочника "Коды, определяющие налоговый (отчётный) период" по идентификатору.
     */
    ReportPeriodType getReportPeriodTypeById(Long id);

    /**
     * Получить сформированное поле периода
     *
     * @param ids список ПНФ
     * @param logLevelType тип операции
     * @return строка периода
     */
    String createLogPeriodFormatById(List<Long> ids, LogLevelType logLevelType);
}
