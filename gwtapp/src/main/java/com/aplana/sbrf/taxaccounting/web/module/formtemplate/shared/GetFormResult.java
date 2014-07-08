package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author Vitalii Samolovskikh
 */
public class GetFormResult implements Result {
    private FormTemplateExt form;
    private List<RefBook> refBookList;
    private String uuid;
    private boolean lockedByAnotherUser = false;
    private String lockDate;
    private String lockedByUser;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public FormTemplateExt getForm() {
        return form;
    }

    public void setForm(FormTemplateExt form) {
        this.form = form;
    }

    public List<RefBook> getRefBookList() {
        return refBookList;
    }

    public void setRefBookList(List<RefBook> refBookList) {
        this.refBookList = refBookList;
    }

    public boolean isLockedByAnotherUser() {
        return lockedByAnotherUser;
    }

    public void setLockedByAnotherUser(boolean lockedByAnotherUser) {
        this.lockedByAnotherUser = lockedByAnotherUser;
    }

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
}
