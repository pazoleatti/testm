package com.aplana.sbrf.taxaccounting.web.model;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;

/**
 * Модель данных о пользователе, которые передаются на клиент
 */
public class UserDataModel {
    private TAUserInfo taUserInfo;
    private RefBookDepartment department;
    private RefBookDepartment terBank;

    public UserDataModel(TAUserInfo taUserInfo, RefBookDepartment department, RefBookDepartment terBank) {
        this.taUserInfo = taUserInfo;
        this.department = department;
        this.terBank = terBank;
    }

    public TAUserInfo getTaUserInfo() {
        return taUserInfo;
    }

    public RefBookDepartment getDepartment() {
        return department;
    }

    public RefBookDepartment getTerBank() {
        return terBank;
    }
}
