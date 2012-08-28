package com.aplana.sbrf.taxaccounting.controller.formdata;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.model.FormData;

@Controller
@RequestMapping("VIEW")
public class ListFormDataController {
	@Autowired
	private FormDataDao formDataDao;
	
	@RenderMapping
	public String list() {
		return "formData/list";
	}
	
	@ModelAttribute("data")
	protected List<FormData> getData() {
		return formDataDao.getAll();
	}
}
