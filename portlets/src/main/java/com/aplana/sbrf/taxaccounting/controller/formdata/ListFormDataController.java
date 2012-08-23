package com.aplana.sbrf.taxaccounting.controller.formdata;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

@Controller
@RequestMapping("VIEW")
public class ListFormDataController {
	@RenderMapping
	public String list() {
		return "formData/list";
	}
}
