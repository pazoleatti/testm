package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * author auldanov
 */
@Service("declarationService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeclarationServiceImpl implements DeclarationService, ScriptComponentContextHolder{

	private static final String DATE_FORMAT = "yyyyMMdd";

    // Тип налога -> ID справочника с параметрами подразделения
    private static final Map<TaxType, Long> TAX_TYPE_TO_REF_BOOK_MAP = new HashMap<TaxType, Long>() {
        {
            put(TaxType.INCOME, RefBook.DEPARTMENT_CONFIG_INCOME);
            put(TaxType.TRANSPORT, RefBook.DEPARTMENT_CONFIG_TRANSPORT);
            put(TaxType.DEAL, RefBook.DEPARTMENT_CONFIG_DEAL);
            put(TaxType.VAT, RefBook.DEPARTMENT_CONFIG_VAT);
            put(TaxType.PROPERTY, RefBook.DEPARTMENT_CONFIG_PROPERTY);
        }
    };

    private static final int PROPERTY_DECLARATION_ID = 3;
    private static final int PROPERTY_AVANS_ID = 8;

    private ScriptComponentContext context;

	@Autowired
	DeclarationDataDao declarationDataDao;

	@Autowired
	DeclarationTypeDao declarationTypeDao;
	
	@Autowired
	private DepartmentFormTypeDao departmentFormTypeDao;
	
	@Autowired
	private FormDataDao formDataDao;
	
	@Autowired
	private DepartmentDao departmentDao;
	
	@Autowired
	private FormTypeDao formTypeDao;
	
	@Autowired
	DeclarationTemplateDao declarationTemplateDao;

    @Autowired
    private RefBookFactory factory;

    @Autowired
    private PeriodService periodService;

    @Autowired
    BlobDataService blobDataService;

    @Override
    public DeclarationData find(int declarationTypeId, int departmentReportPeriodId) {
        return declarationDataDao.find(declarationTypeId, departmentReportPeriodId);
    }

    @Override
    public DeclarationData getLast(int declarationTypeId, int departmentId, int reportPeriodId) {
        return declarationDataDao.getLast(declarationTypeId, departmentId, reportPeriodId);
    }

    @Override
	public String generateXmlFileId(int declarationTypeId, int departmentId, int reportPeriodId) {

        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        TaxType declarationTaxType = declarationTypeDao.get(declarationTypeId).getTaxType();
        String declarationPrefix = getDeclarationPrefix(declarationTypeId, declarationTaxType);
		StringBuilder stringBuilder = new StringBuilder(declarationPrefix);

		RefBookDataProvider tmp = factory.getDataProvider(TAX_TYPE_TO_REF_BOOK_MAP.get(declarationTaxType));
        Date startDate = periodService.getEndDate(reportPeriodId).getTime();
        List<Map<String, RefBookValue>> departmentParams = tmp.getRecords(addDayToDate(startDate, -1), null, String.format("DEPARTMENT_ID = %d", departmentId), null);

        if (departmentParams != null && !departmentParams.isEmpty()) {
            Map<String, RefBookValue> departmentParam = departmentParams.get(0);

            Calendar calendar = Calendar.getInstance();
            stringBuilder.append('_').
                    append(departmentParam.get("TAX_ORGAN_CODE").toString()).
                    append('_').
                    append(departmentParam.get("TAX_ORGAN_CODE").toString()).
                    append('_').
                    append(departmentParam.get("INN").toString()).
                    append(departmentParam.get("KPP").toString()).
                    append('_').
                    append(dateFormat.format(calendar.getTime())).
                    append('_').
                    append(UUID.randomUUID().toString().toUpperCase());

            return stringBuilder.toString();
        }
        return null;
	}

    private String getDeclarationPrefix(int declarationTypeId, TaxType declarationTaxType) {
        switch(declarationTypeId){
            case PROPERTY_DECLARATION_ID :
                return declarationTaxType.getDeclarationPrefix() + "UD";
            case PROPERTY_AVANS_ID :
                return declarationTaxType.getDeclarationPrefix() + "UR";
            default:
                return declarationTaxType.getDeclarationPrefix();
        }
    }

    @Override
	public FormDataCollection getAcceptedFormDataSources(DeclarationData declarationData) {
		int departmentId = declarationData.getDepartmentId();
		int declarationTemplateId = declarationData.getDeclarationTemplateId();
		int reportPeriodId = declarationData.getReportPeriodId();
        ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);

		// Формирование списка НФ-источников в статусе "Принята"
		DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationTemplateId);
		List<DepartmentFormType> sourcesInfo = departmentFormTypeDao.getDeclarationSources(departmentId, declarationTemplate.getType().getId(),
                reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
		List<FormData> records = new ArrayList<FormData>();
		for (DepartmentFormType dft : sourcesInfo) {
            // Ежемесячные формы не являются источниками для декларация, поэтому periodOrder = null
			FormData formData = formDataDao.getLast(dft.getFormTypeId(), dft.getKind(), dft.getDepartmentId(), reportPeriodId, null);
			if (formData != null) {
				if (formData.getState() != WorkflowState.ACCEPTED) {
					Department department = departmentDao.getDepartment(dft.getDepartmentId());
					FormType formType = formTypeDao.get(dft.getFormTypeId());
					context.getLogger().warn(
							"Форма-источник существует, но не может быть использована, так как еще не принята. Вид формы: \"%s\", тип формы: \"%s\", подразделение: \"%s\"",
							formType.getName(),
							dft.getKind().getName(),
							department.getName()
					);
				} else {
					records.add(formData);
				}
			}
		}
		FormDataCollection formDataCollection = new FormDataCollection();
		formDataCollection.setRecords(records);
		return formDataCollection;
	}

    @Override
    public String getXmlData(long declarationDataId) {
        BlobData blobData = blobDataService.get(declarationDataDao.get(declarationDataId).getXmlDataUuid());
        if(blobData == null){
            //если декларация еще не заполнена
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            IOUtils.copy(blobData.getInputStream(), byteArrayOutputStream);
        } catch (IOException e) {
            throw new ServiceException("Не удалось извлечь xml для скрипта.", e);
        }
        return new String(byteArrayOutputStream.toByteArray());
    }

    @Override
	public void setScriptComponentContext(ScriptComponentContext context) {
		this.context = context;
	}

    private Date addDayToDate(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }
}
