package email;

import com.sun.mail.pop3.POP3SSLStore;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.util.Scanner;

public abstract class JavaMailClient {

    public static void main() throws Exception 
    {
        System.out.println("Enter the host you want to connect to ('pop3.uni-jena.de'): ");
        String host;
        Scanner scanner = new Scanner(System.in);
        while (true) 
        {
            host = scanner.nextLine();

            //i was too lazy to type it every time
            if (host.equals(""))
            {
                host = "pop3.uni-jena.de";
                break;
            }
            if (host.contains(" ")) 
            {
                System.out.println("Host cannot contain spaces!");
            }
            //user typed a valid server name (syntaxwise)
            else 
            {
                break;
            }
        }

        //enable ssl connection?
        boolean ssl = false;
        System.out.println("Do you want to connect to the server with SSL? [yes]/[no]");
        
        while(true)
        {
            String answer = scanner.nextLine();

            //== doesnt work so i used equals
            if(answer.equals("yes"))
            {
                ssl = true;
                break;
            }
            else if(answer.equals("no"))
            {
                ssl = false;
                break;
            }
            else
            {
                System.out.println("No valid input, please try again!");
            }
        }


        System.out.println("Enter the port you want to connect to [995]/[110]: ");
        int port;
        while (true)
        {
            String portNumber = scanner.nextLine();
            if(portNumber == "")
            {
                if(ssl == true)
                {
                    port = 995;
                    break;
                }
                else
                {
                    port = 110;
                    break;
                }
            }
            try
            {
                port=Integer.parseInt(portNumber);
                break;
            }
        
            catch(NumberFormatException e)
            {
                System.out.println("Only numbers are valid inputs. Try again!");
                
            }
        }


        System.out.println("Enter your email adress ('max.mustermensch@uni-jena.de'): ");
        String email;
        while (true) 
        {
            email = scanner.nextLine();

            if(email.equals(""))
            {
                email = "patrick.stahl@uni-jena.de";
                break;
            }

            if(email.contains("@"))
            {
                break;
            }
            else
            {
                System.out.println("No valid email entered! (max.mustermensch@uni-jena.de)");
            }
        }

        System.out.println("Enter your password ('password'): ");
        String password;
        while (true) 
        {
            password = scanner.nextLine();

            if (password.equals("")) 
            {
                
                password = "";
                break;
                
                //System.out.println("No password entered! Please enter your password: ");
            } 
            else 
            {
                break;
            }
        }

        //to set up the values used to connect to the server 
        java.util.Properties properties = new java.util.Properties();

        //Set the host and port
        properties.setProperty("mail.pop3.host", host);
        properties.setProperty("mail.pop3.port", String.valueOf(port));

        //if user wants ssl another property is added
        if (ssl == true) 
        {
            properties.setProperty("mail.pop3.ssl.enable", "true");
        }

        //create mailSession with given properties
        Session session = Session.getInstance(properties);

        //Create (SSL)Store object to store and retrieve messages 
        POP3SSLStore sslStore = null;
        Store store = null;
        if (ssl == true) 
        {
            sslStore = new POP3SSLStore(session, null);
        } 
        else 
        {
            store = session.getStore("pop3");
            //store = new POP3SSLStore(session, null);
        }

        //connect to server
        if (ssl == true) 
        {
            sslStore.connect(host, port, email, password);
        } 
        else 
        {
            store.connect(host, port, email, password);
        }

        //get the inbox folder (where messages are stored)
        Folder inbox;
        if(ssl == true)
        {
            inbox = sslStore.getFolder("INBOX");
        }
        else
        {
            inbox = store.getFolder("INBOX");
        }
        

        //Open the inbox folder
        inbox.open(Folder.READ_ONLY);

        //Array with messages from inbox folder
        Message[] messages = inbox.getMessages();

        //[<index>] Date: <date>, Subject: <subject>
        System.out.println("================================================================================");
        //gets date and subject of all messages 
        //i < messages.length
        for (int i = 0; i < 5; i++) 
        {
            System.out.println("[" + i + "] Date: " + messages[i].getSentDate() + ", Subject: " + messages[i].getSubject());
            System.out.println();
        }
        System.out.println("================================================================================");
        System.out.println("Total amount of messages: " + messages.length);


        //user can choose whether to close the program or to display a specific message
        while (true) 
        {
            System.out.println("Enter the number of the message you want to read or close to exit: ");

            String command = scanner.nextLine();

            if (command.equals("close")) 
            {
                System.out.println("================================================================================");
                break;
            } 
            else 
            {
                //catch inputs that arent numbers or "close"
                try 
                {
                    int index = Integer.parseInt(command);

                    //out of bounds exception
                    if (index < 0 || index >= messages.length) 
                    {
                        System.out.println("Invalid index. Please enter a valid index or 'close' to exit: ");
                        System.out.println("================================================================================");
                    } 
                    else 
                    {
                        String receiver = "";
                        try
                        {
                            for(int i = 0; i < messages[index].getAllRecipients().length; i++)
                            {
                                String receiverTemp = messages[index].getAllRecipients()[i].toString();
                                if(receiverTemp.contains("<"))
                                {
                                    receiverTemp = receiverTemp.substring(receiverTemp.indexOf("<") + 1,  receiverTemp.indexOf(">"));
                                }
                                if(i == 0)
                                {
                                    receiver = receiverTemp;
                                }
                                else                             
                                {
                                    receiver = receiver + ", " + receiverTemp;
                                }
                            }   
                        }  
                        catch(NullPointerException e)
                        {

                        }                       

                    
                        //String sender = messages[index].getFrom()[0].toString();
                        String sender = "";
                        for(int i = 0; i < messages[index].getFrom().length; i++)
                        {
                            try
                            {
                                String senderTemp = messages[index].getFrom()[i].toString();
                                if (senderTemp.toString().contains("<")) 
                                {
                                    senderTemp = senderTemp.substring(sender.indexOf("<") + 1, sender.indexOf(">"));
                                }
                                if(i == 0)
                                {
                                    sender = senderTemp;
                                }
                                else
                                {
                                    sender = sender + ", " + senderTemp;
                                }
                            }
                            catch(NullPointerException e)
                            {

                            }
                        }   

                        System.out.println("================================================================================");
                        System.out.println("Date: " + messages[index].getSentDate());
                        System.out.println("Sender: " + sender);
                        System.out.println("Receiver: " + receiver);
                        System.out.println("Subject: " + messages[index].getSubject());
                        System.out.println("======================== Text =============================");
                        //multipart is a container that holds multiple bodyparts
                        if (messages[index].getContent() instanceof MimeMultipart mimeMultipart) 
                        {
                            //print all bodyparts
                            for (int i = 0; i < mimeMultipart.getCount(); i++) 
                            {
                                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                                System.out.println(bodyPart.getContent());
                            }
                        } 
                        //if the mail is not splitted into parts
                        else 
                        {
                            System.out.println(messages[index].getContent());
                        }
                        System.out.println("================================================================================");
                    }
                } 
                catch (NumberFormatException e) {
                    System.out.println("Invalid input!");
                    System.out.println("================================================================================");
                }
            }
        }
    

        scanner.close();
        System.out.println("Closing connection..."); 

        //expunge (permanently remove deleted messages = false)
        inbox.close(false);

        // Close the connection to the server
        if (ssl == true) 
        {
            sslStore.close();
        } 
        else 
        {
            store.close();
        }
    }
}