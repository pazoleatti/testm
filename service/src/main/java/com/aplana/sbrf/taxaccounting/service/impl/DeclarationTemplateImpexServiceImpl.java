package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
public class DeclarationTemplateImpexServiceImpl implements
		DeclarationTemplateImpexService {

	@Autowired
	DeclarationTemplateDao declarationTemplateDao;
	
	@Autowired
	DeclarationTemplateService declarationTemplateService;

	private final static String VERSION_FILE = "version";
    private final static String SCRIPT_FILE = "script.groovy";
	private final static String REPORT_FILE = "report.jrxml";
	private final static String ENCODING = "UTF-8";

    private static final Log logger = LogFactory.getLog(DeclarationTemplateImpexServiceImpl.class);

	@Override
	public void exportDeclarationTemplate(TAUserInfo userInfo, Integer id, OutputStream os) {
		try {
			ZipOutputStream zos = new ZipOutputStream(os);
			
			// Version
			ZipEntry ze = new ZipEntry(VERSION_FILE);
			zos.putNextEntry(ze);
			zos.write("1.0".getBytes());
			zos.closeEntry();
			
			// Script
			ze = new ZipEntry(SCRIPT_FILE);
			zos.putNextEntry(ze);
            declarationTemplateDao.getDeclarationTemplateScript(id);
            String dtScript = declarationTemplateDao.getDeclarationTemplateScript(id);
            if (dtScript != null)
			    zos.write(dtScript.getBytes(ENCODING));
			zos.closeEntry();
			
			// JasperTemplate
			ze = new ZipEntry(REPORT_FILE);
			zos.putNextEntry(ze);
            String dtJrxm = declarationTemplateService.getJrxml(id);
            if (dtJrxm != null)
			    zos.write(dtJrxm.getBytes(ENCODING));
			zos.closeEntry();

			// content
            //Убрал в связи с тем, что ввели версионирование
			/*ze = new ZipEntry(CONTENT_FILE);
			zos.putNextEntry(ze);
			DeclarationTemplateContent dtc = new DeclarationTemplateContent();
			dtc.setType(dt.getType());
			//dtc.setVersion(dt.getVersion());
			JAXBContext jaxbContext = JAXBContext.newInstance(DeclarationTemplateContent.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.marshal(dtc, zos);
			zos.closeEntry();*/
			
			zos.finish();
		} catch (Exception e) {
			throw new ServiceException("Не удалось экспортировать шаблон", e);
		}
	}

	@Override
	public DeclarationTemplate importDeclarationTemplate(TAUserInfo userInfo, Integer id,
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

				/*if (files.get(CONTENT_FILE).length != 0) {
					DeclarationTemplateContent dtc;
					JAXBContext jaxbContext = JAXBContext.newInstance(DeclarationTemplateContent.class);
					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					dtc = (DeclarationTemplateContent) jaxbUnmarshaller.
							unmarshal(new ByteArrayInputStream(files.get(CONTENT_FILE)));
					dt.setType(dtc.getType());
					//dt.setVersion(dtc.getVersion());
				}*/
				if (files.get("script.groovy").length != 0) {
                    byte[] bytes = files.get("script.groovy");
					dt.setCreateScript(new String(bytes, 0, bytes.length, Charset.forName("UTF-8")));
				}
            	declarationTemplateDao.save(dt);
				if (files.get("report.jrxml").length != 0) {
					declarationTemplateService.setJrxml(id, new ByteArrayInputStream(files.get("report.jrxml")));
				}
                return dt;
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
                logger.error("Can't create directory for declarations");
        } catch (IOException e) {
            throw new ServiceException("Ошибки при создании временной директории.");
        }
        List<DeclarationTemplate> declarationTemplates = declarationTemplateService.getByFilter(null);
        ArrayList<String> paths = new ArrayList<String>(declarationTemplates.size());
        for (DeclarationTemplate template : declarationTemplates){
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
                tempFile =  new FileOutputStream(new File(folderTemplate.getAbsolutePath() + File.separator + SCRIPT_FILE));
                String ftScript = declarationTemplateDao.getDeclarationTemplateScript(template.getId());
                if (ftScript != null) {
                    tempFile.write(ftScript.getBytes(ENCODING));
                }
                tempFile.close();
                //
                tempFile =  new FileOutputStream(new File(folderTemplate.getAbsolutePath() + File.separator + REPORT_FILE));
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
            for (String path : paths){
                // Version
                ZipEntry ze = new ZipEntry(TEMPLATES_FOLDER + String.format(pathPattern, path, VERSION_FILE));
                stream.putNextEntry(ze);
                in = new FileInputStream(temFolder.getAbsolutePath() + String.format(pathPattern, path, VERSION_FILE));
                IOUtils.copy(in, stream);
                stream.closeEntry();
                IOUtils.closeQuietly(in);

                // Script
                ze = new ZipEntry(TEMPLATES_FOLDER + String.format(pathPattern, path, SCRIPT_FILE));
                stream.putNextEntry(ze);
                in = new FileInputStream(temFolder.getAbsolutePath() + String.format(pathPattern, path, SCRIPT_FILE));
                IOUtils.copy(in, stream);
                stream.closeEntry();
                IOUtils.closeQuietly(in);

                // JasperTemplate
                ze = new ZipEntry(TEMPLATES_FOLDER + String.format(pathPattern, path, REPORT_FILE));
                stream.putNextEntry(ze);
                in = new FileInputStream(temFolder.getAbsolutePath() + String.format(pathPattern, path, REPORT_FILE));
                IOUtils.copy(in, stream);
                stream.closeEntry();
                IOUtils.closeQuietly(in);
            }
        } catch (IOException e){
            logger.error("Error ", e);
            throw new ServiceException("Error");
        }
    }
}
