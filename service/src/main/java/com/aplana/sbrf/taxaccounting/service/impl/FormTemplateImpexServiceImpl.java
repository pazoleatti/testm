package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.service.FormTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
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

	private final XmlSerializationUtils xmlSerializationUtils = XmlSerializationUtils.getInstance();
	private final static String VERSION_FILE = "version";
	private final static String CONTENT_FILE = "content.xml";
	private final static String SCRIPT_FILE = "script.groovy";
	private final static String ROWS_FILE = "rows.xml";
	private final static String HEADERS_FILE = "headers.xml";
	private final static String UNICODE = "UTF-8";

	@Override
	public void exportFormTemplate(Integer id, OutputStream os) {
		try {
			FormTemplate ft = formTemplateDao.get(id);
			ZipOutputStream zos = new ZipOutputStream(os);

			// Version
			ZipEntry ze = new ZipEntry(VERSION_FILE);
			zos.putNextEntry(ze);
			zos.write("1.0".getBytes());
			zos.closeEntry();

			// content
			ze = new ZipEntry(CONTENT_FILE);
			zos.putNextEntry(ze);
			FormTemplateContent ftc = new FormTemplateContent();
			ftc.fillFormTemplateContent(ft);
			JAXBContext jaxbContext = JAXBContext.newInstance(FormTemplateContent.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.marshal(ftc, zos);
			zos.closeEntry();

			// Script
			ze = new ZipEntry(SCRIPT_FILE);
			zos.putNextEntry(ze);
			if (ft.getScript() != null) {
				zos.write(ft.getScript().getBytes(UNICODE));
			}
			zos.closeEntry();
			
			// DataRows
			ze = new ZipEntry(ROWS_FILE);
			zos.putNextEntry(ze);
			zos.write(xmlSerializationUtils.serialize(ft.getRows()).getBytes());
			zos.closeEntry();

			// Headers
			ze = new ZipEntry(HEADERS_FILE);
			zos.putNextEntry(ze);
			zos.write(xmlSerializationUtils.serialize(ft.getHeaders()).getBytes());
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
			Map<String, byte[]> files = new HashMap<String, byte[]>();
			String version = null;
			while((entry = zis.getNextEntry())!=null) {
				if (VERSION_FILE.equals(entry.getName())) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					IOUtils.copy(zis, baos);
					version = new String(baos.toByteArray());
				} else {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					IOUtils.copy(zis, baos);
					files.put(entry.getName(), baos.toByteArray());
				}
			}

            if ("1.0".equals(version)) {
				FormTemplate ft = formTemplateDao.get(id);
				if (files.get(CONTENT_FILE).length != 0) {
					FormTemplateContent ftc;
					JAXBContext jaxbContext = JAXBContext.newInstance(FormTemplateContent.class);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					ftc = (FormTemplateContent) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(files.get(CONTENT_FILE)));
					ftc.fillFormTemplate(ft);
				}
				if (files.get(SCRIPT_FILE).length != 0) {
					ft.setScript(new String(files.get(SCRIPT_FILE)));
				}
				if (files.get(ROWS_FILE).length != 0) {
					ft.getRows().clear();
					ft.getRows().addAll(xmlSerializationUtils.deserialize
							(new String(files.get(ROWS_FILE)), ft.getColumns(), ft.getStyles(), Cell.class));
				}
				if (files.get(HEADERS_FILE).length != 0) {
					ft.getHeaders().clear();
					ft.getHeaders().addAll(xmlSerializationUtils.deserialize
							(new String(files.get(HEADERS_FILE)), ft.getColumns(), ft.getStyles(), HeaderCell.class));
				}
            	formTemplateDao.save(ft);
            } else {
            	throw new ServiceException("Версия файла для импорта не поддерживается: " + version);
            }
		} catch (Exception e) {
			throw new ServiceException("Не удалось импортировать шаблон", e);
		}
	}
}
