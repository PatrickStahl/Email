/**
 * muss hier noch die nutzerdaten eingeben lassen 
 * und paar methoden einbauen dass man email wie 
 * im meilenstein einzeln anzeigen lassen kann
 */


package email;

import javax.mail.*;
import java.util.Properties;

public class JavaMailClient {
    public static final String USERNAME = "patrick.stahl@uni-jena.de";
    public static final String PASSWORD = "...";

    public static void main() throws Exception {
        
        //sets properties for session
        Properties props = new Properties();
        props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.pop3.socketFactory.fallback", "true");
        props.put("mail.pop3.socketFactory.port", "995");
        props.put("mail.pop3.port", "995");
        props.put("mail.pop3.host", "pop3.uni-jena.de");
        props.put("mail.pop3.user", JavaMailClient.USERNAME);
        props.put("mail.store.protocol", "pop3");
        props.put("mail.pop3.ssl.protocols", "TLSv1.2");

        //creates authentificator
        Authenticator auth = new Authenticator() 
        {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() 
            {
                return new PasswordAuthentication(JavaMailClient.USERNAME, JavaMailClient.PASSWORD);
            }
        };

        Session session = Session.getDefaultInstance(props, auth);

        //connect to store with the login data
        Store store = session.getStore("pop3");
        store.connect("pop3.uni-jena.de", JavaMailClient.USERNAME, JavaMailClient.PASSWORD);

        //opens inbox folder
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        //Retrieve the messages from the folder
        Message[] messages = inbox.getMessages();
        for (Message message : messages) 
        {
            message.writeTo(System.out);
        }

        //closes folder and connection
        inbox.close(false);
        store.close();
    }
}