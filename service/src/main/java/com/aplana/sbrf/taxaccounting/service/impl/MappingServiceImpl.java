package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodMappingDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.migration.RestoreExemplar;
import com.aplana.sbrf.taxaccounting.model.migration.enums.*;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

@Service
public class MappingServiceImpl implements MappingService {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private ReportPeriodMappingDao reportPeriodMappingDao;

    @Autowired
    private FormDataDao formDataDao;

    @Autowired
    private ReportPeriodDao periodDao;

    @Autowired
    private AuditService auditService;

    @Autowired
    private TAUserService taUserService;

    @Autowired
    private LogEntryService logEntryService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
    private static final String CHARSET = "cp866";
    private static final String RNU_EXT = ".rnu";
    private static final String XML_EXT = ".xml";
    private static final String DATE_APPENDER_XML = "20";
    private static final String DATE_APPENDER_RNU = "01.01.";

    @Override
    public void addFormData(String filename, byte[] fileContent) {
        log.info("Принят файл " + filename + ", размер = " + (fileContent == null ? null : fileContent.length));

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(taUserService.getUser(TAUser.SYSTEM_USER_ID));
        userInfo.setIp("127.0.0.1");

        RestoreExemplar restoreExemplar;
        Integer departmentId = null;
        Integer formTypeId = null;
        ReportPeriod reportPeriod = null;
        Integer reportPeriodId = null;
        Integer formTemplateId = null;
        Integer periodOrder = null;

        Boolean isAllreadyCreated = false;

        try {
            InputStream inputStream = new ByteArrayInputStream(fileContent);
            if (filename.toLowerCase().endsWith(RNU_EXT)) {
                restoreExemplar = restoreExemplarFromRnu(filename, fileContent);
            } else if (filename.toLowerCase().endsWith(XML_EXT)) {
                restoreExemplar = restoreExemplarFromXml(filename);
            } else {
                throw new ServiceException("Неправильное имя файла " + filename);
            }

            if (restoreExemplar != null) {
                formTemplateId = restoreExemplar.getFormTemplateId();
                FormTemplate template = formTemplateService.get(formTemplateId);
                formTypeId = template.getType().getId();
                departmentId = restoreExemplar.getDepartmentId();
                periodOrder = restoreExemplar.getPeriodOrder();
            }

            log.debug(restoreExemplar);

            reportPeriodId = reportPeriodMappingDao.getByTaxPeriodAndDict(
                    restoreExemplar.getTaxPeriod(),
                    restoreExemplar.getDictTaxPeriodId());

            if (reportPeriodId != null) {
                reportPeriod = periodDao.get(reportPeriodId);
            }

            Logger logger = new Logger();

            FormData formData = null;
            // Если ежемесячная форма
            if (periodOrder != null) {
                formData = formDataDao.findMonth(formTemplateId, FormDataKind.PRIMARY, departmentId, restoreExemplar.getTaxPeriod(), periodOrder);
            } else {
                formData = formDataDao.find(formTemplateId, FormDataKind.PRIMARY, departmentId, reportPeriod.getId());
            }
            System.out.println("formData " + formData);
            if (formData == null) {
                long formDataId = formDataService.createFormData(logger,
                        userInfo,
                        formTemplateId,
                        departmentId,
                        FormDataKind.PRIMARY,
                        reportPeriod, periodOrder);

                // Вызов скрипта
                formDataService.lock(formDataId, userInfo);
                formDataService.migrationFormData(logger, userInfo, formDataId, inputStream, filename);

                formDataService.saveFormData(logger, userInfo, formDataDao.get(formDataId));
                // Принудительно выставлен статус "Принята"
                formDataDao.updateState(formDataId, WorkflowState.ACCEPTED);
                formDataService.unlock(formDataId, userInfo);
            } else {
                isAllreadyCreated = true;
            }
        } catch (Exception e) {
            if (e instanceof ServiceLoggerException) {
                ServiceLoggerException sle = (ServiceLoggerException)e;
                log.error(ServiceLoggerException.getLogEntriesString(logEntryService.getAll(sle.getUuid())));
            }

            // Ошибка импорта
            log.error("Ошибка импорта файла " + filename + " : " + e.getClass() + " " +  e.getMessage(), e);
            addLog(userInfo, departmentId, reportPeriodId, formTypeId, "Ошибка импорта файла " + filename + ": "
                        + e.getMessage());

            return;
        }

        if(isAllreadyCreated){
            // Форма уже была создана
            log.info("Уже был создан экземпляр формы с такими параметрами как в " + filename + " departmentId = " + departmentId + " reportPeriodId = "
                    + reportPeriodId );
            addLog(userInfo, departmentId, reportPeriodId, formTypeId, "Экзмепляр формы для файла " + filename + " уже существует. Импорт файла был пропущен.");

        } else {
            // Успешный импорт
            log.info("Успешно импортирован файл " + filename + " departmentId = " + departmentId + " reportPeriodId = "
                    + reportPeriodId + " formTypeId = " + formTypeId);
            addLog(userInfo, departmentId, reportPeriodId, formTypeId, "Успешно импортирован файл " + filename);
        }

    }

    /**
     * Запись в журнал аудита
     * @param userInfo
     * @param departmentId
     * @param reportPeriodId
     * @param formTypeId
     * @param msg
     */
    private void addLog(TAUserInfo userInfo, Integer departmentId, Integer reportPeriodId, Integer formTypeId,
                        String msg) {
        try {
            // Ошибка записи в журнал аудита не должна откатывать импорт
            auditService.add(FormDataEvent.MIGRATION, userInfo, departmentId, reportPeriodId, null, formTypeId,
                    FormDataKind.PRIMARY.getId(), msg);
        } catch (Exception e) {
            log.error("Ошибка записи в журнал аудита", e);
        }
    }

    /**
     * Возвращает объект с необходимой информацией для создания формы в нвоой системе
     * Пример первой строки: 99|0013|01.01.2013|31.03.2013|640|90
     *
     * @param rnuFilename название файла с расширением rnu
     * @param fileContent содержимое файла
     * @return
     */
    private RestoreExemplar restoreExemplarFromRnu(String rnuFilename, byte[] fileContent) throws ServiceException {
        RestoreExemplar exemplar = new RestoreExemplar();

        String firstRow;
        try {
            String str = new String(fileContent, CHARSET);
            firstRow = str.substring(0, str.indexOf('\r'));
            log.debug("firstRow: " + firstRow);
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("Failure to get the file first line! " + e.getMessage(), e);
        }

        String[] params = firstRow.split("\\|");
        try {

            exemplar.setBeginDate(DATE_FORMAT.parse(params[2]));
            exemplar.setEndDate(DATE_FORMAT.parse(params[3]));

            // по коду NNN в назавании файла тип налоговой формы
            exemplar.setFormTemplateId(NalogFormType.getNewCodeByNNN(rnuFilename.substring(0, 3)));

            // код подразделения
            String system = params[5].trim();
            String depCode = rnuFilename.substring(3, 6);
            if (system.length() == 1) {
                exemplar.setDepartmentId(DepartmentRnuMapping.getDepartmentId(depCode, system, null));
            } else if (system.length() == 3) {
                exemplar.setDepartmentId(DepartmentRnuMapping.getDepartmentId(depCode, system.substring(0, 1), system.substring(1, 3)));
            } else {
                throw new ServiceException("Error by department code mapping! depCode: " + depCode);
            }

            //по году определяем TAX_PERIOD
            String year = YEAR_FORMAT.format(exemplar.getBeginDate());
            //year = DATE_APPENDER_RNU + year;
            exemplar.setTaxPeriod(reportPeriodMappingDao.getTaxPeriodByDate(year));

            // по коду отчетного периода 7 символа в назавании файла DICT_TAX_PERIOD
            String periodCode = rnuFilename.substring(7, 8);
            exemplar.setDictTaxPeriodId(PeriodMapping.fromCode(periodCode).getDictTaxPeriodId());

            //Если форма ежемесячная, то заполняем месяц
            if (NalogFormType.RNU31.getCodeNew() == exemplar.getFormTemplateId()) {
                exemplar.setPeriodOrder(PeriodMapping.fromCode(periodCode).getDictTaxPeriodIdForMonthly());
            }

            return exemplar;
        } catch (Exception e) {
            throw new ServiceException("Ошибка (" + e.getClass() + ") разбора файла: " + e.getLocalizedMessage() + "Объект парсинга файла: " + exemplar, e);
        }
    }

    /**
     * Возвращает объект с необходимой информацией для создания формы в нвоой системе
     * Пример названия файла 852-64____996300020__10901q0613.xml
     *
     * @param xmlFilename название файла
     * @return
     */
    private RestoreExemplar restoreExemplarFromXml(String xmlFilename) throws ServiceException {
        RestoreExemplar exemplar = new RestoreExemplar();

        try {
            String nalogForm = xmlFilename.substring(0, 10).replace("_", "");   // 852-64____ -> 852-64
            exemplar.setFormTemplateId(NalogFormType.fromCodeNewXml(nalogForm).getCodeNew());

            String depCode = xmlFilename.substring(10, 19);                     // 996300020
            String systemCode = xmlFilename.substring(19, 24).replace("_", ""); // __109 -> 109
            String subSystemCode = xmlFilename.substring(24, 26);               // 00

            exemplar.setDepartmentId(DepartmentXmlMapping.getNewDepCode(depCode, Integer.valueOf(systemCode), subSystemCode));

            //по году определяем TAX_PERIOD
            String yearCut = xmlFilename.substring(29, 31);                     // 13
            yearCut = DATE_APPENDER_XML + yearCut;                                  // 20 + 13 - > 2013
            exemplar.setTaxPeriod(reportPeriodMappingDao.getTaxPeriodByDate(yearCut));

            String period = xmlFilename.substring(26, 29);                      // q06
            exemplar.setDictTaxPeriodId(PeriodMapping.fromCodeXml(period).getDictTaxPeriodId());

            //Если форма ежемесячная, то заполняем месяц
            if (NalogFormType.RNU31.getCodeNew() == exemplar.getFormTemplateId()) {
                exemplar.setPeriodOrder(PeriodMapping.fromCodeXml(period).getDictTaxPeriodIdForMonthly());
            }

            return exemplar;
        } catch (Exception e) {
            throw new ServiceException("Ошибка разбора файла:" + e.getLocalizedMessage(), e);
        }
    }
}
