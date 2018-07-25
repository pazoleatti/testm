package com.aplana.sbrf.taxaccounting.web.model;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;

/**
 * Модель данных о пользователе, которые передаются на клиент
 */
public class UserDataModel {
    private TAUserInfo taUserInfo;
    private RefBookDepartment department;

    public UserDataModel(TAUserInfo taUserInfo, RefBookDepartment department) {
        this.taUserInfo = taUserInfo;
        this.department = department;
    }

    public TAUserInfo getTaUserInfo() {
        return taUserInfo;
    }

    public RefBookDepartment getDepartment() {
        return department;
    }
}
