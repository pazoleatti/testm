package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipInputStream;

/*
 * author auldanov
 */
@Service("declarationService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeclarationServiceImpl implements DeclarationService, ScriptComponentContextHolder{

	private static final String DATE_FORMAT = "yyyyMMdd";

    private static final String CHECK_UNIQUE_ERROR = "Декларация с заданными параметрами уже существует!";
    private static final String CHECK_UNIQUE_NOTIFICATION_ERROR = "Уведомление с заданными параметрами уже существует!";

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
	private DeclarationDataDao declarationDataDao;
	@Autowired
	private DeclarationDataService declarationDataService;
	@Autowired
    private DeclarationTypeDao declarationTypeDao;
	@Autowired
	private FormDataDao formDataDao;
	@Autowired
	private FormDataService formDataService;
	@Autowired
	private DepartmentDao departmentDao;
	@Autowired
	private FormTypeDao formTypeDao;
	@Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
    private DeclarationTemplateDao declarationTemplateDao;
    @Autowired
    private RefBookFactory factory;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;
    @Autowired
    private TAUserService taUserService;

    @Override
    public List<DeclarationData> find(int declarationTypeId, int departmentReportPeriodId) {
        return declarationDataDao.find(declarationTypeId, departmentReportPeriodId);
    }

    @Override
    public DeclarationData find(int declarationTypeId, int departmentReportPeriodId, String kpp, String taxOrganCode) {
        return declarationDataDao.find(declarationTypeId, departmentReportPeriodId, kpp, taxOrganCode);
    }

    @Override
    public DeclarationData getLast(int declarationTypeId, int departmentId, int reportPeriodId) {
        return declarationDataDao.getLast(declarationTypeId, departmentId, reportPeriodId);
    }

    @Override
	public String generateXmlFileId(int declarationTypeId, int departmentReportPeriodId, String taxOrganCode, String kpp) {
        return generateXmlFileId(declarationTypeId, departmentReportPeriodId, taxOrganCode, taxOrganCode, kpp);
    }

    @Override
	public String generateXmlFileId(int declarationTypeId, int departmentReportPeriodId, String taxOrganCodeProm, String taxOrganCode, String kpp) {

        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        TaxType declarationTaxType = declarationTypeDao.get(declarationTypeId).getTaxType();
        String declarationPrefix = getDeclarationPrefix(declarationTypeId, declarationTaxType);
		StringBuilder stringBuilder = new StringBuilder(declarationPrefix);

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(departmentReportPeriodId);

		RefBookDataProvider tmp = factory.getDataProvider(TAX_TYPE_TO_REF_BOOK_MAP.get(declarationTaxType));
        Date endDate = periodService.getEndDate(departmentReportPeriod.getReportPeriod().getId()).getTime();
        List<Map<String, RefBookValue>> departmentParams = tmp.getRecords(addDayToDate(endDate, -1), null, String.format("DEPARTMENT_ID = %d", departmentReportPeriod.getDepartmentId()), null);

        if (departmentParams != null && !departmentParams.isEmpty()) {
            Map<String, RefBookValue> departmentParam = departmentParams.get(0);

            Calendar calendar = Calendar.getInstance();
            if (declarationTaxType == TaxType.PROPERTY || declarationTaxType == TaxType.TRANSPORT || declarationTaxType == TaxType.INCOME) {
                stringBuilder.append('_').
                        append(taxOrganCodeProm).
                        append('_').
                        append(taxOrganCode).
                        append('_').
                        append(departmentParam.get("INN").toString()).
                        append(kpp).
                        append('_').
                        append(dateFormat.format(calendar.getTime())).
                        append('_').
                        append(UUID.randomUUID().toString().toUpperCase());
            } else {
                stringBuilder.append('_').
                        append(departmentParam.get("TAX_ORGAN_CODE_PROM").toString()).
                        append('_').
                        append(departmentParam.get("TAX_ORGAN_CODE").toString()).
                        append('_').
                        append(departmentParam.get("INN").toString()).
                        append(departmentParam.get("KPP").toString()).
                        append('_').
                        append(dateFormat.format(calendar.getTime())).
                        append('_').
                        append(UUID.randomUUID().toString().toUpperCase());
            }

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
		int reportPeriodId = declarationData.getReportPeriodId();

		// Формирование списка НФ-источников в статусе "Принята"
		List<DepartmentFormType> sourcesInfo = declarationDataService.getFormDataSources(declarationData, true, new Logger());
		List<FormData> records = new ArrayList<FormData>();

        DepartmentReportPeriodFilter filter = new DepartmentReportPeriodFilter();
        filter.setDepartmentIdList(Arrays.asList(departmentId));
        filter.setReportPeriodIdList(Arrays.asList(reportPeriodId));
        // Список всех отчетных периодов для пары отчетный период-подразделение
        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodDao.getListByFilter(filter);
        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(declarationData.getDepartmentReportPeriodId());
        Collections.sort(departmentReportPeriodList, new Comparator<DepartmentReportPeriod>() {
            @Override
            public int compare(DepartmentReportPeriod o1, DepartmentReportPeriod o2) {
                if (o1.getCorrectionDate() == null) {
                    return -1;
                }
                if (o2.getCorrectionDate() == null) {
                    return 1;
                }
                return o1.getCorrectionDate().compareTo(o2.getCorrectionDate());
            }
        });

		for (DepartmentFormType dft : sourcesInfo) {
			FormData formData = formDataDao.getLast(dft.getFormTypeId(), dft.getKind(), dft.getDepartmentId(), reportPeriodId, dft.getPeriodOrder());
			if (formData != null) {
				if (formData.getState() != WorkflowState.ACCEPTED) {
                    //TODO возможно перенести initFormTemplateParams внутрь функции
                    FormData prevFormDataCorrection = formDataService.getPreviousFormDataCorrection(formData, departmentReportPeriodList, departmentReportPeriod);
                    if (prevFormDataCorrection == null) {
                        Department department = departmentDao.getDepartment(dft.getDepartmentId());
                        FormType formType = formTypeDao.get(dft.getFormTypeId());
                        context.getLogger().warn(
                                "Форма-источник существует, но не может быть использована, так как еще не принята. Вид формы: \"%s\", тип формы: \"%s\", подразделение: \"%s\"",
                                formType.getName(),
                                dft.getKind().getTitle(),
                                department.getName()
                        );
                    } else {
                        FormTemplate formTemplate = formTemplateDao.get(prevFormDataCorrection.getFormTemplateId());
                        prevFormDataCorrection.initFormTemplateParams(formTemplate);
                        records.add(prevFormDataCorrection);
                    }
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
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipInputStream zipXmlIn = getZipInputStream(declarationDataId);
        if (zipXmlIn != null) {
            try {
                zipXmlIn.getNextEntry();
                IOUtils.copy(zipXmlIn, byteArrayOutputStream);
            } catch (IOException e) {
                throw new ServiceException("Не удалось извлечь xml для скрипта.", e);
            } finally {
                IOUtils.closeQuietly(zipXmlIn);
            }
        }
        return new String(byteArrayOutputStream.toByteArray());
    }

    @Override
    public ZipInputStream getXmlStream(long declarationDataId) {
        ZipInputStream zipXmlIn = getZipInputStream(declarationDataId);
        if (zipXmlIn != null) {
            try {
                zipXmlIn.getNextEntry();
                return zipXmlIn;
            } catch (IOException e) {
                throw new ServiceException("Не удалось получить поток xml для скрипта.", e);
            }
        }
        return null;
    }

    @Override
    public XMLStreamReader getXmlStreamReader(long declarationDataId) {
        ZipInputStream zipXmlIn = getZipInputStream(declarationDataId);
        if (zipXmlIn != null) {
            try {
                zipXmlIn.getNextEntry();
                return XMLInputFactory.newInstance().createXMLStreamReader(zipXmlIn);
            } catch (IOException e) {
                throw new ServiceException("Не удалось получить поток xml для скрипта.", e);
            } catch (XMLStreamException e) {
                throw new ServiceException("Не удалось получить поток xml для скрипта.", e);
            }
        }
        return null;
    }

    private ZipInputStream getZipInputStream(long declarationDataId) {
        InputStream zipXml = declarationDataService.getXmlDataAsStream(declarationDataId, taUserService.getSystemUserInfo());
        if (zipXml != null) {
            return new ZipInputStream(zipXml);
        }
        return null;
    }

    @Override
    public boolean checkExistDeclarationsInPeriod(int declarationTypeId, int reportPeriodId) {
        DeclarationDataFilter declarationFilter = new DeclarationDataFilter();
        // фильтр
        declarationFilter.setDeclarationTypeId(declarationTypeId);
        declarationFilter.setReportPeriodIds(Arrays.asList(reportPeriodId));
        declarationFilter.setTaxType(TaxType.INCOME);

        // пейджинг
        declarationFilter.setSearchOrdering(DeclarationDataSearchOrdering.ID);
        declarationFilter.setStartIndex(0);
        declarationFilter.setCountOfRecords(1);

        PagingResult<DeclarationDataSearchResultItem> result = declarationDataSearchService.search(declarationFilter);
        return (result != null && result.size() > 0);
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

    @Override
    public boolean checkUnique(DeclarationData declarationData, Logger logger) {
        DeclarationTemplate template = declarationTemplateDao.get(declarationData.getDeclarationTemplateId());
        DeclarationData existingDeclarationData = declarationDataDao.find(template.getType().getId(),
                declarationData.getDepartmentReportPeriodId(), declarationData.getKpp(), declarationData.getTaxOrganCode());
        // форма найдена
        if (existingDeclarationData != null) {
            logger.error(template.getType().getTaxType() == TaxType.DEAL ? CHECK_UNIQUE_NOTIFICATION_ERROR : CHECK_UNIQUE_ERROR);
            return false;
        }
        return true;
    }

    @Override
    public String getXmlDataFileName(long declarationDataId) {
        String fileName = declarationDataService.getXmlDataFileName(declarationDataId, taUserService.getSystemUserInfo());
        if (fileName != null) {
            return fileName.replace(".zip", ".xml");
        }
        return null;
    }
}