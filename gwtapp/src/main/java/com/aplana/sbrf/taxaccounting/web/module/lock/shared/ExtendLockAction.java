package com.aplana.sbrf.taxaccounting.web.module.lock.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class ExtendLockAction extends UnsecuredActionImpl<ExtendLockResult> implements ActionName {

    private List<String> keys;

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    @Override
    public String getName() {
        return "Продление блокировки";
    }
}
