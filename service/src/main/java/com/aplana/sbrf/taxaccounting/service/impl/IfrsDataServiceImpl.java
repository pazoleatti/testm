package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.IfrsDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
public class IfrsDataServiceImpl implements IfrsDataService {

    @Autowired
    private IfrsDao ifrsDao;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    private FormTypeService formTypeService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private TAUserService userService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private PrintingService printingService;
    @Autowired
    private SourceService sourceService;

    @Override
    public void create(Integer reportPeriodId) {
        ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
        if (reportPeriod == null) {
            throw new ServiceException("Не найден отчетный период с id = %d.", reportPeriodId);
        }
        if (ifrsDao.get(reportPeriodId) == null) {
            ifrsDao.create(reportPeriodId);
        } else {
            throw new ServiceException("Отчетность для МСФО уже существует за данный период.");
        }
    }

    @Override
    public boolean check(Logger logger, Integer reportPeriodId) {
        ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);

        List<FormData> formDataList = formDataService.getIfrsForm(reportPeriodId);
        List<Integer> formTypeList = formTypeService.getIfrsFormTypes();
        List<FormData> notAcceptedFormDataList = new ArrayList<FormData>();

        for(FormData formData: formDataList) {
            if (!formData.getState().equals(WorkflowState.ACCEPTED)) {
                notAcceptedFormDataList.add(formData);
            }
            if (formTypeList.contains(formData.getFormType().getId())) {
                formTypeList.remove(Integer.valueOf(formData.getFormType().getId()));
            }
        }

        if (!formTypeList.isEmpty()) {
            logger.error("Следующие формы за %s %s, включаемые в архив с отчетностью для МСФО, не созданы:", reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear());
            for(Integer formType: formTypeList) {
                logger.error("Налоговая форма: Вид: \"%s\"", formTypeService.get(formType).getName());
            }
            return false;
        }

        if (!notAcceptedFormDataList.isEmpty()) {
            logger.error("Следующие формы за %s %s, включаемые в архив с отчетностью для МСФО, не находятся в состоянии \"Принята\":", reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear());
            for(FormData formData: notAcceptedFormDataList) {
                logger.error("Налоговая форма: Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\".", departmentService.getParentsHierarchy(formData.getDepartmentId()), formData.getKind().getName(), formData.getFormType().getName());
            }
            return false;
        }
        return true;
    }

    @Override
    public void calculate(Logger logger, Integer reportPeriodId) {
        ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ZipOutputStream zos = new ZipOutputStream(os);
            ZipEntry ze;

            List<FormData> formDataList = formDataService.getIfrsForm(reportPeriodId);
            if (formDataList.isEmpty()) {

            }

            List<Integer> formTypesList = formTypeService.getIfrsFormTypes();
            List<Integer> declarationTypesList = formTypeService.getIfrsFormTypes();

            List<Department> departments = departmentService.getAllChildren(0);
            Map<Integer, Department> departmentsMap = new HashMap<Integer, Department>();
            for(Department department: departments) {
                departmentsMap.put(department.getId(), department);
            }

            for(FormData formData: formDataList) {
                boolean flag = (formData.getId() != 11122);
                List<DepartmentFormType> departmentImpFormTypes = sourceService.getFormDestinations(formData.getDepartmentId(), formData.getFormType().getId(), formData.getKind(), reportPeriodId);
                for(DepartmentFormType departmentFormType: departmentImpFormTypes) {
                    if (formTypesList.contains(departmentFormType.getFormTypeId())) {
                        flag = false;
                        break;
                    }
                }

                if (!flag)
                    continue;

                List<DepartmentDeclarationType> departmentDeclarationTypes = sourceService.getDeclarationDestinations(formData.getDepartmentId(), formData.getFormType().getId(), formData.getKind(), reportPeriodId);
                for(DepartmentDeclarationType departmentDeclarationType: departmentDeclarationTypes) {
                    if (declarationTypesList.contains(departmentDeclarationType.getDeclarationTypeId())) {
                        flag = false;
                        break;
                    }
                }

                if (!flag)
                    continue;

                String uuid = reportService.get(userService.getSystemUserInfo(), formData.getId(), ReportType.EXCEL, false, false, false);
                if (uuid == null) {
                    uuid = printingService.generateExcel(userService.getSystemUserInfo(), formData.getId(), false, false, false);
                }

                BlobData blobData = blobDataService.get(uuid);
                FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
                Department department = departmentsMap.get(formData.getDepartmentId());
                String name = String.format("%s_%s_%s_%s_%s.xlsm", formTemplate.getType().getName(), department.getSbrfCode(), reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear(), formData.getKind().getId());
                ze = new ZipEntry(name);
                zos.putNextEntry(ze);
                zos.write(IOUtils.toByteArray(blobData.getInputStream()));
                zos.closeEntry();
            }

            zos.finish();
        } catch (Exception e) {
            throw new ServiceException("Не удалось сформировать отчетность для МСФО", e);
        }

        ifrsDao.update(reportPeriodId, blobDataService.create(new ByteArrayInputStream(os.toByteArray()), String.format("Отчетность для МСФО %s %s.zip", reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear())));
    }

    @Override
    public void update(IfrsData data) {
        ifrsDao.update(data.getReportPeriodId(), data.getBlobDataId());
    }

    @Override
    public IfrsData get(Integer reportPeriodId) {
        return ifrsDao.get(reportPeriodId);
    }

    @Override
    public PagingResult<IfrsDataSearchResultItem> findByReportPeriod(List<Integer> reportPeriodIds, PagingParams pagingParams) {
        return ifrsDao.findByReportPeriod(reportPeriodIds, pagingParams);
    }

    @Override
    public String generateTaskKey(Integer reportPeriod) {
        return LockData.LOCK_OBJECTS.IFRS.name() + "_" + reportPeriod;
    }
}
