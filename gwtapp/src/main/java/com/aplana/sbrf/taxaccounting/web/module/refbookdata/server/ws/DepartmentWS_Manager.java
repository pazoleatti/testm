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
import com.aplana.sbrf.taxaccounting.web.service.PropertyLoader;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import java.net.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;
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

    private DepartmentWS getDepartmentWSPort() throws Exception {
        int timeout = 0;
        int rootDepartmentId = departmentService.getBankDepartment().getId();
        ConfigurationParamModel configurationParamModel = configurationService.getAllConfig(userService.getSystemUserInfo());
        List<String> sunrAddressParams = configurationParamModel.get(ConfigurationParam.WSDL_ADDRESS_DEPARTMENT_WS_SUNR, rootDepartmentId);
        if (sunrAddressParams == null) {
            throw new ServiceException("Не заполнен конфигурационный параметр \"" + ConfigurationParam.WSDL_ADDRESS_DEPARTMENT_WS_SUNR.getCaption() + '"');
        }
        List<String> sunrDepartmentWSTimeoutParams = configurationParamModel.get(ConfigurationParam.TIMEOUT_SUNR, rootDepartmentId);
        if (sunrDepartmentWSTimeoutParams != null) {
            try{
                timeout = Integer.parseInt(sunrDepartmentWSTimeoutParams.get(0));
            } catch (NumberFormatException ignored) {}
        }

        //проверка доступности wsdl
        URLConnection uc = (new URL(sunrAddressParams.get(0))).openConnection();
        uc.setConnectTimeout(10000);
        uc.setReadTimeout(10000);
        uc.getContent();

        DepartmentWS_Service departmentWS_Service = new DepartmentWS_Service(sunrAddressParams.get(0));
        DepartmentWS departmentWS = departmentWS_Service.getDepartmentWSPort();

        Map<String, Object> requestContext = ((BindingProvider) departmentWS).getRequestContext();
        if (PropertyLoader.isProductionMode()) {
            requestContext.put("timeout", String.valueOf(timeout)); // тайм-аут в сек
        } else {
            requestContext.put("com.sun.xml.internal.ws.request.timeout", timeout*1000); // тайм-аут в мсек
        }
        return departmentWS;
    }

    @Transactional
    public void sendChange(DepartmentChangeOperationType operationType, int depId, Logger logger) {
        DepartmentChange departmentChange = createChange(operationType, depId);
        departmentChangeService.addChange(departmentChange);
        sendChanges(null);
    }

    @Transactional
    public void sendChanges(TaxDepartmentChanges taxDepartmentChanges) {
        TAUserInfo userInfo = userService.getSystemUserInfo();
        try {
            List<DepartmentChange> departmentChanges = departmentChangeService.getAllChanges();
            if (departmentChanges.size() > 0) {
                DepartmentWS departmentWS = getDepartmentWSPort();
                TaxDepartmentChangeStatus status = departmentWS.sendDepartmentChange(convert(departmentChanges));
                if (status.getErrorCode().equalsIgnoreCase("E0")) {
                    departmentChangeService.clean();
                    if (taxDepartmentChanges == null) {
                        String msg = "Изменения подразделении успешно переданы в АС СУНР";
                        auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                                null, null, null, null, msg, null);
                    }
                } else {
                    if (taxDepartmentChanges != null) {
                        taxDepartmentChanges.setErrorCode("E3");
                        taxDepartmentChanges.setErrorText(String.format("Получен код ошибки \"%s\" при обработке изменении подразделении в АС СУНР.", status.getErrorCode()));
                    }
                    String msg = String.format("Изменения подразделении не были отпралено в АС СУНР. Код ошибки: %s, текст ошибки: %s.", status.getErrorCode(), status.getErrorText());
                    auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                            null, null, null, null, msg, null);
                }
            }
        } catch (Exception e) {
            LOG.error("Произошла непредвиденная ошибка при отправке сообщения в АС СУНР", e);
            String msg;
            String errorCode;
            if (ExceptionUtils.indexOfThrowable(e, SocketTimeoutException.class) != -1) {
                msg = "Возникла ошибка при отправке изменении подразделении в АС СУНР. Текст ошибки: «Время ожидания ответа истекло»";
                errorCode = "E4";
            } else if (ExceptionUtils.indexOfThrowable(e, ConnectException.class) != -1) {
                msg = "Возникла ошибка при отправке изменении подразделении в АС СУНР. Текст ошибки: «Произошла ошибка при попытке соединения с веб-сервисом»";
                errorCode = "E5";
            } else {
                msg = "Произошла непредвиденная ошибка при отправке сообщения в АС СУНР. Текст ошибки: «Неизвестная техническая ошибка»";
                errorCode = "E5";
            }
            if (taxDepartmentChanges != null) {
                taxDepartmentChanges.setErrorCode("E5");
                taxDepartmentChanges.setErrorText(errorCode);
            } else {
                auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                        null, null, null, null, msg, null);
            }
        }
    }

    private DepartmentChange createChange(DepartmentChangeOperationType operationType, int depId) {
        DepartmentChange departmentChange = new DepartmentChange();
        departmentChange.setDepartmentId(depId);
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

    private List<TaxDepartmentChange> convert(List<DepartmentChange> departmentChangeList) throws DatatypeConfigurationException {
        List<TaxDepartmentChange> taxDepartmentChangeList = new ArrayList<TaxDepartmentChange>();
        for(DepartmentChange departmentChange: departmentChangeList) {
            TaxDepartmentChange taxDepartmentChange = new TaxDepartmentChange();
            taxDepartmentChange.setOperationType(departmentChange.getOperationType().getCode());
            taxDepartmentChange.setId(departmentChange.getDepartmentId());
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(departmentChange.getLogDate());
            XMLGregorianCalendar changeDatetime = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
            taxDepartmentChange.setChangeDatetime(changeDatetime);
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

    public boolean checkServiceAvailable(String address) {
        try {
            DepartmentWS_Service departmentWS_Service = new DepartmentWS_Service(address);
            DepartmentWS departmentWS = departmentWS_Service.getDepartmentWSPort();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
