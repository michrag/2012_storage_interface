package storageInterface;

/**
 * Interfaccia che definisce il tipo di risorsa su cui opera {@link StorageInterfaceServlet}.
 * Ciascuna risorsa e' univocamente determinata da un URI e puo' avere un contenuto di tipo {@link String}.
 *
 *
 */
public interface IResource
{
    /**
     * Restituisce l'URI della risorsa
     * @return l'URI della risorsa
     */
    public abstract String getURI();

    /**
     * Imposta {@code content} come contenuto della risorsa.
     * @param content il contenuto che si vuole assegnare alla risorsa
     */
    public abstract void setContent(String content);

    /**
     * Restituisce il contenuto della risorsa.
     * @return il contenuto della risorsa
     */
    public abstract String getContent();

    /**
     * Restituisce l'ETag della risorsa
     * @return l'ETag della risorsa
     */
    public abstract String getETag();
}