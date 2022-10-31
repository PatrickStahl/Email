package email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;


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

        System.out.println("Enter the port you want to connect to (110): ");
        int portNumber = 0;
        while (true)
        {
            try
            {
                portNumber = scanner.nextInt();
            }
            catch(InputMismatchException e)
            {
                System.out.println("Only numbers are valid inputs. Try again!");
                scanner.nextLine();
            }
            scanner.nextLine();
            break;
        }

        System.out.println("Enter your email adress ('max.mustermensch@uni-jena.de'): ");
        String email;
        while (true) 
        {
            email = scanner.nextLine();

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
                /** 
                * too lazy to type it every time
                * password = "...";
                * break;
                */
                System.out.println("No password entered! Please enter your password: ");
            } 
            else 
            {
                break;
            }
        }

        //new Client object
        Client client = new Client();
        //connect to host
        client.connect(host, portNumber);
        //log in with user data
        client.authenticate(email, password);
        System.out.println("Connected to " + host + " on port " + portNumber + " as " + email);

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
            try 
            {
                if (command.equals("close")) 
                { 
                    System.out.println("========================================");
                    break;
                } 
                else
                { 
                    //if the command is not close, try to parse it as an integer
                    System.out.println("========================================");
                    //parseInt converts the String (if constistent of numbers into an int variable)
                    int messageNumber = Integer.parseInt(command);
                    client.printMessage(messageNumber); 
                    System.out.println("========================================");
                }
            } 
            catch (Exception e) 
            { 
                //if the command is not an integer or "close", print an error message
                System.out.println("Invalid input!");
                System.out.println("========================================");
            }
        }

        scanner.close();
        System.out.println("Closing connection..."); // tell the user that the connection is closing
        client.close(); // close the connection

    }

    private static class Client 
    {
        
        //endpoint for communication with server
        Socket socket;

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
            socket = new Socket(host, port);
            //reads the chars or bytes or whatever, sent from the server and translates them into strings
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //sends commands to the server; autoflush automatically clears (flushes) the data
            writer = new PrintWriter(socket.getOutputStream(), true);
            //reads the stuff that the server sends 
            line = reader.readLine();
        }

        /**
         * Authenticates the client
         * @param email The email address of the user
         * @param password The password of the user
         * @throws IOException If the authentication fails
         */
        public void authenticate(String email, String password) throws IOException 
        {
            writer.println("USER " + email);
            line = reader.readLine();
            writer.println("PASS " + password);
            line = reader.readLine();
        }

        /**
         * Prints all emails that are found in the inbox of the user
         * @throws IOException If the reading of the emails fails
         */
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
                String subject = "";

                //loops through the whole mail ("." is always generated at the end)
                while (!line.equals(".")) 
                { 
                    if (line.startsWith("Date: ") && !foundDate) 
                    { 
                        //If the line starts with "Date: " and the date has not been printed yet
                        //substring begins at 6th character and goes until the end of the string
                        date = line.substring(6);
                        foundDate = true; 
                    }

                    if (line.startsWith("Subject: ") && !foundSubject) 
                    { 
                        subject = reader.readLine();
                        //subject = line.substring(9); 
                        foundSubject = true; 
                    }

                    line = reader.readLine();
                    
                }

                System.out.print("Date: " + date + ", "); 
                System.out.println("Subject: " + subject.substring(9)); 
                System.out.println(); 
            }
        }

        /**
         * Gets the total amount of messages in the inbox of the user
         * @return The total amount of messages in the inbox of the user
         * @throws IOException If the reading of the emails fails
         */
        int getMessageAmount() throws IOException 
        {
            int numberOfMessages; 
            //Returns: +OK <number of messages> <total size of messages>
            writer.println("STAT"); 
            line = reader.readLine(); 
            numberOfMessages = Integer.parseInt(line.split(" ")[1]);
            return numberOfMessages;
        }

        /**
         * Prints the message with the given number
         * @param messageNumber The number of the message that should be printed
         * @throws IOException If the reading of the emails fails
         */
        public void printMessage(int messageNumber) throws IOException 
        {
            //Returns: +OK message follows, <message>, .
            writer.println("RETR " + messageNumber); 
            line = reader.readLine(); 
            while (!line.equals(".")) 
            {
                //print current line and read next line
                System.out.println(line); 
                line = reader.readLine();
            }
        }

        /**
         * Closes the connection to the server
         * @throws IOException If the closing of the connection fails
         */
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