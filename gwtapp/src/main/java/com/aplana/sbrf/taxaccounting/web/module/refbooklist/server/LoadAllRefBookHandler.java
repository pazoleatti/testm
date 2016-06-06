package com.aplana.sbrf.taxaccounting.web.module.refbooklist.server;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.service.LoadRefBookDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.LoadRefBookAction;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.LoadRefBookResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Component
public class LoadAllRefBookHandler extends AbstractActionHandler<LoadRefBookAction, LoadRefBookResult> {

    @Autowired
    private LoadRefBookDataService loadRefBookDataService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private LockDataService lockDataService;

    public LoadAllRefBookHandler() {
        super(LoadRefBookAction.class);
    }

    @Override
    public LoadRefBookResult execute(LoadRefBookAction arg0,
                                     ExecutionContext arg1) throws ActionException {
        TAUserInfo userInfo = securityService.currentUserInfo();
        LoadRefBookResult result = new LoadRefBookResult();
        Logger logger = new Logger();
        // Проверки путей
        ConfigurationParamModel model = configurationService.getByDepartment(0, securityService.currentUserInfo());
        List<String> okatoList = model.get(ConfigurationParam.OKATO_UPLOAD_DIRECTORY, 0);
        List<String> regionList = model.get(ConfigurationParam.REGION_UPLOAD_DIRECTORY, 0);
        List<String> diasoftList = model.get(ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY, 0);
        List<String> accountPlanList = model.get(ConfigurationParam.ACCOUNT_PLAN_UPLOAD_DIRECTORY, 0);
        boolean hasOkato = okatoList != null && !okatoList.isEmpty();
        boolean hasRegion = regionList != null && !regionList.isEmpty();
        boolean hasDiasoft = diasoftList != null && !diasoftList.isEmpty();
        boolean hasAccountPlan = accountPlanList != null && !accountPlanList.isEmpty();
        if (hasOkato || hasRegion || hasDiasoft || hasAccountPlan) {
            List<String> catalogStrList = new LinkedList<String>();
            if (hasOkato) {
                catalogStrList.add(ConfigurationParam.OKATO_UPLOAD_DIRECTORY.getCaption());
            }
            if (hasRegion) {
                catalogStrList.add(ConfigurationParam.REGION_UPLOAD_DIRECTORY.getCaption());
            }
            if (hasDiasoft) {
                catalogStrList.add(ConfigurationParam.DIASOFT_UPLOAD_DIRECTORY.getCaption());
            }
            if (hasAccountPlan) {
                catalogStrList.add(ConfigurationParam.ACCOUNT_PLAN_UPLOAD_DIRECTORY.getCaption());
            }
            if (catalogStrList.size() == 1) {
                logger.info("Получен: %s.", catalogStrList.get(0));
            } else {
                logger.info("Получены: %s.", StringUtils.join(catalogStrList.toArray(), ", ", null));
            }
            String key = LockData.LockObjects.CONFIGURATION_PARAMS.name() + "_" + UUID.randomUUID().toString().toLowerCase();
            lockDataService.lock(key, userInfo.getUser().getId(),
                    LockData.DescriptionTemplate.CONFIGURATION_PARAMS.getText());
            try {
                String lockId = UUID.randomUUID().toString().toLowerCase();
                logger.info("Номер загрузки: %s", lockId);
                loadRefBookDataService.checkImportRefBooks(securityService.currentUserInfo(), logger, lockId, false);
            } finally {
                lockDataService.unlock(key, userInfo.getUser().getId());
            }
        } else {
            logger.warn("Не указан путь ни к одному из каталогов загрузки ТФ, содержащих данные справочников.");
        }
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    public void undo(LoadRefBookAction arg0, LoadRefBookResult arg1,
                     ExecutionContext arg2) throws ActionException {
        // Auto-generated method stub
    }
}
