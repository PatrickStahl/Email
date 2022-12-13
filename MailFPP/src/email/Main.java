package email;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// interface whose implementation runs the main funciton of the reader/writer file
interface command 
{
    void runCommand();
}


public abstract class Main 
{
    public static void main(String[] args) throws Exception
    {
        // Create a map of all available commands map<key, data>
        Map<String, command> commands = new HashMap<>();
        commands.put("Socket", new command() 
        {
            public void runCommand() {
                try 
                {
                    SocketClient.main();
                } 
                catch (Exception e) 
                {
                    System.out.println("\u001B[31m" + "An exception occurred as shown below:" + "\u001B[0m");
                    e.printStackTrace();
                }
            }
        });
        commands.put("JavaMail", new command() 
        {

            public void runCommand() 
            {
                try
                {
                    JavaMailClient.main();
                } 
                catch (Exception e) 
                {
                    System.out.println("\u001B[31m" + "An exception occurred as shown below:" + "\u001B[0m");
                    e.printStackTrace();
                }
            }
        });
        commands.put("SendJavaMail", new command() 
        {

            public void runCommand() 
            {
                try
                {
                    SendJavaMail.main();
                } 
                catch (Exception e) 
                {
                    System.out.println("\u001B[31m" + "An exception occurred as shown below:" + "\u001B[0m");
                    e.printStackTrace();
                }
            }
        });
        commands.put("SendSocketMail", new command() 
        {
            public void runCommand() 
            {
                try
                {
                    SendSocketMail.main();
                } 
                catch (Exception e) 
                {
                    System.out.println("\u001B[31m" + "An exception occurred as shown below:" + "\u001B[0m");
                    e.printStackTrace();
                }
            }
        });

        // for choosing if the user wants to read or write emails
        System.out.println("\u001B[35mEnter the name of the class you want to run:\u001B[0m");
        // keySet prints the set view of all keys (here Strings) in the hashmap
        System.out.println("\u001B[35mAvailable classes: " + commands.keySet() + "\u001B[0m");

        // for some reason the program dies if I close the scanner, so he just stays open D:
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();


        // input is available in hashmap
        if (commands.containsKey(input)) 
        {
            // get returns the data that is linked to input --> basically it reads command.runCommand
            commands.get(input).runCommand();
        } 
        else 
        {
            System.out.println("\u001B[31mThe command " + input + " is unknown!\u001B[0m");
        }
        scanner.close();
    }
}