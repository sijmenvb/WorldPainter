/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ExportWorldDialog.java
 *
 * Created on Mar 29, 2011, 5:09:50 PM
 */

package org.pepsoft.worldpainter;

import org.pepsoft.minecraft.CustomGenerator;
import org.pepsoft.minecraft.SeededGenerator;
import org.pepsoft.minecraft.SuperflatGenerator;
import org.pepsoft.minecraft.SuperflatPreset;
import org.pepsoft.util.DesktopUtils;
import org.pepsoft.util.IconUtils;
import org.pepsoft.worldpainter.biomeschemes.CustomBiomeManager;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.Populate;
import org.pepsoft.worldpainter.plugins.PlatformManager;
import org.pepsoft.worldpainter.superflat.EditSuperflatPresetDialog;
import org.pepsoft.worldpainter.util.EnumListCellRenderer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static java.util.stream.Collectors.toSet;
import static org.pepsoft.minecraft.Constants.DIFFICULTY_HARD;
import static org.pepsoft.minecraft.Constants.DIFFICULTY_PEACEFUL;
import static org.pepsoft.worldpainter.Constants.*;
import static org.pepsoft.worldpainter.DefaultPlugin.*;
import static org.pepsoft.worldpainter.GameType.*;
import static org.pepsoft.worldpainter.Generator.CUSTOM;
import static org.pepsoft.worldpainter.Generator.DEFAULT;
import static org.pepsoft.worldpainter.Platform.Capability.NAME_BASED;
import static org.pepsoft.worldpainter.Platform.Capability.POPULATE;
import static org.pepsoft.worldpainter.util.BackupUtils.cleanUpBackups;
import static org.pepsoft.worldpainter.util.MaterialUtils.gatherBlocksWithoutIds;

/**
 *
 * @author pepijn
 */
public class ExportWorldDialog extends WorldPainterDialog {
    /** Creates new form ExportWorldDialog */
    public ExportWorldDialog(java.awt.Frame parent, World2 world, ColourScheme colourScheme, CustomBiomeManager customBiomeManager, Collection<Layer> hiddenLayers, boolean contourLines, int contourSeparation, TileRenderer.LightOrigin lightOrigin, WorldPainter view) {
        super(parent);
        this.world = world;
        selectedTiles = world.getTilesToExport();
        selectedDimension = (selectedTiles != null) ? world.getDimensionsToExport().iterator().next() : DIM_NORMAL;
        final Dimension dim0 = world.getDimension(0);
        dim0GeneratorName = (dim0.getGenerator() instanceof CustomGenerator) ? (((CustomGenerator) dim0.getGenerator()).getName()) : null;
        dim0SuperflatPreset = (dim0.getGenerator() instanceof SuperflatGenerator) ? (((SuperflatGenerator) dim0.getGenerator()).getSettings()) : null;
        this.colourScheme = colourScheme;
        this.hiddenLayers = hiddenLayers;
        this.contourLines = contourLines;
        this.contourSeparation = contourSeparation;
        this.lightOrigin = lightOrigin;
        this.customBiomeManager = customBiomeManager;
        this.view = view;
        initComponents();

        Configuration config = Configuration.getInstance();
        if (config.isEasyMode()) {
            jLabel4.setVisible(false);
            comboBoxGenerator.setVisible(false);
            buttonGeneratorOptions.setVisible(false);
            checkBoxMapFeatures.setVisible(false);

            jLabel1.setVisible(false);
            comboBoxMinecraftVersion.setVisible(false);
        }

        Platform platform = world.getPlatform();
        if (config.getExportDirectory(platform) != null) {
            fieldDirectory.setText(config.getExportDirectory(platform).getAbsolutePath());
        } else {
            File exportDir = PlatformManager.getInstance().getDefaultExportDir(platform);
            if (exportDir != null) {
                fieldDirectory.setText(exportDir.getAbsolutePath());
            } else {
                fieldDirectory.setText(DesktopUtils.getDocumentsFolder().getAbsolutePath());
            }
        }
        fieldName.setText(world.getName());

        nameOnlyMaterials = gatherBlocksWithoutIds(world, platform);

        surfacePropertiesEditor.setColourScheme(colourScheme);
        surfacePropertiesEditor.setMode(DimensionPropertiesEditor.Mode.EXPORT);
        surfacePropertiesEditor.setDimension(dim0);
        surfacePropertiesEditor.addBorderListener(this::borderChanged);
        dimensionPropertiesEditors.put(DIM_NORMAL, surfacePropertiesEditor);
        if (world.getDimension(DIM_NETHER) != null) {
            netherPropertiesEditor.setColourScheme(colourScheme);
            netherPropertiesEditor.setMode(DimensionPropertiesEditor.Mode.EXPORT);
            netherPropertiesEditor.setDimension(world.getDimension(DIM_NETHER));
            dimensionPropertiesEditors.put(DIM_NETHER, netherPropertiesEditor);
        } else {
            jTabbedPane1.setEnabledAt(2, false);
        }
        if (world.getDimension(DIM_END) != null) {
            endPropertiesEditor.setColourScheme(colourScheme);
            endPropertiesEditor.setMode(DimensionPropertiesEditor.Mode.EXPORT);
            endPropertiesEditor.setDimension(world.getDimension(DIM_END));
            dimensionPropertiesEditors.put(DIM_END, endPropertiesEditor);
        } else {
            jTabbedPane1.setEnabledAt(4, false);
        }
        if (world.getDimension(DIM_NORMAL_CEILING) != null) {
            surfaceCeilingPropertiesEditor.setColourScheme(colourScheme);
            surfaceCeilingPropertiesEditor.setMode(DimensionPropertiesEditor.Mode.EXPORT);
            surfaceCeilingPropertiesEditor.setDimension(world.getDimension(DIM_NORMAL_CEILING));
            dimensionPropertiesEditors.put(DIM_NORMAL_CEILING, surfaceCeilingPropertiesEditor);
        } else {
            jTabbedPane1.setEnabledAt(1, false);
        }
        if (world.getDimension(DIM_NETHER_CEILING) != null) {
            netherCeilingPropertiesEditor.setColourScheme(colourScheme);
            netherCeilingPropertiesEditor.setMode(DimensionPropertiesEditor.Mode.EXPORT);
            netherCeilingPropertiesEditor.setDimension(world.getDimension(DIM_NETHER_CEILING));
            dimensionPropertiesEditors.put(DIM_NETHER_CEILING, netherCeilingPropertiesEditor);
        } else {
            jTabbedPane1.setEnabledAt(3, false);
        }
        if (world.getDimension(DIM_END_CEILING) != null) {
            endCeilingPropertiesEditor.setColourScheme(colourScheme);
            endCeilingPropertiesEditor.setMode(DimensionPropertiesEditor.Mode.EXPORT);
            endCeilingPropertiesEditor.setDimension(world.getDimension(DIM_END_CEILING));
            dimensionPropertiesEditors.put(DIM_END_CEILING, endCeilingPropertiesEditor);
        } else {
            jTabbedPane1.setEnabledAt(5, false);
        }
        checkBoxGoodies.setSelected(world.isCreateGoodiesChest());
        List<Platform> availablePlatforms = PlatformManager.getInstance().getAllPlatforms();
        Set<Platform> incompatiblePlatforms = availablePlatforms.stream().filter(availablePlatform -> {
            if (! availablePlatform.isCompatible(world)) {
                return false;
            } else {
                Map<String, Set<String>> namedBlocks = gatherBlocksWithoutIds(world, availablePlatform);
                return (! namedBlocks.isEmpty()) && (! availablePlatform.capabilities.contains(NAME_BASED));
            }

        }).collect(toSet());
        comboBoxMinecraftVersion.setToolTipText("Only map formats compatible with this world are displayed");
        comboBoxMinecraftVersion.setModel(new DefaultComboBoxModel<>(availablePlatforms.toArray(new Platform[availablePlatforms.size()])));
        comboBoxMinecraftVersion.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Platform) {
                    if (! incompatiblePlatforms.isEmpty()) {
                        if (incompatiblePlatforms.contains(value)) {
                            setIcon(WARNING_ICON);
                        } else {
                            setIcon(SPACER_ICON);
                        }
                    } else {
                        setIcon(SPACER_ICON);
                    }
                    setText(((Platform) value).displayName);
                } else {
                    setIcon(SPACER_ICON);
                }
                return this;
            }
        });
        comboBoxMinecraftVersion.setSelectedItem(platform);
        if (availablePlatforms.size() < 2) {
            comboBoxMinecraftVersion.setEnabled(false);
        }
        comboBoxGenerator.setModel(new DefaultComboBoxModel<>(platform.supportedGenerators.toArray(new Generator[platform.supportedGenerators.size()])));
        comboBoxGenerator.setSelectedItem(dim0.getGenerator().getType());
        updateGeneratorButtonTooltip();
        comboBoxGenerator.setEnabled(comboBoxGenerator.getItemCount() > 1);
        comboBoxGenerator.setRenderer(new EnumListCellRenderer());
        comboBoxGameType.setModel(new DefaultComboBoxModel<>(platform.supportedGameTypes.toArray(new GameType[platform.supportedGameTypes.size()])));
        comboBoxGameType.setSelectedItem(world.getGameType());
        comboBoxGameType.setEnabled(comboBoxGameType.getItemCount() > 1);
        comboBoxGameType.setRenderer(new EnumListCellRenderer());
        checkBoxAllowCheats.setSelected(world.isAllowCheats());
        if (selectedTiles != null) {
            radioButtonExportSelection.setText("export " + selectedTiles.size() + " selected tiles");
            radioButtonExportSelection.setSelected(true);
        }
        checkBoxMapFeatures.setSelected(world.isMapFeatures());
        comboBoxDifficulty.setSelectedIndex(world.getDifficulty());
        borderChanged(dim0.getBorder());

        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setControlStates();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setControlStates();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setControlStates();
            }
        };
        fieldDirectory.getDocument().addDocumentListener(documentListener);
        fieldName.getDocument().addDocumentListener(documentListener);

        disableDisabledLayersWarning = true;
dims:   for (Dimension dim: world.getDimensions()) {
            for (CustomLayer customLayer: dim.getCustomLayers()) {
                if (! customLayer.isExport()) {
                    disableDisabledLayersWarning = false;
                    break dims;
                }
            }
        }

        rootPane.setDefaultButton(buttonExport);

        setControlStates();

        scaleToUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void borderChanged(Dimension.Border border) {
        endlessBorder = (border != null) && border.isEndless();
        if (endlessBorder && comboBoxGenerator.isEnabled()) {
            savedGenerator = comboBoxGenerator.getSelectedIndex();
            comboBoxGenerator.setSelectedIndex(1);
            comboBoxGenerator.setEnabled(false);
            savedMapFeatures = checkBoxMapFeatures.isSelected();
            checkBoxMapFeatures.setSelected(false);
//            setControlStates();
        } else if ((! endlessBorder) && (! comboBoxGenerator.isEnabled())) {
            comboBoxGenerator.setSelectedIndex(savedGenerator);
            comboBoxGenerator.setEnabled(true);
            checkBoxMapFeatures.setSelected(savedMapFeatures);
//            setControlStates();
        }
    }

    /**
     * Check whether a platform is compatible with the loaded world. If not,
     * reports the reason to the user with a popup and returns {@code false},
     * otherwise returns {@code true}.
     *
     * @param platform The platform to check for compatibility.
     * @return {@code true} is the platform is compatible with the loaded world.
     */
    private boolean checkCompatibility(Platform platform) {
        if ((! nameOnlyMaterials.isEmpty()) && (! platform.capabilities.contains(NAME_BASED))) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append("<p>The world cannot be exported in format ").append(platform.displayName).append(" because it contains the following incompatible block types:");
            sb.append("<table><tr><th align='left'>Block Type</th><th align='left'>Source</th></tr>");
            nameOnlyMaterials.forEach((name, sources) ->
                    sb.append("<tr><td>").append(name).append("</td><td>").append(String.join(",", sources)).append("</td></tr>"));
            sb.append("</table>");
            DesktopUtils.beep();
            JOptionPane.showMessageDialog(this, sb.toString(), "Map Format Not Compatible", JOptionPane.ERROR_MESSAGE);
            comboBoxMinecraftVersion.requestFocusInWindow();
            return false;
        }
        if (! platform.isCompatible(world)) {
            DesktopUtils.beep();
            JOptionPane.showMessageDialog(this, String.format(/* language=HTML */ "<html>" +
                    "<p>The world cannot be exported in format %s because it is not compatible, for one of these reasons:" +
                    "<ul><li>The format does not support the world depth of %d blocks" +
                    "<li>The format does not support the world height of %d blocks" +
                    "<li>The format does not support one or more of the dimensions in this world" +
                    "</ul>" +
                    "</html>", platform.displayName, -world.getPlatform().minZ, world.getMaxHeight()), "Map Format Not Compatible", JOptionPane.ERROR_MESSAGE);
            comboBoxMinecraftVersion.requestFocusInWindow();
            return false;
        }
        return true;
    }

    private void export() {
        // Check for errors
        if (! new File(fieldDirectory.getText().trim()).isDirectory()) {
            fieldDirectory.requestFocusInWindow();
            DesktopUtils.beep();
            JOptionPane.showMessageDialog(this, "The selected output directory does not exist or is not a directory.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (fieldName.getText().trim().isEmpty()) {
            fieldName.requestFocusInWindow();
            DesktopUtils.beep();
            JOptionPane.showMessageDialog(this, "You have not specified a name for the map.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((! radioButtonExportEverything.isSelected()) && ((selectedTiles == null) || selectedTiles.isEmpty())) {
            radioButtonExportEverything.requestFocusInWindow();
            DesktopUtils.beep();
            JOptionPane.showMessageDialog(this, "No tiles have been selected for export.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ((comboBoxGenerator.getSelectedItem() == CUSTOM) && ((dim0GeneratorName == null) || dim0GeneratorName.trim().isEmpty())) {
            buttonGeneratorOptions.requestFocusInWindow();
            DesktopUtils.beep();
            JOptionPane.showMessageDialog(this, "The custom world generator name has not been set.\nUse the [...] button to set it.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Platform platform = (Platform) comboBoxMinecraftVersion.getSelectedItem();
        if (! checkCompatibility(platform)) {
            return;
        }

        // Check for warnings
        StringBuilder sb = new StringBuilder("<html>Please confirm that you want to export the world<br>notwithstanding the following warnings:<br><ul>");
        boolean showWarning = false;
        Configuration config = Configuration.getInstance();
        if ((platform == JAVA_ANVIL_1_18) && (! config.isBeta118WarningDisplayed())) {
            sb.append("<li><strong>Minecraft 1.18 support is still in preview!</strong><br>" +
                    "Be careful and keep backups. If you encounter<br>" +
                    "problems, please report them on GitHub:<br>" +
                    "https://www.worldpainter.net/issues<br>" +
                    "This warning will only be displayed once.");
            showWarning = true;
        }
        Generator generatorType = Generator.values()[comboBoxGenerator.getSelectedIndex()];
        final Dimension dim0 = world.getDimension(DIM_NORMAL);
        if ((surfacePropertiesEditor.isPopulateSelected()
                || dim0.getAllLayers(true).contains(Populate.INSTANCE))
                && (! platform.capabilities.contains(POPULATE))) {
            sb.append("<li>Population and not supported for<br>map format " + platform.displayName + "; it will not have an effect");
            showWarning = true;
        } else if ((! radioButtonExportSelection.isSelected()) || (selectedDimension == DIM_NORMAL)) {
            // The surface dimension is going to be exported
            if ((generatorType == Generator.FLAT) && (surfacePropertiesEditor.isPopulateSelected() || dim0.getAllLayers(true).contains(Populate.INSTANCE))) {
                sb.append("<li>The Superflat world type is selected and Populate is in use.<br>Minecraft will <em>not</em> populate generated chunks for Superflat maps.");
                showWarning = true;
            }
        }
        if (! platform.supportedGenerators.contains(generatorType)) {
            sb.append("<li>Map format " + platform.displayName + " does not support world type " + generatorType.getDisplayName() + ".<br>The world type will be reset to " + platform.supportedGenerators.get(0).getDisplayName() + ".");
            generatorType = platform.supportedGenerators.get(0);
            showWarning = true;
        }
        if (radioButtonExportSelection.isSelected()) {
            if (selectedDimension == DIM_NORMAL) {
                boolean spawnInSelection = false;
                Point spawnPoint = world.getSpawnPoint();
                for (Point tile: selectedTiles) {
                    if ((spawnPoint.x >= (tile.x << 7)) && (spawnPoint.x < ((tile.x + 1) << 7)) && (spawnPoint.y >= (tile.y << 7)) && (spawnPoint.y < ((tile.y + 1) << 7))) {
                        spawnInSelection = true;
                        break;
                    }
                }
                if (! spawnInSelection) {
                    sb.append("<li>The spawn point is not inside the selected area.");
                    showWarning = true;
                }
            }
            String dim;
            switch (selectedDimension) {
                case DIM_NORMAL:
                    dim = "Surface";
                    break;
                case DIM_NETHER:
                    dim = "Nether";
                    break;
                case DIM_END:
                    dim = "End";
                    break;
                default:
                    throw new InternalError();
            }
            sb.append("<li>A tile selection is active! Only " + selectedTiles.size() + " tiles of the<br>" + dim + " dimension are going to be exported.");
            showWarning = showWarning || (! disableTileSelectionWarning);
        }
        int disabledLayerCount = 0;
        for (Dimension dimension: world.getDimensions()) {
            for (CustomLayer customLayer: dimension.getCustomLayers()) {
                if (! customLayer.isExport()) {
                    disabledLayerCount++;
                }
            }
        }
        if (disabledLayerCount > 0) {
            if (disabledLayerCount == 1) {
                sb.append("<li>There are disabled custom layers!<br>One layer is not going to be exported.");
            } else {
                sb.append("<li>There are disabled custom layers!<br>" + disabledLayerCount + " layers are not going to be exported.");
            }
            showWarning = showWarning || (! disableDisabledLayersWarning);
        }
        sb.append("</ul>Do you want to continue with the export?</html>");
        if (showWarning) {
            DesktopUtils.beep();
            if (JOptionPane.showConfirmDialog(this, sb.toString(), "Review Warnings", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }
        }

        File baseDir = new File(fieldDirectory.getText().trim());
        String name = fieldName.getText().trim();

        // Make sure the minimum free disk space is met
        try {
            if (! cleanUpBackups(baseDir, null)) {
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException("I/O error while cleaning backups", e);
        }

        if (! surfacePropertiesEditor.saveSettings()) {
            jTabbedPane1.setSelectedIndex(0);
            return;
        }

        if (world.getDimension(DIM_NETHER) != null) {
            if (! netherPropertiesEditor.saveSettings()) {
                jTabbedPane1.setSelectedIndex(2);
                return;
            }
        }
        if (world.getDimension(DIM_END) != null) {
            if (! endPropertiesEditor.saveSettings()) {
                jTabbedPane1.setSelectedIndex(4);
                return;
            }
        }
        if (world.getDimension(DIM_NORMAL_CEILING) != null) {
            if (! surfaceCeilingPropertiesEditor.saveSettings()) {
                jTabbedPane1.setSelectedIndex(1);
                return;
            }
        }
        if (world.getDimension(DIM_NETHER_CEILING) != null) {
            if (! netherCeilingPropertiesEditor.saveSettings()) {
                jTabbedPane1.setSelectedIndex(3);
                return;
            }
        }
        if (world.getDimension(DIM_END_CEILING) != null) {
            if (! endCeilingPropertiesEditor.saveSettings()) {
                jTabbedPane1.setSelectedIndex(5);
                return;
            }
        }
        world.setPlatform(platform);
        world.setCreateGoodiesChest(checkBoxGoodies.isSelected());
        world.setGameType((GameType) comboBoxGameType.getSelectedItem());
        world.setAllowCheats(checkBoxAllowCheats.isSelected());
        if (! endlessBorder) {
            switch (generatorType) {
                case FLAT:
                    dim0.setGenerator(new SuperflatGenerator(dim0SuperflatPreset));
                    break;
                case DEFAULT:
                case LARGE_BIOMES:
                case BUFFET:
                case CUSTOMIZED:
                    dim0.setGenerator(new SeededGenerator(generatorType, dim0.getMinecraftSeed()));
                    break;
                case CUSTOM:
                    dim0.setGenerator(new CustomGenerator(dim0GeneratorName.trim()));
                    break;
                case UNKNOWN:
                    // Do nothing
                    break;
            }
            world.setMapFeatures(checkBoxMapFeatures.isSelected());
        }
        if (radioButtonExportEverything.isSelected()) {
            world.setDimensionsToExport(null);
            world.setTilesToExport(null);
        } else {
            world.setDimensionsToExport(Collections.singleton(selectedDimension));
            world.setTilesToExport(selectedTiles);
        }
        world.setDifficulty(comboBoxDifficulty.getSelectedIndex());
        
        fieldDirectory.setEnabled(false);
        fieldName.setEnabled(false);
        buttonSelectDirectory.setEnabled(false);
        buttonExport.setEnabled(false);
        buttonCancel.setEnabled(false);
        surfacePropertiesEditor.setEnabled(false);
        netherPropertiesEditor.setEnabled(false);
        endPropertiesEditor.setEnabled(false);
        checkBoxGoodies.setEnabled(false);
        comboBoxGameType.setEnabled(false);
        checkBoxAllowCheats.setEnabled(false);
        comboBoxGenerator.setEnabled(false);
        comboBoxMinecraftVersion.setEnabled(false);
        radioButtonExportEverything.setEnabled(false);
        radioButtonExportSelection.setEnabled(false);
        labelSelectTiles.setForeground(null);
        labelSelectTiles.setCursor(null);
        checkBoxMapFeatures.setEnabled(false);
        comboBoxDifficulty.setEnabled(false);

        config.setExportDirectory(world.getPlatform(), baseDir);
        if (platform == JAVA_ANVIL_1_18) {
            config.setBeta118WarningDisplayed(true);
        }

        ExportProgressDialog dialog = new ExportProgressDialog(this, world, baseDir, name);
        view.setInhibitUpdates(true);
        try {
            dialog.setVisible(true);
        } finally {
            view.setInhibitUpdates(false);
        }
        ok();
    }

    private void setControlStates() {
        boolean notHardcore = comboBoxGameType.getSelectedItem() != HARDCORE;
        Platform platform = (Platform) comboBoxMinecraftVersion.getSelectedItem();
        if (radioButtonExportSelection.isSelected()) {
            labelSelectTiles.setForeground(Color.BLUE);
            labelSelectTiles.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            labelSelectTiles.setForeground(null);
            labelSelectTiles.setCursor(null);
        }
        checkBoxAllowCheats.setEnabled((comboBoxMinecraftVersion.getSelectedItem() != JAVA_MCREGION) && notHardcore);
        buttonGeneratorOptions.setEnabled((! endlessBorder) && ((comboBoxGenerator.getSelectedItem() == Generator.FLAT) || (comboBoxGenerator.getSelectedItem() == CUSTOM)));
        comboBoxDifficulty.setEnabled(notHardcore);
    }

    private void selectDir() {
        // Can't use FileUtils.selectFileForOpen() since it doesn't support
        // selecting a directory
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fieldDirectory.getText().trim()));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fieldDirectory.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void selectTiles() {
        if (radioButtonExportSelection.isSelected()) {
            ExportTileSelectionDialog dialog = new ExportTileSelectionDialog(this, world, selectedDimension, selectedTiles, colourScheme, customBiomeManager, hiddenLayers, contourLines, contourSeparation, lightOrigin);
            dialog.setVisible(true);
            selectedDimension = dialog.getSelectedDimension();
            selectedTiles = dialog.getSelectedTiles();
            radioButtonExportSelection.setText("export " + selectedTiles.size() + " selected tiles");
            setControlStates();
            disableTileSelectionWarning = true;
        }
    }

    private void updateGeneratorButtonTooltip() {
        switch ((Generator) comboBoxGenerator.getSelectedItem()) {
            case FLAT:
                buttonGeneratorOptions.setToolTipText("Edit the Superflat mode preset");
                break;
            case CUSTOM:
                buttonGeneratorOptions.setToolTipText("Set the custom world generator name");
                break;
            default:
                buttonGeneratorOptions.setToolTipText(null);
                break;
        }
    }

    // Change the line adding jTabbedPane1 to the vertical group to:
    //
    //     .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
    //
    // in order to fix a bug where the tabbed pane is made much too tall.

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel2 = new javax.swing.JLabel();
        fieldDirectory = new javax.swing.JTextField();
        buttonSelectDirectory = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        fieldName = new javax.swing.JTextField();
        buttonCancel = new javax.swing.JButton();
        buttonExport = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        surfacePropertiesEditor = new org.pepsoft.worldpainter.DimensionPropertiesEditor();
        surfaceCeilingPropertiesEditor = new org.pepsoft.worldpainter.DimensionPropertiesEditor();
        netherPropertiesEditor = new org.pepsoft.worldpainter.DimensionPropertiesEditor();
        netherCeilingPropertiesEditor = new org.pepsoft.worldpainter.DimensionPropertiesEditor();
        endPropertiesEditor = new org.pepsoft.worldpainter.DimensionPropertiesEditor();
        endCeilingPropertiesEditor = new org.pepsoft.worldpainter.DimensionPropertiesEditor();
        checkBoxGoodies = new javax.swing.JCheckBox();
        comboBoxMinecraftVersion = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        radioButtonExportEverything = new javax.swing.JRadioButton();
        radioButtonExportSelection = new javax.swing.JRadioButton();
        labelSelectTiles = new javax.swing.JLabel();
        checkBoxAllowCheats = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        comboBoxGenerator = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        comboBoxGameType = new javax.swing.JComboBox<>();
        buttonGeneratorOptions = new javax.swing.JButton();
        checkBoxMapFeatures = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        comboBoxDifficulty = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Exporting");

        jLabel2.setText("Directory:");

        fieldDirectory.setText("jTextField1");

        buttonSelectDirectory.setText("...");
        buttonSelectDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelectDirectoryActionPerformed(evt);
            }
        });

        jLabel3.setText("Name:");

        fieldName.setText("jTextField2");

        buttonCancel.setText("Cancel");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonExport.setText("Export");
        buttonExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonExportActionPerformed(evt);
            }
        });

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.RIGHT);
        jTabbedPane1.addTab("Surface", surfacePropertiesEditor);
        jTabbedPane1.addTab("Surface Ceiling", surfaceCeilingPropertiesEditor);
        jTabbedPane1.addTab("Nether", netherPropertiesEditor);
        jTabbedPane1.addTab("Nether Ceiling", netherCeilingPropertiesEditor);
        jTabbedPane1.addTab("End", endPropertiesEditor);
        jTabbedPane1.addTab("End Ceiling", endCeilingPropertiesEditor);

        checkBoxGoodies.setSelected(true);
        checkBoxGoodies.setText("Include chest of goodies");
        checkBoxGoodies.setToolTipText("Include a chest with tools and resources near spawn for you as the level designer");

        comboBoxMinecraftVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxMinecraftVersionActionPerformed(evt);
            }
        });

        jLabel1.setText("Map format:");

        buttonGroup2.add(radioButtonExportEverything);
        radioButtonExportEverything.setSelected(true);
        radioButtonExportEverything.setText("export everything");
        radioButtonExportEverything.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonExportEverythingActionPerformed(evt);
            }
        });

        buttonGroup2.add(radioButtonExportSelection);
        radioButtonExportSelection.setText("export selected tiles");
        radioButtonExportSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonExportSelectionActionPerformed(evt);
            }
        });

        labelSelectTiles.setText("<html><u>select tiles</u></html>");
        labelSelectTiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelSelectTilesMouseClicked(evt);
            }
        });

        checkBoxAllowCheats.setSelected(true);
        checkBoxAllowCheats.setText("Allow cheats");
        checkBoxAllowCheats.setToolTipText("Whether to allow cheats (single player commands)");

        jLabel4.setText("World type:");

        comboBoxGenerator.setToolTipText("<html>The world generator type to use for new land <em>outside</em> the WorldPainter-generated part</html>");
        comboBoxGenerator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxGeneratorActionPerformed(evt);
            }
        });

        jLabel5.setText("Mode:");

        comboBoxGameType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxGameTypeActionPerformed(evt);
            }
        });

        buttonGeneratorOptions.setText("...");
        buttonGeneratorOptions.setToolTipText("Edit the Superflat mode preset");
        buttonGeneratorOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonGeneratorOptionsActionPerformed(evt);
            }
        });

        checkBoxMapFeatures.setSelected(true);
        checkBoxMapFeatures.setText("Structures");
        checkBoxMapFeatures.setToolTipText("<html>Whether Minecraft should generate NPC villages,<br>\nabandoned mines , strongholds, jungle temples and<br>\ndesert temples (only applies to areas with Populate)</html>");

        jLabel6.setText("Difficulty:");

        comboBoxDifficulty.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Peaceful", "Easy", "Normal", "Hard" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fieldName)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(fieldDirectory)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSelectDirectory))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxGameType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxAllowCheats)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxDifficulty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxMinecraftVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addComponent(buttonExport)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel))
                    .addComponent(jTabbedPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(checkBoxGoodies)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxGenerator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonGeneratorOptions)
                        .addGap(18, 18, 18)
                        .addComponent(checkBoxMapFeatures)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(radioButtonExportEverything)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioButtonExportSelection)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelSelectTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonSelectDirectory))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkBoxGoodies)
                    .addComponent(radioButtonExportEverything)
                    .addComponent(radioButtonExportSelection)
                    .addComponent(labelSelectTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(comboBoxGenerator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonGeneratorOptions)
                    .addComponent(checkBoxMapFeatures))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonExport)
                    .addComponent(comboBoxMinecraftVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(checkBoxAllowCheats)
                    .addComponent(jLabel5)
                    .addComponent(comboBoxGameType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(comboBoxDifficulty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExportActionPerformed
        export();
    }//GEN-LAST:event_buttonExportActionPerformed

    private void buttonSelectDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelectDirectoryActionPerformed
        selectDir();
    }//GEN-LAST:event_buttonSelectDirectoryActionPerformed

    private void radioButtonExportEverythingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonExportEverythingActionPerformed
        setControlStates();
    }//GEN-LAST:event_radioButtonExportEverythingActionPerformed

    private void labelSelectTilesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelSelectTilesMouseClicked
        selectTiles();
    }//GEN-LAST:event_labelSelectTilesMouseClicked

    private void radioButtonExportSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioButtonExportSelectionActionPerformed
        if (radioButtonExportSelection.isSelected()) {
            selectTiles();
        } else {
            setControlStates();
        }
    }//GEN-LAST:event_radioButtonExportSelectionActionPerformed

    private void comboBoxMinecraftVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxMinecraftVersionActionPerformed
        Platform newPlatform = (Platform) comboBoxMinecraftVersion.getSelectedItem();
        if (newPlatform != null) {
            Generator generator = (Generator) comboBoxGenerator.getSelectedItem();
            GameType gameType = (GameType) comboBoxGameType.getSelectedItem();
            comboBoxGenerator.setModel(new DefaultComboBoxModel<>(newPlatform.supportedGenerators.toArray(new Generator[newPlatform.supportedGenerators.size()])));
            if (newPlatform.supportedGenerators.contains(generator)) {
                comboBoxGenerator.setSelectedItem(generator);
            } else {
                comboBoxGenerator.setSelectedItem(DEFAULT);
            }
            comboBoxGenerator.setEnabled(newPlatform.supportedGenerators.size() > 1);
            comboBoxGameType.setModel(new DefaultComboBoxModel<>(newPlatform.supportedGameTypes.toArray(new GameType[newPlatform.supportedGameTypes.size()])));
            if (newPlatform.supportedGameTypes.contains(gameType)) {
                comboBoxGameType.setSelectedItem(gameType);
            } else {
                comboBoxGameType.setSelectedItem(SURVIVAL);
            }
            comboBoxGameType.setEnabled(newPlatform.supportedGameTypes.size() > 1);
            if (newPlatform != JAVA_MCREGION) {
                checkBoxAllowCheats.setSelected(gameType == CREATIVE);
            } else {
                checkBoxAllowCheats.setSelected(false);
            }
            File exportDir = Configuration.getInstance().getExportDirectory(newPlatform);
            if ((exportDir == null) || (! exportDir.isDirectory())) {
                exportDir = PlatformManager.getInstance().getDefaultExportDir(newPlatform);
            }
            if ((exportDir != null) && exportDir.isDirectory()) {
                fieldDirectory.setText(exportDir.getAbsolutePath());
            }

            nameOnlyMaterials = gatherBlocksWithoutIds(world, newPlatform);

            // Temporary workaround TODO make the chest work again
            if ((newPlatform == JAVA_ANVIL_1_15) || (newPlatform == JAVA_ANVIL_1_17) || (newPlatform == JAVA_ANVIL_1_18)) {
                checkBoxGoodies.setEnabled(false);
                checkBoxGoodies.setSelected(false);
            } else {
                checkBoxGoodies.setEnabled(true);
                checkBoxGoodies.setSelected(world.isCreateGoodiesChest());
            }

            dimensionPropertiesEditors.forEach((dim, editor) -> editor.setPlatform(newPlatform));

            // Otherwise the JComboBox malfunctions:
            SwingUtilities.invokeLater(() -> buttonExport.setEnabled(checkCompatibility(newPlatform)));
            pack();
        }
        setControlStates();
    }//GEN-LAST:event_comboBoxMinecraftVersionActionPerformed

    private void comboBoxGameTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxGameTypeActionPerformed
        if ((comboBoxMinecraftVersion.getSelectedItem() != JAVA_MCREGION) && (comboBoxGameType.getSelectedItem() == CREATIVE)) {
            checkBoxAllowCheats.setSelected(true);
            comboBoxDifficulty.setSelectedIndex(DIFFICULTY_PEACEFUL);
        } else if (comboBoxGameType.getSelectedItem() == HARDCORE) {
            checkBoxAllowCheats.setSelected(false);
            comboBoxDifficulty.setSelectedIndex(DIFFICULTY_HARD);
        }
        setControlStates();
    }//GEN-LAST:event_comboBoxGameTypeActionPerformed

    private void buttonGeneratorOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGeneratorOptionsActionPerformed
        if (comboBoxGenerator.getSelectedItem() == CUSTOM) {
            String editedGeneratorOptions = JOptionPane.showInputDialog(this, "Edit the custom world generator name:", dim0GeneratorName);
            if (editedGeneratorOptions != null) {
                dim0GeneratorName = editedGeneratorOptions;
            }
        } else {
            if (dim0GeneratorName != null) {
                String editedGeneratorOptions = JOptionPane.showInputDialog(this, "Edit the Superflat mode preset:", dim0GeneratorName);
                if (editedGeneratorOptions != null) {
                    dim0GeneratorName = editedGeneratorOptions;
                }
            } else {
                Platform platform = (Platform) comboBoxMinecraftVersion.getSelectedItem();
                SuperflatPreset mySuperflatPreset = (dim0SuperflatPreset != null)
                        ? dim0SuperflatPreset
                        : SuperflatPreset.defaultPreset(platform);
                EditSuperflatPresetDialog dialog = new EditSuperflatPresetDialog(this, world.getPlatform(), mySuperflatPreset);
                dialog.setVisible(true);
                if (! dialog.isCancelled()) {
                    dim0SuperflatPreset = mySuperflatPreset;
                }
            }
        }
    }//GEN-LAST:event_buttonGeneratorOptionsActionPerformed

    private void comboBoxGeneratorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxGeneratorActionPerformed
        setControlStates();
        updateGeneratorButtonTooltip();
    }//GEN-LAST:event_comboBoxGeneratorActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonExport;
    private javax.swing.JButton buttonGeneratorOptions;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton buttonSelectDirectory;
    private javax.swing.JCheckBox checkBoxAllowCheats;
    private javax.swing.JCheckBox checkBoxGoodies;
    private javax.swing.JCheckBox checkBoxMapFeatures;
    private javax.swing.JComboBox comboBoxDifficulty;
    private javax.swing.JComboBox<GameType> comboBoxGameType;
    private javax.swing.JComboBox<Generator> comboBoxGenerator;
    private javax.swing.JComboBox<Platform> comboBoxMinecraftVersion;
    private org.pepsoft.worldpainter.DimensionPropertiesEditor endCeilingPropertiesEditor;
    private org.pepsoft.worldpainter.DimensionPropertiesEditor endPropertiesEditor;
    private javax.swing.JTextField fieldDirectory;
    private javax.swing.JTextField fieldName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelSelectTiles;
    private org.pepsoft.worldpainter.DimensionPropertiesEditor netherCeilingPropertiesEditor;
    private org.pepsoft.worldpainter.DimensionPropertiesEditor netherPropertiesEditor;
    private javax.swing.JRadioButton radioButtonExportEverything;
    private javax.swing.JRadioButton radioButtonExportSelection;
    private org.pepsoft.worldpainter.DimensionPropertiesEditor surfaceCeilingPropertiesEditor;
    private org.pepsoft.worldpainter.DimensionPropertiesEditor surfacePropertiesEditor;
    // End of variables declaration//GEN-END:variables

    private final World2 world;
    private final ColourScheme colourScheme;
    private final Collection<Layer> hiddenLayers;
    private final boolean contourLines;
    private final int contourSeparation;
    private final TileRenderer.LightOrigin lightOrigin;
    private final CustomBiomeManager customBiomeManager;
    private final WorldPainter view;
    private final Map<Integer, DimensionPropertiesEditor> dimensionPropertiesEditors = new HashMap<>();
    private int selectedDimension, savedGenerator;
    private Set<Point> selectedTiles;
    private boolean disableTileSelectionWarning, disableDisabledLayersWarning, endlessBorder, savedMapFeatures;
    private String dim0GeneratorName;
    private SuperflatPreset dim0SuperflatPreset; // TODO: finish
    private Map<String, Set<String>> nameOnlyMaterials = new HashMap<>();

    private static final Icon WARNING_ICON = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/error.png");
    private static final Icon SPACER_ICON = IconUtils.loadScaledIcon("org/pepsoft/worldpainter/icons/spacer.png");
    private static final long serialVersionUID = 1L;
}