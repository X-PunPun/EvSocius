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
        if (type == null || type.isBlank()) throw new IllegalArgumentException("El tipo es obligatorio.");
        return repository.fetchByType(type);
    }

    @Override
    public List<PokemonDef> getPokemonByDefense(int min, String type) {
        if (min < 0) throw new IllegalArgumentException("La defensa mínima no puede ser negativa.");
        return repository.fetchByDefense(min, type);
    }

    @Override
    public List<PokemonWeight> getPokemonByWeight(int min, int max, String type) {
        if (min < 0) throw new IllegalArgumentException("El peso no puede ser negativo.");
        if (min > max) throw new IllegalArgumentException("El peso mínimo no puede ser mayor al máximo.");
        return repository.fetchByWeight(min, max, type);
    }

    @Override
    public List<PokemonExp> getPokemonByExp(int min, String type) {
        if (min < 0) throw new IllegalArgumentException("La experiencia no puede ser negativa.");
        return repository.fetchByExp(min, type);
    }
}