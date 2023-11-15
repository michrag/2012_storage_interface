package storageInterface;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

/**
 * Implementazione di default di {@link IResource}.
 *
 *
 */
public class SINode implements IResource
{
    private String uri;

    private String content;

    /**
     * Istanzia un nuovo oggetto di tipo {@link SINode}, assegnandogli l'URI {@code siNodeURI}.
     * @param siNodeURI URI che si vuole assegnare al {@link SINode}
     */
    public SINode(String siNodeURI)
    {
        this.uri = siNodeURI;
    }

    /**
     * Istanzia un nuovo oggetto di tipo {@link SINode}, assegnandogli l'URI {@code siNodeURI} e il contenuto {@code content}.
     * @param siNodeURI URI che si vuole assegnare al {@link SINode}
     * @param content contenuto che si vuole assegnare al {@link SINode}
     */
    public SINode(String siNodeURI, String content)
    {
        this(siNodeURI);
        setContent(content);
    }

    @Override
    /*
     * (non-Javadoc)
     * @see storageInterface.ISINode#setContent(java.lang.String)
     */
    public void setContent(String content)
    {
        this.content = content;
    }

    /* (non-Javadoc)
     * @see storageInterface.ISINode#getURI()
     */
    @Override
    public String getURI()
    {
        return uri;
    }

    /* (non-Javadoc)
     * @see storageInterface.ISINode#getContent()
     */
    @Override
    public String getContent()
    {
        return content;
    }

    /* (non-Javadoc)
     * @see storageInterface.ISINode#getETag()
     */
    @Override
    public String getETag()
    {
        String uriConcatContent = uri + content;

        byte[] stringBytes = null;

        try
        {
            stringBytes = uriConcatContent.getBytes("UTF-8");
        }
        catch(UnsupportedEncodingException ueEx)
        {
            ueEx.printStackTrace();
        }

        MessageDigest messageDigest = null;

        try
        {
            messageDigest = MessageDigest.getInstance("SHA-256");
        }
        catch(NoSuchAlgorithmException nsaEx)
        {
            nsaEx.printStackTrace();
        }

        byte[] etagBytes = messageDigest.digest(stringBytes);

        final String etagString = new String(Hex.encodeHex(etagBytes));

        return etagString;
    }


}
