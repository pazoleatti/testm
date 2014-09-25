package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
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
}
