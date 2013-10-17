package com.aplana.sbrf.taxaccounting.scheduler.core.task;

import com.ibm.websphere.scheduler.TaskStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;
import com.aplana.sbrf.taxaccounting.scheduler.core.service.TaskServiceLocal;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;

/**
 * Класс, запускающий логику задачи планировщика
 * @author dloshkarev
 */
@Remote(TaskExecutorRemote.class)
@RemoteHome(TaskExecutorRemoteHome.class)
@Stateless
public class TaskExecutorBean implements TaskExecutorRemote {
    private static final Log LOG = LogFactory.getLog(TaskExecutorBean.class);

    @EJB
    private TaskServiceLocal taskService;

    public TaskExecutorBean() {}

    @Override
    public void process(TaskStatus taskStatus) {
        LOG.info("Processing task with id="+taskStatus.getTaskId());
        try {
            taskService.startTaskById(Long.parseLong(taskStatus.getTaskId()));
        } catch (TaskExecutionException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }
}
