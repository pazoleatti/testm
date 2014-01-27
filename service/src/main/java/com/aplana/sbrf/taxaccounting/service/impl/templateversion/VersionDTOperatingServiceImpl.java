package com.aplana.sbrf.taxaccounting.service.impl.templateversion;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.SegmentIntersection;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
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

    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private DeclarationDataService declarationDataService;

    private Calendar calendar = Calendar.getInstance();

    @Override
    public void isUsedVersion(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        if (template.getStatus() == VersionedObjectStatus.DRAFT)
            return;
        List<Long> ddIds = declarationDataService.getDeclarationDataLisByVersionTemplate(template.getId());
        if (!ddIds.isEmpty()){
            logger.error("Обнаружено использование макета для налоговых форм");
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
                                        intersection.getEndDate(), intersection.getEndDate(), intersection.getEndDate(), intersection.getTemplateId());
                        }
                        //2 Шаг. Система проверяет наличие даты окончания актуальности.
                        //Пересечений нет
                        else if (compareResult == -9 || compareResult == 4 || compareResult == -4){
                            Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd);
                            DeclarationTemplate formTemplate =  createFakeTemplate(date, template.getType());
                            declarationTemplateService.save(formTemplate);
                        }
                        break;
                    case FAKE:
                        compareResult = newIntersection.compareTo(intersection);
                        //Варианты 15
                        if (compareResult == -2){
                            DeclarationTemplate formTemplate = declarationTemplateService.get(intersection.getTemplateId());
                            formTemplate.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd));
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
            DeclarationTemplate declarationTemplate =  createFakeTemplate(versionActualDateEnd, template.getType());
            declarationTemplateService.save(declarationTemplate);
        }
    }



    @Override
    public void createNewVersion(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        isIntersectionVersion(template, versionActualDateEnd, logger);
    }

    @Override
    public void cleanVersions(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        DeclarationTemplate declarationTemplateFake = declarationTemplateService.getNearestDTRight(template.getId(), VersionedObjectStatus.FAKE);
        if (declarationTemplateFake != null)
            declarationTemplateService.delete(declarationTemplateFake);
    }

    private DeclarationTemplate createFakeTemplate(Date date, DeclarationType type){
        DeclarationTemplate declarationTemplate =  new DeclarationTemplate();
        declarationTemplate.setVersion(date);
        declarationTemplate.setStatus(VersionedObjectStatus.FAKE);
        declarationTemplate.setEdition(0);
        declarationTemplate.setType(type);
        return declarationTemplate;
    }

    private Date createActualizationDates(int calendarTime, int time, Date actualTemplateDate){
        calendar.clear();
        calendar.setTime(actualTemplateDate);
        calendar.add(calendarTime, time);
        return calendar.getTime();
    }
}
