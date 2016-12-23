/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.tilltouchclient;

import io.github.davidg95.JTill.jtill.Category;
import io.github.davidg95.JTill.jtill.Customer;
import io.github.davidg95.JTill.jtill.CustomerNotFoundException;
import io.github.davidg95.JTill.jtill.LoginException;
import io.github.davidg95.JTill.jtill.OutOfStockException;
import io.github.davidg95.JTill.jtill.Product;
import io.github.davidg95.JTill.jtill.ProductNotFoundException;
import io.github.davidg95.JTill.jtill.Sale;
import io.github.davidg95.JTill.jtill.ServerConnection;
import io.github.davidg95.JTill.jtill.Staff;
import io.github.davidg95.JTill.jtill.StaffNotFoundException;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author David
 */
public class GUI extends javax.swing.JFrame {

    private final ServerConnection sc;

    private Staff staff;

    private Sale sale;
    private double amountDue;
    private final DefaultTableModel model;

    private final CardLayout categoryCards;
    private final CardLayout screenCards;
    private ButtonGroup cardsButonGroup;

    /**
     * Creates new form GUI
     */
    public GUI() {
        sc = TillTouchClient.getServerConnection();
        initComponents();
        model = (DefaultTableModel) tblProducts.getModel();
        lblHost.setText(TillTouchClient.HOST_NAME);
        categoryCards = (CardLayout) panelMain.getLayout();
        screenCards = (CardLayout) CardsPanel.getLayout();
        cardsButonGroup = new ButtonGroup();
        newSale();
        ClockThread.setClockLabel(lblTime);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public void setButtons() {
        try {
            List<Category> categorys = sc.getCategoryButtons();
            panelCategories.setLayout(new GridLayout(2, 5));
            for (Category c : categorys) {
                addCategoryButton(c);
            }

            for (int i = categorys.size() - 1; i < 10; i++) {
                panelCategories.add(new JPanel());
            }
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void newSale() {
        screenCards.show(CardsPanel, "cardMain");
        sale = new Sale();
        amountDue = 0;
        clearList();
        setTotalLabel(0);
        setItemsLabel(0);
    }

    private void addToList(Product p) {
        double price = p.getPrice();
        Object[] s;
        if (price > 1) {
            DecimalFormat df = new DecimalFormat("#.00"); // Set your desired format here.
            System.out.println(df.format(price));
            s = new Object[]{p.getShortName(), "£" + df.format(price)};
        } else {
            DecimalFormat df = new DecimalFormat("0.00"); // Set your desired format here.
            System.out.println(df.format(price));
            s = new Object[]{p.getShortName(), "£" + df.format(price)};
        }
        model.addRow(s);
    }

    private void clearList() {
        model.setRowCount(0);
    }

    private void setTotalLabel(double val) {
        if (val > 1) {
            DecimalFormat df = new DecimalFormat("#.00"); // Set your desired format here.
            lblTotal.setText("Total: £" + df.format(val));
            lblTotalDue.setText("Total Due: £" + df.format(val));
        } else {
            DecimalFormat df = new DecimalFormat("0.00"); // Set your desired format here.
            lblTotal.setText("Total: £" + df.format(val));
            lblTotalDue.setText("Total Due: £" + df.format(val));
        }
        amountDue = val;
    }

    private void setItemsLabel(int val) {
        lblItems.setText("Items: " + val);
    }

    public void login() {
        screenCards.show(CardsPanel, "cardLogin");
//        try {
//            int staffID = (int) NumberEntry.showNumberEntryDialog(this, "Enter Logon ID");
//            staff = sc.tillLogin(staffID);
//            lblStaff.setText(staff.getName());
//            return;
//        } catch (IOException | LoginException | SQLException ex) {
//            JOptionPane.showMessageDialog(this, ex, "Logon Error", JOptionPane.ERROR_MESSAGE);
//        }
//        login();
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
        JToggleButton cButton = new JToggleButton(c.getName());
        if (c.getColorValue() != 0) {
            cButton.setBackground(new Color(c.getColorValue()));
        }
        cButton.setSize(140, 50);
        cButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                categoryCards.show(panelMain, c.getName());
            }

        });
        cardsButonGroup.add(cButton);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(10, 5));

        List<Product> products;
        try {
            products = sc.getProductButtons(c.getID());
            for (Product p : products) {
                JButton pButton = new JButton(p.getShortName());
                if (p.getColorValue() != 0) {
                    pButton.setBackground(new Color(p.getColorValue()));
                }
                //pButton.setSize(140, 50);
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

            for (int i = products.size() - 1; i < 50; i++) {
                panel.add(new JPanel());
            }

            panelMain.add(panel, c.getName());
            panelCategories.add(cButton);
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addStaffButton(Staff s) {
        JButton button = new JButton(s.getName());
        button.setSize(120, 120);
        button.setPreferredSize(new Dimension(120, 120));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logon(s);
            }

        });
        panelStaff.add(button);
        repaint();
        revalidate();
    }

    private void logon(Staff s) {
        staff = s;
        try {
            staff = sc.tillLogin(s.getId());
            lblStaff.setText(staff.getName());
            screenCards.show(CardsPanel, "cardMain");
            return;
        } catch (IOException | LoginException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Logon Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void completeCurrentSale() {
        try {
            sale.setTime(new Date().getTime());
            sc.addSale(sale);
        } catch (IOException ex) {
        }
        if (amountDue < 0) {
            if (amountDue > 1) {
                DecimalFormat df = new DecimalFormat("#.00"); // Set your desired format here.
                lblTotal.setText("Total: £" + df.format(amountDue));
                lblTotalDue.setText("Total Due: £" + df.format(amountDue));
            } else {
                DecimalFormat df = new DecimalFormat("0.00"); // Set your desired format here.
                lblTotal.setText("Total: £" + df.format(amountDue));
                lblTotalDue.setText("Total Due: £" + df.format(amountDue));
                TouchDialog.showMessageDialog(this, "Change", "Change: £" + df.format(amountDue));
            }
        }
        newSale();
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        CardsPanel = new javax.swing.JPanel();
        panelMainScreen = new javax.swing.JPanel();
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
        jScrollPane2 = new javax.swing.JScrollPane();
        tblProducts = new javax.swing.JTable();
        lblTotal = new javax.swing.JLabel();
        lblItems = new javax.swing.JLabel();
        panelCategories = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        btnLogOut = new javax.swing.JButton();
        btnComplete = new javax.swing.JButton();
        panelPayment = new javax.swing.JPanel();
        btnBack = new javax.swing.JButton();
        lblTotalDue = new javax.swing.JLabel();
        btn£5 = new javax.swing.JButton();
        btn£10 = new javax.swing.JButton();
        btn£20 = new javax.swing.JButton();
        btn£50 = new javax.swing.JButton();
        btnExact = new javax.swing.JButton();
        btnCustomValue = new javax.swing.JButton();
        btnCardPayment = new javax.swing.JButton();
        btnCheque = new javax.swing.JButton();
        btnAddCustomer = new javax.swing.JButton();
        btnAddDiscount = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        lblCustomer = new javax.swing.JLabel();
        panelLogin = new javax.swing.JPanel();
        panelStaff = new javax.swing.JPanel();
        btnLogIn = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JTill Terminal");
        setIconImage(TillTouchClient.getIcon());
        setMaximumSize(null);
        setMinimumSize(null);
        setUndecorated(true);

        CardsPanel.setMaximumSize(new java.awt.Dimension(1024, 768));
        CardsPanel.setMinimumSize(new java.awt.Dimension(1024, 768));
        CardsPanel.setPreferredSize(new java.awt.Dimension(1024, 768));
        CardsPanel.setLayout(new java.awt.CardLayout());

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
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

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

        lblVerison.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblVerison.setText("V0.1B");

        lblHost.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblHost.setText("TERMINAL NAME HERE");

        lblTime.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblTime.setText("00:00");

        lblStaff.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
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
                .addComponent(lblStaff, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
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

        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Product", "Price"
            }
        ));
        jScrollPane2.setViewportView(tblProducts);

        lblTotal.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblTotal.setText("Total: £0.00");

        lblItems.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblItems.setText("Items: 0");

        panelCategories.setMaximumSize(new java.awt.Dimension(99999, 99999));
        panelCategories.setPreferredSize(new java.awt.Dimension(686, 100));
        panelCategories.setLayout(new java.awt.GridLayout(2, 5));

        btnLogOut.setText("Log Out");
        btnLogOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogOutActionPerformed(evt);
            }
        });

        btnComplete.setText("Complete");
        btnComplete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCompleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnLogOut, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnComplete, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnLogOut, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
            .addComponent(btnComplete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panelMainScreenLayout = new javax.swing.GroupLayout(panelMainScreen);
        panelMainScreen.setLayout(panelMainScreenLayout);
        panelMainScreenLayout.setHorizontalGroup(
            panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelMainScreenLayout.createSequentialGroup()
                        .addGroup(panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panelCategories, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(panelNumberEntry, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelMainScreenLayout.createSequentialGroup()
                                .addComponent(lblTotal)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblItems, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        panelMainScreenLayout.setVerticalGroup(
            panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMainScreenLayout.createSequentialGroup()
                        .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 482, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(panelCategories, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelMainScreenLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTotal)
                            .addComponent(lblItems, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelNumberEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        CardsPanel.add(panelMainScreen, "cardMain");

        btnBack.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        btnBack.setText("Back");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        lblTotalDue.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        lblTotalDue.setText("Total Due: £0.00");

        btn£5.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        btn£5.setText("£5");
        btn£5.setPreferredSize(new java.awt.Dimension(130, 130));

        btn£10.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        btn£10.setText("£10");
        btn£10.setPreferredSize(new java.awt.Dimension(130, 130));

        btn£20.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        btn£20.setText("£20");
        btn£20.setPreferredSize(new java.awt.Dimension(130, 130));

        btn£50.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        btn£50.setText("£50");
        btn£50.setPreferredSize(new java.awt.Dimension(130, 130));

        btnExact.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btnExact.setText("EXACT");
        btnExact.setPreferredSize(new java.awt.Dimension(130, 130));
        btnExact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExactActionPerformed(evt);
            }
        });

        btnCustomValue.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btnCustomValue.setText("<html>CUSTOM VALUE</html>");
        btnCustomValue.setPreferredSize(new java.awt.Dimension(130, 130));
        btnCustomValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomValueActionPerformed(evt);
            }
        });

        btnCardPayment.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btnCardPayment.setText("<html>Card Payment</html>");
        btnCardPayment.setPreferredSize(new java.awt.Dimension(130, 130));

        btnCheque.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        btnCheque.setText("Cheque");
        btnCheque.setPreferredSize(new java.awt.Dimension(130, 130));

        btnAddCustomer.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        btnAddCustomer.setText("Add Customer");
        btnAddCustomer.setPreferredSize(new java.awt.Dimension(130, 130));
        btnAddCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCustomerActionPerformed(evt);
            }
        });

        btnAddDiscount.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        btnAddDiscount.setText("Add Discount");
        btnAddDiscount.setPreferredSize(new java.awt.Dimension(130, 130));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel1.setText("Enter Payment Type:");

        lblCustomer.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        lblCustomer.setText("No Customer");

        javax.swing.GroupLayout panelPaymentLayout = new javax.swing.GroupLayout(panelPayment);
        panelPayment.setLayout(panelPaymentLayout);
        panelPaymentLayout.setHorizontalGroup(
            panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPaymentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPaymentLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelPaymentLayout.createSequentialGroup()
                        .addGroup(panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 538, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelPaymentLayout.createSequentialGroup()
                                .addComponent(btn£5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn£10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn£20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn£50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelPaymentLayout.createSequentialGroup()
                                .addGroup(panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(btnAddCustomer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(panelPaymentLayout.createSequentialGroup()
                                        .addComponent(btnExact, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnCustomValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(panelPaymentLayout.createSequentialGroup()
                                        .addComponent(btnCardPayment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnCheque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(btnAddDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblTotalDue, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
                                    .addComponent(lblCustomer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(0, 38, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelPaymentLayout.setVerticalGroup(
            panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPaymentLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn£5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn£10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn£20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn£50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnExact, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCheque, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnCustomValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnCardPayment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelPaymentLayout.createSequentialGroup()
                        .addComponent(lblTotalDue)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCustomer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPaymentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 157, Short.MAX_VALUE)
                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        CardsPanel.add(panelPayment, "cardPayment");

        btnLogIn.setText("Add Staff");
        btnLogIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogInActionPerformed(evt);
            }
        });

        btnExit.setText("Exit");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelLoginLayout = new javax.swing.GroupLayout(panelLogin);
        panelLogin.setLayout(panelLoginLayout);
        panelLoginLayout.setHorizontalGroup(
            panelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLoginLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelStaff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelLoginLayout.createSequentialGroup()
                        .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLogIn, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 739, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelLoginLayout.setVerticalGroup(
            panelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLoginLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(panelStaff, javax.swing.GroupLayout.PREFERRED_SIZE, 608, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnLogIn, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addComponent(btnExit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        CardsPanel.add(panelLogin, "cardLogin");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(CardsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(CardsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLogOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogOutActionPerformed
        logout();
    }//GEN-LAST:event_btnLogOutActionPerformed

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
        screenCards.show(CardsPanel, "cardPayment");
    }//GEN-LAST:event_btnCompleteActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        screenCards.show(CardsPanel, "cardMain");
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnLogInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogInActionPerformed
        int id = (int) NumberEntry.showNumberEntryDialog(this, "Enter Logon ID");
        try {
            Staff s = sc.getStaff(id);
            addStaffButton(s);
        } catch (IOException | StaffNotFoundException | SQLException ex) {
            showError(ex);
        }
    }//GEN-LAST:event_btnLogInActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        sc.close();
        System.exit(0);
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnExactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExactActionPerformed
        if (sale.getProducts().isEmpty()) {
            TouchDialog.showMessageDialog(this, "Sale", "Not in a sale");
        } else {
            for (int p : sale.getProducts()) {
                try {
                    sc.purchaseProduct(p);
                } catch (IOException | ProductNotFoundException | SQLException | OutOfStockException ex) {

                }
            }
            completeCurrentSale();
        }
    }//GEN-LAST:event_btnExactActionPerformed

    private void btnCustomValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomValueActionPerformed
        if (sale.getProducts().isEmpty()) {
            TouchDialog.showMessageDialog(this, "Sale", "Not in a sale");
        } else {
            double val = NumberEntry.showNumberEntryDialog(this, "Enter Amount") / 100;
            amountDue -= val;
            if (amountDue <= 0) {
                for (int p : sale.getProducts()) {
                    try {
                        sc.purchaseProduct(p);
                    } catch (IOException | ProductNotFoundException | SQLException | OutOfStockException ex) {

                    }
                }
                completeCurrentSale();
            } else {
                setTotalLabel(amountDue);
            }
        }
    }//GEN-LAST:event_btnCustomValueActionPerformed

    private void btnAddCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCustomerActionPerformed
        String customerID = JOptionPane.showInputDialog(this, "Enter Customer ID");
        if (!customerID.equals("")) {
            try {
                Customer c = sc.getCustomer(customerID);
                sale.setCustomer(c.getId());
                lblCustomer.setText("Customer: " + c.getName());
            } catch (IOException | CustomerNotFoundException | SQLException ex) {
                TouchDialog.showMessageDialog(this, "Error", ex);
            }
        }
    }//GEN-LAST:event_btnAddCustomerActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton11ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CardsPanel;
    private javax.swing.JButton btnAddCustomer;
    private javax.swing.JButton btnAddDiscount;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnCardPayment;
    private javax.swing.JButton btnCheque;
    private javax.swing.JButton btnComplete;
    private javax.swing.JButton btnCustomValue;
    private javax.swing.JButton btnExact;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnLogIn;
    private javax.swing.JButton btnLogOut;
    private javax.swing.JButton btn£10;
    private javax.swing.JButton btn£20;
    private javax.swing.JButton btn£5;
    private javax.swing.JButton btn£50;
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCustomer;
    private javax.swing.JLabel lblHost;
    private javax.swing.JLabel lblItems;
    private javax.swing.JLabel lblStaff;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTotalDue;
    private javax.swing.JLabel lblVerison;
    private javax.swing.JPanel panelCategories;
    private javax.swing.JPanel panelLogin;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelMainScreen;
    private javax.swing.JPanel panelNumberEntry;
    private javax.swing.JPanel panelPayment;
    private javax.swing.JPanel panelStaff;
    private javax.swing.JTable tblProducts;
    private javax.swing.JPanel topPanel;
    private javax.swing.JTextField txtNumber;
    // End of variables declaration//GEN-END:variables
}