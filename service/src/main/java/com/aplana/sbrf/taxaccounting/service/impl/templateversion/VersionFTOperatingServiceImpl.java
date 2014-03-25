package com.aplana.sbrf.taxaccounting.service.impl.templateversion;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.templateversion.VersionOperatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
@Service(value = "formTemplateOperatingService")
@Transactional
public class VersionFTOperatingServiceImpl implements VersionOperatingService {

    public static final String MSG_IS_USED_VERSION = "Существует экземпляр налоговой формы типа \"%s\" в подразделении \"%s\" периоде %s для макета!";

    @Autowired
    private FormDataDao formDataDao;

    @Autowired
    private FormTypeService formTypeService;

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PeriodService periodService;

    private Calendar calendar = Calendar.getInstance();

    @Override
    public void isUsedVersion(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        if (status == VersionedObjectStatus.DRAFT)
            return;
        List<Long> fdIds = formDataService.getFormDataListInActualPeriodByTemplate(templateId, versionActualDateStart);
        if (!fdIds.isEmpty()){
            for(Long id: fdIds) {
                FormData formData = formDataDao.getWithoutRows(id);
                Department department = departmentService.getDepartment(formData.getDepartmentId());
                ReportPeriod period = periodService.getReportPeriod(formData.getReportPeriodId());

                logger.error(MSG_IS_USED_VERSION, formData.getFormType().getName(), department.getName(), period.getName() + " " + period.getTaxPeriod().getYear());
            }
        }

    }

    @Override
    public void isCorrectVersion(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void isIntersectionVersion(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        //1 Шаг. Система проверяет пересечение с периодом актуальности хотя бы одной версии этого же макета, STATUS которой не равен -1.

        List<VersionSegment> segmentIntersections =
                formTemplateService.findFTVersionIntersections(templateId, typeId, versionActualDateStart, versionActualDateEnd);
        if (!segmentIntersections.isEmpty()){
            VersionSegment newIntersection = new VersionSegment();
            newIntersection.setBeginDate(versionActualDateStart);
            newIntersection.setEndDate(versionActualDateEnd);
            newIntersection.setTemplateId(templateId);
            newIntersection.setStatus(status);
            for (VersionSegment intersection : segmentIntersections){
                int compareResult;
                switch (intersection.getStatus()){
                    case NORMAL:
                    case DRAFT:
                        compareResult = newIntersection.compareTo(intersection);
                        //Варианты 1, 2, 3, 4, 5, 6, 9, 10, 1a, 2a, 3a
                        if (compareResult == 2 || compareResult == 0 ||compareResult == -2 || compareResult == 7 ||
                                compareResult == -7 || compareResult == -1 || compareResult == 16 || compareResult == -11 ||
                                compareResult == 11 || compareResult == -16 || compareResult == 10){
                            logger.error("Обнаружено пересечение указанного срока актуальности с существующей версией");
                            return;
                        }
                        // Варианты 7,8
                        else if(compareResult == 1 || compareResult == -5){
                            isUsedVersion(intersection.getTemplateId(), intersection.getTypeId(), intersection.getStatus(), newIntersection.getBeginDate(),
                                    intersection.getEndDate(), logger);
                            if (logger.containsLevel(LogLevel.ERROR)){
                                logger.error("Обнаружено пересечение указанного срока актуальности с существующей версией");
                                return;
                            } else
                                logger.info("Установлена дата окончания актуальности версии %td.%tm.%tY для предыдущей версии %d",
                                        createActualizationDates(Calendar.DAY_OF_YEAR, -1, newIntersection.getBeginDate().getTime()),
                                        createActualizationDates(Calendar.DAY_OF_YEAR, -1, newIntersection.getBeginDate().getTime()),
                                        createActualizationDates(Calendar.DAY_OF_YEAR, -1, newIntersection.getBeginDate().getTime()),
                                        intersection.getTemplateId());
                        }
                        break;
                    case FAKE:
                        compareResult = newIntersection.compareTo(intersection);
                        //Варианты 15
                        if (compareResult == -2){
                            FormTemplate formTemplate = formTemplateService.get(intersection.getTemplateId());
                            formTemplate.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, 1, newIntersection.getEndDate().getTime()));
                            formTemplateService.save(formTemplate);
                        }
                        //Варианты 16,19,20,18a
                        else if (compareResult == 5 || compareResult == -7 || compareResult == -1 || compareResult == -16 || compareResult == 10
                                || compareResult == 16){
                            formTemplateService.delete(intersection.getTemplateId());
                        }
                        break;
                }
            }
        }
        //2 Шаг. Система проверяет наличие даты окончания актуальности.
        //  Пересечений нет
        else if(versionActualDateEnd != null){
            cleanVersions(templateId, typeId, status, versionActualDateStart, versionActualDateEnd, logger);
            Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd.getTime());
            FormTemplate formTemplate =  createFakeTemplate(date, typeId);
            formTemplateService.save(formTemplate);
        }

    }

    @Override
    public void createNewVersion(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        isIntersectionVersion(templateId, typeId, status, versionActualDateStart, versionActualDateEnd, logger);
    }

    @Override
    public void cleanVersions(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        if (templateId == 0)
            return;
        FormTemplate formTemplateFake = formTemplateService.getNearestFTRight(templateId, VersionedObjectStatus.FAKE);
        if (formTemplateFake != null)
            formTemplateService.delete(templateId);
    }

    private FormTemplate createFakeTemplate(Date date, int formTypeId){
        FormTemplate formTemplate =  new FormTemplate();
        formTemplate.setVersion(date);
        formTemplate.setStatus(VersionedObjectStatus.FAKE);
        formTemplate.setEdition(0);
        formTemplate.setType(formTypeService.get(formTypeId));
        formTemplate.setName("fake");
        formTemplate.setFullName("fake");
        formTemplate.setCode("fake");
        return formTemplate;
    }

    private Date createActualizationDates(int calendarTime, int time, long actualTemplateDate){
        calendar.clear();
        calendar.setTime(new Date(actualTemplateDate));
        calendar.add(calendarTime, time);
        return calendar.getTime();
    }
}
