package com.aplana.sbrf.taxaccounting.controller.form;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.aplana.sbrf.taxaccounting.dao.FormDao;
import com.aplana.sbrf.taxaccounting.model.Form;

@Controller
@RequestMapping("VIEW")
public class ListFormController {
	@Autowired
	private FormDao formDao;
	
	@RenderMapping
	String showList() {
		return "form/list";
	}
	
	@ModelAttribute("forms")
	List<Form> getForms() {
		return formDao.listForms();
	}
}
