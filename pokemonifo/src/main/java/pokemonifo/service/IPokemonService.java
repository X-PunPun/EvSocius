package pokemonifo.service;

import pokemonifo.model.PokemonDef;
import pokemonifo.model.PokemonExp;
import pokemonifo.model.PokemonType;
import pokemonifo.model.PokemonWeight;

import java.util.List;

public interface IPokemonService {
    List<PokemonType> getPokemonByType(String type);
    List<PokemonDef> getPokemonByDefense(int min, String type);
    List<PokemonWeight> getPokemonByWeight(int min, int max, String type);
    List<PokemonExp> getPokemonByExp(int min, String type);
}
