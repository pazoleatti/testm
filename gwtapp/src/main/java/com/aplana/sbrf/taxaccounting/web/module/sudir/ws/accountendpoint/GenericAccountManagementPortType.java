package com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.exception.WSException;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.assembler.GenericAccountInfoAssembler;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.validation.ValidationService;

@WebService(endpointInterface="com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.GenericAccountManagement",
			targetNamespace="http://sberbank.ru/soa/service/sudir/itdi/smallsystem.generic.webservice.connector/1.0.0",
			serviceName="GenericAccountManagementService",
			portName="GenericAccountManagement",
			wsdlLocation="META-INF/wsdl/GenericAccountManagement.wsdl")
public class GenericAccountManagementPortType extends SpringBeanAutowiringSupport{
	
	@Autowired
	private TAUserService userService;
	
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
			userService.setUserIsActive(accountId, false);
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
			userService.updateUser(user);
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
			userService.setUserIsActive(accountId, true);
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
			return gais.desassembleUsers(userService.listAllUsers());
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
			listTAUsersByLogin.add(userService.getUser(accountId));
			listUsersByLogin.addAll(gais.desassembleUsers(listTAUsersByLogin));
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
