package com.aplana.sbrf.taxaccounting.scheduler.core.service;

import com.aplana.sbrf.taxaccounting.scheduler.core.utils.TaskUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskContext;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTask;
import com.aplana.sbrf.taxaccounting.scheduler.core.entity.TaskContextEntity;
import com.aplana.sbrf.taxaccounting.scheduler.core.persistence.TaskPersistenceServiceLocal;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import java.util.HashMap;
import java.util.Map;

@Local(TaskServiceLocal.class)
@Stateless
public class TaskServiceBean implements TaskService {
    private static final Log LOG = LogFactory.getLog(TaskServiceBean.class);

    @EJB
    private TaskPersistenceServiceLocal persistenceService;

    @Override
    public void startTaskById(Long taskId) throws TaskExecutionException {
        try {
            //Получение данных задачи, которую надо запустить
            TaskContextEntity taskContextEntity = persistenceService.getContextByTaskId(taskId);

            //Десериализация контекста и получение параметров
            Map<String, TaskParam> params = new HashMap<String, TaskParam>(0);
            if (taskContextEntity.isCustomParamsExist()) {
                params = TaskUtils.deserializeParams(taskContextEntity.getSerializedParams());
            }

            //Вызов задачи
            InitialContext ic = new InitialContext();
            UserTask userTask = (UserTask) ic.lookup(taskContextEntity.getUserTaskJndi());
            userTask.execute(params);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskExecutionException("Ошибка выполнения задачи", e);
        }
    }

    @Override
    public void startTaskWithContext(TaskContext taskContext) throws TaskExecutionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
