package com.aplana.sbrf.taxaccounting.service.jms;

import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.jms.support.destination.JndiDestinationResolver;

import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;


@Configuration
@EnableJms
@Profile(value = {"development", "jms"})
public class JmsBaseConfig {
        public static final String TO_NDFL_QUEUE_JNDI_NAME = "jms/EdoRequestQueue";

        @Autowired
        private ConfigurationService configurationService;
        /**
         * Конвертер сообщений из xml в объектные модели
         */
        @Bean
        public MessageConverter messageConverter() {
            return new SimpleMessageConverter();
        }

        @Bean
        public JndiDestinationResolver destinationResolver() {
                return new JndiDestinationResolver();
        }

        @Bean
        public JmsTemplate jmsTemplate(MessageConverter messageConverter,
                                       JndiDestinationResolver destinationResolver) throws NamingException {
                JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
                jmsTemplate.setMessageConverter(messageConverter);
                jmsTemplate.setDestinationResolver(destinationResolver);
                return jmsTemplate;
        }


        @Bean
        public JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                          MessageConverter messageConverter,
                                                                          JndiDestinationResolver destinationResolver) {

                DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
                factory.setConnectionFactory(connectionFactory);
                factory.setDestinationResolver(destinationResolver);
                factory.setMaxMessagesPerTask(1);
                factory.setMessageConverter(messageConverter);
                return factory;
        }

        /**
         * Фабрика соединений с MQ для очередей, получается по JNDI у вебсферы.
         */
        @Bean(name = "fundConnectionFactory")
        public ConnectionFactory connectionFactory() throws NamingException {
                return (ConnectionFactory) new InitialContext()
                        .lookup("java:comp/env/jms/FundConnectionFactory");
        }

}
