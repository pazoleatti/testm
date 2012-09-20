package com.aplana.sbrf.taxaccounting.controller.form;

import java.io.IOException;
import java.util.List;

import javax.portlet.ActionResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.util.json.DataRowDeserializer;
import com.aplana.sbrf.taxaccounting.util.json.DataRowSerializer;

@Controller
@RequestMapping("EDIT")
public class EditFormController {
	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private FormDao formDao;
	
	@RenderMapping
	protected ModelAndView showEditForm(@RequestParam("formId") Integer formId) throws JsonGenerationException, JsonMappingException, IOException {
		if (formId == null) {
			return new ModelAndView("emptySession");
		}
		ModelAndView mv = new ModelAndView("form/edit");
		mv.addObject("formId", formId);
		return mv;
	}
	
	@ResourceMapping("getForm")
	protected void getForm(@RequestParam("formId") Integer formId, ResourceResponse response) throws JsonGenerationException, JsonMappingException, IOException {
		Form form;
		if (formId == null) {
			// Создание новой формы
			// Сюда можно добавить инициализацию, если будет необходимо
			form = new Form();
		} else {
			form = formDao.getForm(formId);
		}
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("taxaccounting", new Version(1, 0, 0, null));
		module.addSerializer(DataRow.class, new DataRowSerializer(FormatUtils.getIsoDateFormat()));
		objectMapper.registerModule(module);
		objectMapper.getSerializationConfig().addMixInAnnotations(Column.class, ColumnMixIn.class);
		objectMapper.writeValue(response.getWriter(), form);
	}
	
	@ActionMapping("saveForm")
	protected void saveForm(
		@RequestParam("form") String formJson,
		@RequestParam("rows") String rowsJson,
		ActionResponse response) 
	throws JsonGenerationException, JsonMappingException, IOException {
		// TODO: кто-то накладывает HTML-escaping на передаваемые данные
		// найти точку, где это происходит пока не удалось, поэтому временное решение - принудительный Unescape
		formJson = StringEscapeUtils.unescapeHtml4(formJson);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.getDeserializationConfig().addMixInAnnotations(Column.class, ColumnMixIn.class);
		Form form = objectMapper.readValue(formJson, Form.class);
		
		ObjectMapper dataRowsMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("taxaccounting-form", new Version(1, 0, 0, null));
		module.addDeserializer(DataRow.class, new DataRowDeserializer(form, FormatUtils.getIsoDateFormat(), false));
		dataRowsMapper.registerModule(module);
		List<DataRow> formRows = dataRowsMapper.readValue(rowsJson, new TypeReference<List<DataRow>>() {});
		form.getRows().addAll(formRows);
		
		int formId = formDao.saveForm(form);
		response.setRenderParameter("formId", String.valueOf(formId));
	}	
	
	@ActionMapping("edit")
	protected void editForm(@RequestParam("formId") int formId, ActionResponse response) {
		logger.info("Starting editing form: formId = " + formId);
		response.setRenderParameter("formId", String.valueOf(formId));
	}
}