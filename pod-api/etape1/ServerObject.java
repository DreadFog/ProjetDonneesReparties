import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ServerObject implements SharedObject_itf, Serializable {

    public EtatLock lock; // NL, RL, WL only
    public Object obj; // reference to the object
    private List<Client_itf> callbackClients;
    private int id; // unique identifier to callback clients

    private boolean invalidateWriterSent = false;

    public ServerObject(Object o, int id) {
        this.lock = EtatLock.NL; // pas de lock à la création de l'objet
        this.obj = o;
        this.callbackClients = new ArrayList<Client_itf>();
        this.id = id;
    }

    public void addCallback(Client_itf c) {
        this.callbackClients.add(c);
    }

    @Override
    public void lock_read() {
        switch (this.lock) {

            case NL:
                this.lock = EtatLock.RL;
                break;

            case RL:
                // The lock stays the same (multiple readers allowed at the same time)
                break;

            case WL:
                // Only one writer -> first element of the list
                try {
                    this.obj = this.callbackClients.get(0).reduce_lock(this.id);
                    this.lock = EtatLock.RL;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                throw new RuntimeException("Etat illégal du lock du ServerObject");
        }
    }

    @Override
    public void lock_write() {

        switch (this.lock) {
            case NL:
                this.lock = EtatLock.WL;
                break;

            case RL:

                // Invalidate Reader Locks on all readers
                this.callbackClients.forEach(callback -> {
                    try {
                        callback.invalidate_reader(this.id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                // Remove all reader callbacks
                this.callbackClients.removeAll(callbackClients);

                // Set lock to Write Lock
                this.lock = EtatLock.WL;
                break;

            case WL:

                try {
                    while (invalidateWriterSent) {
                        // System.out.println("waiting here");
                    }
                    // Invalidate the lock of the existing writer
                    // and remove it from the list of callbacks
                    this.invalidateWriterSent = true;
                    this.obj = this.callbackClients.get(0).invalidate_writer(this.id);
                    System.out.println("Waiting There");
                    this.invalidateWriterSent = false;
                    this.callbackClients.removeAll(callbackClients);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            default:
                break;
        }
    }

    @Override
    public void unlock() {
        // Method is never called, as clients locally cache the lock when they don't use
        // the object.
        // Hence, the server has no knowledge of the object being unlocked.
        System.out.println("[W] : Tried to unlock a ServerObject. Ignored");
    }

}