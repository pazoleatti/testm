package com.aplana.sbrf.taxaccounting.service.jms;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;


@Configuration
@EnableJms
public class JmsBaseConfig {

        public static final String FROM_NDFL_QUEUE = "fundFromNdflSubsystemQueue";

        public static final String TO_NDFL_QUEUE = "fundToNdflSubsystemQueue";

        /**
         * Конвертер сообщений из xml в объектные модели
         */
        @Bean
        public MessageConverter messageConverter() {
            return new SimpleMessageConverter();
        }
}
