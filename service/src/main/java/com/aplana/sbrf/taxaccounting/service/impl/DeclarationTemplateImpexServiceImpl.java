package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
public class DeclarationTemplateImpexServiceImpl implements
		DeclarationTemplateImpexService {
	
	@Autowired
	DeclarationTemplateService declarationTemplateService;
    @Autowired
    BlobDataService blobDataService;

    public final static String VERSION_FILE = "version";
    public final static String SCRIPT_FILE = "script.groovy";
    public final static String REPORT_FILE = "report.jrxml";

	private final static String ENCODING = "UTF-8";

	@Override
	public void exportDeclarationTemplate(TAUserInfo userInfo, Integer id, OutputStream os) {
		try {
			ZipOutputStream zos = new ZipOutputStream(os);
            DeclarationTemplate dt = declarationTemplateService.get(id);
			
			// Version
			ZipEntry ze/* = new ZipEntry(VERSION_FILE)*/;
			/*zos.putNextEntry(ze);
			zos.write("1.0".getBytes());
			zos.closeEntry();*/
			
			// Script
			ze = new ZipEntry(SCRIPT_FILE);
			zos.putNextEntry(ze);
            String dtScript = dt.getCreateScript();
            if (dtScript != null)
			    zos.write(dtScript.getBytes(ENCODING));
			zos.closeEntry();
			
			// JasperTemplate
            BlobData jrxml = blobDataService.get(dt.getJrxmlBlobId());
            if (jrxml != null) {
                ze = new ZipEntry(REPORT_FILE);
                zos.putNextEntry(ze);
                IOUtils.copy(jrxml.getInputStream(), zos);
                zos.closeEntry();
            }

            //Xsd
            BlobData xsd = blobDataService.get(dt.getXsdId());
            if (xsd!=null){
                ze = new ZipEntry(xsd.getName());
                zos.putNextEntry(ze);
                IOUtils.copy(xsd.getInputStream(), zos);
                zos.closeEntry();
            }

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
            DeclarationTemplate dt = declarationTemplateService.get(id);
            dt.setXsdId(null);
            dt.setJrxmlBlobId(null);
            dt.setCreateScript("");
            while((entry = zis.getNextEntry())!=null){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (entry.getSize() == 0){
                    throw new ServiceException("Файл " + entry.getName() + " не должен быть пустой");
                }
                if (entry.getName().endsWith(".groovy")) {
                    IOUtils.copy(zis, baos);
                    dt.setCreateScript(new String(baos.toByteArray(), 0, baos.toByteArray().length, Charset.forName("UTF-8")));
                } else if (entry.getName().endsWith(".jrxml")){
                    IOUtils.copy(zis, baos);
                    String uuid = blobDataService.create(new ByteArrayInputStream(baos.toByteArray()), entry.getName());
                    dt.setJrxmlBlobId(uuid);
                }else if (entry.getName().endsWith(".xsd")){
                    IOUtils.copy(zis, baos);
                    String uuid = blobDataService.create(new ByteArrayInputStream(baos.toByteArray()), entry.getName());
                    dt.setXsdId(uuid);
                }
            }
            return dt;
			
            /*if ("1.0".equals(version)){
            	DeclarationTemplate dt = declarationTemplateService.get(id);

				if (files.get("script.groovy").length != 0) {
                    byte[] bytes = files.get("script.groovy");
					dt.setCreateScript(new String(bytes, 0, bytes.length, Charset.forName("UTF-8")));
				}
				if (files.get("report.jrxml").length != 0) {
                    String uuid = blobDataService.create(new ByteArrayInputStream(files.get("report.jrxml")), "report.jrxml");
                    dt.setJrxmlBlobId(uuid);
				}
                return dt;
            } else {
            	throw new ServiceException("Версия файла для импорта не поддерживается: " + version);
            }*/
		} catch (IOException e) {
            throw new ServiceException("Не удалось импортировать шаблон", e);
        }
    }
}
