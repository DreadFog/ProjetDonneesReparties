import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

public class SharedObject implements Serializable, SharedObject_itf {

	public final int id;
	private EtatLock lock;
	public Object obj; // référence à l'objet
	private ReentrantLock mutex;

	public SharedObject(int id) {
		this.id = id;
		this.lock = EtatLock.NL; // à vérifier
		this.mutex = new ReentrantLock();
	}

	// invoked by the user program on the client node
	public void lock_read() {

        System.out.println("Lock Read on object " + this.id);

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

	// invoked by the user program on the client node
	public void lock_write() {

        System.out.println("Lock Write on object " + this.id);
		// this.mutex.lock();
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
		// this.mutex.unlock();
	}

	// invoked by the user program on the client node
	public synchronized void unlock() {
		
        System.out.println("Unlock on object " + this.id);

        switch (this.lock) {
			case RLT:
				this.lock = EtatLock.RLC;
				break;

			case RLT_WLC:
			case WLT:
				this.lock = EtatLock.WLC;
				break;

			default:
				System.out.println("[W] : Mauvaise utilisation de SharedObject.unlock() par l'application");
				break;
		}
		this.notify();
	}

	// callback invoked remotely by the server
	// Used by the server to reduce lock from Write to Read
	public synchronized Object reduce_lock() {

        System.out.println("Reduce Lock on object " + this.id);

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

        System.out.println("Invalidate Reader on object " + this.id);
        
		// Wait until the application has finished reading the object
		while (this.lock != EtatLock.RLC) {
		}

		this.lock = EtatLock.NL;
	}

	public synchronized Object invalidate_writer() {
		// this.mutex.lock();
        System.out.println("Invalidate Writer on object " + this.id);

		// Wait until the application has finished writing to the object
		/* while (this.lock != EtatLock.WLC) {
			System.out.println("waiting in lock " + this.lock);
		} */
		try {
			this.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.lock = EtatLock.NL;
		// this.mutex.unlock();
		return this.obj;
	}
}
