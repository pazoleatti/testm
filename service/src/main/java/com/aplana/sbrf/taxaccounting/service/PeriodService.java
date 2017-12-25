package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
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
     * Последний отчетный период для указанного вида налога и подразделения
     * @param taxType Тип налога
     * @param departmentId Подразделение
     * @return
     */
    DepartmentReportPeriod getLastReportPeriod(TaxType taxType, int departmentId);

	/**
	 * Открываем отчетный период для департамента.
	 * Логика описана в аналитике - Ведение периодов
	 * @return uuid логов
	 */
	String open(DepartmentReportPeriod period);

    /**
     * Создание нового отчетного периода подразделения или открытие существующего по комбинации параметров
     * - Подразделение
     * - Отчетный период
     * - Дата корректировки
     */
    void saveOrOpen(DepartmentReportPeriod departmentReportPeriod, List<LogEntry> logs);

	/**
	 * Создание или открытие периодов для нескольких подразделений
	 * - Подразделение
	 * - Отчетный период
	 * - Дата корректировки
	 */
	void saveOrOpen(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds, List<LogEntry> logs, boolean fullLogging);

	/**
	 * Закрыть период
	 * @param departmentReportPeriodId - идентификатор периода для подразделения "Банк"
	 * @return uuid логгера
	 */
	String close(Integer departmentReportPeriodId);

	/**
	 * Получает список отчетных периодов по отчетному периоду.
	 * 
	 * @param taxPeriodId
	 * @return
	 */
	List<ReportPeriod> listByTaxPeriod(int taxPeriodId);
	
	
	/**
	 * Получает налоговый период по ID
	 * 
	 * @param taxPeriodId
	 * @return
	 */
	TaxPeriod getTaxPeriod(int taxPeriodId);

	/**
	 * Получает отчетный период по ID
	 * 
	 * @param reportPeriodId
	 * @return
	 */
	ReportPeriod getReportPeriod(int reportPeriodId);

    /**
     * Возвращает дату начала отчетного периода
	 *
	 * <p>Информация о периодах в конфлюенсе
	 * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=9600466">Как считать отчетные периоды для разных налогов</a><p/>
     * @param reportPeriodId код отчетного периода
     * @return
     */
    Calendar getStartDate(int reportPeriodId);

    /**
     * Возвращает дату конца отчетного периода
     *
     * <p>Информация о периодах в конфлюенсе
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=9600466">Как считать отчетные периоды для разных налогов</a><p/>
     *
     * @param reportPeriodId код отчетного периода
     * @return
     */
    Calendar getEndDate(int reportPeriodId);

    /**
     * Возвращает "отчетную дату" если требуется в чтз
     * Отчетная дата = дата конца периода + 1 день
     * @param reportPeriodId
     * @return
     */
	// TODO: возможно имеется в виду дата сдачи отчетности. Надо проверить (Marat Fayzullin 22.01.2014)
    Calendar getReportDate(int reportPeriodId);

    /**
     * Получает все отчетные периоды в отсортированном порядке.
     * 
     * @param taxType
     * @return
     */
    List<ReportPeriod> getAllPeriodsByTaxType(TaxType taxType, boolean backOrder);

    /**
     * Получить дату начала месяца.
     *
     * @param reportPeriodId идентификатор отчетного период
     * @param periodOrder очередность месяца в периоде (значение из formData.periodOrder)
     * @return
     */
    Calendar getMonthStartDate(int reportPeriodId, int periodOrder);

    /**
     * Получить дату окончания месяца.
     *
     * @param reportPeriodId идентификатор отчетного период
     * @param periodOrder очередность месяца в периоде (значение из formData.periodOrder)
     * @return
     */
    Calendar getMonthEndDate(int reportPeriodId, int periodOrder);

    /**
     * Получить отчетную дату месяца.
     *
     * @param reportPeriodId идентификатор отчетного период
     * @param periodOrder очередность месяца в периоде (значение из formData.periodOrder)
     * @return
     */
    Calendar getMonthReportDate(int reportPeriodId, int periodOrder);

	/**
	 * Удалить отчетный период
	 * @param taxType тип налога
	 * @param drpId идентификатор периода
	 * @param logger логер, при необходимости
	 * @param user пользователь, который выполняет действие
	 */
	void removeReportPeriod(TaxType taxType, Integer drpId, Logger logger, TAUserInfo user);

	/**
	 * Удалить отчетный период
	 * @param id - идентификатор периода
     * @param userInfo
	 * @return uuid логера
	 */
	String removeReportPeriod(Integer[] id, TAUserInfo userInfo);


	String removeReportPeriod(Integer ids, TAUserInfo userInfo);

	/**
	 * Редактирует период или выдает причину невозможности редактрования
	 * @param departmentReportPeriod
	 * @param user
	 * @return uuid логера
	 */
    String editPeriod(DepartmentReportPeriod departmentReportPeriod, TAUserInfo user);

	/**
	 * Получение типа отчетногопериода из спрвочника по идентификатору
	 * @param id - идентификатор
	 * @return тип отчетного периода
	 */
	ReportPeriodType getPeriodTypeById(Long id);


	/**
	 * Устанавливает дату сдачи отчетности для периода подразделения
	 * @param filter - данные о периоде
	 *
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
	 * Условие из http://conf.aplana.com/pages/viewpage.action?pageId=9570811#id-Ведениепериодов-Требованиякправамдоступа
	 * @param taxType
	 * @param user
	 * @param operation
	 * @param departmentId
     * @return список ид подразделений
     */
	List<Integer> getAvailableDepartments(TaxType taxType, TAUser user, Operation operation, Integer departmentId);

    /**
     * Список отчетных периодов для указанного вида налога и для указанных подразделений
     *
     * @param taxType Вид налога
     * @param departmentList Список подразделений
     * @return Список отчетных периодов
     */
    List<ReportPeriod> getPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList);

    /**
     * Получить список подразделений для закрытия периода
     * @param taxType Тип налога
     * @param user пользователь выполняющий операцию
     * @param departmentId идентификатор подразделения
     * @return список идентификаторов подразделений
     */
    List<Integer> getAvailableDepartmentsForClose(TaxType taxType, TAUser user, int departmentId);

	/**
	 * Проверяет существование периода для подразделения
	 * @param departmentId подразделение, для которого осуществляется проверка существования периода
	 * @return true - существует, false - не существует
	 */
	boolean existForDepartment(int departmentId, int reportPeriodId);

	/**
	 * Проверяет статус периода ОТКРЫТ, ЗАКРЫТ ИЛИ НЕСУЩЕСТВУЕТ
	 * @param taxType
	 * @param year
	 * @param departmentId
	 * @param dictionaryTaxPeriodId
	 * @return
	 */
	PeriodStatusBeforeOpen checkPeriodStatusBeforeOpen(TaxType taxType, int year, int departmentId, long dictionaryTaxPeriodId);

    /**
     * Получает список месяцев, в зависимости от выбранного периода
     *
     * @param reportPeriodId идентификатор отчетного период
     * @return
     */
    List<Months> getAvailableMonthList(int reportPeriodId);

	/**
	 * http://conf.aplana.com/pages/viewpage.action?pageId=11382680
	 * @param user пользователь
	 * @param taxType тип периода
	 * @return множество отчетных периодов
	 */
	Set<ReportPeriod> getOpenForUser(TAUser user, TaxType taxType);

    /**
     * Список открытых периодов
     * @param taxType тип налога
     * @param departmentList подразделения
     * @param withoutCorrect true - без корректирующих периодов false - с корректирующими периодами
     * @return список отчетных периодов
     */
    List<ReportPeriod> getOpenPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList,
                                                             boolean withoutCorrect);

    /**
     * Возвращает предыдущий отчетный период, не привязываясь к налоговому периоду,
     * т.е. если запрашивают предыдущий отчетный период первого отчетного периода в налоговом,
     * то функция возвращает последний отчетный период предыдущего налогового периода,
     * если такой период не найден то null
     *
     * @param reportPeriodId
     * @return предыдущий отчетный период
     */
    ReportPeriod getPrevReportPeriod(int reportPeriodId);

    /**
     * Получить корректирующие периоды
     * @param taxType тип налога
     * @param departmentId идентификатор подразделения
     * @param pagingParams
     * @return список корректирующих периодов
     */
    PagingResult<ReportPeriod> getCorrectPeriods(TaxType taxType, int departmentId, PagingParams pagingParams);

    /**
     * Получить периоды сравнени - выборка 50
     * http://conf.aplana.com/pages/viewpage.action?pageId=20386707
     * @param taxType тип налога
     * @param departmentId идентификатор подразделения
     * @return список корректирующих периодов
     */
    List<ReportPeriod> getComparativPeriods(TaxType taxType, int departmentId);

    /**
     * Открыть корректирующий период
     * @param taxType тип налога
     * @param reportPeriod отчетный период
     * @param departmentId идентификатор подразделения
     * @param term срок сдачи отчетности
     * @param user пользователь, который выполняет действие
     * @param logs логер, при необходимости
     */
    void openCorrectionPeriod(TaxType taxType, ReportPeriod reportPeriod, int departmentId, Date term, TAUserInfo user, List<LogEntry> logs);

	/**
	 * Открыть Корректирующий период
	 * @param period - отчетный период для подразделения
	 * @return logs логер, при необходимости
	 */
	String openCorrectionPeriod(DepartmentReportPeriod period);

    /**
     * проверяет статус периода перед открытием
     * @param reportPeriod отчетный период
     * @param departmentId идентификатор подразделения
     * @param term срок сдачи отчетности
     * @return статус периода
     */
    PeriodStatusBeforeOpen checkPeriodStatusBeforeOpen(ReportPeriod reportPeriod, int departmentId, Date term);

    /**
     * Редактировать отчетный период
	 * @param departmentReportPeriod
	 * @param userInfo
	 * @return uuid логгера
	 */
    String edit(DepartmentReportPeriod departmentReportPeriod, TAUserInfo userInfo);

    /**
     * Редактировать корректирующий период
	 * @param logs логер, при необходимости
	 * @param newPeriod
	 * @param oldPeriod
	 */
    void editCorrectionPeriod(List<LogEntry> logs, DepartmentReportPeriod newPeriod, DepartmentReportPeriod oldPeriod);

	/**
	 * Редактировать отчетный период
	 * Специальный метод для GWT
	 * @param reportPeriodId идентификатор отчетного период
	 * @param newDictTaxPeriodId новый отчетный период
	 * @param newYear новый год :)
	 * @param taxType тип налога
	 * @param user пользователь, который выполняет действие
	 * @param departmentId идентификатор подразделения
	 * @param logs логер, при необходимости
	 */
	void edit(int reportPeriodId, int oldDepartmentId, long newDictTaxPeriodId, int newYear, TaxType taxType, TAUserInfo user,
			  int departmentId, List<LogEntry> logs);

	/**
	 * Редактировать корректирующий период
	 * Специальный метод для GWT
	 * @param reportPeriodId идентификатор отчетного период
	 * @param newReportPeriodId новый идентификатор отчетного период
	 * @param oldDepartmentId старый идентификатор подразделения
	 * @param newDepartmentId новый идентификатор подразделения
	 * @param taxType тип налога
	 * @param correctionDate дата корректировки
	 * @param newCorrectionDate новая дата корректировки
	 * @param user пользователь, который выполняет действие
	 * @param logs логер, при необходимости
	 */
	void editCorrectionPeriod(int reportPeriodId, int newReportPeriodId, int oldDepartmentId, int newDepartmentId, TaxType taxType,
							  Date correctionDate, Date newCorrectionDate, TAUserInfo user, List<LogEntry> logs);

    List<DepartmentReportPeriod> getDRPByDepartmentIds(List<TaxType> taxTypes, List<Integer> departmentIds);

    /**
     * Отчетный период по коду и году
     */
    ReportPeriod getByTaxTypedCodeYear(TaxType taxType, String code, int year);

	/**
	 * @return список отчетных периодов по дате актуальности
     * @param pagingParams
     */
	PagingResult<ReportPeriodType> getPeriodType(PagingParams pagingParams);

	/**
     * Возвращает все периоды по виду налога, которые либо пересекаются с указанным диапазоном дат, либо полностью находятся внутри него
     * @param taxType Вид налога
     * @param startDate Начало периода
     * @param endDate Конец периода
     */
    List<ReportPeriod> getReportPeriodsByDate(TaxType taxType, Date startDate, Date endDate);

    boolean isFirstPeriod(int reportPeriodId);
}
