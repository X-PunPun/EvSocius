package pokemonifo.service;

import pokemonifo.model.PokemonDef;
import pokemonifo.model.PokemonExp;
import pokemonifo.model.PokemonType;
import pokemonifo.model.PokemonWeight;

import java.util.List;

public interface IPokemonRepository {
    List<PokemonType> fetchByType(String type);
    List<PokemonDef> fetchByDefense(int min, String type);
    List<PokemonWeight> fetchByWeight(int min, int max, String type);
    List<PokemonExp> fetchByExp(int min, String type);
}
