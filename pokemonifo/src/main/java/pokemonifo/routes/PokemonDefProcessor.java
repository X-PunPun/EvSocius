package pokemonifo.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;
import pokemonifo.model.PokemonDef;

@Component
public class PokemonDefProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String jsonBody = exchange.getIn().getBody(String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonBody);

        String name = root.path("name").asText();
        int defense = 0;

        // se busca la defensa dentro del array
        JsonNode statsArray = root.path("stats");
        if (statsArray.isArray()) {
            for (JsonNode statNode : statsArray) {
                String statName = statNode.path("stat").path("name").asText();
                if ("defense".equals(statName)) {
                    defense = statNode.path("base_stat").asInt();
                    break;
                }
            }
        }

        // Devolvemos el objeto PokemonDef
        exchange.getIn().setBody(new PokemonDef(name, defense));
    }
}