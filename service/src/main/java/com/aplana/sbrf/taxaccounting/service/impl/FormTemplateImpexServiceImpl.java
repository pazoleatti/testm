package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.FormTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
public class FormTemplateImpexServiceImpl implements
		FormTemplateImpexService {

	@Autowired
	FormTemplateDao formTemplateDao;
	
	@Autowired
	FormTemplateService formTemplateService;
	
	final static String VERSION_FILE = "version";

	@Override
	public void exportFormTemplate(Integer id, OutputStream os) {

		try {
			FormTemplate ft = formTemplateDao.get(id);

			ZipOutputStream zos = new ZipOutputStream(os);
			
			// Version
			ZipEntry ze = new ZipEntry(VERSION_FILE);
			zos.putNextEntry(ze);
			zos.write(ft.getVersion().getBytes());
			zos.closeEntry();

			/*
			// Script
			ze = new ZipEntry("script.groovy");
			zos.putNextEntry(ze);
			zos.write(ft.getCreateScript().getBytes());
			zos.closeEntry();
			
			// JasperTemplate
			ze = new ZipEntry("report.jrxml");
			zos.putNextEntry(ze);
			zos.write(declarationTemplateDao.getJrxml(id).getBytes());
			zos.closeEntry();
			*/
			
			// content
			ze = new ZipEntry("content.xml");
			zos.putNextEntry(ze);
			zos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><formTemplate></formTemplate>".getBytes());
			zos.closeEntry();
			
			zos.finish();

		} catch (Exception e) {
			throw new ServiceException("Не удалось экспортировать шаблон", e);
		}

	}

	@Override
	public void importFormTemplate(Integer id, InputStream is) {
		
		try {

			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry;
			String version = null;
			Map<String, byte[]> files = new HashMap<String, byte[]>();
            while((entry = zis.getNextEntry())!=null){
            	if (VERSION_FILE.equals(entry.getName())){
            		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
            		IOUtils.copy(zis, baos);
            		version = new String(baos.toByteArray());
            	} else {
            		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
            		IOUtils.copy(zis, baos);
            		files.put(entry.getName(), baos.toByteArray());
            	}
            	
            }
			/*
            if ("1.0".equals(version)){
            	FormTemplate ft = formTemplateDao.get(id);
				ft.setCreateScript(new String(files.get("script.groovy")));
            	declarationTemplateDao.save(dt);
            	declarationTemplateService.setJrxml(id, new String(files.get("report.jrxml")));

            } else {
            	throw new ServiceException("Версия файла для импорта не поддерживается: " + version);
            } */


		} catch (Exception e) {
			throw new ServiceException("Не удалось импортировать шаблон", e);
		}

	}

}
