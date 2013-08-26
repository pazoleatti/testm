package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.migration.RestoreExemplar;
import com.aplana.sbrf.taxaccounting.model.migration.enums.DepartmentRnuMapping;
import com.aplana.sbrf.taxaccounting.model.migration.enums.NalogFormType;
import com.aplana.sbrf.taxaccounting.model.migration.enums.PeriodRnuMapping;
import com.aplana.sbrf.taxaccounting.model.migration.enums.YearCode;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.RnuMappingService;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;


@Service
@Transactional
public class RnuMappingServiceImpl implements RnuMappingService {

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

    @Override
    public void addFormDataFromRnuFile(String filename, byte[] fileContent) {

        InputStream inputStream = new ByteArrayInputStream(fileContent);
        RestoreExemplar restoreExemplar = restoreExemplar(filename, fileContent);

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

    private RestoreExemplar restoreExemplar(String rnuFilename, byte[] fileContent) {
        RestoreExemplar exemplar = new RestoreExemplar();

        String firstRow = getFirstRow(fileContent);
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
                exemplar.setDictTaxPeriodId(PeriodRnuMapping.fromCode(periodCode).getDictTaxPeriodIdForMonthly());
            } else {
                exemplar.setDictTaxPeriodId(PeriodRnuMapping.fromCode(periodCode).getDictTaxPeriodId());
            }

            return exemplar;
        } catch (Exception e) {
            throw new ServiceException("Parsing Error", e);
        }
    }

    /**
     * Возвращает заголовок файла (первая строчка в файле)
     * Пример: 99|0013|01.01.2013|31.03.2013|640|901
     *
     * @param rnuFileContent массив байтов содержимого файла
     * @return параметры в виде строки
     */
    private String getFirstRow(byte[] rnuFileContent) {
        String firestRow = null;
        try {
            String str = new String(rnuFileContent, charSet);
            firestRow = str.substring(0, str.indexOf('\r'));
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("Ошибка получения первой строки", e);
        }
        return firestRow;
    }
}
