package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 *
 * @author Andrey Drunk
 */
public class MapXmlToTableAsyncTask extends AbstractAsyncTask {

    private static final Log LOG = LogFactory.getLog(MapXmlToTableAsyncTask.class);

    @Override
    protected TaskStatus executeBusinessLogic(Map<String, Object> params, Logger logger) throws InterruptedException {
        LOG.debug("task will be executed now");

        return null;
    }

    @Override
    protected String getAsyncTaskName() {
        return "НДФЛ. Загрузка первичных данных в БД";
    }

    @Override
    protected String getNotificationMsg(Map<String, Object> params) {
        return null;
    }

    @Override
    protected String getErrorMsg(Map<String, Object> params, boolean unexpected) {
        return null;
    }

    @Override
    protected BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) throws AsyncTaskException {
        return null;
    }

    @Override
    protected ReportType getReportType() {
        return null;
    }
}
