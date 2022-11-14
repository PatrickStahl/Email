package email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.net.URLDecoder;
import java.util.Base64;
import java.io.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public abstract class SocketClient {

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

        //Get the password that the user wants to use
        System.out.println("Enter your password ('password'): ");
        String password;
        while (true) 
        {
            password = scanner.nextLine();

            //if user doesnt enter a password he has to try again until he does
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

        //new Client object
        Client client = new Client();
        //connect to host

        client.connect(host, port);
        //log in with user data
        client.authenticate(email, password);
        System.out.println("Connected to " + host + " on port " + port + " as " + email);

        //Print all message indexes, their date and subject
        System.out.println("========================================");
        client.printAllMessages();
        System.out.println("========================================");

        //Tell the user how many messages are in the inbox
        int totalAmount = client.getMessageAmount();
        System.out.println("Total amount of messages: " + totalAmount);

        //Listen for commands from the user
        while (true) 
        {
            System.out.println("Enter the number of the message you want to read or close to exit: ");
            String command = scanner.nextLine();

            //Check if the command is "close"
            if (command.equals("close")) 
            { 
                System.out.println("========================================");
                break;
            } 
            else
            {
                try 
                {
                    int messageNumber = Integer.parseInt(command);
                    
                    if(messageNumber < 0 || messageNumber > totalAmount)
                    {
                        System.out.println("No valid message number, please try again or type 'close' to exit");
                        System.out.println("========================================");
                    }
                    else
                    {
                        System.out.println("========================================");
                        client.printMessage(messageNumber); 
                        System.out.println("========================================");
                    }
                } 
                catch (Exception e) 
                { 
                    //if the command is not an integer or "close", print an error message
                    System.out.println("Invalid input!");
                    System.out.println("========================================");
                    e.printStackTrace();
                }
            }
        }

        scanner.close();
        System.out.println("Closing connection..."); // tell the user that the connection is closing
        client.close(); // close the connection

    }

    private static class Client 
    {
        
        //less secure socket
        Socket socket;

        //sslsocket
        SSLSocket sslSocket;

        //use ssl
        boolean ssl;

        //read messages from server
        BufferedReader reader;

        //print messages from server
        PrintWriter writer;

        //The last read line from the server
        String line;

        
        public Client() 
        {
            
        }

        public void connect(String host, int port) throws Exception 
        {

            if(ssl == true)
            {
                //what does this do?
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                sslSocket = (SSLSocket) factory.createSocket(host, port);
                //sslSocket.setKeepAlive(true);
                reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                writer = new PrintWriter(sslSocket.getOutputStream(), true);
                line = reader.readLine();
            }

            else
            {
                socket = new Socket(host, port);
                //socket. setKeepAlive(true);
                //reads the chars or bytes or whatever, sent from the server and translates them into strings
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //sends commands to the server; autoflush automatically clears (flushes) the data
                writer = new PrintWriter(socket.getOutputStream(), true);
                //reads the stuff that the server sends 
                line = reader.readLine();
            }
        }


        public void authenticate(String email, String password) throws IOException 
        {
            writer.println("USER " + email);
            line = reader.readLine();
            writer.println("PASS " + password);
            line = reader.readLine();
        }

        public void printAllMessages() throws IOException 
        {
            int numberOfMessages; 

            //Returns: +OK <number of messages> <total size of messages>
            writer.println("STAT"); 
            line = reader.readLine(); 
            /**
             * parseint converts(parses) a string to an integer value
             * split splits the array at the given position and puts the new strings into an array
             * str = moin1meister --> str.split("1") = moin, meister (two array-elements)
             * the index [1] shows that only the element 1, i.e. the number is used
             */
            //numberOfMessages = Integer.parseInt(line.split(" ")[1]); 
            numberOfMessages = 5;

            for (int i = 1; i <= numberOfMessages; i++) 
            { 
                System.out.print("[" + i + "] ");
                //Gets the n-th message from the server (Returns: +OK message follows, <text> .) 
                writer.println("RETR " + i); 
                line = reader.readLine(); 

                //If the date has already been printed
                boolean foundDate = false; 
                //If the subject has already been printed
                boolean foundSubject = false; 

                //date of message
                String date = ""; 
                //subject of message
                StringBuilder subject = new StringBuilder();

                while (!line.equals(".")) 
                { 
                    if (line.toLowerCase().startsWith("date: ") && !foundDate) 
                    { 
                        date = line.substring(6); 
                        foundDate = true; 
                    }

                    if (line.startsWith("Subject: ") && !foundSubject) 
                    { 
                        subject = new StringBuilder(line.substring(9)); 
                        foundSubject = true; 

                        //while the line starts with " " it still belongs to the subject
                        do 
                        {
                            line = reader.readLine();
                            if (line.startsWith(" ")) 
                            { 
                                //If the line starts with " " remove the " " and append it to the rest of the subject
                                subject.append(line.substring(1));
                            }
                        } 
                        //If the next line starts with "Subject: ", read the next line
                        while (line.startsWith(" ")); 
                        //Continue so that the next line isnt skipped
                        continue;
                    }

                    line = reader.readLine(); 
                }

                String[] dateParts = date.split(" "); 
                date = dateParts[0] + " " + dateParts[1] + " " + dateParts[2] + " " + dateParts[3] + " " + dateParts[4]; 

                System.out.print("Date: " + date + ", "); 
                System.out.println("Subject: " + decypher(subject.toString())); 
                System.out.println(); 
            }
        }

        int getMessageAmount() throws IOException 
        {
            int numberOfMessages; 
            //Returns: +OK <number of messages> <total size of messages>
            writer.println("STAT"); 
            line = reader.readLine(); 
            numberOfMessages = Integer.parseInt(line.split(" ")[1]);
            return numberOfMessages;
        }


        public void printMessage(int messageNumber) throws IOException 
        {
            //Returns: +OK message follows, <message>, .
            writer.println("RETR " + messageNumber); 

            String sender = ""; 
            String date = ""; 
            String receiver = ""; 
            String subject = ""; 
            StringBuilder text = new StringBuilder();

            
            boolean startBody = false; 
            // while (true)
            // {
            //     String newLine = reader.readLine();
            //     text.append(newLine).append("\n");
                
            //     if(newLine.equals("."))
            //     {
            //         break;
            //     }
            // }


            while (true) 
            { 

                String newLine = reader.readLine(); //Read the next line

                if (newLine.equals(".")) 
                {
                    break;
                }

                if ((newLine.startsWith("   ") || newLine.startsWith(" ") || newLine.startsWith("\t")) && startBody == false) 
                {
                    line += newLine;
                }
                else
                {
                    if (line.startsWith("-ERR")) 
                    { 
                        System.out.println("Message not found!"); 
                        break;
                    } 
                    else if (line.equals(".")) 
                    { 
                        break; 
                    }

                    // avoid image base64 blocks because of overflows
                    if (line.toLowerCase().startsWith("content-transfer-encoding: base64") || line.toLowerCase().startsWith("content-type: image/")) 
                    {
                        while (!line.equals(".")) 
                        { 
                            line = reader.readLine(); 
                        }
                        break;
                    }


                    if (line.toLowerCase().startsWith("from: ")) 
                    { 
                        //removes "from: "
                        sender = line.substring(6).trim(); 
                        if (sender.contains("<")) 
                        {
                            sender = sender.substring(sender.indexOf("<") + 1, sender.indexOf(">"));
                        }
                    }

                    else if (line.toLowerCase().startsWith("date: ")) 
                    { 
                        String[] dateParts = line.substring(6).split(" "); 
                        //Date: Thu, 19 Aug 2021 08:32:18 +0200 (CEST)
                        //remove the +xxxx and timezone
                        date = dateParts[0] + " " + dateParts[1] + " " + dateParts[2] + " " + dateParts[3] + " " + dateParts[4]; 
                    }

                    else if (line.toLowerCase().startsWith("to: ")) 
                    { 
                        
                        receiver = line.substring(4).trim(); 
                        if (receiver.contains("<")) 
                        {
                            receiver = receiver.substring(receiver.indexOf("<") + 1, receiver.indexOf(">"));
                        }
                    }

                    else if (line.toLowerCase().startsWith("subject: ")) 
                    { 
                        subject = decypher(line.substring(9)); 
                    }

                    else if (line.toLowerCase().startsWith("content-transfer-encoding: "))
                    { 
                        startBody = true;
                    }

                    //can be deleted depending if the whole mail should be printed or not
                    else if(startBody == true && line.toLowerCase().contains("content-type: text/html; charset="))
                    {
                        break;
                    }

                    if (startBody) 
                    { 
                        //fixes the broken umlauts                 
                        String decoded = new String(line.getBytes("ISO-8859-1"), "UTF-8");
                        decoded = replaceUmlauts(decoded);


                        //this part shall fix the problem, where words get seperated at the end of the line and continue in the next one, it breaks at enter keys tho
                        // if(!(newLine.startsWith(" ")) && !line.isBlank() && !line.endsWith(" ") && !line.endsWith(".") && !line.endsWith("\n") &&!newLine.startsWith("\n")) 
                        // {
                        //     String newAppendage = newLine.split(" ")[0];
                        //     newAppendage = replaceUmlauts(newAppendage);
                        //     if(!newLine.toLowerCase().contains("content-type: text/html; charset="))
                        //     {
                        //         decoded += newAppendage;
                        //         newLine = newLine.replace(newAppendage, "");
                        //     }
                        // }
                        
                        if(decoded.startsWith(" "))
                        {
                            decoded = decoded.substring(1);
                        }

                        if(!decoded.contains("Content-Type: ") && !decoded.contains("Content-Transfer-Encoding: "))
                        {
                            text.append(decoded).append("\n");
                        }
                    }

                    line = newLine;
            }
                
            }
            //String finalText = text.toString().replace("= ", "\n");
            System.out.println("Date: " + date);
            System.out.println("Sender: " + sender);
            System.out.println("Receiver: " + receiver);
            System.out.println("Subject: " + subject);
            System.out.println("======================== Text =============================");
            System.out.println(text);
            
        }

        private String decypher(String text) throws IOException
        {
        if (text.startsWith("=?")) 
                { 
                    //splits between = and ?
                    String[] splits = text.split("=\\?");
                    //Stringbuilder creates changeable sequences of characters (i.e. strings)
                    StringBuilder deciphered = new StringBuilder(); 
                    // --> =   ?iso-8859-1?Q?Mentor*innen_f=FCr_internationale_Studierende_gesucht!?   =
        
                    for (int i = 0; i < splits.length; i++) 
                    {
                        String split = splits[i];
                        try 
                        {
                            //If the split is blank, skip current iteration of the loop
                            if (split.isBlank()) 
                            { 
                                continue;
                            }

                            //splits the subject into parts after each "?"
                            String[] parts = split.split("\\?"); 
                            String charset = parts[0];
                            //Moin --> MOIN
                            String encoding = parts[1].toLowerCase(); 
                            String encodedText = parts[2]; 

                            /**
                             * part[0] = iso-8859-1
                             * part[1] = Q
                             * part[2] = Mentor*innen_f=FCr_internationale_Studierende_gesucht!
                             */

                            if (encoding.equals("q")) 
                            { 
                                //use regex because decode cant do it by itself (probably because there is no ä ö ü... in english?)
                                encodedText = encodedText.replaceAll("=([0-9A-Fa-f]{2})", "%$1"); // Replace all "=XX" with "%XX"
                                encodedText = encodedText.replaceAll("_", " ");

                                try 
                                {
                                    //decodes the encoded string by the given charset
                                    deciphered.append(URLDecoder.decode(encodedText, charset));
                                    
                                } 
                                catch (UnsupportedEncodingException e) 
                                {
                                    e.printStackTrace();
                                    System.out.println("Unsupported encoding: " + charset);
                                }
                            } 

                            //TGluQWxnIGbDvHIgSW5mbyAoMjAyMik6IExlc2VhdWZnYWJlIGbDvHIgZGk=
                            else if (encoding.equals("b")) 
                            { 
                                byte[] bytes = Base64.getDecoder().decode(encodedText); // Decode the encoded text
                                try 
                                {
                                    deciphered.append(new String(bytes, charset)); // Decode the bytes
                                } 
                                catch (UnsupportedEncodingException e) 
                                {
                                    e.printStackTrace();
                                    System.out.println("Unsupported encoding: " + charset);
                                }
                            }
                        } 
                        catch (Exception ignored) 
                        {

                        }
                    }

                    return deciphered.toString(); 
                } 
                else 
                { // If the subject does not start with "=?"
                    return text; // Return the subject
                }
            }
 
            

        public String replaceUmlauts (String text)
        {
            text = text.replaceAll("=C3=84|=C4", "Ä");
            text = text.replaceAll("=C3=96|=D6", "Ö");
            text = text.replaceAll("=C3=9C|=DC", "Ü");
            text = text.replaceAll("=C3=9F|=DF", "ß");
            text = text.replaceAll("=C3=A4|=E4", "ä");
            text = text.replaceAll("=C3=B6|=F6", "ö");
            text = text.replaceAll("=C3=BC|=FC", "ü");

            
            if(text != null && text.length()>0 && text.charAt(text.length()-1) == '=')
            {
                text = text.substring(0, text.length()-1);
            }
            
            return text;
        }    

        public void close() throws IOException 
        {
            //Returns: +OK POP3 server signing off (and closes connection)
            writer.println("QUIT"); 
            line = reader.readLine(); 
            //closes socket
            socket.close(); 
        }
    }
}