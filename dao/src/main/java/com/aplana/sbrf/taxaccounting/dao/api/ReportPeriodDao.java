package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.dao.PermissionDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodType;
import com.aplana.sbrf.taxaccounting.model.result.ReportPeriodResult;

import java.util.Date;
import java.util.List;

/**
 * Интерфейс DAO для работы с {@link ReportPeriod}
 *
 * @author dsultanbekov
 */
public interface ReportPeriodDao extends PermissionDao {

    /**
     * Получить объект отчётного периода по идентификатору периода
     *
     * @param reportPeriodId идентификатор отчётного периода
     * @return объект {@link ReportPeriod}
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если периода с заданным идентификатором не существует
     */
    ReportPeriod fetchOne(Integer reportPeriodId);

    /**
     * Получение отчетного периода по налоговому периоду и периоду в справочнике "Коды, определяющие налоговый (отчётный) период"
     *
     * @param taxPeriodId     идентификатор налогового периода
     * @param dictTaxPeriodId идентификатор записи справочника "Коды, определяющие налоговый (отчётный) период"
     * @return объект {@link ReportPeriod} или null
     */
    ReportPeriod fetchOneByTaxPeriodAndDict(int taxPeriodId, long dictTaxPeriodId);

    /**
     * Получение списка отчётных периодов, входящий в данный налоговый период.
     * Список отсортирован по {@link ReportPeriod#getOrder()} порядковым номерам отчётных периодов
     *
     * @param taxPeriodId идентификатор налогового периода
     * @return список {@link ReportPeriod} или пустой список
     */
    List<ReportPeriod> fetchAllByTaxPeriod(int taxPeriodId);

    /**
     * Создание нового отчетного периода
     *
     * @param reportPeriod отчётный период
     * @return идентификатор нового отчетного периода
     */
    Integer create(ReportPeriod reportPeriod);

    /**
     * Удалениие периода
     *
     * @param reportPeriodId идентификатор периода
     */
    void remove(int reportPeriodId);

    /**
     * Получение списка отчетных периодов для указанных подразделений
     *
     * @param departmentList Список подразделений
     * @return Список {@link ReportPeriod} или пустой список
     */
    List<ReportPeriod> fetchAllByDepartments(List<Integer> departmentList);

    /**
     * Возвращает все периоды которые либо пересекаются с указанным диапазоном дат, либо полностью находятся внутри него
     *
     * @param startDate начало диапазона
     * @param endDate   конец диапазона
     * @return список {@link ReportPeriod}
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если не найдены отчетные периоды
     */
    @Deprecated
    List<ReportPeriod> getReportPeriodsByDate(Date startDate, Date endDate);

    /**
     * Получение списка открытых периодов по списку подразделений и признаку корректировки
     *
     * @param departmentList список подразделений
     * @param withoutCorrect признак отсутствия корректировки(true - без корректировки/false - все периоды)
     * @return список {@link ReportPeriod} или пустой список
     */
    List<ReportPeriod> getOpenPeriodsAndDepartments(List<Integer> departmentList, boolean withoutCorrect);

    /**
     * Получить корректирующие периоды
     *
     * @param departmentId идентификатор подразделения
     * @return список {@link ReportPeriod} или пустой список
     */
    List<ReportPeriod> getCorrectPeriods(int departmentId);

    /**
     * Получение отчетного периода по коду записи справочника "Коды, определяющие налоговый (отчётный) период" и году
     *
     * @param code код записи справочника "Коды, определяющие налоговый (отчётный) период"
     * @param year год отчетного периода
     * @return объект {@link ReportPeriod} или null
     */
    ReportPeriod getByTaxTypedCodeYear(String code, int year);


    /**
     * Получение списка всех записей справочника "Коды, определяющие налоговый (отчётный) период"
     *
     * @return список {@link ReportPeriodType} или пустой список
     */
    List<ReportPeriodType> getPeriodType();

    /**
     * Получение записи справочника "Коды, определяющие налоговый (отчётный) период" по идентификатору
     *
     * @param id идентификатор
     * @return объект {@link ReportPeriodType}
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если записи справочника с таким id не существует
     */
    ReportPeriodType getReportPeriodTypeById(Long id);

    /**
     * Получить открытые периода назначеннных подразделению
     * @param departmentId идентификатор подразделения
     * @return период с датой корректировкиhttps://jira.aplana.com/browse/SBRFNDFL-5117
     */
    List<ReportPeriodResult> fetchActiveByDepartment(Integer departmentId);
}