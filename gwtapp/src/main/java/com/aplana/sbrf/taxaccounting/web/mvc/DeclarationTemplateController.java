package com.aplana.sbrf.taxaccounting.web.mvc;

import java.io.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping("/uploadJrxml")
public class DeclarationTemplateController {

	@Autowired
	DeclarationTemplateService declarationTemplateService;

	@RequestMapping(value = "/{declarationTemplateId}",method = RequestMethod.POST)
	public void processDownload(@PathVariable int declarationTemplateId, HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		System.out.println("DeclarationTemplateController");
	}
}
