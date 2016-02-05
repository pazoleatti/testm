package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.Translator;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * User: avanteev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("FormTemplateImpexpServiceImplTest.xml")
public class FormTemplateImpexpServiceImplTest {

    @Autowired
    FormTemplateService formTemplateService;
    @Autowired
    FormTemplateImpexService formTemplateImpexService;
    @Autowired
    DeclarationTemplateService declarationTemplateService;
    @Autowired
    FormTemplateDao formTemplateDao;

    private static String folder = "Архив-зип";
    private static String zipName = "zip_file";
    private List<String> files;

    private static int MAX_DIRS = 3;

    @Test
    @SuppressWarnings("all")
    public void testZip() throws IOException {
        File tempFolder = File.createTempFile(Translator.transliterate(folder), "");
        tempFolder.delete();
        tempFolder.mkdir();
        files = new ArrayList<String>(10);

        for (int i = 0; i < 10; i++){
            File file = new File(Translator.transliterate(tempFolder.getAbsolutePath() + File.separator +
                    "(РНУ-102) Регистр " + File.separator + "Налог"));
            file.delete();
            file.mkdirs();
        }

        for (int i = 0; i < 10; i++)
            files.add(File.createTempFile(tempFolder.getName() + File.separator + "name", ".xml").getName());

        File tmpZipFile = File.createTempFile(zipName, ".zip");
        FileOutputStream fileOutputStream = new FileOutputStream(tmpZipFile);
        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(fileOutputStream);
        zipOutputStream.setEncoding("UTF8");
        zipOutputStream.setUseLanguageEncodingFlag(true);
        FileInputStream in = null;
        for (String fileName : files){
            ZipArchiveEntry ze = new ZipArchiveEntry(Translator.transliterate(folder + File.separator + fileName));
            zipOutputStream.putArchiveEntry(ze);
            try {
                in = new FileInputStream(tempFolder.getAbsolutePath() + File.separator + fileName);
                IOUtils.copy(in, zipOutputStream);
            } finally {
                in.close();
            }
            zipOutputStream.closeArchiveEntry();
        }

        zipOutputStream.finish();
        fileOutputStream.close();
        dirDelete(tempFolder);
        tmpZipFile.delete();
    }

    @Test
    public void exportTemplatesAll() throws IOException {
        //Data prepare
        Calendar calendar = Calendar.getInstance();
        ArrayList<FormTemplate> formTemplates = new ArrayList<FormTemplate>(100);
        ArrayList<DeclarationTemplate> declarationTemplates = new ArrayList<DeclarationTemplate>(100);
        for (int i=0; i<MAX_DIRS; i++){
            FormTemplate formTemplate = new FormTemplate();
            formTemplate.setFullName("full_name");
            formTemplate.setHeader("header");
            formTemplate.setName("name");
            formTemplate.setStatus(VersionedObjectStatus.NORMAL);
            formTemplate.setId(1);
            formTemplate.setScript("create script");
            FormType type = new FormType();
            type.setName("(РНУ-12) Регистр налогового учета расходов по хозяйственным операциям и оказанным Банку услугам ");
            type.setTaxType(TaxType.INCOME);
            formTemplate.setType(type);
            formTemplate.getColumns().add(new StringColumn());
            formTemplate.getStyles().add(new FormStyle());
            calendar.set(Calendar.YEAR, i);
            formTemplate.setVersion(calendar.getTime());
            formTemplates.add(formTemplate);
        }

        for (int i=0; i<MAX_DIRS; i++){
            DeclarationTemplate declarationTemplate = new DeclarationTemplate();
            declarationTemplate.setXsdId("xsd_id");
            declarationTemplate.setId(1);
            declarationTemplate.setStatus(VersionedObjectStatus.NORMAL);
            declarationTemplate.setCreateScript("create script");
            declarationTemplate.setJrxmlBlobId("jrxml_blob");
            declarationTemplate.setName("name");
            DeclarationType type = new DeclarationType();
            type.setName(TaxType.INCOME.getName());
            type.setTaxType(TaxType.INCOME);
            declarationTemplate.setType(type);
            calendar.set(Calendar.YEAR, i);
            declarationTemplate.setVersion(calendar.getTime());
            declarationTemplates.add(declarationTemplate);
        }

        when(formTemplateService.listAll()).thenReturn(formTemplates);
        when(declarationTemplateService.listAll()).thenReturn(declarationTemplates);
        //when(formTemplateDao.getFormTemplateScript(1)).thenReturn("create script");
        when(declarationTemplateService.getDeclarationTemplateScript(1)).thenReturn("create script");

        File tmp = File.createTempFile("test","");
        if (!tmp.delete())
            System.out.println(String.format("File %s not deleted ", tmp.getPath()));
        if (!tmp.mkdir())
            System.out.println(String.format("Can't create temp dir %s", tmp.getPath()));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        formTemplateImpexService.exportAllTemplates(byteArrayOutputStream);
        tmp.delete();
    }

    @SuppressWarnings("all")
    private void dirDelete(File directory) throws FileNotFoundException {
        assert  directory != null && directory.listFiles()!= null;
        for (File file : directory.listFiles()){
            if (file.isDirectory()){
                dirDelete(file);
                if (!file.delete());
            } else {
                if (!file.delete())
                    throw new FileNotFoundException("");
            }
        }
        if (directory.listFiles() == null || directory.listFiles().length==0){
            if (!directory.delete()){
                System.out.println("Cant't delete dir");
            }
        }
    }
}
