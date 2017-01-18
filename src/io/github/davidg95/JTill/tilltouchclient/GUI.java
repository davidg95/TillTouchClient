/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.tilltouchclient;

import io.github.davidg95.JTill.jtill.Button;
import io.github.davidg95.JTill.jtill.Customer;
import io.github.davidg95.JTill.jtill.CustomerNotFoundException;
import io.github.davidg95.JTill.jtill.LoginException;
import io.github.davidg95.JTill.jtill.OutOfStockException;
import io.github.davidg95.JTill.jtill.Product;
import io.github.davidg95.JTill.jtill.ProductNotFoundException;
import io.github.davidg95.JTill.jtill.Sale;
import io.github.davidg95.JTill.jtill.Screen;
import io.github.davidg95.JTill.jtill.ScreenNotFoundException;
import io.github.davidg95.JTill.jtill.ServerConnection;
import io.github.davidg95.JTill.jtill.Staff;
import io.github.davidg95.JTill.jtill.StaffNotFoundException;
import io.github.davidg95.JTill.jtill.TillInitData;
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

    private int quantity = 1;

    private Sale sale;
    private double amountDue;
    private final DefaultTableModel model;

    private final CardLayout categoryCards;
    private final CardLayout screenCards;
    private final ButtonGroup cardsButonGroup;

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
        //setExtendedState(JFrame.MAXIMIZED_BOTH);
        lblMessage.setText(TillInitData.initData.getLogonScreenMessage());
    }

    public void setButtons() {
        try {
            List<Screen> screens = sc.getAllScreens();
            panelCategories.setLayout(new GridLayout(2, 5));
            for (Screen s : screens) {
                addScreenButton(s);
            }

            for (int i = screens.size() - 1; i < 10; i++) {
                panelCategories.add(new JPanel());
            }
        } catch (IOException | SQLException ex) {
            showError(ex);
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
            s = new Object[]{quantity, p.getShortName(), "£" + df.format(price * quantity)};
        } else {
            DecimalFormat df = new DecimalFormat("0.00"); // Set your desired format here.
            s = new Object[]{quantity, p.getShortName(), "£" + df.format(price * quantity)};
        }
        //for (int i = 0; i < quantity; i++) {
            model.addRow(s);
        //}
        quantity = 1;
        btnQuantity.setText("Quantity: 1");
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
    }

    public void logout() {
        try {
            sc.tillLogout(staff.getId());
            lblStaff.setText("Not Logged In");
            staff = null;
        } catch (IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (StaffNotFoundException ex) {

        }
        login();
    }

    public void addScreenButton(Screen s) {
        JToggleButton cButton = new JToggleButton(s.getName());
        if (s.getColorValue() != 0) {
            cButton.setBackground(new Color(s.getColorValue()));
        }
        //cButton.setSize(140, 50);
        cButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                categoryCards.show(panelMain, s.getName());
            }

        });
        cardsButonGroup.add(cButton);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(10, 5));

        List<Button> buttons;
        try {
            buttons = sc.getButtonsOnScreen(s);
            for (Button b : buttons) {
                JButton pButton = new JButton(b.getName());
                if (b.getName().equals("[SPACE]")) {
                    panel.add(new JPanel());
                } else {
                    if (b.getColorValue() != 0) {
                        pButton.setBackground(new Color(b.getColorValue()));
                    }
                    //pButton.setSize(140, 50);
                    pButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                Product p = sc.getProduct(b.getProduct_id());
                                if (p.isOpen()) {
                                    double price = NumberEntry.showNumberEntryDialog(GUI.this, "Enter Price") / 100;
                                    if (price > 0) {
                                        p.setPrice(price);
                                        sale.addItem(p, quantity);
                                        setTotalLabel(sale.getTotal());
                                        setItemsLabel(sale.getItemCount());
                                        addToList(p);
                                    }
                                } else {
                                    sale.addItem(p, quantity);
                                    setTotalLabel(sale.getTotal());
                                    setItemsLabel(sale.getItemCount());
                                    addToList(p);
                                }
                            } catch (IOException | ProductNotFoundException | SQLException ex) {
                                showError(ex);
                            }
                        }

                    });
                    panel.add(pButton);
                }
            }

            for (int i = buttons.size() - 1; i < 49; i++) {
                panel.add(new JPanel());
            }

            panelMain.add(panel, s.getName());
            panelCategories.add(cButton);
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this, ex, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ScreenNotFoundException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
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
                TouchDialog.showMessageDialog(this, "Change", "Change: £" + df.format(-amountDue));
            }
        }
        newSale();
        if (TillInitData.initData.autoLogout) {
            logout();
        }
    }

    private void showError(Exception ex) {
        TouchDialog.showMessageDialog(this, "Error", ex);
    }

    private void addMoney(double val) {
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
        btnQuantity = new javax.swing.JButton();
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
        lblMessage = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("JTill Terminal");
        setIconImage(TillTouchClient.getIcon());
        setMaximumSize(new java.awt.Dimension(1024, 768));
        setMinimumSize(new java.awt.Dimension(1024, 768));

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

        panelMain.setPreferredSize(new java.awt.Dimension(708, 494));
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
                .addComponent(lblStaff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(111, 111, 111)
                .addComponent(lblTime))
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
                "Qty.", "Product", "Price"
            }
        ));
        jScrollPane2.setViewportView(tblProducts);
        if (tblProducts.getColumnModel().getColumnCount() > 0) {
            tblProducts.getColumnModel().getColumn(0).setResizable(false);
            tblProducts.getColumnModel().getColumn(1).setResizable(false);
            tblProducts.getColumnModel().getColumn(2).setResizable(false);
        }

        lblTotal.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblTotal.setText("Total: £0.00");

        lblItems.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblItems.setText("Items: 0");

        panelCategories.setMaximumSize(new java.awt.Dimension(708, 100));
        panelCategories.setMinimumSize(new java.awt.Dimension(708, 100));
        panelCategories.setPreferredSize(new java.awt.Dimension(708, 100));
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
            .addComponent(btnLogOut, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
            .addComponent(btnComplete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        btnQuantity.setText("Quantity: 1");
        btnQuantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuantityActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMainScreenLayout = new javax.swing.GroupLayout(panelMainScreen);
        panelMainScreen.setLayout(panelMainScreenLayout);
        panelMainScreenLayout.setHorizontalGroup(
            panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainScreenLayout.createSequentialGroup()
                .addGroup(panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainScreenLayout.createSequentialGroup()
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
                                .addComponent(lblItems, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnQuantity)))
                    .addComponent(topPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelMainScreenLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelMainScreenLayout.setVerticalGroup(
            panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMainScreenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelMainScreenLayout.createSequentialGroup()
                        .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 494, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelCategories, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33))
                    .addGroup(panelMainScreenLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTotal)
                            .addComponent(lblItems, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(panelNumberEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
        btn£5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn£5ActionPerformed(evt);
            }
        });

        btn£10.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        btn£10.setText("£10");
        btn£10.setPreferredSize(new java.awt.Dimension(130, 130));
        btn£10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn£10ActionPerformed(evt);
            }
        });

        btn£20.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        btn£20.setText("£20");
        btn£20.setPreferredSize(new java.awt.Dimension(130, 130));
        btn£20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn£20ActionPerformed(evt);
            }
        });

        btn£50.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        btn£50.setText("£50");
        btn£50.setPreferredSize(new java.awt.Dimension(130, 130));
        btn£50.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn£50ActionPerformed(evt);
            }
        });

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
                .addGroup(panelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLoginLayout.createSequentialGroup()
                        .addComponent(btnExit, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(btnLogIn, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(panelLoginLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(panelStaff, javax.swing.GroupLayout.DEFAULT_SIZE, 1004, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelLoginLayout.setVerticalGroup(
            panelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLoginLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(panelStaff, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                .addGroup(panelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLoginLayout.createSequentialGroup()
                        .addGap(165, 165, 165)
                        .addGroup(panelLoginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnExit, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                            .addComponent(btnLogIn, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelLoginLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblMessage)
                        .addGap(73, 73, 73))))
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
            Product p = sc.getProductByBarcode(barcode);
            sale.addItem(p, quantity);
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
            addMoney(val);
        }
    }//GEN-LAST:event_btnCustomValueActionPerformed

    private void btnAddCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCustomerActionPerformed
        if (sale.getCustomer() == -1) {
            int customerID = (int) NumberEntry.showNumberEntryDialog(this, "Enter Customer ID");
            if (customerID > 0) {
                try {
                    Customer c = sc.getCustomer(customerID);
                    sale.setCustomer(c.getId());
                    lblCustomer.setText("Customer: " + c.getName());
                    btnAddCustomer.setText("Remove Customer");
                    btnAddCustomer.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
                } catch (IOException | CustomerNotFoundException | SQLException ex) {
                    TouchDialog.showMessageDialog(this, "Error", ex);
                }
            }
        } else {
            sale.setCustomer(-1);
            btnAddCustomer.setText("Add Customer");
            btnAddCustomer.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
            lblCustomer.setText("No Customer");
        }
    }//GEN-LAST:event_btnAddCustomerActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton11ActionPerformed

    private void btn£5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn£5ActionPerformed
        if (sale.getProducts().isEmpty()) {
            TouchDialog.showMessageDialog(this, "Sale", "Not in a sale");
        } else {
            addMoney(5);
        }
    }//GEN-LAST:event_btn£5ActionPerformed

    private void btn£10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn£10ActionPerformed
        if (sale.getProducts().isEmpty()) {
            TouchDialog.showMessageDialog(this, "Sale", "Not in a sale");
        } else {
            addMoney(10);
        }
    }//GEN-LAST:event_btn£10ActionPerformed

    private void btn£20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn£20ActionPerformed
        if (sale.getProducts().isEmpty()) {
            TouchDialog.showMessageDialog(this, "Sale", "Not in a sale");
        } else {
            addMoney(20);
        }
    }//GEN-LAST:event_btn£20ActionPerformed

    private void btn£50ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn£50ActionPerformed
        if (sale.getProducts().isEmpty()) {
            TouchDialog.showMessageDialog(this, "Sale", "Not in a sale");
        } else {
            addMoney(50);
        }
    }//GEN-LAST:event_btn£50ActionPerformed

    private void btnQuantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuantityActionPerformed
        quantity = (int) NumberEntry.showNumberEntryDialog(this, "Enter Quantity for next Product");
        btnQuantity.setText("Quantity: " + quantity);
    }//GEN-LAST:event_btnQuantityActionPerformed

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
    private javax.swing.JButton btnQuantity;
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
    private javax.swing.JLabel lblMessage;
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
