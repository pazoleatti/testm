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

	
	public void validate(GenericAccountInfo gai) throws GenericAccountManagementException_Exception{
        if(gai.getAttributes().getItem().size() <= 5){
            Map<String, FieldNames> copy = FieldNames.getFieldNamesMap();
            for (GenericAttribute ga : gai.getAttributes().getItem()) {
                if (copy.containsKey(ga.getName()))
                    copy.remove((ga.getName()));
            }
            if (copy.size() == 1 && !copy.containsKey(FieldNames.EMAIL.nameField()))
                throw new GenericAccountManagementException_Exception(
                        String.format("Не все обязательные атрибуты заполнены. Не заполнен атрибут: %s ", copy.keySet().toArray()[0]),
                        errorCreator(SudirErrorCodes.SUDIR_007));
        }
		for (GenericAttribute ga : gai.getAttributes().getItem()) {
			if(!FieldNames.containsName(ga.getName()))
				throw new GenericAccountManagementException_Exception("Передаваемая информация содержит лишние данные " + ga.getName(),
						errorCreator(SudirErrorCodes.SUDIR_010));

			if(FieldNames.getByName(ga.getName()) != FieldNames.ROLE_CODE && FieldNames.getByName(ga.getName()) != FieldNames.NAME){
				if(ga.getValues().getItem().size() > 1)
					throw new GenericAccountManagementException_Exception("Передаваемые однозначные атрибуты имеют множество значений.", errorCreator(SudirErrorCodes.SUDIR_009));
			}
			
			if(FieldNames.getByName(ga.getName()) == FieldNames.ROLE_CODE){
				if(ga.getValues().getItem().isEmpty())
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
