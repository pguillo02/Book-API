package es.ulpgc;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import spark.Request;
import spark.Response;
import spark.Spark;

import static spark.Spark.*;

public class Main {

    public static void main(String[] args) {

       HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();

        Spark.port(4567);

        path("/", () -> {
            get("/:palabra", (request, response) -> obtenerInformacionPalabra(request, response, hazelcastInstance));

        });
    }

    private static String obtenerInformacionPalabra(Request request, Response response, HazelcastInstance hazelcastInstance) {
        String palabra = request.params(":palabra");

        IMap<String, Word> wordMap = hazelcastInstance.getMap("wordMap");
        Word word = wordMap.get(palabra);

        if (word != null) {
            response.type("application/json");
            return "{ word: \"" + palabra + "\", references: " + convertirReferenciasAJson(word) + " }";
        } else {
            response.status(404);
            return "{ error: \"The word '" + palabra + "' is not found in any book.\" }";
        }
    }

    private static String convertirReferenciasAJson(Word word) {
        StringBuilder jsonBuilder = new StringBuilder("[");
        word.getReferences().forEach((libro, frecuencia) -> {
            jsonBuilder.append("{ book: \"").append(libro).append("\", number of appearances: ").append(frecuencia).append(" },");
        });

        if (jsonBuilder.length() > 1) {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }
}
