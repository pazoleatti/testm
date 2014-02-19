package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author fmukhametdinov
 */
public class GetKindListResult implements Result {

    private List<FormDataKind> dataKinds;

    public List<FormDataKind> getDataKinds() {
        return dataKinds;
    }

    public void setDataKinds(List<FormDataKind> dataKinds) {
        this.dataKinds = dataKinds;
    }
}
