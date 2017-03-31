package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetNdflReferencesResult implements Result {

    List<NdflReferenceDTO> ndflReferences;

    public List<NdflReferenceDTO> getNdflReferences() {
        return ndflReferences;
    }

    public void setNdflReferences(List<NdflReferenceDTO> ndflReferences) {
        this.ndflReferences = ndflReferences;
    }
}
