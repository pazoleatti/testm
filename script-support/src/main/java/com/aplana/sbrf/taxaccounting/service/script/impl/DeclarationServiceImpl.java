package com.aplana.sbrf.taxaccounting.service.script.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.PeriodService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataCollection;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;

/*
 * author auldanov
 */

@Service("declarationService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeclarationServiceImpl implements DeclarationService, ScriptComponentContextHolder{

	private static final String DATE_FORMAT = "yyyyMMdd";

    private static final Long DEPARTMENT_PARAM_REF_BOOK_ID = 31L;
	
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

	@Override
	public DeclarationData find(int declarationTypeId, int departmentId, int reportPeriodId) {
		return declarationDataDao.find(declarationTypeId, departmentId, reportPeriodId);
	}

	@Override
	public String generateXmlFileId(int declarationTypeId, int departmentId, int reportPeriodId) {

        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String declarationPrefix = declarationTypeDao.get(declarationTypeId).getTaxType().getDeclarationPrefix();
		StringBuilder stringBuilder = new StringBuilder(declarationPrefix);
		RefBookDataProvider tmp = factory.getDataProvider(DEPARTMENT_PARAM_REF_BOOK_ID);
        Date startDate = periodService.getStartDate(reportPeriodId).getTime();
        List<Map<String, RefBookValue>> departmentParams = tmp.getRecords(startDate, null, String.format("DEPARTMENT_ID = %d", departmentId), null);
        Map<String, RefBookValue> departmentParam = departmentParams.get(0);

        Calendar calendar = Calendar.getInstance();
		stringBuilder.append('_' +
				departmentParam.get("TAX_ORGAN_CODE").toString() + '_' +
				departmentParam.get("TAX_ORGAN_CODE").toString() + '_' +
				departmentParam.get("INN").toString() + departmentParam.get("KPP").toString() + '_' +
				dateFormat.format(calendar.getTime()) + '_' +
				UUID.randomUUID().toString().toUpperCase());

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
        return declarationDataDao.getXmlData(declarationDataId);
    }

    @Override
	public void setScriptComponentContext(ScriptComponentContext context) {
		this.context = context;
	}

}
