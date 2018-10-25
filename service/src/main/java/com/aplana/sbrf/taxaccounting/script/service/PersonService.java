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
     * Сохранить группу Физлиц.
     * @param persons коллекция Физлиц
     * @return список ФЛ с добавленными идентификаторами ФЛ
     */
   List<RegistryPerson> saveNewPersons(List<NaturalPerson> persons);

    /**
     * Обновить несколько записей объектов идентификации - физлиц
     * @param persons список ФЛ
     */
   void updatePersons(List<NaturalPerson> persons);

}
