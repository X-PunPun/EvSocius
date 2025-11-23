package pokemonifo.model;

public class PokemonType {
    private String name;
    private String type; // <--- Guardamos el tipo (ej: "fire") en vez de la URL

    public PokemonType() {}

    public PokemonType(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
