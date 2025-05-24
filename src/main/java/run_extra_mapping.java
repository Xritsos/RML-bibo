import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class run_extra_mapping {

    public static void main(String[] args) {
        String csvFile = "./src/main/resources/bibo/books.csv";
        String baseURI = "http://example.org/";
        Model model = new LinkedHashModel();

        // convert date format to match xsd:dateTime
        ValueFactory vf = SimpleValueFactory.getInstance();
        SimpleDateFormat inputFormat = new SimpleDateFormat("M/d/yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String header = br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length < 12) continue;

                String bookID = fields[0].trim();
                String authorsRaw = fields[2].trim();
                String pubDateRaw = fields[10].trim();
                String publishersRaw = fields[11].trim();

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

                // Process Publishing Organizations (due to strange characters occurring)
                String pubId = publishersRaw.toLowerCase().replaceAll("[^a-z0-9]", "_");
                IRI pubIRI = vf.createIRI(baseURI, "organization/" + pubId);
                model.add(pubIRI, RDF.TYPE, vf.createIRI("http://xmlns.com/foaf/0.1/Organization"));
                model.add(pubIRI, vf.createIRI("http://xmlns.com/foaf/0.1/name"), vf.createLiteral(publishersRaw));
                model.add(bookIRI, vf.createIRI("http://purl.org/dc/terms/publisher"), pubIRI);

                // Convert and add publication date
                try {
                    Date parsedDate = inputFormat.parse(pubDateRaw);
                    String isoDate = outputFormat.format(parsedDate);
                    Literal dateLiteral = vf.createLiteral(isoDate, XSD.DATETIME);
                    model.add(bookIRI, DCTERMS.ISSUED, dateLiteral);
                } catch (ParseException e) {
                    System.err.println("Failed to parse date: " + pubDateRaw + "for book: " + bookID);
                    System.err.println("Error: " + e);
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
