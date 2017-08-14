package com.aplana.sbrf.taxaccounting.web.model;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

/**
 * Модель данных о пользователе, которые передаются на клиент
 */
public class UserDataModel {
    private TAUserInfo taUserInfo;
    private String department;

    public UserDataModel(TAUserInfo taUserInfo, String department) {
        this.taUserInfo = taUserInfo;
        this.department = department;
    }

    public TAUserInfo getTaUserInfo() {
        return taUserInfo;
    }

    public String getDepartment() {
        return department;
    }
}
