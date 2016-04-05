package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.MessageGenerator;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DataRowResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class SaveFormDataHandler extends
        AbstractActionHandler<SaveFormDataAction, DataRowResult> {

	@Autowired
	private SecurityService securityService;
	@Autowired
	private FormDataService formDataService;
	@Autowired
	private DataRowService dataRowService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
	private RefBookHelper refBookHelper;
    @Autowired
    private LockDataService lockService;


    public SaveFormDataHandler() {
		super(SaveFormDataAction.class);
	}

	@Override
    @Transactional
	public DataRowResult execute(SaveFormDataAction action, ExecutionContext context) throws ActionException {
		Logger logger = new Logger();
		FormData formData = action.getFormData();
        TAUserInfo userInfo = securityService.currentUserInfo();
        // Форма должна быть заблокирована текущим пользователем для редактирования
        formDataService.checkLockedMe(formDataService.getObjectLock(action.getFormData().getId(), userInfo), userInfo.getUser());
        // Проверяем не заблокирована ли нф какими-либо операциями
        formDataService.checkLockedByTask(formData.getId(), logger, securityService.currentUserInfo(), String.format("Сохранение %s", MessageGenerator.mesSpeckSingleD(action.getFormData().getFormType().getTaxType())), true);
		if (!action.getModifiedRows().isEmpty()) {
            refBookHelper.dataRowsCheck(action.getModifiedRows(), formData.getFormColumns());
		    dataRowService.update(userInfo, formData.getId(), action.getModifiedRows(), formData.isManual());
		}
		formDataService.saveFormData(logger, userInfo, formData, false);
		// Создаем контрольную точку восстановления
		dataRowService.createCheckPoint(formData);

		logger.info("Данные успешно записаны");
		DataRowResult result = new DataRowResult();
        result.setUuid(logEntryService.save(logger.getEntries()));
		return result;
	}

	@Override
	public void undo(SaveFormDataAction action, DataRowResult result,
			ExecutionContext context) throws ActionException {
		// Nothing!
	}
}
