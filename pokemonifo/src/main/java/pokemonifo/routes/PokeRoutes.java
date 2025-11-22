package pokemonifo.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pokemonifo.model.PokemonDef;
import pokemonifo.model.PokemonInfo;

import java.util.ArrayList;
import java.util.List;

@Component
public class PokeRoutes extends RouteBuilder {

    @Autowired
    private PokeDefRoutes pokeDef; // Esto está bien, inyectas el processor

    @Override
    public void configure() throws Exception {

        // Manejo de excepciones (igual que antes)
        onException(Exception.class)
                .handled(true)
                .log("Error al procesar la ruta: ${exception.message}")
                .end();

        // RUTA 1 (Igual que antes, asumiendo que PokemonInfo existe)
        from("direct:getPokemonByType")
                .routeId("GetPokemonByTypeRoute")
                .log("Consultando PokeAPI para tipo: ${header.type}")
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .toD("{{pokeapi.base-url}}type/${header.type}?bridgeEndpoint=true")
                .process(exchange -> {
                    String jsonRaw = exchange.getIn().getBody(String.class);
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(jsonRaw);
                    JsonNode pokemonArray = root.path("pokemon");
                    List<PokemonInfo> listaLimpia = new ArrayList<>();

                    if (pokemonArray.isArray()) {
                        for (JsonNode node : pokemonArray) {
                            JsonNode pData = node.path("pokemon");
                            listaLimpia.add(new PokemonInfo(
                                    pData.path("name").asText(),
                                    pData.path("url").asText()
                            ));
                        }
                    }
                    exchange.getIn().setBody(listaLimpia);
                });

        // RUTA 2: FILTRO DE DEFENSA
        from("direct:getPokemonByDefense")
                .routeId("FilterByDefenseRoute")
                .log("Buscando Pokemons con defensa >= ${header.minDefense}")

                .setHeader("type", constant("steel"))
                .to("direct:getPokemonByType")

                .split(body(), new ListaAgregar())
                .parallelProcessing()

                .setHeader("pokemonName", simple("${body.name}"))
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))

                .toD("{{pokeapi.base-url}}pokemon/${header.pokemonName}?bridgeEndpoint=true")

                // Aquí usas tu clase PokeDefRoutes para convertir JSON a PokemonDef
                .process(pokeDef)

                // --- AQUÍ ESTABA EL ERROR ---
                .process(exchange -> {
                    // CORRECCIÓN: Cambiado PokemonStats por PokemonDef
                    PokemonDef stats = exchange.getIn().getBody(PokemonDef.class);

                    Integer minDefense = exchange.getIn().getHeader("minDefense", Integer.class);

                    if (stats != null && minDefense != null) {
                        // CORRECCIÓN: stats.getDefense() ahora funciona porque stats es del tipo correcto
                        if (stats.getDefense() < minDefense) {
                            exchange.getIn().setBody(null); // Borrar si no cumple
                        }
                    }
                })
                .end();
    }
}
