package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageContentType;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageState;
import lombok.*;

@Data
public class UploadTransportDataResult {
    private TransportMessageState messageState;
    private TransportMessageContentType contentType;
    private String processMessageResult;
    private String notificationMessage;
}
