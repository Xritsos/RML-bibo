# RML-bibo
The purpose of this project is to design and implement a pipeline that transforms structured CSV data into an RDF knowledge graph using semantic web technologies. By leveraging known and well-documented ontologies such as BIBO, FOAF, and DCTERMS, the project aims to semantically enrich a bibliographic dataset and store it in GraphDB for advanced querying. The pipeline is implemented via Java scripts and includes data preprocessing, mapping with RML, RDF generation using the RDF4J library, and execution of SPARQL queries to demonstrate the advantages of RDF-based data representation

# Dataset
The dataset used in this project is the Goodreads Books dataset publicly available on Kaggle (https://www.kaggle.com/datasets/jealousleopard/goodreadsbooks). It contains information about over 10,000 books entirely scraped via the Goodreads API.  Goodreads is an American social cataloging website that allows individuals to search its database of books, annotations, quotes, and reviews. The currated dataset can be found on `src/main/resources/bibo/books.csv`.

# Ontology
### BIBO (Bibliographic Ontology) 

The Bibliographic Ontology (http://purl.org/ontology/bibo/) provides classes and properties to describe books and other types of publications. The Bibliographic Ontology describes bibliographic things on the semantic Web in RDF. This ontology can be used as a citation ontology, as a document classification ontology, or simply as a way to describe any kind of document in RDF. It has been inspired by many existing document description metadata formats, and can be used as a common ground for converting other bibliographic data sources.


### DCTERMS (Dublin Core Terms) 

The Dublin Core Terms (http://dublincore.org/documents/dcmi-terms) vocabulary is a widely adopted metadata standard used for describing a broad range of digital and physical resources.


### FOAF (Friend of a Friend) 

The FOAF ontology (http://xmlns.com/foaf/0.1/) is designed to describe people, their activities, and their relations to other people and objects.


### Custom Ontology 

A custom ontology was created for properties not covered by the existing vocabularies. It was namespaced as “custom: http://www.custom.org/ontology/”.


![ontology schema](https://github.com/user-attachments/assets/c63a2e8c-7422-4a0a-ab12-d8314ec68422)

# Methodology

The mapping was performed in two stages: a) some mappings were performed via RML mapper, and b) the others using RDF4J (Java). The first mapping uses an RML turtle file `src/main/resources/bibo/RMLmapping.ttl` and is executed via Java (file `src/main/java/run_mapping.java`), producing the mapping file `src/main/resources/bibo/result_1.ttl`. The secong mapping includes some more complex processing due to the presence of multiple authors per book, a different date format for the published date, and some invalid characters for the publishers' names. This mapping is executed entirely programmatically throught the `src/main/java/run_extra_mapping.java` file, producing the second mapping file `src/main/resources/bibo/result_2.ttl`. Finally, the ontology, `src/main/resources/bibo/bibo_modified.ttl`, along with the results are pushed to a local GraphDB repository, where complex queries are executed.

# SPARQL Queries

The final step includes the run of some complex queries that showcase the usefulness of the mapping. To execute them we run the `src/main/java/run_graphdb.java` file that pushes the necessacery files to the local GraphDB repository and runs the queries that are saved as sparql files in `src/main/resources/queries`.


<table cellspacing="0" cellpadding="0" style="width: 100%; border: 1px solid black; border-collapse: collapse;">
  <tr>
    <td style="width: 30%; border: 1px solid black; text-align: right; vertical-align: bottom;">
      <img src="https://github.com/user-attachments/assets/93d139e9-8f5e-4a07-b001-4c61b4acb36b" alt="Screenshot 1" style="width: 100%; display: block;"/>
    </td>
    <td style="width: 70%; border: 1px solid black; text-align: left; vertical-align: top;">
      <img src="https://github.com/user-attachments/assets/52c37dd8-2fd1-4b8c-8470-94f6ba0fe12a" alt="Screenshot 2" style="width: 100%; display: block;"/>
    </td>
  </tr>
</table>







