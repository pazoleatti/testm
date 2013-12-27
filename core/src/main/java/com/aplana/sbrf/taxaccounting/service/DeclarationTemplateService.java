package com.aplana.sbrf.taxaccounting.service;

import java.io.InputStream;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;

/**
 * Сервис для работы с шаблонами деклараций 
 * @author dsultanbekov
  */
public interface DeclarationTemplateService {
	/**
	 * Получить полный список всех деклараций
	 * @return список всех DeclarationTemplate
	 */
	List<DeclarationTemplate> listAll();
	/**
	 * Получить шаблон декларации
	 * @param declarationTemplateId идентификатор шаблона декларации
	 * @return объект шаблона декларации
	 */
	DeclarationTemplate get(int declarationTemplateId);
	/**
	 * Сохранить шаблон декларации.
	 * Если сохраняется новый объект, то у него должен быть пустой id (id == null), в этом случае он будет сгенерирован
	 * @param declarationTemplate объект шаблона декларации
	 * @return идентификатор сохранённой записи в БД
	 * @throws AccessDeniedException если у пользователя нет прав на изменение шаблона декларации 
	 */
	int save(DeclarationTemplate declarationTemplate);
	/**
	 * Возвращает идентификатор действующего {@link DeclarationTemplate описания декларации} по виду декларации
	 * Такое описание для каждого вида декларации в любой момент времени может быть только одно
	 * @param declarationTypeId идентификатор вида декларации
	 * @return идентификатор описания декларации
	 * @throws DaoException если не удалось найти активное описание декларации по заданному типу,
	 * 	или если обнаружено несколько действуюшие описаний по данному виду декларации
	 */
	int getActiveDeclarationTemplateId(int declarationTypeId);
	/**
	 * Задать шаблон Jrxml-файла
	 * Метод компилирует jrxml-файл и записиывает в БД, как сам jrxml-файл, так и его откомпилированную версию (jasper-файл).
	 * @param declarationTemplateId идентификатор шаблона декларации 
	 * @param jrxml jrxml в виде строки
	 * @throws AccessDeniedException если у пользователя нет прав на изменение шаблона декларации
	 * @throws ServiceException если при компиляции jrxml произошла ошибка
	 */
	void setJrxml(int declarationTemplateId, InputStream jrxml);
	/**
	 * Получить шаблон Jrxml-файла
	 * @param declarationTemplateId идентификатор вида декларации
	 * @return jrxml-файл в виде строки
	 */
	String getJrxml(int declarationTemplateId);
	
	/**
	 * Получить jasper-файл
	 * @param declarationTemplateId идентификатор вида декларации
	 * @return поток jasper-файла
	 */
    InputStream getJasper(int declarationTemplateId);

	/**
	 * Снять блокировку с declarationTemplate.
	 * @param declarationTemplateId - идентификатор шаблона налоговой формы
	 * @param userInfo - информация о пользователе
	 * @return true - если удалось разблокировать форму декларации, иначе - false
	 * */
	boolean unlock(int declarationTemplateId, TAUserInfo userInfo);

	/**
	 * Блокировка declarationTemplate.
	 * @param declarationTemplateId - идентификатор налоговой формы
	 * @param userInfo - информация о пользователе
	 * @return информацию о блокировке объекта
	 */
	boolean lock(int declarationTemplateId, TAUserInfo userInfo);

	/**
	 * Проверяет, не заблокирован ли шаблон декларации другим пользователем
	 * @param declarationTemplateId идентификатор вида декларации
	 * @param userInfo - информация о пользователе
	 */
	void checkLockedByAnotherUser(Integer declarationTemplateId, TAUserInfo userInfo);

    /**
     * Получение тела скрипта.
     * @param declarationTemplateId идентификатор вида декларации
     * @return тело скрипта
     */
    String getDeclarationTemplateScript(int declarationTemplateId);

    /**
     * Получить список шаблонов деклараций по фильтру
     * @param filter фильтр
     * @return отфильтрованый список шаблонов
     */
    List<DeclarationTemplate> getByFilter(TemplateFilter filter);

}
