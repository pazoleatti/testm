package com.aplana.sbrf.taxaccounting.controller.formdata;

import java.io.IOException;
import java.util.List;

import javax.portlet.ResourceResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;

@Controller
@SessionAttributes({"formData", "form"})
@RequestMapping("EDIT")
public class EditFormDataController {
	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private FormDao formDao;
	
	private final static int FORM_ID = 1;
	
	@ModelAttribute("form")
	public Form getForm() {
		return formDao.getForm(FORM_ID);
	}
	
	@ModelAttribute("formData")
	FormData getFormData(@ModelAttribute("form") Form form) {
		FormData formData = new FormData(null, form);
		formData.appendDataRow().setCode("1");
		formData.appendDataRow().setCode("2");
		return formData;
	}
	
	@ActionMapping(params="action=new")
	public void newRecord() {
		logger.warn("NOT IMPLEMENTED");
	}
	
	@RenderMapping
	public String showEdit() {
		return "formData/edit";
	}
	
	@ResourceMapping("dataRows")
	@RequestMapping(method=RequestMethod.GET)
	public void getDataRows(@ModelAttribute("formData") FormData formData, ResourceResponse response) throws JsonGenerationException, JsonMappingException, IOException {
		response.setContentType("application/json");
		List<DataRow> dataRows = formData.getDataRows();
		response.setProperty("Content-Range", "items 0-" + (dataRows.size() - 1) + "/" + dataRows.size());
		ObjectMapper objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule("taxaccounting", new Version(1, 0, 0, null));
		module.addSerializer(DataRow.class, new DataRowSerializer());
		objectMapper.registerModule(module);
		objectMapper.writeValue(response.getPortletOutputStream(), dataRows);
	}
}
