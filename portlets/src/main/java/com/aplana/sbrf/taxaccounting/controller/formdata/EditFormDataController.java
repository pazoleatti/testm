package com.aplana.sbrf.taxaccounting.controller.formdata;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.portlet.ResourceResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.log.LogLevel;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.util.DojoFileStoreData;
import com.aplana.sbrf.taxaccounting.util.json.DataRowDeserializer;
import com.aplana.sbrf.taxaccounting.util.json.DataRowSerializer;

@Controller
@SessionAttributes("formBean")
@RequestMapping("EDIT")
public class EditFormDataController {
	private Log logger = LogFactory.getLog(getClass());
	
	private static class DataRowFileStoreData extends DojoFileStoreData<DataRow> {};
	
	@Autowired
	private FormDataDao formDataDao;
	@Autowired
	private FormDataScriptingService formDataService;
	
	@ModelAttribute("formBean")
	protected EditFormDataBean getFormBean() {
		return new EditFormDataBean();
	}
	
	@ActionMapping("new")
	public void processNew(@RequestParam("formId") int formId, @ModelAttribute("formBean") EditFormDataBean formBean) {
		logger.info("Creating new form data, formId = " + formId);
		formBean.getLogger().clear();
		FormData formData = formDataService.createForm(formBean.getLogger(), formId);
		formBean.setFormData(formData);
	}
	
	@ActionMapping("view")
	public void processView(@RequestParam("id") long formDataId, @ModelAttribute("formBean") EditFormDataBean formBean) {
		logger.info("Opening form data for view, formDataId = " + formDataId);
		formBean.getLogger().clear();
		formBean.setFormData(formDataDao.get(formDataId));
	}
	
	@RenderMapping
	public ModelAndView showEdit(@ModelAttribute("formBean") EditFormDataBean formBean) throws JsonGenerationException, JsonMappingException, IOException {
		if (formBean == null || formBean.getFormData() == null) {
			return new ModelAndView("emptySession");
		}
		ModelAndView result = new ModelAndView("formData/edit");
		Form form = formBean.getForm();
		List<Column> columns = form.getColumns();
		String gridLayout = getObjectMapper(form).writeValueAsString(columns);
		result.addObject("gridLayout", gridLayout);
		return result;
	}
	
	@ResourceMapping("dataRows")
	public void getDataRows(@ModelAttribute("formBean") EditFormDataBean formBean, ResourceResponse response) throws JsonGenerationException, JsonMappingException, IOException {
		FormData formData = formBean.getFormData();
		List<DataRow> dataRows = formData.getDataRows();
		DataRowFileStoreData data = new DataRowFileStoreData();
		data.setIdentifier("alias");
		data.setItems(dataRows);
		response.setContentType("application/json");
		getObjectMapper(formData.getForm()).writeValue(response.getPortletOutputStream(), data);
	}
	
	@ResourceMapping("log")
	public void getLogEntries(@ModelAttribute("formBean") EditFormDataBean formBean, ResourceResponse response) throws JsonGenerationException, JsonMappingException, IOException {
		Form form = formBean.getFormData().getForm();
		response.setContentType("application/json");
		getObjectMapper(form).writeValue(response.getPortletOutputStream(), formBean.getLogger().getEntries());
	}
	
	@ResourceMapping("saveRows")
	public void saveDataRows(
			@ModelAttribute("formBean") EditFormDataBean formBean, 
			@RequestParam("data") String json,
			ResourceResponse response
	) throws JsonProcessingException, UnsupportedEncodingException, IOException {
		FormData formData = formBean.getFormData();
		ObjectMapper objectMapper = getObjectMapper(formData.getForm());
		DataRowFileStoreData data = objectMapper.readValue(json, DataRowFileStoreData.class);
		formData.getDataRows().clear();
		formData.getDataRows().addAll(data.getItems());
		
		Logger log = formBean.getLogger();
		log.clear();
		formDataService.processFormData(log, formData);
		if (!log.containsLevel(LogLevel.ERROR)) {
			long formDataId = formDataDao.save(formData);
			log.info("Данные успешно записаны, идентификтор: %d", formDataId);
		} else {
			log.warn("Данные формы не сохранены, так как обнаружены ошибки");
		}
		objectMapper.writeValue(response.getWriter(), log.getEntries());
	}
	
	private ObjectMapper getObjectMapper(Form form) {
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("taxaccounting", new Version(1, 0, 0, null));
		module.addSerializer(DataRow.class, new DataRowSerializer());
		module.addDeserializer(DataRow.class, new DataRowDeserializer(form, true));
		module.addSerializer(Column.class, new DojoGridColumnSerializer());
		objectMapper.registerModule(module);
		return objectMapper;
	}
}
