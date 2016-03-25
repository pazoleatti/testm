package com.aplana.sbrf.taxaccounting.web.module.formtemplate.server;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.StyleService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.FormTemplateExt;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFormResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author Vitalii Samolovskikh
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class GetFormHandler extends AbstractActionHandler<GetFormAction, GetFormResult> {
    @Autowired
	private FormTemplateService formTemplateService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private StyleService styleService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private TAUserService taUserService;

    public GetFormHandler() {
        super(GetFormAction.class);
    }

    @Override
    public GetFormResult execute(GetFormAction action, ExecutionContext context) throws ActionException {
		TAUserInfo userInfo = securityService.currentUserInfo();

        Logger logger = new Logger();
        GetFormResult result = new GetFormResult();
        fillLockData(action, userInfo, result);
        FormTemplateExt formTemplateExt = new FormTemplateExt();
		FormTemplate formTemplate = formTemplateService.get(action.getId(), logger);
        formTemplateExt.setActualEndVersionDate(formTemplateService.getFTEndDate(formTemplate.getId()));
        formTemplateExt.setStyles(styleService.getAll());
        formTemplate.setScript(formTemplate.getScript());
        formTemplateExt.setFormTemplate(formTemplate);
        result.setForm(formTemplateExt);
        List<RefBook> refBookList = refBookFactory.getAll(false);
        Collections.sort(refBookList, new Comparator<RefBook>() {
            @Override
            public int compare(RefBook o1, RefBook o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        result.setRefBookList(refBookList);

        if (!logger.getEntries().isEmpty())
            result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    /**
     * Блокирует макет при необходимости, заполняет состояние блокировки
     *
     * @param action
     * @param userInfo
     * @param result
     */
    private void fillLockData(GetFormAction action, TAUserInfo userInfo,
                              GetFormResult result) {
        LockData lockInformation = formTemplateService.getObjectLock(action
                .getId(), securityService.currentUserInfo());
        if (lockInformation != null) {
            // Если данная форма уже заблокирована
            result.setLockedByUser(taUserService.getUser(lockInformation.getUserId()).getName());
            result.setLockDate(getFormedDate(lockInformation.getDateLock()));
            if (lockInformation.getUserId() != userInfo.getUser().getId()) {
                result.setLockedByAnotherUser(true);
            }
        }
        if (!result.isLockedByAnotherUser()) {
            formTemplateService.lock(action.getId(), userInfo);
        }
    }

    private static String getFormedDate(Date dateToForm) {
        // Преобразуем Date в строку вида "dd.mm.yyyy hh:mm"
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        formatter.format(dateToForm);
        return (formatter.format(dateToForm));
    }

    @Override
    public void undo(GetFormAction action, GetFormResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
