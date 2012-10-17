package com.aplana.sbrf.taxaccounting.gwtapp.server;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.gwtapp.shared.RecordList;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.util.json.ColumnMixIn;
import com.aplana.sbrf.taxaccounting.util.json.DataRowDeserializer;
import com.aplana.sbrf.taxaccounting.util.json.DataRowSerializer;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
public class GetFormDataListHandler extends AbstractActionHandler<GetFormDataList, RecordList<String>> {
	@Autowired
	private FormDataDao formDataDao;

	public GetFormDataListHandler() {
		super(GetFormDataList.class);
	}
	
	@Override
	public RecordList<String> execute(GetFormDataList action,	ExecutionContext context) throws ActionException {
		List<FormData> formDataList = formDataDao.getAll();
		List<String> res = new ArrayList<String>(formDataList.size());
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
		}
		
		return new RecordList<String>(res);
	}

	@Override
	public void undo(GetFormDataList action, RecordList<String> result, ExecutionContext context) throws ActionException {
		// ничего не делаем
	}
	

}
