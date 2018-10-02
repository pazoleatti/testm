package com.aplana.sbrf.taxaccounting.model.action;

import java.util.List;

/**
 * Объект для передачи с клиента данных об изменениях в дубликатах и оригинале ФЛ
 */
public class PersonOriginalAndDuplicatesAction {
    /**
     * Идентификатор изменяемого ФЛ
     */
    private Long changingPersonRecordId;

    /**
     * Исходный идентификатор изменяемого ФЛ
     */
    private Long changingPersonOldId;

    /**
     * Идентификатор версии добавленного оригинала
     */
    private Long addedOriginalVersionId;

    /**
     * Идентификатор добавленного оригинала
     */
    private Long addedOriginal;

    /**
     * Флаг указывающий на удаление оригинала
     */
    private boolean deleteOriginal;


    /**
     * Идентификаторы добавленных дубликатов
     */
    private List<Long> addedDuplicates;

    /**
     * Идентификаторы удаленных дубликатов
     */
    private List<Long> deletedDuplicates;

    public Long getChangingPersonRecordId() {
        return changingPersonRecordId;
    }

    public void setChangingPersonRecordId(Long changingPersonRecordId) {
        this.changingPersonRecordId = changingPersonRecordId;
    }

    public Long getAddedOriginalVersionId() {
        return addedOriginalVersionId;
    }

    public void setAddedOriginalVersionId(Long addedOriginalVersionId) {
        this.addedOriginalVersionId = addedOriginalVersionId;
    }

    public Long getChangingPersonOldId() {
        return changingPersonOldId;
    }

    public void setChangingPersonOldId(Long changingPersonOldId) {
        this.changingPersonOldId = changingPersonOldId;
    }

    public Long getAddedOriginal() {
        return addedOriginal;
    }

    public void setAddedOriginal(Long addedOriginal) {
        this.addedOriginal = addedOriginal;
    }

    public boolean isDeleteOriginal() {
        return deleteOriginal;
    }

    public void setDeleteOriginal(boolean deleteOriginal) {
        this.deleteOriginal = deleteOriginal;
    }

    public List<Long> getAddedDuplicates() {
        return addedDuplicates;
    }

    public void setAddedDuplicates(List<Long> addedDuplicates) {
        this.addedDuplicates = addedDuplicates;
    }

    public List<Long> getDeletedDuplicates() {
        return deletedDuplicates;
    }

    public void setDeletedDuplicates(List<Long> deletedDuplicates) {
        this.deletedDuplicates = deletedDuplicates;
    }
}
