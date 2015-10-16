package czsem.gate.utils;

import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.LanguageResource;
import gate.Resource;
import gate.corpora.DocumentImpl;
import gate.creole.ResourceInstantiationException;
import gate.event.CreoleListener;
import gate.event.DatastoreListener;
import gate.persist.PersistenceException;
import gate.persist.SerialDataStore;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class DataStoreWrapper {
	
	protected SerialDataStore ds;
	protected URL dataStoreDir;


	public DataStoreWrapper(String dataStoreDir) {
		this(new File(dataStoreDir));
	}

	public DataStoreWrapper(File dataStoreDir) {
		this(file2url(dataStoreDir));
	}

	public static URL file2url(File file) {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public DataStoreWrapper(URL dataStoreDir) {
		this.dataStoreDir = dataStoreDir;
	}

	public void openExisting() throws PersistenceException {
		ds = (SerialDataStore) Factory.openDataStore(
				SerialDataStore.class.getCanonicalName(), 
				dataStoreDir.toString());
	}

	public void createNew() throws PersistenceException {
		ds = (SerialDataStore) Factory.createDataStore(
				SerialDataStore.class.getCanonicalName(), 
				dataStoreDir.toString());
	}
	
	public synchronized void openOrCreate() throws MalformedURLException, PersistenceException {
		try {
			openExisting();
		} catch (PersistenceException e) {
			createNew();
		}
	}
	
	
	private static final String LR_PERSISTENCE_ID_SUFFIX = "___" + "0" + "___" + ".ser";
	
	public static String lrNameToLRPersistenceId(String lrName) {
		//return lrName + "___" + new Date().getTime() + "___" + random();
		
		return lrName + LR_PERSISTENCE_ID_SUFFIX;
	}

	public static String lRPersistenceIdToLrName(String lRPersistenceId) {
		if (lRPersistenceId.endsWith(LR_PERSISTENCE_ID_SUFFIX))
			return lRPersistenceId.substring(0, lRPersistenceId.length() - LR_PERSISTENCE_ID_SUFFIX.length());
		
		return null;
	}

	public synchronized void persistDoc(Document gateDoc, String lrName) throws PersistenceException, SecurityException {
		persistDocWithId(gateDoc, lrNameToLRPersistenceId(lrName));
	}
	
	public void persistDocWithId(Document gateDoc, String persistenceId) throws PersistenceException, SecurityException {
		gateDoc.setLRPersistenceId(persistenceId);
		
		//This is important to avoid memory leaks
		Gate.getCreoleRegister().removeCreoleListener((CreoleListener) gateDoc);
		
		ds.adopt(gateDoc);
		ds.sync(gateDoc);
		
		if (gateDoc instanceof DatastoreListener)
			ds.removeDatastoreListener((DatastoreListener) gateDoc);
	}

	public synchronized void persistDoc(Document gateDoc) throws PersistenceException, SecurityException {
		persistDoc(gateDoc, gateDoc.getName());
	}
	
	public Resource loadResource(Class<? extends LanguageResource> cls, String lrId) throws ResourceInstantiationException {
		Resource ret = GateUtils.loadResourceFormDatastore(ds, cls.getCanonicalName(), lrId);
		
		if (ret instanceof DatastoreListener)
			ds.removeDatastoreListener((DatastoreListener) ret);
		
		return ret;

	}

	public Document loadDoc(String docLrId) throws ResourceInstantiationException {
		return (Document) loadResource(DocumentImpl.class, docLrId);
	}

	public Document loadAndForgetDoc(String docLrId) throws ResourceInstantiationException, PersistenceException {
		Document ret = loadDoc(docLrId);
		ret.setDataStore(null);
		return ret;
	}

	public boolean containsGateDocument(String fileName) {
	    return containsResource(DocumentImpl.class, fileName);
	}

	public boolean containsResource(Class<? extends LanguageResource> c, String fileName) {
		return containsLrId(c, lrNameToLRPersistenceId(fileName)); 
	}

	public boolean containsDocumentLrId(String lrId) {
		return containsLrId(DocumentImpl.class, lrId);
	}
	
	public boolean containsLrId(Class<? extends LanguageResource> c, String lrId) { 
	    File resourceTypeDir = new File(ds.getStorageDir(), c.getCanonicalName());
	    
	    if(! resourceTypeDir.exists()) return false;
	    
	    File resourceFile = new File(resourceTypeDir, lrId); 

	    return resourceFile.exists();
	}


	public void close() throws PersistenceException {
		if (ds == null) return;
		
		ds.close();
	}
	
	public class ResourceIterator <T extends Resource> implements Iterator<T> {

		private Iterator<String> lrIdsIterator;
		private Class<? extends LanguageResource> cls;

		public ResourceIterator(Class<? extends LanguageResource> cls) throws PersistenceException {
			this.cls = cls;
			lrIdsIterator = getLrIds(cls).iterator();
		}

		public ResourceIterator(Class<? extends LanguageResource> cls, double startProportion) throws PersistenceException {
			this.cls = cls;
			lrIdsIterator = getLrIds(cls, startProportion).iterator();
		}

		@Override
		public boolean hasNext() {
			return lrIdsIterator.hasNext();
		}

		@SuppressWarnings("unchecked")
		@Override
		public T next() {
			try {
				return (T) loadResource(cls, lrIdsIterator.next());
			} catch (ResourceInstantiationException e) {
				throw new NoSuchElementException(e.toString());
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("not implemented");
		}
		
	}
	
	public List<String> getDocumentLrIds() throws PersistenceException {
		return getLrIds(DocumentImpl.class);
	}

	public List<String> getDocumentLrIds(double startProportion) throws PersistenceException {
		return getLrIds(DocumentImpl.class, startProportion);
	}

	public List<String> getLrIds(Class<? extends LanguageResource> cls) throws PersistenceException {
		
		List<String> ret = ds.getLrIds(cls.getCanonicalName());
		
		return ret;
	}

	public List<String> getLrIds(Class<? extends LanguageResource> cls, double startProportion) throws PersistenceException {
		
		List<String> ret = ds.getLrIds(cls.getCanonicalName());
		
		int size = ret.size();
		int startIndex = (int) (size * startProportion);
		
		return ret.subList(startIndex, size);
	}
	

	public Iterable<? extends Document> iterateAllDocuments() {
		return new Iterable<DocumentImpl>() {
			
			@Override
			public Iterator<DocumentImpl> iterator() {
				try {
					return new ResourceIterator<DocumentImpl>(DocumentImpl.class);
				} catch (PersistenceException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	public Iterable<? extends Document> iterateAllDocuments(final double startAtStoreProportion) {
		return new Iterable<DocumentImpl>() {
			
			@Override
			public Iterator<DocumentImpl> iterator() {
				try {
					return new ResourceIterator<DocumentImpl>(DocumentImpl.class, startAtStoreProportion);
				} catch (PersistenceException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

}
