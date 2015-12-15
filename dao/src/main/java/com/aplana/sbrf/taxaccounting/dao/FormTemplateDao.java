package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.aplana.sbrf.taxaccounting.model.VersionSegment;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DAO-Интерфейс для работы с макетами налоговых форм
 * @author dsultanbekov
 */
public interface FormTemplateDao {
	/**
	 * Получить полный список всех действующих версий шаблонов налоговых форм
	 * (Внимание, объекты в результирующей коллекции могут быть только частично инициализированы,
	 * в них может остаться незаполненной информация по столбцам, скрипта и т.д.) 
	 * @return список шаблонов
	 */
	List<FormTemplate> listAll();
	/**
	 * Получить макет налоговой формы.
	 * @param formTemplateId идентификатор макета
	 * @return объект, представляющий описание налоговой формы
	 */
	FormTemplate get(int formTemplateId);
	/**
	 * Сохранить описание налоговой формы
	 * @param formTemplate объект, содержащий описание налоговой формы
	 * @return идентификатор сохранённой записи
	 */
	int save(FormTemplate formTemplate);

    /**
     * Обновление данных версий макетов
     * Если сохраняется новый объект, то у него должен быть пустой id (id == null), в этом случае он будет сгенерирован
     * @param formTemplates объект шаблона декларации
     * @return массив успешных апдейтов обновленных версий (0 - неуспешный, 1 - успешный)
     */
    int[] update(List<FormTemplate> formTemplates);
	
	/**
	 * Возвращает идентификатор действующего {@link FormTemplate описания налоговой формы} по виду налоговой формы
	 * Такое описание для каждого вида формы в любой момент времени может быть только одно
	 * @param formTypeId идентификатор вида налоговой формы
	 * @return идентификатор описания налоговой формы
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если не удалось найти активное описание налоговой формы по заданному типу,
	 * 	или если обнаружено несколько действуюших описаний по данному виду формы
	 */
	int getActiveFormTemplateId(int formTypeId, int reportPeriodId);

    /**
     * Возвращает идентификатор {@link FormTemplate описания налоговой формы} по виду налоговой формы
     * Такое описание для каждого вида формы в любой момент времени может быть только одно
     * @param formTypeId идентификатор вида налоговой формы
     * @param startDate дата начала периода
     * @param endDate дата окончания периода
     * @return идентификатор описания налоговой формы
     */
    int getFormTemplateIdByFTAndReportPeriod(int formTypeId, Date startDate, Date endDate);

    /**
     * Получить список идентификаторов макетов налоговых форм по фильтру
     * @param filter фильтр
     * @return список отфильтрованых идентификаторов
     */
    List<Integer> getByFilter(TemplateFilter filter);

    /**
     * получить все идентификаторы шаблонов налоговых форм
     * @return список всех идентификаторов
     */
    List<Integer> listAllId();

    /**
     * Получает список id версий макета по типу шаблона и статусу версии.
     * @param formTypeId вид шаблона
     * @param statusList статус формы
     * @return список версий
     */
    List<Integer> getFormTemplateVersions(int formTypeId, List<Integer> statusList);

    /**
     * Метод для поиска пересечений версий макетов в указанных датах
     * @param formTypeId вид шаблона
     * @param formTemplateId дентификатор шаблона, который исключить из поиска, если нет такого то 0
     * @param actualStartVersion дата начала
     * @param actualEndVersion дата окончания
     * @return список пеересечений
     */
    List<VersionSegment> findFTVersionIntersections(int formTypeId, int formTemplateId, Date actualStartVersion, Date actualEndVersion);

    /**
     * Поиск даты окончания версии макета, которая находится следующей по дате(т.е. "справа") от данной версии
     * @param formTypeId идентификатор вида налога
     * @param actualBeginVersion дата актуализации версии, для которой ведем поиск
     * @return дату окончания версии
     */
    Date getFTVersionEndDate(int formTypeId, Date actualBeginVersion);

    /**
     * Поиск версии макета, которая находится следующей по дате(т.е. "справа") от данной версии
     * @param formTypeId идентификатор вида налога
     * @param statusList список статусов макатеов, которые искать
     * @param actualBeginVersion дата актуализации версии, для которой ведем поиск
     * @return идентификатор "правой" версии макета
     */
    int getNearestFTVersionIdRight(int formTypeId, List<Integer> statusList, Date actualBeginVersion);

    /**
     * Удаляет версию шаблона.
     * @param formTemplateId идентификатор макета
     * @return удаленный идентификатор макета
     */
    int delete(int formTemplateId);

    /**
     * Удаляет версии шаблонов.
     * @param formTemplateIds идентификатор макета
     */
    void delete(Collection<Integer> formTemplateIds);

    /**
     * Сохраняем новый шаблон
     * @param formTemplate шаблон
     * @return идентификатор нового шаблона
     */
    int saveNew(FormTemplate formTemplate);

    /**
     * Количество весий для вида шаблона
     * @param formTypeId вид шаблона
     * @param statusList статусы
     * @return количество
     */
    int versionTemplateCount(int formTypeId, List<Integer> statusList);

    /**
     * Количество активных весий для вида шаблона
     * @param formTypeId вид шаблона
     * @return количество
     */
    List<Map<String,Object>> versionTemplateCountByType(Collection<Integer> formTypeId);

    int updateVersionStatus(VersionedObjectStatus versionStatus, int formTemplateId);

    /**
     * Проверка существования активного шаблона нф
     * с типом formTypeId и датой актуальности которой является период включающий
     * reportPeriodId
     *
     * @param formTypeId вид шаблона
     * @param reportPeriodId отчетный период
     * @param excludeInactiveTemplate исключить нф-источники с макетом выведенным из действия?
     * @return
     */
    boolean existFormTemplate(int formTypeId, int reportPeriodId, boolean excludeInactiveTemplate);

	/**
	 * Проверяет, есть ли в списке строковых значений, строка с длиной больше maxLength
	 * @param formTemplateId по данному макету осуществляется поиск
	 * @param columnId среди данных этого столбца
	 * @param maxLength с данным значением сравниваем длину строк
	 * @return true - есть длинные строки, false - нет
	 */
	boolean checkExistLargeString(Integer formTemplateId, Integer columnId, int maxLength);

    /**
     * Метод для создания таблиц НФ в новой структуре.
     * НЕ ИСПОЛЬЗОВАТЬ нигде больше.
     * http://jira.aplana.com/browse/SBRFACCTAX-11384
     * @param ftId идентификатор макета НФ
     */
    void createFDTable(int ftId);

    /**
     * Метод для удаления таблиц НФ в новой структуре.
     * НЕ ИСПОЛЬЗОВАТЬ нигде больше.
     * @param ftId идентификатор версии макета НФ
     */
    void dropFDTable(int ftId);

    /**
     * Метод для удаления таблиц НФ в новой структуре.
     * НЕ ИСПОЛЬЗОВАТЬ нигде больше.
     * @param ftId идентификатор id версий макета {@link com.aplana.sbrf.taxaccounting.model.FormType}
     */
    void dropFTTable(List<Integer> ftId);

    boolean isFDTableExist(int ftId);

    /**
     * Получает макет нф по типу и году (версии)
     * @param formTypeId
     * @param year
     * @return идентификатор макета, либо null, если он не найден
     */
    Integer get(int formTypeId, int year);

    /**
     * Обновляет скрипт макета
     * @param formTemplateId идентификатор макета
     * @param script скрипт
     */
    void updateScript(int formTemplateId, String script);
}
