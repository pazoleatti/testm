package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetDeclarationAction extends UnsecuredActionImpl<GetDeclarationResult> {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
