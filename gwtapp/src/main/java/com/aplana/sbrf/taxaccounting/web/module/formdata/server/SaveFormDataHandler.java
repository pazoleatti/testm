package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/** @author Vitalii Samolovskikh */
@Service
public class SaveFormDataHandler extends AbstractActionHandler<SaveFormDataAction, SaveFormDataResult> {
    private final Log logger = LogFactory.getLog(getClass());
    
	@Autowired
	private FormDataScriptingService service;
    @Autowired
    private FormDataDao formDataDao;
	@Autowired
	private SecurityService securityService;

    public SaveFormDataHandler() {
        super(SaveFormDataAction.class);
    }

    @Override
    public SaveFormDataResult execute(SaveFormDataAction action, ExecutionContext context) throws ActionException {
        try {
            Logger logger = new Logger();
            final FormData formData = action.getFormData();
            service.processFormData(logger, formData, securityService.currentUser());

            SaveFormDataResult result = new SaveFormDataResult();            
            
            if (!logger.containsLevel(LogLevel.ERROR)) {
                long formDataId = formDataDao.save(formData);
                logger.info("Данные успешно записаны, идентификтор: %d", formDataId);
                formData.setId(formDataId);
            } else {
                logger.warn("Данные формы не сохранены, так как обнаружены ошибки");
            }
            result.setFormData(formData);            
            result.setLogEntries(logger.getEntries());
            return result;
        } catch (Throwable t) {
            logger.error("Failed to save FormData object", t);
            throw new ActionException(t);
        }
    }

    @Override
    public void undo(SaveFormDataAction action, SaveFormDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
