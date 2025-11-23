package pokemonifo.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import pokemonifo.model.PokemonWeight;

@Component
public class PokemonWeightProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String jsonBody = exchange.getIn().getBody(String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonBody);

        String name = root.path("name").asText();
        int weight = root.path("weight").asInt(); // El peso está en la raíz del JSON

        // Creamos el objeto limpio, SIN defensa
        exchange.getIn().setBody(new PokemonWeight(name, weight));
    }
}