package pokemonifo.controller;

import org.apache.camel.ProducerTemplate;
import org.springframework.web.bind.annotation.*;
import pokemonifo.model.PokemonDef;
import pokemonifo.model.PokemonInfo;


import java.util.List;

@RestController
@RequestMapping("/api/v1/pokemon")
public class PokeController {

    private final ProducerTemplate producerTemplate;

    public PokeController(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @GetMapping("/type/{type}")
    public List<PokemonInfo> getByType(@PathVariable String type) {
        // Invocamos la ruta de Camel 'direct:getPokemonByType'
        // Enviamos el parámetro 'type' en el header
        return producerTemplate.requestBodyAndHeader(
                "direct:getPokemonByType",
                null,
                "type",
                type,
                List.class // Esperamos una lista de vuelta
        );
    }

    // ERROR CORREGIDO AQUÍ ABAJO:
    // Antes decías: public List<PokeDefRoutes> ...
    // PokeDefRoutes es un Procesador, no un dato. Debes devolver PokemonDef.
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
}
