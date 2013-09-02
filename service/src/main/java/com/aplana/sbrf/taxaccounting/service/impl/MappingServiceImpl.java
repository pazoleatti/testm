package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
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
    FormTemplateService formTemplateService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private TAUserService taUserService;

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    private static String charSet = "UTF-8";
    private static String RNU_EXT = ".rnu";
    private static String XML_EXT = ".xml";

    @Override
    public void addFormData(String filename, byte[] fileContent) {
        //TODO сделать правильную загрузку данных текущего пользователя
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(taUserService.getUser("admin"));
        userInfo.setIp("127.0.0.1");

        RestoreExemplar restoreExemplar;
        Integer departmentId = null;
        Integer formTypeId = null;
        Integer reportPeriodId = null;

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
                FormTemplate template = formTemplateService.get(restoreExemplar.getFormTemplateId());
                formTypeId = template.getType().getId();
                departmentId = restoreExemplar.getDepartmentId();
            }

            log.debug(restoreExemplar);

            ReportPeriod reportPeriod = reportPeriodDao.getByTaxPeriodAndDict(
                    restoreExemplar.getTaxPeriod(),
                    restoreExemplar.getDictTaxPeriodId());

            if (reportPeriod != null) {
                reportPeriodId = reportPeriod.getId();
            }

            Logger logger = new Logger();

            long formDataId = formDataService.createFormData(logger,
                    userInfo,
                    restoreExemplar.getFormTemplateId(),
                    restoreExemplar.getDepartmentId(),
                    FormDataKind.PRIMARY,
                    reportPeriod);

            // Вызов скрипта
            formDataService.importFormData(logger, userInfo, formDataId, restoreExemplar.getFormTemplateId(),
                    restoreExemplar.getDepartmentId(), FormDataKind.PRIMARY, reportPeriod.getId(), inputStream,
                    filename);
        } catch (Exception e) {
            // Ошибка импорта
            auditService.add(FormDataEvent.IMPORT, userInfo, departmentId, reportPeriodId, null, formTypeId,
                    FormDataKind.PRIMARY.getId(), "Ошибка импорта файла " + filename + " : " + e.getMessage());

            return;
        }
        // Успешный импорт
        auditService.add(FormDataEvent.IMPORT, userInfo, departmentId, reportPeriodId, null, formTypeId,
                FormDataKind.PRIMARY.getId(), "Успешно импортирован файл " + filename);
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
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("Ошибка получения первой строки файла", e);
        }

        String[] params = firstRow.split("|");
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
                exemplar.setDepartmentId(DepartmentRnuMapping.getDepartmentId(depCode, system.substring(0, 1), system.substring(1, 2)));
            } else {
                throw new ServiceException("Ошибка при маппинге кода подразделения");
            }

            //по году определяем TAX_PERIOD
            Integer year = Integer.valueOf(yearFormat.format(params[2]));
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
            throw new ServiceException("Ошибка разбора файла", e);
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
            throw new ServiceException("Ошибка разбора файла", e);
        }
    }
}
