package pokemonifo.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import pokemonifo.model.PokemonExp;

@Component
public class PokemonExpProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String jsonBody = exchange.getIn().getBody(String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonBody);

        String name = root.path("name").asText();

        // La experiencia raíz del JSON de PokeAPI
        int baseExp = root.path("base_experience").asInt();

        exchange.getIn().setBody(new PokemonExp(name, baseExp));
    }
}
