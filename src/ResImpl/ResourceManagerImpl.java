// -------------------------------
// adapted from Kevin T. Manley

// CSE 593
//
/**
 * Added a few suppresswarnings to clean up compile time
 * (I'm putting faith in whoever wrote this class for now).
 * If there's time, may parameterize the offending objects.
 * -Nicolas
 */
package ResImpl;

import ResInterface.*;
import TransactionManager.OP_CODE;
import TransactionManager.Vote;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RMISecurityManager;

import javax.transaction.InvalidTransactionException;

@SuppressWarnings("unchecked")
public class ResourceManagerImpl implements ResourceManager 
{
	protected static RMHashtable m_itemHT = new RMHashtable();
	private static RMHashtable non_committed_items = new RMHashtable();
	private static RMHashtable abort_items = new RMHashtable();
	private static String rm_name;
	private static final String registry_name = "group_7_RM";
	private static int port;
	private static CrashType failType;
	private static String serverToCrash;

    public static void main(String args[]) {
    	
    	failType = null;
    	serverToCrash = null;
    	
        // Figure out where server is running
        String server = "localhost";
        port = 7707;

        if (args.length == 1) {
        	rm_name = args[0];
        } else if (args.length == 2) {
        	rm_name = args[0];
            server = server + ":" + args[1];
            port = Integer.parseInt(args[1]);
        } else if (args.length != 1 &&  args.length != 2) {
            System.err.println ("Wrong usage");
            System.out.println("Usage: java ResImpl.ResourceManagerImpl [port]");
            System.exit(1);
        }

        Trace.info("RM_NAME: " + rm_name);
        
        try {
            // create a new Server object
            ResourceManagerImpl obj = new ResourceManagerImpl();
            // dynamically generate the stub (client proxy)
            ResourceManager rm = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);
            
            
            
            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind(registry_name, rm);

            //read in any existing data
            System.out.println("Reading in existing data...");
            String masterPath = "/home/2011/nwebst1/comp512/data/" + rm_name + "/master_record.loc";
            File f = new File(masterPath);
            //if Master Record doesn't exist we ignore all other file reads
            if (f.exists())
            {
            	//get path to master record
            	FileInputStream fis = new FileInputStream(masterPath);
            	ObjectInputStream ois = new ObjectInputStream(fis);
            	String masterRecordPath = (String) ois.readObject();
            	fis.close();
            	ois.close();
            	
            	//get paths to data items for this RM
            	String filePathItems = masterRecordPath + "items_table.data";
            	
            	//create file objects for these data files
            	File items_file = new File(filePathItems);
    	      	
    	      	//load items data into memory
    	    	if(items_file.exists()){
    	        	fis = new FileInputStream(items_file);
    	        	ois = new ObjectInputStream(fis);

    	        	m_itemHT = (RMHashtable) ois.readObject();
    	        	fis.close();
    	        	ois.close();
    	        }
            }
            
           
    	
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
    }
    
    /**
     * This method is called whenever something is committed/aborted in order to flush changes to disk; 
     */
    public synchronized void flushToDisk() throws RemoteException
    {   
    	try {
	    	//retrieve master record file (if it doesn't exist, create it and write out string)
	        String masterPath = "/home/2011/nwebst1/comp512/data/" + rm_name + "/master_record.loc";
			String newLocation = "/home/2011/nwebst1/comp512/data/" + rm_name;
	        
	        File masterFile = new File(masterPath);
	        
	        //if master doesn't exist, create it and write default path
	        if (!masterFile.exists())
	        {
	        	//create master record file
	        	masterFile.getParentFile().getParentFile().mkdir();
	        	masterFile.getParentFile().mkdir();
	        	masterFile.createNewFile();
	        	
	        	//create default string
	        	newLocation = "/home/2011/nwebst1/comp512/data/" + rm_name + "/dataA/";
	        	
	        	FileOutputStream fos = new FileOutputStream(masterFile);
	        	ObjectOutputStream oos = new ObjectOutputStream(fos);
	        	oos.writeObject(newLocation);
	        	fos.close();
	        	oos.close();
	        }
	        //otherwise, read in string file path for master record location
	        else
	        {
	        	FileInputStream fis = new FileInputStream(masterFile);
	        	ObjectInputStream ois = new ObjectInputStream(fis);
	        	String dataPath = (String) ois.readObject();
	        	fis.close();
	        	ois.close();
	        	
	        	//update master record		
				String[] masterPathArray = dataPath.split("/");
				String data_location = masterPathArray[masterPathArray.length - 1];
				
				if (data_location.equals("dataA"))
				{
					newLocation = newLocation + "/dataB/";
				}
				else
				{
					newLocation = newLocation + "/dataA/";
				}
				
				Trace.info("NEW MASTERFILE LOCATION: " + newLocation);
				
				//write new location to master_record.loc
				masterFile = new File(masterPath);
				FileOutputStream fos = new FileOutputStream(masterFile);
		    	ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(newLocation);
				fos.close();
				oos.close();
	        }
	        
	    	//create file paths for data for this RM
        	//get paths to data items for this RM
        	String filePathItems = newLocation + "items_table.data";
        	
        	//create file objects so that we can write data to disk
	    	File items_file = new File(filePathItems);
	    	
    		// if files don't exist, then create then
    		if (!items_file.exists()) {
    			items_file.getParentFile().mkdirs();
    			items_file.createNewFile();
    		}
    		
        	//write "persistent" items to disk
	    	FileOutputStream fos = new FileOutputStream(items_file);
	    	ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(m_itemHT);
			fos.close();
			oos.close();		
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}  	
    }
    
    /**
     * This method loads in the least-recently written data
     */
    public void rollback() throws RemoteException 
    {
    	try {
		    System.out.println("Reading in existing data...");
		    String masterPath = "/home/2011/nwebst1/comp512/data/" + rm_name + "/master_record.loc";
		    File f = new File(masterPath);
		    
			String newLocation = "/home/2011/nwebst1/comp512/data/" + rm_name;
		    
			//if master doesn't exist, create it and write default path
		    if (f.exists())
		    {
			    //get path to master record
		    	FileInputStream fis = new FileInputStream(f);
		    	ObjectInputStream ois = new ObjectInputStream(fis);
		    	String dataPath = (String) ois.readObject();
		    	fis.close();
		    	ois.close();
		    	
		    	//update master record		
				String[] masterPathArray = dataPath.split("/");
				String data_location = masterPathArray[masterPathArray.length - 1];
				
				if (data_location.equals("dataA"))
				{
					newLocation = newLocation + "/dataB/";
				}
				else
				{
					newLocation = newLocation + "/dataA/";
				}	
		    }
			
			//get paths to data items for this RM
			String filePathItems = newLocation + "items_table.data";
			
			//create file objects for these data files
			File items_file = new File(filePathItems);
		  	
		  	//load items data into memory
			if(items_file.exists()){
		    	FileInputStream fis = new FileInputStream(items_file);
		    	ObjectInputStream ois = new ObjectInputStream(fis);
		    	m_itemHT = (RMHashtable) ois.readObject();
		    	fis.close();
		    	ois.close();
		    }
    	}
    	catch (IOException e) 
    	{
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    }
     
    public ResourceManagerImpl() throws RemoteException {
    }
     
    public int start()
    {
    	return -1;
    }
    
    /**
     * Commit operation with ID
     */
    public boolean commit(int op_id) throws RemoteException, InvalidTransactionException
    {
    	if(failType == CrashType.AFTER_DECISION_BEFORE_COMMIT)
    	{
    		crash(serverToCrash);
    	}
    	
    	//write any changes to disk
    	ReservableItem item =(ReservableItem)non_committed_items.get("" + op_id);
    	if (item != null)
    	{
        	writeData(op_id, item.getKey(), item);
        	non_committed_items.remove("" + op_id);
    	}
    	//delete any temp data from abort_items
    	item = (ReservableItem)abort_items.get("" + op_id);
    	if (item != null)
    	{
    		abort_items.remove("" + op_id);
    	}

    	return true;
    }
    
    /**
     * Abort operation with ID. Removes data from temp object if it's there.
     * Also writes back any items that were in abort_items (this is so that
     * we don't have to manually undo increments or decrements that are caused
     * when calling addx on an item x that already exists
     */
    public void abort(int op_id) throws RemoteException
    {
    	if(failType == CrashType.AFTER_DECISION_BEFORE_COMMIT)
    	{
    		crash(serverToCrash);
    	}
    	
    	//put back any old data (used for cases where the state of an object is changed
    	//instead of having been simply newly created
    	ReservableItem item = (ReservableItem) abort_items.get("" + op_id);
    	if (item != null)
    	{
    		writeData(op_id, item.getKey(), item);
    	}
    	
    	//remove any temporary data from non_committed_items
    	item = (ReservableItem) non_committed_items.get("" + op_id);
    	if (item != null)
    	{
        	non_committed_items.remove(item.getKey());
    	}
    }
    
    // Reads a data item
    private RMItem readData( int id, String key )
    {
            return (RMItem) m_itemHT.get(key);
    }
    
    //Reads a data item from uncomitted data
    private RMItem readNonCommittedData( int id )
    {
         RMItem item = (RMItem) non_committed_items.get("" + id);
        /*if (item == null)
        {
        	item = (RMItem) non_committed_items.get(key);
        }*/
        return item;
    }

    // Writes a data item
    private void writeData( int id, String key, RMItem value )
    {
            m_itemHT.put(key, value);
    }
    
    // Remove the item out of storage
    protected RMItem removeData(int id, String key) 
    {	
        return (RMItem)m_itemHT.remove(key);
    }
    
    // deletes the entire item
    protected boolean deleteItem(int id, String key)
    {
        Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
        ReservableItem curObj;     
        if ((curObj = (ReservableItem) readNonCommittedData( id ))==null)
       	{
        	curObj = (ReservableItem) readData( id, key);
  		}
        
        // Check if there is such an item in the storage
        if ( curObj == null ) {
            Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
            return false;
        } else {
            if (curObj.getReserved()==0) {
            	abort_items.put("" + id, curObj);
                if (removeData(id, curObj.getKey()) == null)
                {
                	Trace.info("ITEM WAS FOUND AND HOPEFULLY REMOVED");
                }
                Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
                return true;
            }
            else {
                Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers still have it reserved." );
                return false;
            }
        } // if
    }
    

    // query the number of available seats/rooms/cars
    protected int queryNum(int id, String key) 
    {
        Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
        
        ReservableItem curObj = null;
        
        if ((curObj = (ReservableItem) readNonCommittedData( id ))==null)
       	{
        	curObj = (ReservableItem) readData( id, key);
  		}
        
        int value = 0;  
        if ( curObj != null ) {
            value = curObj.getCount();
        } // else
        Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
        return value;
    }    
    
    // query the price of an item
    protected int queryPrice(int id, String key) 
    {
        Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
        
        ReservableItem curObj = null;
        
        if ((curObj = (ReservableItem) readNonCommittedData( id ))==null)
       	{
        	curObj = (ReservableItem) readData( id, key);
  		}
 
        int value = 0; 
        if ( curObj != null ) {
            value = curObj.getPrice();
        } // else
        Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
        return value;        
    }
    
    // reserve an item
    protected boolean reserveItem(int id, int customerID, String key, String location) {
        Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );        
        // Read customer object if it exists (and read lock it)
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );        
        if ( cust == null ) {
            Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
            return false;
        } 
        
        // check if the item is available
        ReservableItem item = (ReservableItem)readData(id, key);
        if ( item == null ) {
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
            return false;
        } else if (item.getCount()==0) {
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
            return false;
        } else {            
            cust.reserve( key, location, item.getPrice());        
            writeData( id, cust.getKey(), cust );
            
            // decrease the number of available items in the storage
            item.setCount(item.getCount() - 1);
            item.setReserved(item.getReserved()+1);
            
            Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
            return true;
        }        
    }
    
    /**
     * Added 25/09/13 - returns a reservable item (so that Middleware can manage reservations without 
     * providing customer data to RMs or needing to manage inventory
     */
    public ReservableItem getReservableItem(int id, String key)
    {
        ReservableItem reserved_item;
        if ((reserved_item = (ReservableItem) readNonCommittedData( id ))==null)
       	{
        	reserved_item = (ReservableItem) readData( id, key);
  		}
    	return reserved_item;
    }
    
	/**
	 * Method updates object's availability after it has been reserved in the Middleware 
	 */
	public boolean itemReserved(int id, ReservableItem item) throws RemoteException {

        abort_items.put("" + id, item);

        ReservableItem item_to_update;
        if ((item_to_update = (ReservableItem) readNonCommittedData( id ))==null)
       	{
        	item_to_update = (ReservableItem) readData( id, item.getKey());
  		}

        // decrease the number of available items in the storage
        item_to_update.setCount(item_to_update.getCount() - 1);
        item_to_update.setReserved(item_to_update.getReserved()+1);
		return true;
	}
	
	/**
	 * Method used when a customer is deleted - this method frees up the resources 
	 * of that customer
	 * @param id
	 * @param item
	 * @return
	 * @throws RemoteException
	 */
	public void itemUnReserved(int id, int customerID, String key, ReservedItem reserveditem) throws RemoteException {
        ReservableItem item = getReservableItem(id, key);
		Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
       
		//add the reservedItem to abort_items so that if the customer aborts their deletion the 
		//old counts will be replaced
		abort_items.put("" + id, reserveditem);
		
		item.setReserved(item.getReserved()-reserveditem.getCount());
        item.setCount(item.getCount()+reserveditem.getCount());
        return;
	}

    
    // Create a new flight, or add seats to existing flight
    //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
        throws RemoteException
    {
        Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $" + flightPrice + ", " + flightSeats + ") called" );
        Flight curObj;
        if ((curObj = (Flight) readNonCommittedData( id ))==null)
       	{
        	curObj = (Flight) readData( id, Flight.getKey(flightNum));
  		}
        if ( curObj == null ) {
            // doesn't exist...add it
            Flight newObj = new Flight( flightNum, flightSeats, flightPrice );
            Trace.info("RM::addFlight(" + id + ") created new flight " + flightNum + ", seats=" +
                    flightSeats + ", price=$" + flightPrice );
            non_committed_items.put("" + id, newObj);
            return true;
        } else {
        	//TODO add check to make sure not putting object before commit into abort table
        	//Creates a copy of the current object and adds to items that need to be written back on abort
        	Flight tempObj = new Flight (Integer.parseInt(curObj.getLocation()), curObj.getCount(), curObj.getPrice());
        	tempObj.setReserved(curObj.getReserved());
        	abort_items.put("" + id, tempObj);
        	
            //add seats to existing flight and update the price...
            curObj.setCount( curObj.getCount() + flightSeats );
            if ( flightPrice > 0 ) {
                curObj.setPrice( flightPrice );
            } // if
            Trace.info("RM::addFlight(" + id + ") modified existing flight " + flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice );
            non_committed_items.put("" + id, curObj);
            return true;
        } // else
    }
    


    
    public boolean deleteFlight(int id, int flightNum)
        throws RemoteException
    {
        return deleteItem(id, Flight.getKey(flightNum));
    }



    // Create a new room location or add rooms to an existing location
    //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int id, String location, int count, int price)
        throws RemoteException
    {
        Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
        Hotel curObj;
        if ((curObj = (Hotel) readNonCommittedData( id ))==null)
        {
        	curObj = (Hotel) readData( id, Hotel.getKey(location));
        }
        if ( curObj == null ) {
            // doesn't exist...add it
            Hotel newObj = new Hotel( location, count, price );
            non_committed_items.put("" + id, newObj);
            Trace.info("RM::addRooms(" + id + ") created new room location " + location + ", count=" + count + ", price=$" + price );
        } else {
        	//Creates a copy of the current object and adds to items that need to be written back on abort
        	Hotel tempObj = new Hotel (curObj.getLocation(), curObj.getCount(), curObj.getPrice());
        	tempObj.setReserved(curObj.getReserved());
        	abort_items.put("" + id, tempObj);
        	
            // add count to existing object and update price...
            curObj.setCount( curObj.getCount() + count );
            if ( price > 0 ) {
                curObj.setPrice( price );
            } // if
            non_committed_items.put("" + id, curObj);
            Trace.info("RM::addRooms(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
        } // else
        return(true);
    }

    // Delete rooms from a location
    public boolean deleteRooms(int id, String location)
        throws RemoteException
    {
        return deleteItem(id, Hotel.getKey(location));
        
    }

    // Create a new car location or add cars to an existing location
    //  NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int id, String location, int count, int price)
        throws RemoteException
    {
        Trace.info("RM::addCars(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
        Car curObj;
        if ((curObj = (Car) readNonCommittedData( id ))==null)
        {
        	curObj = (Car) readData( id, Car.getKey(location));
        }
        if ( curObj == null ) {
            // car location doesn't exist...add it
            Car newObj = new Car( location, count, price );
            non_committed_items.put("" + id, newObj);
            Trace.info("RM::addCars(" + id + ") created new location " + location + ", count=" + count + ", price=$" + price );
            return true;
        } else {
        	//Creates a copy of the current object and adds to items that need to be written back on abort
        	Car tempObj = new Car (curObj.getLocation(), curObj.getCount(), curObj.getPrice());
        	tempObj.setReserved(curObj.getReserved());
        	abort_items.put("" + id, tempObj);
        	
            // add count to existing car location and update price...
            curObj.setCount( curObj.getCount() + count );
            if ( price > 0 ) {
                curObj.setPrice( price );
            } // if
            non_committed_items.put("" + id, curObj);
            Trace.info("RM::addCars(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
            return true;
        } // else
    }


    // Delete cars from a location
    public boolean deleteCars(int id, String location)
        throws RemoteException
    {
        return deleteItem(id, Car.getKey(location));
    }



    // Deletes customer from the database. 
	public boolean deleteCustomer(int id, int customerID)
	    throws RemoteException
	{
	   /* Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
	    Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
	    if ( cust == null ) {
	        Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
	        return false;
	    } else {            
	        // Increase the reserved numbers of all reservable items which the customer reserved. 
	        RMHashtable reservationHT = cust.getReservations();
	        for (Enumeration e = reservationHT.keys(); e.hasMoreElements();) {        
	            String reservedkey = (String) (e.nextElement());
	            ReservedItem reserveditem = cust.getReservedItem(reservedkey);
	            Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
	            ReservableItem item  = (ReservableItem) readData(id, reserveditem.getKey());
	            Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
	            item.setReserved(item.getReserved()-reserveditem.getCount());
	            item.setCount(item.getCount()+reserveditem.getCount());
	        }
	        
	        // remove the customer from the storage
	        removeData(id, cust.getKey());
	        
	        Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
	        return true;
	    } // if*/
		return false;
	}

	// Returns the number of empty seats on this flight
    public int queryFlight(int id, int flightNum)
        throws RemoteException
    {
        return queryNum(id, Flight.getKey(flightNum));
    }

    // Returns the number of reservations for this flight. 
//    public int queryFlightReservations(int id, int flightNum)
//        throws RemoteException
//    {
//        Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") called" );
//        RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
//        if ( numReservations == null ) {
//            numReservations = new RMInteger(0);
//        } // if
//        Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") returns " + numReservations );
//        return numReservations.getValue();
//    }


    // Returns price of this flight
    public int queryFlightPrice(int id, int flightNum )
        throws RemoteException
    {
        return queryPrice(id, Flight.getKey(flightNum));
    }


    // Returns the number of rooms available at a location
    public int queryRooms(int id, String location)
        throws RemoteException
    {
        return queryNum(id, Hotel.getKey(location));
    }


    
    
    // Returns room price at this location
    public int queryRoomsPrice(int id, String location)
        throws RemoteException
    {
        return queryPrice(id, Hotel.getKey(location));
    }


    // Returns the number of cars available at a location
    public int queryCars(int id, String location)
        throws RemoteException
    {
        return queryNum(id, Car.getKey(location));
    }


    // Returns price of cars at this location
    public int queryCarsPrice(int id, String location)
        throws RemoteException
    {
        return queryPrice(id, Car.getKey(location));
    }

    // Returns data structure containing customer reservation info. Returns null if the
    //  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
    //  reservations.
    public RMHashtable getCustomerReservations(int id, int customerID)
        throws RemoteException
    {
        Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return null;
        } else {
            return cust.getReservations();
        } // if
    }

    // return a bill
    public String queryCustomerInfo(int id, int customerID)
        throws RemoteException
    {
        Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
        } else {
                String s = cust.printBill();
                Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
                System.out.println( s );
                return s;
        } // if
    }

    // customer functions
    // new customer just returns a unique customer identifier
    
    public int newCustomer(int id)
        throws RemoteException
    {
        Trace.info("INFO: RM::newCustomer(" + id + ") called" );
        // Generate a globally unique ID for the new customer
        int cid = Integer.parseInt( String.valueOf(id) +
                                String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                                String.valueOf( Math.round( Math.random() * 100 + 1 )));
        Customer cust = new Customer( cid );
        writeData( id, cust.getKey(), cust );
        Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
        return cid;
    }

    // I opted to pass in customerID instead. This makes testing easier
    public boolean newCustomer(int id, int customerID )
        throws RemoteException
    {
        Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if ( cust == null ) {
            cust = new Customer(customerID);
            writeData( id, cust.getKey(), cust );
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
            return true;
        } else {
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
            return false;
        } // else
    }

    /*
    // Frees flight reservation record. Flight reservation records help us make sure we
    // don't delete a flight if one or more customers are holding reservations
    public boolean freeFlightReservation(int id, int flightNum)
        throws RemoteException
    {
        Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") called" );
        RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
        if ( numReservations != null ) {
            numReservations = new RMInteger( Math.max( 0, numReservations.getValue()-1) );
        } // if
        writeData(id, Flight.getNumReservationsKey(flightNum), numReservations );
        Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") succeeded, this flight now has "
                + numReservations + " reservations" );
        return true;
    }
    */
    
    // Adds car reservation to this customer. 
    public boolean reserveCar(int id, int customerID, String location)
        throws RemoteException
    {
        return reserveItem(id, customerID, Car.getKey(location), location);
    }

    // Adds room reservation to this customer. 
    public boolean reserveRoom(int id, int customerID, String location)
        throws RemoteException
    {
        return reserveItem(id, customerID, Hotel.getKey(location), location);
    }
    // Adds flight reservation to this customer.  
    public boolean reserveFlight(int id, int customerID, int flightNum)
        throws RemoteException
    {
        return reserveItem(id, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
    }
    
    // Reserve an itinerary 
    @SuppressWarnings("rawtypes")
	public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean Car,boolean Room)
        throws RemoteException
    {
        return false;
    }

	@Override
	public int newCustomerExecute(int op_id, int cid) throws RemoteException {
		return 0;
	}

	/**
	 * This method simulates a crash in this RM
	 */
	public void crash(String which) throws RemoteException 
	{
		if (which.equals(rm_name))
		{
			try {
				//unregister this RM from the registry
				Naming.unbind("//localhost:" + port + "/" + registry_name);
				
				//Unexport; this will also remove this RM from the RMI runtime.
				UnicastRemoteObject.unexportObject(this, true);
				
				Trace.info("Simulating " + rm_name + " resource manager crash...");
				System.exit(0);
				
			} catch (NotBoundException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * This method cleanly shuts down this RM
	 */
	public void shutdown() throws RemoteException 
	{
		flushToDisk();

		try {
			//unregister this RM from the registry
			Naming.unbind("//localhost:" + port + "/" + registry_name);
			
			//Unexport; this will also remove this RM from the RMI runtime.
			UnicastRemoteObject.unexportObject(this, true);
			
			Trace.info("Shutting down " + rm_name + " Resource Manager.");
						
			
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	// check if operation has any temporary data, if it does vote yes otherwise vote no
	@Override
	public Vote vote(int operationID, OP_CODE code) throws RemoteException 
	{
		boolean voteYes = (non_committed_items.get("" + operationID) != null) || (abort_items.get("" + operationID) != null);
		
		boolean queryMethod = code.equals(OP_CODE.QUERY_CAR_PRICE) || code.equals(OP_CODE.QUERY_CARS) 
				|| code.equals(OP_CODE.QUERY_CUSTOMER_INFO) || code.equals(OP_CODE.QUERY_FLIGHT_PRICE) 
				|| code.equals(OP_CODE.QUERY_FLIGHTS)  || code.equals(OP_CODE.QUERY_ROOM_PRICE) 
				|| code.equals(OP_CODE.QUERY_ROOMS);
		
		if(!voteYes && queryMethod)
			voteYes = true;
		
		if(failType == CrashType.AFTER_REQUEST_BEFORE_VOTE_RETURN)
		{
			crash(serverToCrash);
			//TODO have to shutdown following crash so that no votes are returned
		}
		
		Vote vote = ((voteYes) ? Vote.YES : Vote.NO);
		
		Trace.info(rm_name + " RM voted " + vote + " on Operation " + operationID);
		
		flushToDisk();
		
		return vote;
		
	}

	@Override
	public String getName() throws RemoteException {
		return ResourceManagerImpl.rm_name;
	}


	@Override
	public void setCrashFlags(String which, CrashType type) throws RemoteException 
	{
		ResourceManagerImpl.serverToCrash = which;
		ResourceManagerImpl.failType = type;
	}
}


