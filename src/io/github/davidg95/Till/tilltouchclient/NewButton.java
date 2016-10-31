/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.Till.tilltouchclient;

import io.github.davidg95.Till.till.ButtonFunction;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JDialog;

/**
 *
 * @author 1301480
 */
public class NewButton extends javax.swing.JDialog {

    private static JDialog dialog;
    private static CustomButton button;
    private ButtonFunction function = ButtonFunction.PRODUCT;

    /**
     * Creates new form NewButton
     */
    public NewButton(Window parent) {
        super(parent);
        initComponents();
        this.setLocationRelativeTo(parent);
        this.setModal(true);
    }

    public static CustomButton showNewButtonDialog(Component parent) {
        Window window = null;
        if(parent instanceof Dialog || parent instanceof Frame){
            window = (Window)parent;
        }
        dialog = new NewButton(window);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        button = null;
        dialog.setVisible(true);
        return button;
    }

    private void init() {

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtButtonName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        cmbFunction = new javax.swing.JComboBox<>();
        panelProduct = new javax.swing.JPanel();
        lblCode = new javax.swing.JLabel();
        txtCode = new javax.swing.JTextField();
        panelScreen = new javax.swing.JPanel();
        lblScreen = new javax.swing.JLabel();
        cmbScreen = new javax.swing.JComboBox<>();
        panelDialog = new javax.swing.JPanel();
        lblDialog = new javax.swing.JLabel();
        cmbDialog = new javax.swing.JComboBox<>();
        btnAdd = new javax.swing.JButton();

        jLabel1.setText("Button Name:");

        jLabel2.setText("Button Function:");

        cmbFunction.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Product", "Show Screen", "Show Dialog"}));
        cmbFunction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFunctionActionPerformed(evt);
            }
        });

        panelProduct.setBorder(javax.swing.BorderFactory.createTitledBorder("Product"));

        lblCode.setText("Product Code:");

        javax.swing.GroupLayout panelProductLayout = new javax.swing.GroupLayout(panelProduct);
        panelProduct.setLayout(panelProductLayout);
        panelProductLayout.setHorizontalGroup(
            panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProductLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(lblCode)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCode, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(97, Short.MAX_VALUE))
        );
        panelProductLayout.setVerticalGroup(
            panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProductLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(panelProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCode)
                    .addComponent(txtCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(216, Short.MAX_VALUE))
        );

        panelScreen.setBorder(javax.swing.BorderFactory.createTitledBorder("Show Screen"));
        panelScreen.setEnabled(false);

        lblScreen.setText("Screen To Show:");
        lblScreen.setEnabled(false);

        cmbScreen.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbScreen.setEnabled(false);

        javax.swing.GroupLayout panelScreenLayout = new javax.swing.GroupLayout(panelScreen);
        panelScreen.setLayout(panelScreenLayout);
        panelScreenLayout.setHorizontalGroup(
            panelScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScreenLayout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(lblScreen)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbScreen, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(84, Short.MAX_VALUE))
        );
        panelScreenLayout.setVerticalGroup(
            panelScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScreenLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(panelScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblScreen)
                    .addComponent(cmbScreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelDialog.setBorder(javax.swing.BorderFactory.createTitledBorder("Show Dialog"));
        panelDialog.setEnabled(false);

        lblDialog.setText("Dialog To Show:");
        lblDialog.setEnabled(false);

        cmbDialog.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbDialog.setEnabled(false);

        javax.swing.GroupLayout panelDialogLayout = new javax.swing.GroupLayout(panelDialog);
        panelDialog.setLayout(panelDialogLayout);
        panelDialogLayout.setHorizontalGroup(
            panelDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDialog)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbDialog, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(118, Short.MAX_VALUE))
        );
        panelDialogLayout.setVerticalGroup(
            panelDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDialogLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(panelDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDialog)
                    .addComponent(cmbDialog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnAdd.setText("Add Button");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnAdd)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(panelProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(panelScreen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(panelDialog, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel2)
                                .addComponent(jLabel1))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtButtonName)
                                .addComponent(cmbFunction, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtButtonName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(cmbFunction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelDialog, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelScreen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addComponent(btnAdd)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cmbFunctionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFunctionActionPerformed
        String item = (String) cmbFunction.getSelectedItem();
        switch (item) {
            case "Product":
                function = ButtonFunction.PRODUCT;
                panelProduct.setEnabled(true);
                lblCode.setEnabled(true);
                txtCode.setEnabled(true);
                panelScreen.setEnabled(false);
                lblScreen.setEnabled(false);
                cmbScreen.setEnabled(false);
                panelDialog.setEnabled(false);
                lblDialog.setEnabled(false);
                cmbDialog.setEnabled(false);
                break;
            case "Show Screen":
                function = ButtonFunction.SHOW_SCREEN;
                panelProduct.setEnabled(false);
                lblCode.setEnabled(false);
                txtCode.setEnabled(false);
                panelScreen.setEnabled(true);
                lblScreen.setEnabled(true);
                cmbScreen.setEnabled(true);
                panelDialog.setEnabled(false);
                lblDialog.setEnabled(false);
                cmbDialog.setEnabled(false);
                break;
            case "Show Dialog":
                panelProduct.setEnabled(false);
                lblCode.setEnabled(false);
                txtCode.setEnabled(false);
                panelScreen.setEnabled(false);
                lblScreen.setEnabled(false);
                cmbScreen.setEnabled(false);
                panelDialog.setEnabled(true);
                lblDialog.setEnabled(true);
                cmbDialog.setEnabled(true);
                break;
            default:
                break;
        }
    }//GEN-LAST:event_cmbFunctionActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        String name = txtButtonName.getText();
        button = new CustomButton(name, function);
        this.setVisible(false);
    }//GEN-LAST:event_btnAddActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JComboBox<String> cmbDialog;
    private javax.swing.JComboBox<String> cmbFunction;
    private javax.swing.JComboBox<String> cmbScreen;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblCode;
    private javax.swing.JLabel lblDialog;
    private javax.swing.JLabel lblScreen;
    private javax.swing.JPanel panelDialog;
    private javax.swing.JPanel panelProduct;
    private javax.swing.JPanel panelScreen;
    private javax.swing.JTextField txtButtonName;
    private javax.swing.JTextField txtCode;
    // End of variables declaration//GEN-END:variables
}
