package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

/**
 * @author lhaziev
 */
public class EditRefBookAction extends UnsecuredActionImpl<EditRefBookResult> implements ActionName {

    private long refBookId;

    public long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(long refBookId) {
        this.refBookId = refBookId;
    }

    @Override
	public String getName() {
		return "Загрузка справочника";
	}

    
}
