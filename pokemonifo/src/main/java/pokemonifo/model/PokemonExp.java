package pokemonifo.model;

public class PokemonExp {
    private String name;
    private int baseExperience;

    public PokemonExp() {}

    public PokemonExp(String name, int baseExperience) {
        this.name = name;
        this.baseExperience = baseExperience;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBaseExperience() {
        return baseExperience;
    }

    public void setBaseExperience(int baseExperience) {
        this.baseExperience = baseExperience;
    }
}
