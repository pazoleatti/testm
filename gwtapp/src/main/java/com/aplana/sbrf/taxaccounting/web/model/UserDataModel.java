package com.aplana.sbrf.taxaccounting.web.model;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;

/**
 * Модель данных о пользователе, которые передаются на клиент
 */
public class UserDataModel {
    private TAUserInfo taUserInfo;
    private RefBookDepartment userDepartment;
    private Department userTB;

    public UserDataModel(TAUserInfo taUserInfo, RefBookDepartment userDepartment, Department userTB) {
        this.taUserInfo = taUserInfo;
        this.userDepartment = userDepartment;
        this.userTB = userTB;
    }

    public TAUserInfo getTaUserInfo() {
        return taUserInfo;
    }

    public RefBookDepartment getUserDepartment() {
        return userDepartment;
    }

    public Department getUserTB() {
        return userTB;
    }
}
