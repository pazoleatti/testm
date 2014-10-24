package com.aplana.sbrf.taxaccounting.common.model;

import java.io.Serializable;

/**
 * Модель данных о пользователе
 *
 * @author aivanov
 */
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 3668701744115263174L;

    private long userId;
    private String userIp;

    public UserInfo() {
    }

    public UserInfo(long userId, String userIp) {
        this.userId = userId;
        this.userIp = userIp;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("UserInfo{");
        sb.append("userId=").append(userId);
        sb.append(", userIp='").append(userIp).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
