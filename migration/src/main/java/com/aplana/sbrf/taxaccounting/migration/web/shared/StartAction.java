package com.aplana.sbrf.taxaccounting.migration.web.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class StartAction extends UnsecuredActionImpl<StartResult> {

    private List<Long> rnuList;


    public StartAction(List<Long> rnuList) {
        this.rnuList = rnuList;
    }

    public StartAction() {
    }

    public List<Long> getRnuList() {
        return rnuList;
    }

    public void setRnuList(List<Long> rnuList) {
        this.rnuList = rnuList;
    }
}
