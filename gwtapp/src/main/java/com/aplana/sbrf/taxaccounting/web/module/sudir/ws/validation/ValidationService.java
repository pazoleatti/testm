package com.aplana.sbrf.taxaccounting.web.module.sudir.ws.validation;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.exception.WSException.SudirErrorCodes;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.GenericAccountInfo;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.GenericAccountManagementException;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.GenericAccountManagementException_Exception;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.accountendpoint.GenericAttribute;
import com.aplana.sbrf.taxaccounting.web.module.sudir.ws.assembler.FieldNames;

import java.util.Map;

public class ValidationService {
	
public static Map<String, Integer> fieldNames = FieldNames.getFieldNamesMap();
	
	public void validate(GenericAccountInfo gai) throws GenericAccountManagementException_Exception{
		for (GenericAttribute ga : gai.getAttributes().getItem()) {
			if(fieldNames.get(ga.getName()) == null)
				throw new GenericAccountManagementException_Exception("Передаваемая информация содержит лишние данные" + ga.getName(),
						errorCreator(SudirErrorCodes.SUDIR_010));
			
			if(fieldNames.get(ga.getName()) == 2){
				if(ga.getValues().getItem().size() != 1)
					throw new GenericAccountManagementException_Exception("Должен содержаться только один аттрибут.", errorCreator(SudirErrorCodes.SUDIR_009));
			}
			
			if(fieldNames.get(ga.getName()) == 5){
				if(ga.getValues().getItem().size() == 0)
					throw new GenericAccountManagementException_Exception("Не заполнены роли пользователя. ", errorCreator(SudirErrorCodes.SUDIR_007));
			}
				
		}
	}

    public void validate(TAUser user) throws GenericAccountManagementException_Exception {
        if (user.getId() < 1)
            throw new GenericAccountManagementException_Exception("Ошибка при получении пользователя по логину " + user.getLogin(),
                    errorCreator(SudirErrorCodes.SUDIR_004));
    }
	
	private GenericAccountManagementException errorCreator(SudirErrorCodes codeErr){
		GenericAccountManagementException gam = new GenericAccountManagementException();
		gam.setGenericSudirStatusCode(codeErr.toString());
		gam.setDetails(codeErr.detailCode());
		
		return gam;
	}

}
