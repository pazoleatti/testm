package com.aplana.sbrf.taxaccounting.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.aplana.sbrf.taxaccounting.model.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

@Service
@Transactional
public class DeclarationTemplateImpexServiceImpl implements
		DeclarationTemplateImpexService {

	@Autowired
	DeclarationTemplateDao declarationTemplateDao;
	
	@Autowired
	DeclarationTemplateService declarationTemplateService;

	private final static String VERSION_FILE = "version";
	private final static String CONTENT_FILE = "content.xml";
	private final static String SCRIPT_FILE = "script.groovy";
	private final static String REPORT_FILE = "report.jrxml";

	@Override
	public void exportDeclarationTemplate(TAUserInfo userInfo, Integer id, OutputStream os) {
		try {
			DeclarationTemplate dt = declarationTemplateDao.get(id);
			ZipOutputStream zos = new ZipOutputStream(os);
			
			// Version
			ZipEntry ze = new ZipEntry(VERSION_FILE);
			zos.putNextEntry(ze);
			zos.write("1.0".getBytes());
			zos.closeEntry();
			
			// Script
			ze = new ZipEntry(SCRIPT_FILE);
			zos.putNextEntry(ze);
			zos.write(dt.getCreateScript().getBytes());
			zos.closeEntry();
			
			// JasperTemplate
			ze = new ZipEntry(REPORT_FILE);
			zos.putNextEntry(ze);
			zos.write(declarationTemplateDao.getJrxml(id).getBytes());
			zos.closeEntry();

			// content
			ze = new ZipEntry(CONTENT_FILE);
			zos.putNextEntry(ze);
			DeclarationTemplateContent dtc = new DeclarationTemplateContent();
			dtc.setType(dt.getDeclarationType());
			dtc.setVersion(dt.getVersion());
			JAXBContext jaxbContext = JAXBContext.newInstance(DeclarationTemplateContent.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.marshal(dtc, zos);
			zos.closeEntry();
			
			zos.finish();
		} catch (Exception e) {
			throw new ServiceException("Не удалось экспортировать шаблон", e);
		}
	}

	@Override
	public void importDeclarationTemplate(TAUserInfo userInfo, Integer id,
			InputStream is) {
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
			
            if ("1.0".equals(version)){
            	DeclarationTemplate dt = declarationTemplateDao.get(id);

				if (files.get(CONTENT_FILE).length != 0) {
					DeclarationTemplateContent dtc;
					JAXBContext jaxbContext = JAXBContext.newInstance(DeclarationTemplateContent.class);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					dtc = (DeclarationTemplateContent) jaxbUnmarshaller.
							unmarshal(new ByteArrayInputStream(files.get(CONTENT_FILE)));
					dt.setDeclarationType(dtc.getType());
					dt.setVersion(dtc.getVersion());
				}
				if (files.get("script.groovy").length != 0) {
					dt.setCreateScript(new String(files.get("script.groovy")));
				}
            	declarationTemplateDao.save(dt);
				if (files.get("report.jrxml").length != 0) {
					declarationTemplateService.setJrxml(id, new String(files.get("report.jrxml")));
				}
            } else {
            	throw new ServiceException("Версия файла для импорта не поддерживается: " + version);
            }
		} catch (Exception e) {
			throw new ServiceException("Не удалось импортировать шаблон", e);
		}
	}
}
