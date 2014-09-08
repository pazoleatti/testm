package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.XmlSerializationUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
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
import java.text.SimpleDateFormat;
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

    @Autowired
    DeclarationTemplateService declarationTemplateService;

	private final XmlSerializationUtils xmlSerializationUtils = XmlSerializationUtils.getInstance();
	private final static String VERSION_FILE = "version";
	private final static String CONTENT_FILE = "content.xml";
	private final static String SCRIPT_FILE = "script.groovy";
	private final static String ROWS_FILE = "rows.xml";
	private final static String HEADERS_FILE = "headers.xml";
    private static String[] strings = new String[]{VERSION_FILE, CONTENT_FILE, SCRIPT_FILE, ROWS_FILE, HEADERS_FILE};
	private final static String ENCODING = "UTF-8";

    private static final Log logger = LogFactory.getLog(FormTemplateImpexServiceImpl.class);

    private static final int MAX_NAME_OF_DIR = 50;

    private static final String DEC_TEMPLATES_FOLDER = "declaration";
    private static final String FORM_TEMPLATES_FOLDER = "form";

    private static final String TEMPLATE_OF_FOLDER_NAME =
            "%s" + File.separator + "%s-%s" + File.separator + "%s";
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT_YEAR = new SimpleDateFormat("yyyy");

	@Override
	public void exportFormTemplate(Integer id, OutputStream os) {
		try {
			FormTemplate ft = formTemplateService.getFullFormTemplate(id, new Logger());
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
            throw new ServiceException("Загрузить макет не удалось. Проверьте источник данных", e);
		}
	}

    @Override
    public void exportAllTemplates(OutputStream stream) {
        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(stream);
        zipOutputStream.setEncoding("UTF-8");
        zipOutputStream.setFallbackToUTF8(true);
        exportFormTemplate(zipOutputStream);
        exportDecTemplates(zipOutputStream);
        try {
            zipOutputStream.finish();
        } catch (IOException e) {
            logger.error("", e);
            throw new ServiceException("", e);
        }
    }

    //Удаление временных паоок
    private void dirTempDelete(File directory){
        if (directory.listFiles() == null){
            if (!directory.delete())
                logger.warn("Faild to delete file " + directory);
            return;
        }
        for (File file : directory.listFiles()){
            if (file.isDirectory()){
                dirTempDelete(file);
                if (!file.delete())
                    logger.warn("Faild to delete file " + file);
            } else {
                if (!file.delete())
                    logger.warn("Faild to delete file " + file);
            }
        }
    }

    private void exportDecTemplates(ZipArchiveOutputStream zipOutputStream){
        FileInputStream in;
        File temFolder;
        try {
            temFolder = File.createTempFile(DEC_TEMPLATES_FOLDER, "");
            temFolder.delete();
            if (!temFolder.mkdir())
                logger.error("Can't create directory for declarations");
        } catch (IOException e) {
            throw new ServiceException("Ошибки при создании временной директории.");
        }
        List<DeclarationTemplate> declarationTemplates = declarationTemplateService.getByFilter(null);
        ArrayList<String> paths = new ArrayList<String>(declarationTemplates.size());
        for (DeclarationTemplate template : declarationTemplates){
            String folderTemplateName =
                    Translator.transliterate(String.format(TEMPLATE_OF_FOLDER_NAME,
                            template.getType().getTaxType().name().toLowerCase(),
                            template.getType().getId(),
                            template.getType().getName().length() > MAX_NAME_OF_DIR ?
                                    template.getType().getName().substring(0, MAX_NAME_OF_DIR).trim()
                                    : template.getType().getName(),
                            SIMPLE_DATE_FORMAT_YEAR.format(template.getVersion())));
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
                tempFile =  new FileOutputStream(new File(folderTemplate.getAbsolutePath() + File.separator + SCRIPT_FILE));
                String ftScript = declarationTemplateService.getDeclarationTemplateScript(template.getId());
                if (ftScript != null) {
                    tempFile.write(ftScript.getBytes(ENCODING));
                }
                tempFile.close();
                //
                tempFile =  new FileOutputStream(new File(folderTemplate.getAbsolutePath() + File.separator + DeclarationTemplateImpexServiceImpl.REPORT_FILE));
                String dtJrxm = declarationTemplateService.getJrxml(template.getId());
                if (dtJrxm != null)
                    tempFile.write(dtJrxm.getBytes(ENCODING));
                tempFile.close();

            } catch (IOException e) {
                logger.error("Ошибки при создании временной директории. Шаблон " + template.getName(), e);
                throw new ServiceException("Ошибки при создании временной директории.");
            }
        }

        String pathPattern = File.separator + "%s" + File.separator + "%s";
        try {
            ZipArchiveEntry ze;
            for (String path : paths){
                // Version
                ze = new ZipArchiveEntry(DEC_TEMPLATES_FOLDER + String.format(pathPattern, path, DeclarationTemplateImpexServiceImpl.VERSION_FILE));
                zipOutputStream.putArchiveEntry(ze);
                in = new FileInputStream(temFolder.getAbsolutePath() + String.format(pathPattern, path, DeclarationTemplateImpexServiceImpl.VERSION_FILE));
                IOUtils.copy(in, zipOutputStream);
                zipOutputStream.closeArchiveEntry();
                IOUtils.closeQuietly(in);

                // Script
                ze = new ZipArchiveEntry(DEC_TEMPLATES_FOLDER + String.format(pathPattern, path, DeclarationTemplateImpexServiceImpl.SCRIPT_FILE));
                zipOutputStream.putArchiveEntry(ze);
                in = new FileInputStream(temFolder.getAbsolutePath() + String.format(pathPattern, path, DeclarationTemplateImpexServiceImpl.SCRIPT_FILE));
                IOUtils.copy(in, zipOutputStream);
                zipOutputStream.closeArchiveEntry();
                IOUtils.closeQuietly(in);

                // JasperTemplate
                ze = new ZipArchiveEntry(DEC_TEMPLATES_FOLDER + String.format(pathPattern, path, DeclarationTemplateImpexServiceImpl.REPORT_FILE));
                zipOutputStream.putArchiveEntry(ze);
                in = new FileInputStream(temFolder.getAbsolutePath() + String.format(pathPattern, path, DeclarationTemplateImpexServiceImpl.REPORT_FILE));
                IOUtils.copy(in, zipOutputStream);
                zipOutputStream.closeArchiveEntry();
                IOUtils.closeQuietly(in);
            }
        } catch (IOException e){
            logger.error("Error ", e);
            throw new ServiceException("Error");
        } finally {
            dirTempDelete(temFolder);
        }
    }

    private void exportFormTemplate(ZipArchiveOutputStream zipOutputStream){
        FileInputStream in;
        File temFolder;
        try {
            temFolder = File.createTempFile(FORM_TEMPLATES_FOLDER, "");
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
                    Translator.transliterate(String.format(TEMPLATE_OF_FOLDER_NAME,
                            template.getType().getTaxType().name().toLowerCase(),
                            template.getType().getId(),
                            template.getType().getName().length() > MAX_NAME_OF_DIR ?
                                    template.getType().getName().substring(0, MAX_NAME_OF_DIR).trim()
                                    : template.getType().getName(),
                            SIMPLE_DATE_FORMAT_YEAR.format(template.getVersion())));
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
                try {
                    tempFile.write(xmlSerializationUtils.serialize(formTemplateDao.getDataCells(template)).getBytes(ENCODING));
                } catch (DaoException ignore){

                }
                tempFile.close();
                //
                tempFile = new FileOutputStream(new File(folderTemplate.getAbsolutePath() + File.separator + HEADERS_FILE));
                try {
                    tempFile.write(xmlSerializationUtils.serialize(formTemplateDao.getHeaderCells(template)).getBytes(ENCODING));
                } catch (DaoException ignore){

                }
                tempFile.close();

            } catch (IOException e) {
                logger.error("Ошибки при создании временной директории. Шаблон " + template.getName(), e);
            } catch (JAXBException e) {
                throw new ServiceException("Ошибка экспорта. Шаблон " + template.getName());
            }
        }

        try {
            String pathPattern = File.separator + "%s" + File.separator + "%s";
            for (String path : paths){
                ZipArchiveEntry ze;
                for (String s : strings){
                    ze = new ZipArchiveEntry(FORM_TEMPLATES_FOLDER + String.format(pathPattern, path, s));
                    zipOutputStream.putArchiveEntry(ze);
                    in = new FileInputStream(temFolder.getAbsolutePath() + String.format(pathPattern, path, s));
                    IOUtils.copy(in, zipOutputStream);
                    zipOutputStream.closeArchiveEntry();
                    IOUtils.closeQuietly(in);
                }
            }
        } catch (IOException e){
            logger.error("Error", e);
            throw new ServiceException("Error");
        } finally {
            dirTempDelete(temFolder);
        }
    }
}
