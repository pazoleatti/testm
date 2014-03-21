package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.VersionSegment;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;

import java.util.Date;
import java.util.List;

/**
 * Dao для работы с {@link DeclarationTemplate шаблонами деклараций}
 * @author dsultanbekov
 */
public interface DeclarationTemplateDao {

	/**
	 * Получить полный список всех деклараций
	 * @return список всех DeclarationTemplate
	 */
	List<DeclarationTemplate> listAll();
	/**
	 * Получить шаблон декларации (без тела скрипта).
     * Скрипт получается с помощью метода {@link #getDeclarationTemplateScript(int)}
	 * @param declarationTemplateId идентификатор шаблона декларации
	 * @return объект шаблона декларации
	 * @throws DaoException если декларации с таким id не существует
	 */
	DeclarationTemplate get(int declarationTemplateId);
	/**
	 * Возвращает идентификатор действующего {@link com.aplana.sbrf.taxaccounting.model.DeclarationTemplate описания декларации} по виду декларации
	 * Такое описание для каждого вида декларации в любой момент времени может быть только одно
	 * @param declarationTypeId идентификатор вида декларации
	 * @return идентификатор описания декларации
	 * @throws DaoException если не удалось найти активное описание декларации по заданному типу,
	 * 	или если обнаружено несколько действуюшие описаний по данному виду декларации
	 */
	int getActiveDeclarationTemplateId(int declarationTypeId, int reportPeriodId);
	/**
	 * Сохранить шаблон декларации.
	 * Если сохраняется новый объект, то у него должен быть пустой id (id == null), в этом случае он будет сгенерирован
	 * @param declarationTemplate объект шаблона декларации
	 * @return идентификатор сохранённой записи в БД
	 */
	int save(DeclarationTemplate declarationTemplate);
	/**
	 * Задать Jrxml-файла
	 * @param declarationTemplateId идентификатор шаблона декларации
	 * @param jrxmlBlobId идентификатор бинарного представления шаблона
	 * @throws DaoException если не существует шаблона декларации с таким id
	 */
	void setJrxml(int declarationTemplateId, String jrxmlBlobId);

    /**
     * Получение тела скрипта.
     * @param declarationTemplateId идентификатор макета
     * @return тело скрипта
     */
    String getDeclarationTemplateScript(int declarationTemplateId);

    /**
    * Получить список идентификаторов макетов деклараций по фильтру
    * @param filter фильтр
    * @return список отфильтрованых идентификаторов
    */
    List<Integer> getByFilter(TemplateFilter filter);

    /**
     * получить все идентификаторы шаблонов деклараций
     * @return список всех идентификаторов
     */
    List<Integer> listAllId();

    /**
     * Получает список id версий макета по типу шаблона и статусу версии.
     * @param decTypeId вид шаблона
     * @param decTemplateId идентификатор шаблона, котрый исключить из поиска, если нет такого то 0
     * @param statusList статус формы
     * @return список версий
     */
    List<Integer> getDeclarationTemplateVersions(int decTypeId, int decTemplateId, List<Integer> statusList, Date actualStartVersion, Date actualEndVersion);

    /**
     * Метод для поиска пересечений версий макетов в указанных датах
     * @param formTypeId вид шаблона
     * @param formTemplateId дентификатор шаблона, который исключить из поиска, если нет такого то 0
     * @param statusList статус формы
     * @param actualStartVersion дата начала
     * @param actualEndVersion дата окончания
     * @return список пеересечений
     */
    List<VersionSegment> findFTVersionIntersections(int formTypeId, int formTemplateId, Date actualStartVersion, Date actualEndVersion);

    /**
     * Поиск даты окончания версии макета, которая находится следующей по дате(т.е. "справа") от данной версии
     * @param templateId идентификатор версии макета
     * @param typeId идентификатор вида налога
     * @param actualBeginVersion дата актуализации версии, для которой ведем поиск
     * @return дата окончания
     */
    Date getDTVersionEndDate(int templateId, int typeId, Date actualBeginVersion);

    /**
     * Поиск версии макета, которая находится следующей по дате(т.е. "справа") от данной версии
     * @param typeId идентификатор вида налога
     * @param statusList список статусов макатеов, которые искать
     * @param actualBeginVersion дата актуализации версии, для которой ведем поиск
     * @return идентификатор "правой" версии макета
     */
    int getNearestDTVersionIdRight(int typeId, List<Integer> statusList, Date actualBeginVersion);

    /**
     * Поиск версии макета, которая предшествует по дате(т.е. "слева") данной версии
     * @param typeId идентификатор вида налога
     * @param statusList список статусов макатеов, которые искать
     * @param actualBeginVersion дата актуализации версии, для которой ведем поиск
     * @return идентификатор версии макета "слева"
     */
    int getNearestDTVersionIdLeft(int typeId, List<Integer> statusList, Date actualBeginVersion);

    /**
     * Удаляет версию шаблона.
     * По идее удалять полностью только фейковые версии шаблонов.
     * @param declarationTemplateId идентификатор макета
     * @return удаленный идентификатор макета
     */
    int delete(int declarationTemplateId);

    /**
     * Количество весий для вида шаблона
     * @param decTypeId вид шаблона
     * @param statusList статусы
     * @return количество
     */
    int versionTemplateCount(int decTypeId, List<Integer> statusList);
}
