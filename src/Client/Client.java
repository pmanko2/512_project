package Client;

import ResImpl.CrashType;
import ResInterface.*;
import TransactionManager.TransactionAbortedException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.util.*;
import java.io.*;

import javax.transaction.InvalidTransactionException;

    
public class Client
{
    static String message = "blank";
    //this references a Middleware Object
    static ResourceManager rm = null;
    private static int CURRENT_TRXN;
    private static boolean user_said_start;

	@SuppressWarnings("unchecked")
	public static void main(String args[]) throws TransactionAbortedException
    {
        Client obj = new Client();
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        String command = "";
        @SuppressWarnings("rawtypes")
		Vector arguments  = new Vector();
        user_said_start = false;
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
            //System.setSecurityManager(new RMISecurityManager());
        }

        
        
        
        System.out.println("\n\n\tClient Interface");
        System.out.println("Type \"help\" for list of supported commands.");
        System.out.println("\nType start to start a new transaction. To terminate a"
        		+ " transaction, type commit to save changes or abort to discard changes.");
        System.out.println("\nIf you do not start a transaction before performing an operation,"
        		+ " a transaction will be started for you and will attempt to commit automatically.");
        while(true){
	        System.out.print("\n>");
	        try{
	            //read the next command
	            command =stdin.readLine();
	        }
	        catch (IOException io){
	            System.out.println("Unable to read from standard in");
	            System.exit(1);
	        }
	        //remove heading and trailing white space
	        command=command.trim();
	        arguments=obj.parse(command);
	        
	        try
	        {
	        	//decide which of the commands this was
	            switch(obj.findChoice((String)arguments.elementAt(0))){
	            case 1: //help section
	                if(arguments.size()==1)   //command was "help"
	                	obj.listCommands();
	                else if (arguments.size()==2)  //command was "help <commandname>"
	                	obj.listSpecific((String)arguments.elementAt(1));
	                else  //wrong use of help command
	                	System.out.println("Improper use of help command. Type help or help, <commandname>");
	                break;
	                
	            case 2:  //new flight
	                if(arguments.size()!=5){
	    	            obj.wrongNumber();
	    	            break;
	                }
	                System.out.println("Adding a new Flight using id: "+arguments.elementAt(1));
	                System.out.println("Flight number: "+arguments.elementAt(2));
	                System.out.println("Add Flight Seats: "+arguments.elementAt(3));
	                System.out.println("Set Flight Price: "+arguments.elementAt(4));        
	              
                	//if the user hadn't manually started a transaction, start a transaction
                	start();
                    
    	            Id = CURRENT_TRXN;
    	            flightNum = obj.getInt(arguments.elementAt(2));
    	            flightSeats = obj.getInt(arguments.elementAt(3));
    	            flightPrice = obj.getInt(arguments.elementAt(4));
    	            
    	            if(rm.addFlight(Id,flightNum,flightSeats,flightPrice))
    	            {   
    	            	//commits only if the user hadn't manually started a transaction
    	            	autoCommit("Flight added successfully.");
                	}
    	            else
    	            {
    	                System.out.println("Flight could not be added.");
    	            	rm.abort(CURRENT_TRXN);
    	            }

	                break;
	                
	            case 3:  //new Car
	                if(arguments.size()!=5){
	    	            obj.wrongNumber();
	    	            break;
	                }
	                
	                System.out.println("Adding a new Car using id: "+arguments.elementAt(1));
	                System.out.println("Car Location: "+arguments.elementAt(2));
	                System.out.println("Add Number of Cars: "+arguments.elementAt(3));
	                System.out.println("Set Price: "+arguments.elementAt(4));

                	start();
                	
    	            Id = CURRENT_TRXN;
    	            location = obj.getString(arguments.elementAt(2));
    	            numCars = obj.getInt(arguments.elementAt(3));
    	            price = obj.getInt(arguments.elementAt(4));
    	            
    	            if(rm.addCars(Id, location, numCars, price))
    	            {   	
    	            		autoCommit("Cars added.");      	
    	            }
    	            else
    	            {
    	                System.out.println("Cars could not be added. Please try again.");
    	                rm.abort(CURRENT_TRXN);
    	            }

	                break;
	                
	            case 4:  //new Room
	                if(arguments.size()!=5){
	    	            obj.wrongNumber();
	    	            break;
	                }
	                System.out.println("Adding a new Room using id: "+arguments.elementAt(1));
	                System.out.println("Room Location: "+arguments.elementAt(2));
	                System.out.println("Add Number of Rooms: "+arguments.elementAt(3));
	                System.out.println("Set Price: "+arguments.elementAt(4));
	               
                	start();
                	
                	Id = CURRENT_TRXN;
    	            location = obj.getString(arguments.elementAt(2));
    	            numRooms = obj.getInt(arguments.elementAt(3));
    	            price = obj.getInt(arguments.elementAt(4));
    	            
    	            if(rm.addRooms(Id, location, numRooms, price))
    	            {   	
    	            		autoCommit("Rooms added.");      	
    	            }
    	            else
    	            {
    	                System.out.println("Rooms could not be added");
    	                rm.abort(CURRENT_TRXN);
    	            }

	                break;
	                
	            case 5:  //new Customer
	                if(arguments.size()!=2){
	    	            obj.wrongNumber();
	    	            break;
	                }
	                System.out.println("Adding a new Customer using id:"+arguments.elementAt(1));
	               
                	start();
                	
                	Id = CURRENT_TRXN;
                	int customer=rm.newCustomer(Id);
                	if (customer != -1)
                	{
                		autoCommit("New customer id: " + customer);      	

                	}
    	            else
    	            {
    	                System.out.println("Customer could not be added");
    	                rm.abort(CURRENT_TRXN);
    	            }

	                break;
	                
	            case 6: //delete Flight
	                if(arguments.size()!=3){
	    	            obj.wrongNumber();
	    	            break;
	                }
	                
	                System.out.println("Deleting a flight using id: "+arguments.elementAt(1));
	                System.out.println("Flight Number: "+arguments.elementAt(2));

                	start();

                	Id = CURRENT_TRXN;
                	flightNum = obj.getInt(arguments.elementAt(2));
                	
    	            if(rm.deleteFlight(Id,flightNum))
    	            {
    	            	autoCommit("Flight Deleted");
    	            }
    	            else
    	            {
    	                System.out.println("Flight could not be deleted");
    	            	rm.abort(CURRENT_TRXN);
    	            }

	                break;
	                
	            case 7: //delete Car
	                if(arguments.size()!=3){
	    	            obj.wrongNumber();
	    	            break;
	                }
	                System.out.println("Deleting the cars from a particular location  using id: "+arguments.elementAt(1));
	                System.out.println("Car Location: "+arguments.elementAt(2));

                	start();
                	
                	Id = CURRENT_TRXN;
                	location = obj.getString(arguments.elementAt(2));
                
    	            if(rm.deleteCars(Id,location))
    	            {
    	            	autoCommit("Cars Deleted.");
    	            }
    	            else
    	            {
    	                System.out.println("Cars could not be deleted");
    	                rm.abort(CURRENT_TRXN);
    	            }
	                
	                break;
	                
	            case 8: //delete Room
	                if(arguments.size()!=3)
	                {
	                	obj.wrongNumber();
	                	break;
	                }
	                
	                System.out.println("Deleting all rooms from a particular location  using id: "+arguments.elementAt(1));
	                System.out.println("Room Location: "+arguments.elementAt(2));
	
                	start();
                	
                	Id = CURRENT_TRXN;
                	location = obj.getString(arguments.elementAt(2));
               
                	if(rm.deleteRooms(Id,location))
                	{
                		autoCommit("Rooms Deleted");
                	}
                	else
                	{
                		System.out.println("Rooms could not be deleted");
                		rm.abort(CURRENT_TRXN);
                	}

	                break;
	                
	            case 9: //delete Customer
	                if(arguments.size()!=3)
	                {
	                	obj.wrongNumber();
	                	break;
	                }
	                
	                System.out.println("Deleting a customer from the database using id: "+arguments.elementAt(1));
	                System.out.println("Customer id: "+arguments.elementAt(2));

                	start();
                	
                	Id = CURRENT_TRXN;
                	customer = obj.getInt(arguments.elementAt(2));
                	
                	if(rm.deleteCustomer(Id,customer))
                	{
                		autoCommit("Customer Deleted");
                	}
                	else
                	{
                		System.out.println("Customer could not be deleted");
                		rm.abort(CURRENT_TRXN);
                	}

	                break;
	                
	            case 10: //querying a flight
	                if(arguments.size()!=3)
	                {
	                	obj.wrongNumber();
	                	break;
	                }
	                
	                System.out.println("Querying a flight using id: "+arguments.elementAt(1));
	                System.out.println("Flight number: "+arguments.elementAt(2));

                	start();

                	Id = CURRENT_TRXN;
                	flightNum = obj.getInt(arguments.elementAt(2));
                	int seats = rm.queryFlight(Id,flightNum);
                	
                	if(seats != -1)
                	{
                		autoCommit("Number of seats available: " + seats);
                	}
                	else
                	{
                		System.out.println("Could not query flight");
                		rm.abort(CURRENT_TRXN);
                	}

	                break;
	                
	            case 11: //querying a Car Location
	                if(arguments.size()!=3)
	                {
	                	obj.wrongNumber();
	                	break;
	                }
	                
	                System.out.println("Querying a car location using id: "+arguments.elementAt(1));
	                System.out.println("Car location: "+arguments.elementAt(2));

                	start();

                	Id = CURRENT_TRXN;
                	location = obj.getString(arguments.elementAt(2));
                	
                	numCars=rm.queryCars(Id,location);
                	
                	if(numCars != -1)
                	{
                		autoCommit("Number of cars at this location: " + numCars);
                	}
                	else
                	{
                		System.out.println("Could not query cars at this location");
                	}

	                break;
	                
	            case 12: //querying a Room location
	                if(arguments.size()!=3)
	                {
	                	obj.wrongNumber();
	                	break;
	                }
	                
	                System.out.println("Querying a room location using id: "+arguments.elementAt(1));
	                System.out.println("Room location: "+arguments.elementAt(2));

                	start();
                	
                	Id = CURRENT_TRXN;
                	location = obj.getString(arguments.elementAt(2));
                	numRooms=rm.queryRooms(Id,location);
                	
                	if(numRooms != -1)
                	{
                		autoCommit("Number of Rooms at this location: " + numRooms);
                	}
                	else
                	{
                		System.out.println("Could not query rooms at this lcoation");
                		rm.abort(CURRENT_TRXN);
                	}

	                break;
	                
	            case 13: //querying Customer Information
	                if(arguments.size()!=3)
	                {
	                	obj.wrongNumber();
	                	break;
	                }
	                
	                System.out.println("Querying Customer information using id: "+arguments.elementAt(1));
	                System.out.println("Customer id: "+arguments.elementAt(2));
	                
                	start();
                	
                	Id = CURRENT_TRXN;
                	customer = obj.getInt(arguments.elementAt(2));
                	String bill=rm.queryCustomerInfo(Id,customer);
                	
                	if(bill != null)
                	{
                		autoCommit("Customer Info" + bill);
                	}
                	else
                	{
                		System.out.println("Could not query customer's bill");
                		rm.abort(CURRENT_TRXN);
                	}

	                break;               
	                
	            case 14: //querying a flight Price
	                if(arguments.size()!=3)
	                {
	                	obj.wrongNumber();
	                	break;
	                }
	                
	                System.out.println("Querying a flight Price using id: "+arguments.elementAt(1));
	                System.out.println("Flight number: "+arguments.elementAt(2));
	                
                	start();
                	
                	Id = CURRENT_TRXN;
                	flightNum = obj.getInt(arguments.elementAt(2));
                	price=rm.queryFlightPrice(Id,flightNum);
                	
                	if(price != -1)
                	{
                		autoCommit("Price of a seat: " + price);
                	}
                	else
                	{
                		System.out.println("Could not query flight price");
                		rm.abort(CURRENT_TRXN);
                	}
                	
                	System.out.println("Price of a seat:"+price);

	                break;
	                
	            case 15: //querying a Car Price
	                if(arguments.size()!=3)
	                {
	                	obj.wrongNumber();
	                	break;
	                }
	                
	                System.out.println("Querying a car price using id: "+arguments.elementAt(1));
	                System.out.println("Car location: "+arguments.elementAt(2));
	                
                	start();
                	
                	Id = CURRENT_TRXN;
                	location = obj.getString(arguments.elementAt(2));
                	price=rm.queryCarsPrice(Id,location);
                	
                	if(price != -1)
                	{
                		autoCommit("Price of a car at this location: " + price);
                	}
                	else
                	{
                		System.out.println("Could not query car price at this location");
                		rm.abort(CURRENT_TRXN);
                	}
	                	              
	                break;
	
	            case 16: //querying a Room price
	                if(arguments.size()!=3)
	                {
	                	obj.wrongNumber();
	                	break;
	                }
	                
	                System.out.println("Querying a room price using id: "+arguments.elementAt(1));
	                System.out.println("Room Location: "+arguments.elementAt(2));

                	start();
                	
                	Id = CURRENT_TRXN;
                	location = obj.getString(arguments.elementAt(2));
                	price=rm.queryRoomsPrice(Id,location);
                	
                	if(price != -1)
                	{
                		autoCommit("Price of a room at this location: " + price);
                	}
                	else
                	{
                		System.out.println("Could not query room price at this location");
                		rm.abort(CURRENT_TRXN);
                	}

	                break;
	                
	            case 17:  //reserve a flight
	                if(arguments.size()!=4)
	                {
	                	obj.wrongNumber();
	                	break;
	                }
	                
	                System.out.println("Reserving a seat on a flight using id: "+arguments.elementAt(1));
	                System.out.println("Customer id: "+arguments.elementAt(2));
	                System.out.println("Flight number: "+arguments.elementAt(3));

                	start();
                	
                	Id = CURRENT_TRXN;
                	customer = obj.getInt(arguments.elementAt(2));
                	flightNum = obj.getInt(arguments.elementAt(3));
                	
    	            if(rm.reserveFlight(Id,customer,flightNum))
    	            {
    	            	autoCommit("Flight Reserved");
    	            }
    	            else
    	            {
    	            	System.out.println("Flight could not be reserved.");
    	            	rm.abort(CURRENT_TRXN);
    	            }

	                break;
	                
	            case 18:  //reserve a car
	                if(arguments.size()!=4)
	                {
	                	obj.wrongNumber();
	                	break;
	                }
	                
	                System.out.println("Reserving a car at a location using id: "+arguments.elementAt(1));
	                System.out.println("Customer id: "+arguments.elementAt(2));
	                System.out.println("Location: "+arguments.elementAt(3));

                	start();
                	
                	Id = CURRENT_TRXN;
                	customer = obj.getInt(arguments.elementAt(2));
                	location = obj.getString(arguments.elementAt(3));
                
    	            if(rm.reserveCar(Id,customer,location))
    	            {
    	            	autoCommit("Car Reserved");
    	            }
    	            else
    	            {
    	            	System.out.println("Car could not be reseved");
    	            	rm.abort(CURRENT_TRXN);
    	            }

	                break;
	                
	            case 19:  //reserve a room
	                if(arguments.size()!=4)
	                {
	                	obj.wrongNumber();
	                	break;
	                }
	                
	                System.out.println("Reserving a room at a location using id: "+arguments.elementAt(1));
	                System.out.println("Customer id: "+arguments.elementAt(2));
	                System.out.println("Location: "+arguments.elementAt(3));

                	start();
                	
                	Id = CURRENT_TRXN; 
                	customer = obj.getInt(arguments.elementAt(2));
                	location = obj.getString(arguments.elementAt(3));
                
                	if(rm.reserveRoom(Id,customer,location))
    	            {
    	            	autoCommit("Room Reserved");
    	            }
    	            else
    	            {
    	            	System.out.println("Room could not be reseved");
    	            	rm.abort(CURRENT_TRXN);
    	            }

	                break;
	                
	            case 20:  //reserve an Itinerary
	                if(arguments.size()<7)
	                {
	                	obj.wrongNumber();
	                	break;
	                }
	                
	                System.out.println("Reserving an Itinerary using id:"+arguments.elementAt(1));
	                System.out.println("Customer id:"+arguments.elementAt(2));
	                
	                for(int i=0;i<arguments.size()-6;i++)
	                	System.out.println("Flight number"+arguments.elementAt(3+i));
	                
	                System.out.println("Location for Car/Room booking:"+arguments.elementAt(arguments.size()-3));
	                System.out.println("Car to book?:"+arguments.elementAt(arguments.size()-2));
	                System.out.println("Room to book?:"+arguments.elementAt(arguments.size()-1));
	                	                	
                	start();
                	
                	Id = CURRENT_TRXN;
                	customer = obj.getInt(arguments.elementAt(2));
                	
    	            @SuppressWarnings("rawtypes")
    				Vector flightNumbers = new Vector();
    	            for(int i=0;i<arguments.size()-6;i++)
    	                flightNumbers.addElement(arguments.elementAt(3+i));
    	            
    	            location = obj.getString(arguments.elementAt(arguments.size()-3));
    	            Car = obj.getBoolean(arguments.elementAt(arguments.size()-2));
    	            Room = obj.getBoolean(arguments.elementAt(arguments.size()-1));
    	            
    	            if(rm.itinerary(Id,customer,flightNumbers,location,Car,Room))
    	            {
    	            	autoCommit("Itinerary Reserved");
    	            }
    	            else
    	            {
    	            	System.out.println("Itinerary could not be reserved");
    	            	rm.abort(CURRENT_TRXN);
    	            }
	                break;
	                            
	            case 21:  //quit the client
	                if(arguments.size()!=1){
		                obj.wrongNumber();
		                break;
	                }
	                System.out.println("Quitting client.");
	                System.exit(1);
	                break;
	                            
	            case 22:  //new Customer given id
	                if(arguments.size()!=3){
	    	            obj.wrongNumber();
	    	            break;
	                }
	                System.out.println("Adding a new Customer using id:"+arguments.elementAt(1) + " and cid " +arguments.elementAt(2));
    	           
	                start();
                	
                	Id = CURRENT_TRXN;
    	            Cid = obj.getInt(arguments.elementAt(2));

                	if (rm.newCustomer(Id,Cid))
                	{
                		autoCommit("New customer id: " + Cid);      	

                	}
    	            else
    	            {
    	                System.out.println("Customer could not be added. Another client may be "
    	                		+ "holding a lock for this customer ID or it might already exist.");
    	                rm.abort(CURRENT_TRXN);
    	            }
	                
	                break;
	                
	            case 23:
	                if(arguments.size()==2){
	    	            int idToUse = obj.getInt(arguments.elementAt(1));
	    	            CURRENT_TRXN = idToUse;
	    	            user_said_start = true;
	    	            break;
	                }
	                else
	                {
	    				CURRENT_TRXN = rm.start();
	    	        	user_said_start = true;
	    				System.out.println("A new transaction has been started. To commit changes at any "
	    						+ "point, type commit. To discard changes, type abort.");
		            	break;	
	                }
	            	
	            case 24:
    				boolean result = rm.commit(CURRENT_TRXN);
    				user_said_start = false;
    				if (result)
    				{
    					System.out.println("All changes since last commit have been committed."
    					    						+ " To start a new transaction, type start.");
    				}
    				else
    				{
    					System.out.println("Something went wrong and this transaction was aborted. "
    							+ "Please try again.");
    				}
	            	break;
	            	
	            case 25:
    				rm.abort(CURRENT_TRXN);
    				user_said_start = false;
    				System.out.println("All changes since last commit have been discarded. T"
    						+ "o start a new transaction, type start.");
	            	break;
	            case 26:
	            	rm.shutdown();
	                System.out.println("Quitting client.");
	                System.exit(1);
	                break;
	            case 27:
	                if(arguments.size()!=3){
		                obj.wrongNumber();
		                break;
	                }
    	            String serverToCrash = obj.getString(arguments.elementAt(1));
    	            //make sure string is a valid option
    	            if (!serverToCrash.equals("flights") && !serverToCrash.equals("cars")
    	            		&& !serverToCrash.equals("hotels") && !serverToCrash.equals("middleware")) 
    	            {
    	            	System.out.println("\"" + serverToCrash + "\" isn't a valid option. Please try again.");
    	            	break;
    	            }
    	            
    	            String crashType = obj.getString(arguments.elementAt(2));
    	            
    	            if(!crashType.equals("beforeVoteRequest") && !crashType.equals("afterRequestBeforeVoteReturn")
    	            		&& !crashType.equals("afterVoteReturnBeforeCommitRequest") && !crashType.equals("afterSendingAnswer")
    	            		&& !crashType.equals("afterDecisionBeforeCommit") && !crashType.equals("TMBeforeReplies")
    	            		&& !crashType.equals("TMSomeReplies") && !crashType.equals("TMBeforeDecision")
    	            		&& !crashType.equals("TMSomeDecisions") && !crashType.equals("TMAllDecisions")
    	            		&& !crashType.equals("TMBeforeDecisionSent"))
    	            {
    	            	System.out.println("\"" + crashType + "\" isn't a valid option. Please try again.");
    	            	break;
    	            }
    	            //send request
    	            CrashType type;
    	            
    	            if(crashType.equals("beforeVoteRequest"))
    	            	type = CrashType.BEFORE_VOTE_REQUEST;
    	            else if(crashType.equals("afterRequestBeforeVoteReturn"))
    	            	type = CrashType.AFTER_REQUEST_BEFORE_VOTE_RETURN;
    	            else if(crashType.equals("afterSendingAnswer"))
    	            	type = CrashType.AFTER_SENDING_ANSWER;
    	            else if(crashType.equals("afterDecisionBeforeCommit"))
    	            	type = CrashType.AFTER_DECISION_BEFORE_COMMIT;
    	            else if(crashType.equals("TMBeforeReplies"))
    	            	type = CrashType.TM_BEFORE_REPLIES;
    	            else if(crashType.equals("TMSomeReplies"))
    	            	type = CrashType.TM_SOME_REPLIES;
    	            else if(crashType.equals("TMBeforeDecision"))
    	            	type = CrashType.TM_BEFORE_DECISION;
    	            else if(crashType.equals("TMSomeDecisions"))
    	            	type = CrashType.TM_SOME_DECISIONS_SENT;
    	            else if(crashType.equals("TMAllDecisions"))
    	            	type = CrashType.TM_ALL_DECISIONS_SENT;
    	            else if(crashType.equals("TMBeforeDecisionSent"))
    	            	type = CrashType.TM_BEFORE_DECISION_SENT;
    	            else
    	            	type = CrashType.AFTER_VOTE_RETURN_BEFORE_COMMIT_REQUEST;
    	            
    	            rm.setCrashFlags(serverToCrash, type);
    	       
	            	break;
	            	
	            default:
	                System.out.println("The interface does not support this command.");
	                break;
	            }//end of switch
	        }
	        catch (TransactionAbortedException e)
	        {
	        	//e.printStackTrace();
	        	System.out.println("The transaction has already been terminated - "
	        			+ "this is likely because the transaction timed out. Or, "
	        			+ "perhaps the last transaction was already committed/aborted. "
	        			+ "Please try again.");
	        	user_said_start = false;
	        } 
	        catch (InvalidTransactionException e) 
	        {
				e.printStackTrace();
				user_said_start = false;
			} 
	        catch (NullPointerException e)
	        {
	        	System.out.println("The transaction didn't exist serverside. "
	        			+ "This likely means that it was aborted by the server. Please try again.");
	        	user_said_start = false;
	        	//e.printStackTrace();
	        	try {
					rm.abort(CURRENT_TRXN);
				} catch (InvalidTransactionException e1) {
					e1.printStackTrace();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (TransactionAbortedException e1)
				{
					System.out.println("\nConfirmation - this transaction was aborted by the server.");
				}
	        }
	        catch (RemoteException e) 
	        {
				e.printStackTrace();
			}
	        catch (Exception e) 
	        {
				e.printStackTrace();
			}
        
        }//end of while(true)
    }
	
	/**
	 * This method starst a new transaction if the user hadn't already typed
	 * start; otherwise, it does nothing.
	 */
	private static void start()
	{
		if (user_said_start)
		{
			//do nothing
		}
		//otherwise, start a transaction
		else
		{
			try {
				CURRENT_TRXN = rm.start();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void autoCommit(String success) throws TransactionAbortedException
	{
        try {
        	if (!user_said_start)
        	{
    			rm.commit(CURRENT_TRXN);
    			user_said_start = false;
    			System.out.println("Autocommitted");
        	}
			System.out.println(success);
			
		} catch (InvalidTransactionException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NullPointerException e)
		{
			System.out.println("The last transaction has already been terminated.");
		}
	}
        
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector parse(String command)
    {
    Vector arguments = new Vector();
    StringTokenizer tokenizer = new StringTokenizer(command,",");
    String argument ="";
    while (tokenizer.hasMoreTokens())
        {
        argument = tokenizer.nextToken();
        argument = argument.trim();
        arguments.add(argument);
        }
    return arguments;
    }
    public int findChoice(String argument)
    {
    if (argument.compareToIgnoreCase("help")==0)
        return 1;
    else if(argument.compareToIgnoreCase("newflight")==0)
        return 2;
    else if(argument.compareToIgnoreCase("newcar")==0)
        return 3;
    else if(argument.compareToIgnoreCase("newroom")==0)
        return 4;
    else if(argument.compareToIgnoreCase("newcustomer")==0)
        return 5;
    else if(argument.compareToIgnoreCase("deleteflight")==0)
        return 6;
    else if(argument.compareToIgnoreCase("deletecar")==0)
        return 7;
    else if(argument.compareToIgnoreCase("deleteroom")==0)
        return 8;
    else if(argument.compareToIgnoreCase("deletecustomer")==0)
        return 9;
    else if(argument.compareToIgnoreCase("queryflight")==0)
        return 10;
    else if(argument.compareToIgnoreCase("querycar")==0)
        return 11;
    else if(argument.compareToIgnoreCase("queryroom")==0)
        return 12;
    else if(argument.compareToIgnoreCase("querycustomer")==0)
        return 13;
    else if(argument.compareToIgnoreCase("queryflightprice")==0)
        return 14;
    else if(argument.compareToIgnoreCase("querycarprice")==0)
        return 15;
    else if(argument.compareToIgnoreCase("queryroomprice")==0)
        return 16;
    else if(argument.compareToIgnoreCase("reserveflight")==0)
        return 17;
    else if(argument.compareToIgnoreCase("reservecar")==0)
        return 18;
    else if(argument.compareToIgnoreCase("reserveroom")==0)
        return 19;
    else if(argument.compareToIgnoreCase("itinerary")==0)
        return 20;
    else if (argument.compareToIgnoreCase("quit")==0)
        return 21;
    else if (argument.compareToIgnoreCase("newcustomerid")==0)
        return 22;
    else if (argument.compareToIgnoreCase("start")==0)
    	return 23;
    else if (argument.compareToIgnoreCase("commit")==0)
    	return 24;
    else if (argument.compareToIgnoreCase("abort")==0)
    	return 25;
    else if (argument.compareToIgnoreCase("shutdown")==0)
    	return 26;
    else if (argument.compareToIgnoreCase("crash")==0)
    	return 27;
    else
        return 666;

    }

    public void listCommands()
    {
    System.out.println("\nWelcome to the client interface provided to test your project.");
    System.out.println("Commands accepted by the interface are:");
    System.out.println("help");
    System.out.println("start\ncommit\nabort");
    System.out.println("newflight\nnewcar\nnewroom\nnewcustomer\nnewcustomerid\ndeleteflight\ndeletecar\ndeleteroom");
    System.out.println("deletecustomer\nqueryflight\nquerycar\nqueryroom\nquerycustomer");
    System.out.println("queryflightprice\nquerycarprice\nqueryroomprice");
    System.out.println("reserveflight\nreservecar\nreserveroom\nitinerary");
    System.out.println("nquit\nshutdown\ncrash");
    System.out.println("\ntype help, <commandname> for detailed info(NOTE the use of comma).");
    }


    public void listSpecific(String command)
    {
    System.out.print("Help on: ");
    switch(findChoice(command))
        {
        case 1:
        System.out.println("Help");
        System.out.println("\nTyping help on the prompt gives a list of all the commands available.");
        System.out.println("Typing help, <commandname> gives details on how to use the particular command.");
        break;

        case 2:  //new flight
        System.out.println("Adding a new Flight.");
        System.out.println("Purpose:");
        System.out.println("\tAdd information about a new flight.");
        System.out.println("\nUsage:");
        System.out.println("\tnewflight,<id>,<flightnumber>,<flightSeats>,<flightprice>");
        break;
        
        case 3:  //new Car
        System.out.println("Adding a new Car.");
        System.out.println("Purpose:");
        System.out.println("\tAdd information about a new car location.");
        System.out.println("\nUsage:");
        System.out.println("\tnewcar,<id>,<location>,<numberofcars>,<pricepercar>");
        break;
        
        case 4:  //new Room
        System.out.println("Adding a new Room.");
        System.out.println("Purpose:");
        System.out.println("\tAdd information about a new room location.");
        System.out.println("\nUsage:");
        System.out.println("\tnewroom,<id>,<location>,<numberofrooms>,<priceperroom>");
        break;
        
        case 5:  //new Customer
        System.out.println("Adding a new Customer.");
        System.out.println("Purpose:");
        System.out.println("\tGet the system to provide a new customer id. (same as adding a new customer)");
        System.out.println("\nUsage:");
        System.out.println("\tnewcustomer,<id>");
        break;
        
        
        case 6: //delete Flight
        System.out.println("Deleting a flight");
        System.out.println("Purpose:");
        System.out.println("\tDelete a flight's information.");
        System.out.println("\nUsage:");
        System.out.println("\tdeleteflight,<id>,<flightnumber>");
        break;
        
        case 7: //delete Car
        System.out.println("Deleting a Car");
        System.out.println("Purpose:");
        System.out.println("\tDelete all cars from a location.");
        System.out.println("\nUsage:");
        System.out.println("\tdeletecar,<id>,<location>,<numCars>");
        break;
        
        case 8: //delete Room
        System.out.println("Deleting a Room");
        System.out.println("\nPurpose:");
        System.out.println("\tDelete all rooms from a location.");
        System.out.println("Usage:");
        System.out.println("\tdeleteroom,<id>,<location>,<numRooms>");
        break;
        
        case 9: //delete Customer
        System.out.println("Deleting a Customer");
        System.out.println("Purpose:");
        System.out.println("\tRemove a customer from the database.");
        System.out.println("\nUsage:");
        System.out.println("\tdeletecustomer,<id>,<customerid>");
        break;
        
        case 10: //querying a flight
        System.out.println("Querying flight.");
        System.out.println("Purpose:");
        System.out.println("\tObtain Seat information about a certain flight.");
        System.out.println("\nUsage:");
        System.out.println("\tqueryflight,<id>,<flightnumber>");
        break;
        
        case 11: //querying a Car Location
        System.out.println("Querying a Car location.");
        System.out.println("Purpose:");
        System.out.println("\tObtain number of cars at a certain car location.");
        System.out.println("\nUsage:");
        System.out.println("\tquerycar,<id>,<location>");        
        break;
        
        case 12: //querying a Room location
        System.out.println("Querying a Room Location.");
        System.out.println("Purpose:");
        System.out.println("\tObtain number of rooms at a certain room location.");
        System.out.println("\nUsage:");
        System.out.println("\tqueryroom,<id>,<location>");        
        break;
        
        case 13: //querying Customer Information
        System.out.println("Querying Customer Information.");
        System.out.println("Purpose:");
        System.out.println("\tObtain information about a customer.");
        System.out.println("\nUsage:");
        System.out.println("\tquerycustomer,<id>,<customerid>");
        break;               
        
        case 14: //querying a flight for price 
        System.out.println("Querying flight.");
        System.out.println("Purpose:");
        System.out.println("\tObtain price information about a certain flight.");
        System.out.println("\nUsage:");
        System.out.println("\tqueryflightprice,<id>,<flightnumber>");
        break;
        
        case 15: //querying a Car Location for price
        System.out.println("Querying a Car location.");
        System.out.println("Purpose:");
        System.out.println("\tObtain price information about a certain car location.");
        System.out.println("\nUsage:");
        System.out.println("\tquerycarprice,<id>,<location>");        
        break;
        
        case 16: //querying a Room location for price
        System.out.println("Querying a Room Location.");
        System.out.println("Purpose:");
        System.out.println("\tObtain price information about a certain room location.");
        System.out.println("\nUsage:");
        System.out.println("\tqueryroomprice,<id>,<location>");        
        break;

        case 17:  //reserve a flight
        System.out.println("Reserving a flight.");
        System.out.println("Purpose:");
        System.out.println("\tReserve a flight for a customer.");
        System.out.println("\nUsage:");
        System.out.println("\treserveflight,<id>,<customerid>,<flightnumber>");
        break;
        
        case 18:  //reserve a car
        System.out.println("Reserving a Car.");
        System.out.println("Purpose:");
        System.out.println("\tReserve a given number of cars for a customer at a particular location.");
        System.out.println("\nUsage:");
        System.out.println("\treservecar,<id>,<customerid>,<location>,<nummberofCars>");
        break;
        
        case 19:  //reserve a room
        System.out.println("Reserving a Room.");
        System.out.println("Purpose:");
        System.out.println("\tReserve a given number of rooms for a customer at a particular location.");
        System.out.println("\nUsage:");
        System.out.println("\treserveroom,<id>,<customerid>,<location>,<nummberofRooms>");
        break;
        
        case 20:  //reserve an Itinerary
        System.out.println("Reserving an Itinerary.");
        System.out.println("Purpose:");
        System.out.println("\tBook one or more flights.Also book zero or more cars/rooms at a location.");
        System.out.println("\nUsage:");
        System.out.println("\titinerary,<id>,<customerid>,<flightnumber1>....<flightnumberN>,<LocationToBookCarsOrRooms>,<NumberOfCars>,<NumberOfRoom>");
        break;
        

        case 21:  //quit the client
        System.out.println("Quitting client.");
        System.out.println("Purpose:");
        System.out.println("\tExit the client application.");
        System.out.println("\nUsage:");
        System.out.println("\tquit");
        break;
        
        case 22:  //new customer with id
            System.out.println("Create new customer providing an id");
            System.out.println("Purpose:");
            System.out.println("\tCreates a new customer with the id provided");
            System.out.println("\nUsage:");
            System.out.println("\tnewcustomerid, <id>, <customerid>");
            break;
            
        case 23: //start
        	System.out.println("Starts a new transaction");
        	System.out.println("\nUsage:\n\tstart");
        	break;
        	
        case 24: //commit
        	System.out.println("Commits the last transaction that was started.");
        	System.out.println("Purpose:\n\tSaves all changes made since start was called by user.");
        	System.out.println("\nUsage:\n\tcommit");
        	break;
        	
        case 25: //abort
        	System.out.println("Aborts the last transaction that was started.");
        	System.out.println("Purpose:\n\tDiscards all changes made since start was called by user.");
        	System.out.println("\nUsage:\n\tabort");
        	break;

        case 26:
        	System.out.println("Shuts down servers (RM and MW) and then quits the client. This"
        			+ "call assumes that it will not be detrimental to kill all these processes"
        			+ "immediately.");
        	break;
        	
        case 27:
        	System.out.println("Simulates a crash in a particular server.\n Usage: crash, [name]"
        			+ "where name can be either flights, cars, hotels, or middleware. Note that"
        			+ "middleware contains the transaction manager/coordinator.");
        	break;
        default:
	        System.out.println(command);
	        System.out.println("The interface does not support this command.");
	        break;
        }
    }
    
    public void wrongNumber() {
    System.out.println("The number of arguments provided in this command are wrong.");
    System.out.println("Type help, <commandname> to check usage of this command.");
    }



    public int getInt(Object temp) throws Exception {
    try {
        return (new Integer((String)temp)).intValue();
        }
    catch(Exception e) {
        throw e;
        }
    }
    
    public boolean getBoolean(Object temp) throws Exception {
        try {
            return (new Boolean((String)temp)).booleanValue();
            }
        catch(Exception e) {
            throw e;
            }
    }

    public String getString(Object temp) throws Exception {
    try {    
        return (String)temp;
        }
    catch (Exception e) {
        throw e;
        }
    }
}
