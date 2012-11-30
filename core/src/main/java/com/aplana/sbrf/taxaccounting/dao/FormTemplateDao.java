package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;

/**
 * DAO-Интерфейс для работы с макетами налоговых форм
 * @author dsultanbekov
 */
public interface FormTemplateDao {
	/**
	 * Получить полный список всех описаний налоговых форм
	 * (Внимание, объекты в результирующей коллекции могут быть только частично инициализированы,
	 * в них может остаться незаполненной информация по столбцам, скрипта и т.д.) 
	 * @return
	 */
	List<FormTemplate> listAll();
	/**
	 * Получить макет налоговой формы
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
	 * @throws DaoException если не удалось найти активное описание налоговой формы по заданному типу, 
	 * 	или если обнаружено несколько действуюшие описаний по данному виду формы 
	 */
	int getActiveFormTemplateId(int formTypeId);
}
