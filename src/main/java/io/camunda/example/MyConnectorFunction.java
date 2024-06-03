package io.camunda.example;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.example.dto.MyConnectorRequest;
import io.camunda.example.dto.MyConnectorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@OutboundConnector(
        name = "MYCONNECTOR",
        inputVariables = {"authentication", "message"},
        type = "BenType")
@ElementTemplate(
        id = "io.camunda.connector.Template.v1",
        name = "Template connector",
        version = 1,
        description = "Description du connecteur",
        icon = "icon.svg",
        documentationRef = "https://docs.camunda.io/docs/components/connectors/out-of-the-box-connectors/available-connectors-overview/",
        propertyGroups = {
                @ElementTemplate.PropertyGroup(id = "authentication", label = "Authentication"),
                @ElementTemplate.PropertyGroup(id = "compose", label = "Compose")
        },
        inputDataClass = MyConnectorRequest.class)
public class MyConnectorFunction implements OutboundConnectorFunction {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyConnectorFunction.class);

  @Override
  public Object execute(OutboundConnectorContext context) {
    final var connectorRequest = context.bindVariables(MyConnectorRequest.class);
    return executeConnector(connectorRequest);
  }

  private MyConnectorResult executeConnector(final MyConnectorRequest connectorRequest) {
    LOGGER.info("Executing my connector with request {}", connectorRequest);
    String message = connectorRequest.message();
    if (message != null && message.toLowerCase().startsWith("fail")) {
      throw new ConnectorException("FAIL", "My property started with 'fail', was: " + message);
    }
    sendEmail("abijj@gmail.com", "Test Email", "Cette phase est un TEST");
    return new MyConnectorResult("Message received: " + message);
  }

  private void sendEmail(String to, String subject, String body) {
    String from = "your-email@gmail.com"; // Remplace par ton email
    String host = "smtp.gmail.com"; // Serveur SMTP

    Properties properties = System.getProperties();
    properties.setProperty("mail.smtp.host", host);
    properties.setProperty("mail.smtp.port", "587");
    properties.setProperty("mail.smtp.auth", "true");
    properties.setProperty("mail.smtp.starttls.enable", "true");

    Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
      protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
        return new javax.mail.PasswordAuthentication("your-email@gmail.com", "your-password"); // Remplace par ton email et mot de passe
      }
    });

    try {
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(from));
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
      message.setSubject(subject);
      message.setText(body);

      Transport.send(message);
      LOGGER.info("Email sent successfully to {}", to);
    } catch (MessagingException mex) {
      mex.printStackTrace();
      LOGGER.error("Error sending email", mex);
    }
  }
}
