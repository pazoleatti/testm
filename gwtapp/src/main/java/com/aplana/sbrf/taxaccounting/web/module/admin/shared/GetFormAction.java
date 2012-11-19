package com.aplana.sbrf.taxaccounting.web.module.admin.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Vitalii Samolovskikh
 */
public class GetFormAction extends UnsecuredActionImpl<GetFormResult> {
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
