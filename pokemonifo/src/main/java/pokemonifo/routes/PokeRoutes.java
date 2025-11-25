package pokemonifo.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pokemonifo.model.PokemonDef;
import pokemonifo.model.PokemonExp;
import pokemonifo.model.PokemonType;
import pokemonifo.model.PokemonWeight;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PokeRoutes extends RouteBuilder {

    @Autowired private PokemonDefProcessor pokemonDefProces;
    @Autowired private PokemonWeightProcessor pokeWeight;
    @Autowired private PokemonExpProcessor pokemonExpProcessor;

    @Override
    public void configure() throws Exception {

        // MANEJO DE ERRORES
        onException(SocketTimeoutException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(504))
                .process(e -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("error", "Timeout");
                    map.put("message", "La PokeAPI tardó demasiado en responder.");
                    e.getIn().setBody(map);
                });

        onException(HttpOperationFailedException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .process(e -> {
                    HttpOperationFailedException ex = e.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                    Map<String, String> map = new HashMap<>();
                    map.put("error", "API Error");
                    map.put("message", "Recurso no encontrado. Status: " + ex.getStatusCode());
                    e.getIn().setBody(map);
                });

        onException(IllegalArgumentException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .process(e -> {
                    String msg = e.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class).getMessage();
                    Map<String, String> map = new HashMap<>();
                    map.put("error", "Petición Inválida");
                    map.put("message", msg);
                    e.getIn().setBody(map);
                });

        // RUTA 1: OBTENER LISTA POR TIPO (Reutilizable)
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
                    exchange.getIn().setBody(listaLimpia);
                });

        // RUTA 2: DEFENSA
        from("direct:getPokemonByDefense")
                .routeId("GetByDefense")
                .process(e -> {
                    Integer min = e.getIn().getHeader("minDefense", Integer.class);
                    if (min != null && min < 0) throw new IllegalArgumentException("La defensa mínima no puede ser negativa.");
                })
                // Ya no fijamos el tipo aquí. Usamos el header "type" que viene del Adapter.
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
                .end();

        // RUTA 3: PESO
        from("direct:getPokemonByWeight")
                .routeId("GetByWeight")
                .process(e -> {
                    Integer min = e.getIn().getHeader("minWeight", Integer.class);
                    Integer max = e.getIn().getHeader("maxWeight", Integer.class);
                    if (min != null && max != null && min > max) throw new IllegalArgumentException("Peso mín > máx.");
                    if (min != null && min < 0) throw new IllegalArgumentException("El peso no puede ser negativo.");
                })
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
                .end();

        // RUTA 4: EXPERIENCIA
        from("direct:getPokemonByExp")
                .routeId("GetByExp")
                .process(e -> {
                    Integer min = e.getIn().getHeader("minExp", Integer.class);
                    if (min != null && min < 0) throw new IllegalArgumentException("La experiencia no puede ser negativa.");
                })
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
                .end();
    }
}