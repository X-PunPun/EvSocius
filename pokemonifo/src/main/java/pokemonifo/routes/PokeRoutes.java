package pokemonifo.routes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import pokemonifo.model.PokemonInfo;

import java.util.ArrayList;
import java.util.List;

@Component
public class PokeRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Ruta para obtener Pokémon por Tipo
        from("direct:getPokemonByType")
                .routeId("GetPokemonByTypeRoute")
                .log("Consultando PokeAPI para tipo: ${header.type}")

                // Limpiamos headers HTTP anteriores para evitar conflictos
                .removeHeaders("CamelHttp*")

                // Configuramos el método GET
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))

                // Llamada Dinámica (toD) requerida por el PDF
                // bridgeEndpoint=true ayuda a pasar la petición correctamente
                .toD("{{pokeapi.base-url}}type/${header.type}?bridgeEndpoint=true")

                // Processor: Transforma el JSON feo de la API a tu lista limpia
                .process(exchange -> {
                    String jsonRaw = exchange.getIn().getBody(String.class);

                    // Usamos Jackson para leer el árbol JSON
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
                    // Guardamos la lista limpia en el cuerpo del mensaje
                    exchange.getIn().setBody(listaLimpia);
                });
    }
}
