package com.aplana.sbrf.taxaccounting.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс, представляющий собой набор данных по налоговым формам, позволяющий осуществлять быстрый поиск по коллекции
 * по разным параметрам, а также проверять наличие/осутствие некоторых объектов
 * Предназначен для выполнения работ при формировании консолидированных/сводных налоговых форм и деклараций, когда необходимо
 * обработать набор налоговых форм-источников и сформировать на их основе новый объект
 * @author dsultanbekov
 */
public class FormDataCollection {
	private List<FormData> records;

	/**
	 * Возвращает полный набор {@link FormData налоговых форм}, содержащихся в коллекции
	 */
	public List<FormData> getRecords() {
		return records;
	}

	/**
	 * Задать полный набор {@link FormData налоговых форм}, содержащихся в коллекции
	 */
	public void setRecords(List<FormData> records) {
		this.records = records;
	}
	
	/**
	 * Позволяет найти налоговую форму по сочетанию вида, типа и подразделения
	 * 
	 * TODO: в будущем возможны ситуации, когда сочетание этих трёх параметров будет неуникальным,
	 * тогда нужно будет просто добавить метод findAll, возвращающий коллекцию данных
	 * 
	 * @param departmentId идентификатор подразделения
	 * @param formTypeId идентификатор {@link FormType вида налоговой формы}
	 * @param kind тип налоговой формы
	 * @return {@link FormData налоговая форма}, соответствующая условиям, или null, если такой записи не найдено
	 */
	public FormData find(int departmentId, int formTypeId, FormDataKind kind) {
		for (FormData formData: records) {
			if (formData.getDepartmentId() == departmentId && formData.getFormType().getId() == formTypeId && formData.getKind() == kind) {
				return formData;
			}
		}
		return null;
	}
	
	/**
	 * Возвращает все налоговые формы по заданному виду и типу
	 * @param formTypeId идентификатор {@link FormType вида налоговой формы}
	 * @param kind {@link FormDataKind тип налоговой формы}
	 * @return перечень налоговых форм, удовлетворяющих условиям поиска
	 */
	public List<FormData> findAllByFormTypeAndKind(int formTypeId, FormDataKind kind) {
		List<FormData> result = new ArrayList<FormData>();
		for (FormData formData: records) {
			if (formData.getFormType().getId() == formTypeId && formData.getKind() == kind) {
				result.add(formData);
			}
		}
		return result;
	}
}
