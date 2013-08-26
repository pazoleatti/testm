package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.migration.RestoreExemplar;
import com.aplana.sbrf.taxaccounting.model.migration.enums.DepartmentRnuMapping;
import com.aplana.sbrf.taxaccounting.model.migration.enums.DepartmentXmlMapping;
import com.aplana.sbrf.taxaccounting.model.migration.enums.NalogFormType;
import com.aplana.sbrf.taxaccounting.model.migration.enums.PeriodMapping;
import com.aplana.sbrf.taxaccounting.model.migration.enums.YearCode;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.MappingService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
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
    private TAUserService taUserService;

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    private static String charSet = "UTF-8";
    private static String RNU_EXT = "rnu";
    private static String XML_EXT = "xml";

    @Override
    public void addFormData(String filename, byte[] fileContent) {

        InputStream inputStream = new ByteArrayInputStream(fileContent);

        RestoreExemplar restoreExemplar;

        String ext = filename.substring(filename.indexOf(".")).trim().toLowerCase();
        if (RNU_EXT.equals(ext)) {
            restoreExemplar = restoreExemplarFromRnu(filename, fileContent);
        } else if (XML_EXT.equals(ext)) {
            restoreExemplar = restoreExemplarFromXml(filename);
        } else {
            throw new ServiceException("Не правильное название файла");
        }

        log.debug(restoreExemplar);

        //TODO сделать правильную загрузку данных текущего пользователя
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(taUserService.getUser("admin"));
        userInfo.setIp("127.0.0.1");

        ReportPeriod reportPeriod = reportPeriodDao.getReportPeriodByTaxPeriodAndDict(
                restoreExemplar.getTaxPeriod(),
                restoreExemplar.getDictTaxPeriodId());

        Logger logger = new Logger();

        long formDataId = formDataService.createFormData(logger,
                userInfo,
                restoreExemplar.getFormTemplateId(),
                restoreExemplar.getDepartmentId(),
                FormDataKind.PRIMARY,
                reportPeriod);

        // вызов скрипта
        formDataService.importFormData(logger, userInfo, formDataId, restoreExemplar.getFormTemplateId(), restoreExemplar.getDepartmentId(),
                FormDataKind.PRIMARY, reportPeriod.getId(), inputStream, filename);
    }

    /**
     * Возвращает объект с необходимой информацией для создания формы в нвоой системе
     * Пример первой строки: 99|0013|01.01.2013|31.03.2013|640|90
     *
     * @param rnuFilename азвание файла с расширением rnu
     * @param fileContent содержимое файла
     * @return
     */
    private RestoreExemplar restoreExemplarFromRnu(String rnuFilename, byte[] fileContent) {
        RestoreExemplar exemplar = new RestoreExemplar();

        String firstRow = null;
        try {
            String str = new String(fileContent, charSet);
            firstRow = str.substring(0, str.indexOf('\r'));
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("Ошибка получения первой строки", e);
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
            if (NalogFormType.RNU31.getCodeNew() == exemplar.getFormTemplateId()) {
                exemplar.setDictTaxPeriodId(PeriodMapping.fromCode(periodCode).getDictTaxPeriodIdForMonthly());
            } else {
                exemplar.setDictTaxPeriodId(PeriodMapping.fromCode(periodCode).getDictTaxPeriodId());
            }

            return exemplar;
        } catch (Exception e) {
            throw new ServiceException("Parsing Error", e);
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
            if (NalogFormType.RNU31.getCodeNew() == exemplar.getFormTemplateId()) {
                exemplar.setDictTaxPeriodId(PeriodMapping.fromCodeXml(period).getDictTaxPeriodIdForMonthly());
            } else {
                exemplar.setDictTaxPeriodId(PeriodMapping.fromCodeXml(period).getDictTaxPeriodId());
            }

            return exemplar;
        } catch (Exception e) {
            throw new ServiceException("Parsing Error", e);
        }
    }
}
