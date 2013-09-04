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
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

@Service
@Transactional
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

    private boolean isAuditAddOn = true;
    private boolean isImportOn = true;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    private static String charSet = "UTF-8";
    private static String RNU_EXT = ".rnu";
    private static String XML_EXT = ".xml";
    private static String USER_APPENDER = "controlUnp";

    @Override
    public void addFormData(String filename, byte[] fileContent) {

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(taUserService.getUser(USER_APPENDER));
        //TODO (alivanov 3.09.13) подставить правильного пользователя для создания форм при операции импорта "старых" данных
        // Пользователя брать с некого конфигурационного файла (его пока нет)
        // Это будет специальный пользователь для операции миграции (импорта "старых" данных)
        // Сейчас же пока подставлен controlUnp
        userInfo.setIp("127.0.0.1");

        RestoreExemplar restoreExemplar;
        Integer departmentId = null;
        Integer formTypeId = null;
        ReportPeriod reportPeriod = null;
        Integer reportPeriodId = null;
        Integer formTemplateId = null;

        try {
            InputStream inputStream = new ByteArrayInputStream(fileContent);
            log.debug("addFormData from filename: " + filename);
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
            }

            log.debug(restoreExemplar);

            reportPeriodId = reportPeriodMappingDao.getByTaxPeriodAndDict(
                    restoreExemplar.getTaxPeriod(),
                    restoreExemplar.getDictTaxPeriodId());

            if (reportPeriodId != null) {
                reportPeriod = periodDao.get(reportPeriodId);
            }

            Logger logger = new Logger();

            long formDataId = formDataService.createFormData(logger,
                    userInfo,
                    formTemplateId,
                    departmentId,
                    FormDataKind.PRIMARY,
                    reportPeriod);

            log.debug("Form created! FormDataKind.PRIMARY, formDataId: " + formDataId + " formTemplateId: " + formTemplateId + " departmentId: " + departmentId + " period: " + reportPeriod.getName());

            // Добавляем месяц, если форма ежемесячная
            if (restoreExemplar.getPeriodOrder() != null) {
                formDataDao.updatePeriodOrder(formDataId, restoreExemplar.getPeriodOrder());
            }

            //Вызов скрипта
            if (isImportOn) {
                formDataService.importFormData(logger, userInfo, formDataId, formTemplateId,
                        departmentId, FormDataKind.PRIMARY, reportPeriodId, inputStream, filename);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            if (e instanceof ServiceLoggerException) {
                log.error(((ServiceLoggerException) e).getLogEntriesString());
            }

            // Ошибка импорта
            if (isAuditAddOn && isImportOn) {
                auditService.add(FormDataEvent.IMPORT, userInfo, departmentId, reportPeriodId, null, formTypeId,
                        FormDataKind.PRIMARY.getId(), "Ошибка импорта файла " + filename + " : " + e.getMessage());
            }
            return;
        }
        // Успешный импорт
        if (isAuditAddOn && isImportOn) {
            auditService.add(FormDataEvent.IMPORT, userInfo, departmentId, reportPeriodId, null, formTypeId,
                    FormDataKind.PRIMARY.getId(), "Успешно импортирован файл " + filename);
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
            String str = new String(fileContent, charSet);
            firstRow = str.substring(0, str.indexOf('\r'));
            log.debug("firstRow: " + firstRow);
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("Failure to get the file first line! " + e.getMessage(), e);
        }

        String[] params = firstRow.split("\\|");
        try {

            exemplar.setBeginDate(dateFormat.parse(params[2]));
            exemplar.setEndDate(dateFormat.parse(params[3]));

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
            Integer year = Integer.valueOf(yearFormat.format(exemplar.getBeginDate()));
            exemplar.setTaxPeriod(YearCode.fromYear(year).getTaxPeriodId());

            // по коду отчетного периода 7 символа в назавании файла DICT_TAX_PERIOD
            String periodCode = rnuFilename.substring(7, 8);
            exemplar.setDictTaxPeriodId(PeriodMapping.fromCode(periodCode).getDictTaxPeriodId());

            //Если форма ежемесячная, то заполняем месяц
            if (NalogFormType.RNU31.getCodeNew() == exemplar.getFormTemplateId()) {
                exemplar.setPeriodOrder(PeriodMapping.fromCode(periodCode).getDictTaxPeriodIdForMonthly());
            }

            return exemplar;
        } catch (Exception e) {
            throw new ServiceException("Ошибка разбора файла:" + e.getLocalizedMessage(), e);
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
            String nalogForm = xmlFilename.substring(0, 10).replace("_", "");
            exemplar.setFormTemplateId(NalogFormType.fromCodeNewXml(nalogForm).getCodeNew());

            String depCode = xmlFilename.substring(10, 19);
            String systemCode = xmlFilename.substring(19, 24).replace("_", "");
            String subSystemCode = xmlFilename.substring(24, 26);

            exemplar.setDepartmentId(DepartmentXmlMapping.getNewDepCode(depCode, Integer.valueOf(systemCode), subSystemCode));

            //по году определяем TAX_PERIOD
            String yearCut = xmlFilename.substring(29, 31);
            exemplar.setTaxPeriod(YearCode.fromYearCut(yearCut).getTaxPeriodId());

            String period = xmlFilename.substring(26, 29);
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

    @Override
    public boolean isAuditAddOn() {
        return isAuditAddOn;
    }

    @Override
    public boolean isImportOn() {
        return isImportOn;
    }

    @Override
    public void setProperties(boolean isAuditAddOn, boolean isImportOn){
        this.isAuditAddOn = isAuditAddOn;
        this.isImportOn = isImportOn;
    }

}
