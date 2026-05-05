package tn.esprit.services;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;

public class SentimentService {

    private final StanfordCoreNLP pipeline;

    public SentimentService() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String analyserSentiment(String texte) {
        try {
            if (texte == null || texte.isBlank()) return "😐 NEUTRE";

            Annotation annotation = new Annotation(texte);
            pipeline.annotate(annotation);

            String sentiment = "Neutral";
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                break; // première phrase suffit
            }

            System.out.println("Sentiment détecté : " + sentiment);

            return switch (sentiment) {
                case "Very positive", "Positive" -> "😊 POSITIF";
                case "Very negative", "Negative" -> "😞 NÉGATIF";
                default                          -> "😐 NEUTRE";
            };

        } catch (Exception e) {
            System.err.println("Erreur sentiment : " + e.getMessage());
            return "😐 NEUTRE";
        }
    }
}