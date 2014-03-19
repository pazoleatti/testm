package com.aplana.sbrf.taxaccounting.service.impl.templateversion;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
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
@Service("declarationTemplateOperatingService")
@Transactional
public class VersionDTOperatingServiceImpl implements VersionOperatingService {

    public static final String MSG_IS_USED_VERSION = "Существует экземпляр декларации в подразделении \"%s\" периоде %s для макета!";

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
    private PeriodService periodService;

    private Calendar calendar = Calendar.getInstance();

    @Override
    public void isUsedVersion(int templateId, int typeId, VersionedObjectStatus status, Date versionActualDateStart, Date versionActualDateEnd, Logger logger) {
        if (status == VersionedObjectStatus.DRAFT)
            return;
        List<Long> ddIds = declarationDataService.getFormDataListInActualPeriodByTemplate(templateId, versionActualDateStart);
        if (!ddIds.isEmpty()){
            for(Long id: ddIds) {
                DeclarationData declarationData = declarationDataDao.get(id);
                Department department = departmentService.getDepartment(declarationData.getDepartmentId());
                ReportPeriod period = periodService.getReportPeriod(declarationData.getReportPeriodId());

                logger.error(MSG_IS_USED_VERSION, department.getName(), period.getName() + " " + period.getTaxPeriod().getYear());
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
                        //2 Шаг. Система проверяет наличие даты окончания актуальности.
                        //Пересечений нет
                        else if (compareResult == -9 || compareResult == 4 || compareResult == -4){
                            Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd.getTime());
                            cleanVersions(newIntersection.getTemplateId(), newIntersection.getTypeId(), newIntersection.getStatus(),
                                    newIntersection.getBeginDate(), newIntersection.getEndDate(), logger);
                            DeclarationTemplate formTemplate =  createFakeTemplate(date, newIntersection.getTypeId());
                            declarationTemplateService.save(formTemplate);
                        }
                        break;
                    case FAKE:
                        compareResult = newIntersection.compareTo(intersection);
                        //Варианты 15
                        if (compareResult == -2){
                            DeclarationTemplate formTemplate = declarationTemplateService.get(intersection.getTemplateId());
                            formTemplate.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd.getTime()));
                            declarationTemplateService.save(formTemplate);
                        }
                        //Варианты 16,18a,19,20
                        else if (compareResult == 5 || compareResult == -7 || compareResult == -1 || compareResult == -16 || compareResult == 10){
                            declarationTemplateService.delete(declarationTemplateService.get(intersection.getTemplateId()));
                        }
                        break;
                }
            }
        } else if (versionActualDateEnd != null){
            cleanVersions(templateId, typeId, status, versionActualDateStart, versionActualDateEnd, logger);
            DeclarationTemplate declarationTemplate =  createFakeTemplate(versionActualDateEnd, typeId);
            declarationTemplateService.save(declarationTemplate);
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
        DeclarationTemplate declarationTemplateFake = declarationTemplateService.getNearestDTRight(templateId, VersionedObjectStatus.FAKE);
        if (declarationTemplateFake != null)
            declarationTemplateService.delete(declarationTemplateFake);
    }

    private DeclarationTemplate createFakeTemplate(Date date, int typeId){
        DeclarationTemplate declarationTemplate =  new DeclarationTemplate();
        declarationTemplate.setVersion(date);
        declarationTemplate.setStatus(VersionedObjectStatus.FAKE);
        declarationTemplate.setEdition(0);
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
