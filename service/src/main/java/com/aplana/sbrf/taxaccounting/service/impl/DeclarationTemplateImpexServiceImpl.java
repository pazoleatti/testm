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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
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
			ZipEntry ze = new ZipEntry(VERSION_FILE);
			zos.putNextEntry(ze);
			zos.write("1.0".getBytes());
			zos.closeEntry();
			
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
            }
		} catch (Exception e) {
			throw new ServiceException("Не удалось импортировать шаблон", e);
		}
	}
}
