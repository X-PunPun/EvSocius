package pokemonifo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.camel.ProducerTemplate;
import org.springframework.web.bind.annotation.*;
import pokemonifo.model.PokemonDef;
import pokemonifo.model.PokemonExp;
import pokemonifo.model.PokemonType;
import pokemonifo.model.PokemonWeight;
import pokemonifo.service.IPokemonService;


import java.util.List;

@RestController
@RequestMapping("/api/v1/pokemon")
@Tag(name = "Pokemon API", description = "API Hexagonal con Camel")
public class PokeController {

    private final IPokemonService pokemonService;

    // Inyectamos el servicio, no el ProducerTemplate
    public PokeController(IPokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @Operation(summary = "Buscar por Tipo", description = "Retorna lista de Pokemones de un tipo específico")
    @GetMapping("/type/{type}")
    public List<PokemonType> getByType(@PathVariable String type) {
        return pokemonService.getPokemonByType(type);
    }

    @Operation(summary = "Filtrar por Defensa", description = "Filtra Pokemones tipo Acero con defensa mínima")
    @GetMapping("/defense")
    public List<PokemonDef> getByDefense(@RequestParam(value = "min") int min) {
        return pokemonService.getPokemonByDefense(min);
    }

    @Operation(summary = "Filtrar por Peso", description = "Filtra Pokemones tipo Normal en rango de peso")
    @GetMapping("/weight")
    public List<PokemonWeight> getByWeight(@RequestParam(value = "min") int min, @RequestParam(value = "max") int max) {
        return pokemonService.getPokemonByWeight(min, max);
    }

    @Operation(summary = "Filtrar por Experiencia", description = "Filtra Pokemones tipo Psíquico por experiencia base")
    @GetMapping("/exp")
    public List<PokemonExp> getByExp(@RequestParam(value = "min") int min) {
        return pokemonService.getPokemonByExp(min);
    }
}
