/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.bo2;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.KeyStroke;
import javax.vecmath.Point3i;
import org.pepsoft.worldpainter.ColourScheme;
import org.pepsoft.worldpainter.objects.WPObject;

/**
 *
 * @author pepijn
 */
public class OffsetEditor extends javax.swing.JDialog {
    /**
     * Creates new form OffsetEditor
     */
    public OffsetEditor(Window parent, Point3i offset, int yVariation, WPObject object, ColourScheme colourScheme) {
        super(parent, ModalityType.DOCUMENT_MODAL);
        
        initComponents();
        
        offsetViewer1.setObject(object);
        offsetViewer1.setOffset(offset);
        offsetViewer1.setColourScheme(colourScheme);
        
        spinnerX.setValue(offset.x);
        spinnerY.setValue(offset.y);
        spinnerZ.setValue(offset.z);
        spinnerYVariation.setValue(yVariation);
        
        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put("cancel", new AbstractAction("cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }

            private static final long serialVersionUID = 1L;
        });

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        
        KeyListener listener = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c == 'l') {
                    offsetViewer1.rotateLeft();
                    e.consume();
                } else if (c == 'r') {
                    offsetViewer1.rotateRight();
                    e.consume();
                }
            }
        };
        ((DefaultEditor) spinnerX.getEditor()).getTextField().addKeyListener(listener);
        ((DefaultEditor) spinnerY.getEditor()).getTextField().addKeyListener(listener);
        ((DefaultEditor) spinnerZ.getEditor()).getTextField().addKeyListener(listener);
        buttonOK.addKeyListener(listener);
        buttonCancel.addKeyListener(listener);
        jTextArea1.addKeyListener(listener);

        rootPane.setDefaultButton(buttonOK);
        
        pack();
        setLocationRelativeTo(parent);
    }

    public boolean isCancelled() {
        return cancelled;
    }
    
    public Point3i getOffset() {
        return new Point3i((Integer) spinnerX.getValue(), (Integer) spinnerY.getValue(), (Integer) spinnerZ.getValue());
    }
    
    public int getYVariation(){
        return (Integer)spinnerYVariation.getValue();
    }

    private void updateOffsetViewer() {
        offsetViewer1.setOffset(getOffset());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        spinnerX = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        spinnerY = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        spinnerZ = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        spinnerYVariation = new javax.swing.JSpinner();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        offsetViewer1 = new org.pepsoft.worldpainter.layers.bo2.OffsetViewer();
        jTextArea1 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Edit Offset");

        jLabel2.setText("X axis (west to east):");

        spinnerX.setModel(new javax.swing.SpinnerNumberModel(0, -999, 999, 1));
        spinnerX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerXStateChanged(evt);
            }
        });

        jLabel3.setText("Z axis (north to south):");

        spinnerY.setModel(new javax.swing.SpinnerNumberModel(0, -999, 999, 1));
        spinnerY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerYStateChanged(evt);
            }
        });

        jLabel4.setText("Y axis (vertical):");

        spinnerZ.setModel(new javax.swing.SpinnerNumberModel(0, -999, 999, 1));
        spinnerZ.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerZStateChanged(evt);
            }
        });

        jLabel6.setText("Y variation (vertical):");

        spinnerYVariation.setModel(new javax.swing.SpinnerNumberModel(0, 0, 999, 1));
        spinnerYVariation.setMinimumSize(new java.awt.Dimension(78, 22));
        spinnerYVariation.setPreferredSize(new java.awt.Dimension(78, 22));
        spinnerYVariation.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerYVariationStateChanged(evt);
            }
        });

        buttonCancel.setText("Cancel");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonOK.setText("OK");
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOKActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        javax.swing.GroupLayout offsetViewer1Layout = new javax.swing.GroupLayout(offsetViewer1);
        offsetViewer1.setLayout(offsetViewer1Layout);
        offsetViewer1Layout.setHorizontalGroup(
            offsetViewer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 277, Short.MAX_VALUE)
        );
        offsetViewer1Layout.setVerticalGroup(
            offsetViewer1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(offsetViewer1, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(offsetViewer1, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
        );

        jTextArea1.setEditable(false);
        jTextArea1.setFont(jLabel2.getFont());
        jTextArea1.setLineWrap(true);
        jTextArea1.setText("The dotted square indicates the \"origin\" of the object; i.e. the block that will be placed directly on the surface at the location of the object. You can change it by adjusting the offset. The dotted line indicates ground level.\n\nRotate the object by pressing L or R.");
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setBorder(null);
        jTextArea1.setOpaque(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(spinnerZ)
                                    .addComponent(spinnerY)
                                    .addComponent(spinnerX)
                                    .addComponent(spinnerYVariation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jTextArea1))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(spinnerX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(spinnerY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(spinnerZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(spinnerYVariation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jTextArea1, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOK))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        cancelled = false;
        dispose();
    }//GEN-LAST:event_buttonOKActionPerformed

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        dispose();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void spinnerXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerXStateChanged
        updateOffsetViewer();
    }//GEN-LAST:event_spinnerXStateChanged

    private void spinnerYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerYStateChanged
        updateOffsetViewer();
    }//GEN-LAST:event_spinnerYStateChanged

    private void spinnerZStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerZStateChanged
        updateOffsetViewer();
    }//GEN-LAST:event_spinnerZStateChanged

    private void spinnerYVariationStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerYVariationStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_spinnerYVariationStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.JButton buttonOK;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextArea jTextArea1;
    private org.pepsoft.worldpainter.layers.bo2.OffsetViewer offsetViewer1;
    private javax.swing.JSpinner spinnerX;
    private javax.swing.JSpinner spinnerY;
    private javax.swing.JSpinner spinnerYVariation;
    private javax.swing.JSpinner spinnerZ;
    // End of variables declaration//GEN-END:variables

    private boolean cancelled = true;

    private static final long serialVersionUID = 1L;
}