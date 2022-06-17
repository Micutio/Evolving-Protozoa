package biology.genes;

import core.Settings;

import java.io.Serializable;

public class ProtozoaMaxTurnGene extends BoundedFloatGene implements Serializable {

    public ProtozoaMaxTurnGene() {
        super((float) Math.toRadians(3), (float) Math.toRadians(15));
    }

    public ProtozoaMaxTurnGene(Float value) {
        super((float) Math.toRadians(3), (float) Math.toRadians(15), value);
    }

    @Override
    public <G extends Gene<Float>> G createNew(Float value) {
        return (G) new ProtozoaMaxTurnGene(value);
    }
}

