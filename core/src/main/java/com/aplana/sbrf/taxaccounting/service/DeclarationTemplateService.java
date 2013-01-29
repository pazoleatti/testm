package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.service.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.service.exception.ServiceException;

import java.util.List;

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
	 * Задать шаблон Jrxml-файла
	 * Метод компилирует jrxml-файл и записиывает в БД, как сам jrxml-файл, так и его откомпилированную версию (jasper-файл).
	 * @param declarationTemplateId идентификатор шаблона декларации 
	 * @param jrxml jrxml в виде строки
	 * @throws AccessDeniedException если у пользователя нет прав на изменение шаблона декларации
	 * @throws ServiceException если при компиляции jrxml произошла ошибка
	 */
	void setJrxml(int declarationTemplateId, String jrxml);
	/**
	 * Получить шаблон Jrxml-файла
	 * @param declarationTemplateId
	 * @return jrxml-файл в виде строки
	 */
	String getJrxml(int declarationTemplateId);
	
	/**
	 * Получить jasper-файл
	 * @param declarationTemplateId
	 * @return jasper-файл в виде байтового массива
	 */
	byte[] getJasper(int declarationTemplateId);	
}
