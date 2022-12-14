package email;

import java.net.*;
import java.util.Base64;
import java.util.Scanner;

import java.io.*;

public class SendSocketMail 
{

    public static void main() throws UnknownHostException, IOException 
    {
        System.out.println("\u001B[34mEnter the host you want to connect to ('smtp.uni-jena.de'): \u001B[0m");
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

        System.out.println("\u001B[34mEnter the port you want to connect to (587): \u001B[0m");
        int port;
        while (true) 
        {
            String portNumber = scanner.nextLine();
            if (portNumber.isEmpty()) 
            {
                port = 587;
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

        client.connect(host, port);
        client.authenticate(email, password);
        client.send(email);

        while(client.alreadyQuitted == false)
        {
            System.out.println("\u001B[34mDo you want to send another message? [yes/no]: \u001B[0m");
            String answer = scanner.nextLine();
            if(answer.equals("yes"))
            {
                client.send(email);
            }
            else if(answer.equals("no"))
            {
                break;
            }
            else
            {
                System.out.println("\u001B[31mWrong input!\u001B[0m");
            }
        }
        client.quit();
        System.out.println("\u001B[34mClosing... \u001B[0m");
        scanner.close();
    }
}

class client
{

    static Socket mailSocket;
    static PrintWriter out;
    static BufferedReader in;
    static boolean alreadyQuitted = false;
    
    public static void connect(String host, int port) throws UnknownHostException, IOException
    {
        String status;
        mailSocket = new Socket(host, 587);
        mailSocket.setKeepAlive(true);
        out = new PrintWriter( mailSocket.getOutputStream(), true );
        in = new BufferedReader(new InputStreamReader(mailSocket.getInputStream()));
        status = in.readLine();
        if(!(status.startsWith("220")))
        {
            System.out.println("Error, unknown state: " + status);
            client.quit();
            return;
        }

        out.println("EHLO smtp.uni-jena.de");
        status = in.readLine();
        if(!(status.startsWith("250")))
        {
            System.out.println("Error, unknown state: " + status);
            client.quit();
            return;
        }
        //System.out.println("Connected to " + host);
    }

    public static void authenticate(String username, String password) throws IOException
    {
        if(alreadyQuitted == false)
        {
            String status = in.readLine();
            out.println("AUTH LOGIN");
            while(!(status.startsWith("334")))
            {
                status = in.readLine();    
                if(status.startsWith("334"))
                {
                    break;
                }
            }

            String encodedUsername = Base64.getEncoder().encodeToString(username.getBytes());
            String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes()); 
            out.println(encodedUsername);
            status = in.readLine();
            if(!(status.startsWith("334")))
            {
                System.out.println("Error, unknown state: " + status);
                client.quit();
                return;
            }
            out.println(encodedPassword);  
            status = in.readLine();
            if(!(status.startsWith("235")))
            {
                System.out.println("Error, unknown state: " + status);
                client.quit();
                return;
            }
            //System.out.println("Authentification done");
        }
    }

    public static void send(String from) throws IOException 
    {
        if(alreadyQuitted == false)
        {
            String status;
            Scanner scanner = new Scanner(System.in);
            System.out.println("\u001B[34mEnter the receiver of the mail: \u001B[0m");
            String receiver;
            while (true) 
            {
                receiver = scanner.nextLine();

                if (receiver.isEmpty()) 
                {
                    System.out.println("\u001B[31mNo password entered, please try again!\u001B[0m");
                } 
                else 
                {
                    break;
                }
                
            }
            out.println("MAIL FROM:<" + from +">");
            status = in.readLine();
            if(!(status.startsWith("250")))
            {
                System.out.println("Error, unknown state: " + status);
                client.quit();
                return;
            }
            out.println("RCPT TO:<" + receiver + ">");
            status = in.readLine();
            if(!(status.startsWith("250")))
            {
                System.out.println("Error, unknown state: " + status);
                client.quit();
                return;
            }

            while (true)
            {
                System.out.println("\u001B[34mDo you want to enter another receiver of the mail? [yes]/[no] \u001B[0m");
                String answer = scanner. nextLine();
                if(answer.equals("yes"))
                {
                    System.out.println("\u001B[34mEnter the receiver of the mail: \u001B[0m");
                    String newReceiver = scanner.nextLine();
                    out.println( "RCPT TO:<" + newReceiver + ">");
                    status = in.readLine();
                    if(!(status.startsWith("250")))
                    {
                        System.out.println("Error, unknown state: " + status);
                        client.quit();
                        return;
                    }
                    continue;
                }
                else if(answer.equals("no"))
                {
                    break;
                }
                else 
                {
                    System.out.println("\u001B[34mEnter [yes] or [no] to continue \u001B[0m");
                }
            }
            out.println( "DATA" );
            status = in.readLine();

            System.out.println("\u001B[34mEnter the subject of the mail: \u001B[0m");
            String subject;
            while (true) 
            {
                subject = scanner.nextLine();

                if (receiver.isEmpty()) 
                {
                    System.out.println("\u001B[31mNo subject entered, please try again!\u001B[0m");
                } 
                else 
                {
                    break;
                }
            }
            out.println( "Subject: " + subject );

            System.out.println("\u001B[34mEnter the body of the mail (type 'close' in a new line to send): \u001B[0m");
            StringBuilder body = new StringBuilder();

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
            out.println(body);
            out.println( ".");
            System.out.println("\u001B[32mMessage sent!\u001B[0m");
            //program will die if the scanner is closed
            //scanner.close();
        }
    }

    public static void quit() throws IOException
    {
        alreadyQuitted = true;
        out.println("QUIT");
    }
}