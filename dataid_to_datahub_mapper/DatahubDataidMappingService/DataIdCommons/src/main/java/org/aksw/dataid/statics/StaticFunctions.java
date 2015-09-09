package org.aksw.dataid.statics;

import org.aksw.dataid.config.RdfContext;
import org.aksw.dataid.errors.DataIdInputException;
import org.aksw.dataid.jsonutils.StaticJsonHelper;
import org.openrdf.model.*;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.*;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.turtle.TurtleWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Chile on 9/7/2015.
 */
public class StaticFunctions {

    public static Model createDefaultModel(RdfContext rdfContext)
    {
        TreeModel model = new TreeModel();
        return addRdfContext(model, rdfContext);
    }

    private static Model addRdfContext(Model model, RdfContext context)
    {
        for(String key : context)
            model.setNamespace(key, context.getUri(key));
        return model;
    }

    public static URI GetDataIdUri(String stump, String property)
    {
        return new URIImpl(stump + property);
    }

    public static URI GetDataIdUri(String uri)
    {
        return new URIImpl(uri);
    }

    public static boolean CompareToUri(URI uri, String uri2)
    {
        return CompareToUri(uri, GetDataIdUri(uri2));
    }

    public static boolean CompareToUri(URI uri, URI uri2)
    {
        if(uri.equals(uri2))
            return true;
        else
            return false;
    }

    public static <T extends Object> List<T> CreateGenericList(Class<T> type)
    {
        return new ArrayList<T>();
    }

    public static String GetDataIdType(Model model, Resource subject)
    {
        for(Statement st : model.filter(subject, StaticContent.a, null))
            if(st.getObject().stringValue().contains(StaticContent.dataIdStump)
                    || st.getObject().stringValue().contains(StaticContent.DataIdUri))
                return st.getObject().stringValue();
        return null;
    }

    public static Date ParseDate(String date) throws ParseException {
        date = date.replace("  ", " ");
        if (date.contains("."))
            return StaticContent.msDateFormatter.parse(date);
        else if (date.contains(":"))
            return StaticContent.secDateFormatter.parse(date);
        else if (date.contains("-"))
            return StaticContent.dayDateFormatter.parse(date);
        else
            throw new ParseException("date format not rcognized : 'yyyy-MM-dd (HH:mm:ss.(SSS))'", 0);
    }

    public static String FormatDate(Date date){
        return StaticContent.secDateFormatter.format(date);
    }

    public static Model parseModel(String ttl) throws DataIdInputException {

        StringReader sr = new StringReader(ttl);
        try {
            return Rio.parse(sr, "", StaticFunctions.getRDFFormat(ttl));
        } catch (IOException | RDFParseException e) {
            throw new DataIdInputException(e);
        }
    }

    public static String writeSerialization(Model m, RDFFormat serialization) throws DataIdInputException {
        try {
            StringWriter sw = new StringWriter();
            WriterConfig conf = new WriterConfig();
            conf.set(BasicWriterSettings.PRETTY_PRINT, true);
            Rio.write(m, sw, serialization, conf);
            sw.close();
            return sw.toString();
        } catch (RDFHandlerException e) {
            throw new DataIdInputException(e);
        } catch (IOException e) {
            throw new DataIdInputException(e);
        }
    }

    public static RDFFormat getRDFFormat(String input) throws DataIdInputException {
        input = input.trim();
        StringBuilder errorBuilder = new StringBuilder();
        try {
            if(StaticJsonHelper.isJsonLdValid(input))
                return RDFFormat.JSONLD;
        } catch (Exception e) {
            errorBuilder.append("JSONLD: " + e.getMessage() + "\n");
        }
        try {
        if(StaticJsonHelper.isTurtleValid(input))
            return RDFFormat.TURTLE;
        } catch (Exception e) {
            errorBuilder.append("TURTLE: " + e.getMessage() + "\n");
        }
        try {
        if(StaticJsonHelper.isNquadValid(input))
            return RDFFormat.NQUADS;
        } catch (Exception e) {
            errorBuilder.append("NQUADS: " + e.getMessage() + "\n");
        }
        try {
        if(StaticJsonHelper.isRdfXmlValid(input))
            return RDFFormat.RDFXML;
        } catch (Exception e) {
            errorBuilder.append("RDFXML: " + e.getMessage() + "\n");
        }
        throw new DataIdInputException("Serialization format could not be determined. Please check the following parser errors:\n" + errorBuilder.toString());
    }

}
