package storageInterface;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * {@link HttpServlet} che implementa l'applicazione "Storage Interface".
 *
 *
 */
@SuppressWarnings("serial")
public class StorageInterfaceServlet extends HttpServlet
{
    // database
    private IDatabase db = new DatastoreDatabase();

    // http://localhost:8888/StorageInterface/!SI/
    private final static String storageInterfaceURIprefix = "/StorageInterface/!SI/";

    // percorso dell'xml schema da usare per la validazione
    // (a partire dalla cartella war/)
    private final static String xsdFilePath = "xsd/sinode.xsd";

    // headers: nomi
    private final static String etagHeaderName = "ETag";

    private final static String acceptHeaderName = "Accept";

    private final static String contentTypeHeaderName = "Content-Type";

    private final static String ifMatchHeaderName = "If-Match";

    // headers: valori validi
    private final static String validAcceptHeader = "application/xml";

    private final static String validContentTypeHeader = validAcceptHeader;


    // messaggi di errore (usati dal metodo sendError)
    private final static String postMethodUndefinedMessage = "POST method is undefined, use PUT instead";

    private final static String emptyResourceNameMessage = "Resource name cannot be empty";

    private final static String invalidAcceptHeaderMessage = acceptHeaderName + " header must be \"" + validAcceptHeader + "\"\n";

    private final static String invalidContentTypeHeaderMessage = contentTypeHeaderName + " header must be \"" + validContentTypeHeader + "\"\n";

    private final static String ifMatchHeaderMissingMessage = ifMatchHeaderName + " header missing";

    private final static String invalidIfMatchHeaderMessage = etagHeaderName + " mismatch";

    private final static String resourceNotFoundMessage = "Resource not found";

    private final static String databaseFatalErrorMessage = "Database fatal error";

    private final static String invalidBodyMessage = "Request body does not match xml schema";

    //private final static String xmlValidatorFatalErrorMessage = "XML Validator fatal error";




    // ------------------------------------------------ GET -----------------------------------------------------------------
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws IOException
    {
        String resourceName = getResourceName(req);

        if(resourceName.isEmpty())
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, emptyResourceNameMessage);
            return;
        }


        if(!isAcceptHeaderValid(req))   // la richiesta NON e' accettata
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, invalidAcceptHeaderMessage);
            return;
        }
        else // la richiesta e' accettata
        {
            IResource siNode = db.get(resourceName);

            if(siNode == null)   // il nodo NON esiste
            {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, resourceNotFoundMessage);
                return;
            }
            else // il nodo esiste
            {
                String body = siNode.getContent();
                resp.getWriter().write(body);

                String eTag = siNode.getETag();
                resp.setHeader(etagHeaderName, eTag);

                resp.setStatus(HttpServletResponse.SC_OK);
            }

        }
    }



    // ------------------------------------------------ DELETE --------------------------------------------------------------
    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        String resourceName = getResourceName(req);

        if(resourceName.isEmpty())
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, emptyResourceNameMessage);
            return;
        }


        IResource siNode = db.get(resourceName);

        if(siNode == null)   // il nodo NON esiste
        {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, resourceNotFoundMessage);
            return;
        }
        else // il nodo esiste
        {
            if(db.delete(siNode))
            {
                resp.setStatus(HttpServletResponse.SC_OK);
            }
            else
            {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, databaseFatalErrorMessage);
            }

        }
    }


    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, postMethodUndefinedMessage);
        return;
    }


    // ------------------------------------------------ PUT -----------------------------------------------------------------
    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        String resourceName = getResourceName(req);

        if(resourceName.isEmpty())
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, emptyResourceNameMessage);
            return;
        }


        // accept header sia per create che update
        if(!isAcceptHeaderValid(req))
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, invalidAcceptHeaderMessage);
            return;
        }

        // content type header sia per create che update
        if(!isContentTypeHeaderValid(req))
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, invalidContentTypeHeaderMessage);
            return;
        }


        // prima di distinguere tra create e update, controllo se il body e' valido
        // altrimenti non ha senso andare avanti...!

        // VALIDAZIONE DEL BODY SULL'XML SCHEMA
        String body = getRequestBody(req);

        boolean validBody = true;

        try
        {
            validBody = isValid(body);
        }
        catch(Exception e)  // IOException o SAXException, in entrambi i casi ha fallito internamente il validatore
        {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        }

        if(!validBody)   // il body NON rispetta lo schema xml
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, invalidBodyMessage);
            return;
        }


        // ... se arrivo qui il body rispetta l'xml schema dato!

        boolean update = true;

        IResource siNode = db.get(resourceName);

        if(siNode != null)   //--------------------------- UPDATE -------------------------------------------
        {
            if(isIfMatchHeaderPresent(req))   // if match header presente
            {
                String updatingNodeETag = siNode.getETag();

                if(!isIfMatchHeaderValid(req, updatingNodeETag))    // ETag mismatch
                {
                    resp.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, invalidIfMatchHeaderMessage);
                    return;
                }
            }
            else // if match header NON presente
            {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ifMatchHeaderMissingMessage);
                return;
            }
        }
        else // -------------------------------------------- CREATE -------------------------------------------
        {
            update = false;
        }

        // in entrambi i casi...
        // creo il nodo...
        IResource savingNode = new SINode(resourceName);
        // gli assegno il contenuto...
        savingNode.setContent(body);
        // lo salvo nel db.
        db.put(savingNode);

        // adesso recupero dal db il nodo appena salvato...
        IResource savedNode = db.get(resourceName);

        if(savedNode == null)   //...ooops!
        {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, databaseFatalErrorMessage);
            return;
        }
        else
        {
            String savedNodeETag = savedNode.getETag();
            resp.setHeader(etagHeaderName, savedNodeETag);

            String savedNodeBody = savedNode.getContent();
            resp.getWriter().write(savedNodeBody);

            if(update)
            {
                resp.setStatus(HttpServletResponse.SC_OK);
            }
            else
            {
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }
        }

    }



    private String getResourceName(HttpServletRequest req)
    {
        String requestURI = new String(req.getRequestURI());
        String resourceName = requestURI.replace(storageInterfaceURIprefix, "");

        return resourceName;
    }


    private String getRequestBody(HttpServletRequest req) throws IOException
    {
        String body = new String();
        String line = req.getReader().readLine();

        while(line != null)
        {
            body = body.concat(line);
            body = body.concat("\n");
            line = req.getReader().readLine();
        }

        return body;
    }


    private boolean isAcceptHeaderValid(HttpServletRequest req)
    {
        String acceptHeader = req.getHeader(acceptHeaderName);

        if(acceptHeader == null || !acceptHeader.equals(validAcceptHeader))
        {
            return false;
        }

        return true;
    }


    private boolean isContentTypeHeaderValid(HttpServletRequest req)
    {
        String contentTypeHeader = req.getHeader(contentTypeHeaderName); // application/xml; charset=UTF-8

        if(contentTypeHeader == null || !contentTypeHeader.startsWith(validContentTypeHeader))
        {
            return false;
        }

        return true;
    }


    private boolean isIfMatchHeaderPresent(HttpServletRequest req)
    {
        String ifMatchHeader = req.getHeader(ifMatchHeaderName);

        if(ifMatchHeader == null)
        {
            return false;
        }

        return true;
    }


    private boolean isIfMatchHeaderValid(HttpServletRequest req, String updatingNodeETagString)
    {
        String ifMatchHeader = req.getHeader(ifMatchHeaderName);

        return ifMatchHeader.equals(updatingNodeETagString);
    }


    // se lancia una SAXException e' perche' ha fallito: Schema schema = factory.newSchema(schemaFile);
    private boolean isValid(String inputXml) throws SAXException, IOException
    {
        File schemaFile = new File(xsdFilePath);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(schemaFile); // lancia SAXException
        Validator validator = schema.newValidator();

        Source source = new StreamSource(new StringReader(inputXml));

        boolean validXMLinput = true;

        try
        {
            validator.validate(source); // se la SAXException la lanciasse lui, la beccherei qua! ...
            // ... pero' lui puo' lanciare anche IOException (se succede casino dentro), che lascio passare
        }
        catch(SAXException e)  // xml non valido per lo schema
        {
            validXMLinput = false;
        }

        return validXMLinput;
    }


}
