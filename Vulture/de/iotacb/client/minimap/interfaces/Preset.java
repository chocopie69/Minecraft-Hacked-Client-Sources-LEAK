package de.iotacb.client.minimap.interfaces;

import net.minecraft.client.resources.*;

public class Preset
{
    public int[][] coords;
    public boolean[][] types;
    private String name;
    
    public Preset(final String name, final int[][] coords, final boolean[][] types) {
        this.coords = coords;
        this.types = types;
        this.name = name;
        InterfaceHandler.presets.add(this);
    }
    
    public String getName() {
        return I18n.format(this.name, new Object[0]);
    }
}
