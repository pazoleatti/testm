package com.aplana.sbrf.taxaccounting.controller.forms;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

@Controller
@RequestMapping("VIEW")
public class FormsController {
	@RenderMapping
	String showList() {
		return "list";
	}
}
