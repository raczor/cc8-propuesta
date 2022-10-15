package project.routing;

import java.util.List;

//Modulo de Aplicacion, Distance Vector
public class MessageJsonRoutingDV extends MessageJsonRoutingBase {
    private List<Vector> vectors;

    public static class Vector {
        private String node;
        private int cost;
    }

}
