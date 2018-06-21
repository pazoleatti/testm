package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class CreateApplication2ServiceImpl implements CreateApplication2Service{

    @Autowired
    private AsyncManager asyncManager;

    @Autowired
    private LockDataService lockDataService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private RefBookScriptingService refBookScriptingService;

    @Autowired
    private BlobDataService blobDataService;

    @Autowired
    private ApplicationInfo applicationInfo;

    @Override
    @PreAuthorize("hasPermission(#userInfo.getUser(), T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_TAXES_CREATE_APPLICATION_2)")
    public String createApplication2Task(int reportYear, TAUserInfo userInfo) {
        Logger logger = new Logger();
        String keyTask = "CREATE_APPLICATION_2_FOR_YEAR_" + reportYear;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("reportYear", reportYear);
        asyncManager.executeTask(keyTask, AsyncTaskType.CREATE_APPLICATION_2, userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
            @Override
            public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                return lockDataService.lockAsync(keyTask, userInfo.getUser().getId());
            }
        });
        String uuid = logEntryService.save(logger.getEntries());
        return uuid;
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.getUser(), T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_TAXES_CREATE_APPLICATION_2)")
    public String performCreateApplication2(int reportYear, TAUserInfo userInfo, Logger logger) throws IOException {
        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put("reportYear", reportYear);
        ScriptSpecificRefBookReportHolder scriptSpecificReportHolder = new ScriptSpecificRefBookReportHolder();
        File reportFile = File.createTempFile("report", ".dat");
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(reportFile))) {
            scriptSpecificReportHolder.setFileOutputStream(outputStream);
            scriptSpecificReportHolder.setFileName("report.rnu");
            additionalParameters.put("dataHolder", scriptSpecificReportHolder);
            additionalParameters.put("version", applicationInfo.getVersion());
            refBookScriptingService.executeScript(userInfo, RefBook.Id.DECLARATION_TEMPLATE.getId(), FormDataEvent.CREATE_APPLICATION_2, logger, additionalParameters);
            return blobDataService.create(reportFile.getPath(), scriptSpecificReportHolder.getFileName());
        } finally {
            reportFile.delete();
        }
    }

}
