package com.aplana.sbrf.taxaccounting.service.impl.transport.edo;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.jms.MessageStatus;
import com.aplana.sbrf.taxaccounting.model.jms.TaxMessageReceipt;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageContentType;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageFilter;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageState;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocState;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocState.*;
import static com.aplana.sbrf.taxaccounting.service.impl.transport.edo.EdoMessageServiceImpl.MESSAGE_EXPLANATION_DATE_FORMAT;
import static com.aplana.sbrf.taxaccounting.service.impl.transport.edo.EdoMessageServiceImpl.TRANSPORT_MESSAGE_CHANGE_NOTE_FORMAT;

@Service
public class EdoResponseHandlerImpl implements EdoResponseHandler {
    private static final Log LOG = LogFactory.getLog(EdoResponseHandlerImpl.class);

    @Autowired
    private AuditService auditService;
    @Autowired
    private TransportMessageService transportMessageService;
    @Autowired
    private LogBusinessService logBusinessService;
    @Autowired
    private TAUserService taUserService;
    @Autowired
    private DeclarationDataService declarationDataService;
    @Autowired
    private CommonRefBookService commonRefBookService;

    @Override
    public void handleTechReceipt(TransportMessage transportMessage, TaxMessageReceipt taxMessageReceipt) {
        transportMessage.setContentType(TransportMessageContentType.TECH_RECEIPT);

        TransportMessage sourceTransportMessage = getSourceMessageFromEdoTechReceipt(taxMessageReceipt);
        if (sourceTransportMessage == null) {
            throw new ServiceException("Для данной технологической квитанции не найдено исходное сообщение");
        }

        updateSourceTransportMessageState(sourceTransportMessage, taxMessageReceipt.getStatus());
        transportMessageService.sendAuditMessage(TRANSPORT_MESSAGE_CHANGE_NOTE_FORMAT, sourceTransportMessage); // 9.e

        DeclarationData declarationData = declarationDataService.get(sourceTransportMessage.getDeclaration().getId());
        if (declarationData == null) {
            String errorMessage = "Не удалось найти налоговую форму № " + sourceTransportMessage.getDeclaration().getId() +
                    " для изменения \"Состояние ЭД\" на " + SENT_TO_EDO.getName() + ". " +
                    "Идентификатор транспортного сообщения: " + transportMessage.getMessageUuid();
            throw new ServiceException(errorMessage);
        }
        updateDeclarationState(declarationData, taxMessageReceipt.getStatus(), sourceTransportMessage);
        auditService.add(null, taUserService.getSystemUserInfo(), declarationData,
                "Изменение \"Состояние ЭД\", для отчетной формы: № " + declarationData.getId(), null); // 9.i

        transportMessage.setState(TransportMessageState.CONFIRMED);
        transportMessage.setDeclaration(sourceTransportMessage.getDeclaration());
        transportMessageService.sendAuditMessage(TRANSPORT_MESSAGE_CHANGE_NOTE_FORMAT, transportMessage); // 9.k
    }

    private TransportMessage getSourceMessageFromEdoTechReceipt(TaxMessageReceipt taxMessageReceipt) {
        TransportMessageFilter transportMessageFilter = new TransportMessageFilter();
        transportMessageFilter.setMessageUuid(taxMessageReceipt.getUuid());
        return transportMessageService.findFirstByFilter(transportMessageFilter);
    }

    private void updateSourceTransportMessageState(TransportMessage transportMessage, MessageStatus status) {
        TransportMessageState messageState = TransportMessageState.fromInt(status.getCode());
        if (TransportMessageState.CONFIRMED == messageState) {
            transportMessage.setState(TransportMessageState.CONFIRMED);
        } else if (TransportMessageState.ERROR == messageState) {
            transportMessage.setState(TransportMessageState.ERROR);

            SimpleDateFormat dateFormat = new SimpleDateFormat(MESSAGE_EXPLANATION_DATE_FORMAT);
            String sourceTransportMessageExplanation = transportMessage.getExplanation();
            String taxMessageErrorDetail = dateFormat.format(new Date()) + " " + status.getDetail();
            if (StringUtils.isEmpty(sourceTransportMessageExplanation)) {
                transportMessage.setExplanation(taxMessageErrorDetail);
            } else {
                transportMessage.setExplanation(sourceTransportMessageExplanation + "\n-----------------\n" +
                        taxMessageErrorDetail);
            }
        }
        transportMessageService.update(transportMessage);
    }

    private void updateDeclarationState(DeclarationData declaration, MessageStatus msgStatus, TransportMessage sourceMsg) {
        TransportMessageState incomeTransportMessageState = TransportMessageState.fromInt(msgStatus.getCode());
        RefBookDocState newDeclarationState = null;
        List<Long> confirmedDocStates = Arrays.asList(SENDING_TO_EDO.getId(), NOT_SENT.getId(), EXPORTED.getId());

        if (TransportMessageState.CONFIRMED == incomeTransportMessageState
                && confirmedDocStates.contains(declaration.getDocStateId())) {
            newDeclarationState = SENT_TO_EDO;
        } else if (TransportMessageState.ERROR == incomeTransportMessageState
                && TransportMessageState.CANCELED != sourceMsg.getState()
                && SENDING_TO_EDO.getId().equals(declaration.getDocStateId())
        ) {
            newDeclarationState = NOT_SENT;
        }

        if (newDeclarationState != null) {
            Logger localLogger = new Logger();
            declarationDataService.updateDocState(declaration.getId(), newDeclarationState.getId());
            LOG.info("Статус декларации #" + declaration.getId() + " изменен с " +
                    declaration.getDocStateId() + " на " + newDeclarationState.getId());
            RefBookDocState oldRefBookDocState = commonRefBookService.fetchRecord(RefBook.Id.DOC_STATE.getId(), declaration.getDocStateId());
            RefBookDocState newRefBookDocState = commonRefBookService.fetchRecord(RefBook.Id.DOC_STATE.getId(), newDeclarationState.getId());
            logBusinessService.logFormEvent(
                    declaration.getId(),
                    FormDataEvent.CHANGE_STATUS_ED,
                    localLogger.getLogId(),
                    "Изменено \"Состояние ЭД\": \"[" + oldRefBookDocState.getName() + "]\" -> \"[" +  newRefBookDocState.getName() + "]\" на основании полученной технологической квитанции от ЭДО",
                    taUserService.getSystemUserInfo()
            );
        }
    }

}
