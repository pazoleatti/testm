package com.aplana.sbrf.taxaccounting.service.jms;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.jms.support.destination.BeanFactoryDestinationResolver;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.InitialContext;
import javax.naming.NamingException;


@Configuration
@EnableJms
@Profile(value = {"development", "jms"})
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

        @Bean
        public BeanFactoryDestinationResolver destinationResolver(BeanFactory beanFactory) {
                return new BeanFactoryDestinationResolver(beanFactory);
        }

        @Bean
        public JmsTemplate jmsTemplate(MessageConverter messageConverter,
                                       BeanFactoryDestinationResolver destinationResolver) throws NamingException {
                JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
                jmsTemplate.setMessageConverter(messageConverter);
                jmsTemplate.setDestinationResolver(destinationResolver);
                return jmsTemplate;
        }


        @Bean
        public JmsListenerContainerFactory<?> jmsListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                          MessageConverter messageConverter,
                                                                          BeanFactoryDestinationResolver destinationResolver) {

                DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
                factory.setConnectionFactory(connectionFactory);
                factory.setDestinationResolver(destinationResolver);
                factory.setSessionTransacted(true);
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

        /**
         * Очередь входящих запросов, получается по JNDI у вебсферы.
         */
        @Bean(name = JmsBaseConfig.FROM_NDFL_QUEUE)
        public Destination fundFromNdflSubsystemQueue() throws NamingException {
                return (Destination) new InitialContext()
                        .lookup("java:comp/env/jms/FundFromNdflSubsystemQueue");
        }

        /**
         * Очередь исходящих запросо, получается по JNDI у вебсферы.
         */
        @Bean(name = JmsBaseConfig.TO_NDFL_QUEUE)
        public Destination fundToNdflSubsystemQueue() throws NamingException {
                return (Destination) new InitialContext()
                        .lookup("java:comp/env/jms/FundToNdflSubsystemQueue");
        }
}
