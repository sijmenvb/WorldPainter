/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package org.pepsoft.worldpainter.layers.tunnel;

import org.pepsoft.worldpainter.*;
import org.pepsoft.worldpainter.themes.TerrainListCellRenderer;
import org.pepsoft.worldpainter.util.BiomeUtils;

import javax.swing.*;
import java.awt.*;

import static org.pepsoft.util.CollectionUtils.nullAnd;

/**
 *
 * @author pepijn
 */
public class FloorDimensionSettingsDialog extends WorldPainterDialog {
    /**
     * Creates new form FloorDimensionSettingsDialog
     */
    public FloorDimensionSettingsDialog(Window parent, ColourScheme colourScheme, Platform platform, int minHeight, int maxHeight, int level, NoiseSettings variation, int waterLevel, boolean floodWithLava, Terrain terrain) {
        super(parent);

        initComponents();

        ((SpinnerNumberModel) spinnerFloorLevel.getModel()).setMinimum(minHeight);
        ((SpinnerNumberModel) spinnerFloorLevel.getModel()).setMaximum(maxHeight - 1);
        ((SpinnerNumberModel) spinnerFloodLevel.getModel()).setMinimum(minHeight);
        ((SpinnerNumberModel) spinnerFloodLevel.getModel()).setMaximum(maxHeight - 1);
        comboBoxTerrain.setRenderer(new TerrainListCellRenderer(colourScheme));
        comboBoxTerrain.setModel(new DefaultComboBoxModel<>(Terrain.getConfiguredValues()));
        comboBoxBiome.setRenderer(new BiomeListCellRenderer(colourScheme, null, platform));
        comboBoxBiome.setModel(new DefaultComboBoxModel<>(nullAnd(BiomeUtils.getAllBiomes(platform, null)).toArray(new Integer[0])));

        spinnerFloorLevel.setValue(level);
        noiseSettingsEditorFloor.setNoiseSettings(variation);
        comboBoxTerrain.setSelectedItem(terrain);
        spinnerFloodLevel.setValue(waterLevel);
        checkBoxFloodWithLava.setSelected(floodWithLava);

        setLocationRelativeTo(parent);
    }

    public int getLevel() {
        return (int) spinnerFloorLevel.getValue();
    }

    public NoiseSettings getVariation() {
        return noiseSettingsEditorFloor.getNoiseSettings();
    }

    public int getWaterLevel() {
        return (int) spinnerFloodLevel.getValue();
    }

    public boolean isFloodWithLava() {
        return checkBoxFloodWithLava.isSelected();
    }

    public Terrain getTerrain() {
        return (Terrain) comboBoxTerrain.getSelectedItem();
    }

    public Integer getBiome() {
        return (Integer) comboBoxBiome.getSelectedItem();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        spinnerFloorLevel = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        noiseSettingsEditorFloor = new org.pepsoft.worldpainter.NoiseSettingsEditor();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        comboBoxTerrain = new javax.swing.JComboBox<>();
        jLabel23 = new javax.swing.JLabel();
        comboBoxBiome = new javax.swing.JComboBox<>();
        jLabel21 = new javax.swing.JLabel();
        spinnerFloodLevel = new javax.swing.JSpinner();
        checkBoxFloodWithLava = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Floor Dimension Defaults");
        setResizable(false);

        jLabel3.setText("Level:");

        spinnerFloorLevel.setModel(new javax.swing.SpinnerNumberModel(32, -64, 319, 1));

        jLabel5.setText("Variation:");

        jButton1.setText("Cancel");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Create");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setText("Configure the default settings for the new floor dimension here:");

        jLabel2.setText("Terrain:");

        jLabel23.setText("Biome:");

        jLabel21.setText("Water level:");

        spinnerFloodLevel.setModel(new javax.swing.SpinnerNumberModel(62, -64, 319, 1));

        checkBoxFloodWithLava.setText("lava instead of water:");
        checkBoxFloodWithLava.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloorLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(noiseSettingsEditorFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxTerrain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxBiome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerFloodLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(checkBoxFloodWithLava)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(spinnerFloorLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(noiseSettingsEditorFloor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(comboBoxTerrain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(comboBoxBiome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(spinnerFloodLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(checkBoxFloodWithLava))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        cancel();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        ok();
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox checkBoxFloodWithLava;
    private javax.swing.JComboBox<Integer> comboBoxBiome;
    private javax.swing.JComboBox<Terrain> comboBoxTerrain;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private org.pepsoft.worldpainter.NoiseSettingsEditor noiseSettingsEditorFloor;
    private javax.swing.JSpinner spinnerFloodLevel;
    private javax.swing.JSpinner spinnerFloorLevel;
    // End of variables declaration//GEN-END:variables
}