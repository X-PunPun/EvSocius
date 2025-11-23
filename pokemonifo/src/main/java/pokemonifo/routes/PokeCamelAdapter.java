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
    public List<PokemonDef> fetchByDefense(int min) {
        return producerTemplate.requestBodyAndHeader("direct:getPokemonByDefense", null, "minDefense", min, List.class);
    }

    @Override
    public List<PokemonWeight> fetchByWeight(int min, int max) {
        return producerTemplate.requestBodyAndHeaders("direct:getPokemonByWeight", null, Map.of("minWeight", min, "maxWeight", max), List.class);
    }

    @Override
    public List<PokemonExp> fetchByExp(int min) {
        return producerTemplate.requestBodyAndHeader("direct:getPokemonByExp", null, "minExp", min, List.class);
    }
}