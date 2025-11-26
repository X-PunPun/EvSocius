package pokemonifo.routes;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.ArrayList;
import java.util.List;

public class ListaAgregar implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        List<Object> list;
        Object newBody = newExchange.getIn().getBody();

        if (oldExchange == null) {
            // Creamos la lista aunque el primer elemento sea null.
            list = new ArrayList<>();
            if (newBody != null) {
                list.add(newBody);
            }
            // Retornamos el newExchange con la lista iniciada
            newExchange.getIn().setBody(list);
            return newExchange;
        } else {
            // Recuperamos la lista acumulada y agregamos si no es null
            list = oldExchange.getIn().getBody(List.class);
            if (newBody != null) {
                list.add(newBody);
            }
            return oldExchange;
        }
    }
}