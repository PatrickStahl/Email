package email;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;


public abstract class SocketClient 
{
    // throws needed for methods that throw exceptions
    public static void main() throws Exception 
    {
        System.out.println("\u001B[34mEnter the host you want to connect to ('pop3.uni-jena.de'): \u001B[0m");
        String host;
        Scanner scanner = new Scanner(System.in);
        while (true) 
        {
            host = scanner.nextLine();

            // i was too lazy to type it every time
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
            //hides input
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

        // new Client object (shown below)
        Client client = new Client();
        if (ssl) 
        {
            client.ssl = true;
        }

        // connect to host
        client.connect(host, port);
        // log in with user data
        client.authenticate(email, password);
        System.out.println("\u001B[32mConnected to " + host + " on port " + port + " as " + email + "\u001B[0m");

        // Print all message indexes, their date and subject
        client.printAllMessages(1, 5);

        // Tell the user how many messages are in the inbox
        int totalAmount = client.getMessageAmount();
        System.out.println("\u001B[32mTotal amount of messages: " + totalAmount + "\u001B[0m");
        System.out.println();

        // Listen for commands from the user
        while (true) 
        {
            System.out.println("\u001B[34mEnter the number of the message you want to read, the range of messages you want to show (Ex.: '10-20') or 'close' to exit: \u001B[0m");
            String command = scanner.nextLine();

            // Check if the command is "close"
            if (command.equalsIgnoreCase("close")) 
            {
                System.out.println("\u001B[32m===========================================================\u001B[0m");
                break;
            } 
            else if (command.contains("-")) 
            {
                // get numbers from command
                try 
                {
                    String[] numbers = command.split("-");
                    if (numbers.length != 2) {
                        System.out.println("\u001B[31mInvalid input, please try again!\u001B[0m");
                        continue;
                    }

                    int firstNumber = Integer.parseInt(numbers[0]);
                    int secondNumber = Integer.parseInt(numbers[1]);

                    // check if numbers are valid
                    if (firstNumber > 0 && secondNumber <= totalAmount && firstNumber <= secondNumber) 
                    {
                        client.printAllMessages(firstNumber, secondNumber);
                    } else 
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
                try 
                {
                    int messageNumber = Integer.parseInt(command);

                    if (messageNumber < 0 || messageNumber > totalAmount) 
                    {
                        System.out.println("\u001B[31mNo valid message number, please try again or type 'close' to exit\u001B[0m");
                        System.out.println("\u001B[32m===========================================================\u001B[0m");
                    } 
                    else 
                    {
                        System.out.println("\u001B[32m===========================================================\u001B[0m");
                        client.printMessage(messageNumber);
                        System.out.println("\u001B[32m===========================================================\u001B[0m");
                    }
                }
                catch (Exception e) 
                {
                    // if the command is not an integer or "close", print an error message
                    System.out.println("\u001B[31mInvalid input, please try again!\u001B[0m");
                    System.out.println("\u001B[32m===========================================================\u001B[0m");
                }
            }
        }

        scanner.close();
        System.out.println("\u001B[32mClosing connection...\u001B[0m"); // tell the user that the connection is closing
        client.close(); // close the connection

    }

    private static class Client 
    {

        // less secure socket
        Socket socket;

        // sslsocket
        SSLSocket sslSocket;

        // use ssl
        boolean ssl = false;

        // read messages from server
        BufferedReader reader;

        // print messages from server
        PrintWriter writer;

        // The last read line from the server
        String line;


        public Client() 
        {

        }

        public void connect(String host, int port) throws Exception 
        {

            if (ssl) 
            {
                // creates socket with deafult parameters
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                sslSocket = (SSLSocket) factory.createSocket(host, port);
                sslSocket.setKeepAlive(true);
                // inputstreamreader reads bytes from socket and converts them to chars
                reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                writer = new PrintWriter(sslSocket.getOutputStream(), true);
            } 
            else 
            {
                socket = new Socket(host, port);
                // socket. setKeepAlive(true);
                // reads the chars or bytes or whatever, sent from the server and translates them into strings
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // sends commands to the server; autoflush automatically clears (flushes) the data
                writer = new PrintWriter(socket.getOutputStream(), true);
                // reads the stuff that the server sends
            }
            line = reader.readLine();
        }


        public void authenticate(String email, String password) throws IOException 
        {
            writer.println("USER " + email);
            line = reader.readLine();
            writer.println("PASS " + password);
            line = reader.readLine();
        }

        public void printAllMessages(int firstNumber, int lastNumber) throws IOException 
        {

            // Returns: +OK <number of messages> <total size of messages>
            writer.println("STAT");
            line = reader.readLine();
            // numberOfMessages = Integer.parseInt(line.split(" ")[1]);

            System.out.println("\u001B[32m===========================================================\u001B[0m");
            System.out.println();
            for (int i = firstNumber; i <= lastNumber; i++) 
            {
                System.out.print("\u001B[34m[" + i + "] \u001B[0m");
                // Gets the n-th message from the server (Returns: +OK message follows, <text> .)
                writer.println("RETR " + i);
                line = reader.readLine();

                // If the date has already been printed
                boolean foundDate = false;
                // If the subject has already been printed
                boolean foundSubject = false;

                // date of message
                String date = "";
                // subject of message
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

                        // while the line starts with " " it still belongs to the subject
                        do 
                        {
                            line = reader.readLine();
                            if (line.startsWith(" ")) 
                            {
                                // If the line starts with " " remove the " " and append it to the rest of the subject
                                subject.append(line.substring(1));
                            }
                        }
                        // If the next line starts with "Subject: ", read the next line
                        while (line.startsWith(" "));
                        // Continue so that the next line isnt skipped
                        continue;
                    }

                    line = reader.readLine();
                }

                String[] dateParts = date.split(" ");
                date = dateParts[0] + " " + dateParts[1] + " " + dateParts[2] + " " + dateParts[3] + " " + dateParts[4];

                System.out.print("\u001B[34mDate: " + date + ", \u001B[0m");
                System.out.println("\u001B[34mSubject: " + decypher(subject.toString()) + "\u001B[0m");
                System.out.println();
            }
            System.out.println("\u001B[32m===========================================================\u001B[0m");
        }

        int getMessageAmount() throws IOException 
        {
            int numberOfMessages;
            // Returns: +OK <number of messages> <total size of messages>
            writer.println("STAT");
            line = reader.readLine();
            numberOfMessages = Integer.parseInt(line.split(" ")[1]);
            return numberOfMessages;
        }


        public void printMessage(int messageNumber) throws IOException 
        {
            // Returns: +OK message follows, <message>, .
            writer.println("RETR " + messageNumber);

            String sender = "";
            String date = "";
            String receiver = "";
            String subject = "";
            StringBuilder text = new StringBuilder();

            boolean startBody = false;
            line = reader.readLine();
            if (line.startsWith("-ERR")) 
            {
                System.out.println("\u001B[31mMessage not found!\u001B[0m");
                return;
            } 
            List<String> files = new ArrayList<String>();
            while (true) 
            {
                //String decoded = new String(line.getBytes("ISO-8859-1"), "UTF-8");
                String newLine = new String(reader.readLine().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                //newLine = new String(newLine.getBytes("ISO-8859-1"), "UTF-8");
                boolean finish = false; // If the message is finished
                if (newLine.equals(".")) 
                {
                    startBody = false;
                    finish = true;
                }

                if (startBody) 
                {
                    if (newLine.toLowerCase().startsWith("content-type: ") && !newLine.toLowerCase().startsWith("content-type: text/")) 
                    {
                        // get the content type
                        String contentType = newLine.substring(14);
                        files.add("\u001B[34m" + contentType + "\u001B[0m");
                        // If the content-type is not text/plain, skip the rest
                        while (!newLine.equals("content-type:")) 
                        {
                            newLine = reader.readLine();

                            if (newLine.equals(".")) 
                            {
                                finish = true;
                                break;
                            }
                        }
                    }
                    text.append(newLine).append("\n");
                }

                if (newLine.startsWith("\t")) 
                {
                    line += newLine;
                } 
                else 
                {
                    if (line.toLowerCase().startsWith("from: ") && !startBody) 
                    {
                        // removes "from: "
                        sender = line.substring(6).trim();
                        if (sender.contains("<")) 
                        {
                            sender = sender.substring(sender.indexOf("<") + 1, sender.indexOf(">"));
                        }
                    } 
                    else if (line.toLowerCase().startsWith("date: ") && !startBody) 
                    {
                        String[] dateParts = line.substring(6).split(" ");
                        // Date: Thu, 19 Aug 2021 08:32:18 +0200 (CEST)
                        date = dateParts[0] + " " + dateParts[1] + " " + dateParts[2] + " " + dateParts[3] + " " + dateParts[4];
                    } 
                    else if (line.toLowerCase().startsWith("to: ") && !startBody) 
                    {

                        receiver = line.substring(4).trim();
                        if (receiver.contains("<")) 
                        {
                            receiver = receiver.substring(receiver.indexOf("<") + 1, receiver.indexOf(">"));
                        }
                    } 
                    else if (line.toLowerCase().startsWith("subject: ") && !startBody) 
                    {
                        subject = decypher(line.substring(9));
                    }
                    else if (line.toLowerCase().startsWith("content-transfer-encoding: ")) 
                    {
                        startBody = true;
                    }

                    if (finish) 
                    {
                        // fixes the broken umlauts
                        text = new StringBuilder(replaceUmlauts(text.toString()));
                        line = newLine;
                        break;
                    }

                    line = newLine;
                }
            }

            System.out.println("\u001B[34mDate: " + date + "\u001B[0m");
            System.out.println("\u001B[34mSender: " + sender + "\u001B[0m");
            System.out.println("\u001B[34mReceiver: " + receiver + "\u001B[0m");
            System.out.println("\u001B[34mSubject: " + subject + "\u001B[0m");
            System.out.println("\u001B[32m======================== Text =============================\u001B[0m");
            System.out.println("\u001B[34m" + text + "\u001B[0m");

            if (!files.isEmpty()) {
                System.out.println("\u001B[32m======================== Files =============================\u001B[0m");

                for (String file : files) 
                {
                    System.out.println(file);
                }
            }

            line = "";
        }

        // subject: =?iso-8859-1?Q?Mentor*innen_f=FCr_internationale_Studierende_gesucht!?=
        private String decypher(String text) 
        {
            // allways true (i think)
            if (text.startsWith("=?")) 
            {
                // splits between = and ?
                String[] splits = text.split("=\\?");
                // Stringbuilder creates changeable sequences of characters (i.e. strings)
                StringBuilder deciphered = new StringBuilder();
                // --> =   ?iso-8859-1?Q?Mentor*innen_f=FCr_internationale_Studierende_gesucht!?   =

                for (int i = 0; i < splits.length; i++) 
                {
                    String split = splits[i];
                    try 
                    {
                        // If the split is blank, skip current iteration of the loop
                        if (split.isEmpty())
                        {
                            continue;
                        }

                        // splits the subject into parts after each "?"
                        String[] parts = split.split("\\?");
                        String charset = parts[0];
                        String encoding = parts[1].toLowerCase();
                        String encodedText = parts[2];

                         // part[0] = iso-8859-1
                         // part[1] = Q
                         // part[2] = Mentor*innen_f=FCr_internationale_Studierende_gesucht!

                        if (encoding.equals("q")) 
                        {
                            // use regex because decode cant do it by itself (probably because there is no ä ö ü... in english?)
                            encodedText = encodedText.replaceAll("=([0-9A-Fa-f]{2})", "%$1"); // Replace all "=XX" with "%XX"
                            encodedText = encodedText.replaceAll("_", " ");

                            try 
                            {
                                // decodes the encoded string by the given charset
                                deciphered.append(URLDecoder.decode(encodedText, charset));

                            }
                            // never happens
                            catch (UnsupportedEncodingException e) 
                            {
                                System.out.println("\u001B[31mUnsupported encoding: " + charset + "\u001B[0m");
                            }
                        }

                        // =?utf-8?B?dWRpZXJlbmRlIHVuZCBNaXRhcmJlaXRlcjogbmV1ZSBvZGVyIGdlw6Ru?=
                        else if (encoding.equals("b")) 
                        {
                            byte[] bytes = Base64.getDecoder().decode(encodedText);
                            try 
                            {
                                deciphered.append(new String(bytes, charset));
                            } 
                            catch (UnsupportedEncodingException e) 
                            {
                                System.out.println("\u001B[31mUnsupported encoding: " + charset + "\u001B[0m");
                            }
                        }
                    }
                    // program dies without the try catch stuff
                    catch (Exception ignored) 
                    {

                    }
                }

                return deciphered.toString();
            } 
            else 
            { 
                // If the subject does not start with "=?"
                return text; // Return the subject
            }
        }


        public String replaceUmlauts(String text) {
            text = text.replaceAll("=C3=84|=C4", "Ä");
            text = text.replaceAll("=C3=96|=D6", "Ö");
            text = text.replaceAll("=C3=9C|=DC", "Ü");
            text = text.replaceAll("=C3=9F|=DF", "ß");
            text = text.replaceAll("=C3=A4|=E4", "ä");
            text = text.replaceAll("=C3=B6|=F6", "ö");
            text = text.replaceAll("=C3=BC|=FC", "ü");


            if (text.length() > 0 && text.charAt(text.length() - 1) == '=') 
            {
                text = text.substring(0, text.length() - 1);
            }

            return text;
        }

        public void close() 
        {
            // Returns: +OK POP3 server signing off (and closes connection)
            writer.println("QUIT");
            try 
            {
                writer.close();
                reader.close();
                socket.close();
            } 
            catch (Exception ignored) 
            {
            
            }
        }
    }
}