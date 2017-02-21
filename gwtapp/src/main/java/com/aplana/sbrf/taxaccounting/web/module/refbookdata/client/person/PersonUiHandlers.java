package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.person;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;

/**
 * @author Lhaziev
 */
public interface PersonUiHandlers extends UiHandlers{
    void onSave();
    RefBookDataRow getRow();
    List<RefBookDataRow> getDuplicateRows();
    RefBookDataRow getOriginalRow();
    void addOriginalPerson(Long recordId);
    void removeOriginalPerson();
    void addDuplicatePerson(Long recordId);
    void removeDuplicatePerson(List<RefBookDataRow> refBookDataRowList);
}
