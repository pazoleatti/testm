package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server.ws;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DepartmentChangeService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentmsendpoint.TaxDepartmentChanges;
import com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentws.*;
import com.aplana.sbrf.taxaccounting.web.module.department.ws.departmentws.TaxDepartmentChange;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.ws.BindingProvider;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author lhaziev
 */
@Component
public class DepartmentWS_Manager {

    private static final Log LOG = LogFactory.getLog(DepartmentWS_Manager.class);

    @Autowired
    private DepartmentChangeService departmentChangeService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private RefBookFactory refBookFactory;

    @Autowired
    private AuditService auditService;

    @Autowired
    private TAUserService userService;

    @Autowired
    private ConfigurationService configurationService;

    private DepartmentWS getDepartmentWSPort() throws MalformedURLException {
        int timeout = 0;
        int rootDepartmentId = departmentService.getBankDepartment().getId();
        ConfigurationParamModel configurationParamModel = configurationService.getAllConfig(userService.getSystemUserInfo());
        List<String> sunrAddressParams = configurationParamModel.get(ConfigurationParam.ADDRESS_SUNR, rootDepartmentId);
        if (sunrAddressParams == null) {
            throw new ServiceException("Не заполнен конфигурационный параметр \"" + ConfigurationParam.ADDRESS_SUNR.getCaption() + '"');
        }
        List<String> sunrDepartmentWSTimeoutParams = configurationParamModel.get(ConfigurationParam.TIMEOUT_SUNR, rootDepartmentId);
        if (sunrDepartmentWSTimeoutParams != null) {
            try{
                timeout = Integer.parseInt(sunrDepartmentWSTimeoutParams.get(0));
            } catch (NumberFormatException ignored) {}
        }

        DepartmentWS_Service departmentWS_Service = new DepartmentWS_Service(sunrAddressParams.get(0)+"DepartmentWS?wsdl");
        DepartmentWS departmentWS = departmentWS_Service.getDepartmentWSPort();

        Map<String, Object> requestContext = ((BindingProvider) departmentWS).getRequestContext();
        requestContext.put("com.sun.xml.internal.ws.request.timeout", timeout); // Timeout in millis
        return departmentWS;
    }

    @Transactional
    public void sendChange(DepartmentChangeOperationType operationType, int depId, Logger logger) {
        DepartmentChange departmentChange = createChange(operationType, depId);
        departmentChangeService.addChange(departmentChange);
        sendChanges(null, logger);
    }

    @Transactional
    public void sendChanges(TaxDepartmentChanges taxDepartmentChanges, Logger logger) {
        TAUserInfo userInfo = userService.getSystemUserInfo();
        try {
            List<DepartmentChange> departmentChanges = departmentChangeService.getAllChanges();
            if (departmentChanges.size() > 0) {
                DepartmentWS departmentWS = getDepartmentWSPort();
                TaxDepartmentChangeStatus status = departmentWS.sendDepartmentChange(convert(departmentChanges));
                if (status.getErrorCode().equalsIgnoreCase("E0")) {
                    departmentChangeService.clean();
                    String msg = "Изменения подразделении успешно переданы в АС СУНР";
                    logger.info(msg);
                    auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                            null, null, null, null, msg, null);
                } else {
                    String msg = String.format("Изменения подразделении не были отпралено в АС СУНР. Код ошибки: %s, текст ошибки: %s.", status.getErrorCode(), status.getErrorText());
                    logger.warn(msg);
                    auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                            null, null, null, null, msg, null);
                    if (taxDepartmentChanges != null) {
                        taxDepartmentChanges.setErrorCode("E3");
                        taxDepartmentChanges.setErrorText(String.format("Получен код ошибки \"%s\" при обработке изменении подразделении в АС СУНР.", status.getErrorCode()));
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Произошла непредвиденная ошибка при отправке сообщения в АС СУНР", e);
            String msg;
            if (ExceptionUtils.indexOfThrowable(e, SocketTimeoutException.class) != -1) {
                msg = "Возникла ошибка при отправке изменении подразделении в АС СУНР. Текст ошибки: «Время ожидания ответа истекло»";
            } else {
                msg = "Произошла непредвиденная ошибка при отправке сообщения в АС СУНР. Текст ошибки: "+ e.getLocalizedMessage();
            }
            if (taxDepartmentChanges != null) {
                taxDepartmentChanges.setErrorCode("E5");
                taxDepartmentChanges.setErrorText(msg);
            }
            logger.warn(msg);
            auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, msg, null);
        }
    }

    private DepartmentChange createChange(DepartmentChangeOperationType operationType, int depId) {
        DepartmentChange departmentChange = new DepartmentChange();
        departmentChange.setId(depId);
        departmentChange.setOperationType(operationType);
        if (operationType != DepartmentChangeOperationType.DELETE) {
            Department department = departmentService.getDepartment(depId);
            departmentChange.setParentId(department.getParentId());
            departmentChange.setLevel(departmentService.getHierarchyLevel(depId));
            departmentChange.setName(department.getName());
            departmentChange.setShortName(department.getShortName());
            departmentChange.setParentId(department.getParentId());
            departmentChange.setType(department.getType());
            departmentChange.setTbIndex(department.getTbIndex());
            departmentChange.setSbrfCode(department.getSbrfCode());

            if (department.getRegionId() != null) {
                RefBookDataProvider dataProvider = refBookFactory.getDataProvider(4L);
                Map<String, RefBookValue> refBookValueMap = dataProvider.getRecordData(department.getRegionId());
                departmentChange.setRegion(refBookValueMap.get("NAME").getStringValue());
            } else {
                departmentChange.setRegion(null);
            }
            departmentChange.setIsActive(department.isActive());
            departmentChange.setCode(department.getCode());
            departmentChange.setGarantUse(department.isGarantUse());
            departmentChange.setSunrUse(department.isSunrUse());
        }
        return departmentChange;
    }

    private List<TaxDepartmentChange> convert(List<DepartmentChange> departmentChangeList) {
        List<TaxDepartmentChange> taxDepartmentChangeList = new ArrayList<TaxDepartmentChange>();
        for(DepartmentChange departmentChange: departmentChangeList) {
            TaxDepartmentChange taxDepartmentChange = new TaxDepartmentChange();
            taxDepartmentChange.setOperationType(departmentChange.getOperationType().getCode());
            taxDepartmentChange.setId(departmentChange.getId());
            taxDepartmentChange.setChange_datetime(departmentChange.getLogDate());
            if (departmentChange.getOperationType() != DepartmentChangeOperationType.DELETE) {
                taxDepartmentChange.setLevel(departmentChange.getLevel());
                taxDepartmentChange.setName(departmentChange.getName());
                taxDepartmentChange.setShortName(departmentChange.getShortName());
                taxDepartmentChange.setParentId(departmentChange.getParentId());
                taxDepartmentChange.setType(departmentChange.getType().getCode());
                taxDepartmentChange.setTbIndex(departmentChange.getTbIndex());
                taxDepartmentChange.setSbrfCode(departmentChange.getSbrfCode());
                taxDepartmentChange.setRegion(departmentChange.getRegion());
                taxDepartmentChange.setIsActive(departmentChange.getIsActive());
                taxDepartmentChange.setCode(departmentChange.getCode());
                taxDepartmentChange.setGarantUse(departmentChange.getGarantUse());
                taxDepartmentChange.setSunrUse(departmentChange.getSunrUse());
            }
            taxDepartmentChangeList.add(taxDepartmentChange);
        }
        return taxDepartmentChangeList;
    }

}
