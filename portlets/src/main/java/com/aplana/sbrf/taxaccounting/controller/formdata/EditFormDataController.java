package com.aplana.sbrf.taxaccounting.controller.formdata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;

@Controller
@RequestMapping("EDIT")
@SessionAttributes("record")
public class EditFormDataController {
	@Autowired
	private FormDao formDao;
	
	private final static int FORM_ID = 1;
	
	@ModelAttribute("record")
	FormData getRecord() {
		Form form = formDao.getForm(FORM_ID);
		FormData formData = new FormData(null, form);
		return formData;
	}
	
	@ActionMapping(params="action=new")
	public void newRecord() {
		
	}
	
	@RenderMapping
	public String showEdit() {
		return "formData/edit";
	}
}
