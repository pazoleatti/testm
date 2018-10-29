package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.refbook.RefBookCache;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: avanteev
 */
@Service
@Transactional
public class RefBookHelperImpl implements RefBookHelper {

    private ApplicationContext applicationContext;

    public RefBookHelperImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    public void checkReferenceValues(RefBook refBook, Map<RefBookDataProvider, List<RefBookLinkModel>> references, Logger logger) {
        if (!references.isEmpty()) {
            // id ссылок на справочники объединяются по провайдеру, который обрабатывает эти справочники +
            // по входной дате окончания для каждой ссылки, которая может быть разной например для случая импорта справочников
            Map<RefBookDataProvider, Map<Date, List<Long>>> idsByProvider = new HashMap<>();

            //получаем названия колонок
            Map<String, String> aliases = new HashMap<>();
            for (RefBookAttribute attribute : refBook.getAttributes()) {
                aliases.put(attribute.getAlias(), attribute.getName());
            }
            //Собираем ссылки в кучу для каждого провайдера
            for (Map.Entry<RefBookDataProvider, List<RefBookLinkModel>> entry : references.entrySet()) {
                RefBookDataProvider provider = entry.getKey();
                if (!idsByProvider.containsKey(provider)) {
                    idsByProvider.put(provider, new HashMap<Date, List<Long>>());
                }
                for (RefBookLinkModel link : entry.getValue()) {
                    Date versionTo = link.getVersionTo();
                    if (!idsByProvider.get(provider).containsKey(versionTo)) {
                        idsByProvider.get(provider).put(versionTo, new ArrayList<Long>());
                    }
                    idsByProvider.get(provider).get(versionTo).add(link.getReferenceValue());
                }
            }

            boolean hasFatalError = false;

            //Проверяем ссылки отдельно по каждому провайдеру
            for (Map.Entry<RefBookDataProvider, List<RefBookLinkModel>> entry : references.entrySet()) {
                RefBookDataProvider provider = entry.getKey();
                Map<Date, List<Long>> idsByVersionEnd = idsByProvider.get(provider);
                List<RefBookLinkModel> links = entry.getValue();
                if (links != null && links.size() > 0 && idsByVersionEnd.size() > 0) {
                    //Дата начала для проверки - можно взять у любой записи, т.к она всегда одинаковая у всех
                    Date versionFrom = links.get(0).getVersionFrom();

                    //Обрабатываем группы записей с одним и тем же провайдером и датой окончания
                    for (Map.Entry<Date, List<Long>> ids : idsByVersionEnd.entrySet()) {
                        Date versionTo = ids.getKey();
                        List<ReferenceCheckResult> inactiveRecords = provider.getInactiveRecordsInPeriod(ids.getValue(), versionFrom, versionTo);
                        if (!inactiveRecords.isEmpty()) {
                            for (ReferenceCheckResult inactiveRecord : inactiveRecords) {
                                //ищем информацию по плохим записям и формируем сообщение по каждой
                                for (RefBookLinkModel link : links) {
                                    if (inactiveRecord.getRecordId().equals(link.getReferenceValue())) {
                                        switch (inactiveRecord.getResult()) {
                                            case NOT_EXISTS: {
                                                String msg = buildMsg("Поле \"%s\" содержит ссылку на несуществующую версию записи справочника!", link);
                                                hasFatalError = logFatalError(logger, msg, link, aliases);
                                                break;
                                            }
                                            case NOT_CROSS: {
                                                String msg;
                                                msg = buildMsg("\"%s\": Период актуальности выбранного значения не пересекается с периодом актуальности версии!", link);
                                                hasFatalError = logFatalError(logger, msg, link, aliases);
                                            }
                                            case NOT_LAST: {
                                                String msg;
                                                msg = buildMsg("\"%s\": Выбранная версия записи справочника не является последней действующей в периоде сохраняемой версии (с %s по %s)!", link);
                                                logger.warn(msg,
                                                        link.getIndex() != null ? link.getIndex() :
                                                                link.getSpecialId() != null ? link.getSpecialId() : "",
                                                        aliases.get(link.getAttributeAlias()),
                                                        sdf.get().format(versionFrom),
                                                        versionTo != null ? sdf.get().format(versionTo) : "-"
                                                );
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (hasFatalError) {
                //Исключение выбрасывается только для справочников и тут не важно что писать, т.к текст заменяется в вызывающем коде
                throw new ServiceException("Поля содержат некорректные справочные ссылки!");
            }
        }
    }

    private boolean logFatalError(Logger logger, String msg, RefBookLinkModel link, Map<String, String> aliases) {
        logger.error(msg,
                link.getIndex() != null ? link.getIndex() :
                        link.getSpecialId() != null ? link.getSpecialId() : "",
                aliases.get(link.getAttributeAlias()));
        return true;
    }

    private String buildMsg(String msg, RefBookLinkModel link) {
        if (link.getIndex() != null) {
            msg = "Строка %s: " + msg;
        } else if (link.getSpecialId() != null) {
            //Если проверка выполняется для нескольких записей справочника (например при импорте справочника), то формируем специальное имя для каждой записи
            msg = "Запись \"%s\", " + msg;
        } else {
            msg = "%s" + msg;
        }
        return msg;
    }

    @Override
    public Map<Long, Map<Long, String>> dereferenceValues(RefBook refBook, List<Map<String, RefBookValue>> refBookPage) {
        final RefBookCache refBookCacher = applicationContext.getBean(RefBookCache.class);
        Map<Long, Map<Long, String>> dereferenceValues = new HashMap<>(); // Map<attrId, Map<referenceId, value>>
        if (refBookPage.isEmpty()) {
            return dereferenceValues;
        }
        List<RefBookAttribute> attributes = refBook.getRefAttributes();

        // разыменовывание ссылок
        for (RefBookAttribute attribute : attributes) {
            // сбор всех ссылок
            String alias = attribute.getAlias();
            Set<Long> recordIds = new HashSet<>();
            for (Map<String, RefBookValue> record : refBookPage) {
                RefBookValue value = record.get(alias);
                if (value != null && !value.isEmpty()) {
                    recordIds.add(value.getReferenceValue());
                }
            }
            // групповое разыменование, если есть что разыменовывать
            if (!recordIds.isEmpty()) {
                RefBookDataProvider provider = refBookCacher.getDataProvider(attribute.getRefBookId());
                Map<Long, RefBookValue> values = provider.dereferenceValues(attribute.getRefBookAttributeId(), recordIds);
                if (values != null && !values.isEmpty()) {
                    Map<Long, String> stringValues = new HashMap<>();
                    for (Map.Entry<Long, RefBookValue> entry : values.entrySet()) {
                        stringValues.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                    dereferenceValues.put(attribute.getId(), stringValues);
                }
            }
        }
        return dereferenceValues;
    }

    @Override
    public String refBookRecordToString(RefBook refBook, RefBookRecord record) {
        Map<String, RefBookValue> refBookValueMap = record.getValues();
        // [attrId : [refId : strValue]]
        Map<Long, Map<Long, String>> dereferenceValues = dereferenceValues(refBook, Collections.singletonList(refBookValueMap));
        Map<String, String> strValues = new HashMap<>();
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            strValues.put(attribute.getAlias(), dereferenceValue(refBookValueMap, dereferenceValues, attribute));
        }
        StringBuilder sb = new StringBuilder("[id:");
        sb
                .append(record.getUniqueRecordId())
                .append("; ")
                .append("recordId:")
                .append(record.getRecordId());
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            sb
                    .append("; ")
                    .append(attribute.getName())
                    .append(":\"")
                    .append(strValues.get(attribute.getAlias()))
                    .append('"');
        }
        return sb.append(']').toString();
    }

    private String dereferenceValue(Map<String, RefBookValue> record, Map<Long, Map<Long, String>> dereferenceValues, RefBookAttribute attribute) {
        RefBookValue value = record.get(attribute.getAlias());
        String dereferenceValue;
        if (value == null) {
            dereferenceValue = "";
        } else {
            switch (value.getAttributeType()) {
                case NUMBER:
                    if (value.getNumberValue() == null) dereferenceValue = "";
                    else dereferenceValue = value.getNumberValue().toString();
                    break;
                case DATE:
                    if (value.getDateValue() == null) dereferenceValue = "";
                    else {
                        if (attribute.getFormat() != null) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                                    attribute.getFormat().getFormat());
                            dereferenceValue = simpleDateFormat.format(value.getDateValue());
                        } else {
                            dereferenceValue = value.getDateValue().toString();
                        }
                    }
                    break;
                case STRING:
                    if (value.getStringValue() == null) dereferenceValue = "";
                    else dereferenceValue = value.getStringValue();
                    break;
                case REFERENCE:
                    if (value.getReferenceValue() == null) dereferenceValue = "";
                    else {
                        dereferenceValue = dereferenceValues.get(attribute.getId()).get(value.getReferenceValue());
                    }
                    break;
                default:
                    dereferenceValue = "undefined";
                    break;
            }
        }
        return dereferenceValue;
    }
}