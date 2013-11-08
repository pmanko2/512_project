package Client;

import ResInterface.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RMISecurityManager;
import java.util.*;
import java.io.*;

    
public class AnalysisClient
{
    static String message = "blank";
    //this references a Middleware Object
    static ResourceManager rm = null;

    @SuppressWarnings({ "rawtypes", "unchecked", "unused", "fallthrough"})
	public static void main(String args[])
    {
        Client obj = new Client();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String command = "";
        Vector arguments  = new Vector();
        int Id, Cid;
        int flightNum;
        int flightPrice;
        int flightSeats;
        boolean Room;
        boolean Car;
        int price;
        int numRooms;
        int numCars;
        String location;
        String numIterationsString = "0";
        int NUM_TRANSACTIONS;


        String server = "teaching.cs.mcgill.ca";
       // int port = 1099;
        int port = 8807;
        if (args.length > 0)
        {
            server = args[0];
        }
        if (args.length > 1)
        {
            port = Integer.parseInt(args[1]);
        }
        if (args.length > 2)
        {
            System.out.println ("Usage: java client [rmihost [rmiport]]");
            System.exit(1);
        }
        
        try 
        {
            // get a reference to the rmiregistry for the middleware server
            Registry registry = LocateRegistry.getRegistry(server, port);
            // get the proxy and the remote reference by rmiregistry lookup
            rm = (ResourceManager) registry.lookup("group_7_middle");
            if(rm!=null)
            {
                System.out.println("Successful");
                System.out.println("Connected to Middle RM");
            }
            else
            {
                System.out.println("Unsuccessful");
            }
            // make call on remote method
        } 
        catch (Exception e) 
        {    
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
        
        
        
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        
        boolean correctInput = false;
        
        while(!correctInput)
        {
        	 System.out.println("How many transactions would you like the client to submit?\n>");
             try {
     			numIterationsString = stdin.readLine();
     			NUM_TRANSACTIONS = Integer.parseInt(numIterationsString);
     			correctInput = true;
     		} catch (IOException e1) {
     			e1.printStackTrace();
     		} catch(NumberFormatException notNumber){
     			System.out.println("Incorrect input, please try again");
     			correctInput = false;
     		}
        }
        
        Random random = new Random();
        int selection = random.nextInt(22);

    }
    
    private void newItem(Type type, ArrayList<String> params)
    {
    	
    }
    
    private void bookItem(Type type, ArrayList<String> params)
    {
    	
    }
    
    private void deleteItem(Type type, ArrayList<String> params)
    {
    	
    }
    
    private void queryItem(Type type, ArrayList<String> params)
    {
    	
    }
    
    private void reserveItinerary()
    {
    	
    }

    
    private enum Type{
    	CAR, FLIGHT, PLANE
    }
}
