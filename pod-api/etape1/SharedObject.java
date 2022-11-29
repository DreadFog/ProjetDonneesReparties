import java.io.*;

public class SharedObject implements Serializable, SharedObject_itf {
	
	private int id;
	private EtatLock eL;

	public SharedObject (int id) {
		this.id = id;
	}

	// invoked by the user program on the client node
	public void lock_read() {
	}

	// invoked by the user program on the client node
	public void lock_write() {
	}

	// invoked by the user program on the client node
	public synchronized void unlock() {
	}


	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
	}

	public synchronized Object invalidate_writer() {
	}
}
