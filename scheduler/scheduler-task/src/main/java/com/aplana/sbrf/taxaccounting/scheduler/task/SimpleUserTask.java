package com.aplana.sbrf.taxaccounting.scheduler.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.InvalidTaskParamException;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskExecutionException;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTask;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskLocal;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskRemote;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import java.util.Date;
import java.util.Map;

@Local(UserTaskLocal.class)
@Remote(UserTaskRemote.class)
@Stateless
public class SimpleUserTask implements UserTask {
    private static final Log LOG = LogFactory.getLog(SimpleUserTask.class);

    @Override
    public void execute(Map<String, TaskParam> params) throws TaskExecutionException {
        LOG.info("SimpleUserTask started");
        try {
            boolean b = (Boolean) params.get("testB").getTypifiedValue();
            int i = (Integer) params.get("testI").getTypifiedValue();
            float f = (Float) params.get("testF").getTypifiedValue();
            Date d = (Date) params.get("testD").getTypifiedValue();
            System.out.println(b);
            System.out.println(i);
            System.out.println(f);
            System.out.println(d);
        } catch (InvalidTaskParamException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskExecutionException("Ошибка получения параметров", e);
        }
    }
}
