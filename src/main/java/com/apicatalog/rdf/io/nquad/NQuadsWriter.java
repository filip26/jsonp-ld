package com.apicatalog.rdf.io.nquad;

import java.io.IOException;
import java.io.Writer;

import com.apicatalog.iri.IRI;
import com.apicatalog.jsonld.lang.BlankNode;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.RdfLiteral;
import com.apicatalog.rdf.RdfObject;
import com.apicatalog.rdf.RdfSubject;
import com.apicatalog.rdf.RdfTriple;
import com.apicatalog.rdf.io.RdfWriter;

public class NQuadsWriter implements RdfWriter {

    private final Writer writer;
    
    public NQuadsWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void write(RdfDataset dataset) throws IOException {
        for (RdfTriple triple : dataset.getDefaultGraph().getList()) {
            write(triple);
        }
        
        writer.flush();
    }
    
    public void write(RdfTriple triple) throws IOException {
        
        write(triple.getSubject());
        writer.write(' ');
        
        write(triple.getPredicate());
        writer.write(' ');
        
        write(triple.getObject());
        writer.write(" .\n");
    }

    public void write(RdfObject object) throws IOException {
        if (object == null) {
            throw new IllegalArgumentException();
        }
        
        if (object.isIRI()) {
            write(object.asIRI());
            return;
        }
        
        if (object.isLiteral()) {
            write(object.asLiteral());
            return;
        }
        
        if (object.isBlankNode()) {
            write(object.asBlankNode());
            return;
        }

        throw new IllegalStateException();
    }

    private void write(BlankNode blankNode) throws IOException {
        writer.write(blankNode.toString());
    }

    private void write(RdfLiteral literal) throws IOException {
        writer.write('"');
        writer.write(literal.getValue().replaceAll("\"", "\\\""));
        writer.write('"');
        
        if (literal.getLanguage() != null) {
            writer.write("@");
            writer.write(literal.getLanguage());
            
        } else if (literal.getDatatype() != null) {
            
            if ("http://www.w3.org/2001/XMLSchema#string".equals(literal.getDatatype().toString())) { //TODO constants!
                return;
            }
            
            writer.write("^^");
            write(literal.getDatatype());
        }
    }

    public void write(IRI iri) throws IOException {
        if (iri == null) {
            throw new IllegalArgumentException();
        }
        
        writer.write('<');
        writer.write(iri.toString());
        writer.write('>');
    }

    public void write(RdfSubject subject) throws IOException {

        if (subject == null) {
            throw new IllegalArgumentException();
        }
            
        if (subject.isIRI()) {
            write(subject.asIRI());
            return;
        }

        if (subject.isBlankNode()) {
            writer.write(subject.asBlankNode().toString());
            return;
        }

        throw new IllegalStateException();
    }
}
