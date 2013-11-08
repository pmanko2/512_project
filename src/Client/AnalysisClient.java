package Client;

import ResInterface.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.*;
import java.io.*;

import javax.transaction.InvalidTransactionException;

    
public class AnalysisClient
{
    static String message = "blank";
    //this references a Middleware Object
    static ResourceManager rm = null;
    
  //arraylists of room, car, flight, customer ids
    ArrayList<Integer> roomList = new ArrayList<Integer>();
    ArrayList<Integer> carList = new ArrayList<Integer>();
    static ArrayList<Integer> flightList = new ArrayList<Integer>();
    static ArrayList<Integer> customerList = new ArrayList<Integer>();
    
    static Random random = new Random();
    
    private static int CURRENT_TRXN;
    private static long reactionTime;

    @SuppressWarnings({ "rawtypes", "unchecked", "unused", "fallthrough"})
	public static void main(String args[]) throws InterruptedException
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
        int numTransactions = 0;
        int testingSelection = -1;
        boolean automateTesting = false;
        boolean setupClient = false;
        final int TRXNS_PER_SECOND = 2;
        reactionTime = 0;
        


        String server = "teaching.cs.mcgill.ca";
       // int port = 1099;
        int port = 8807;
        if (args.length > 0)
        {
            server = args[0];
        }
        if (args.length > 1)
        {
            int clientType = Integer.parseInt(args[1]);
            setupClient = (clientType == 1) ? true : false;
        }
        if (args.length > 2)
        {
        	port = Integer.parseInt(args[2]);
        }
        
        if (args.length > 3)
        {
            System.out.println ("Usage: java client [rmihost] [setupwithclient(0 or 1)] [port]");
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
        
        boolean correctOption = false;
        
        boolean correctInput = false;
        while(!correctInput)
        {
        	System.out.println("Please choose an option:\n\n1. Choose transaction to test \n 2. Automate Testing");
            try {
    			String selection = stdin.readLine();
    			testingSelection = Integer.parseInt(selection);
    			
    			if(!(testingSelection == 1 || testingSelection == 2))
    			{
    				System.out.println("Incorrect selection, Please try again");
    				break;
    			}
    			else
    			{
    				correctInput = true;
    			}
    			
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            
            if(testingSelection == 1)
            {
            	printTransactionOptions();
            }
            else
            {
            	automateTesting = true;
            }
            
        	
        	 System.out.println("How many transactions would you like the client to submit?\n>");
             try {
     			numIterationsString = stdin.readLine();
     			numTransactions = Integer.parseInt(numIterationsString);
     			correctInput = true;
     		} catch (IOException e1) {
     			e1.printStackTrace();
     		} catch(NumberFormatException notNumber){
     			System.out.println("Incorrect input, please try again");
     			correctInput = false;
     		}
        }
        
        for(int i = 0; i < numTransactions; i++)
        {
        	long transactionStart = System.currentTimeMillis();
        	
        	if(automateTesting)
        	{
        		int transactionSelection = random.nextInt(4);
        		
        		switch(transactionSelection)
        		{
        			case 0:
        				bookCarRoomQueryFlight();
        				break;
        			case 1:
        				addQueryCars();
        				break;
        			case 3:
        				returnBills();
        				break;
        			default:
        				reserveItinerary();
        				break;
        		}
        	}
        	else
        	{
        		switch(testingSelection)
        		{
	        		case 1:
	    				bookCarRoomQueryFlight();
	    				break;
	    			case 2:
	    				addQueryCars();
	    				break;
	    			case 3:
	    				returnBills();
	    				break;
	    			default:
	    				reserveItinerary();
        				break;
        		}
        	}
        	
        	//TODO interval stuff
        	
        	long transactionEnd = System.currentTimeMillis();
        	reactionTime = transactionEnd - transactionStart;
        	System.out.println("Response Time: " + reactionTime);
        	
        	int millisForTransaction = 1000 / TRXNS_PER_SECOND;
        	int lowerTrxnBound = millisForTransaction - 50;
        	int upperTrxnBound = millisForTransaction + 50; 
        	
        	if(millisForTransaction - reactionTime < 0)
        	{
        		continue;
        	}
        	else
        	{
        		Thread.sleep(random.nextInt(upperTrxnBound - lowerTrxnBound) + lowerTrxnBound);
        	}
        		
        }
    }
    
    private void environmentSetup()
    {
    	for(int i = 0; i < 10; i++)
    	{
    		addCustomer(i);
    	}
    	
    	for(int i = 0; i < 25; i++)
    	{
    		int flightNum = random.nextInt(100) + 1;
    		int flightSeats = random.nextInt(100) + 1;
    		int flightPrice = random.nextInt(1000) + 1;
    		
    		addFlight(i, flightNum, flightSeats, flightPrice);
    	}
    	
		int numCars = 100;
		int carPrice = 200;
		int numRooms = 200;
		int roomPrice = 100;
		
		addCar("Montreal",1,numCars,carPrice);
		addCar("Toronto",2,numCars,carPrice);
		addCar("New York",3,numCars,carPrice);
		addCar("New Jersey",4,numCars,carPrice);
		
		addRoom("Montreal",1,numRooms,roomPrice);
		addRoom("Toronto",2,numRooms,roomPrice);
		addRoom("New York",3,numRooms,roomPrice);
		addRoom("New Jersey",4,numRooms,roomPrice);
    }
    
    private static void bookCarRoomQueryFlight()
    {
    	try 
		{
			CURRENT_TRXN = rm.start();

			int custID = customerList.get(random.nextInt(customerList.size()));
			
			boolean reservedCar = rm.reserveCar(1, custID, chooseLocation());
			boolean reservedRoom = rm.reserveRoom(1, custID, chooseLocation());
			int flightPrice = rm.queryFlight(1, flightList.get(random.nextInt(flightList.size())));
			
			if(reservedCar && reservedRoom && (flightPrice != -1))
			{
				rm.commit(CURRENT_TRXN);
			}
			else
			{
				rm.abort(CURRENT_TRXN);
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    private static void addQueryCars()
    {
    	try 
		{
			CURRENT_TRXN = rm.start();
			
			int carsToAdd = random.nextInt(10) + 1;
			String location = chooseLocation();
		
			
			int queryBefore = rm.queryCars(1, location);
			boolean add = rm.addCars(1, location, carsToAdd, 100);
			int queryAfter = rm.queryCars(1, location);
			
			if((queryBefore != -1) && add && (queryAfter != -1))
			{
				rm.commit(CURRENT_TRXN);
			}
			else
			{
				rm.abort(CURRENT_TRXN);
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    private static void returnBills()
    {
    	try 
		{
			CURRENT_TRXN = rm.start();
			
			int numBills = random.nextInt(5) + 1;
			String[] bills = new String[numBills];
			boolean canCommit = true;
			
			for(int i = 0; i < numBills; i++)
			{
				bills[i] = rm.queryCustomerInfo(1, customerList.get(random.nextInt(customerList.size())));
			}
			
			for(int i = 0; i < numBills; i++)
			{
				if(bills[i] == null)
					canCommit = false;
			}
			
			if(canCommit)
			{
				rm.commit(CURRENT_TRXN);
			}
			else
			{
				rm.abort(CURRENT_TRXN);
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    @SuppressWarnings("unchecked")
	private static void reserveItinerary()
    {
    	try 
		{
			CURRENT_TRXN = rm.start();
			String location = chooseLocation();
			int custID = customerList.get(random.nextInt(customerList.size()));
			int numFlights = random.nextInt(4) + 1;
			boolean reserveRoom = (random.nextInt(2) == 0) ? true : false;
			boolean reserveCar = (random.nextInt(2) == 0) ? true : false;
			Vector flightNumbers = new Vector();
			
			for(int i = 0; i < numFlights; i++)
			{
				flightNumbers.addElement(flightList.get(random.nextInt(flightList.size())));
			}
			
			
			if(rm.itinerary(1, custID, flightNumbers, location, reserveCar, reserveRoom))
			{
				rm.commit(CURRENT_TRXN);
			}
			else
			{
				rm.abort(CURRENT_TRXN);
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    private void addCustomer(int id)
    {
    	int cId;
    	
		try 
		{
			CURRENT_TRXN = rm.start();
			
			cId = rm.newCustomer(id);
			
			if(cId != -1)
			{
				customerList.add(new Integer(cId));
				rm.commit(CURRENT_TRXN);
			}
			else
			{
				rm.abort(CURRENT_TRXN);
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    private void addFlight(int id, int flightNumber, int flightSeats, int flightPrice)
    {
    	try 
		{
			CURRENT_TRXN = rm.start();
			
			if(rm.addFlight(id, flightNumber, flightSeats, flightPrice))
			{
				flightList.add(new Integer(flightNumber));
				rm.commit(CURRENT_TRXN);
			}
			else
			{
				rm.abort(CURRENT_TRXN);
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    private void addCar(String location, int id, int numCars, int carPrice)
    {
    	try 
		{
			CURRENT_TRXN = rm.start();
			
			if(rm.addCars(id, location, numCars, carPrice))
			{
				carList.add(new Integer(id));
				rm.commit(CURRENT_TRXN);
			}
			else
			{
				rm.abort(CURRENT_TRXN);
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    private void addRoom(String location, int id, int numRooms, int roomPrice)
    {
    	try 
		{
			CURRENT_TRXN = rm.start();
			
			if(rm.addCars(id, location, numRooms, roomPrice))
			{
				roomList.add(new Integer(id));
				rm.commit(CURRENT_TRXN);
			}
			else
			{
				rm.abort(CURRENT_TRXN);
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
    
    private static String chooseLocation()
    {
    	int locationSelection = random.nextInt(4);
		String location;
		
		switch(locationSelection)
		{
			case 0: 
				location = "Montreal";
				break;
			case 1:
				location = "Toronto";
				break;
			case 2: 
				location = "New York";
				break;
			default:
				location = "New Jersey";
				break;
		}
		
		return location;
    }
    
    private static void printTransactionOptions()
    {
    	System.out.println("Choose a transaction type below:");
    	System.out.println("1. Book Car and Room, Query Flight");
    	System.out.println("2. Add and Query Cars");
    	System.out.println("3. Return Customer Bills");
    	System.out.println("4. Reserve Itinerary\n");
    }
}
