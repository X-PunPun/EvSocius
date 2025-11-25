package pokemonifo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    public PokeController(IPokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @Operation(summary = "Buscar por Tipo", description = "Retorna lista de Pokemones de un tipo específico")
    @GetMapping("/type/{type}")
    public List<PokemonType> getByType(@PathVariable String type) {
        return pokemonService.getPokemonByType(type);
    }

    @Operation(summary = "Filtrar por Defensa", description = "Filtra Pokemones de un tipo dado con defensa mínima")
    @GetMapping("/defense")
    public List<PokemonDef> getByDefense(
            @RequestParam(value = "min") int min,
            @RequestParam(value = "type", defaultValue = "steel") String type) { // Dinámico
        return pokemonService.getPokemonByDefense(min, type);
    }

    @Operation(summary = "Filtrar por Peso", description = "Filtra Pokemones de un tipo dado en rango de peso")
    @GetMapping("/weight")
    public List<PokemonWeight> getByWeight(
            @RequestParam(value = "min") int min,
            @RequestParam(value = "max") int max,
            @RequestParam(value = "type", defaultValue = "normal") String type) { // Dinámico
        return pokemonService.getPokemonByWeight(min, max, type);
    }

    @Operation(summary = "Filtrar por Experiencia", description = "Filtra Pokemones de un tipo dado por experiencia base")
    @GetMapping("/exp")
    public List<PokemonExp> getByExp(
            @RequestParam(value = "min") int min,
            @RequestParam(value = "type", defaultValue = "psychic") String type) { // Dinámico
        return pokemonService.getPokemonByExp(min, type);
    }
}