import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
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

            // Adding the mappings file (adjust the path if necessary)
            connection.add(run_graphdb.class.getResourceAsStream("/bibo/results_1.ttl"), "urn:base", RDFFormat.TURTLE);
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
        String queryString = "PREFIX bibo: <http://purl.org/ontology/bibo/> \n";
        queryString += "PREFIX terms: <http://purl.org/dc/terms/> \n";
        queryString += "PREFIX cu: <http://www.custom.org/ontology/> \n";
        queryString += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n";
        queryString += "SELECT ?book ?title ?rating \n";
        queryString += "WHERE { \n";
        queryString += "    ?book a bibo:Book. \n";  // Match resources of type bibo:Book
        queryString += "    ?book terms:title ?title. \n";  // Match the title of the book
        queryString += "    ?book cu:averageRating ?rating. \n";  // Match the averageRating of the book
        queryString += "    FILTER(xsd:float(?rating) > 3.0) \n";  // Filter books with averageRating > 3.0
        queryString += "}";

        TupleQuery query = connection.prepareTupleQuery(queryString);

        TupleQueryResult results = query.evaluate();
        // A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
//        try (TupleQueryResult result = query.evaluate()) {
            // we just iterate over all solutions in the result...
        for (BindingSet result : results) {
            // ... and print out the value of the variable binding for ?s and ?n
            System.out.println("Title: " + result.getValue("title"));
            System.out.println("Rating: " + result.getValue("rating") + "\n");
        }
//        }

        connection.close();
        repository.shutDown();
    }
}
