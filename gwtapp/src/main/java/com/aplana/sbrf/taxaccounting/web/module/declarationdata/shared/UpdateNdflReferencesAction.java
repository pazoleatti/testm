package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class UpdateNdflReferencesAction extends UnsecuredActionImpl<UpdateNdflReferenceResult> {

    private List<Long> ndflReferences;

    private String note;

    public List<Long> getNdflReferences() {
        return ndflReferences;
    }

    public void setNdflReferences(List<Long> ndflReferences) {
        this.ndflReferences = ndflReferences;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
