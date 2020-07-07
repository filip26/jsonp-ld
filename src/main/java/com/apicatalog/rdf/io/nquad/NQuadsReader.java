package com.apicatalog.rdf.io.nquad;

import java.io.Reader;
import java.util.Arrays;

import com.apicatalog.jsonld.uri.UriUtils;
import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.RdfLiteral;
import com.apicatalog.rdf.RdfNQuad;
import com.apicatalog.rdf.RdfResource;
import com.apicatalog.rdf.RdfValue;
import com.apicatalog.rdf.io.RdfReader;
import com.apicatalog.rdf.io.nquad.Tokenizer.Token;
import com.apicatalog.rdf.io.nquad.Tokenizer.TokenType;

/**
 * 
 * @see <a href="https://www.w3.org/TR/n-quads/">RDF 1.1. N-Quads</a>
 *
 */
public final class NQuadsReader implements RdfReader {

    private final Tokenizer tokenizer;
    
    private RdfDataset dataset;
    
    public NQuadsReader(final Reader reader) {
        this.tokenizer = new Tokenizer(reader);
        this.dataset = null;
    }
    
    @Override
    public RdfDataset readDataset() throws NQuadsReaderException {

        if (dataset != null) {
            return dataset;
        }
        
        dataset = Rdf.createDataset();
        
        while (tokenizer.hasNext()) {

            // skip EOL and whitespace
            if (tokenizer.accept(Tokenizer.TokenType.END_OF_LINE)
                    || tokenizer.accept(Tokenizer.TokenType.WHITE_SPACE)
                    || tokenizer.accept(Tokenizer.TokenType.COMMENT)
                    ) {

                continue;
            }
            
            dataset.add(reaStatement());
        }

        return dataset;
    }
    
    private RdfNQuad reaStatement() throws NQuadsReaderException {

        RdfResource subject = readResource("Subject");
  
        skipWhitespace(0);
  
        RdfResource predicate = readResource("Predicate");

        skipWhitespace(0);
  
        RdfValue object = readObject();

        RdfResource graphName = null;
        
        skipWhitespace(0);
        
        if (TokenType.IRI_REF == tokenizer.token().getType()) {

            final String graphNameIri = tokenizer.token().getValue();

            assertAbsoluteIri(graphNameIri, "Graph name");
            
            graphName = Rdf.createIRI(graphNameIri);
            tokenizer.next();
            skipWhitespace(0);
        }

        if (TokenType.BLANK_NODE_LABEL == tokenizer.token().getType()) {
            
            graphName = Rdf.createBlankNode("_:".concat(tokenizer.token().getValue()));
            tokenizer.next();
            skipWhitespace(0);
        }

        if (TokenType.END_OF_STATEMENT != tokenizer.token().getType()) {
            unexpected(tokenizer.token(), TokenType.END_OF_STATEMENT);
        }
        
        tokenizer.next();

        skipWhitespace(0);

        // skip comment
        if (TokenType.COMMENT == tokenizer.token().getType()) {
            tokenizer.next();

        // skip end of line
        } else if (TokenType.END_OF_LINE != tokenizer.token().getType() && TokenType.END_OF_INPUT != tokenizer.token().getType()) {
            unexpected(tokenizer.token(), TokenType.END_OF_LINE, TokenType.END_OF_INPUT);
            tokenizer.next();
        }
        
        return Rdf.createNQuad(subject, predicate, object, graphName);
    }
    
    private RdfResource readResource(String name)  throws NQuadsReaderException {

        final Token token = tokenizer.token();
        
        if (TokenType.IRI_REF == token.getType()) {
            
            tokenizer.next();
            
            final String iri = token.getValue();
            
            assertAbsoluteIri(iri, name);

            return Rdf.createIRI(iri);
        } 

        if (TokenType.BLANK_NODE_LABEL == token.getType()) {
            
            tokenizer.next();
            
            return Rdf.createBlankNode("_:".concat(token.getValue()));            
        }
  
        return unexpected(token);
    }
    
    private RdfValue readObject()  throws NQuadsReaderException {
        
        Token token = tokenizer.token();
        
        if (TokenType.IRI_REF == token.getType()) {
            tokenizer.next();
            
            final String iri = token.getValue();
            
            assertAbsoluteIri(iri, "Object");
            
            return Rdf.createIRI(iri);
        } 

        if (TokenType.BLANK_NODE_LABEL == token.getType()) {
            
            tokenizer.next();
            
            return Rdf.createBlankNode("_:".concat(token.getValue()));            
        }
  
        return readLiteral();
    }
    
    private RdfLiteral readLiteral()  throws NQuadsReaderException {
  
        Token value = tokenizer.token();
        
        if (TokenType.STRING_LITERAL_QUOTE != value.getType()) {
            unexpected(value);
        }

        tokenizer.next();
        
        skipWhitespace(0);
        
        if (TokenType.LANGTAG == tokenizer.token().getType()) {
            
            String langTag = tokenizer.token().getValue();
            
            tokenizer.next();
            
            return Rdf.createLangString(value.getValue(), langTag);
            
        } else if (TokenType.LITERAL_DATA_TYPE == tokenizer.token().getType()) {
            
            tokenizer.next();
            skipWhitespace(0);
            
            Token attr = tokenizer.token();
                        
            if (TokenType.IRI_REF == attr.getType()) {
                
                tokenizer.next();
                
                final String iri = attr.getValue();
                
                assertAbsoluteIri(iri, "DataType");

                return Rdf.createTypedString(value.getValue(), iri);
            }
                
            unexpected(attr);
        }
        
        return Rdf.createString(value.getValue());
    }

    
    private static final <T> T unexpected(Token token, TokenType ...types) throws NQuadsReaderException {
        throw new NQuadsReaderException(
                    "Unexpected token " + token.getType() + (token.getValue() != null ? "[" + token.getValue() + "]" : "" ) +  ". "
                    + "Expected one of " + Arrays.toString(types) + "."
                    );
    }
    
    private void skipWhitespace(int min) throws NQuadsReaderException {
  
        int count = 0;
        
        while (tokenizer.accept(TokenType.WHITE_SPACE)) {
            count++;
        }
  
        if (count < min) {
            unexpected(tokenizer.token());
        }        
    }
    
    private static final void assertAbsoluteIri(final String iri, final String what) throws NQuadsReaderException {
        if (UriUtils.isNotAbsoluteUri(iri)) {
            throw new NQuadsReaderException(what + " must be an absolute IRI [" + iri  +  "]. ");
        }
    }
}
