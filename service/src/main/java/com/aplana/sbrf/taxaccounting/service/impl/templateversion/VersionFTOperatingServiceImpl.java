package com.aplana.sbrf.taxaccounting.service.impl.templateversion;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.SegmentIntersection;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
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
public class VersionFTOperatingServiceImpl implements VersionOperatingService<FormTemplate> {

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private FormDataService formDataService;

    private Calendar calendar = Calendar.getInstance();

    @Override
    public void isUsedVersion(FormTemplate template, Date versionActualDateEnd, Logger logger) {
        List<Long> fdIds = formDataService.getFormDataLisByVersionTemplate(template.getId());
        if (!fdIds.isEmpty()){
            logger.error("Обнаружено использование макета для налоговых форм");
        }

    }

    @Override
    public void isCorrectVersion(FormTemplate template, Date versionActualDateEnd, Logger logger) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void isIntersectionVersion(FormTemplate template, Date versionActualDateEnd, Logger logger) {
        //1 Шаг. Система проверяет пересечение с периодом актуальности хотя бы одной версии этого же макета, STATUS которой не равен -1.
        SegmentIntersection newIntersection = new SegmentIntersection();
        newIntersection.setBeginDate(template.getVersion());
        newIntersection.setEndDate(versionActualDateEnd);
        newIntersection.setTemplateId(template.getId() != null?template.getId() : 0);
        newIntersection.setStatus(template.getStatus());
        List<SegmentIntersection> segmentIntersections = formTemplateService.findFTVersionIntersections(template, versionActualDateEnd);
        if (!segmentIntersections.isEmpty()){
            for (SegmentIntersection intersection : segmentIntersections){
                int compareResult = 0;
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
                            isUsedVersion(formTemplateService.get(intersection.getTemplateId()), null, logger);
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
                            FormTemplate formTemplate =  createFakeTemplate(date, template.getType());
                            formTemplateService.save(formTemplate);
                        }
                        break;
                    case FAKE:
                        compareResult = newIntersection.compareTo(intersection);
                        //Варианты 15
                        if (compareResult == -2){
                            FormTemplate formTemplate = formTemplateService.get(intersection.getTemplateId());
                            formTemplate.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd));
                            formTemplateService.save(formTemplate);
                        }
                        //Варианты 16,19,20
                        else if (compareResult == 5 || compareResult == -7 || compareResult == -1){
                            formTemplateService.delete(formTemplateService.get(intersection.getTemplateId()));
                        }
                        break;
                }
            }
        } else if(newIntersection.getEndDate() != null){
            FormTemplate formTemplate =  createFakeTemplate(versionActualDateEnd, template.getType());
            formTemplateService.save(formTemplate);
        }

    }

    @Override
    public void createNewVersion(FormTemplate template, Date versionActualDateEnd, Logger logger) {
        isIntersectionVersion(template, versionActualDateEnd, logger);
    }

    @Override
    public void cleanVersions(FormTemplate template, Date versionActualDateEnd, Logger logger) {
        FormTemplate formTemplateFake = formTemplateService.getNearestFTRight(template.getId(), VersionedObjectStatus.FAKE);
        if (formTemplateFake != null)
            formTemplateService.delete(formTemplateFake);
    }

    private FormTemplate createFakeTemplate(Date date, FormType formType){
        FormTemplate formTemplate =  new FormTemplate();
        formTemplate.setVersion(date);
        formTemplate.setStatus(VersionedObjectStatus.FAKE);
        formTemplate.setEdition(0);
        formTemplate.setType(formType);
        formTemplate.setName("fake");
        formTemplate.setFullName("fake");
        formTemplate.setCode("fake");
        return formTemplate;
    }

    private Date createActualizationDates(int calendarTime, int time, Date actualTemplateDate){
        calendar.clear();
        calendar.setTime(actualTemplateDate);
        calendar.add(calendarTime, time);
        return calendar.getTime();
    }
}
