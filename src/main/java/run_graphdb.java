import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.IOException;

public class run_graphdb {

    /**
     * How to connect to a graphdb repository, load a file to a repository, add some data through the API and perform a
     * sparql query
     */
    public static void main(String[] args) {

        HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/MiniProject");
        RepositoryConnection connection = repository.getConnection();

        // Clear the repository before we start
        connection.clear();

        // load a simple ontology from a file
        connection.begin();
        // Adding the family ontology
        try {
            connection.add(run_graphdb.class.getResourceAsStream("/bibo/bibo_modified.ttl"), "urn:base",
                    RDFFormat.TURTLE);

            // Adding the mappings file (adjust the path if necessary) (without authors)
            connection.add(run_graphdb.class.getResourceAsStream("/bibo/results_1.ttl"), "urn:base", RDFFormat.TURTLE);

            // Adding the mappings file (adjust the path if necessary) (with authors)
            connection.add(run_graphdb.class.getResourceAsStream("/bibo/results_2.ttl"), "urn:base", RDFFormat.TURTLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Committing the transaction persists the data
        connection.commit();

//        ModelBuilder builder = new ModelBuilder();
//        builder.setNamespace("ex", "http://www.semanticweb.org/simple_ontology#")
//                .subject("ex:george")
//                .add(RDF.TYPE, "ex:Professor");
//
//        Model model = builder.build();
//
//        // add our data
//        connection.begin();
//        connection.add(model);
//        connection.commit();

        // Perform a SPARQL SELECT-query to retrieve books with averageRating > 3.0
        String queryString =
            "PREFIX bibo: <http://purl.org/ontology/bibo/>\n" +
            "PREFIX terms: <http://purl.org/dc/terms/>\n" +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
            "PREFIX cu: <http://www.custom.org/ontology/>\n" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
            "SELECT ?book ?title ?rating ?date ?lang ?publ ?auth_name\n" +
            "WHERE {\n" +
            "    ?book a bibo:Book.\n" +
            "    ?book terms:title ?title.\n" +
            "    ?book cu:averageRating ?rating.\n" +
            "    ?book terms:issued ?date.\n" +
            "    ?book terms:language ?lang.\n" +
            "    ?book terms:publisher ?publ.\n" +
            "    ?book terms:creator ?auth.\n" +
            "    ?auth foaf:name ?auth_name.\n" +
            "    FILTER (?date = '2006-09-16T00:00:00'^^xsd:dateTime)\n" +
            "}";

        TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

        TupleQueryResult results = query.evaluate();
        // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
    // try (TupleQueryResult result = query.evaluate()) {
            // we just iterate over all solutions in the result...
        for (BindingSet result : results) {
            // ... and print out the value of the variable binding for ?s and ?n
            System.out.println("Book URI: " + result.getValue("book") + "\n");
            System.out.println("Title: " + result.getValue("title") + "\n");
            System.out.println("Rating: " + result.getValue("rating") + "\n");
            System.out.println("Issued Date: " + result.getValue("date") + "\n");
            System.out.println("Language: " + result.getValue("lang") + "\n");
            System.out.println("hasPublisher: " + result.getValue("publ") + "\n");
            System.out.println("hascreator(s): " + result.getValue("auth_name") + "\n");
        }
    // }

        connection.close();
        repository.shutDown();
    }
}
