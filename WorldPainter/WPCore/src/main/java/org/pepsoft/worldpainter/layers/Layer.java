/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.layers;

import org.pepsoft.util.IconUtils;
import org.pepsoft.util.mdc.MDCCapturingRuntimeException;
import org.pepsoft.util.plugins.PluginManager;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.exporting.LayerExporter;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.layers.groundcover.GroundCoverLayer;
import org.pepsoft.worldpainter.layers.renderers.LayerRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author pepijn
 */
public abstract class Layer implements Serializable, Comparable<Layer> {
    @Deprecated
    protected Layer(String name, String description, DataSize dataSize, boolean discrete, int priority) {
        this(name, name, description, dataSize, discrete, priority, '\0');
    }

    @Deprecated
    protected Layer(String name, String description, DataSize dataSize, boolean discrete, int priority, char mnemonic) {
        this(name, name, description, dataSize, discrete, priority, mnemonic);
    }

    protected Layer(String id, String name, String description, DataSize dataSize, boolean discrete, int priority) {
        this(id, name, description, dataSize, discrete, priority, '\0');
    }
    
    protected Layer(String id, String name, String description, DataSize dataSize, boolean discrete, int priority, char mnemonic) {
        if (id == null) {
            throw new NullPointerException("id");
        }
        if (name == null) {
            throw new NullPointerException("name");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.dataSize = dataSize;
        this.discrete = discrete;
        this.priority = priority;
        this.mnemonic = mnemonic;
        init();
    }
    
    public final DataSize getDataSize() {
        return dataSize;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    /**
     * Return the type of the exporter that {@link #getExporter(Dimension, Platform, ExporterSettings)} will return.
     * If that method is implemented, this method must also be implemented. May be used to examine which phases the
     * exporter requires.
     */
    public Class<? extends LayerExporter> getExporterType() {
        return exporterType;
    }

    /**
     * Create a new exporter for this layer.
     *
     * <p>If this method is implemented, {@link #getExporterType()} must also be implemented to indicate which type this
     * method will return.</p>
     *
     * @param dimension The dimension that is being exported.
     * @param platform  The platform for which the dimension is being exported.
     * @param settings  The configured settings for the layer, if any. May be {@code null}.
     * @return A new exporter for this layer.
     */
    public LayerExporter getExporter(Dimension dimension, Platform platform, ExporterSettings settings) {
        if (exporterType == null) {
            // Layer has no default exporter
            return null;
        } else {
            try {
                Constructor<? extends LayerExporter> exporterConstructor = exporterType.getConstructor(Dimension.class, Platform.class, ExporterSettings.class);
                return exporterConstructor.newInstance(dimension, platform, settings);
            } catch (InstantiationException e) {
                throw new RuntimeException("Instantiation exception while instantiating exporter for layer " + name, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Access denied while instantiating exporter for layer " + name, e);
            } catch (NoSuchMethodException e) {
                throw new MDCCapturingRuntimeException("Exporter class for layer " + name + " is missing a (Dimension, Platform) constructor", e);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof Error) {
                    throw (Error) e.getTargetException();
                } else if (e.getTargetException() instanceof RuntimeException) {
                    throw (RuntimeException) e.getTargetException();
                } else {
                    throw new RuntimeException("Constructor for exporter for layer " + name + " threw " + e.getTargetException().getClass().getSimpleName() + " (message: " + e.getTargetException().getMessage() + ")", e);
                }
            }
        }
    }
    
    public LayerRenderer getRenderer() {
        return renderer;
    }
    
    public BufferedImage getIcon() {
        return icon;
    }
    
    public char getMnemonic() {
        return mnemonic;
    }

    public int getPriority() {
        return priority;
    }

    public String getId() {
        return id;
    }

    /**
     * The default value which will be returned wherever the layer has not been
     * set. By default this is zero (or false).
     * 
     * @return The default value which will be returned where the layer value is
     *     not set.
     */
    public int getDefaultValue() {
        return 0;
    }

    /**
     * Indicates whether this layer should be allowed to be exported to a file. This may not make sense for some layers,
     * e.g. TunnelLayers with a floor dimension.
     */
    public boolean isExportableToFile() {
        return true;
    }

    @Override
    public final boolean equals(Object obj) {
        return (obj instanceof Layer)
            && id.equals(((Layer) obj).id);
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    // Comparable

    @Override
    public final int compareTo(Layer layer) {
        // First sort by index, where layers without an index are sorted last
        if ((this instanceof CustomLayer) && (((CustomLayer) this).getExportIndex() != null)) {
            if ((layer instanceof CustomLayer) && (((CustomLayer) layer).getExportIndex() != null)) {
                // Both layers have an index; sort by index. We assume the indexes are always unique, so we don't bother
                // sorting by another criterion if they are the same
                return Integer.compare(((CustomLayer) this).getExportIndex(), ((CustomLayer) layer).getExportIndex());
            } else {
                // Only we have an index; sort us first
                return -1;
            }
        } else {
            if ((layer instanceof CustomLayer) && (((CustomLayer) layer).getExportIndex() != null)) {
                // Only the other layer has an index; sort us last
                return 1;
            } else {
                // Neither layer has an index; sort using secondary criteria
                if (priority != layer.priority) {
                    return Integer.compare(priority, layer.priority);
                }
                if ((this instanceof GroundCoverLayer) && (layer instanceof GroundCoverLayer)
                        && (Math.abs(((GroundCoverLayer) layer).getThickness()) != Math.abs(((GroundCoverLayer) this).getThickness()))) {
                    // TODO: is this stable?
                    return Math.abs(((GroundCoverLayer) layer).getThickness()) - Math.abs(((GroundCoverLayer) this).getThickness());
                }
                final String palette1 = (this instanceof CustomLayer) ? ((CustomLayer) this).getPalette() : null;
                final String palette2 = (layer instanceof CustomLayer) ? ((CustomLayer) layer).getPalette() : null;
                if (palette1 != null) {
                    if (palette2 != null) {
                        // Both layers have a palette
                        if (! palette1.equals(palette2)) {
                            // And they are different, so sort by name
                            return palette1.compareTo(palette2);
                        } else {
                            // And they are the same, so sort by palette
                            return name.compareTo(layer.name);
                        }
                    } else {
                        // Only we have a palette; sort us first
                        return -1;
                    }
                } else {
                    if (palette2 != null) {
                        // Only the other layer has a palette; sort us last
                        return 1;
                    } else {
                        // Neither layer has a palette; sort by name
                        return name.compareTo(layer.name);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void init() {
        final Class<? extends Layer> clazz = getClass();
        final ClassLoader pluginClassLoader = PluginManager.getPluginClassLoader();
        try {
            try {
                renderer = (LayerRenderer) pluginClassLoader.loadClass(clazz.getPackage().getName() + ".renderers." + clazz.getSimpleName() + "Renderer").newInstance();
            } catch (ClassNotFoundException | InstantiationException e) {
                // This most likely means the class does not exist
                renderer = null;
            }
            try {
                exporterType = (Class<? extends LayerExporter>) pluginClassLoader.loadClass(clazz.getPackage().getName() + ".exporters." + clazz.getSimpleName() + "Exporter");
            } catch (ClassNotFoundException e) {
                // This most likely means the class does not exist
                exporterType = null;
            }
        } catch (IllegalAccessException e) {
            throw new MDCCapturingRuntimeException("Access denied while creating renderer for layer " + name, e);
        }
        icon = IconUtils.loadScaledImage(clazz.getClassLoader(), "org/pepsoft/worldpainter/icons/" + getClass().getSimpleName().toLowerCase() + ".png");
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Legacy
        if (id == null) {
            id = name;
        }
        if ((! discrete) && ((this instanceof Annotations) || (this instanceof Biome) || (this instanceof GardenCategory))) {
            discrete = true;
        }

        init();
    }

    private String name, description;
    public final DataSize dataSize;
    public final int priority;
    protected String id;
    public boolean discrete;
    private final transient char mnemonic;
    private transient LayerRenderer renderer;
    private transient Class<? extends LayerExporter> exporterType;
    private transient BufferedImage icon;

    private static final long serialVersionUID = 2011032901L;

    public enum DataSize {
        BIT(1) {
            @Override
            public String toString(int value) {
                return value == 0 ? "off" : "on";
            }
        },

        NIBBLE(15) {
            @Override
            public String toString(int value) {
                int strength = (value > 0) ? ((value - 1) * 100  / 14 + 1): 0;
                if ((strength == 51) || (strength == 101)) {
                    strength--;
                }
                return strength + "%";
            }
        },

        BYTE(255) {
            @Override
            public String toString(int value) {
                return (value * 100 / 255) + "%";
            }
        },

        BIT_PER_CHUNK(1) {
            @Override
            public String toString(int value) {
                return value == 0 ? "off" : "on";
            }
        },

        NONE(-1) {
            @Override
            public String toString(int value) {
                return "N/A";
            }
        };

        DataSize(int maxValue) {
            this.maxValue = maxValue;
        }

        public abstract String toString(int value);

        public final int maxValue;
    }
}
