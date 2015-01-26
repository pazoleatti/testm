package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CheckSettingExistResult implements Result {
    boolean settingsExist = true;

    public boolean isSettingsExist() {
        return settingsExist;
    }

    public void setSettingsExist(boolean settingsExist) {
        this.settingsExist = settingsExist;
    }
}
