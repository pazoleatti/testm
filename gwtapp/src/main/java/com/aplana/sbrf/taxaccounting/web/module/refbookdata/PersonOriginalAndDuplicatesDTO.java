package com.aplana.sbrf.taxaccounting.web.module.refbookdata;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPerson;

import java.util.List;

/**
 * Объект для передачи с клиента данных об изменениях в дубликатах и оригинале ФЛ
 * @author dloshkarev
 */
public class PersonOriginalAndDuplicatesDTO {
    /**
     * Новый оригинал. Если = null, значит старый был удален
     */
    private RefBookPerson currentPerson;
    /**
     * Новый оригинал. Если = null, значит старый был удален
     */
    private RefBookPerson original;
    /**
     * Список новых дубликатов
     */
    private List<RefBookPerson> newDuplicates;
    /**
     * Список удаленных дубликатов
     */
    private List<RefBookPerson> deletedDuplicates;

    public RefBookPerson getOriginal() {
        return original;
    }

    public void setOriginal(RefBookPerson original) {
        this.original = original;
    }

    public List<RefBookPerson> getNewDuplicates() {
        return newDuplicates;
    }

    public void setNewDuplicates(List<RefBookPerson> newDuplicates) {
        this.newDuplicates = newDuplicates;
    }

    public List<RefBookPerson> getDeletedDuplicates() {
        return deletedDuplicates;
    }

    public void setDeletedDuplicates(List<RefBookPerson> deletedDuplicates) {
        this.deletedDuplicates = deletedDuplicates;
    }

    public RefBookPerson getCurrentPerson() {
        return currentPerson;
    }

    public void setCurrentPerson(RefBookPerson currentPerson) {
        this.currentPerson = currentPerson;
    }
}
