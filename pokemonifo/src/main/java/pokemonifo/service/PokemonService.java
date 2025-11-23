package pokemonifo.service;

import org.springframework.stereotype.Service;
import pokemonifo.model.PokemonDef;
import pokemonifo.model.PokemonExp;
import pokemonifo.model.PokemonType;
import pokemonifo.model.PokemonWeight;

import java.util.List;

@Service
public class PokemonService implements IPokemonService {

    private final IPokemonRepository repository;

    public PokemonService(IPokemonRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PokemonType> getPokemonByType(String type) {
        return repository.fetchByType(type);
    }

    @Override
    public List<PokemonDef> getPokemonByDefense(int min) {
        return repository.fetchByDefense(min);
    }

    @Override
    public List<PokemonWeight> getPokemonByWeight(int min, int max) {
        // Validación de negocio antes de llamar a Camel
        if (min < 0 || max < min) {
            throw new IllegalArgumentException("Rango de peso inválido: min debe ser menor que max y positivo.");
        }
        return repository.fetchByWeight(min, max);
    }

    @Override
    public List<PokemonExp> getPokemonByExp(int min) {
        return repository.fetchByExp(min);
    }
}