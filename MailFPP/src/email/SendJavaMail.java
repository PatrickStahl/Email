package email;

import javax.mail.Session;

import java.util.Properties;
import java.util.Scanner;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class SendJavaMail 
{
    public static void main() throws AddressException, MessagingException
    {

        System.out.println("\u001B[34mEnter the host you want to connect to ('pop3.uni-jena.de'): \u001B[0m");
        String host;
        Scanner scanner = new Scanner(System.in);
        while (true) 
        {
            host = scanner.nextLine();

            if (host.isEmpty()) 
            {
                host = "smtp.uni-jena.de";
                break;
            }
            if (host.contains(" ")) 
            {
                System.out.println("\u001B[31mHost cannot contain spaces, please try again!\u001B[0m");
            }
            // user typed a valid server name (syntaxwise)
            else 
            {
                break;
            }
        }

        boolean ssl = false;
        System.out.println("\u001B[34mDo you want to connect to the server with SSL?: \u001B[0m");
        while (true) 
        {
            String answer = scanner.nextLine();

            // == doesnt work so i used equals
            if (answer.equals("yes"))
            {
                ssl = true;
                break;
            } 
            else if (answer.equals("no")) 
            {
                break;
            } 
            else 
            {
                System.out.println("\u001B[31mNo valid input, please try again!\u001B[0m");
            }
        }


        System.out.println("\u001B[34mEnter the port you want to connect to (587): \u001B[0m");
        int port;
        while (true) 
        {
            String portNumber = scanner.nextLine();
            if (portNumber.isEmpty()) 
            {
                if (ssl) 
                {
                    port = 587;
                } 
                else 
                {
                    port = 465;
                }
                break;
            }
            try 
            {
                port = Integer.parseInt(portNumber);
                break;
            } 
            catch (NumberFormatException e) 
            {
                System.out.println("\u001B[31mOnly numbers are valid inputs, please try again!\u001B[0m");
            }
        }


        System.out.println("\u001B[34mEnter your email address ('max.mustermensch@uni-jena.de'): \u001B[0m");
        String email;
        while (true) 
        {
            email = scanner.nextLine();

            if (email.isEmpty()) 
            {
                email = "patrick.stahl@uni-jena.de";
                //System.out.println("\u001B[31mNo email entered, please try again!\u001B[0m");
            }

            if (email.contains("@")) 
            {
                break;
            } 
            else 
            {
                System.out.println("\u001B[31mNo valid email entered, please try again!\u001B[0m");
            }
        }
        //ist das zu russisch? eigentlich nicht
        String emailFinal = email;
        
        // Get the password that the user wants to use
        System.out.println("\u001B[34mEnter your password ('password'): \u001B[0m");
        String password;
        while (true) 
        {
            password = new String(System.console().readPassword());

            // if user doesnt enter a password he has to try again until he does
            if (password.isEmpty()) 
            {
                password = "";
                break;
                //System.out.println("\u001B[31mNo password entered, please try again!\u001B[0m");
            } 
            else 
            {
                break;
            }
        }
        String passwordFinal = password;




        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", port);
        

        if(ssl == true)
        {
            prop.put("mali.smtp.ssl.enable", "true");
            prop.put("mail.smtp.ssl.trust", host);
            prop.put("mail.smtp.starttls.enable", "true");
        }

        Session session = Session.getInstance(prop, new Authenticator() 
        {
            protected PasswordAuthentication getPasswordAuthentication() 
            {
                return new PasswordAuthentication(emailFinal, passwordFinal);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(email));

        sendMail.send(message);
        while(true)
        {
            System.out.println("\u001B[34mDo you want to send another mail? [yes]/[no] \u001B[0m");
            String answer = scanner.nextLine();
            if(answer.equals("yes"))
            {
                sendMail.send(message);
            }
            else if(answer.equals("no"))
            {
                System.out.println("\u001B[34mOk, closing the connection... \u001B[0m");
                break;
            }
            else
            {
                System.out.println("\u001B[34mWrong input, use [yes] or [no] \u001B[0m");
            }
        }
        scanner.close();
    }
}

class sendMail
{

    public static void send(Message message) throws AddressException, MessagingException
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\u001B[34mEnter the receiver of the mail: \u001B[0m");
        String receiver;
        while (true) 
        {
            receiver = scanner.nextLine();

            // if user doesnt enter a password he has to try again until he does
            if (receiver.isEmpty()) 
            {
                receiver = "patrickstahl880@gmail.com";
                break;
                //System.out.println("\u001B[31mNo password entered, please try again!\u001B[0m");
            } 
            else 
            {
                break;
            }
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
        //message.setRecipients(Message.RecipientType.CC, InternetAddress.parse("lutzsandrastahl@gmail.com,stahlingrid385@gmail.com"));

        System.out.println("\u001B[34mDo you want to Enter another receiver for this mail? [yes]/[no]\u001B[0m");
        outerLoop:
        while(true)
        {
            String answer = scanner.nextLine();
            if(answer.equals("yes"))
            {
                StringBuilder cc = new StringBuilder();
                while(true)
                {
                    System.out.println("\u001B[34mEnter the receiver or 'close' to stop: \u001B[0m");
                    String ccPart = scanner.nextLine();
                    if(ccPart.equals("close"))
                    {
                        break;
                    }
                    else
                    {
                        cc.append(ccPart);
                        cc.append(",");
                    }
                }
                cc.deleteCharAt(cc.length()-1);
                //System.out.println(cc);
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc.toString()));
                break outerLoop;
            }
            else if(answer.equals("no"))
            {
                break;
            }
            else
            {
                System.out.println("\u001B[34mNo valid input, no receivers will be added\u001B[0m");
            }
        }


        System.out.println("\u001B[34mEnter the subject of the mail: \u001B[0m");
        String subject = scanner.nextLine();
        message.setSubject(subject);


        //cant contain umlauts
        System.out.println("\u001B[34mEnter the body of the mail (type enter + close + enter to finish): \u001B[0m");
        StringBuilder body = new StringBuilder();
        //boolean close = false;

        while(true)
        {
            String line = scanner.nextLine();
            if(line.equals("close"))
            {
                break;
            }
            else
            {
                body.append(line);
                body.append("\n");
            }
        }

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(body.toString(), "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);

        System.out.println("\u001B[34mMessage sent!\u001B[0m");
    }
}
