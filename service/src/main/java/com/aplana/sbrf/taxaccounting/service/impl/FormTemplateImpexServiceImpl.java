package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationUtils;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.FormTemplateContent;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.service.FormTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static String[] strings = new String[]{VERSION_FILE, CONTENT_FILE, SCRIPT_FILE, ROWS_FILE, HEADERS_FILE};
	private final static String ENCODING = "UTF-8";

    private static final Log logger = LogFactory.getLog(FormTemplateImpexServiceImpl.class);

	@Override
	public void exportFormTemplate(Integer id, OutputStream os) {
		try {
			FormTemplate ft = formTemplateService.getFullFormTemplate(id);
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

            String ftScript = formTemplateDao.getFormTemplateScript(id);
			if (ftScript != null) {
				zos.write(ftScript.getBytes(ENCODING));
            }
			zos.closeEntry();
			
			// DataRows
			ze = new ZipEntry(ROWS_FILE);
			zos.putNextEntry(ze);
			zos.write(xmlSerializationUtils.serialize(ft.getRows()).getBytes(ENCODING));
			zos.closeEntry();

			// Headers
			ze = new ZipEntry(HEADERS_FILE);
			zos.putNextEntry(ze);
			zos.write(xmlSerializationUtils.serialize(ft.getHeaders()).getBytes(ENCODING));
			zos.closeEntry();

			zos.finish();
		} catch (Exception e) {
			throw new ServiceException("Не удалось экспортировать шаблон", e);
		}

	}

	@Override
	public FormTemplate importFormTemplate(Integer id, InputStream is) {
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
				FormTemplate ft = formTemplateService.getFullFormTemplate(id);
				if (files.get(CONTENT_FILE).length != 0) {
					FormTemplateContent ftc;
					JAXBContext jaxbContext = JAXBContext.newInstance(FormTemplateContent.class);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					ftc = (FormTemplateContent) jaxbUnmarshaller.unmarshal(
                            new InputStreamReader(new ByteArrayInputStream(files.get(CONTENT_FILE)), ENCODING));
					ftc.fillFormTemplate(ft);
				}
				if (files.get(SCRIPT_FILE).length != 0) {
					ft.setScript(new String(files.get(SCRIPT_FILE), ENCODING));
                }
				if (files.get(ROWS_FILE).length != 0) {
					ft.getRows().clear();
					ft.getRows().addAll(xmlSerializationUtils.deserialize
							(new String(files.get(ROWS_FILE), ENCODING), ft.getColumns(), ft.getStyles(), Cell.class));
                }
				if (files.get(HEADERS_FILE).length != 0) {
					ft.getHeaders().clear();
					ft.getHeaders().addAll(xmlSerializationUtils.deserialize
							(new String(files.get(HEADERS_FILE), ENCODING), ft.getColumns(), ft.getStyles(), HeaderCell.class));
				}
            	return ft;
            } else {
            	throw new ServiceException("Версия файла для импорта не поддерживается: " + version);
            }
		} catch (Exception e) {
            throw new ServiceException("Не удалось импортировать шаблон", e);
		}
	}

    @Override
    public void exportAllTemplates(ZipOutputStream stream) {

        FileInputStream in;
        File temFolder;
        try {
            temFolder = File.createTempFile(TEMPLATES_FOLDER, "");
            temFolder.delete();
            if (!temFolder.mkdir())
                logger.error("");
        } catch (IOException e) {
            logger.error("Ошибки при создании временной директории.",e);
            throw new ServiceException("Ошибки при создании временной директории.");
        }

        List<FormTemplate> formTemplates = formTemplateService.listAll();
        ArrayList<String> paths = new ArrayList<String>(formTemplates.size());
        for (FormTemplate template : formTemplates){
            String folderTemplateName =
                    String.format(TEMPLATE_OF_FOLDER_NAME,
                            template.getType().getTaxType().getName(),
                            template.getType().getId(),
                            SIMPLE_DATE_FORMAT.format(template.getVersion()));
            paths.add(folderTemplateName);
            try {
                File folderTemplate = new File(temFolder.getAbsolutePath() + File.separator + folderTemplateName, "");
                folderTemplate.delete();
                if (!folderTemplate.mkdirs())
                    logger.warn("Can't create temporary directory");
                //
                FileOutputStream tempFile = new FileOutputStream(new File(folderTemplate.getAbsolutePath() + File.separator + VERSION_FILE));
                tempFile.write("1.0".getBytes());
                tempFile.close();
                //
                tempFile = new FileOutputStream(new File(folderTemplate.getAbsolutePath() + File.separator + CONTENT_FILE));
                FormTemplateContent ftc = new FormTemplateContent();
                ftc.fillFormTemplateContent(template);
                JAXBContext jaxbContext = JAXBContext.newInstance(FormTemplateContent.class);
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.marshal(ftc, tempFile);
                tempFile.close();
                //
                tempFile =  new FileOutputStream(new File(folderTemplate.getAbsolutePath() + File.separator + SCRIPT_FILE));
                String ftScript = formTemplateDao.getFormTemplateScript(template.getId());
                if (ftScript != null) {
                    tempFile.write(ftScript.getBytes(ENCODING));
                }
                tempFile.close();
                //
                tempFile = new FileOutputStream(new File(folderTemplate.getAbsolutePath() + File.separator + ROWS_FILE));
                tempFile.write(xmlSerializationUtils.serialize(formTemplateDao.getDataCells(template)).getBytes(ENCODING));
                tempFile.close();
                //
                tempFile = new FileOutputStream(new File(folderTemplate.getAbsolutePath() + File.separator + HEADERS_FILE));
                tempFile.write(xmlSerializationUtils.serialize(formTemplateDao.getHeaderCells(template)).getBytes(ENCODING));
                tempFile.close();

            } catch (IOException e) {
                logger.error("Ошибки при создании временной директории. Шаблон " + template.getName(), e);
            } catch (JAXBException e) {
                throw new ServiceException("Ошибка экспорта. Шаблон " + template.getName());
            }
        }

        String pathPattern = File.separator + "%s" + File.separator + "%s";
        for (String path : paths){
            ZipEntry ze;
            try {
                for (String s : strings){
                    ze = new ZipEntry(TEMPLATES_FOLDER + String.format(pathPattern, path, s));
                    stream.putNextEntry(ze);
                    in = new FileInputStream(temFolder.getAbsolutePath() + String.format(pathPattern, path, s));
                    IOUtils.copy(in, stream);
                    stream.closeEntry();
                    IOUtils.closeQuietly(in);

                }
            } catch (IOException e){
                logger.error("Error path " + path, e);
                throw new ServiceException("Error");
            }
        }
        try {
            for (String path : paths){
                ZipEntry ze;
                for (String s : strings){
                    ze = new ZipEntry(TEMPLATES_FOLDER + String.format(pathPattern, path, s));
                    stream.putNextEntry(ze);
                    in = new FileInputStream(temFolder.getAbsolutePath() + String.format(pathPattern, path, s));
                    IOUtils.copy(in, stream);
                    stream.closeEntry();
                    IOUtils.closeQuietly(in);
                }
            }
        } catch (IOException e){
            logger.error("Error", e);
            throw new ServiceException("Error");
        }
    }
}
