import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.util.UUID;

public class run_authors {

    public static void main(String[] args) {
        String csvFile = "./src/main/resources/bibo/books.csv";
        String baseURI = "http://example.org/";
        Model model = new LinkedHashModel();

        ValueFactory vf = SimpleValueFactory.getInstance();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String header = br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length < 3) continue;

                String bookID = fields[0].trim();
                String authorsRaw = fields[2].trim();

                // Create Book IRI using bookID
                IRI bookIRI = vf.createIRI(baseURI, "book/" + bookID);
                model.add(bookIRI, RDF.TYPE, vf.createIRI("http://purl.org/ontology/bibo/Book"));

                // Process authors
                for (String name : authorsRaw.split("/")) {
                    name = name.trim();
                    if (name.isEmpty()) continue;

                    String personId = name.toLowerCase().replaceAll("[^a-z0-9]", "_");
                    IRI personIRI = vf.createIRI(baseURI, "person/" + personId);
                    model.add(personIRI, RDF.TYPE, vf.createIRI("http://xmlns.com/foaf/0.1/Person"));
                    model.add(personIRI, vf.createIRI("http://xmlns.com/foaf/0.1/name"), vf.createLiteral(name));
                    model.add(bookIRI, vf.createIRI("http://purl.org/dc/terms/creator"), personIRI);
                }
            }

            // Write output RDF to TTL file
            try (OutputStream out = new FileOutputStream("./src/main/resources/bibo/results_2.ttl")) {
                Rio.write(model, out, RDFFormat.TURTLE);
            }

            System.out.println("RDF mapping complete: src/main/resources/bibo/results_2.ttl");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
