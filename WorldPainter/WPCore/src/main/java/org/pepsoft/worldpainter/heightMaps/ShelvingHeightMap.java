package org.pepsoft.worldpainter.heightMaps;

import org.pepsoft.util.IconUtils;
import org.pepsoft.worldpainter.HeightMap;

import javax.swing.*;

/**
 * Created by pepijn on 28-3-16.
 */
public class ShelvingHeightMap extends DelegatingHeightMap {
    public ShelvingHeightMap(HeightMap baseHeightMap) {
        super("baseHeightMap");
        setHeightMap(0, baseHeightMap);
    }

    public int getShelveHeight() {
        return shelveHeight;
    }

    public void setShelveHeight(int shelveHeight) {
        this.shelveHeight = shelveHeight;
    }

    public int getShelveStrength() {
        return shelveStrength;
    }

    public void setShelveStrength(int shelveStrength) {
        this.shelveStrength = shelveStrength;
    }

    // HeightMap

    @Override
    public double doGetHeight(float x, float y) {
        double value = children[0].getHeight(x, y);
        return value - Math.sin(value * DOUBLE_PI / shelveHeight) * shelveStrength;
    }

    @Override
    public Icon getIcon() {
        return ICON_SHELVING_HEIGHTMAP;
    }

    @Override
    public double[] getRange() {
        double[] range0 = children[0].getRange();
        // TODO this is wrong! It should return two values:
        return new double[] {range0[1] - Math.sin(range0[1] * DOUBLE_PI / shelveHeight) * shelveStrength};
    }

    private int shelveHeight = 32, shelveStrength = 8;

    private static final long serialVersionUID = 1L;
    private static final double DOUBLE_PI = Math.PI * 2;
    private static final Icon ICON_SHELVING_HEIGHTMAP = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/shelving.png");
}