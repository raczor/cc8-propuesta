package project;

import java.util.List;

public class MessageJsonRoutingDV extends MessageJsonRoutingBase {
    private List<Vector> vectors;

    public static class Vector {
        private String node;
        private int cost;
    }

}
