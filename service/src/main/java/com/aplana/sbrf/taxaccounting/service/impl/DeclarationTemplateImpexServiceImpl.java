package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateImpexService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
    private static final String CONTENT_FILE = "content.xml";

	private final static String ENCODING = "UTF-8";

	@Override
	public void exportDeclarationTemplate(TAUserInfo userInfo, Integer id, OutputStream os) {
		try {
			ZipOutputStream zos = new ZipOutputStream(os);
            DeclarationTemplate dt = declarationTemplateService.get(id);
            dt.setCreateScript(declarationTemplateService.getDeclarationTemplateScript(id));
			
			// Version
			ZipEntry ze/* = new ZipEntry(VERSION_FILE)*/;
			/*zos.putNextEntry(ze);
			zos.write("1.0".getBytes());
			zos.closeEntry();*/

            ze = new ZipEntry(CONTENT_FILE);
            zos.putNextEntry(ze);
            DeclarationTemplateContent dtc = new DeclarationTemplateContent();
            dtc.fillDeclarationTemplateContent(dt);
            for (DeclarationSubreportContent declarationSubreportContent: dtc.getSubreports()) {
                if (declarationSubreportContent.getBlobDataId() != null) {
                    declarationSubreportContent.setFileName(blobDataService.get(declarationSubreportContent.getBlobDataId()).getName());
                }
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(DeclarationTemplateContent.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            jaxbMarshaller.marshal(dtc, zos);
            zos.closeEntry();

            for(DeclarationSubreport subreport: dt.getSubreports()) {
                if (subreport.getBlobDataId() != null) {
                    ze = new ZipEntry(subreport.getBlobDataId());
                    zos.putNextEntry(ze);
                    InputStream is = blobDataService.get(subreport.getBlobDataId()).getInputStream();
                    try {
                        IOUtils.copy(is, zos);
                    } finally {
                        IOUtils.closeQuietly(is);
                        zos.closeEntry();
                    }
                }
            }

			// Script
            String dtScript = dt.getCreateScript();
            if (dtScript != null){
                ze = new ZipEntry(SCRIPT_FILE);
                zos.putNextEntry(ze);
                zos.write(dtScript.getBytes(ENCODING));
                zos.closeEntry();
            }
			
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
            DeclarationTemplate dt = SerializationUtils.clone(declarationTemplateService.get(id));
            dt.setXsdId(null);
            dt.setJrxmlBlobId(null);
            dt.setCreateScript("");
            dt.setSubreports(new ArrayList<DeclarationSubreport>());
            Map<String, byte[]> files = new HashMap<String, byte[]>();
            DeclarationTemplateContent dtc = null;
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
                } else if (entry.getName().endsWith(".xsd")){
                    IOUtils.copy(zis, baos);
                    String uuid = blobDataService.create(new ByteArrayInputStream(baos.toByteArray()), entry.getName());
                    dt.setXsdId(uuid);
                } else if (entry.getName().equals(CONTENT_FILE)){
                    IOUtils.copy(zis, baos);
                    JAXBContext jaxbContext = JAXBContext.newInstance(DeclarationTemplateContent.class);
                    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                    dtc = (DeclarationTemplateContent) jaxbUnmarshaller.unmarshal(
                            new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()), ENCODING));
                } else {
                    baos = new ByteArrayOutputStream();
                    IOUtils.copy(zis, baos);
                    files.put(entry.getName(), baos.toByteArray());
                }
            }
            if (dtc != null) {
                if (dtc.getSubreports() != null)
                    for (DeclarationSubreportContent declarationSubreportContent : dtc.getSubreports()) {
                        if (declarationSubreportContent.getBlobDataId() != null && files.get(declarationSubreportContent.getBlobDataId()) != null) {
                            String uuid = blobDataService.create(new ByteArrayInputStream(files.get(declarationSubreportContent.getBlobDataId())), declarationSubreportContent.getFileName());
                            declarationSubreportContent.setBlobDataId(uuid);
                        }
                    }
                dtc.fillDeclarationTemplate(dt);
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
		} catch (Exception e) {
            throw new ServiceException("Не удалось импортировать шаблон", e);
        }
    }
}
