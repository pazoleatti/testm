package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class SetActiveAction extends UnsecuredActionImpl<SetActiveResult> {
    private boolean force;

    private int dtId;

    public boolean getForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public int getDtId() {
        return dtId;
    }

    public void setDtId(int dtId) {
        this.dtId = dtId;
    }
}
