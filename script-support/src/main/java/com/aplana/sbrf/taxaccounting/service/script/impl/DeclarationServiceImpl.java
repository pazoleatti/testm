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
            put(TaxType.INCOME, 33L);
            put(TaxType.TRANSPORT, 31L);
            put(TaxType.DEAL, 37L);
        }
    };

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
	public DeclarationData find(int declarationTypeId, int departmentId, int reportPeriodId) {
		return declarationDataDao.find(declarationTypeId, departmentId, reportPeriodId);
	}

	@Override
	public String generateXmlFileId(int declarationTypeId, int departmentId, int reportPeriodId) {

        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        TaxType declarationTaxType = declarationTypeDao.get(declarationTypeId).getTaxType();
        String declarationPrefix = declarationTaxType.getDeclarationPrefix();
		StringBuilder stringBuilder = new StringBuilder(declarationPrefix);

		RefBookDataProvider tmp = factory.getDataProvider(TAX_TYPE_TO_REF_BOOK_MAP.get(declarationTaxType));
        Date startDate = periodService.getStartDate(reportPeriodId).getTime();
        List<Map<String, RefBookValue>> departmentParams = tmp.getRecords(startDate, null, String.format("DEPARTMENT_ID = %d", departmentId), null);
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
	
	/**
	 * Возвращает список налоговых форм, являющихся источником для указанной декларации и находящихся в статусе
	 * "Создана"
	 *
	 * @param declarationData декларация
	 * @return список НФ-источников в статусе "Принята"
	 */
	@Override
	public FormDataCollection getAcceptedFormDataSources(DeclarationData declarationData) {
		int departmentId = declarationData.getDepartmentId();
		int declarationTemplateId = declarationData.getDeclarationTemplateId();
		int reportPeriodId = declarationData.getReportPeriodId();

		// Формирование списка НФ-источников в статусе "Принята"
		DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationTemplateId);
		List<DepartmentFormType> sourcesInfo = departmentFormTypeDao.getDeclarationSources(departmentId, declarationTemplate.getDeclarationType().getId());
		List<FormData> records = new ArrayList<FormData>();
		for (DepartmentFormType dft : sourcesInfo) {
			// В будущем возможны ситуации, когда по заданному сочетанию параметров будет несколько
			// FormData, в этом случае данный код нужно будет зарефакторить
			FormData formData = formDataDao.find(dft.getFormTypeId(), dft.getKind(), dft.getDepartmentId(), reportPeriodId);
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
        BlobData blobData = blobDataService.get(declarationDataDao.get(declarationDataId).getXlsxDataUuid());
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

}
