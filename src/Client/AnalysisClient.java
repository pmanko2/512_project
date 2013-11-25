package Client;

import ResInterface.*;
import TransactionManager.TransactionAbortedException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.*;
import java.io.*;


    
public class AnalysisClient
{
    static String message = "blank";
    //this references a Middleware Object
    static ResourceManager rm = null;
    
  //arraylists of room, car, flight, customer ids
    static ArrayList<Integer> roomList = new ArrayList<Integer>();
    static ArrayList<Integer> carList = new ArrayList<Integer>();
    static ArrayList<Integer> flightList = new ArrayList<Integer>();
    static ArrayList<Integer> customerList = new ArrayList<Integer>();
    
    static Random random = new Random();
    
    private static int CURRENT_TRXN;
    private static long reactionTime;

    @SuppressWarnings({ "rawtypes", "unused", "fallthrough"})
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
        int transSelection = 0;
        int testingSelection = -1;
        boolean automateTesting = false;
        boolean setupClient = false;
        int TRXNS_PER_SECOND = 2;
        reactionTime = 0;
        long averageReactionTime = 0;
        int loadNum = 0;
        


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
        
        
        if(setupClient)
        	environmentSetup();
        
        boolean correctOption = false;
        
        boolean correctInput = false;
        while(!correctInput)
        {
        	System.out.println("Please choose an option:\n\n1. Choose transaction to test \n2. Automate Testing");
            try {
    			String selection = stdin.readLine();
    			testingSelection = Integer.parseInt(selection);
    			
    			if(!(testingSelection == 1 || testingSelection == 2))
    			{
    				System.out.println("Incorrect selection, Please try again");
    				continue;
    			}
    			else
    			{
    				correctInput = true;
    			}
    			
    			if(testingSelection == 1)
                {
                	printTransactionOptions();
                	String transTypeSelection = stdin.readLine();
                	transSelection = Integer.parseInt(transTypeSelection);
                }
                else
                {
                	automateTesting = true;
                }
    			
    		} catch (IOException e) 
    		{
    			e.printStackTrace();
    		} catch(NumberFormatException nutNumber)
    		{
    			System.out.println("Incorrect input, please try again");
     			correctInput = false;
     			continue;
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
     			continue;
     		}
             
             System.out.println("Load of System?\n>");
             try {
     			String loadString = stdin.readLine();
     			TRXNS_PER_SECOND = Integer.parseInt(loadString);
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
        	
        	
        	long transactionEnd = System.currentTimeMillis();
        	reactionTime = transactionEnd - transactionStart;
        	averageReactionTime = averageReactionTime + reactionTime;
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
        
        System.out.println("Total reaction time: " + averageReactionTime);
        System.out.println("Average reaction time: " + (averageReactionTime / numTransactions));
    }
    
    private static void environmentSetup()
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
    		
    		System.out.println("Flight Num: " + flightNum + "Flight seats: " + flightSeats + "flightPrice: " + flightPrice);
    		
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
			
			boolean reservedCar = rm.reserveCar(CURRENT_TRXN, custID, chooseLocation());
			boolean reservedRoom = rm.reserveRoom(CURRENT_TRXN, custID, chooseLocation());
			int flightPrice = rm.queryFlight(CURRENT_TRXN, flightList.get(random.nextInt(flightList.size())));
			
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
		} catch (TransactionAbortedException e) {
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
		
			
			int queryBefore = rm.queryCars(CURRENT_TRXN, location);
			boolean add = rm.addCars(CURRENT_TRXN, location, carsToAdd, 100);
			int queryAfter = rm.queryCars(CURRENT_TRXN, location);
			
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
		} catch (TransactionAbortedException e) {
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
				bills[i] = rm.queryCustomerInfo(CURRENT_TRXN, customerList.get(random.nextInt(customerList.size())));
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
		} catch (TransactionAbortedException e) {
			e.printStackTrace();
		}
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
				flightNumbers.addElement(String.valueOf(flightList.get(random.nextInt(flightList.size()))));
			}
			
			
			if(rm.itinerary(CURRENT_TRXN, custID, flightNumbers, location, reserveCar, reserveRoom))
			{
				rm.commit(CURRENT_TRXN);
			}
			else
			{
				rm.abort(CURRENT_TRXN);
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TransactionAbortedException e) {
			e.printStackTrace();
		}
    }
    
    private static void addCustomer(int id)
    {
    	int cId;
    	
		try 
		{
			CURRENT_TRXN = rm.start();
			
			System.out.println(CURRENT_TRXN);
			
			cId = rm.newCustomer(CURRENT_TRXN);
			
			System.out.println("Cid: " + cId);
			
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
		} catch (TransactionAbortedException e) {
			e.printStackTrace();
		}
    }
    
    private static void addFlight(int id, int flightNumber, int flightSeats, int flightPrice)
    {
    	try 
		{
			CURRENT_TRXN = rm.start();
			
			if(rm.addFlight(CURRENT_TRXN, flightNumber, flightSeats, flightPrice))
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
		} catch (TransactionAbortedException e) {
			e.printStackTrace();
		}
    }
    
    private static void addCar(String location, int id, int numCars, int carPrice)
    {
    	try 
		{
			CURRENT_TRXN = rm.start();
			
			if(rm.addCars(CURRENT_TRXN, location, numCars, carPrice))
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
		} catch (TransactionAbortedException e) {
			e.printStackTrace();
		}
    }
    
    private static void addRoom(String location, int id, int numRooms, int roomPrice)
    {
    	try 
		{
			CURRENT_TRXN = rm.start();
			
			if(rm.addCars(CURRENT_TRXN, location, numRooms, roomPrice))
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
		} catch (TransactionAbortedException e) {
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
