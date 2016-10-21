package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Date;
import java.util.List;

/**
 * Интерфейс сервиса для работы с привязкой департаментов к подразделениям
 * @author auldanov
 */
@ScriptExposed
public interface DepartmentFormTypeService {
    /**
     * Возвращает информацию об источниках, которые должны использоваться при
     * формировании налоговой формы назначения с заданными параметрами
     */
    List<DepartmentFormType> getFormSources(int departmentId, int formTypeId, FormDataKind kind, Date periodStart,
                                            Date periodEnd);

    /**
     * Возвращает информацию о формах-потребителях, которые должны использовать
     * информацию из данной налоговой формы в качестве источника
     */
    List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind,
                                                 Date periodStart, Date periodEnd);

    /**
     * Возвращает информацию о декларациях-потребителях, которые должны использовать
     * информацию из данной налоговой формы в качестве источника
     */
    List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId, int sourceFormTypeId,
                                                               FormDataKind sourceKind, Date periodStart, Date periodEnd);

    /**
     * Возвращает информацию о формах-источниках, которые должны использоваться
     * при формировании декларации
     */
    List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId, Date periodStart, Date periodEnd);

    /**
     * Возвращает информацию он назначенных подразделению формах по заданному виду налога.
     *
     * @param departmentId идентификатор подразделения
     * @param taxType      вид налога
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return список назначенных подразделению форм (с учётом вида и типа) по заданному виду налога
     */
    List<DepartmentFormType> getByTaxType(int departmentId, TaxType taxType, Date periodStart, Date periodEnd);
}
