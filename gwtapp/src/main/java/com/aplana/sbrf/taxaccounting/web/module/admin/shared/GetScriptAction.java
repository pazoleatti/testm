package com.aplana.sbrf.taxaccounting.web.module.admin.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Vitalii Samolovskikh
 */
public class GetScriptAction extends UnsecuredActionImpl<GetScriptResult> {
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
