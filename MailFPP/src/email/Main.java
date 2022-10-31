package email;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.Scanner;

//interface whose implementation runs the main funciton of the reader/writer file
interface command
{
    void runCommand();
}


public abstract class Main {
    public static void main(String[] args) throws Exception {
        // Create a map of all available commands map<key, data>
        Map<String, command> commands = new HashMap<String, command>();
        commands.put("Socket", new command() 
        {
            public void runCommand()
            {
                try 
                {
                    SocketClient.main();
                }
                catch (Exception e)
                {
                    System.out.println("An exception occured as shown below:");
                    //shows line numbers and class names of the exception
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
                    System.out.println("An exception occured as shown below:");
                    //shows line numbers and class names of the exception
                    e.printStackTrace();
                }
            }
        });

        //for choosing if the user wants to read or write emails
        System.out.println("Enter the name of the class you want to run: "); 
        //keySet prints the set view of all keys (here Strings) in the hashmap
        System.out.println("Available classes: " + commands.keySet()); 
        
        //for some reason the program dies if i close the scanner so he just stays open D:
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();


        //input is available in hashmap
        if(commands.containsKey(input) == true)
        {
            //get returns the data that is linked to input --> basically it reads command.runCommand
            commands.get(input).runCommand();
        }
        else
        {
            System.out.println("The command " + input + " is unknown!");
        }
    }
}
