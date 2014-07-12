package com.aplana.sbrf.taxaccounting.web.module.scheduler.shared;

import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskJndiInfo;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * Результат получения списка доступных задач
 * @author dloshkarev
 */
public class GetAvailableTasksResult implements Result {
    private static final long serialVersionUID = 1215417224661845534L;

    private List<TaskJndiInfo> jndiList;

    public List<TaskJndiInfo> getJndiList() {
        return jndiList;
    }

    public void setJndiList(List<TaskJndiInfo> jndiList) {
        this.jndiList = jndiList;
    }
}
