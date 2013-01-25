package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;

/**
 * Dao для работы с {@link DeclarationTemplate шаблонами деклараций}
 * @author dsultanbekov
 */
public interface DeclarationTemplateDao {
	/**
	 * Получить шаблон декларации
	 * @param declarationTemplateId идентификатор шаблона декларации
	 * @return объект шаблона декларации
	 * @throws DaoException если декларации с таким id не существует
	 */
	DeclarationTemplate get(int declarationTemplateId);
	/**
	 * Сохранить шаблон декларации.
	 * Если сохраняется новый объект, то у него должен быть пустой id (id == null), в этом случае он будет сгенерирован
	 * @param declarationTemplate объект шаблона декларации
	 * @return идентификатор сохранённой записи в БД
	 */
	int save(DeclarationTemplate declarationTemplate);
	/**
	 * Задать шаблон Jrxml-файла
	 * @param declarationTemplateId идентификатор шаблона декларации 
	 * @param jrxml jrxml в виде строки
	 * @throws DaoException если не существует шаблона декларации с таким id
	 */
	void setJrxml(int declarationTemplateId, String jrxml);
	/**
	 * Получить шаблон Jrxml-файла
	 * @param declarationTemplateId
	 * @return jrxml-файл в виде строки
	 * @throws DaoException если не существует шаблона декларации с таким id 
	 */
	String getJrxml(int declarationTemplateId);
}
