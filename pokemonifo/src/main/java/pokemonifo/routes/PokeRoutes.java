package pokemonifo.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pokemonifo.exception.PokemonNotFoundException;
import pokemonifo.model.PokemonDef;
import pokemonifo.model.PokemonExp;
import pokemonifo.model.PokemonType;
import pokemonifo.model.PokemonWeight;

import java.util.ArrayList;
import java.util.List;

@Component
public class PokeRoutes extends RouteBuilder {

    @Autowired private PokemonDefProcessor pokemonDefProces;
    @Autowired private PokemonWeightProcessor pokeWeight;
    @Autowired private PokemonExpProcessor pokemonExpProcessor;

    @Override
    public void configure() throws Exception {

        // RUTA 1 busca por TIPO
        from("direct:getPokemonByType")
                .routeId("GetByType")
                .log("Consultando API externa para el tipo: ${header.type}")
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .toD("{{pokeapi.base-url}}type/${header.type}?bridgeEndpoint=true")
                .process(exchange -> {
                    String jsonRaw = exchange.getIn().getBody(String.class);
                    String typeName = exchange.getIn().getHeader("type", String.class);
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(jsonRaw);
                    JsonNode pokemonArray = root.path("pokemon");
                    List<PokemonType> listaLimpia = new ArrayList<>();
                    if (pokemonArray.isArray()) {
                        for (JsonNode node : pokemonArray) {
                            JsonNode pData = node.path("pokemon");
                            listaLimpia.add(new PokemonType(pData.path("name").asText(), typeName));
                        }
                    }
                    // VALIDACIÓN DE VACÍO  404
                    if (listaLimpia.isEmpty()) {
                        throw new PokemonNotFoundException("No se encontraron Pokemones para el tipo: " + typeName);
                    }
                    exchange.getIn().setBody(listaLimpia);
                });

        // RUTA 2 busca por DEFENSA
        from("direct:getPokemonByDefense")
                .routeId("GetByDefense")
                .to("direct:getPokemonByType")
                .split(body(), new ListaAgregar()).parallelProcessing()
                .setHeader("pokemonName", simple("${body.name}"))
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .toD("{{pokeapi.base-url}}pokemon/${header.pokemonName}?bridgeEndpoint=true")
                .process(pokemonDefProces)
                .process(e -> {
                    PokemonDef p = e.getIn().getBody(PokemonDef.class);
                    Integer min = e.getIn().getHeader("minDefense", Integer.class);
                    if (p != null && min != null && p.getDefense() < min) {
                        e.getIn().setBody(null);
                    }
                })
                .end()

                // VALIDACIÓN DE VACÍO FINAL  404
                .process(e -> {
                    List<?> list = e.getIn().getBody(List.class);
                    if (list == null || list.isEmpty()) {
                        throw new PokemonNotFoundException("Ningún Pokémon cumple con la defensa mínima solicitada.");
                    }
                });

        // RUTA 3 busca por PESO
        from("direct:getPokemonByWeight")
                .routeId("GetByWeight")
                .to("direct:getPokemonByType")
                .split(body(), new ListaAgregar()).parallelProcessing()
                .setHeader("pokemonName", simple("${body.name}"))
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .toD("{{pokeapi.base-url}}pokemon/${header.pokemonName}?bridgeEndpoint=true")
                .process(pokeWeight)
                .process(e -> {
                    PokemonWeight p = e.getIn().getBody(PokemonWeight.class);
                    Integer min = e.getIn().getHeader("minWeight", Integer.class);
                    Integer max = e.getIn().getHeader("maxWeight", Integer.class);
                    if (p != null && min != null && max != null) {
                        if (p.getWeight() < min || p.getWeight() > max) e.getIn().setBody(null);
                    }
                })
                .end()

                // VALIDACIÓN DE VACÍO FINAL  404
                .process(e -> {
                    List<?> list = e.getIn().getBody(List.class);
                    if (list == null || list.isEmpty()) {
                        throw new PokemonNotFoundException("Ningún Pokémon se encuentra en ese rango de peso.");
                    }
                });

        // RUTA 4: EXPERIENCIA
        from("direct:getPokemonByExp")
                .routeId("GetByExp")
                .to("direct:getPokemonByType")
                .split(body(), new ListaAgregar()).parallelProcessing()
                .setHeader("pokemonName", simple("${body.name}"))
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .toD("{{pokeapi.base-url}}pokemon/${header.pokemonName}?bridgeEndpoint=true")
                .process(pokemonExpProcessor)
                .process(e -> {
                    PokemonExp p = e.getIn().getBody(PokemonExp.class);
                    Integer min = e.getIn().getHeader("minExp", Integer.class);
                    if (p != null && min != null && p.getBaseExperience() < min) {
                        e.getIn().setBody(null);
                    }
                })
                .end()

                // VALIDACIÓN DE VACÍO FINAL 404
                .process(e -> {
                    List<?> list = e.getIn().getBody(List.class);
                    if (list == null || list.isEmpty()) {
                        throw new PokemonNotFoundException("Ningún Pokémon tiene la experiencia base mínima solicitada.");
                    }
                });
    }
}