package MWS;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import ResInterface.ResourceManager;

public class MiddlewareImpl implements ResourceManager {
	
    //this references the RM server for cars
    static ResourceManager cars_rm = null;
    
    //this references the RM server for flights
    static ResourceManager flights_rm = null;
    
    //this references the RM server for rooms
    static ResourceManager rooms_rm = null;


	public static void main(String[] args)
	{
		// Figure out where server is running
        String server = "teaching";
       // int port = 1099;
        /**
         * Creating our own RMI registry, global one isn't working
         */
        int port = 8807;
        
        /**
         * RM SERVERS (hard-coded for now - soon this will be command line options
         */
        String cars_server = "lab2-10.cs.mcgill.ca";        
        String flights_server = "lab2-11.cs.mcgill.ca";
        String rooms_server = "lab2-12.cs.mcgill.ca";
        
        int rm_port = 7707;


        if (args.length == 1) {
            server = server + ":" + args[0];
            port = Integer.parseInt(args[0]);
        } else if (args.length != 0 &&  args.length != 1) {
            System.err.println ("Wrong usage");
            System.out.println("Usage: java ResImpl.ResourceManagerImpl [port]");
            System.exit(1);
        }
        

        try {
        	/**
        	 * CONNECT TO RMIREGISTRY AS SERVER TO BE CONNECTED TO FROM CLIENT
        	 */
            // create a new Server object
            MiddlewareImpl obj = new MiddlewareImpl();
            // dynamically generate the stub (client proxy)
            ResourceManager mw = (ResourceManager) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            //Registry registryMiddle = LocateRegistry.getRegistry(port);
            Registry registryMiddle = LocateRegistry.createRegistry(port);
            registryMiddle.rebind("group_7_middle", mw);

            System.err.println("Server ready");
            
            /**
             * CONNECT TO RM SERVERS AS A CLIENT
             * 
             * note: registry might overwrite something - may need to have two registry objects?
             */
            
            // get the proxy and the remote reference by rmiregistry lookup for the cars server
            Registry registry_cars = LocateRegistry.getRegistry(cars_server, rm_port);
            cars_rm = (ResourceManager) registry_cars.lookup("group_7_RM");
            
            // get the proxy and the remote reference by rmiregistry lookup for the flights server
            Registry registry_flights = LocateRegistry.getRegistry(flights_server, rm_port);
            flights_rm = (ResourceManager) registry_flights.lookup("group_7_RM");
            
            // get the proxy and the remote reference by rmiregistry lookup for the rooms server
            Registry registry_rooms = LocateRegistry.getRegistry(rooms_server, rm_port);
            rooms_rm = (ResourceManager) registry_rooms.lookup("group_7_RM");
            
            if(cars_rm!=null && flights_rm!=null && rooms_rm!=null)
            {
                System.out.println("Successful");
                System.out.println("Connected to RMs");
            }
            else
            {
                System.out.println("Unsuccessful");
            }
            // make call on remote method
            
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
	}
	
	public MiddlewareImpl() throws RemoteException {
    }
	
	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats,
			int flightPrice) throws RemoteException {
			
		return flights_rm.addFlight(id, flightNum, flightSeats, flightPrice);
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price)
			throws RemoteException {
		
		return cars_rm.addCars(id, location, numCars, price);
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price)
			throws RemoteException {

		return rooms_rm.addRooms(id, location, numRooms, price);
	}

	@Override
	public int newCustomer(int id) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean newCustomer(int id, int cid) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteFlight(int id, int flightNum) throws RemoteException {

		return flights_rm.deleteFlight(id, flightNum);
	}

	@Override
	public boolean deleteCars(int id, String location) throws RemoteException {

		return cars_rm.deleteCars(id, location);
	}

	@Override
	public boolean deleteRooms(int id, String location) throws RemoteException {

		return rooms_rm.deleteRooms(id, location);
	}

	@Override
	public boolean deleteCustomer(int id, int customer) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int queryFlight(int id, int flightNumber) throws RemoteException {

		return flights_rm.queryFlight(id, flightNumber);
	}

	@Override
	public int queryCars(int id, String location) throws RemoteException {

		return cars_rm.queryCars(id, location);
	}

	@Override
	public int queryRooms(int id, String location) throws RemoteException {

		return rooms_rm.queryRooms(id, location);
	}

	@Override
	public String queryCustomerInfo(int id, int customer)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber)
			throws RemoteException {

		return flights_rm.queryFlightPrice(id, flightNumber);
	}

	@Override
	public int queryCarsPrice(int id, String location) throws RemoteException {

		return cars_rm.queryCarsPrice(id, location);
	}

	@Override
	public int queryRoomsPrice(int id, String location) throws RemoteException {

		return rooms_rm.queryRoomsPrice(id, location);
	}

	@Override
	public boolean reserveFlight(int id, int customer, int flightNumber)
			throws RemoteException {

		return flights_rm.reserveFlight(id, customer, flightNumber);
	}

	@Override
	public boolean reserveCar(int id, int customer, String location)
			throws RemoteException {

		return cars_rm.reserveCar(id, customer, location);
	}

	@Override
	public boolean reserveRoom(int id, int customer, String locationd)
			throws RemoteException {

		return rooms_rm.reserveRoom(id, customer, locationd);
	}

	@Override
	public boolean itinerary(int id, int customer, Vector flightNumbers,
			String location, boolean Car, boolean Room) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}


}
