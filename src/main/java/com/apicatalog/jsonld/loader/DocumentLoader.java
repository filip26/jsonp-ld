package com.apicatalog.jsonld.loader;

import java.net.URI;

import com.apicatalog.jsonld.api.JsonLdError;
import com.apicatalog.jsonld.document.Document;

/**
 * The {@link DocumentLoader} defines an interface that custom document
 * loaders have to implement to be used to retrieve remote documents and
 * contexts. 
 * 
 * @see <a href=
 *      "https://www.w3.org/TR/json-ld11-api/#loaddocumentcallback">LoadDocumentCallback
 *      Specification</a>
 *
 */
public interface DocumentLoader {

    Document loadDocument(URI url, DocumentLoaderOptions options) throws JsonLdError;

}
