package com.aplana.sbrf.taxaccounting.service.impl.templateversion;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
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
@Service("declarationTemplateOperatingService")
@Transactional
public class VersionDTOperatingServiceImpl implements VersionOperatingService {

    public static final String MSG_IS_USED_VERSION =
            "Существует экземпляр декларации для макета:";
    private static final String MSG_HAVE_DESTINATION =
            "Существует назначение налоговой формы в качестве источника данных для декларации вида \"%s\" в подразделении \"%s\" начиная с периода %s!";
    private static final String MSG_HAVE_SOURCE =
            "Существует назначение декларации в качестве приёмника данных для %s типа \"%s\" вида \"%s\" в подразделении \"%s\" начиная с периода %s!";

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };
    @Autowired
    private DeclarationDataDao declarationDataDao;
    @Autowired
    private DeclarationTypeService declarationTypeService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private FormTypeService formTypeService;

    private Calendar calendar = Calendar.getInstance();

    @Override
    public void isUsedVersion(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        List<Long> ddIds = declarationDataService.getFormDataListInActualPeriodByTemplate(templateId, versionActualDateStart);
        for (long declarationId : ddIds) {
            DeclarationData declarationData = declarationDataDao.get(declarationId);
            DepartmentReportPeriod drp = departmentReportPeriodService.get(declarationData.getDepartmentReportPeriodId());

            logger.error(MessageGenerator.getDDMsg(MSG_IS_USED_VERSION,
                    declarationTemplateService.get(declarationData.getDeclarationTemplateId()).getType().getName(),
                    departmentService.getDepartment(declarationData.getDepartmentId()).getName(),
                    drp,
                    declarationData.getTaxOrganCode(),
                    declarationData.getKpp()));
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
                declarationTemplateService.findFTVersionIntersections(templateId, typeId, versionActualDateStart, versionActualDateEnd);
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
                        if (compareResult == 5 || compareResult == 2 || compareResult == 0 ||compareResult == -2 || compareResult == 7 ||
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
                                    Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd.getTime());
                                    cleanVersions(templateId, typeId, status, versionActualDateStart, versionActualDateEnd, logger);
                                    DeclarationTemplate declarationTemplate =  createFakeTemplate(date, typeId);
                                    declarationTemplateService.save(declarationTemplate);
                                }
                            }
                        }
                        break;
                    case FAKE:
                        compareResult = newIntersection.compareTo(intersection);
                        //Варианты 15
                        if (compareResult == -2 || compareResult == -7 || compareResult == -16){
                            DeclarationTemplate formTemplate = declarationTemplateService.get(intersection.getTemplateId());
                            formTemplate.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd.getTime()));
                            declarationTemplateService.save(formTemplate);
                        }
                        //Варианты 16,18a,19,20
                        else if (compareResult == 11 || compareResult == 5 || compareResult == -1 || compareResult == 10 || compareResult == 16){
                            declarationTemplateService.delete(intersection.getTemplateId());
                        }
                        break;
                }
            }
        }
        //2 Шаг. Система проверяет наличие даты окончания актуальности.
        //Пересечений нет
        else if (versionActualDateEnd != null){
            Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd.getTime());
            cleanVersions(templateId, typeId, status, versionActualDateStart, versionActualDateEnd, logger);
            DeclarationTemplate declarationTemplate =  createFakeTemplate(date, typeId);
            declarationTemplateService.save(declarationTemplate);
        }
    }

    @Override
    public void cleanVersions(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        if (templateId == 0)
            return;
        DeclarationTemplate declarationTemplateFake = declarationTemplateService.getNearestDTRight(templateId,
                VersionedObjectStatus.NORMAL, VersionedObjectStatus.DRAFT, VersionedObjectStatus.FAKE);
        if (declarationTemplateFake != null && declarationTemplateFake.getStatus() == VersionedObjectStatus.FAKE)
            declarationTemplateService.delete(declarationTemplateFake.getId());
    }

    @Override
    public void checkDestinationsSources(int typeId, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        List<Pair<DepartmentFormType, Pair<Date, Date>>> sourcePairs = sourceService.findSourceFTsForDeclaration(typeId, versionActualDateStart, versionActualDateEnd);
        List<Pair<DepartmentDeclarationType, Pair<Date, Date>>> destinationPairs = sourceService.findDestinationDTsForFormType(typeId, versionActualDateStart, versionActualDateEnd);
        for (Pair<DepartmentFormType, Pair<Date, Date>> pair : sourcePairs){
            DepartmentFormType first = pair.getFirst();
            FormType typeSource = formTypeService.get(first.getFormTypeId());
            logger.error(
                    String.format(MSG_HAVE_SOURCE,
                            MessageGenerator.mesSpeckPlural(typeSource.getTaxType()),
                            first.getKind().getTitle(),
                            formTypeService.get(first.getFormTypeId()).getName(),
                            departmentService.getDepartment(first.getDepartmentId()).getName(),
                            getPeriod(pair.getSecond())
                    )
            );
        }
        for (Pair<DepartmentDeclarationType, Pair<Date, Date>> pair : destinationPairs){
            DepartmentDeclarationType first = pair.getFirst();
            logger.error(
                    String.format(MSG_HAVE_DESTINATION,
                            declarationTypeService.get(first.getDeclarationTypeId()).getName(),
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
    public void checkDestinationsSources(int typeId, Pair<Date, Date> beginRange, Pair<Date, Date> endRange, Logger logger) {
        if (beginRange != null)
            checkDestinationsSources(typeId, beginRange.getFirst(), beginRange.getSecond(), logger);
        if (endRange != null)
            checkDestinationsSources(typeId, endRange.getFirst(), endRange.getSecond(), logger);
    }

    private DeclarationTemplate createFakeTemplate(Date date, int typeId){
        DeclarationTemplate declarationTemplate =  new DeclarationTemplate();
        declarationTemplate.setVersion(date);
        declarationTemplate.setStatus(VersionedObjectStatus.FAKE);
        declarationTemplate.setType(declarationTypeService.get(typeId));
	    declarationTemplate.setName("FAKE");
        return declarationTemplate;
    }

    private Date createActualizationDates(int calendarTime, int time, long actualTemplateDate){
        calendar.clear();
        calendar.setTime(new Date(actualTemplateDate));
        calendar.add(calendarTime, time);
        return calendar.getTime();
    }
}
