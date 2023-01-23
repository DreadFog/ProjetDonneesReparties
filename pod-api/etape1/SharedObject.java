import java.io.*;

public class SharedObject implements Serializable, SharedObject_itf {

	public final int id;
	private EtatLock lock;
	// [Etape 3] transient : Permet d'éviter que la sérialisation soit récursive
	public transient Object obj; // référence à l'objet

	public SharedObject(int id) {
		this.id = id;
		this.lock = EtatLock.NL; // à vérifier
	}

	// invoked by the user program on the client node
	public void lock_read() {
		synchronized(this){
			switch (this.lock) {
				case NL:
					// Only call client when the read lock is not cached
					this.obj = Client.lock_read(id);
					this.lock = EtatLock.RLT;
					break;

				case RLC:
					this.lock = EtatLock.RLT;
					break;

				case WLC:
					this.lock = EtatLock.RLT_WLC;
					break;

				default:
					System.out.println("[W] : Mauvaise utilisation de SharedObject.lock_read() par l'application");
					break;
			}
		}
	}

	// invoked by the user program on the client node
	public void lock_write() {
		synchronized(this){
			switch (this.lock) {
				case NL:
					// Only call client when the write lock is not cached
					this.obj = Client.lock_write(id);
					this.lock = EtatLock.WLT;
					break;

				case RLC:
					// Only call client when the write lock is not cached
					this.obj = Client.lock_write(id);
					this.lock = EtatLock.WLT;
					break;

				case WLC:
					this.lock = EtatLock.WLT;
					break;

				default:
					System.out.println("[W] : Mauvaise utilisation de SharedObject.lock_write() par l'application");
					break;
			}
		}
	}

	// invoked by the user program on the client node
	public synchronized void unlock() {
        switch (this.lock) {
			case RLT:
				this.lock = EtatLock.RLC;
				break;

			case RLT_WLC:
			case WLT:
				this.lock = EtatLock.WLC;
				break;

			default:
				System.out.println("[W] : Mauvaise utilisation de SharedObject.unlock() par l'application. Etat actuel : " + this.lock);
				break;
		}
		this.notify();
	}

	// callback invoked remotely by the server
	// Used by the server to reduce lock from Write to Read
	public synchronized Object reduce_lock() {

        //System.out.println("Reduce Lock on object " + this.id);

		// In case the application is only reading the object while the write lock is
		// cached, remove the write lock
		if (this.lock == EtatLock.RLT_WLC) {
			this.lock = EtatLock.RLT;
			return this.obj;
		}

		// Wait until the application has finished writing to the object
		while (this.lock != EtatLock.WLC) {
		}

		this.lock = EtatLock.RLC;
		return this.obj;
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {        
		// Wait until the application has finished reading the object
		while (this.lock != EtatLock.RLC) {
		}

		this.lock = EtatLock.NL;
	}

	public synchronized Object invalidate_writer() {
		try {
			while (this.lock == EtatLock.WLT){
				this.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.lock = EtatLock.NL;
		return this.obj;
	}

    public Object readResolve() throws ObjectStreamException {
		// [Etape 3] Lors d'une désérialisation, on renvoie l'objet courant auquel on donne le bon état du verrou

		SharedObject_itf localSharedObject = Client.getSharedObjectById(this.id);

		if (localSharedObject == null) {
			this.lock = EtatLock.NL;
		} else {
			this.lock = ((SharedObject)localSharedObject).lock;
		}

		return this;
    }
}
