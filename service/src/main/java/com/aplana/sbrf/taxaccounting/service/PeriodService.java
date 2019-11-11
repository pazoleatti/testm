package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.OpenCorrectionPeriodAction;
import com.aplana.sbrf.taxaccounting.model.result.*;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import net.sf.jasperreports.web.actions.ActionException;

import java.util.List;

/**
 * Сервис для работы с отчетными периодами
 */
public interface PeriodService {

    /**
     * Открываем отчетный период для департамента.
     * Логика описана в аналитике - Ведение периодов
     *
     * @param period   отчетный период подразделения, который необходимо открыть
     * @param userInfo пользователь запустивший операцию
     * @return {@link OpenPeriodResult}
     */
    OpenPeriodResult open(DepartmentReportPeriod period, TAUserInfo userInfo);

    /**
     * Открыть Корректирующий период
     *
     * @param action данные по корректирующему периоду
     * @return {@link OpenPeriodResult}
     */
    OpenPeriodResult openCorrectionPeriod(OpenCorrectionPeriodAction action);

    /**
     * Открывает периоды для одного подразделения, чтобы они соответствовали периодам ТерБанка
     *
     * @param departmentId идентифиактор подразделения
     */
    void openForNewDepartment(int departmentId);

    /**
     * Переоткрывает закрытый период
     *
     * @param departmentReportPeriodId период
     * @return {@link ReopenPeriodResult}
     */
    ReopenPeriodResult reopen(Integer departmentReportPeriodId);

    /**
     * Закрыть период
     *
     * @param departmentReportPeriodId идентификатор периода для подразделения "Банк"
     * @param skipHasNotAcceptedCheck  пропускает проверку наличия форм в состоянии отличном от "Принято"
     * @return {@link ClosePeriodResult}
     */
    ClosePeriodResult close(Integer departmentReportPeriodId, boolean skipHasNotAcceptedCheck);

    /**
     * Удалить отчетный период
     *
     * @param id идентификатор периода
     * @return {@link DeletePeriodResult}
     */
    DeletePeriodResult delete(Integer id);

    /**
     * Получить объект отчётного периода по идентификатору периода
     *
     * @param reportPeriodId идентификатор отчётного периода
     * @return объект {@link ReportPeriod}
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если периода с заданным идентификатором не существует
     */
    ReportPeriod fetchReportPeriod(int reportPeriodId);

    /**
     * Получение списка всех отчётных периодов.
     */
    List<ReportPeriod> findAll();

    /**
     * Получение списка всех отчётных периодов для 2-НДФЛ (ФЛ)
     */
    List<ReportPeriod> findAllFor2NdflFL();

    /**
     * Получение записи справочника "Коды, определяющие налоговый (отчётный) период" по идентификатору
     *
     * @param id идентификатор
     * @return объект {@link ReportPeriodType}
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если записи справочника с таким id не существует
     */

    ReportPeriodType getPeriodTypeById(Long id);


    /**
     * Устанавливает дату сдачи отчетности для периода подразделения
     *
     * @param filter фильтр с датой сдачи отчетности {@link DepartmentReportPeriodFilter#getDeadline()}
     * @throws ActionException если дата сдачи отчетности не указана
     */
    void updateDeadline(DepartmentReportPeriodFilter filter) throws ActionException;

    /**
     * Получение списка отчетных периодов для указанных подразделений
     *
     * @param departmentList Список подразделений
     * @return Список {@link ReportPeriod} или пустой список
     */
    List<ReportPeriod> getPeriodsByDepartments(List<Integer> departmentList);


    /**
     * Проверяет существование периода для подразделения
     *
     * @param departmentId подразделение, для которого осуществляется проверка существования периода
     * @return true - существует, false - не существует
     */
    boolean existForDepartment(int departmentId, int reportPeriodId);

    /**
     * http://conf.aplana.com/pages/viewpage.action?pageId=11382680
     *
     * @param user пользователь
     * @return множество {@link ReportPeriod} или пустое множество
     */
    List<ReportPeriod> findAllActive(TAUser user);

    /**
     * Получить корректирующие периоды
     *
     * @param departmentId идентификатор подразделения
     * @return список корректирующих периодов
     */
    List<ReportPeriod> getCorrectPeriods(int departmentId);

    /**
     * Получение списка всех записей справочника "Коды, определяющие налоговый (отчётный) период"
     *
     * @return список {@link ReportPeriodType} или пустой список
     */
    List<ReportPeriodType> getPeriodType();

    /**
     * Получить открытые периода назначеннных подразделению
     *
     * @param departmentId идентификатор подразделения
     * @return период с датой корректировки
     */
    List<ReportPeriodResult> fetchActiveByDepartment(Integer departmentId);

    /**
     * Получить сформированное поле периода
     *
     * @param declarationsIds список КНФ
     * @param logLevelType тип операции
     * @return период для логов
     */
    String createLogPeriodFormatById(List<Long> declarationsIds, Integer logLevelType);

    /**
     * Проверка на то, что тип отчетного периода соответствует значению "год" или
     * "год при реорганизации (ликвидации) организации"
     *
     * @param reportPeriod отчетный период
     * @return true, если тип отчетного период соответствует значению "год" или
     * "год при реорганизации (ликвидации) организации"
     */
    boolean isYearPeriodType(ReportPeriod reportPeriod);

    /**
     * Проверка на то, что тип отчетного периода соответствует значению "год" или
     * "год при реорганизации (ликвидации) организации"
     *
     * @param periodType тип отчетного периода
     * @return true, если тип отчетного период соответствует значению "год" или
     * "год при реорганизации (ликвидации) организации"
     */
    boolean isYearPeriodType(ReportPeriodType periodType);

    /**
     * Проверка на то, что тип НФ является 6-НДФЛ или 2-НДФЛ(1)
     *
     * @param reportPeriod отчтетный период
     * @return true, если тип НФ является 6-НДФЛ или 2-НДФЛ(1)
     */
    boolean is6NdflOr2Ndfl1TaxFormType(ReportPeriod reportPeriod);
}
