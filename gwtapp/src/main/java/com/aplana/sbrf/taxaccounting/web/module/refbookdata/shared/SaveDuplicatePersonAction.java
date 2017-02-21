package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;
import java.util.List;

public class SaveDuplicatePersonAction extends UnsecuredActionImpl<SaveDuplicatePersonResult> implements ActionName {

    /**
     * Выбранная запись
     */
    private RefBookDataRow record;
    /**
     * Запись в таблице оригинала
     */
    private RefBookDataRow originalRecord;
    /**
     * Записи в таблице дубликатов
     */
    private List<RefBookDataRow> duplicateRecords;
    /**
     * Записи в таблице дубликатов
     */
    private List<RefBookDataRow> deleteDuplicateRecords;

    public RefBookDataRow getRecord() {
        return record;
    }

    public void setRecord(RefBookDataRow record) {
        this.record = record;
    }

    public RefBookDataRow getOriginalRecord() {
        return originalRecord;
    }

    public void setOriginalRecord(RefBookDataRow originalRecord) {
        this.originalRecord = originalRecord;
    }

    public List<RefBookDataRow> getDuplicateRecords() {
        return duplicateRecords;
    }

    public void setDuplicateRecords(List<RefBookDataRow> duplicateRecords) {
        this.duplicateRecords = duplicateRecords;
    }

    public List<RefBookDataRow> getDeleteDuplicateRecords() {
        return deleteDuplicateRecords;
    }

    public void setDeleteDuplicateRecords(List<RefBookDataRow> deleteDuplicateRecords) {
        this.deleteDuplicateRecords = deleteDuplicateRecords;
    }

    @Override
	public String getName() {
		return "Получить строку из справочника";
	}
}
