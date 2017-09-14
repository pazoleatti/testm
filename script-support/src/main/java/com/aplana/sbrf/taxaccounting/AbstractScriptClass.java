package com.aplana.sbrf.taxaccounting;

import com.aplana.sbrf.taxaccounting.dao.script.BlobDataService;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.*;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class AbstractScriptClass {
    protected Logger logger;
    protected FormDataEvent formDataEvent;
    protected DeclarationData declarationData;
    protected DepartmentReportPeriodService departmentReportPeriodService;
    protected DeclarationService declarationService;
    protected ReportPeriodService reportPeriodService;
    protected DepartmentService departmentService;
    protected CalendarService calendarService;
    protected TAUserInfo userInfo;
    protected FiasRefBookService fiasRefBookService;
    protected NdflPersonService ndflPersonService;
    protected Map<String, Object> calculateParams;
    protected RefBookPersonService refBookPersonService;
    protected ScriptSpecificDeclarationDataReportHolder scriptSpecificReportHolder;
    protected RefBookFactory refBookFactory;
    protected Boolean needSources;
    protected Boolean light;
    protected FormSources sources;
    protected File dataFile;
    protected String UploadFileName;
    protected InputStream ImportInputStream;
    protected FileWriter xml;
    protected RefBookService refBookService;
    protected Integer partNumber;
    protected BlobDataService blobDataServiceDaoImpl;
    protected File xmlFile;
    protected Map<Long, Map<String, Object>> formMap;
    protected Map<String, Object> scriptParams;
    protected List<Long> ndflPersonKnfId;
    protected Integer partTotal;
    protected OutputStream outputStream;
    protected Boolean excludeIfNotExist;
    protected State stateRestriction;
    protected String applicationVersion;
    protected InputStream inputStream;
    protected StringBuilder msgBuilder;
    protected List<Map<String, RefBookValue>> saveRecords;
    protected Long uniqueRecordId;
    protected Boolean isNewRecords;
    protected Date validDateFrom;
    protected Long sourceUniqueRecordId;
    protected ImportFiasDataService importFiasDataService;
    protected ScriptStatusHolder scriptStatusHolder;

    private AbstractScriptClass() {
    }

    @SuppressWarnings("unchecked")
    public AbstractScriptClass(groovy.lang.Script scriptClass) {
        if (scriptClass.getBinding().hasVariable("logger")) {
            this.logger = (Logger) scriptClass.getProperty("logger");
        }
        if (scriptClass.getBinding().hasVariable("formDataEvent")) {
            this.formDataEvent = (FormDataEvent) scriptClass.getProperty("formDataEvent");
        }
        if (scriptClass.getBinding().hasVariable("declarationData")) {
            this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData");
        }
        if (scriptClass.getBinding().hasVariable("departmentReportPeriodService")) {
            this.departmentReportPeriodService = (DepartmentReportPeriodService) scriptClass.getProperty("departmentReportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("declarationService")) {
            this.declarationService = (DeclarationService) scriptClass.getProperty("declarationService");
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("departmentService")) {
            this.departmentService = (DepartmentService) scriptClass.getProperty("departmentService");
        }
        if (scriptClass.getBinding().hasVariable("reportPeriodService")) {
            this.reportPeriodService = (ReportPeriodService) scriptClass.getProperty("reportPeriodService");
        }
        if (scriptClass.getBinding().hasVariable("calendarService")) {
            this.calendarService = (CalendarService) scriptClass.getProperty("calendarService");
        }
        if (scriptClass.getBinding().hasVariable("userInfo")) {
            this.userInfo = (TAUserInfo) scriptClass.getProperty("userInfo");
        }
        if (scriptClass.getBinding().hasVariable("fiasRefBookService")) {
            this.fiasRefBookService = (FiasRefBookService) scriptClass.getProperty("fiasRefBookService");
        }
        if (scriptClass.getBinding().hasVariable("ndflPersonService")) {
            this.ndflPersonService = (NdflPersonService) scriptClass.getProperty("ndflPersonService");
        }
        if (scriptClass.getBinding().hasVariable("calculateParams")) {
            this.calculateParams = (Map<String, Object>) scriptClass.getProperty("calculateParams");
        }
        if (scriptClass.getBinding().hasVariable("refBookPersonService")) {
            this.refBookPersonService = (RefBookPersonService) scriptClass.getProperty("refBookPersonService");
        }
        if (scriptClass.getBinding().hasVariable("scriptSpecificReportHolder")) {
            this.scriptSpecificReportHolder = (ScriptSpecificDeclarationDataReportHolder) scriptClass.getProperty("scriptSpecificReportHolder");
        }
        if (scriptClass.getBinding().hasVariable("refBookFactory")) {
            this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory");
        }
        if (scriptClass.getBinding().hasVariable("needSources")) {
            this.needSources = (Boolean) scriptClass.getProperty("needSources");
        }
        if (scriptClass.getBinding().hasVariable("light")) {
            this.light = (Boolean) scriptClass.getProperty("light");
        }
        if (scriptClass.getBinding().hasVariable("sources")) {
            this.sources = (FormSources) scriptClass.getProperty("sources");
        }
        if (scriptClass.getBinding().hasVariable("dataFile")) {
            this.dataFile = (File) scriptClass.getProperty("dataFile");
        }
        if (scriptClass.getBinding().hasVariable("UploadFileName")) {
            this.UploadFileName = (String) scriptClass.getProperty("UploadFileName");
        }
        if (scriptClass.getBinding().hasVariable("ImportInputStream")) {
            this.ImportInputStream = (InputStream) scriptClass.getProperty("ImportInputStream");
        }
        if (scriptClass.getBinding().hasVariable("xml")) {
            this.xml = (FileWriter) scriptClass.getProperty("xml");
        }
        if (scriptClass.getBinding().hasVariable("refBookService")) {
            this.refBookService = (RefBookService) scriptClass.getBinding().getProperty("refBookService");
        }
        if (scriptClass.getBinding().hasVariable("partNumber")) {
            this.partNumber = (Integer) scriptClass.getBinding().getProperty("partNumber");
        }
        if (scriptClass.getBinding().hasVariable("blobDataServiceDaoImpl")) {
            this.blobDataServiceDaoImpl = (BlobDataService) scriptClass.getBinding().getProperty("blobDataServiceDaoImpl");
        }
        if (scriptClass.getBinding().hasVariable("xmlFile")) {
            this.xmlFile = (File) scriptClass.getBinding().getProperty("xmlFile");
        }
        if (scriptClass.getBinding().hasVariable("formMap")) {
            this.formMap = (Map<Long, Map<String, Object>>) scriptClass.getBinding().getProperty("formMap");
        }
        if (scriptClass.getBinding().hasVariable("scriptParams")) {
            this.scriptParams = (Map<String, Object>) scriptClass.getBinding().getProperty("scriptParams");
        }
        if (scriptClass.getBinding().hasVariable("ndflPersonKnfId")) {
            this.ndflPersonKnfId = (List<Long>) scriptClass.getBinding().getProperty("ndflPersonKnfId");
        }
        if (scriptClass.getBinding().hasVariable("partTotal")){
            this.partTotal = (Integer) scriptClass.getBinding().getProperty("partTotal");
        }
        if (scriptClass.getBinding().hasVariable("outputStream")) {
            this.outputStream = (OutputStream) scriptClass.getBinding().getProperty("outputStream");
        }
        if (scriptClass.getBinding().hasVariable("excludeIfNotExist")) {
            this.excludeIfNotExist = (Boolean) scriptClass.getBinding().getProperty("excludeIfNotExist");
        }
        if (scriptClass.getBinding().hasVariable("stateRestriction")) {
            this.stateRestriction = (State) scriptClass.getBinding().getProperty("stateRestriction");
        }
        if (scriptClass.getBinding().hasVariable("applicationVersion")) {
            this.applicationVersion = (String) scriptClass.getBinding().getProperty("applicationVersion");
        }
        if (scriptClass.getBinding().hasVariable("inputStream")) {
            this.inputStream = (InputStream) scriptClass.getBinding().getProperty("inputStream");
        }
        if (scriptClass.getBinding().hasVariable("msgBuilder")) {
            this.msgBuilder = (StringBuilder) scriptClass.getBinding().getProperty("msgBuilder");
        }
        if (scriptClass.getBinding().hasVariable("saveRecords")) {
            this.saveRecords = (List<Map<String, RefBookValue>>) scriptClass.getBinding().getProperty("saveRecords");
        }
        if (scriptClass.getBinding().hasVariable("uniqueRecordId")) {
            this.uniqueRecordId = (Long) scriptClass.getBinding().getProperty("uniqueRecordId");
        }
        if (scriptClass.getBinding().hasVariable("isNewRecords")) {
            this.isNewRecords = (Boolean) scriptClass.getBinding().getProperty("isNewRecords");
        }
        if (scriptClass.getBinding().hasVariable("validDateFrom")) {
            this.validDateFrom = (Date) scriptClass.getBinding().getProperty("validDateFrom");
        }
        if (scriptClass.getBinding().hasVariable("sourceUniqueRecordId")) {
            this.sourceUniqueRecordId = (Long) scriptClass.getBinding().getProperty("sourceUniqueRecordId");
        }
        if (scriptClass.getBinding().hasVariable("importFiasDataService")) {
            this.importFiasDataService = (ImportFiasDataService) scriptClass.getBinding().getProperty("importFiasDataService");
        }
        if (scriptClass.getBinding().hasVariable("scriptStatusHolder")) {
            this.scriptStatusHolder = (ScriptStatusHolder) scriptClass.getBinding().getProperty("scriptStatusHolder");
        }
    }

    public abstract void run();
}
