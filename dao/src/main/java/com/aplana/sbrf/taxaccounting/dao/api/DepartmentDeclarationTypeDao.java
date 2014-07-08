package com.aplana.sbrf.taxaccounting.dao.api;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Dao для работы с {@link DepartmentDeclarationType информацией о назначении деклараций подразделениям}  
 * @author dsultanbekov
 */
public interface DepartmentDeclarationTypeDao {

	/**
	 * Возвращает идентификаторы всех подразделений, в которых есть декларации по данному виду налога.
	 * @param taxType тип налога
	 * @return набор идентификаторов подразделений
	 */
	Set<Integer> getDepartmentIdsByTaxType(TaxType taxType);

    /**
     * Возвращает информацию о назначенных подразделению декларациях по заданному виду налога
     * @param departmentId идентификатор подразделения
     * @param taxType вид налога
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return список назначенных подразделению деклараций (с учётом вида и типа) по заданному виду налога
     */
    List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType, Date periodStart, Date periodEnd);

	/**
	 * Возвращает информацию о назначенных подразделению декларациях по заданному виду налога
	 * @param departmentId идентификатор подразделения
	 * @param taxType вид налога
	 * @return список назначенных подразделению деклараций (с учётом вида и типа) по заданному виду налога
	 */
    @Deprecated
	List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType);
	
    /**
     * Добавляет назначения декларации
     */
    void save(int departmentId, int declarationTypeId);
    
    /**
     * Удаляет назначение декларации
     */
    void delete(Long id);
}
