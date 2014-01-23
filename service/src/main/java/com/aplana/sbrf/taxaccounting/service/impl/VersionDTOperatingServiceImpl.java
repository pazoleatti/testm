package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.SegmentIntersection;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.VersionOperatingService;
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
        }
    }

   /* @Override
    public void isIntersectionVersion(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        List<Integer> formTemplateIds =
                declarationTemplateService.findFTVersionIntersections(template, versionActualDateEnd);
        //1 Шаг. Система проверяет пересечение с периодом актуальности хотя бы одной версии этого же макета, STATUS которой не равен -1.
        if (!formTemplateIds.isEmpty()){
            //Обнаружена хотя бы одна версия, с которой есть пересечение.
            DeclarationTemplate declarationTemplate = declarationTemplateService.get(formTemplateIds.get(0));
            switch (declarationTemplate.getStatus()){
                case NORMAL:
                case DRAFT:
                    //Статус равен 0 или 1 и дата окончания актуальности существует.
                    DeclarationTemplate declarationTemplateEnd = declarationTemplateService.getNearestDTRight(declarationTemplate);
                    if (declarationTemplateEnd != null && declarationTemplateEnd.getVersion() != null){
                        logger.error("Обнаружено пересечение указанного срока актуальности с существующей версией");
                        return;
                    }
                    //Статус равен 0 или 1 и дата окончания актуальности не существует.
                    else {
                            *//*calendar.setTime(formTemplate.getVersion());*//*
                        //Т.к. по постановке, в случае если отсутствует дата окончания актуальности версии
                        // то датой запроса является крайний день текущего года. Крайний день всегда 31 декабря.
                            *//*calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);*//*
                        isUsedVersion(declarationTemplate, null, logger);
                        if (logger.containsLevel(LogLevel.ERROR)){
                            logger.error("Обнаружено пересечение указанного срока актуальности с существующей версией");
                            return;
                        }

                        Date ftEndDate = createActualizationDates(Calendar.DAY_OF_YEAR, -1, template.getVersion());
                        DeclarationTemplate dt = createFakeTemplate(ftEndDate, declarationTemplate);
                        declarationTemplateService.save(dt);
                        logger.info("Установлена дата окончания актуальности версии %td.%tm.%tY для предыдущей версии",
                                ftEndDate, ftEndDate, ftEndDate);
                    }
                    break;
                case FAKE:
                    //Система проверяет период актуальности обнаруженной версии.
                        *//* Дата начала ее актуальности меньше даты начала актуальности проверяемой версии
                        Переход к шагу 2 основного сценария.*//*

                    //Дата начала ее актуальности больше или равна дате начала актуальности проверяемой версии и
                    // дата окончания ее актуальности больше даты окончания актуальности проверяемой версии.
                        *//*
                        Вариант когда:
                            Дата начала ее актуальности больше или равна дате начала актуальности проверяемой версии
                            и дата окончания ее актуальности больше даты окончания актуальности проверяемой версии
                            не стал реализовывать, т.к.перекрывается реализованным.
                         *//*
                    // Дата начала ее актуальности меньше даты начала актуальности проверяемой версии
                    if (declarationTemplate.getVersion().compareTo(template.getVersion()) >= 0){
                        DeclarationTemplate nextDeclarationTemplate = declarationTemplateService.getNearestDTRight(declarationTemplate);
                        //Система проверяет, указан ли дата окончания проверяемой версии. Дата окончания указана.
                        //Система задает дату начала актуальности обнаруженной версии, равной дате окончания проверяемой версии + 1 день.
                        if (versionActualDateEnd != null && nextDeclarationTemplate != null &&
                                nextDeclarationTemplate.getVersion().compareTo(versionActualDateEnd) >= 0){
                            declarationTemplate.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd));
                            declarationTemplateService.save(declarationTemplate);
                        }
                        //Система проверяет, указан ли дата окончания проверяемой версии. Дата окончания не указана.
                        else {
                            //Система удаляет обнаруженную версию.
                            declarationTemplateService.delete(declarationTemplate);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        //2 Шаг. Система проверяет наличие даты окончания актуальности.
        //Дата окончания актуальности указана.
        //Дата окончания актуальности указана и имеет значение, равное дате начала актуальности следующей версии, уменьшенной на 1 день.
        if (versionActualDateEnd != null){
            //Следующая версия существует.
            DeclarationTemplate nextDeclarationTemplate = declarationTemplateService.getNearestDTRight(template);
            if (nextDeclarationTemplate != null){
                calendar.setTime(versionActualDateEnd);
                Calendar calendarFirstFT = Calendar.getInstance();
                calendarFirstFT.setTime(nextDeclarationTemplate.getVersion());
                Calendar distinct = Calendar.getInstance();
                distinct.setTime(new Date(calendarFirstFT.getTime().getTime() - calendar.getTime().getTime()));
                calendar.clear();
                //Разница между датами не равна 1.
                if (distinct.get(Calendar.DAY_OF_YEAR) != 1){
                    Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd);
                    DeclarationTemplate declarationTemplate =  createFakeTemplate(date, template);
                    declarationTemplateService.save(declarationTemplate);
                }
            }
            //Следующая версия не существует.
            else{
                Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd);
                DeclarationTemplate declarationTemplate =  createFakeTemplate(date, template);
                declarationTemplateService.save(declarationTemplate);
            }
        }
        // Дата окончания актуальности не указана.
        else {
            DeclarationTemplate nextDeclarationTemplate = declarationTemplateService.getNearestDTRight(template);
            if(nextDeclarationTemplate != null){
                template.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, -1, nextDeclarationTemplate.getVersion()));
                declarationTemplateService.save(template);
                logger.info(
                        "Установлена дата окончания актуальности версии %td.%tm.%tY в связи с наличием следующей версии",
                        template.getVersion(), template.getVersion(), template.getVersion()
                );

            }
        }
    }*/

    @Override
    public void createNewVersion(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        isIntersectionVersion(template, versionActualDateEnd, logger);
    }

    @Override
    public void cleanVersions(DeclarationTemplate template, Date versionActualDateEnd, Logger logger) {
        DeclarationTemplate declarationTemplateFake = declarationTemplateService.getNearestDTRight(template, VersionedObjectStatus.FAKE);
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
