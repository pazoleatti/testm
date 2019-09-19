package com.aplana.sbrf.taxaccounting.service.impl.transport.edo;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.dto.LogBusinessDTO;
import com.aplana.sbrf.taxaccounting.model.exception.ConfigurationParameterAbsentException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.jms.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.messaging.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocState;
import com.aplana.sbrf.taxaccounting.model.result.UploadTransportDataResult;
import com.aplana.sbrf.taxaccounting.model.util.AppFileUtils;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.component.factory.TransportMessageFactory;
import com.aplana.sbrf.taxaccounting.service.component.lock.locker.DeclarationLocker;
import com.aplana.sbrf.taxaccounting.service.jms.transport.MessageSender;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocState.*;

@Service
public class EdoMessageServiceImpl implements EdoMessageService {
    private static final Log LOG = LogFactory.getLog(EdoMessageService.class);

    private static final String MESSAGE_FORMAT_VERSION = "2.0";

    private final DeclarationDataService declarationDataService;
    private final DeclarationTemplateService declarationTemplateService;
    private final DeclarationLocker declarationLocker;
    private final BlobDataService blobDataService;
    private final ConfigurationService configurationService;
    private final ValidateXMLService validateXMLService;
    private final TransportMessageService transportMessageService;
    private final CommonRefBookService commonRefBookService;
    private final LogBusinessService logBusinessService;
    private final LockDataService lockDataService;
    private final TransactionHelper transactionHelper;
    private final TAUserService taUserService;
    private final UploadTransportDataService uploadTransportDataService;
    private final SubsystemService subsystemService;
    private final TransportMessageFactory transportMessageFactory;
    private final NotificationService notificationService;
    private final LogEntryService logEntryService;
    private final EdoResponseHandler edoResponseHandler;
    @Autowired(required = false)
    private MessageSender messageSender;
    @Autowired
    private AuditService auditService;

    static final String TRANSPORT_MESSAGE_CHANGE_NOTE_FORMAT = "Изменение статуса Транспортного сообщения № %s, " + // <Объект."Номер">
            "Идентификатор сообщения: %s, " + // <Объект.\"Идентификатор сообщения\">
            "Тип сообщения: %s, " + // <Объект."Тип сообщения">
            "Статус сообщения: %s"; // <Объект."Статус сообщения">

    private static final String SYSTEM_ERROR_NOTE_FORMAT = "Системная ошибка при обработке транспортного сообщения № %s, " + // <Объект."Номер">
            "Идентификатор сообщения: %s, " + //<Объект."Идентификатор сообщения">
            "Тип сообщения: %s, " + // <Объект."Тип сообщения">
            "Статус сообщения: %s"; // <Объект."Статус сообщения">

    private static final String TRANSPORT_MESSAGE_CREATE_NOTE_FORMAT = "Создание Транспортного сообщения № %s, " + // 3. <Объект."Номер">
            "Идентификатор сообщения: %s, " + // <Объект."Идентификатор сообщения">
            "Тип сообщения: %s, " + // <Объект."Тип сообщения">
            "Статус сообщения: %s"; // <Объект."Статус сообщения">

    static final String MESSAGE_EXPLANATION_DATE_FORMAT = "dd.MM.YY HH:mm";

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    public EdoMessageServiceImpl(DeclarationDataService declarationDataService, DeclarationTemplateService declarationTemplateService, DeclarationLocker declarationLocker,
                                 BlobDataService blobDataService, ConfigurationService configurationService, ValidateXMLService validateXMLService,
                                 TransportMessageService transportMessageService, CommonRefBookService commonRefBookService, LogBusinessService logBusinessService,
                                 LockDataService lockDataService, TransactionHelper transactionHelper, TAUserService taUserService, UploadTransportDataService uploadTransportDataService,
                                 SubsystemService subsystemService, TransportMessageFactory transportMessageFactory, NotificationService notificationService, LogEntryService logEntryService,
                                 EdoResponseHandler edoResponseHandler) {
        this.declarationDataService = declarationDataService;
        this.declarationTemplateService = declarationTemplateService;
        this.declarationLocker = declarationLocker;
        this.blobDataService = blobDataService;
        this.configurationService = configurationService;
        this.validateXMLService = validateXMLService;
        this.transportMessageService = transportMessageService;
        this.commonRefBookService = commonRefBookService;
        this.logBusinessService = logBusinessService;
        this.lockDataService = lockDataService;
        this.transactionHelper = transactionHelper;
        this.taUserService = taUserService;
        this.uploadTransportDataService = uploadTransportDataService;
        this.subsystemService = subsystemService;
        this.transportMessageFactory = transportMessageFactory;
        this.notificationService = notificationService;
        this.logEntryService = logEntryService;
        this.edoResponseHandler = edoResponseHandler;
    }

    @Override
    public SendToEdoResult sendToEdo(List<Long> declarationDataIds, TAUserInfo userInfo, Logger logger) {
        SendToEdoResult result = new SendToEdoResult();
        for (long declarationId : declarationDataIds) {
            DeclarationData declarationData = declarationDataService.get(declarationId);
            SendDeclarationToEdoContext sendDeclarationToEdoContext = new SendDeclarationToEdoContext(declarationData, userInfo, logger);
            sendDeclarationToEdoContext.sendToEdo();
            result.put(sendDeclarationToEdoContext.resultLogLevel, declarationData);
        }
        return result;
    }

    @Override
    public void accept(final String xmlMessage) {
        TransportMessage transportMessage = transactionHelper.executeInNewTransaction(new TransactionLogic<TransportMessage>() {
            @Override
            public TransportMessage execute() {
                return storeIncomingMessage(xmlMessage);
            }
        });
        try {
            validateIncomeMessage(xmlMessage, transportMessage);
            handleIncomeMessage(xmlMessage, transportMessage);
        } catch (Exception e) {
            transportMessageService.sendAuditMessage(SYSTEM_ERROR_NOTE_FORMAT, transportMessage);
            LOG.warn("В результате обработки файла ответа от ЭДО произошла исключительная ситуация." +
                    "Создано сообщение в ЖА", e);
        } finally {
            transportMessageService.update(transportMessage);
        }
    }

    @Override
    public void cancel(Long declarationId) {
        TransportMessageFilter filter = new TransportMessageFilter();
        filter.setTypeId(TransportMessageType.OUTGOING.getIntValue());
        filter.setStateIds(Collections.singletonList(TransportMessageState.SENT.getIntValue()));
        filter.setDeclarationId(declarationId.toString());

        List<TransportMessage> transportMessages = transportMessageService.findByFilter(filter, null);
        Collections.sort(transportMessages, new Comparator<TransportMessage>() {
            @Override
            public int compare(TransportMessage o1, TransportMessage o2) {
                return o1.getDateTime().isBefore(o2.getDateTime()) ? 1 : 0;
            }
        });

        TransportMessage lastTransportMessage = transportMessages.get(0);
        try {
            lastTransportMessage.setState(TransportMessageState.CANCELED);
            transportMessageService.update(lastTransportMessage);
        } catch (Exception e) {
            LOG.warn("Произошла ошибка при смене статуса ТС на '" + TransportMessageState.CANCELED.getText() + "'", e);

            String declarationFullDesc = declarationDataService.getFullDeclarationDescription(declarationId);
            throw new ServiceException(String.format("Не выполнена операция изменения состояния ЭД для налоговой формы: " +
                    "%s Причина: Не удалось изменить статус связанного с формой транспортного сообщения в журнале " +
                    "\"Обмен с ФП АС Учет Налогов\". Номер сообщения: %d", declarationFullDesc, lastTransportMessage.getId()));
        }
    }

    private TransportMessage storeIncomingMessage(String message) {
        TransportMessage transportMessage = new TransportMessage();

        transportMessage.setDateTime(LocalDateTime.fromDateFields(new Date()));
        transportMessage.setReceiverSubsystem(new Subsystem(getConfigIntValue(ConfigurationParam.NDFL_SUBSYSTEM_ID)));
        transportMessage.setInitiatorUser(taUserService.getUser(TAUser.SYSTEM_USER_ID));
        transportMessage.setBody(message);
        transportMessage.setContentType(TransportMessageContentType.UNKNOWN);
        transportMessage.setState(TransportMessageState.RECEIVED);
        transportMessage.setType(TransportMessageType.INCOMING);

        transportMessageService.create(transportMessage);

        return transportMessage;
    }

    private void validateIncomeMessage(String message, TransportMessage transportMessage) {
        Logger validationLogger = new Logger();
        RefBook declarationTypeRefBook = commonRefBookService.get(RefBook.Id.DECLARATION_TEMPLATE.getId());
        BlobData xsd = blobDataService.get(declarationTypeRefBook.getXsdId());
        if (!validateXMLService.validate(validationLogger, message, xsd.getName(), xsd.getInputStream())) {
            String errorMessage = "Входящее XML сообщение из ФП \"Фонды\", на основании которого было создано " +
                    "Транспортное Сообщение №:" + transportMessage.getId() + ", не соответствует XSD схеме.";
            failHandleEdoMessage(transportMessage, errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    private void handleIncomeMessage(String xmlMessage, TransportMessage incomeTransportMessage) {
        BaseMessage taxMessage = getTaxMessage(xmlMessage);
        enrichTransportBaseMessage(incomeTransportMessage, taxMessage);
        transportMessageService.sendAuditMessage(TRANSPORT_MESSAGE_CREATE_NOTE_FORMAT, incomeTransportMessage);
        validateSenderAndRecipient(xmlMessage, taxMessage);

        if (isMessageFromFns(taxMessage)) {
            TaxMessageTechDocument taxMessageTechDocument = (TaxMessageTechDocument) taxMessage;
            handleFnsResponse(incomeTransportMessage, taxMessageTechDocument);
        } else if (isEdoTechReceipt(taxMessage)) {
            TaxMessageReceipt taxMessageReceipt = (TaxMessageReceipt) taxMessage;
            try {
                edoResponseHandler.handleTechReceipt(incomeTransportMessage, taxMessageReceipt);
            } catch (ServiceException e) {
                failHandleEdoMessage(incomeTransportMessage, e.getMessage());
                throw e;
            }
        }
    }

    private TransportMessage geSourceTransportMessageFromEdoTechDoc(TaxMessageTechDocument taxMessageTechDoc) {
        TransportMessageFilter transportMessageFilter = new TransportMessageFilter();
        transportMessageFilter.setMessageUuid(taxMessageTechDoc.getParentDocument());
        transportMessageFilter.setTypeId(TransportMessageType.OUTGOING.getIntValue());
        return transportMessageService.findFirstByFilter(transportMessageFilter);
    }

    private BaseMessage getTaxMessage(String message) {
        try {
            Class[] taxMessageClasses = {TaxMessageReceipt.class, TaxMessageDocument.class, TaxMessageTechDocument.class};
            JAXBContext jaxbContext = JAXBContext.newInstance(taxMessageClasses);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            StringReader messageReader = new StringReader(message);
            return (BaseMessage) jaxbUnmarshaller.unmarshal(messageReader);
        } catch (JAXBException e) {
            throw new IllegalStateException("Ошибка конвертации XML сообщения в Java объект, сообщение: " + message, e);
        }
    }

    private void enrichTransportBaseMessage(TransportMessage transportMessage, BaseMessage taxMessageReceipt) {
        transportMessage.setMessageUuid(taxMessageReceipt.getUuid());
        transportMessage.setSenderSubsystem(new Subsystem(taxMessageReceipt.getSource()));
    }
    private void validateSenderAndRecipient(String xmlMessage, BaseMessage taxMessage) {
        if (!ndflSubsystemIsRecipient(taxMessage)) {
            throw new IllegalStateException("НДФЛ не является получателем транспортного сообщения: " + xmlMessage);
        }

        if (!senderIsEdo(taxMessage)) {
            throw new IllegalStateException("Отправителем транспортного сообщения не является ЭДО, сообщение: " + xmlMessage);
        }
    }

    private boolean ndflSubsystemIsRecipient(BaseMessage taxMessageReceipt) {
        return taxMessageReceipt.getDestination() == getConfigIntValue(ConfigurationParam.NDFL_SUBSYSTEM_ID);
    }
    private boolean senderIsEdo(BaseMessage taxMessageReceipt) {
        return taxMessageReceipt.getSource() == getConfigIntValue(ConfigurationParam.TARGET_SUBSYSTEM_ID);
    }

    private boolean isMessageFromFns(BaseMessage taxMessage) {
        return taxMessage instanceof TaxMessageTechDocument;
    }

    private boolean isEdoTechReceipt(BaseMessage taxMessage) {
        return taxMessage instanceof TaxMessageReceipt;
    }

    private void failHandleEdoMessage(TransportMessage transportMessage, String errorMessage) {
        LOG.info(String.format("В результате обработки ответа от ЭДО возникла ошибка: '%s' , ТС №%d",
                errorMessage, transportMessage.getId()));
        SimpleDateFormat dateFormat = new SimpleDateFormat(MESSAGE_EXPLANATION_DATE_FORMAT);
        transportMessage.setState(TransportMessageState.ERROR);
        transportMessage.setExplanation(dateFormat.format(new Date()) + " " + errorMessage);
    }

    //TODO следует перенести в EdoResponseHandler во избежании перегруженности сервисного класса
    private void handleFnsResponse(TransportMessage incomeTransportMessage, TaxMessageTechDocument taxMessageTechDoc) {
        UploadTransportDataResult uploadTransportDataResult = new UploadTransportDataResult();
        FileWrapper sharedFileNameFromFns = null;
        DeclarationShortInfo declarationShortInfo = null;
        Logger transportFileImporterLogger = new Logger();
        boolean isUploadStarted = false;
        try {
            TransportMessage sourceTransportMessage = geSourceTransportMessageFromEdoTechDoc(taxMessageTechDoc);
            if (sourceTransportMessage == null) {
                throw new IllegalStateException("Для данного ответа из ФНС не найдено исходное сообщение.");
            }

            declarationShortInfo = sourceTransportMessage.getDeclaration();
            if (!declarationDataService.existDeclarationData(declarationShortInfo.getId())) {
                throw new IllegalStateException("Не удалось определить налоговую форму в исходящем ТС.");
            }
            incomeTransportMessage.setDeclaration(declarationShortInfo);

            sharedFileNameFromFns = getFileFromFnsFileSharing(incomeTransportMessage.getId(),
                    taxMessageTechDoc);
            BlobData fnsResponseBlobData = storeFileInTransportMessage(incomeTransportMessage, sharedFileNameFromFns);

            LOG.info("Запущена процедура разбора файла от ФНС в автоматическом режиме");
            isUploadStarted = true;
            uploadTransportDataResult = uploadTransportDataService.processTransportFileUploading(
                    transportFileImporterLogger,
                    taUserService.getSystemUserInfo(),
                    fnsResponseBlobData.getName(),
                    fnsResponseBlobData.getInputStream(),
                    true
            );

            if (uploadTransportDataResult.getContentType() == null || uploadTransportDataResult.getMessageState() == null) {
                LOG.error("Непредвинная ошибка обработки ответа ФНС, реузльтат = " + uploadTransportDataResult +
                        ", содержимое логгера скрипта: " + transportFileImporterLogger.getEntries());
                throw new IllegalStateException("В результате работы алгоритма обработки ответа от ФНС " +
                        "произошла непредвинная ошибка.");
            }
            LOG.info("Процедура обработки файла от ФНС в автоматическом режиме завершена с результатом: " +
                    uploadTransportDataResult);
            incomeTransportMessage.setContentType(uploadTransportDataResult.getContentType());
        } catch (Exception e) {
            LOG.warn("В результате обработки ответа от ФНС произошла ошибка", e);

            failHandleEdoMessage(incomeTransportMessage, e.getMessage());

            uploadTransportDataResult.setMessageState(TransportMessageState.ERROR);
            uploadTransportDataResult.setProcessMessageResult(e.getMessage());
        } finally {
            if (isUploadStarted) {
                sendNotification(
                        uploadTransportDataResult.getMessageState(),
                        transportFileImporterLogger,
                        declarationShortInfo.getId(),
                        sharedFileNameFromFns.getName()
                );
            }
        }

        TaxMessageReceipt taxMessageReceipt = buildTaxMessageReceipt(
                taxMessageTechDoc.getUuid(),
                uploadTransportDataResult.getMessageState(),
                uploadTransportDataResult.getProcessMessageResult()
        );

        if (sharedFileNameFromFns != null) {
            try {
                sharedFileNameFromFns.delete();
            } catch (Exception e) {
                LOG.warn("Ошибка удаления файла " + sharedFileNameFromFns.getName() +
                        " из папки обмена для транспортного сообщения #" + incomeTransportMessage.getMessageUuid(), e);
            }
            LOG.info("Файл ФНС \"" + sharedFileNameFromFns.getName() + "\" удален из папки обмена");
        }

        try {
            String xmlMessage = toXml(taxMessageReceipt);
            sendTechReceiptToEdo(incomeTransportMessage, xmlMessage);
            createTechReceiptOutcomeTransportMessage(
                    taxMessageReceipt, xmlMessage, taxMessageTechDoc.getFileName(), declarationShortInfo);

            incomeTransportMessage.setState(uploadTransportDataResult.getMessageState());
            transportMessageService.sendAuditMessage(TRANSPORT_MESSAGE_CHANGE_NOTE_FORMAT, incomeTransportMessage); // 8.h
        } catch (Exception e) {
            failHandleEdoMessage(incomeTransportMessage, e.getMessage());
        }
    }

    private void sendNotification(TransportMessageState handleState, Logger logger, Long declarationId, String fileName) {
        LogBusinessDTO lastSendToEdoLog =
                logBusinessService.getLastByDeclarationIdAndEvent(declarationId, FormDataEvent.SEND_EDO);

        if (lastSendToEdoLog != null) {
            LOG.info(String.format("Отправка нотификации пользователю '%s' о завершении автоматической " +
                    "обработки ответа от ФНС", lastSendToEdoLog.getUserName()));
            TAUser declarationSender = taUserService.getUser(lastSendToEdoLog.getUserName());

            String fullDeclarationDescription = declarationDataService.getFullDeclarationDescription(declarationId);
            String notificationMessage;
            if (TransportMessageState.CONFIRMED == handleState) {
                notificationMessage = String.format("Загрузка файла полученного от ЭДО \"%s\" завершена. " +
                        "Выполнена загрузка ответа ФНС для формы %s", fileName, fullDeclarationDescription);
            } else if (TransportMessageState.ERROR == handleState) {
                notificationMessage = String.format("Загрузка файла \"%s\" завершена с ошибкой. " +
                        "Выполнена загрузка ответа ФНС для формы %s", fileName, fullDeclarationDescription);
            } else {
                LOG.error("Не определ результат обработки ответа от ФНС для файла ответа " + fileName);
                return;
            }

            Notification notification = new Notification();
            notification.setUserId(declarationSender.getId());
            notification.setCreateDate(new Date());
            notification.setText(notificationMessage);
            notification.setLogId(logEntryService.save(logger));
            notification.setNotificationType(NotificationType.DEFAULT);

            notificationService.create(Collections.singletonList(notification));

            LOG.info(String.format("Нотификация пользователю '%s' успешно отправлена", lastSendToEdoLog.getUserName()));
        } else {
            LOG.warn("Не найдена запись в истории формы об отправке в ЭДО");
        }
    }

    private BlobData storeFileInTransportMessage(TransportMessage transportMessage, FileWrapper sharedFileNameFromFns) {
        String fnsResponseBlobId = blobDataService.create(sharedFileNameFromFns.getInputStream(), sharedFileNameFromFns.getName());
        BlobData fnsResponseBlobData = blobDataService.get(fnsResponseBlobId);
        transportMessage.setBlob(fnsResponseBlobData);
        LOG.info("Файл ФНС '" + sharedFileNameFromFns.getName() + "'сохранен в БД и установлен ТС #" +
                transportMessage.getMessageUuid());
        return fnsResponseBlobData;
    }

    @NotNull
    private FileWrapper getFileFromFnsFileSharing(Long messageId, TaxMessageTechDocument taxMessageTechDocument) {
        String fileName = taxMessageTechDocument.getFileName();
        String sharingFolderPath = getTargetSystemSharingFolder();
        FileWrapper directory = ResourceUtils.getSharedResource(sharingFolderPath, false);
        String errorMessage = "В папке обмена не обнаружен файл: " + fileName +
                " для транспортного сообщения № " + messageId;
        if (directory.exists()) {
            try {
                FileWrapper sharedResource = ResourceUtils.getSharedResource(sharingFolderPath + "/" + fileName);
                LOG.info("Прочитан файл от ФНС из папки обмена " + sharedResource.getName());
                return sharedResource;
            } catch (Exception e) {
                throw new ServiceException(errorMessage);
            }
        } else {
            throw new ServiceException(errorMessage);
        }
    }

    private String getTargetSystemSharingFolder() {
        Configuration configuration = configurationService.fetchByEnum(ConfigurationParam.DOCUMENT_EXCHANGE_DIRECTORY);
        if (configuration == null || StringUtils.isEmpty(configuration.getValue())) {
            throw new ConfigurationParameterAbsentException("В конфигурационном параметре: \"" +
                    ConfigurationParam.DOCUMENT_EXCHANGE_DIRECTORY.getCaption() + "\" отсутствует информация.");
        }
        return configuration.getValue();
    }

    private TaxMessageReceipt buildTaxMessageReceipt(String incomeMessageUuid, TransportMessageState messageState, String message) {
        MessageStatus messageStatus = new MessageStatus();
        messageStatus.setCode(messageState.getIntValue());
        messageStatus.setDetail(message);

        TaxMessageReceipt taxMessageReceipt = new TaxMessageReceipt();
        taxMessageReceipt.setFormatVersion(MESSAGE_FORMAT_VERSION);
        taxMessageReceipt.setUuid(incomeMessageUuid);
        taxMessageReceipt.setSource(getConfigIntValue(ConfigurationParam.NDFL_SUBSYSTEM_ID));
        taxMessageReceipt.setDestination(getConfigIntValue(ConfigurationParam.TARGET_SUBSYSTEM_ID));
        taxMessageReceipt.setStatus(messageStatus);

        return taxMessageReceipt;
    }

    private void sendTechReceiptToEdo(TransportMessage transportMessage, String xmlMessage) {
        Integer taxMessageRetryCount = configurationService.getParamIntValue(ConfigurationParam.TAX_MESSAGE_RETRY_COUNT);
        Integer taxMessageReceiptWaitingSec =
                configurationService.getParamIntValue(ConfigurationParam.TAX_MESSAGE_RECEIPT_WAITING_TIME);
        try {
            trySendTechReceiptToEdo(xmlMessage, 1, taxMessageRetryCount, taxMessageReceiptWaitingSec);
            LOG.info("Ответная технологическая квитанция отправлена в ЭДО для сообщения #" +
                    transportMessage.getMessageUuid());
        } catch (ConfigurationParameterAbsentException e) {
            throw new ServiceException("В параметре: \"" + ConfigurationParam.JNDI_QUEUE_OUT.getCaption() +
                    "\" не прописано наименование очереди.");
        } catch (Exception e) {
            long senderSubsystemId = transportMessage.getSenderSubsystem().getId();
            // SenderSubsystem в транспортном сообщении не имеет имени, т.к. оно не заоплняется на этапе инициалазации
            Subsystem senderSubsystem = subsystemService.findById(senderSubsystemId);
            throw new ServiceException("Не удалось отправить XML-сообщение c идентификатором: " +
                    transportMessage.getMessageUuid() + " в систему: " + senderSubsystem.getName() + " .");
        }
    }

    private void trySendTechReceiptToEdo(final String messageXml, int attemptNumber, final Integer retryCount,
                                         final Integer waitingSec) {
        try {
            messageSender.sendMessage(messageXml);
        } catch (ConfigurationParameterAbsentException e) {
            throw e;
        } catch (Exception e) {
            if (attemptNumber < retryCount) {
                LOG.info("Неуспешная попытка " + attemptNumber + " отправить сообщение." +
                        " Повторная попытка отправки сообщения будет осуществлена через " + waitingSec +
                        " секунде еще " + retryCount + " раз через раз.");
                final int newAttemptNumber = ++attemptNumber;
                scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        trySendTechReceiptToEdo(messageXml, newAttemptNumber, retryCount, waitingSec);
                    }
                }, waitingSec, TimeUnit.SECONDS);
            } else {
                throw e;
            }
        }
    }

    private void createTechReceiptOutcomeTransportMessage(TaxMessageReceipt taxMessageReceipt, String message,
                                                          String attachFileName, DeclarationShortInfo declarationInfo) {
        TransportMessage transportMessage = transportMessageFactory.createOutcomeMessageToEdo(taxMessageReceipt, message);
        transportMessage.setInitiatorUser(taUserService.getSystemUserInfo().getUser());
        transportMessage.setContentType(TransportMessageContentType.TECH_RECEIPT);
        transportMessage.setSourceFileName(attachFileName);
        transportMessage.setDeclaration(declarationInfo);
        transportMessageService.create(transportMessage);
        LOG.info("Транспортное сообщение овтетной технологической квитанции создано #" +
                transportMessage.getMessageUuid());

        transportMessageService.sendAuditMessage(TRANSPORT_MESSAGE_CREATE_NOTE_FORMAT, transportMessage);
    }

    private int getConfigIntValue(ConfigurationParam param) {
        Integer result = configurationService.getParamIntValue(param);
        if (result == null) {
            throw new ServiceException(String.format("не задан конфигурационный параметр: \"%s\"", param.getCaption()));
        }
        return result;
    }

    private String toXml(Object obj) {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(obj.getClass());
        Map<String, Object> props = new HashMap<>();
        props.put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setMarshallerProperties(props);
        Writer writer = new StringWriter();
        Result result = new StreamResult(writer);
        marshaller.marshal(obj, result);
        return writer.toString();
    }

    private class SendDeclarationToEdoContext {
        DeclarationData declarationData;
        DeclarationTemplate declarationTemplate;
        TAUserInfo userInfo;
        Logger logger;
        DeclarationDataFile declarationXmlFile;
        LogLevel resultLogLevel = LogLevel.ERROR;

        SendDeclarationToEdoContext(DeclarationData declarationData, TAUserInfo userInfo, Logger logger) {
            this.declarationData = declarationData;
            declarationTemplate = declarationTemplateService.get(declarationData.getDeclarationTemplateId());
            this.userInfo = userInfo;
            this.logger = logger;
        }

        private void sendToEdo() {
            LockData lockData = null;
            File tmpXmlFile = null;
            try {
                declarationXmlFile = findDeclarationXmlFile();
                if (DeclarationDataPermission.SEND_EDO.isGranted(getAuthUser(), declarationData, logger, declarationXmlFile.getFileName())) {
                    lockData = declarationLocker.establishLock(declarationData.getId(), OperationType.SEND_EDO, userInfo, logger);
                    if (lockData != null) {
                        tmpXmlFile = createTempFile(declarationXmlFile);
                        validateXml(tmpXmlFile, declarationXmlFile.getFileName());
                        copyToDocumentExchangeDirectory(tmpXmlFile, declarationXmlFile.getFileName());
                        TaxMessageDocument message = buildEdoMessage(declarationXmlFile.getFileName(), userInfo);
                        sendToEdo(message);
                        declarationDataService.updateDocState(declarationData.getId(), SENDING_TO_EDO.getId());
                        logSuccessToLogger();
                        auditService.add(null, userInfo, declarationData, "Отправка в ЭДО отчетной формы: № " + declarationData.getId(), null);
                    }
                }
            } catch (Exception e) {
                LOG.error(e);
                String fileNameString = declarationXmlFile != null ? " \"" + declarationXmlFile.getFileName() + "\"" : "";
                logger.error("Ошибка отправки в ЭДО файла%s по отчетной форме %s. Причина: %s.",
                        fileNameString, buildDeclarationDescription(), e.getMessage());
            } finally {
                AppFileUtils.deleteTmp(tmpXmlFile);
                if (lockData != null) {
                    lockDataService.unlock(lockData.getKey());
                }
                logToDeclarationHistory();
            }
        }

        private void sendToEdo(TaxMessageDocument message) {
            String messageXml = toXml(message);
            try {
                messageSender.sendMessage(messageXml);
            } catch (Exception e) {
                LOG.error("В процессе отправки сообщения в ЭДО произошла ошибка", e);
                throw new ServiceException("файл не удалось поместить в системную очередь для отправки");
            }
            createTransportMessage(message, messageXml);
        }

        private DeclarationDataFile findDeclarationXmlFile() {
            List<DeclarationDataFile> declarationDataFiles = declarationDataService.findAllFilesByDeclarationIdAndType(declarationData.getId(), AttachFileType.OUTGOING_TO_FNS);
            if (declarationDataFiles.size() < 1) {
                throw new ServiceException("для ОНФ не найден файл для отправки");
            } else if (declarationDataFiles.size() > 1) {
                throw new ServiceException("для ОНФ найдено более одного файла для отправки.");
            } else {
                return declarationDataFiles.get(0);
            }
        }

        private void validateXml(File xmlFile, String fileName) {
            if (!validateXMLService.validate(declarationData, new Logger(), xmlFile, fileName, null)) {
                throw new ServiceException("файл не соответствует xsd схеме");
            }
        }

        private void copyToDocumentExchangeDirectory(File file, String fileName) {
            Configuration dirExchangeConfig = configurationService.fetchByEnum(ConfigurationParam.DOCUMENT_EXCHANGE_DIRECTORY);
            if (dirExchangeConfig != null) {
                FileWrapper directory = ResourceUtils.getSharedResource(dirExchangeConfig.getValue(), false);
                if (directory.exists()) {
                    try {
                        FileWrapper xmlFileWrapper = ResourceUtils.getSharedResource(dirExchangeConfig.getValue() + "/" + fileName, false);
                        try (InputStream inputStream = new FileInputStream(file);
                             OutputStream outputStream = new FileOutputStream(xmlFileWrapper.getFile())) {
                            IOUtils.copy(inputStream, outputStream);
                        }
                    } catch (Exception e) {
                        throw new ServiceException("файл не удалось выложить в папку обмена \"" + dirExchangeConfig.getValue() + "\"", e);
                    }
                } else {
                    throw new ServiceException("файл не удалось выложить в папку обмена \"" + dirExchangeConfig.getValue() + "\"");
                }
            } else {
                throw new ServiceException("не задан конфигурационный параметр: \"" + ConfigurationParam.DOCUMENT_EXCHANGE_DIRECTORY.getCaption() + "\"");
            }
        }

        private TaxMessageDocument buildEdoMessage(String fileName, TAUserInfo userInfo) {
            TaxMessageDocument taxMessageDocument = new TaxMessageDocument();
            taxMessageDocument.setFormatVersion(MESSAGE_FORMAT_VERSION);
            taxMessageDocument.setUuid(UUID.randomUUID().toString().toLowerCase());
            taxMessageDocument.setSource(getConfigIntValue(ConfigurationParam.NDFL_SUBSYSTEM_ID));
            taxMessageDocument.setDestination(getConfigIntValue(ConfigurationParam.TARGET_SUBSYSTEM_ID));
            taxMessageDocument.setLogin(userInfo.getUser().getLogin());
            taxMessageDocument.setFilename(fileName);
            return taxMessageDocument;
        }

        private void createTransportMessage(TaxMessageDocument message, String messageXml) {
            TransportMessage transportMessage = transportMessageFactory.createOutcomeMessageToEdo(message, messageXml);
            transportMessage.setInitiatorUser(userInfo.getUser());
            transportMessage.setBlob(blobDataService.get(declarationXmlFile.getUuid()));
            transportMessage.setContentType(TransportMessageContentType.fromDeclarationType(declarationTemplate.getType()));
            transportMessage.setSourceFileName(declarationXmlFile.getFileName());
            transportMessage.setDeclaration(DeclarationShortInfo.builder().id(declarationData.getId()).build());
            transportMessageService.create(transportMessage);
        }

        private String buildDeclarationDescription() {
            return declarationDataService.getFullDeclarationDescription(declarationData.getId());
        }

        private void logSuccessToLogger() {
            String addMessage;
            RefBookDocState docState = commonRefBookService.fetchRecord(RefBook.Id.DOC_STATE.getId(), declarationData.getDocStateId());
            resultLogLevel = LogLevel.INFO;
            if (NOT_SENT.getId().equals(declarationData.getDocStateId())) {
                addMessage = "";
            } else if (EXPORTED.getId().equals(declarationData.getDocStateId())) {
                addMessage = "Внимание: Данный xml-файл ранее был выгружен в архиве для отправки в ЭДО (Состояние ЭД: " + docState.getName() + ").";
            } else if (ERROR.getId().equals(declarationData.getDocStateId())) {
                addMessage = "Внимание! Данный xml-файл уже отправлялся в ЭДО, и для него от ФНС была получена техническая ошибка (Состояние ЭД: " + docState.getName() + ").";
            } else {
                resultLogLevel = LogLevel.WARNING;
                addMessage = "Внимание! Данный xml-файл уже отправлялся в ЭДО, и для него был получен ответ ФНС (Состояние ЭД: " + docState.getName() + ").";
            }
            logger.log(resultLogLevel, String.format("В ЭДО отправлен xml-файл \"%s\" по отчетной форме %s. %s",
                    declarationXmlFile.getFileName(),
                    buildDeclarationDescription(),
                    addMessage));
        }

        private void logToDeclarationHistory() {
            String note;
            String fileNameString = declarationXmlFile != null ? " \"" + declarationXmlFile.getFileName() + "\"" : "";
            if (resultLogLevel == LogLevel.INFO) {
                note = "Выполнена операция \"Отправка xml-файла в ЭДО\"" + fileNameString + ".";
            } else if (resultLogLevel == LogLevel.WARNING) {
                note = "Выполнена операция \"Отправка xml-файла в ЭДО\"" + fileNameString + " (присутствуют нефатальные ошибки).";
            } else {
                note = "Не выполнена операция \"Отправка xml-файла в ЭДО\"" + fileNameString + " (присутствуют фатальные ошибки).";
            }
            logBusinessService.logFormEvent(declarationData.getId(), FormDataEvent.SEND_EDO, logger.getLogId(), note, userInfo);
        }

        private File createTempFile(DeclarationDataFile declarationDataFile) throws IOException {
            BlobData xmlBlobData = blobDataService.get(declarationDataFile.getUuid());
            File tmpXmlFile = File.createTempFile(declarationDataFile.getFileName(), ".xml");
            try (OutputStream outputStream = new FileOutputStream(tmpXmlFile)) {
                IOUtils.copy(xmlBlobData.getInputStream(), outputStream);
            }
            return tmpXmlFile;
        }

        private User getAuthUser() {
            return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
    }
}
