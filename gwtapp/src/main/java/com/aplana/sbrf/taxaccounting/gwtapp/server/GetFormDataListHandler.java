package com.aplana.sbrf.taxaccounting.gwtapp.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.GetFormDataListResult;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
public class GetFormDataListHandler extends AbstractActionHandler<GetFormDataList, GetFormDataListResult> {
	@Autowired
	private FormDataDao formDataDao;

	public GetFormDataListHandler() {
		super(GetFormDataList.class);
	}
	
	@Override
	public GetFormDataListResult execute(GetFormDataList action,	ExecutionContext context) throws ActionException {
		List<FormData> formDataList = formDataDao.getAll();
		/*
		List<String> resList = new ArrayList<String>(formDataList.size());		
		for (FormData fd: formDataList) {

			ObjectMapper objectMapper = new ObjectMapper();
			SimpleModule module = new SimpleModule("taxaccounting", new Version(1, 0, 0, null));
			module.addSerializer(DataRow.class, new DataRowSerializer(FormatUtils.getIsoDateFormat()));
			module.addDeserializer(DataRow.class, new DataRowDeserializer(fd.getForm(), FormatUtils.getIsoDateFormat(), true));
			objectMapper.registerModule(module);
			objectMapper.getSerializationConfig().addMixInAnnotations(Column.class, ColumnMixIn.class);
			try {
				res.add(objectMapper.writeValueAsString(fd));
			} catch (Exception e) {
				throw new ActionException(e);
			}
			resList.add("1111");
		}
		*/
		GetFormDataListResult res = new GetFormDataListResult();
		res.setRecords(formDataList);
		System.out.println("--> Handler: result list size is " + res.getRecords().size());
		return res;
	}

	@Override
	public void undo(GetFormDataList action, GetFormDataListResult result, ExecutionContext context) throws ActionException {
		// ничего не делаем
	}
	

}
