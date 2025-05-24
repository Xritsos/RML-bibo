import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class run_graphdb {
    /**
     * How to connect to a graphdb repository, load a file to a repository, add some data through the API and perform a
     * sparql query
     */
    public static void main(String[] args) {
        HTTPRepository repository = new HTTPRepository("http://localhost:7200/repositories/MiniProject");
        try (RepositoryConnection connection = repository.getConnection()) {

            // Clear the repository before we start
            connection.clear();

            // Load ontology and mappings
            connection.begin();
            try {
                connection.add(run_graphdb.class.getResourceAsStream("/bibo/bibo_modified.ttl"), "urn:base",
                        RDFFormat.TURTLE);

                // Adding the mappings file (adjust the path if necessary) (without authors/publishers)
                connection.add(run_graphdb.class.getResourceAsStream("/bibo/results_1.ttl"), "urn:base", RDFFormat.TURTLE);

                // Adding the mappings file (adjust the path if necessary) (with authors/publishers)
                connection.add(run_graphdb.class.getResourceAsStream("/bibo/results_2.ttl"), "urn:base", RDFFormat.TURTLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Committing the transaction persists the data
            connection.commit();

            // List of query filenames (without path)
            List<String> queries = Arrays.asList(
                "authors_by_books.sparql",
                "authors_by_decade.sparql",
                "books_by_rating.sparql",
                "books_by_year.sparql",
                "publishers_by_books.sparql",
                "publishers_by_pages.sparql",
                "publishers_by_rating.sparql"
            );

            // Execute each query
            for (String queryFile : queries) {
                System.out.println("Executing: " + queryFile);
                String queryString = loadQueryFromFile(queryFile);
                TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

                try (TupleQueryResult results = query.evaluate()) {
                    while (results.hasNext()) {
                        BindingSet binding = results.next();
                        for (String name : binding.getBindingNames()) {
                            System.out.println(name + ": " + binding.getValue(name));
                        }
                        System.out.println("------");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            repository.shutDown();
        }
    }

    // Utility method to load a SPARQL query from /queries/ folder
    private static String loadQueryFromFile(String fileName) throws IOException {
        ClassLoader classLoader = run_graphdb.class.getClassLoader();
        try (InputStream input = classLoader.getResourceAsStream("queries/" + fileName)) {
            if (input == null) {
                throw new FileNotFoundException("Query file not found in classpath: queries/" + fileName);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
