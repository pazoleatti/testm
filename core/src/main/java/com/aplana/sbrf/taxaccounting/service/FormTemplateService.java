package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

/**
 * Сервис для работы с шаблонами налоговых форм. В первую очередь предназначен для использования в админке
 * @author dsultanbekov
 */
public interface FormTemplateService {
	/**
	 * Получить полный список всех описаний налоговых форм
	 * (Внимание, объекты в результирующей коллекции могут быть только частично инициализированы,
	 * в них может остаться незаполненной информация по столбцам, скрипта и т.д.) 
	 * @return список всех FormTemplate
	 */
	List<FormTemplate> listAll();
	/**
	 * Получить макет налоговой формы
	 * @param formTemplateId идентификатор макета
	 * @return объект, представляющий описание налоговой формы
	 */
	FormTemplate get(int formTemplateId);
	/**
	 * Валидировать модель данных для описания налоговой формы
	 * @param formTemplate объект, содержащий описание налоговой формы
	 * @param logger объект, для ведения логов
	 */
	void validateFormTemplate(FormTemplate formTemplate, Logger logger);
	/**
	 * Сохранить описание налоговой формы
	 * @param formTemplate объект, содержащий описание налоговой формы
	 * @return идентификатор сохранённой записи
	 */
	int save(FormTemplate formTemplate);
	
	/**
	 * Возвращает идентификатор действующего {@link FormTemplate описания налоговой формы} по виду налоговой формы
	 * Такое описание для каждого вида формы в любой момент времени может быть только одно
	 * @param formTypeId идентификатор вида налоговой формы
	 * @return идентификатор описания налоговой формы
	 * @throws DaoException если не удалось найти активное описание налоговой формы по заданному типу, 
	 * 	или если обнаружено несколько действуюшие описаний по данному виду формы 
	 */
	int getActiveFormTemplateId(int formTypeId);

	/**
	 * Снять блокировку с formTemplate.
	 * @param formTemplateId - идентификатор шаблона налоговой формы
	 * @param userInfo - информация о пользователе
	 * @return true - если удалось разблокировать налоговую форму, иначе - false
	 * */
	boolean unlock(int formTemplateId,  TAUserInfo userInfo);

	/**
	 * Блокировка formTemplate.
	 * @param formTemplateId - идентификатор налоговой формы
	 * @param userInfo - информация о пользователе
	 * @return информацию о блокировке объекта
	 */
	boolean lock(int formTemplateId,  TAUserInfo userInfo);

	/**
	 * Проверяет, не заблокирован ли шаблон формы другим пользователем
	 * @param formTemplateId
	 * @param userInfo
	 */
	void checkLockedByAnotherUser(Integer formTemplateId, TAUserInfo userInfo);

    /**
     * Исполяет для теста написанный скрипт от имени пользователя controlUnp
     * @param formTemplate
     */
    void executeTestScript(FormTemplate formTemplate);
}
