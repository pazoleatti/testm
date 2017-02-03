package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.core.api.LockStateLogger;
import com.aplana.sbrf.taxaccounting.dao.IfrsDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

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
    private DeclarationDataSearchService declarationDataSearchService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private DeclarationTypeService declarationTypeService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ReportService reportService;
    @Autowired
    private TAUserService userService;
    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private LockDataService lockService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TARoleService roleService;

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

        List<Integer> formTypeList = formTypeService.getIfrsFormTypes();
        List<Integer> declarationTypeList = declarationTypeService.getIfrsDeclarationTypes();
        if (formTypeList.isEmpty() && declarationTypeList.isEmpty()) {
            logger.error("Отсутствуют макеты налоговых форм/деклараций с признаком \"Отчетность для МСФО\"");
            return false;
        }

        List<FormData> formDataList = formDataService.getIfrsForm(reportPeriodId);
        List<DeclarationData> declarationDataList = declarationDataSearchService.getIfrs(reportPeriodId);

        List<FormData> notAcceptedFormDataList = new ArrayList<FormData>();
        List<DeclarationData> notAcceptedDeclarationDataList = new ArrayList<DeclarationData>();

        List<FormData> notReportFormDataList = new ArrayList<FormData>();
        List<DeclarationData> notReportDeclarationDataList = new ArrayList<DeclarationData>();

        for(FormData formData: formDataList) {
            if (!formData.getState().equals(WorkflowState.ACCEPTED)) {
                notAcceptedFormDataList.add(formData);
                formTypeList.remove(Integer.valueOf(formData.getFormType().getId()));
            } else {
                if (formTypeList.contains(formData.getFormType().getId())) {
                    formTypeList.remove(Integer.valueOf(formData.getFormType().getId()));
                }
                if (reportService.get(userService.getSystemUserInfo(), formData.getId(), FormDataReportType.EXCEL, true, formData.isManual(), true) == null)
                    notReportFormDataList.add(formData);
            }
        }

        for(DeclarationData declarationData: declarationDataList) {
            if (!declarationData.getState().equals(State.ACCEPTED)) {
                notAcceptedDeclarationDataList.add(declarationData);
            } else {
                DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
                if (declarationTypeList.contains(declarationTemplate.getType().getId())) {
                    declarationTypeList.remove(Integer.valueOf(declarationTemplate.getType().getId()));
                }
                if (reportService.getDec(userService.getSystemUserInfo(), declarationData.getId(), DeclarationDataReportType.EXCEL_DEC) == null) {
                    notReportDeclarationDataList.add(declarationData);
                }
            }
        }

        if (!formTypeList.isEmpty() || !declarationTypeList.isEmpty()) {
            logger.error("Следующие формы за %s %s, включаемые в архив с отчетностью для МСФО, не созданы:", reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear());
            for(Integer formType: formTypeList) {
                logger.error("Налоговая форма: Вид: \"%s\"", formTypeService.get(formType).getName());
            }
            for(Integer declarationType: declarationTypeList) {
                logger.error("Декларация: Вид: \"%s\"", declarationTypeService.get(declarationType).getName());
            }
            return false;
        }

        if (!notAcceptedFormDataList.isEmpty() || !notAcceptedDeclarationDataList.isEmpty()) {
            logger.error("Следующие формы за %s %s, включаемые в архив с отчетностью для МСФО, не находятся в состоянии \"Принята\":", reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear());
            for(FormData formData: notAcceptedFormDataList) {
                logger.error("Налоговая форма: Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\".", departmentService.getParentsHierarchy(formData.getDepartmentId()), formData.getKind().getTitle(), formData.getFormType().getName());
            }
            for(DeclarationData declarationData: notAcceptedDeclarationDataList) {
                DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
                logger.error("Декларация: Подразделение: \"%s\", Вид: \"%s\".", departmentService.getParentsHierarchy(declarationData.getDepartmentId()), declarationTemplate.getName());
            }
            return false;
        }
        if (!notReportFormDataList.isEmpty() || !notReportDeclarationDataList.isEmpty()) {
            logger.error("Для следующих форм за %s %s, еще не созданы отчетные формы, включаемые в архив с отчетностью для МСФО:", reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear());
            for(FormData formData: notReportFormDataList) {
                logger.error("Налоговая форма: Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\".", departmentService.getParentsHierarchy(formData.getDepartmentId()), formData.getKind().getTitle(), formData.getFormType().getName());
            }
            for(DeclarationData declarationData: notReportDeclarationDataList) {
                DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
                logger.error("Декларация: Подразделение: \"%s\", Вид: \"%s\".", departmentService.getParentsHierarchy(declarationData.getDepartmentId()), declarationTemplate.getName());
            }
            return false;
        }

        return true;
    }

    @Override
    public void calculate(Logger logger, Integer reportPeriodId, LockStateLogger stateLogger) {
        if (!check(logger, reportPeriodId)) {
            throw new ServiceException("");
        }
        ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(os);
        zos.setEncoding("cp866");
        try {
            ZipArchiveEntry ze;

            stateLogger.updateState("Получение налоговых форм для архива");
            List<FormData> formDataList = formDataService.getIfrsForm(reportPeriodId);
            stateLogger.updateState("Получение деклараций для архива");
            List<DeclarationData> declarationDataList = declarationDataSearchService.getIfrs(reportPeriodId);

            List<Department> departments = departmentService.getAllChildren(0);
            Map<Integer, Department> departmentsMap = new HashMap<Integer, Department>();
            for(Department department: departments) {
                departmentsMap.put(department.getId(), department);
            }

            stateLogger.updateState("Добавление налоговых форм в архив");
            for(FormData formData: formDataList) {
                String uuid = reportService.get(userService.getSystemUserInfo(), formData.getId(), FormDataReportType.EXCEL, true, formData.isManual(), true);
                BlobData blobData = blobDataService.get(uuid);
                FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
                Department department = departmentsMap.get(formData.getDepartmentId());
                String name = String.format("%s_%s_%s_%s.xlsm", formTemplate.getType().getIfrsName(), department.getSbrfCode(), reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear());
                ze = new ZipArchiveEntry (name);
                zos.putArchiveEntry(ze);
                zos.write(IOUtils.toByteArray(blobData.getInputStream()));
                zos.closeArchiveEntry();
            }

            stateLogger.updateState("Добавление деклараций в архив");
            for(DeclarationData declarationData: declarationDataList) {
                DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
                String uuid = reportService.getDec(userService.getSystemUserInfo(), declarationData.getId(), DeclarationDataReportType.EXCEL_DEC);
                BlobData blobData = blobDataService.get(uuid);
                String name = String.format("%s_%s_%s.xlsx", declarationTemplate.getType().getIfrsName(), reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear());
                ze = new ZipArchiveEntry(name);
                zos.putArchiveEntry(ze);
                zos.write(IOUtils.toByteArray(blobData.getInputStream()));
                zos.closeArchiveEntry();
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Не удалось сформировать отчетность для МСФО", e);
        } finally {
            IOUtils.closeQuietly(zos);
            IOUtils.closeQuietly(os);
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
        return LockData.LockObjects.IFRS.name() + "_" + reportPeriod;
    }

    @Override
    public void cancelTask(FormData formData, TAUserInfo userInfo) {
        String key = generateTaskKey(formData.getReportPeriodId());
        List<Integer> usersList = lockService.getUsersWaitingForLock(key);
        lockService.unlock(key, userInfo.getUser().getId(), true);
        ReportPeriod reportPeriod = periodService.getReportPeriod(formData.getReportPeriodId());
        FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
        Department department = departmentService.getDepartment(formData.getDepartmentId());

        String msg = String.format("Отменено формирование архива с отчетностью для МСФО за %s %s, так как распринят экземпляр налоговой формы с отчетом для МСФО: Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\"",
                        reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear(), department.getName(), formData.getKind().getTitle(), formTemplate.getName());
        sendNotification(usersList, msg);
    }

    @Override
    public void deleteReport(FormData formData, TAUserInfo userInfo) {
        ifrsDao.update(formData.getReportPeriodId(), null);
        ReportPeriod reportPeriod = periodService.getReportPeriod(formData.getReportPeriodId());
        FormTemplate formTemplate = formTemplateService.get(formData.getFormTemplateId());
        Department department = departmentService.getDepartment(formData.getDepartmentId());

        String msg = String.format("Удален архив с отчетностью для МСФО за %s %s, так как распринят экземпляр налоговой формы с отчетом для МСФО: Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\"",
                reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear(), department.getName(), formData.getKind().getTitle(), formTemplate.getName());
        sendNotification(getIfrsUsers(), msg);
    }

    @Override
    public void cancelTask(DeclarationData declarationData, TAUserInfo userInfo) {
        String key = generateTaskKey(declarationData.getReportPeriodId());
        List<Integer> usersList = lockService.getUsersWaitingForLock(key);
        lockService.unlock(key, userInfo.getUser().getId(), true);
        ReportPeriod reportPeriod = periodService.getReportPeriod(declarationData.getReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());

        String msg = String.format("Отменено формирование архива с отчетностью для МСФО за %s %s, так как распринят экземпляр декларации с отчетом для МСФО: Подразделение: \"%s\", Вид: \"%s\"",
                reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear(), department.getName(), declarationTemplate.getName());
        sendNotification(usersList, msg);
    }

    @Override
    public void deleteReport(DeclarationData declarationData, TAUserInfo userInfo) {
        ifrsDao.update(declarationData.getReportPeriodId(), null);
        ReportPeriod reportPeriod = periodService.getReportPeriod(declarationData.getReportPeriodId());
        DeclarationTemplate declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
        Department department = departmentService.getDepartment(declarationData.getDepartmentId());

        String msg = String.format("Удален архив с отчетностью для МСФО за %s %s, так как распринят экземпляр декларации с отчетом для МСФО: Подразделение: \"%s\", Вид: \"%s\"",
                reportPeriod.getName(), reportPeriod.getTaxPeriod().getYear(), department.getName(), declarationTemplate.getName());
        sendNotification(getIfrsUsers(), msg);
    }

    void sendNotification(List<Integer> usersList, String msg) {
        if (!usersList.isEmpty()) {
            List<Notification> notifications = new ArrayList<Notification>();
            for (Integer userId : usersList) {
                Notification notification = new Notification();
                notification.setUserId(userId);
                notification.setCreateDate(new Date());
                notification.setText(msg);
                notification.setLogId(null);
                notifications.add(notification);
            }
            notificationService.saveList(notifications);
        }
    }

    @Override
    public List<Integer> getIfrsUsers() {
        List<Integer> usersList = new ArrayList<Integer>();
        MembersFilterData membersFilterData = new MembersFilterData() {{
            setRoleIds(Arrays.asList((long) roleService.getByAlias(TARole.ROLE_CONTROL_UNP).getId()));
        }};
        List<TAUserView> unpList = userService.getUsersByFilter(membersFilterData);
        for (TAUserView userView : unpList) {
            usersList.add(userView.getId());
        }
        return usersList;
    }

    @Override
    public void delete(List<Integer> reportPeriodIds) {
        try {
            ifrsDao.delete(reportPeriodIds);
        } catch (Exception e) {
            throw new ServiceException(e.getLocalizedMessage(), e);
        }
    }
}
