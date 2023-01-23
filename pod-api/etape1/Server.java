import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server extends UnicastRemoteObject implements Server_itf{

    private List<SharedObject_itf> serverObjects;
    private static int objectCounter;

    private Map<String, Integer> registry;

    public Server() throws RemoteException {
        this.serverObjects = new ArrayList<SharedObject_itf>();
        objectCounter = 0;
        this.registry = new HashMap<String, Integer>();
    }

    @Override
    public int lookup(String name) throws RemoteException {
        try {
            return registry.get(name);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public void register(String name, int id) throws RemoteException {
        this.registry.put(name, id);
    }

    @Override
    public int create(Object o) throws RemoteException {
        int objectID = objectCounter;
        
        this.serverObjects.add(new ServerObject(o, objectID));
        System.out.println(this.serverObjects);
        objectCounter++;
        return objectID;
    }

    @Override
    public Object lock_read(int id, Client_itf client) throws RemoteException {
        ServerObject requestedServerObject = (ServerObject) this.serverObjects.get(id);

        requestedServerObject.lock_read();
        requestedServerObject.addCallback(client);

        return requestedServerObject.obj; // objet réel
    }

    @Override
    public Object lock_write(int id, Client_itf client) throws RemoteException {
        System.out.println("Received a lock write from client " + client);
        serverObjects.forEach(obj -> {
            System.out.println(obj);
        });
        ServerObject requestedServerObject = (ServerObject) this.serverObjects.get(id);
        synchronized(requestedServerObject) {
            requestedServerObject.lock_write();
            System.out.println("Locked object #" + id + " for writing");
            requestedServerObject.addCallback(client);
        }
        return requestedServerObject.obj; // objet réel

    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            Registry reg = LocateRegistry.createRegistry(4269);
            Naming.rebind("//localhost:4269/server", server);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
