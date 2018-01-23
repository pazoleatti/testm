package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import net.sf.jasperreports.web.actions.ActionException;

import java.util.Calendar;
import java.util.Date;
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
     * @param period - Отчетный период подразделения, который необходимо открыть
     * @return uuid логов
     */
    String open(DepartmentReportPeriod period);

    /**
     * Создание нового отчетного периода подразделения или открытие существующего по комбинации параметров
     * - Подразделение
     * - Отчетный период
     * - Дата корректировки
     *
     * @param departmentReportPeriod отчетный период подразделения
     * @param logs                   логгер
     */
    void saveOrOpen(DepartmentReportPeriod departmentReportPeriod, List<LogEntry> logs);


    /**
     * Закрыть период
     *
     * @param departmentReportPeriodId - идентификатор периода для подразделения "Банк"
     * @return uuid логгера
     */
    String close(Integer departmentReportPeriodId);

    /**
     * Получение списка отчётных периодов, входящий в данный налоговый период.
     * Список отсортирован по {@link ReportPeriod#getOrder()} порядковым номерам отчётных периодов
     *
     * @param taxPeriodId идентификатор налогового периода
     * @return список {@link ReportPeriod} или пустой список
     */
    List<ReportPeriod> fetchAllByTaxPeriod(int taxPeriodId);

    /**
     * Получить объект отчётного периода по идентификатору периода
     *
     * @param reportPeriodId идентификатор отчётного периода
     * @return объект {@link ReportPeriod}
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если периода с заданным идентификатором не существует
     */
    ReportPeriod fetchReportPeriod(int reportPeriodId);

    /**
     * Возвращает дату начала отчетного периода
     *
     * @param reportPeriodId идентификатор отчетного периода
     * @return объект {@link Calendar} или null
     */
    Calendar getStartDate(int reportPeriodId);

    /**
     * Возвращает дату конца отчетного периода
     *
     * @param reportPeriodId идентификатор отчетного периода
     * @return объект {@link Calendar} или null
     */
    Calendar getEndDate(int reportPeriodId);

    /**
     * Возвращает "отчетную дату" если требуется в чтз
     * Отчетная дата = дата конца периода + 1 день
     *
     * @param reportPeriodId идентификатор отчетного периода
     * @return объект {@link Calendar} или null
     */
    // TODO: возможно имеется в виду дата сдачи отчетности. Надо проверить (Marat Fayzullin 22.01.2014)
    Calendar getReportDate(int reportPeriodId);


    /**
     * Получить дату начала месяца.
     *
     * @param reportPeriodId идентификатор отчетного период
     * @param periodOrder    очередность месяца в периоде (значение из formData.periodOrder)
     * @return объект {@link Calendar} или null
     */
    Calendar getMonthStartDate(int reportPeriodId, int periodOrder);

    /**
     * Получить дату окончания месяца.
     *
     * @param reportPeriodId идентификатор отчетного период
     * @param periodOrder    очередность месяца в периоде (значение из formData.periodOrder)
     * @return объект {@link Calendar} или null
     */
    Calendar getMonthEndDate(int reportPeriodId, int periodOrder);

    /**
     * Получить отчетную дату месяца.
     *
     * @param reportPeriodId идентификатор отчетного период
     * @param periodOrder    очередность месяца в периоде (значение из formData.periodOrder)
     * @return объект {@link Calendar} или null
     */
    Calendar getMonthReportDate(int reportPeriodId, int periodOrder);

    /**
     * Удалить отчетный период
     *
     * @param drpId  идентификатор периода
     * @param logger логер, при необходимости
     * @param user   пользователь, который выполняет действие
     */
    @Deprecated
    void removeReportPeriod(Integer drpId, Logger logger, TAUserInfo user);

    /**
     * Удалить отчетный период
     *
     * @param id       идентификатор периода
     * @param userInfo пользователь, который обновляет запись
     * @return uuid логера
     */
    String removeReportPeriod(Integer id, TAUserInfo userInfo);

    /**
     * Редактирует период или выдает причину невозможности редактрования
     *
     * @param departmentReportPeriod обновленный отчетный период подразделения
     * @param user                   пользователь, который обновляет запись
     * @return uuid логера
     */
    String editPeriod(DepartmentReportPeriod departmentReportPeriod, TAUserInfo user);

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
    void setDeadline(DepartmentReportPeriodFilter filter) throws ActionException;

    enum Operation {
        FIND, // Поиск периода
        OPEN, // Открытие периода
        CLOSE, // Закрытие периода
        DELETE, // Удаление периода
        EDIT_DEADLINE, // Изменение срока сдачи отчетности в периоде
        EDIT // Редактирование периода
    }

    /**
     * Получение идентификаторов подразделений, для которых доступна операция {@link Operation} для пользователя {@link TAUser}
     *
     * @param user      пользователь, который совершает операцию
     * @param operation операция, совершаемая над отчетным периодом
     * @return список идентификаторов подразделений или пустой список
     */
    List<Integer> getAvailableDepartments(TAUser user, Operation operation);

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
     * Проверяет статус подразделения
     *
     * @param year                  год отчетного периода
     * @param departmentId          идентификатор подразделения
     * @param dictionaryTaxPeriodId идентификатор записи справочника "Коды, определяющие налоговый (отчётный) период"
     * @return объект {@link PeriodStatusBeforeOpen}
     * @throws ServiceException если найдено несколько {@link ReportPeriod} для указанного года
     */
    PeriodStatusBeforeOpen checkPeriodStatus(int year, int departmentId, long dictionaryTaxPeriodId);

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
     * Открыть корректирующий период
     *
     * @param reportPeriod отчетный период
     * @param departmentId идентификатор подразделения
     * @param term         срок сдачи отчетности
     * @param user         пользователь, который выполняет действие
     * @param logs         логер, при необходимости
     */
    @Deprecated
    void openCorrectionPeriod(ReportPeriod reportPeriod, int departmentId, Date term, TAUserInfo user, List<LogEntry> logs);

    /**
     * Открыть Корректирующий период
     *
     * @param period - отчетный период для подразделения
     * @return logs логер, при необходимости
     */
    String openCorrectionPeriod(DepartmentReportPeriod period);

    /**
     * проверяет статус периода перед открытием
     *
     * @param reportPeriod отчетный период
     * @param departmentId идентификатор подразделения
     * @param term         срок сдачи отчетности
     * @return статус периода
     */
    @Deprecated
    PeriodStatusBeforeOpen checkPeriodStatusBeforeOpen(ReportPeriod reportPeriod, int departmentId, Date term);

    /**
     * Редактировать отчетный период
     *
     * @param departmentReportPeriod
     * @param userInfo
     * @return uuid логгера
     */
    String edit(DepartmentReportPeriod departmentReportPeriod, TAUserInfo userInfo);

    /**
     * Редактировать отчетный период
     * Специальный метод для GWT
     *
     * @param reportPeriodId     идентификатор отчетного период
     * @param newDictTaxPeriodId новый отчетный период
     * @param newYear            новый год :)
     * @param user               пользователь, который выполняет действие
     * @param departmentId       идентификатор подразделения
     * @param logs               логер, при необходимости
     */
    @Deprecated
    void edit(int reportPeriodId, int oldDepartmentId, long newDictTaxPeriodId, int newYear, TAUserInfo user,
              int departmentId, List<LogEntry> logs);

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

    /**
     * Возвращает все периоды по виду налога, которые либо пересекаются с указанным диапазоном дат, либо полностью находятся внутри него
     *
     * @param startDate Начало периода
     * @param endDate   Конец периода
     */
    @Deprecated
    List<ReportPeriod> getReportPeriodsByDate(Date startDate, Date endDate);
}
