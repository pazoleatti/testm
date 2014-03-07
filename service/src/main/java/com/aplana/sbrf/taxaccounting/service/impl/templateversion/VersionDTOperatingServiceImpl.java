package com.aplana.sbrf.taxaccounting.service.impl.templateversion;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
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
@Service("declarationTemplateOperatingService")
@Transactional
public class VersionDTOperatingServiceImpl implements VersionOperatingService<DeclarationTemplate> {

    public static final String MSG_IS_USED_VERSION = "Существует экземпляр декларации в подразделении \"%s\" периоде %s для макета!";

    @Autowired
    private DeclarationDataDao declarationDataDao;
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
    public void isUsedVersion(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        if (template.getStatus() == VersionedObjectStatus.DRAFT)
            return;
        List<Long> ddIds = declarationDataService.getDeclarationDataLisByVersionTemplate(template.getId());
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
    public void isCorrectVersion(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void isIntersectionVersion(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        //1 Шаг. Система проверяет пересечение с периодом актуальности хотя бы одной версии этого же макета, STATUS которой не равен -1.
        SegmentIntersection newIntersection = new SegmentIntersection();
        newIntersection.setBeginDate(template.getVersion());
        newIntersection.setEndDate(versionActualDateEnd);
        newIntersection.setTemplateId(template.getId() != null?template.getId() : 0);
        newIntersection.setStatus(template.getStatus());
        List<SegmentIntersection> segmentIntersections = declarationTemplateService.findFTVersionIntersections(template, versionActualDateEnd);
        if (!segmentIntersections.isEmpty()){
            for (SegmentIntersection intersection : segmentIntersections){
                int compareResult;
                switch (intersection.getStatus()){
                    case NORMAL:
                    case DRAFT:
                        compareResult = newIntersection.compareTo(intersection);
                        //Варианты 1, 2, 3, 4, 5, 6, 9, 10
                        if (compareResult == 2 || compareResult == 0 ||compareResult == -2 || compareResult == 7 || compareResult == -7 || compareResult == -1){
                            logger.error("Обнаружено пересечение указанного срока актуальности с существующей версией");
                            return;
                        }
                        // Варианты 7,8
                        else if(compareResult == 1 || compareResult == -5){
                            isUsedVersion(declarationTemplateService.get(intersection.getTemplateId()), null, logger);
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
                            cleanVersions(newIntersection.getTemplateId(), newIntersection.getEndDate(), logger);
                            DeclarationTemplate formTemplate =  createFakeTemplate(date, template.getType());
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
                        //Варианты 16,19,20
                        else if (compareResult == 5 || compareResult == -7 || compareResult == -1){
                            declarationTemplateService.delete(declarationTemplateService.get(intersection.getTemplateId()));
                        }
                        break;
                }
            }
        } else if (newIntersection.getEndDate() != null){
            cleanVersions(newIntersection.getTemplateId(), newIntersection.getEndDate(), logger);
            DeclarationTemplate declarationTemplate =  createFakeTemplate(versionActualDateEnd, template.getType());
            declarationTemplateService.save(declarationTemplate);
        }
    }



    @Override
    public void createNewVersion(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        isIntersectionVersion(template, versionActualDateEnd, logger);
    }

    @Override
    public void cleanVersions(int templateId, Date versionActualDateEnd, Logger logger) {
        if (templateId == 0)
            return;
        DeclarationTemplate declarationTemplateFake = declarationTemplateService.getNearestDTRight(templateId, VersionedObjectStatus.FAKE);
        if (declarationTemplateFake != null)
            declarationTemplateService.delete(declarationTemplateFake);
    }

    private DeclarationTemplate createFakeTemplate(Date date, DeclarationType type){
        DeclarationTemplate declarationTemplate =  new DeclarationTemplate();
        declarationTemplate.setVersion(date);
        declarationTemplate.setStatus(VersionedObjectStatus.FAKE);
        declarationTemplate.setEdition(0);
        declarationTemplate.setType(type);
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
