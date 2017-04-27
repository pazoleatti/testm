package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.Translator;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateImpexService;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FormTemplateImpexServiceImpl implements
		FormTemplateImpexService {

    @Autowired
    DeclarationTemplateService declarationTemplateService;

	private static final String VERSION_FILE = "version";
	private static final String CONTENT_FILE = "content.xml";
	private static final String SCRIPT_FILE = "script.groovy";
	private static final String ROWS_FILE = "rows.xml";
	private static final String HEADERS_FILE = "headers.xml";
	private static final String ENCODING = "UTF-8";
    private static final String REG_EXP = "[^\\d\\sA-Za-z'-]";

    private static final Log LOG = LogFactory.getLog(FormTemplateImpexServiceImpl.class);

    private static final int MAX_NAME_OF_DIR = 50;

    private static final String DEC_TEMPLATES_FOLDER = "declaration";
    private static final String FORM_TEMPLATES_FOLDER = "form";

    private static final String TEMPLATE_OF_FOLDER_NAME =
            "%s" + File.separator + "%s-%s" + File.separator + "%s";
    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT_YEAR = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy");
        }
    };

    @Override
    public void exportAllTemplates(OutputStream stream) {
        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(stream);
        zipOutputStream.setEncoding("UTF-8");
        zipOutputStream.setFallbackToUTF8(true);
        exportDecTemplates(zipOutputStream);
        try {
            zipOutputStream.finish();
        } catch (IOException e) {
            LOG.error("", e);
            throw new ServiceException("", e);
        }
    }

    //Удаление временных паоок
    @SuppressWarnings("all")
    private void dirTempDelete(File directory){
        for (File file : directory.listFiles()){
            if (file.isDirectory()){
                dirTempDelete(file);
                if (!file.delete())
                    LOG.warn("Faild to delete directory " + file);
            } else {
                if (!file.delete())
                    LOG.warn("Faild to delete file " + file);
            }
        }
        if (directory.listFiles() == null || directory.listFiles().length==0){
            if (!directory.delete())
                LOG.warn("Faild to delete directory " + directory);
        }
    }

    private void exportDecTemplates(ZipArchiveOutputStream zipOutputStream){
        FileInputStream in;
        File temFolder;
        try {
            temFolder = File.createTempFile(DEC_TEMPLATES_FOLDER, "");
            if (!temFolder.delete()){
                LOG.error(String.format("Can't delete file %s for declarations with goal to create dir .",
						temFolder.getPath()));
            }
            if (!temFolder.mkdir())
                LOG.error("Can't create directory for declarations");
        } catch (IOException e) {
            throw new ServiceException("Ошибки при создании временной директории.");
        }
        try {

            List<DeclarationTemplate> declarationTemplates = declarationTemplateService.listAll();
            ArrayList<String> paths = new ArrayList<String>(declarationTemplates.size());
            for (DeclarationTemplate template : declarationTemplates){
                String translatedName = Translator.transliterate(template.getType().getName());
                String folderTemplateName =
                        Translator.transliterate(String.format(TEMPLATE_OF_FOLDER_NAME,
                                template.getType().getTaxType().name().toLowerCase(),
                                template.getType().getId(),
                                (translatedName.length() > MAX_NAME_OF_DIR
                                        ? translatedName.substring(0, MAX_NAME_OF_DIR).trim().replaceAll(REG_EXP,"")
                                        : translatedName.trim().replaceAll(REG_EXP,"")).trim(),
                                SIMPLE_DATE_FORMAT_YEAR.get().format(template.getVersion())));
                try {
                    File folderTemplate = new File(temFolder.getAbsolutePath() + File.separator + folderTemplateName, "");
                    folderTemplate.delete();
                    if (!folderTemplate.mkdirs())
                        LOG.warn(String.format("Can't create temporary directory %s", folderTemplate.getAbsolutePath()));
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

                    paths.add(folderTemplateName);
                } catch (IOException e) {
                    LOG.error("Ошибки при создании временной директории. Шаблон " + template.getName(), e);
                    throw new ServiceException("Ошибки при создании временной директории.");
                }
            }

            String pathPattern = File.separator + "%s" + File.separator + "%s";
            ZipArchiveEntry ze;
            for (String path : paths){
                // Version
                ze = new ZipArchiveEntry(DEC_TEMPLATES_FOLDER + String.format(pathPattern, path, DeclarationTemplateImpexServiceImpl.VERSION_FILE));
                zipOutputStream.putArchiveEntry(ze);
                in = new FileInputStream(temFolder.getAbsolutePath() + String.format(pathPattern, path, DeclarationTemplateImpexServiceImpl.VERSION_FILE));
				try {
					IOUtils.copy(in, zipOutputStream);
					zipOutputStream.closeArchiveEntry();
				} finally {
                	IOUtils.closeQuietly(in);
				}

                // Script
                ze = new ZipArchiveEntry(DEC_TEMPLATES_FOLDER + String.format(pathPattern, path, DeclarationTemplateImpexServiceImpl.SCRIPT_FILE));
                zipOutputStream.putArchiveEntry(ze);
                in = new FileInputStream(temFolder.getAbsolutePath() + String.format(pathPattern, path, DeclarationTemplateImpexServiceImpl.SCRIPT_FILE));
				try {
					IOUtils.copy(in, zipOutputStream);
					zipOutputStream.closeArchiveEntry();
				} finally {
                	IOUtils.closeQuietly(in);
				}

                // JasperTemplate
                ze = new ZipArchiveEntry(DEC_TEMPLATES_FOLDER + String.format(pathPattern, path, DeclarationTemplateImpexServiceImpl.REPORT_FILE));
                zipOutputStream.putArchiveEntry(ze);
                in = new FileInputStream(temFolder.getAbsolutePath() + String.format(pathPattern, path, DeclarationTemplateImpexServiceImpl.REPORT_FILE));
				try {
					IOUtils.copy(in, zipOutputStream);
					zipOutputStream.closeArchiveEntry();
				} finally {
					IOUtils.closeQuietly(in);
				}
            }
        } catch (IOException e){
            LOG.error("Error ", e);
            throw new ServiceException("Error");
        } finally {
            dirTempDelete(temFolder);
        }
    }
}
