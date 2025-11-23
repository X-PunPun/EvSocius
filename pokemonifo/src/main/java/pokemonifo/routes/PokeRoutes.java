package pokemonifo.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pokemonifo.model.PokemonDef;
import pokemonifo.model.PokemonExp;
import pokemonifo.model.PokemonType;
import pokemonifo.model.PokemonWeight;

import java.util.ArrayList;
import java.util.List;

@Component
public class PokeRoutes extends RouteBuilder {

    @Autowired
    private PokemonDefProcessor pokemonDefProces; // Esto está bien, inyectas el processor

    @Autowired
    private PokemonWeightProcessor pokeWeight;

    @Autowired
    private PokemonExpProcessor pokemonExpProcessor;

    @Override
    public void configure() throws Exception {

        // Manejo de excepciones (igual que antes)
        onException(Exception.class)
                .handled(true)
                .log("Error al procesar la ruta: ${exception.message}")
                .end();

        // --- RUTA 1: Obtener por Tipo (ACTUALIZADA) ---
        from("direct:getPokemonByType")
                .routeId("GetPokemonByTypeRoute")
                .log("Consultando PokeAPI para tipo: ${header.type}")
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .toD("{{pokeapi.base-url}}type/${header.type}?bridgeEndpoint=true")
                .process(exchange -> {
                    String jsonRaw = exchange.getIn().getBody(String.class);

                    // 1. Obtenemos el tipo desde el Header (ej. "fire")
                    String typeName = exchange.getIn().getHeader("type", String.class);

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(jsonRaw);
                    JsonNode pokemonArray = root.path("pokemon");

                    // 2. Creamos la lista de tu nueva clase PokemonType
                    List<PokemonType> listaLimpia = new ArrayList<>();

                    if (pokemonArray.isArray()) {
                        for (JsonNode node : pokemonArray) {
                            JsonNode pData = node.path("pokemon");

                            // 3. Llenamos el objeto: Nombre (del JSON) y Tipo (del Header)
                            listaLimpia.add(new PokemonType(
                                    pData.path("name").asText(),
                                    typeName
                            ));
                        }
                    }
                    exchange.getIn().setBody(listaLimpia);
                });

        // --- RUTA DE DEFENSA USANDO PokemonDef ---
        from("direct:getPokemonByDefense")
                .routeId("FilterByDefenseRoute")
                .log("Buscando defensa mínima: ${header.minDefense}")

                // Usamos 'steel' (acero) como base de búsqueda
                .setHeader("type", constant("steel"))
                .to("direct:getPokemonByType")

                .split(body(), new ListaAgregar())
                .parallelProcessing()

                .setHeader("pokemonName", simple("${body.name}"))
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))

                .toD("{{pokeapi.base-url}}pokemon/${header.pokemonName}?bridgeEndpoint=true")

                // PASO 1: Transformar JSON -> PokemonDef
                .process(pokemonDefProces)

                // PASO 2: Filtrar lógicamente
                .process(exchange -> {
                    // Obtenemos el objeto PokemonDef
                    PokemonDef item = exchange.getIn().getBody(PokemonDef.class);
                    Integer min = exchange.getIn().getHeader("minDefense", Integer.class);

                    if (item != null && min != null) {
                        // Si la defensa es MENOR al mínimo, lo descartamos (null)
                        if (item.getDefense() < min) {
                            exchange.getIn().setBody(null);
                        }
                    }
                })
                .end();

        // --- RUTA 3: FILTRAR POR PESO (MODIFICADA) ---
        from("direct:getPokemonByWeight")
                .routeId("FilterByWeightRoute")
                .log("Buscando por peso: Min ${header.minWeight} - Max ${header.maxWeight}")

                .setHeader("type", constant("normal"))
                .to("direct:getPokemonByType")

                .split(body(), new ListaAgregar())
                .parallelProcessing()

                .setHeader("pokemonName", simple("${body.name}"))
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))

                .toD("{{pokeapi.base-url}}pokemon/${header.pokemonName}?bridgeEndpoint=true")

                // CAMBIO 1: Usamos el procesador que devuelve solo PokemonWeight
                .process(pokeWeight)

                // CAMBIO 2: Filtramos usando el objeto PokemonWeight
                .process(exchange -> {
                    PokemonWeight item = exchange.getIn().getBody(PokemonWeight.class);

                    Integer min = exchange.getIn().getHeader("minWeight", Integer.class);
                    Integer max = exchange.getIn().getHeader("maxWeight", Integer.class);

                    if (item != null && min != null && max != null) {
                        int pesoActual = item.getWeight();

                        if (pesoActual < min || pesoActual > max) {
                            exchange.getIn().setBody(null); // Eliminar si no cumple
                        }
                    }
                })
                .end();

        // --- RUTA 4: FILTRAR POR EXPERIENCIA ---
        from("direct:getPokemonByExp")
                .routeId("FilterByExpRoute")
                .log("Buscando experiencia mínima: ${header.minExp}")

                // Usamos 'psychic' (psíquico) como base, suelen tener exp variada
                .setHeader("type", constant("psychic"))
                .to("direct:getPokemonByType")

                .split(body(), new ListaAgregar())
                .parallelProcessing()

                .setHeader("pokemonName", simple("${body.name}"))
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))

                .toD("{{pokeapi.base-url}}pokemon/${header.pokemonName}?bridgeEndpoint=true")

                // Paso 1: Convertir a PokemonExp
                .process(pokemonExpProcessor)

                // Paso 2: Filtro manual (Experiencia >= Mínimo)
                .process(exchange -> {
                    PokemonExp item = exchange.getIn().getBody(PokemonExp.class);
                    Integer min = exchange.getIn().getHeader("minExp", Integer.class);

                    if (item != null && min != null) {
                        // Si la experiencia es MENOR al mínimo, borrar
                        if (item.getBaseExperience() < min) {
                            exchange.getIn().setBody(null);
                        }
                    }
                })
                .end();
    }
}
