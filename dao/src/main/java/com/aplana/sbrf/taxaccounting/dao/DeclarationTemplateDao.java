package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;

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
	 * Получить шаблон декларации
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
	int getActiveDeclarationTemplateId(int declarationTypeId);
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
}
