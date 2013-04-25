package com.aplana.sbrf.taxaccounting.web.module.sudir.validation;

import java.util.Map;

import com.aplana.sbrf.taxaccounting.web.module.sudir.assembler.FieldNames;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.GenericAccountInfo;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.GenericAccountManagementException;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.GenericAccountManagementException_Exception;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.GenericAttribute;

public class ValidationService {
	
public static Map<String, Integer> fieldNames= FieldNames.getFieldNamesMap();
	
	public void validate(GenericAccountInfo gai) throws GenericAccountManagementException_Exception{
		if(gai.getAttributes().getItem().size() < 6){
			throw new GenericAccountManagementException_Exception("Не все обязательные атрибуты заполнены. " +
					"Должно быть " + fieldNames.size(), errorCreator(SudirErrorCodes.SUDIR_007));
		}
		for (GenericAttribute ga : gai.getAttributes().getItem()) {
			if(fieldNames.get(ga.getName()) == null)
				throw new GenericAccountManagementException_Exception("", errorCreator(SudirErrorCodes.SUDIR_010));
			
			if(fieldNames.get(ga.getName()) == 2){
				if(ga.getValues().getItem().size() != 1)
					throw new GenericAccountManagementException_Exception("", errorCreator(SudirErrorCodes.SUDIR_009));
			}
			
			if(fieldNames.get(ga.getName()) == 5){
				if(ga.getValues().getItem().size() == 0)
					throw new GenericAccountManagementException_Exception("Не заполнены роли пользователя. ", errorCreator(SudirErrorCodes.SUDIR_007));
			}
				
		}
	}
	
	private GenericAccountManagementException errorCreator(SudirErrorCodes codeErr){
		GenericAccountManagementException gam = new GenericAccountManagementException();
		gam.setGenericSudirStatusCode(codeErr.toString());
		gam.setDetails(codeErr.detailCode());
		
		return gam;
	}

}
