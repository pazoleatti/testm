package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodType;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.action.OpenCorrectionPeriodAction;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import net.sf.jasperreports.web.actions.ActionException;

import java.util.List;
import java.util.Set;

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
     * @return uuid логов
     */
    String open(DepartmentReportPeriod period, TAUserInfo userInfo);

    /**
     * Закрыть период
     *
     * @param departmentReportPeriodId идентификатор периода для подразделения "Банк"
     * @return uuid логгера
     */
    String close(Integer departmentReportPeriodId);

    /**
     * Получить объект отчётного периода по идентификатору периода
     *
     * @param reportPeriodId идентификатор отчётного периода
     * @return объект {@link ReportPeriod}
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если периода с заданным идентификатором не существует
     */
    ReportPeriod fetchReportPeriod(int reportPeriodId);

    /**
     * Переоткрывает закрытый период
     *
     * @param departmentReportPeriodId период
     * @return uuid логера
     */
    String reopen(Integer departmentReportPeriodId);

    /**
     * Удалить отчетный период
     *
     * @param id идентификатор периода
     * @return uuid логера
     */
    String delete(Integer id);

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

    enum Operation {
        FIND, // Поиск периода
        OPEN, // Открытие периода
        CLOSE, // Закрытие периода
        DELETE, // Удаление периода
        EDIT_DEADLINE, // Изменение срока сдачи отчетности в периоде
        EDIT // Редактирование периода
    }

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
    Set<ReportPeriod> getOpenReportPeriodForUser(TAUser user);

    /**
     * Список открытых периодов
     *
     * @param departmentList подразделения
     * @param withoutCorrect true - без корректирующих периодов false - с корректирующими периодами
     * @return список {@link ReportPeriod} или пустой список
     */
    List<ReportPeriod> getOpenPeriodsByDepartments(List<Integer> departmentList, boolean withoutCorrect);

    /**
     * Получить корректирующие периоды
     *
     * @param departmentId идентификатор подразделения
     * @return список корректирующих периодов
     */
    List<ReportPeriod> getCorrectPeriods(int departmentId);

    /**
     * Открыть Корректирующий период
     *
     * @param action данные по корректирующему периоду
     * @return logs логер, при необходимости
     */
    String openCorrectionPeriod(OpenCorrectionPeriodAction action);

    /**
     * Получение отчетных периодов подразделения по списку идентификаторов подразделений
     *
     * @param departmentIds список идентификаторов подразделений
     * @return список {@link DepartmentReportPeriod} или пустой список
     */
    List<DepartmentReportPeriod> getDRPByDepartmentIds(List<Integer> departmentIds);

    /**
     * Получение отчетного периода по коду записи справочника "Коды, определяющие налоговый (отчётный) период" и году
     *
     * @param code код записи справочника "Коды, определяющие налоговый (отчётный) период"
     * @param year год отчетного периода
     * @return объект {@link ReportPeriod} или null
     */
    ReportPeriod getByDictCodeAndYear(String code, int year);

    /**
     * Получение списка всех записей справочника "Коды, определяющие налоговый (отчётный) период"
     *
     * @return список {@link ReportPeriodType} или пустой список
     */
    List<ReportPeriodType> getPeriodType();
}
