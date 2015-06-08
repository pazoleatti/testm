package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * Результат выполнения действий, которые моифицируют форму каким-либо образом.
 *
 * @author Eugene Stetsenko
 */
public class UploadFormDataResult implements Result {
	private static final long serialVersionUID = -4686362790466910194L;

    private String uuid;
    private boolean lock;
    private boolean save;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public boolean isSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }
}