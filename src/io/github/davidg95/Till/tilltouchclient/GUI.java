/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.Till.tilltouchclient;

import io.github.davidg95.Till.till.Category;
import io.github.davidg95.Till.till.LoginException;
import io.github.davidg95.Till.till.OutOfStockException;
import io.github.davidg95.Till.till.Product;
import io.github.davidg95.Till.till.ProductNotFoundException;
import io.github.davidg95.Till.till.Sale;
import io.github.davidg95.Till.till.ServerConnection;
import io.github.davidg95.Till.till.Staff;
import io.github.davidg95.Till.till.StaffNotFoundException;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class GUI extends javax.swing.JFrame {

    private final ServerConnection sc;

    private Staff staff;

    private Sale sale;
    private final DefaultTableModel model;

    private final CardLayout cards;

    /**
     * Creates new form GUI
     */
    public GUI() {
        sc = TillTouchClient.getServerConnection();
        initComponents();
        model = (DefaultTableModel) tblProducts.getModel();
        lblHost.setText(TillTouchClient.HOST_NAME);
        cards = (CardLayout) panelMain.getLayout();
        newSale();
        ClockThread.setClockLabel(lblTime);
    }

    public void setButtons() {
        try {
            List<Category> categorys = sc.getCategoryButtons();
            for (Category c : categorys) {
                addCategoryButton(c);
            }
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void newSale() {
        sale = new Sale();
        clearList();
        setTotalLabel(0);
        setItemsLabel(0);
    }

    private void addToList(Product p) {
        Object[] s = new Object[]{p.getShortName(), p.getPrice()};
        model.addRow(s);
    }

    private void clearList() {
        model.setRowCount(0);
    }

    private void setTotalLabel(double val) {
        lblTotal.setText("Total: £" + val);
    }

    private void setItemsLabel(int val) {
        lblItems.setText("Items: " + val);
    }

    public void login() {
        try {
            int staffID = (int) NumberEntry.showNumberEntryDialog(this, "Enter Logon ID");
            staff = sc.tillLogin(staffID);
            lblStaff.setText(staff.getName());
            return;
        } catch (IOException | LoginException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Logon Error", JOptionPane.ERROR_MESSAGE);
        }
        login();
//        staff = LoginDialog.showDialog(this);
    }

    public void logout() {
        try {
            sc.tillLogout(staff.getId());
            lblStaff.setText("Not Logged In");
            staff = null;
        } catch (IOException | StaffNotFoundException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        login();
    }

    public void addCategoryButton(Category c) {
        JButton cButton = new JButton(c.getName());
        if (c.getColorValue() != 0) {
            cButton.setBackground(new Color(c.getColorValue()));
        }
        cButton.setSize(140, 50);
        cButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cards.show(panelMain, c.getName());
            }

        });
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 10));

        List<Product> products;
        try {
            products = sc.getProductButtons(c.getID());
            for (Product p : products) {
                JButton pButton = new JButton(p.getShortName());
                if (p.getColorValue() != 0) {
                    pButton.setBackground(new Color(p.getColorValue()));
                }
                pButton.setSize(140, 50);
                pButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (p.isOpen()) {
                            double price = NumberEntry.showNumberEntryDialog(GUI.this, "Enter Price") / 100;
                            if (price > 0) {
                                p.setPrice(price);
                                sale.addItem(p);
                                setTotalLabel(sale.getTotal());
                                setItemsLabel(sale.getItemCount());
                                addToList(p);
                            }
                        } else {
                            sale.addItem(p);
                            setTotalLabel(sale.getTotal());
                            setItemsLabel(sale.getItemCount());
                            addToList(p);
                        }
                    }

                });
                panel.add(pButton);
            }

            panelMain.add(panel, c.getName());
            panelCategories.add(cButton);
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelNumberEntry = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        txtNumber = new javax.swing.JTextField();
        panelMain = new javax.swing.JPanel();
        topPanel = new javax.swing.JPanel();
        lblVerison = new javax.swing.JLabel();
        lblHost = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        lblStaff = new javax.swing.JLabel();
        btnLogOut = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblProducts = new javax.swing.JTable();
        btnComplete = new javax.swing.JButton();
        lblTotal = new javax.swing.JLabel();
        lblItems = new javax.swing.JLabel();
        panelCategories = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JTill Terminal");
        setIconImage(TillTouchClient.getIcon());

        jButton1.setText("1");
        jButton1.setMaximumSize(new java.awt.Dimension(100, 60));
        jButton1.setMinimumSize(new java.awt.Dimension(100, 60));
        jButton1.setPreferredSize(new java.awt.Dimension(100, 60));

        jButton2.setText("2");
        jButton2.setMaximumSize(new java.awt.Dimension(100, 60));
        jButton2.setMinimumSize(new java.awt.Dimension(100, 60));
        jButton2.setPreferredSize(new java.awt.Dimension(100, 60));

        jButton3.setText("3");
        jButton3.setMaximumSize(new java.awt.Dimension(100, 60));
        jButton3.setMinimumSize(new java.awt.Dimension(100, 60));
        jButton3.setPreferredSize(new java.awt.Dimension(100, 60));

        jButton4.setText("5");
        jButton4.setMaximumSize(new java.awt.Dimension(100, 60));
        jButton4.setMinimumSize(new java.awt.Dimension(100, 60));
        jButton4.setPreferredSize(new java.awt.Dimension(100, 60));

        jButton5.setText("6");
        jButton5.setMaximumSize(new java.awt.Dimension(100, 60));
        jButton5.setMinimumSize(new java.awt.Dimension(100, 60));
        jButton5.setPreferredSize(new java.awt.Dimension(100, 60));

        jButton6.setText("4");
        jButton6.setMaximumSize(new java.awt.Dimension(100, 60));
        jButton6.setMinimumSize(new java.awt.Dimension(100, 60));
        jButton6.setPreferredSize(new java.awt.Dimension(100, 60));

        jButton7.setText("8");
        jButton7.setMaximumSize(new java.awt.Dimension(100, 60));
        jButton7.setMinimumSize(new java.awt.Dimension(100, 60));
        jButton7.setPreferredSize(new java.awt.Dimension(100, 60));

        jButton8.setText("9");
        jButton8.setMaximumSize(new java.awt.Dimension(100, 60));
        jButton8.setMinimumSize(new java.awt.Dimension(100, 60));
        jButton8.setPreferredSize(new java.awt.Dimension(100, 60));

        jButton9.setText("7");
        jButton9.setMaximumSize(new java.awt.Dimension(100, 60));
        jButton9.setMinimumSize(new java.awt.Dimension(100, 60));
        jButton9.setPreferredSize(new java.awt.Dimension(100, 60));

        jButton10.setText("00");
        jButton10.setMaximumSize(new java.awt.Dimension(100, 60));
        jButton10.setMinimumSize(new java.awt.Dimension(100, 60));
        jButton10.setPreferredSize(new java.awt.Dimension(100, 60));

        jButton11.setText("Enter");
        jButton11.setMaximumSize(new java.awt.Dimension(100, 60));
        jButton11.setMinimumSize(new java.awt.Dimension(100, 60));
        jButton11.setPreferredSize(new java.awt.Dimension(100, 60));

        jButton12.setText("0");
        jButton12.setMaximumSize(new java.awt.Dimension(100, 60));
        jButton12.setMinimumSize(new java.awt.Dimension(100, 60));
        jButton12.setPreferredSize(new java.awt.Dimension(100, 60));

        txtNumber.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txtNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNumberActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelNumberEntryLayout = new javax.swing.GroupLayout(panelNumberEntry);
        panelNumberEntry.setLayout(panelNumberEntryLayout);
        panelNumberEntryLayout.setHorizontalGroup(
            panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txtNumber)
            .addGroup(panelNumberEntryLayout.createSequentialGroup()
                .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelNumberEntryLayout.createSequentialGroup()
                        .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(panelNumberEntryLayout.createSequentialGroup()
                            .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(panelNumberEntryLayout.createSequentialGroup()
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(0, 0, 0)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 0, 0)
                .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(panelNumberEntryLayout.createSequentialGroup()
                .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        panelNumberEntryLayout.setVerticalGroup(
            panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelNumberEntryLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        panelMain.setLayout(new java.awt.CardLayout());

        lblVerison.setText("V0.1B");

        lblHost.setText("SITE NAME HERE");

        lblTime.setText("TIME HERE");

        lblStaff.setText("STAFF NAME HERE");

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addComponent(lblVerison, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblHost, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStaff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTime, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTime, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(lblStaff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblVerison, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                    .addComponent(lblHost, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        btnLogOut.setText("Log Out");
        btnLogOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogOutActionPerformed(evt);
            }
        });

        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product", "Price"
            }
        ));
        jScrollPane2.setViewportView(tblProducts);

        btnComplete.setText("Complete");
        btnComplete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCompleteActionPerformed(evt);
            }
        });

        lblTotal.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblTotal.setText("Total: £0.00");

        lblItems.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblItems.setText("Items: 0");

        panelCategories.setPreferredSize(new java.awt.Dimension(686, 120));
        panelCategories.setLayout(new java.awt.GridLayout(2, 5));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelCategories, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnLogOut, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 704, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnComplete, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(lblItems, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblTotal)
                        .addComponent(panelNumberEntry, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
            .addComponent(topPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelCategories, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTotal)
                            .addComponent(lblItems, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelNumberEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnComplete, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnLogOut, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLogOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogOutActionPerformed
        logout();
    }//GEN-LAST:event_btnLogOutActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        try {
            sc.tillLogout(staff.getId());
        } catch (IOException | StaffNotFoundException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        sc.close();
        System.exit(0);
    }//GEN-LAST:event_btnExitActionPerformed

    private void txtNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNumberActionPerformed
        try {
            String barcode = txtNumber.getText();
            Product p = sc.getProductByBarCode(barcode);
            sale.addItem(p);
            setTotalLabel(sale.getTotal());
            setItemsLabel(sale.getItemCount());
            addToList(p);
            txtNumber.setText("");
        } catch (IOException | ProductNotFoundException | SQLException ex) {
            TouchDialog.showMessageDialog(this, "Product Not Found", ex.getMessage());
        }
    }//GEN-LAST:event_txtNumberActionPerformed

    private void btnCompleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompleteActionPerformed
        for (int p : sale.getProducts()) {
            try {
                sc.purchaseProduct(p);
            } catch (IOException | ProductNotFoundException | OutOfStockException | SQLException ex) {

            }
        }
        try {
            sc.addSale(sale);
        } catch (IOException ex) {
        }
        newSale();
    }//GEN-LAST:event_btnCompleteActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnComplete;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnLogOut;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblHost;
    private javax.swing.JLabel lblItems;
    private javax.swing.JLabel lblStaff;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblVerison;
    private javax.swing.JPanel panelCategories;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelNumberEntry;
    private javax.swing.JTable tblProducts;
    private javax.swing.JPanel topPanel;
    private javax.swing.JTextField txtNumber;
    // End of variables declaration//GEN-END:variables
}
