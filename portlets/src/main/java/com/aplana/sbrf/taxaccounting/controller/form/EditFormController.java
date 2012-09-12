package com.aplana.sbrf.taxaccounting.controller.form;

import java.io.IOException;

import javax.portlet.ResourceResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
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
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.Form;

@Controller
@RequestMapping("EDIT")
@SessionAttributes("formBean")
public class EditFormController {
	private Log logger = LogFactory.getLog(getClass());
	
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
	
	@ResourceMapping("saveForm")
	protected void saveForm(@ModelAttribute("formBean") EditFormBean formBean, @RequestParam("formData") String formJson) throws JsonGenerationException, JsonMappingException, IOException {
		// TODO: кто-то накладывает HTML-escaping на передаваемые данные
		// найти точку пока не удалось, поэтому временное решение - принудительный Unescape
		formJson = StringEscapeUtils.unescapeHtml4(formJson);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.getDeserializationConfig().addMixInAnnotations(Column.class, ColumnMixIn.class);
		
		Form f = objectMapper.readValue(formJson, Form.class);
		int formId = formDao.saveForm(f);
		formBean.setForm(formDao.getForm(formId));
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