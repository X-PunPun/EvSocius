package pokemonifo.controller;

import org.apache.camel.ProducerTemplate;
import org.springframework.web.bind.annotation.*;
import pokemonifo.model.PokemonDef;
import pokemonifo.model.PokemonExp;
import pokemonifo.model.PokemonType;
import pokemonifo.model.PokemonWeight;


import java.util.List;

@RestController
@RequestMapping("/api/v1/pokemon")
public class PokeController {

    private final ProducerTemplate producerTemplate;

    public PokeController(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @GetMapping("/type/{type}")
    public List<PokemonType> getByType(@PathVariable String type) { // <--- Devuelve List<PokemonType>

        return producerTemplate.requestBodyAndHeader(
                "direct:getPokemonByType",
                null,
                "type",
                type,
                List.class
        );
    }

    // Requerimiento 2.2: Filtrar por defensa mínima
    @GetMapping("/defense")
    public List<PokemonDef> getByDefense(@RequestParam(value = "min") int min) {

        return producerTemplate.requestBodyAndHeader(
                "direct:getPokemonByDefense",
                null,
                "minDefense",
                min,
                List.class
        );
    }

    @GetMapping("/weight")
    public List<PokemonWeight> getByWeight(
            @RequestParam(value = "min") int min,
            @RequestParam(value = "max") int max) {

        return producerTemplate.requestBodyAndHeaders(
                "direct:getPokemonByWeight",
                null,
                java.util.Map.of("minWeight", min, "maxWeight", max),
                List.class
        );
    }

    // Requerimiento 2.4: Filtrar por experiencia base
    @GetMapping("/exp")
    public List<PokemonExp> getByExp(@RequestParam(value = "min") int min) {

        return producerTemplate.requestBodyAndHeader(
                "direct:getPokemonByExp",
                null,
                "minExp",
                min,
                List.class
        );
    }
}
