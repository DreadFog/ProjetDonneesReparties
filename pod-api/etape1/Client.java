import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class Client extends UnicastRemoteObject implements Client_itf {

    private static Server_itf boundServer;
    private static Map<Integer, SharedObject_itf> sharedObjects;
    private static Client client;

    private Client() throws RemoteException {
        super();
    }

    ///////////////////////////////////////////////////
    // Interface to be used by applications
    ///////////////////////////////////////////////////

    // initialization of the client layer
    public static void init() {
        try {
            client = new Client();
            boundServer = (Server_itf) Naming.lookup("//localhost:4269/server");
            sharedObjects = new HashMap<Integer, SharedObject_itf>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SharedObject_itf getSharedObjectById(int id){
        return sharedObjects.get(id);
    }

    // lookup in the name server
    public static SharedObject lookup(String name) {
        try {
            int id = boundServer.lookup(name);

            if (id == -1) {
                return null;
            }

            SharedObject sharedObject = new SharedObject(id);
            // a new shared object is added to the list of shared objects.
            // its obj field is null as there has not been any lock yet
            if (!sharedObjects.containsKey(id)) {
                //System.out.println("Added object #" + id + " to client map");
                sharedObjects.put(id, sharedObject);
            }

            return sharedObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // binding in the name server
    public static void register(String name, SharedObject_itf so) {
        try {
            // casting the shared object itf to a shared object to get the id
            SharedObject objectToRegister = (SharedObject) so;
            boundServer.register(name, objectToRegister.id);
        } catch (Exception e) {
            // could not get the id of the shared object
            System.out.println("[E] : Could not register the name " + name + " on the server");
            e.printStackTrace();
        }
    }

    // creation of a shared object
    public static SharedObject create(Object o) {
        try {
            // get the server side id of the shared object
            int id = boundServer.create(o);
            SharedObject sharedObject = new SharedObject(id);
            sharedObjects.put(id, sharedObject);
            //System.out.println("Added object #" + id + " to client map");
            return sharedObject;
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("[E] : Could not create the object on the server side");
            return null;
        }
    }

    /////////////////////////////////////////////////////////////
    // Interface to be used by the consistency protocol
    ////////////////////////////////////////////////////////////

    // request a read lock from the server
    public static Object lock_read(int id) {

        try {
            return boundServer.lock_read(id, client);
        } catch (Exception e) {
            throw new RuntimeException("Network Error");
        }

    }

    // request a write lock from the server
    public static Object lock_write(int id) {
        try {
            return boundServer.lock_write(id, client);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Network Error");
        }
    }

    // receive a lock reduction request from the server
    public Object reduce_lock(int id) throws java.rmi.RemoteException {
        SharedObject chosenObject = (SharedObject) sharedObjects.get(id);
        // forwards the request to the object and returns the answer
        return chosenObject.reduce_lock();
    }

    // receive a reader invalidation request from the server
    public void invalidate_reader(int id) throws java.rmi.RemoteException {
        SharedObject chosenObject = (SharedObject) sharedObjects.get(id);
        chosenObject.invalidate_reader();
    }

    // receive a writer invalidation request from the server
    public Object invalidate_writer(int id) throws java.rmi.RemoteException {
        SharedObject chosenObject = (SharedObject) sharedObjects.get(id);
        // forwards the request to the object and returns the answer
        //System.out.println("invalidated");
        return chosenObject.invalidate_writer();
    }
}
