package com.aplana.sbrf.taxaccounting.service.impl.transport.edo;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.jms.TaxMessageDocument;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.messaging.DeclarationShortInfo;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageContentType;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageState;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocState;
import com.aplana.sbrf.taxaccounting.model.util.AppFileUtils;
import com.aplana.sbrf.taxaccounting.permissions.DeclarationDataPermission;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.EdoMessageService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.service.TransportMessageService;
import com.aplana.sbrf.taxaccounting.service.ValidateXMLService;
import com.aplana.sbrf.taxaccounting.service.component.lock.locker.DeclarationLocker;
import com.aplana.sbrf.taxaccounting.service.jms.transport.MessageSender;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.xml.bind.Marshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocState.*;

@Service
public class EdoMessageServiceImpl implements EdoMessageService {
    private static final Log LOG = LogFactory.getLog(EdoMessageService.class);

    private final DeclarationDataService declarationDataService;
    private final DeclarationTemplateService declarationTemplateService;
    private final DeclarationLocker declarationLocker;
    private final BlobDataService blobDataService;
    private final ConfigurationService configurationService;
    private final ValidateXMLService validateXMLService;
    private final MessageSender messageSender;
    private final TransportMessageService transportMessageService;
    private final CommonRefBookService commonRefBookService;
    private final LogBusinessService logBusinessService;
    private final LockDataService lockDataService;

    @Autowired
    public EdoMessageServiceImpl(DeclarationDataService declarationDataService, DeclarationTemplateService declarationTemplateService, DeclarationLocker declarationLocker,
                                 BlobDataService blobDataService, ConfigurationService configurationService, ValidateXMLService validateXMLService,
                                 @Autowired(required = false) MessageSender messageSender, TransportMessageService transportMessageService, CommonRefBookService commonRefBookService,
                                 LogBusinessService logBusinessService, LockDataService lockDataService) {
        this.declarationDataService = declarationDataService;
        this.declarationTemplateService = declarationTemplateService;
        this.declarationLocker = declarationLocker;
        this.blobDataService = blobDataService;
        this.configurationService = configurationService;
        this.validateXMLService = validateXMLService;
        this.messageSender = messageSender;
        this.transportMessageService = transportMessageService;
        this.commonRefBookService = commonRefBookService;
        this.logBusinessService = logBusinessService;
        this.lockDataService = lockDataService;
    }

    @Override
    public SendToEdoResult sendToEdo(List<Long> declarationDataIds, TAUserInfo userInfo, Logger logger) {
        SendToEdoResult result = new SendToEdoResult();
        for (long declarationId : declarationDataIds) {
            DeclarationData declarationData = declarationDataService.get(declarationId);
            SendToEdoContext sendToEdoContext = new SendToEdoContext(declarationData, userInfo, logger);
            sendToEdoContext.sendToEdo();
            result.put(sendToEdoContext.resultLogLevel, declarationData);
        }
        return result;
    }

    private class SendToEdoContext {
        DeclarationData declarationData;
        DeclarationTemplate declarationTemplate;
        TAUserInfo userInfo;
        Logger logger;
        DeclarationDataFile declarationXmlFile;
        LogLevel resultLogLevel = LogLevel.ERROR;

        SendToEdoContext(DeclarationData declarationData, TAUserInfo userInfo, Logger logger) {
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
                        declarationDataService.updateDocState(declarationData.getId(), SENT_EDO.getId());
                        logSuccessToLogger();
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
            taxMessageDocument.setFormatVersion("2.0");
            taxMessageDocument.setUuid(UUID.randomUUID().toString().toLowerCase());
            taxMessageDocument.setSource(getConfigIntValue(ConfigurationParam.NDFL_SUBSYSTEM_ID));
            taxMessageDocument.setDestination(getConfigIntValue(ConfigurationParam.TARGET_SUBSYSTEM_ID));
            taxMessageDocument.setLogin(userInfo.getUser().getLogin());
            taxMessageDocument.setFilename(fileName);
            return taxMessageDocument;
        }

        private String toXml(Object obj) {
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setClassesToBeBound(TaxMessageDocument.class);
            Map<String, Object> props = new HashMap<>();
            props.put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setMarshallerProperties(props);
            Writer writer = new StringWriter();
            Result result = new StreamResult(writer);
            marshaller.marshal(obj, result);
            return writer.toString();
        }

        private void createTransportMessage(TaxMessageDocument message, String messageXml) {
            TransportMessage transportMessage = new TransportMessage();
            transportMessage.setDateTime(LocalDateTime.fromDateFields(message.getDateTime()));
            transportMessage.setMessageUuid(message.getUuid());
            transportMessage.setSenderSubsystem(new Subsystem(getConfigIntValue(ConfigurationParam.NDFL_SUBSYSTEM_ID)));
            transportMessage.setReceiverSubsystem(new Subsystem(getConfigIntValue(ConfigurationParam.TARGET_SUBSYSTEM_ID)));
            transportMessage.setInitiatorUser(userInfo.getUser());
            transportMessage.setBody(messageXml);
            transportMessage.setBlob(blobDataService.get(declarationXmlFile.getUuid()));
            transportMessage.setState(TransportMessageState.SENT);
            transportMessage.setContentType(TransportMessageContentType.fromDeclarationType(declarationTemplate.getType()));
            transportMessage.setType(TransportMessageType.OUTGOING);
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

        private int getConfigIntValue(ConfigurationParam param) {
            Configuration configuration = configurationService.fetchByEnum(param);
            if (configuration != null) {
                try {
                    return Integer.valueOf(configuration.getValue());
                } catch (NumberFormatException e) {
                    throw new ServiceException("не задан конфигурационный параметр: \"" + param.getCaption() + "\"");
                }
            } else {
                throw new ServiceException("не задан конфигурационный параметр: \"" + param.getCaption() + "\"");
            }
        }

        private User getAuthUser() {
            return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
    }
}
