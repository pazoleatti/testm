package com.aplana.sbrf.taxaccounting.service.impl.templateversion;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.templateversion.VersionOperatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
@Service(value = "formTemplateOperatingService")
@Transactional
public class VersionFTOperatingServiceImpl implements VersionOperatingService {

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };
    private static final String MSG_IS_USED_VERSION =
            "Существует экземпляр %s: ";
    private static final String MSG_HAVE_DESTINATION =
            "Существует назначение %s в качестве источника данных для %s типа: \"%s\" вида \"%s\" в подразделении \"%s\" начиная с периода %s!";

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
    private SourceService sourceService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

    private Calendar calendar = Calendar.getInstance();

    @Override
    public boolean isUsedVersion(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        List<Long> fdIds = formDataService.getFormDataListInActualPeriodByTemplate(templateId, versionActualDateStart);

        boolean result = false;
        for (long formDataId : fdIds) {
            FormData formData = formDataDao.getWithoutRows(formDataId);
            DepartmentReportPeriod drp = departmentReportPeriodService.get(formData.getDepartmentReportPeriodId());
            DepartmentReportPeriod drpCompare = formData.getComparativePeriodId() != null ?
                    departmentReportPeriodService.get(formData.getComparativePeriodId()) : null;

            logger.error(MessageGenerator.getFDMsg(
                    String.format(MSG_IS_USED_VERSION, MessageGenerator.mesSpeckPlural(formData.getFormType().getTaxType())),
                    formData,
                    departmentService.getDepartment(formData.getDepartmentId()).getName(),
                    formData.isManual(),
                    drp,
                    drpCompare));
            result = true;
        }
        return result;
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
                        if (compareResult == 5 || compareResult == 2 || compareResult == 0 || compareResult == -2 || compareResult == 7 ||
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
                            } else {
                                logger.info("Установлена дата окончания актуальности версии %td.%tm.%tY для предыдущей версии с датой начала %td.%tm.%tY",
                                        createActualizationDates(Calendar.DAY_OF_YEAR, -1, newIntersection.getBeginDate().getTime()),
                                        createActualizationDates(Calendar.DAY_OF_YEAR, -1, newIntersection.getBeginDate().getTime()),
                                        createActualizationDates(Calendar.DAY_OF_YEAR, -1, newIntersection.getBeginDate().getTime()),
                                        intersection.getBeginDate(), intersection.getBeginDate(), intersection.getBeginDate());
                                if (versionActualDateEnd != null){
                                    cleanVersions(templateId, typeId, status, versionActualDateStart, versionActualDateEnd, logger);
                                    Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd.getTime());
                                    FormTemplate formTemplate =  createFakeTemplate(date, typeId);
                                    formTemplateService.save(formTemplate);
                                }
                            }
                        }
                        break;
                    case FAKE:
                        compareResult = newIntersection.compareTo(intersection);
                        //Варианты 15,19,18a
                        if (compareResult == -2 || compareResult == -7 || compareResult == -16){
                            FormTemplate formTemplate = formTemplateService.get(intersection.getTemplateId());
                            formTemplate.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, 1, newIntersection.getEndDate().getTime()));
                            formTemplateService.save(formTemplate);
                        }
                        //Варианты 16,19,20,18a,10a,1a
                        else if (compareResult == 11 || compareResult == 5 || compareResult == -1 || compareResult == 10 || compareResult == 16){
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
    public void cleanVersions(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        if (templateId == 0)
            return;
        FormTemplate formTemplateFake = formTemplateService.getNearestFTRight(templateId,
                VersionedObjectStatus.NORMAL, VersionedObjectStatus.DRAFT, VersionedObjectStatus.FAKE);
        if (formTemplateFake != null && formTemplateFake.getStatus() == VersionedObjectStatus.FAKE)
            formTemplateService.delete(formTemplateFake.getId());
    }

    @Override
    public void checkDestinationsSources(int typeId, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        List<Pair<DepartmentFormType, Pair<Date, Date>>> sourcePairs = sourceService.findSourceFTsForFormType(typeId, versionActualDateStart, versionActualDateEnd);
        List<Pair<DepartmentFormType, Pair<Date, Date>>> destinationPairs = sourceService.findDestinationFTsForFormType(typeId, versionActualDateStart, versionActualDateEnd);
        FormType typeRelated = formTypeService.get(typeId);
        for (Pair<DepartmentFormType, Pair<Date, Date>> pair : sourcePairs){
            DepartmentFormType first = pair.getFirst();
            FormType typeSource = formTypeService.get(first.getFormTypeId());
            logger.error(
                    String.format(MSG_HAVE_DESTINATION,
                            MessageGenerator.mesSpeckPlural(typeRelated.getTaxType()),
                            MessageGenerator.mesSpeckPlural(typeSource.getTaxType()),
                            first.getKind().getTitle(),
                            typeSource.getName(),
                            departmentService.getDepartment(first.getDepartmentId()).getName(),
                            getPeriod(pair.getSecond())
                    )
            );
        }
        for (Pair<DepartmentFormType, Pair<Date, Date>> pair : destinationPairs){
            DepartmentFormType first = pair.getFirst();
            FormType typeTarget = formTypeService.get(first.getFormTypeId());
            logger.error(
                    String.format(MSG_HAVE_DESTINATION,
                            MessageGenerator.mesSpeckPlural(typeRelated.getTaxType()),
                            MessageGenerator.mesSpeckPlural(typeTarget.getTaxType()),
                            first.getKind().getTitle(),
                            typeTarget.getName(),
                            departmentService.getDepartment(first.getDepartmentId()).getName(),
                            getPeriod(pair.getSecond())
                    )
            );
        }
    }

    private String getPeriod(Pair<Date, Date> pair) {
        if (pair.getSecond() == null) {
            return String.format("\"%s\"", sdf.get().format(pair.getFirst()));
        } else {
            return String.format("\"%s\" до периода \"%s\"", sdf.get().format(pair.getFirst()), sdf.get().format(pair.getSecond()));
        }
    }

    @Override
    public void checkDestinationsSources(int typeId, Pair<Date,Date> beginRange, Pair<Date,Date> endRange, Logger logger) {
        if (beginRange != null)
            checkDestinationsSources(typeId, beginRange.getFirst(), beginRange.getSecond(), logger);
        if (endRange != null)
            checkDestinationsSources(typeId, endRange.getFirst(), endRange.getSecond(), logger);
    }

    private FormTemplate createFakeTemplate(Date date, int formTypeId){
        FormTemplate formTemplate =  new FormTemplate();
        formTemplate.setVersion(date);
        formTemplate.setStatus(VersionedObjectStatus.FAKE);
        formTemplate.setType(formTypeService.get(formTypeId));
        formTemplate.setName("fake");
        formTemplate.setFullName("fake");
        formTemplate.setHeader("fake");
        return formTemplate;
    }

    private Date createActualizationDates(int calendarTime, int time, long actualTemplateDate){
        calendar.clear();
        calendar.setTime(new Date(actualTemplateDate));
        calendar.add(calendarTime, time);
        return calendar.getTime();
    }
}
