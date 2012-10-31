package com.aplana.sbrf.taxaccounting.gwtapp.server;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.SaveDataAction;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.SaveDataResult;
import com.aplana.sbrf.taxaccounting.log.LogLevel;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** @author Vitalii Samolovskikh */
@Service
public class SaveDataHandler extends AbstractActionHandler<SaveDataAction, SaveDataResult> {
    private FormDataScriptingService service;
    private FormDataDao formDataDao;
    private static final Log log = LogFactory.getLog(SaveDataHandler.class);

    public SaveDataHandler() {
        super(SaveDataAction.class);
    }

    @Override
    public SaveDataResult execute(SaveDataAction action, ExecutionContext context) throws ActionException {
        try {
            log.info("Enter to method.");

            Logger logger = new Logger();
            final FormData formData = action.getFormData();
            service.processFormData(logger, formData);

            if (!logger.containsLevel(LogLevel.ERROR)) {
                long formDataId = formDataDao.save(formData);
                logger.info("Данные успешно записаны, идентификтор: %d", formDataId);
            } else {
                logger.warn("Данные формы не сохранены, так как обнаружены ошибки");
            }

            SaveDataResult result = new SaveDataResult();
            result.setLogEntries(logger.getEntries());

            log.info("get out");
            return result;
        } catch (Throwable t) {
            log.error("Error!", t);
            throw new ActionException(t);
        }
    }

    @Override
    public void undo(SaveDataAction action, SaveDataResult result, ExecutionContext context)
            throws ActionException {
        // Nothing!
    }

    @Autowired
    public void setService(FormDataScriptingService service) {
        this.service = service;
    }

    @Autowired
    public void setFormDataDao(FormDataDao formDataDao) {
        this.formDataDao = formDataDao;
    }
}
