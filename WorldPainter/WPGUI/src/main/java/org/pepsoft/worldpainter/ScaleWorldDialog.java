/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package org.pepsoft.worldpainter;

import org.pepsoft.util.ProgressReceiver;
import org.pepsoft.util.SubProgressReceiver;
import org.pepsoft.util.swing.ProgressDialog;
import org.pepsoft.util.swing.ProgressTask;
import org.pepsoft.worldpainter.Dimension.Anchor;
import org.pepsoft.worldpainter.history.HistoryEntry;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static org.pepsoft.util.swing.MessageUtils.beepAndShowError;
import static org.pepsoft.util.swing.MessageUtils.beepAndShowWarning;
import static org.pepsoft.util.swing.ProgressDialog.NOT_CANCELABLE;
import static org.pepsoft.worldpainter.App.INT_NUMBER_FORMAT;
import static org.pepsoft.worldpainter.Constants.TILE_SIZE;
import static org.pepsoft.worldpainter.Dimension.Role.DETAIL;
import static org.pepsoft.worldpainter.util.MinecraftUtil.blocksToWalkingTime;

/**
 *
 * @author pepijn
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"}) // Managed by NetBeans
public class ScaleWorldDialog extends WorldPainterDialog {

    /**
     * Creates new form ScaleWorldDialog
     */
    public ScaleWorldDialog(Window parent, World2 world, Anchor anchor) {
        super(parent);
        this.world = world;
        this.anchor = anchor;
        affectedDimensions = world.getDimensions().stream()
                .filter(dimension -> dimension.getAnchor().dim == anchor.dim)
                .collect(toList());

        initComponents();
        setTitle("Scale " + new Anchor(anchor.dim, DETAIL, false, 0).getDefaultName() + " Dimension");
        final Dimension dimension = world.getDimension(anchor);
        final int width = dimension.getWidth() * TILE_SIZE, height = dimension.getHeight() * TILE_SIZE;
        labelCurrentSize.setText(format("%s x %s blocks", INT_NUMBER_FORMAT.format(width), INT_NUMBER_FORMAT.format(height)));
        labelCurrentWalkingTime.setText(getWalkingTime(width, height));
        updateNewSize();

        getRootPane().setDefaultButton(buttonScale);

        setLocationRelativeTo(parent);
    }

    private void scale() {
        final int percentage = (int) spinnerScaleFactor.getValue();
        if (percentage == 100) {
            beepAndShowError(this, "Select a scaling factor other than 100%", "Select Scaling Factor");
            return;
        } else if (JOptionPane.showConfirmDialog(this, "Are you sure you want to scale this dimension by " + percentage + "%?\nThis cannot be undone!", "Confirm Scaling", YES_NO_OPTION) != OK_OPTION) {
            return;
        }
        final CoordinateTransform transform = CoordinateTransform.getScalingInstance(percentage / 100f);
        ProgressDialog.executeTask(this, new ProgressTask<Void>() {
            @Override
            public String getName() {
                return "Scaling dimension(s)";
            }

            @Override
            public Void execute(ProgressReceiver progressReceiver) throws ProgressReceiver.OperationCancelled {
                for (int i = 0; i < affectedDimensions.size(); i++) {
                    final Dimension dimension = affectedDimensions.get(i);
                    world.transform(dimension.getAnchor(), transform, (progressReceiver != null) ? new SubProgressReceiver(progressReceiver, (float) i / affectedDimensions.size(), 1.0f / affectedDimensions.size()) : null);
                    world.addHistoryEntry(HistoryEntry.WORLD_DIMENSION_SCALED, dimension.getName(), percentage);
                }
                return null;
            }
        }, NOT_CANCELABLE);
        if (affectedDimensions.stream().flatMap(dimension -> dimension.getOverlays().stream()).anyMatch(overlay -> ! overlay.getFile().canRead())) {
            beepAndShowWarning(this, "One or more overlay image files could not be read,\nand have therefore not been scaled.\nYou will need to scale these manually.", "Not All Overlays Scaled");
        }
        ok();
    }
    
    private void updateNewSize() {
        final Dimension dimension = world.getDimension(anchor);
        final float scale = (int) spinnerScaleFactor.getValue() / 100f;
        final int newWidth = Math.round(dimension.getWidth() * TILE_SIZE * scale), newHeight = Math.round(dimension.getHeight() * TILE_SIZE * scale);
        labelNewSize.setText(format("%s x %s blocks", INT_NUMBER_FORMAT.format(newWidth), INT_NUMBER_FORMAT.format(newHeight)));
        labelNewWalkingTime.setText(getWalkingTime(newWidth, newHeight));
    }
    
    private void setControlStates() {
        buttonScale.setEnabled((int) spinnerScaleFactor.getValue() != 100);
    }

    private String getWalkingTime(int width, int height) {
        if (width == height) {
            return blocksToWalkingTime(width);
        } else {
            String westEastTime = blocksToWalkingTime(width);
            String northSouthTime = blocksToWalkingTime(height);
            if (westEastTime.equals(northSouthTime)) {
                return westEastTime;
            } else {
                return "west to east: " + westEastTime + ", north to south: " + northSouthTime;
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"}) // Managed by NetBeans
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        labelCurrentSize = new javax.swing.JLabel();
        labelCurrentWalkingTime = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        spinnerScaleFactor = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        buttonCancel = new javax.swing.JButton();
        buttonScale = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        labelNewSize = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        labelNewWalkingTime = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Scale World");

        jLabel1.setText("Scale the current dimension according to these settings:");

        jLabel2.setText("Current size:");

        jLabel3.setText("Current edge to edge walking time:");

        labelCurrentSize.setText("jLabel4");

        labelCurrentWalkingTime.setText("jLabel4");

        jLabel4.setLabelFor(spinnerScaleFactor);
        jLabel4.setText("Scale factor:");

        spinnerScaleFactor.setModel(new javax.swing.SpinnerNumberModel(100, 10, 1000, 1));
        spinnerScaleFactor.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerScaleFactorStateChanged(evt);
            }
        });

        jLabel5.setText("%");

        buttonCancel.setText("Cancel");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonScale.setText("Scale");
        buttonScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonScaleActionPerformed(evt);
            }
        });

        jLabel6.setText("New size:");

        labelNewSize.setText("jLabel7");

        jLabel7.setText("New edge to edge walking time:");

        labelNewWalkingTime.setText("jLabel8");

        jLabel8.setText("<html>\nNotes:\n<ul>\n<li>The terrain height and continuous layers such as trees, Custom Object layers, Frost, etc.<br>\nwill be scaled smoothly using bicubic interpolation. Artefacts are still unavoidable though,<br>\nespecially at larger scales.\n<li>Discrete layers such as Annotations and Biomes, but also the terrain type will be scaled<br>\nusing nearest neighbour interpolation. At larger scales these will become noticeably blocky.\n<li>Scaling low-resolution terrain (Imported from Minecraft, or from a low resolution height<br>\nmap) may result in blocky results.\n<li>Extra land may be added around the edges in order to meet chunk boundaries.\n<li><strong>Caution:</strong> if you are scaling up this may fail if there is not enough memory. Save your work to<br>\ndisk first!\n</ul>\n</html>");

        jLabel9.setFont(jLabel9.getFont().deriveFont((jLabel9.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel9.setText("This operation cannot be undone!");

        jLabel10.setText("<html><i>All associated dimension such as Ceiling Dimensions and<br> Custom Cave/Tunnel Floor Dimensions will be scaled together.</i></html>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                                .addGap(0, 0, Short.MAX_VALUE)
                                                                .addComponent(buttonScale)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(buttonCancel))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jLabel1)
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(jLabel2)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(labelCurrentSize))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(jLabel3)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(labelCurrentWalkingTime))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(jLabel4)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(spinnerScaleFactor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(0, 0, 0)
                                                                                .addComponent(jLabel5))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(jLabel6)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(labelNewSize))
                                                                        .addGroup(layout.createSequentialGroup()
                                                                                .addComponent(jLabel7)
                                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(labelNewWalkingTime))
                                                                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(jLabel9))
                                                                .addGap(0, 0, Short.MAX_VALUE))))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(labelCurrentSize))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(labelCurrentWalkingTime))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(spinnerScaleFactor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel5))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6)
                                        .addComponent(labelNewSize))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel7)
                                        .addComponent(labelNewWalkingTime))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(buttonCancel)
                                        .addComponent(buttonScale))
                                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        cancel();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonScaleActionPerformed
        scale();
    }//GEN-LAST:event_buttonScaleActionPerformed

    private void spinnerScaleFactorStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerScaleFactorStateChanged
        setControlStates();
        updateNewSize();
    }//GEN-LAST:event_spinnerScaleFactorStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonScale;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel labelCurrentSize;
    private javax.swing.JLabel labelCurrentWalkingTime;
    private javax.swing.JLabel labelNewSize;
    private javax.swing.JLabel labelNewWalkingTime;
    private javax.swing.JSpinner spinnerScaleFactor;
    // End of variables declaration//GEN-END:variables

    private final World2 world;
    private final Anchor anchor;
    private final List<Dimension> affectedDimensions;
}