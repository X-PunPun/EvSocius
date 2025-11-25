package pokemonifo.routes;

import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;
import pokemonifo.model.PokemonDef;
import pokemonifo.model.PokemonExp;
import pokemonifo.model.PokemonType;
import pokemonifo.model.PokemonWeight;
import pokemonifo.service.IPokemonRepository;

import java.util.List;
import java.util.Map;

@Component
public class PokeCamelAdapter implements IPokemonRepository {

    private final ProducerTemplate producerTemplate;

    public PokeCamelAdapter(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @Override
    public List<PokemonType> fetchByType(String type) {
        return producerTemplate.requestBodyAndHeader("direct:getPokemonByType", null, "type", type, List.class);
    }

    @Override
    public List<PokemonDef> fetchByDefense(int min, String type) {
        // Enviamos 'type' en el header
        Map<String, Object> headers = Map.of("minDefense", min, "type", type);
        return producerTemplate.requestBodyAndHeaders("direct:getPokemonByDefense", null, headers, List.class);
    }

    @Override
    public List<PokemonWeight> fetchByWeight(int min, int max, String type) {
        Map<String, Object> headers = Map.of("minWeight", min, "maxWeight", max, "type", type);
        return producerTemplate.requestBodyAndHeaders("direct:getPokemonByWeight", null, headers, List.class);
    }

    @Override
    public List<PokemonExp> fetchByExp(int min, String type) {
        Map<String, Object> headers = Map.of("minExp", min, "type", type);
        return producerTemplate.requestBodyAndHeaders("direct:getPokemonByExp", null, headers, List.class);
    }
}