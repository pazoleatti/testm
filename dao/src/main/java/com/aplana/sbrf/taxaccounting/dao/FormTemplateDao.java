package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;

import java.util.List;

/**
 * DAO-Интерфейс для работы с макетами налоговых форм
 * @author dsultanbekov
 */
public interface FormTemplateDao {
	/**
	 * Получить полный список всех описаний налоговых форм
	 * (Внимание, объекты в результирующей коллекции могут быть только частично инициализированы,
	 * в них может остаться незаполненной информация по столбцам, скрипта и т.д.) 
	 * @return список шаблонов
	 */
	List<FormTemplate> listAll();
	/**
	 * Получить макет налоговой формы (без скрипта). Для получения тела скрипта {@link #getFormTemplateScript(int)}}
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
	 * Возвращает идентификатор действующего {@link FormTemplate описания налоговой формы} по виду налоговой формы
	 * Такое описание для каждого вида формы в любой момент времени может быть только одно
	 * @param formTypeId идентификатор вида налоговой формы
	 * @return идентификатор описания налоговой формы
	 * @throws com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException если не удалось найти активное описание налоговой формы по заданному типу,
	 * 	или если обнаружено несколько действуюшие описаний по данному виду формы 
	 */
	int getActiveFormTemplateId(int formTypeId);

    /**
     * Получает тело скрипта. Необходим для lazy-инициализации.
     * @return тело скрипта
     */
    String getFormTemplateScript(int formTemplateId);
}
