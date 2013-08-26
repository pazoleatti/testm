package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.migration.enums.DepartmentRnuMapping;
import com.aplana.sbrf.taxaccounting.model.migration.enums.NalogFormType;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


@Service
@Transactional
public class XmlMappingServiceImpl implements XmlMappingService {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private ReportPeriodService reportPeriodService;

    @Autowired
    private TAUserService taUserService;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static String charSet = "UTF-8";

    @Override
    public void addFormDataFrom(String filename, byte[] fileContent) {

        InputStream inputStream = new ByteArrayInputStream(fileContent);
        String firstRow = getFirstRow(fileContent);

        int formTemplateId = getFormTemplateId(filename);
        int departmentId = getDepartamentId(filename, firstRow);
        long formDataId;

         //TODO сделать правильную загрузку данных текущего пользователя
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(taUserService.getUser("admin"));
        userInfo.setIp("127.0.0.1");

        //TODO сделать правильный поиск отчетного периода, нужны данные в базе
        ReportPeriod reportPeriod = new ReportPeriod();
        //reportPeriod = reportPeriodService.listByTaxPeriodAndDepartment();

        Logger logger = new Logger();
        formDataId = formDataService.createFormData(logger, userInfo, formTemplateId, departmentId, FormDataKind.PRIMARY, reportPeriod);

        // вызов скрипта
        formDataService.importFormData(logger, userInfo, formDataId, formTemplateId, departmentId,
                FormDataKind.PRIMARY, reportPeriod.getId(), inputStream, filename);
    }

    private Integer getFormTemplateId(String rnuFilename) {
        return NalogFormType.getNewCodeByNNN(rnuFilename.substring(0, 3));
    }

    /**
     * Возвращает номер месяца, если РНУ ежемесячное
     *
     * @param firstRow заголовок в файле
     * @return номер или null, если РНУ не ежемесячное
     */
    private Integer getMonth(String firstRow) {
        String[] params = firstRow.split("|");
        DateFormat format = new SimpleDateFormat("MM");
        try {
            Date start = dateFormat.parse(params[2]);
            Date end = dateFormat.parse(params[3]);
            String monthStart = format.format(start);
            String monthEnd = format.format(end);
            if (monthStart.equals(monthEnd))
                return Integer.valueOf(monthStart);
            else {
                return null;
            }
        } catch (Exception e) {
            throw new ServiceException("Ошибка при маппинге дат периодов");
        }
    }

    /**
     * Возвращает ид департамента новой системы
     */
    private Integer getDepartamentId(String filename, String firstRow) {
        String depCode = filename.substring(3, 6);
        String system = firstRow.split("|")[5].trim();
        if (system.length() == 1) {
            return DepartmentRnuMapping.getDepartmentId(depCode, system, null);
        } else if (system.length() == 3) {
            return DepartmentRnuMapping.getDepartmentId(depCode, system.substring(0, 1), system.substring(1, 2));
        } else {
            throw new ServiceException("Ошибка при маппинге кода подразделения");
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
