package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodMappingDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.migration.RestoreExemplar;
import com.aplana.sbrf.taxaccounting.model.migration.enums.DepartmentRnuMapping;
import com.aplana.sbrf.taxaccounting.model.migration.enums.DepartmentXmlMapping;
import com.aplana.sbrf.taxaccounting.model.migration.enums.NalogFormType;
import com.aplana.sbrf.taxaccounting.model.migration.enums.PeriodMapping;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class MappingServiceImpl implements MappingService {

    private static final Log LOG = LogFactory.getLog(MappingServiceImpl.class);

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

    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");
    private static final String CHARSET = "cp866";
    private static final String RNU_EXT = ".rnu";
    private static final String XML_EXT = ".xml";
    private static final String DATE_APPENDER_XML = "20";

    @Override
    public void addFormData(String filename, byte[] fileContent) {
        LOG.info("Принят файл " + filename + ", размер = " + (fileContent == null ? null : fileContent.length));

        TAUserInfo userInfo = taUserService.getSystemUserInfo();

        RestoreExemplar restoreExemplar;
        Integer departmentId = null;
        String formTypeName = null;
        ReportPeriod reportPeriod = null;
        Integer reportPeriodId = null;
        Integer formTemplateId = null;
        Integer periodOrder = null;

        Boolean isAlreadyCreated = false;

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
                formTypeName = template.getType().getName();
                departmentId = restoreExemplar.getDepartmentId();
                periodOrder = restoreExemplar.getPeriodOrder();
            }

            LOG.debug(restoreExemplar);

            reportPeriodId = reportPeriodMappingDao.getByTaxPeriodAndDict(
                    restoreExemplar.getTaxPeriod(),
                    restoreExemplar.getDictTaxPeriodId());

            if (reportPeriodId != null) {
                reportPeriod = periodDao.get(reportPeriodId);
            }

            // Отчетный период подразделения
            DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
            filter.setDepartmentIdList(Arrays.asList(departmentId));
            filter.setReportPeriodIdList(Arrays.asList(reportPeriod.getId()));
            filter.setIsActive(true);
            // Открытый отчетный период подразделения может быть только один или отсутствовать
            List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodDao.getListByFilter(filter);
            DepartmentReportPeriod departmentReportPeriod = null;
            if (departmentReportPeriodList.size() == 1) {
                departmentReportPeriod = departmentReportPeriodList.get(0);
            } else {
                throw new ServiceException("Не определен отчетный период подразделения");
            }

            Logger logger = new Logger();

            FormData formData;
            formData = formDataDao.find(formTemplateId.intValue(), FormDataKind.PRIMARY,
                    departmentReportPeriod.getId().intValue(),
                    periodOrder == null ? null : periodOrder,
                    null, false);

            if (formData == null) {
                long formDataId = formDataService.createFormData(logger,
                        userInfo,
                        formTemplateId,
                        departmentReportPeriod.getId(),
                        null,
                        false,
                        FormDataKind.PRIMARY,
                        periodOrder,
                        false);

                // Вызов скрипта
                formDataService.lock(formDataId, false, userInfo);
                try {
                    formDataService.migrationFormData(logger, userInfo, formDataId, inputStream, filename);

                    formDataService.saveFormData(logger, userInfo, formDataDao.get(formDataId, false));
                    // Принудительно выставлен статус "Принята"
                    formDataDao.updateState(formDataId, WorkflowState.ACCEPTED);
                } finally {
                    formDataService.unlock(formDataId, userInfo);
                }
            } else {
                isAlreadyCreated = true;
            }
        } catch (Exception e) {
            if (e instanceof ServiceLoggerException) {
                ServiceLoggerException sle = (ServiceLoggerException)e;
                LOG.error(ServiceLoggerException.getLogEntriesString(logEntryService.getAll(sle.getUuid())));
            }

            // Ошибка импорта
            LOG.error("Ошибка импорта файла " + filename + " : " + e.getClass() + " " + e.getMessage(), e);
            addLog(userInfo, departmentId, reportPeriodId, formTypeName, "Ошибка импорта файла " + filename + ": "
                        + e.getMessage());

            return;
        }

        if(isAlreadyCreated){
            // Форма уже была создана
            LOG.info("Уже был создан экземпляр формы с такими параметрами как в " + filename + " departmentId = " + departmentId + " reportPeriodId = "
					+ reportPeriodId);
            addLog(userInfo, departmentId, reportPeriodId, formTypeName, "Экзмепляр формы для файла " + filename + " уже существует. Импорт файла был пропущен.");

        } else {
            // Успешный импорт
            LOG.info("Успешно импортирован файл " + filename + " departmentId = " + departmentId + " reportPeriodId = "
					+ reportPeriodId + " formTypeName = " + formTypeName);
            addLog(userInfo, departmentId, reportPeriodId, formTypeName, "Успешно импортирован файл " + filename);
        }

    }

    /**
     * Запись в журнал аудита
     * @param userInfo
     * @param departmentId
     * @param reportPeriodId
     * @param formTypeName
     * @param msg
     */
    private void addLog(TAUserInfo userInfo, Integer departmentId, Integer reportPeriodId, String formTypeName,
                        String msg) {
        try {
            // Ошибка записи в журнал аудита не должна откатывать импорт
            auditService.add(FormDataEvent.MIGRATION, userInfo, departmentId, reportPeriodId, null, formTypeName,
                    FormDataKind.PRIMARY.getId(), msg, null, null);
        } catch (Exception e) {
            LOG.error("Ошибка записи в журнал аудита", e);
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
    public RestoreExemplar restoreExemplarFromRnu(String rnuFilename, byte[] fileContent) {
        RestoreExemplar exemplar = new RestoreExemplar();

        String firstRow;
        try {
            String str = new String(fileContent, CHARSET);
            firstRow = str.substring(0, str.indexOf('\r'));
            LOG.debug("firstRow: " + firstRow);
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
    private RestoreExemplar restoreExemplarFromXml(String xmlFilename) {
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
