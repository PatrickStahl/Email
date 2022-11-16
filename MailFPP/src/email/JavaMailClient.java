package email;

import com.sun.mail.pop3.POP3SSLStore;
import com.sun.mail.pop3.POP3Store;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.util.Scanner;


public abstract class JavaMailClient 
{
    public static void main() throws Exception 
    {
        System.out.println("\u001B[34mEnter the host you want to connect to ('pop3.uni-jena.de'): \u001B[0m");
        String host;
        Scanner scanner = new Scanner(System.in);
        while (true) 
        {
            host = scanner.nextLine();

            if (host.equals("")) 
            {
                host = "pop3.uni-jena.de";
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
        System.out.println("\u001B[34mDo you want to connect to the server with SSL? ('yes' or 'no'): \u001B[0m");
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


        System.out.println("\u001B[34mEnter the port you want to connect to ('995' for SSL, '110' for non-SSL): \u001B[0m");
        int port;
        while (true) {
            String portNumber = scanner.nextLine();
            if (portNumber.isEmpty()) 
            {
                if (ssl) 
                {
                    port = 995;
                } 
                else 
                {
                    port = 110;
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
                System.out.println("\u001B[31mNo email entered, please try again!\u001B[0m");
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
        
        // Get the password that the user wants to use
        System.out.println("\u001B[34mEnter your password ('password'): \u001B[0m");
        String password;
        while (true) 
        {
            password = new String(System.console().readPassword());

            // if user doesnt enter a password he has to try again until he does
            if (password.isEmpty()) 
            {
                System.out.println("\u001B[31mNo password entered, please try again!\u001B[0m");
            } 
            else 
            {
                break;
            }
        }

        // to set up the values used to connect to the server
        java.util.Properties properties = new java.util.Properties();

        // Set the host and port
        properties.setProperty("mail.pop3.host", host);
        properties.setProperty("mail.pop3.port", String.valueOf(port));

        // if user wants ssl another property is added
        if (ssl) 
        {
            properties.setProperty("mail.pop3.ssl.enable", "true");
        }

        // create mailSession with given properties
        Session session = Session.getInstance(properties);

        // Create (SSL)Store object to store and retrieve messages
        POP3SSLStore sslStore = null;
        Store store = null;
        if (ssl) 
        {
            sslStore = new POP3SSLStore(session, null);
        } 
        else 
        {
            store = new POP3Store(session, null);
        }

        // connect to server
        if (ssl) 
        {
            sslStore.connect(host, port, email, password);
        } 
        else 
        {
            store.connect(host, port, email, password);
        }

        // messages or other folders are stored here
        Folder inbox;
        if (ssl) 
        {
            inbox = sslStore.getFolder("INBOX");
        } 
        else 
        {
            inbox = store.getFolder("INBOX");
        }


        // Open the inbox folder
        inbox.open(Folder.READ_ONLY);

        // Array with messages from inbox folder
        Message[] messages = inbox.getMessages();

        // [<index>] Date: <date>, Subject: <subject>
        System.out.println("\u001B[34m================================================================================\u001B[0m");
        // gets date and subject of all messages
        // i < messages.length
        System.out.println();
        for (int i = 0; i < 5; i++)
        {
            System.out.println("\u001B[32m[" + (i + 1) + "] Date: " + messages[i].getSentDate() + ", Subject: " + messages[i].getSubject() + "\u001B[0m");
            System.out.println();
        }
        System.out.println("\u001B[34m================================================================================\u001B[0m");
        System.out.println("\u001B[32mTotal amount of messages: " + messages.length + "\u001B[0m");
        System.out.println();


        // user can choose whether to close the program or to display a specific message
        while (true) 
        {
            System.out.println("\u001B[34mEnter the number of the message you want to read, the range of messages you want to show (Ex.: '10-20') or 'close' to exit: \u001B[0m");

            String command = scanner.nextLine();

            if (command.equalsIgnoreCase("close")) 
            {
                System.out.println("\u001B[34m================================================================================\u001B[0m");
                break;
            } 
            else if (command.contains("-")) 
            {
                // get numbers from command
                try 
                {
                    String[] numbers = command.split("-");
                    if (numbers.length != 2) 
                    {
                        System.out.println("\u001B[31mInvalid input, please try again!\u001B[0m");
                        continue;
                    }

                    int firstNumber = Integer.parseInt(numbers[0]);
                    int secondNumber = Integer.parseInt(numbers[1]);

                    // check if numbers are valid
                    if (firstNumber > 0 && secondNumber <= messages.length && firstNumber < secondNumber) {
                        // print all messages in range
                        System.out.println();
                        System.out.println("\u001B[34m================================================================================\u001B[0m");
                        System.out.println();
                        for (int i = firstNumber; i < secondNumber; i++) 
                        {
                            System.out.println("\u001B[32m[" + (i + 1) + "] Date: " + messages[i].getSentDate() + ", Subject: " + messages[i].getSubject() + "\u001B[0m");
                            System.out.println();
                        }
                        System.out.println("\u001B[34m================================================================================\u001B[0m");
                    } 
                    else 
                    {
                        System.out.println("\u001B[31mInvalid range, please try again!\u001B[0m");
                    }
                } 
                catch (NumberFormatException e) 
                {
                    System.out.println("\u001B[31mInvalid range, please try again!\u001B[0m");
                }
            } 
            else 
            {
                // catch inputs that arent numbers or "close"
                try 
                {
                    int index = Integer.parseInt(command);
                    index--;

                    // out of bounds exception
                    if (index < 0 || index >= messages.length) 
                    {
                        System.out.println("\u001B[31mInvalid index. Please enter a valid index or 'close' to exit: \u001B[0m");
                        System.out.println("\u001B[34m================================================================================\u001B[0m");
                    } 
                    else 
                    {
                        String receiver = "";
                        try 
                        {
                            for (int i = 0; i < messages[index].getAllRecipients().length; i++) 
                            {
                                String receiverTemp = messages[index].getAllRecipients()[i].toString();
                                if (receiverTemp.contains("<")) 
                                {
                                    receiverTemp = receiverTemp.substring(receiverTemp.indexOf("<") + 1, receiverTemp.indexOf(">"));
                                }
                                if (i == 0) 
                                {
                                    receiver = receiverTemp;
                                } 
                                else 
                                {
                                    receiver = receiver + ", " + receiverTemp;
                                }
                            }
                        } 
                        catch (NullPointerException ignored) 
                        {

                        }

                        String sender = "";
                        for (int i = 0; i <= messages[index].getFrom().length - 1; i++) 
                        {
                            try 
                            {
                                String senderTemp = messages[index].getFrom()[i].toString();
                                if (senderTemp.contains("<")) 
                                {
                                    senderTemp = senderTemp.substring(sender.indexOf("<") + 1, sender.indexOf(">"));
                                }
                                if (i == 0) 
                                {
                                    sender = senderTemp;
                                } else {
                                    sender = sender + ", " + senderTemp;
                                }
                            } 
                            catch (StringIndexOutOfBoundsException e) 
                            {
                                sender = messages[index].getFrom()[i].toString();
                            }
                        }

                        System.out.println("\u001B[34m================================================================================\u001B[0m");
                        System.out.println("\u001B[32mDate: " + messages[index].getSentDate() + "\u001B[0m");
                        System.out.println("\u001B[32mSender: " + sender + "\u001B[0m");
                        System.out.println("\u001B[32mReceiver: " + receiver + "\u001B[0m");
                        System.out.println("\u001B[32mSubject: " + messages[index].getSubject() + "\u001B[0m");
                        System.out.println("\u001B[34m======================== Text =============================\u001B[0m");
                        // multipart is a container that holds multiple bodyparts
                        if (messages[index].getContent() instanceof MimeMultipart) 
                        {
                            MimeMultipart mimeMultipart = (MimeMultipart) messages[index].getContent();
                            // print all bodyparts
                            for (int i = 0; i < mimeMultipart.getCount(); i++) 
                            {
                                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                                System.out.println("\u001B[32m" + bodyPart.getContent() + "\u001B[0m");
                            }
                        }
                        // if the mail is not splitted into parts
                        else 
                        {
                            System.out.println("\u001B[32m" + messages[index].getContent() + "\u001B[0m");
                        }
                        System.out.println("\u001B[34m================================================================================\u001B[0m");
                    }
                } 
                catch (NumberFormatException e) 
                {
                    System.out.println("\u001B[31mInvalid input!\u001B[0m");
                    System.out.println("\u001B[34m================================================================================\u001B[0m");
                }
            }
        }


        scanner.close();
        System.out.println("\u001B[31mClosing connection...\u001B[0m");

        // expunge (permanently remove deleted messages = false)
        inbox.close(false);

        // Close the connection to the server
        if (ssl) 
        {
            sslStore.close();
        } else 
        {
            store.close();
        }
    }
}