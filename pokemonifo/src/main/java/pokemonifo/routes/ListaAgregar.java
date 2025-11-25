package pokemonifo.routes;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.ArrayList;
import java.util.List;

public class ListaAgregar implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        List<Object> list;

        // 1. Si el nuevo intercambio tiene el cuerpo NULL, LO IGNORAMOS y devolvemos lo que ya teníamos
        if (newExchange.getIn().getBody() == null) {
            return oldExchange;
        }

        if (oldExchange == null) {
            list = new ArrayList<>();
            list.add(newExchange.getIn().getBody());
            newExchange.getIn().setBody(list);
            return newExchange;
        } else {
            list = oldExchange.getIn().getBody(List.class);
            list.add(newExchange.getIn().getBody());
            return oldExchange;
        }
    }

}
