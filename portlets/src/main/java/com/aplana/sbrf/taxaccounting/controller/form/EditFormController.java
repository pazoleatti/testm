package com.aplana.sbrf.taxaccounting.controller.form;

import java.io.IOException;

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
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.util.DojoFileStoreData;

@Controller
@RequestMapping("EDIT")
@SessionAttributes("formBean")
public class EditFormController {
	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private FormDao formDao;
	
	@RenderMapping
	protected ModelAndView showEditForm(@ModelAttribute("formBean") EditFormBean formBean) throws JsonGenerationException, JsonMappingException, IOException {
		ModelAndView mv = new ModelAndView("form/edit");
		Form form = formBean.getForm();
		if (form == null) {
			return new ModelAndView("emptySession");
		}
		ObjectMapper objectMapper = new ObjectMapper();
		DojoFileStoreData<Column> columnsData = new DojoFileStoreData<Column>();
		columnsData.setItems(formBean.getForm().getColumns());
		columnsData.setIdentifier("id");
		String columnsJson = objectMapper.writeValueAsString(columnsData);
		mv.addObject("columnsJson", columnsJson);
		return mv;
	}
	
	@ModelAttribute("formBean")
	protected EditFormBean getFormBean() {
		return new EditFormBean();
	}
	
	@ActionMapping("edit")
	protected void editForm(@RequestParam("formId") int formId, @ModelAttribute("formBean") EditFormBean formBean) {
		logger.info("Starting editing form: formId = " + formId);
		formBean.setForm(formDao.getForm(formId));
	}
}