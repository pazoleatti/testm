package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 * Created by lhaziev on 16.06.2015.
 */
public class LockInfo implements IsSerializable, Serializable {
    private static final long serialVersionUID = -1516573159851378944L;

    private String lockDate;
    private String lockedByUser;
    private String title;

    // форма находиться в режиме редактирования(неважно кем)
    private boolean editMode;
    // форма заблокирована текущим пользователем
    private boolean lockedMe;

    public String getLockDate() {
        return lockDate;
    }

    public void setLockDate(String lockDate) {
        this.lockDate = lockDate;
    }

    public String getLockedByUser() {
        return lockedByUser;
    }

    public void setLockedByUser(String lockedByUser) {
        this.lockedByUser = lockedByUser;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isLockedMe() {
        return lockedMe;
    }

    public void setLockedMe(boolean lockedMe) {
        this.lockedMe = lockedMe;
    }
}
