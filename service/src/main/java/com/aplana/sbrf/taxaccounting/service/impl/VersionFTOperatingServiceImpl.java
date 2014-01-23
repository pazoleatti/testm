package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.SegmentIntersection;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
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
        }

    }

    /*@Override
    public void isIntersectionVersion(FormTemplate template, Date versionActualDateEnd, Logger logger) {

        List<Integer> formTemplateIds =
                formTemplateService.findFTVersionIntersections(template, versionActualDateEnd);
        //1 Шаг. Система проверяет пересечение с периодом актуальности хотя бы одной версии этого же макета, STATUS которой не равен -1.
        if (!formTemplateIds.isEmpty()){
            //Обнаружена хотя бы одна версия, с которой есть пересечение.
            FormTemplate formTemplate = formTemplateService.get(formTemplateIds.get(0));
            System.out.println(formTemplate.getVersion() + " " + formTemplate.getStatus());
            switch (formTemplate.getStatus()){
                case NORMAL:
                case DRAFT:
                    //Статус равен 0 или 1 и дата окончания актуальности существует.
                    FormTemplate ftRight = formTemplateService.getNearestFTRight(formTemplate);
                    if (ftRight != null && ftRight.getVersion() != null){
                        logger.error("Обнаружено пересечение указанного срока актуальности с существующей версией");
                        return;
                    }
                        //Статус равен 0 или 1 и дата окончания актуальности не существует.
                    else {
                            *//*calendar.setTime(formTemplate.getVersion());*//*
                        //Т.к. по постановке, в случае если отсутствует дата окончания актуальности версии
                        // то датой запроса является крайний день текущего года. Крайний день всегда 31 декабря.
                            *//*calendar.set(calendar.get(Calendar.YEAR), Calendar.DECEMBER, 31);*//*
                        isUsedVersion(formTemplate, null, logger);
                        if (logger.containsLevel(LogLevel.ERROR)){
                            logger.error("Обнаружено пересечение указанного срока актуальности с существующей версией");
                            return;
                        }

                        Date ftEndDate = createActualizationDates(Calendar.DAY_OF_YEAR, -1, template.getVersion());
                        FormTemplate ft = createFakeTemplate(ftEndDate, formTemplate);
                        formTemplateService.save(ft);
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
                    if (formTemplate.getVersion().compareTo(template.getVersion()) >= 0){
                        FormTemplate nextFormTemplate = formTemplateService.getNearestFTRight(formTemplate);
                        //Система проверяет, указан ли дата окончания проверяемой версии. Дата окончания указана.
                        //Система задает дату начала актуальности обнаруженной версии, равной дате окончания проверяемой версии + 1 день.
                        if (versionActualDateEnd != null && nextFormTemplate != null &&
                                nextFormTemplate.getVersion().compareTo(versionActualDateEnd) >= 0){
                            formTemplate.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd));
                            formTemplateService.save(formTemplate);
                        }
                        //Система проверяет, указан ли дата окончания проверяемой версии. Дата окончания не указана.
                        else {
                            //Система удаляет обнаруженную версию.
                            formTemplateService.delete(formTemplate);
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
            FormTemplate nextFormTemplate = formTemplateService.getNearestFTRight(template);
            if (nextFormTemplate != null){
                calendar.setTime(versionActualDateEnd);
                Calendar calendarFirstFT = Calendar.getInstance();
                calendarFirstFT.setTime(nextFormTemplate.getVersion());
                Calendar distinct = Calendar.getInstance();
                distinct.setTime(new Date(calendarFirstFT.getTime().getTime() - calendar.getTime().getTime()));
                calendar.clear();
                //Разница между датами не равна 1.
                if (distinct.get(Calendar.DAY_OF_YEAR) != 1){
                    Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd);
                    FormTemplate formTemplate =  createFakeTemplate(date, template);
                    formTemplateService.save(formTemplate);
                }
            }
            //Следующая версия не существует.
            else{
                Date date = createActualizationDates(Calendar.DAY_OF_YEAR, 1, versionActualDateEnd);
                FormTemplate formTemplate =  createFakeTemplate(date, template);
                formTemplateService.save(formTemplate);
            }
        }
        // Дата окончания актуальности не указана.
        else {
            FormTemplate nextFormTemplate = formTemplateService.getNearestFTRight(template);
            if(nextFormTemplate != null){
                template.setVersion(createActualizationDates(Calendar.DAY_OF_YEAR, -1, nextFormTemplate.getVersion()));
                formTemplateService.save(template);
                logger.info(
                        "Установлена дата окончания актуальности версии %td.%tm.%tY в связи с наличием следующей версии",
                        template.getVersion(), template.getVersion(), template.getVersion()
                );

            }
        }
    }*/

    @Override
    public void createNewVersion(FormTemplate template, Date versionActualDateEnd, Logger logger) {
        isIntersectionVersion(template, versionActualDateEnd, logger);
    }

    @Override
    public void cleanVersions(FormTemplate template, Date versionActualDateEnd, Logger logger) {
        FormTemplate formTemplateFake = formTemplateService.getNearestFTRight(template, VersionedObjectStatus.FAKE);
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
