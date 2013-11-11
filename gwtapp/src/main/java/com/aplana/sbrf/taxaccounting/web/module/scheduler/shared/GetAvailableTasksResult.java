package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * Результат получения списка доступных задач
 * @author dloshkarev
 */
public class GetAvailableTasksResult implements Result {
    private static final long serialVersionUID = 1215417224661845534L;

    private List<TaskInfoItem> jndiList;

    public List<TaskInfoItem> getJndiList() {
        return jndiList;
    }

    public void setJndiList(List<TaskInfoItem> jndiList) {
        this.jndiList = jndiList;
    }
}
