package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class LoadRefBookResult implements Result {
	private static final long serialVersionUID = -1968464631322240909L;

    public enum CreateAsyncTaskStatus {
        LOCKED, //есть блокировка
        EXIST_TASK, //существуют задачи, которые будут удалены при выполнении данной
        CREATE //создана новая задача
    }
	private String uuid;
    private CreateAsyncTaskStatus status;
    private String restartMsg;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public CreateAsyncTaskStatus getStatus() {
        return status;
    }

    public void setStatus(CreateAsyncTaskStatus status) {
        this.status = status;
    }

    public String getRestartMsg() {
        return restartMsg;
    }

    public void setRestartMsg(String restartMsg) {
        this.restartMsg = restartMsg;
    }
}
