package com.aplana.sbrf.taxaccounting.service.impl;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateImpexService;

@Service
@Transactional
public class DeclarationTemplateImpexServiceImpl implements
		DeclarationTemplateImpexService {

	@Autowired
	DeclarationTemplateDao declarationTemplateDao;

	@Override
	public void exportDeclarationTemplate(TAUserInfo userInfo, Integer id, OutputStream os) {

		try {

			DeclarationTemplate dt = declarationTemplateDao.get(id);


			ZipOutputStream zout = new ZipOutputStream(os);
			DataOutputStream zoutData = new DataOutputStream(zout);
			
			// Version
			ZipEntry ze = new ZipEntry("version");
			zout.putNextEntry(ze);
			zoutData.write("1.0".getBytes());
			zoutData.flush();
			zout.closeEntry();
			
			// Script
			ze = new ZipEntry("script.groovy");
			zout.putNextEntry(ze);
			zoutData.write(dt.getCreateScript().getBytes());
			zoutData.flush();
			zout.closeEntry();
			
			// JasperTemplate
			ze = new ZipEntry("report.jrxml");
			zout.putNextEntry(ze);
			zoutData.write(declarationTemplateDao.getJrxml(id).getBytes());
			zoutData.flush();
			zout.closeEntry();
			
			
			// content
			ze = new ZipEntry("content.xml");
			zout.putNextEntry(ze);
			zoutData.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><declarationTemplate></declarationTemplate>".getBytes());
			zoutData.flush();
			zout.closeEntry();
			
			
			zout.finish();

		} catch (Exception e) {
			throw new ServiceException("Неудалось экспортировать шаблон", e);
		}

	}

	@Override
	public void importDeclarationTemplate(TAUserInfo userInfo, Integer id,
			InputStream is) {
		// TODO Auto-generated method stub

	}

}
