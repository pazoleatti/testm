package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class GetUserRoleUploadTransportDataResult implements Result {
	private static final long serialVersionUID = -6037420163541321038L;

    private boolean isCanUpload;
    private boolean isCanLoad;

    public boolean isCanUpload() {
        return isCanUpload;
    }

    public void setCanUpload(boolean canUpload) {
        isCanUpload = canUpload;
    }

    public boolean isCanLoad() {
        return isCanLoad;
    }

    public void setCanLoad(boolean canLoad) {
        isCanLoad = canLoad;
    }
}
