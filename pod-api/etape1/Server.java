import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Server extends UnicastRemoteObject implements Server_itf{

    private List<SharedObject_itf> serverObjects;
    private static int objectCounter;

    public Server() throws RemoteException {
        this.serverObjects = new ArrayList<SharedObject_itf>();
        objectCounter = 0;
    }

    @Override
    public int lookup(String name) throws RemoteException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void register(String name, int id) throws RemoteException {
        // TODO Auto-generated method stub

    }

    @Override
    public int create(Object o) throws RemoteException {
        objectCounter++;
        this.serverObjects.set(objectCounter, new ServerObject(o, objectCounter));
        return objectCounter;
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

        ServerObject requestedServerObject = (ServerObject) this.serverObjects.get(id);

        requestedServerObject.lock_write();
        requestedServerObject.addCallback(client);

        return requestedServerObject.obj; // objet réel

    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            Registry reg = LocateRegistry.createRegistry(4269);
            Naming.rebind("//localhost:4269/server", server);
        } catch (RemoteException | MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
