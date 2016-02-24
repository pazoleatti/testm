package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.PrintingService;
import com.aplana.sbrf.taxaccounting.service.script.PrintingScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("printingScriptService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrintingScriptScriptServiceImpl implements PrintingScriptService {

    @Autowired
    private PrintingService service;

    @Override
    public String generateExcel(TAUserInfo userInfo, long formDataId, boolean manual, boolean isShowChecked, boolean saved, boolean deleteHiddenColumns, LockStateLogger stateLogger) {
        return service.generateExcel(userInfo, formDataId, manual, isShowChecked, saved, deleteHiddenColumns, stateLogger);
    }
}
