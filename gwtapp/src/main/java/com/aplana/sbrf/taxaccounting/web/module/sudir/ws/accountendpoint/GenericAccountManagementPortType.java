package com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.WSException;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.assembler.GenericAccountInfoAssembler;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.validation.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.List;

@WebService(endpointInterface="com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.GenericAccountManagement",
			targetNamespace="http://sberbank.ru/soa/service/sudir/itdi/smallsystem.generic.webservice.connector/1.0.0",
			serviceName="GenericAccountManagementService",
			portName="GenericAccountManagement",
			wsdlLocation="META-INF/wsdl/GenericAccountManagement.wsdl")
public class GenericAccountManagementPortType extends SpringBeanAutowiringSupport{

	@Autowired
	private TAUserService userService;

    @Autowired
    private AuditService auditService;
	
	private ValidationService validationService = new ValidationService();
	private GenericAccountInfoAssembler gais = new GenericAccountInfoAssembler();

	public void resetPassword(String accountId)
			throws GenericAccountManagementException_Exception {
		
		GenericAccountManagementException gam = new GenericAccountManagementException();
		gam.setGenericSudirStatusCode(WSException.SudirErrorCodes.SUDIR_002.toString());
		gam.setDetails(WSException.SudirErrorCodes.SUDIR_002.detailCode());
		throw new GenericAccountManagementException_Exception("Метод реализуется в системе без функциональности", gam);
	}

	public boolean validatePassword(String accountId, String password)
			throws GenericAccountManagementException_Exception {
		
		GenericAccountManagementException gam = new GenericAccountManagementException();
		gam.setGenericSudirStatusCode(WSException.SudirErrorCodes.SUDIR_002.toString());
		gam.setDetails(WSException.SudirErrorCodes.SUDIR_002.detailCode());
		throw new GenericAccountManagementException_Exception("Метод реализуется в системе без функциональности", gam);
		
	}

	public void setPassword(String accountId, String password)
			throws GenericAccountManagementException_Exception {
		
		GenericAccountManagementException gam = new GenericAccountManagementException();
		gam.setGenericSudirStatusCode(WSException.SudirErrorCodes.SUDIR_002.toString());
		gam.setDetails(WSException.SudirErrorCodes.SUDIR_002.detailCode());
		throw new GenericAccountManagementException_Exception("Метод реализуется в системе без функциональности", gam);
		
	}

	public void suspendAccount(String accountId)
			throws GenericAccountManagementException_Exception {
		try {
            validationService.validate(userService.getUser(accountId));
			userService.setUserIsActive(accountId, false);
            TAUserInfo userInfo = userService.getSystemUserInfo();
            auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, "Успешный обмен данными с вебсервисом СУДИР.", null);
		} catch (WSException e) {
			GenericAccountManagementException gam = new GenericAccountManagementException();
			gam.setGenericSudirStatusCode(e.getErrorCode().toString());
			gam.setDetails(e.getErrorCode().detailCode());
			throw new GenericAccountManagementException_Exception(e.toString(), gam);
		} catch (Exception e) {
			GenericAccountManagementException gam = new GenericAccountManagementException();
			gam.setGenericSudirStatusCode(WSException.SudirErrorCodes.SUDIR_001.toString());
			gam.setDetails(WSException.SudirErrorCodes.SUDIR_001.detailCode());
			throw new GenericAccountManagementException_Exception(e.toString(), gam);
		}
		
	}

	public void createAccount(GenericAccountInfo accountInfo)
			throws GenericAccountManagementException_Exception {
		validationService.validate(accountInfo);
		
		try {
			TAUser user = gais.assembleUser(accountInfo);
			userService.createUser(user);
            TAUserInfo userInfo = userService.getSystemUserInfo();
            auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, "Успешный обмен данными с вебсервисом СУДИР.", null);
		} catch (WSException e) {
			GenericAccountManagementException gam = new GenericAccountManagementException();
			gam.setGenericSudirStatusCode(e.getErrorCode().toString());
			gam.setDetails(e.getErrorCode().detailCode());
			throw new GenericAccountManagementException_Exception(e.toString(), gam);
		} catch (Exception e) {
			GenericAccountManagementException gam = new GenericAccountManagementException();
			gam.setGenericSudirStatusCode(WSException.SudirErrorCodes.SUDIR_001.toString());
			gam.setDetails(WSException.SudirErrorCodes.SUDIR_001.detailCode());
			throw new GenericAccountManagementException_Exception(e.toString(), gam);
		}
		
	}

	public void modifyAccount(GenericAccountInfo accountInfo)
			throws GenericAccountManagementException_Exception {
		validationService.validate(accountInfo);
		
		try {
			TAUser user = gais.assembleUser(accountInfo);
            validationService.validate(userService.getUser(user.getLogin()));
			userService.updateUser(user);
            TAUserInfo userInfo = userService.getSystemUserInfo();
            auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, "Успешный обмен данными с вебсервисом СУДИР.", null);
		} catch (WSException e) {
			GenericAccountManagementException gam = new GenericAccountManagementException();
			gam.setGenericSudirStatusCode(e.getErrorCode().toString());
			gam.setDetails(e.getErrorCode().detailCode());
			throw new GenericAccountManagementException_Exception(e.toString(), gam);
		} catch (Exception e) {
			GenericAccountManagementException gam = new GenericAccountManagementException();
			gam.setGenericSudirStatusCode(WSException.SudirErrorCodes.SUDIR_001.toString());
			gam.setDetails(WSException.SudirErrorCodes.SUDIR_001.detailCode());
			throw new GenericAccountManagementException_Exception(e.toString(), gam);
		}
		
	}

	public void restoreAccount(String accountId)
			throws GenericAccountManagementException_Exception {

        try {
            validationService.validate(userService.getUser(accountId));
			userService.setUserIsActive(accountId, true);
            TAUserInfo userInfo = userService.getSystemUserInfo();
            auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, "Успешный обмен данными с вебсервисом СУДИР.", null);
		} catch (WSException e) {
			GenericAccountManagementException gam = new GenericAccountManagementException();
			gam.setGenericSudirStatusCode(e.getErrorCode().toString());
			gam.setDetails(e.getErrorCode().detailCode());
			throw new GenericAccountManagementException_Exception(e.toString(), gam);
		} catch (Exception e) {
			GenericAccountManagementException gam = new GenericAccountManagementException();
			gam.setGenericSudirStatusCode(WSException.SudirErrorCodes.SUDIR_001.toString());
			gam.setDetails(WSException.SudirErrorCodes.SUDIR_001.detailCode());
			throw new GenericAccountManagementException_Exception(e.toString(), gam);
		}
		
	}

	public void deleteAccount(String accountId)
			throws GenericAccountManagementException_Exception {
		GenericAccountManagementException gam = new GenericAccountManagementException();
		gam.setGenericSudirStatusCode(WSException.SudirErrorCodes.SUDIR_002.toString());
		gam.setDetails(WSException.SudirErrorCodes.SUDIR_002.detailCode());
		throw new GenericAccountManagementException_Exception("Метод реализуется в системе без функциональности", gam);
	}

	public List<GenericAccountInfo> getAccountList()
			throws GenericAccountManagementException_Exception {
		try {
            List<GenericAccountInfo> accountInfos = gais.desassembleUsers(userService.listAllUsers());
            TAUserInfo userInfo = userService.getSystemUserInfo();
            auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(), null, null, null, null,
                    "Успешный обмен данными с вебсервисом СУДИР.", null);
			return accountInfos;
		} catch (Exception e) {
			GenericAccountManagementException gam = new GenericAccountManagementException();
			gam.setGenericSudirStatusCode(WSException.SudirErrorCodes.SUDIR_001.toString());
			gam.setDetails(WSException.SudirErrorCodes.SUDIR_001.detailCode());
			throw new GenericAccountManagementException_Exception(e.toString(), gam);
		}
		
	}

	public List<GenericAccountInfo> getAccountListById(String accountId)
			throws GenericAccountManagementException_Exception {
		List<GenericAccountInfo> listUsersByLogin = new ArrayList<GenericAccountInfo>();
		try {
			List<TAUser> listTAUsersByLogin = new ArrayList<TAUser>();
			TAUser user = userService.getUser(accountId.toLowerCase());
			validationService.validate(user);
			listTAUsersByLogin.add(user);
			listUsersByLogin.addAll(gais.desassembleUsers(listTAUsersByLogin));
            TAUserInfo userInfo = userService.getSystemUserInfo();
            auditService.add(FormDataEvent.EXTERNAL_INTERACTION, userInfo, userInfo.getUser().getDepartmentId(),
                    null, null, null, null, "Успешный обмен данными с вебсервисом СУДИР.", null);
		} catch (WSException e) {
			GenericAccountManagementException gam = new GenericAccountManagementException();
			gam.setGenericSudirStatusCode(e.getErrorCode().toString());
			gam.setDetails(e.getErrorCode().detailCode());
			throw new GenericAccountManagementException_Exception(e.toString(), gam);
		} catch (Exception e) {
			GenericAccountManagementException gam = new GenericAccountManagementException();
			gam.setGenericSudirStatusCode(WSException.SudirErrorCodes.SUDIR_001.toString());
			gam.setDetails(WSException.SudirErrorCodes.SUDIR_001.detailCode());
			throw new GenericAccountManagementException_Exception(e.toString(), gam);
		}
		
		return listUsersByLogin;
	}

}
