package io.agritrack.philosofish.common;

import java.util.Calendar;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
    static final String CLIENT = "AVRAMAR";
    static final String FROM = "support@agritrack.io";
    static final String FROMNAME = "support";
    static final String TO = "dimitrism@agritrack.io" + "," +
            "thanosm@agritrack.io" + "," +
            "nikosb@agritrack.io" + "," +
            "nikosp@agritrack.io" + "," +
            "georges@agritrack.io" + "," +
            "vlasist@agritrack.io";

    static final String SUBJECT = String.format("[%s] Support Button", CLIENT);

    static final String BODY = String.join(
            System.getProperty("line.separator"),
            String.format("<h1>%s Terminal Support</h1>", CLIENT),
            "<p>Date: %s",
            "<p>Requester Name: %s",
            "<p>Requester Tel: %s",
            "<p>Issue Description: %s"
    );

//    private static final String SMTP_HOST = "smtp.gmail.com";
//    private static final String SMTP_USERNAME = null;
//    private static final String SMTP_PASSWORD = null;
//    private static final int SMTP_PORT = 465;

    private static final String SMTP_HOST = "email-smtp.eu-central-1.amazonaws.com";
    private static final String SMTP_USERNAME = "AKIAWFKFS5ZIDMQVWA56";
    private static final String SMTP_PASSWORD = "BENk6V9OFz3ScxRS7LXXjfKCjyOjlZHGp4FJuBVUE6d9";
    private static final int SMTP_PORT = 587;

    public static void sendEmail(String senderName, String senderTel, String issueDescription) throws Exception {

        // Create a Properties object to contain connection configuration information.
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // GMAIL only properties
        if("smtp.gmail.com".equalsIgnoreCase(SMTP_HOST)){
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class",
                    "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        }

        // Create a Session object to represent a mail session with the specified properties.
        Session session = Session.getDefaultInstance(props);

        // Create a message with the specified information.
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM,FROMNAME));
        msg.setRecipients(Message.RecipientType.TO, TO);
        msg.setSubject(SUBJECT, "UTF-8");

        String messageBody = String.format(BODY, Calendar.getInstance().getTime(), senderName, senderTel, issueDescription);
        msg.setContent(messageBody,"text/html; charset=UTF-8");

        // Create a transport.

        // Send the message.
        try (Transport transport = session.getTransport()) {
            System.out.println("Sending...");
            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(SMTP_HOST, SMTP_USERNAME, SMTP_PASSWORD);

            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
        } catch (Exception ex) {
            throw new Exception(ex);
        }
    }
}