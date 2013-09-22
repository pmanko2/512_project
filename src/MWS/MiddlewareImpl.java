package MWS;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import ResInterface.ResourceManager;

public class MiddlewareImpl implements ResourceManager {
	
    //this references an RM Object (NOT Middleware)
    static ResourceManager rm = null;


	public static void main(String[] args)
	{
		// Figure out where server is running
        String server = "teaching";
       // int port = 1099;
        /**
         * Creating our own RMI registry, global one isn't working
         */
        int port = 8807;
        
        String rmserver = "lab2-10";
        int rmport = 7707;

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
            
            // get the proxy and the remote reference by rmiregistry lookup
            Registry registryRM = LocateRegistry.getRegistry(rmserver, rmport);
            rm = (ResourceManager) registryRM.lookup("group_7_RM");
            if(rm!=null)
            {
                System.out.println("Successful");
                System.out.println("Connected to RM(s)");
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCars(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteRooms(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCustomer(int id, int customer) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int queryFlight(int id, int flightNumber) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryCars(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryRooms(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryCarsPrice(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryRoomsPrice(int id, String location) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean reserveFlight(int id, int customer, int flightNumber)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reserveCar(int id, int customer, String location)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reserveRoom(int id, int customer, String locationd)
			throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean itinerary(int id, int customer, Vector flightNumbers,
			String location, boolean Car, boolean Room) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}


}
