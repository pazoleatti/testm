package com.aplana.sbrf.taxaccounting.controller.form;

import java.io.IOException;
import java.util.List;

import javax.portlet.ResourceResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.dao.PredefinedRowsDao;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.util.json.DataRowDeserializer;
import com.aplana.sbrf.taxaccounting.util.json.DataRowSerializer;

@Controller
@RequestMapping("EDIT")
@SessionAttributes("formBean")
public class EditFormController {
	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private PredefinedRowsDao predefinedRowsDao;
	
	@Autowired
	private FormDao formDao;
	
	@RenderMapping
	protected String showEditForm(@ModelAttribute("formBean") EditFormBean formBean) throws JsonGenerationException, JsonMappingException, IOException {
		Form form = formBean.getForm();
		if (form == null) {
			return "emptySession";
		}
		return "form/edit";
	}
	
	@ResourceMapping("getForm")
	protected void getForm(@ModelAttribute("formBean") EditFormBean formBean, ResourceResponse response) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.getSerializationConfig().addMixInAnnotations(Column.class, ColumnMixIn.class);
		objectMapper.writeValue(response.getWriter(), formBean.getForm());
	}
	
	@ResourceMapping("getPredefinedRows")
	protected void getPredefinedRows(@ModelAttribute("formBean") EditFormBean formBean, ResourceResponse response) throws JsonGenerationException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("taxaccounting", new Version(1, 0, 0, null));
		module.addSerializer(DataRow.class, new DataRowSerializer());
		objectMapper.registerModule(module);
		objectMapper.writeValue(response.getWriter(), predefinedRowsDao.getPredefinedRows(formBean.getForm()));
	}
	
	@ResourceMapping("saveForm")
	protected void saveForm(
		@ModelAttribute("formBean") EditFormBean formBean, 
		@RequestParam("formData") String formJson,
		@RequestParam("predefinedRows") String predefinedRowsJson)
	throws JsonGenerationException, JsonMappingException, IOException {
		// TODO: кто-то накладывает HTML-escaping на передаваемые данные
		// найти точку пока не удалось, поэтому временное решение - принудительный Unescape
		formJson = StringEscapeUtils.unescapeHtml4(formJson);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.getDeserializationConfig().addMixInAnnotations(Column.class, ColumnMixIn.class);
		Form form = objectMapper.readValue(formJson, Form.class);
		int formId = formDao.saveForm(form);
		form = formDao.getForm(formId);
		formBean.setForm(form);
		
		ObjectMapper dataRowsMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("taxaccounting", new Version(1, 0, 0, null));
		module.addDeserializer(DataRow.class, new DataRowDeserializer(form, false));
		dataRowsMapper.registerModule(module);
		List<DataRow> predefinedRows = dataRowsMapper.readValue(predefinedRowsJson, new TypeReference<List<DataRow>>() {});
		predefinedRowsDao.savePredefinedRows(form, predefinedRows);
	}	
	
	@ModelAttribute("formBean")
	protected EditFormBean getFormBean() {
		return new EditFormBean();
	}	
	
	@ActionMapping("edit")
	protected void editForm(@RequestParam("formId") int formId, @ModelAttribute("formBean") EditFormBean formBean) {
		logger.info("Starting editing form: formId = " + formId);
		Form form = formDao.getForm(formId);
		formBean.setForm(form);
	}
}