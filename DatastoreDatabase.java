package storageInterface;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

/**
 * Implementazione di {@link IDatabase} basata sul
 * <a href="https://developers.google.com/appengine/docs/java/datastore/">DataStore</a>
 * di Google App Engine
 *
 *
 */

//https://developers.google.com/appengine/docs/java/gettingstarted/usingdatastore
//The development web server uses a local version of the Datastore for testing your application, using local files.
//The data persists as long as the temporary files exist, and the web server does not reset these files unless you ask it to do so.
//The file is named local_db.bin, and it is created in your application's WAR directory,
//in the WEB-INF/appengine-generated/ directory. To clear the Datastore, delete this file.

public class DatastoreDatabase implements IDatabase
{
    private String entityKind;
    // il kind delle entita' e' fissato ed uguale per tutte le entita', in modo tale che la chiave sia
    // ottenibile dal solo identificatore (key name)

    private String contentPropertyName;

    /**
     * Istanzia un nuovo oggetto di tipo {@link DatastoreDatabase}.
     */
    public DatastoreDatabase()
    {
        entityKind = "SInode";
        contentPropertyName = "content";
    }


    /* (non-Javadoc)
     * @see storageInterface.IStorageInterfaceDatabase#get(java.lang.String)
     */
    @Override
    public IResource get(String siNodeURI)
    {
        Key entityKey = getEntityKey(siNodeURI);

        Entity siNodeEntity = getEntity(entityKey);

        if(siNodeEntity != null)
        {
            String siNodeEntityURI = entityKey.getName();

            String siNodeContent = ((Text) siNodeEntity.getProperty(contentPropertyName)).getValue();

            return new SINode(siNodeEntityURI, siNodeContent);
        }
        else
        {
            return null;
        }
    }


    /* (non-Javadoc)
     * @see storageInterface.IStorageInterfaceDatabase#delete(storageInterface.SINode)
     */
    @Override
    public boolean delete(IResource siNode)
    {
        String resourceName = siNode.getURI();

        return delete(resourceName);
    }


    /* (non-Javadoc)
     * @see storageInterface.IStorageInterfaceDatabase#delete(java.lang.String)
     */
    @Override
    public boolean delete(String siNodeURI)
    {
        Key entityKey = getEntityKey(siNodeURI);

        Entity siNodeEntity = getEntity(entityKey);

        if(siNodeEntity != null)
        {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.delete(entityKey);
            return true;
        }
        else
        {
            return false;
        }
    }


    /* (non-Javadoc)
     * @see storageInterface.IStorageInterfaceDatabase#put(storageInterface.SINode)
     */
    @Override
    public void put(IResource siNode)
    {
        String resourceName = siNode.getURI();
        String content = siNode.getContent();

        Key entityKey = getEntityKey(resourceName);

        Entity siNodeEntity = new Entity(entityKey);

        // setProperty accetta stringhe lunghe <= 500 caratteri
        // percio' usiamo un oggetto Text, che incapsula una stringa di arbitraria lunghezza
        // il limite per Text e' 1 MB
        Text contentText = new Text(content);
        siNodeEntity.setProperty(contentPropertyName, contentText);

        // NOTA: NON salvo l'etag nella entity - e' inutile

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        // https://developers.google.com/appengine/docs/java/datastore/entities
        // Note: The Datastore API does not distinguish between creating a new entity and updating an existing one.
        // If the object's key represents an entity that already exists, the put() method overwrites the existing entity.
        datastore.put(siNodeEntity);

    }


    private Key getEntityKey(String entityName)
    {
        Key entityKey = KeyFactory.createKey(entityKind, entityName);

        return entityKey;
    }


    private Entity getEntity(Key entityKey)
    {
        Entity siNodeEntity;

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try
        {
            siNodeEntity = datastore.get(entityKey);
        }
        catch(EntityNotFoundException e)
        {
            return null;
        }

        return siNodeEntity;
    }

}
