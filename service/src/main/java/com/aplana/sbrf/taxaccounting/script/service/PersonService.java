package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;
import java.util.Set;

@ScriptExposed
public interface PersonService {

    List<Long> getDuplicate(Set<Long> originalRecordId);

    /**
     * Получить количество уникальных записей в справочнике физлиц, которые не являются дублями для налоговой формы.
     * @param declarationDataId идентификатор налоговой формы
     * @return  количество записей
     */
    int getCountOfUniqueEntries(long declarationDataId);

    /**
     * Сохранить группу Физлиц созданных при идентификаации
     * @param persons коллекция Физлиц
     * @return список ФЛ с добавленными идентификаторами ФЛ
     */
   List<RegistryPerson> saveNewIdentificatedPersons(List<NaturalPerson> persons);

    /**
     * Сохранить группу Физлиц.
     * @param persons коллекция Физлиц
     * @return список ФЛ с добавленными идентификаторами ФЛ
     */
    List<RegistryPerson> savePersons(List<RegistryPerson> persons);

    /**
     * Обновить несколько записей объектов идентификации - физлиц
     * @param persons список ФЛ
     */
   void updatePersons(List<NaturalPerson> persons);

    /**
     * Найти актуальные на текущую дату записи реестра ФЛ связанные с определенной налоговой формой
     * @param declarationDataId идентификатор налоговой формы
     * @return список найденных записей реестра ФЛ
     */
    List<RegistryPerson> findActualRefPersonsByDeclarationDataId(Long declarationDataId);

    /**
     * Найти общее количество ДУЛ для ФЛ
     * @param personRecordId идентификатор ФЛ
     * @return количество ДУЛ ФЛ
     */
    int findIdDocCount(Long personRecordId);
}
