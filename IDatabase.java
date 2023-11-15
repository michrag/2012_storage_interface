package storageInterface;

/**
 * Interfaccia che definisce un database utilizzabile da {@link StorageInterfaceServlet}.
 * Tale database opera su oggetti di tipo {@link IResource}.
 *
 *
 */
public interface IDatabase
{
    /**
     * Crea (se e solo se {@code resource} non e' presente nel database) o aggiorna
     * (se e solo se {@code resource} e' gia' presente nel database) l'{@link IResource} {@code resource}.
     *
     *
     * @param resource {@link IResource} da creare o aggiornare
     */
    public abstract void put(IResource resource);

    /**
     * Se esiste, restituisce l'{@link IResource} univocamente determinato da {@code resourceURI}; altrimenti restituisce {@code null}.
     * @param resourceURI {@link String} che identifica un {@link IResource}
     * @return l'{@link IResource} univocamente determinato da {@code resourceURI}, se esiste; {@code null} altrimenti
     */
    public abstract IResource get(String resourceURI);

    /**
     * Elimina l'{@link IResource} {@code resource} dal database.
     * @param resource {@link IResource} da eliminare
     * @return {@code true} se e solo se {@code resource} e' stato effettivamente eliminato, {@code false} altrimenti
     * (e.g.: {@code resource} non era presente nel database)
     */
    public abstract boolean delete(IResource resource);

    /**
     * Elimina dal database l'{@link IResource} univocamente determinato da {@code resourceURI}.
     * @param resourceURI {@link String} che identifica l'{@link IResource} da eliminare
     * @return {@code true} se e solo se la risorsa e' stata effettivamente eliminata, {@code false} altrimenti
     * (e.g.: {@code resourceURI} non identificava un {@link IResource} nel database)
     */
    public abstract boolean delete(String resourceURI);

}