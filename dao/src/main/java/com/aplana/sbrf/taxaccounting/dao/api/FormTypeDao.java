package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;

/**
 * Интерфейс DAO для работы с видами налоговых форм
 * @author dsultanbekov
 */
public interface FormTypeDao {
	/**
	 * Получить вид налоговой формы по идентификатору
	 * @param typeId идентификатор вида
	 * @return Объект, представляющий вид налоговой формы
	 * @throws DaoException если в БД нет записи с соответствующим ключом
	 */
	FormType get(int typeId);
	
	/**
	 * Получить полный список видов налоговых форм
     * Список только активных версий с полем status = 0
	 * @return список видов налоговых форм
	 */
    List<Integer> getAll();

    /**
     * Получить все существующие виды налоговых форм по виду налога
     * @param taxType вид налога
     * @return список всех существующих видов налоговых форм по виду налога
     */
	List<FormType> getByTaxType(TaxType taxType);

    List<Integer> getByFilter(TemplateFilter filter);

    /**
     * Сохранение нового ааблона
     * @param formType шаблон
     * @return идентификатор созданного шаблона
     */
    int save(FormType formType);

    /**
     * Обновить имя типа налоговой формы
     * @param formTypeId идентификатор типа налоговой формы
     * @param newName новое имя типа налоговой формы
     */
    void updateFormTypeName(int formTypeId, String newName);

    void delete(int formTypeId);

    /**
     * Получает макеты, у которых есть активные версии в данном отчетном периоде
     * @param departmentId идентификатор департамент
     * @param reportPeriod отчетный период
     * @param taxType Типы налогов
     * @param kind  тип НФ
     * @return активные макеты в данном периоде
     */
	List<FormType> getFormTypes(int departmentId, ReportPeriod reportPeriod, TaxType taxType, List<FormDataKind> kind);
}
