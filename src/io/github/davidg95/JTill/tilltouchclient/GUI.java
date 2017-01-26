/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.davidg95.JTill.tilltouchclient;

import io.github.davidg95.JTill.jtill.Button;
import io.github.davidg95.JTill.jtill.Category;
import io.github.davidg95.JTill.jtill.CategoryNotFoundException;
import io.github.davidg95.JTill.jtill.Customer;
import io.github.davidg95.JTill.jtill.CustomerNotFoundException;
import io.github.davidg95.JTill.jtill.LoginException;
import io.github.davidg95.JTill.jtill.OutOfStockException;
import io.github.davidg95.JTill.jtill.Product;
import io.github.davidg95.JTill.jtill.ProductNotFoundException;
import io.github.davidg95.JTill.jtill.RestrictionException;
import io.github.davidg95.JTill.jtill.Sale;
import io.github.davidg95.JTill.jtill.SaleItem;
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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    private SaleItem lastAdded;

    /**
     * Creates new form GUI
     */
    public GUI(ServerConnection sc) {
        this.sc = sc;
        initComponents();
        model = (DefaultTableModel) tblProducts.getModel();
        lblHost.setText(TillTouchClient.HOST_NAME);
        categoryCards = (CardLayout) panelMain.getLayout();
        screenCards = (CardLayout) CardsPanel.getLayout();
        cardsButonGroup = new ButtonGroup();
        newSale();
        ClockThread.setClockLabel(lblTime);
        screenCards.show(CardsPanel, "cardLogin");
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
            if (cardsButonGroup.getElements().hasMoreElements()) {
                cardsButonGroup.getElements().nextElement().doClick();
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
        if (cardsButonGroup.getElements().hasMoreElements()) {
            cardsButonGroup.getElements().nextElement().doClick();
        }
    }

    private void updateList() {
        model.setRowCount(0);
        for (SaleItem item : sale.getSaleItems()) {
            DecimalFormat df;
            if (item.getPrice().compareTo(BigDecimal.ZERO) > 1) {
                df = new DecimalFormat("#.00");
            } else {
                df = new DecimalFormat("0.00");
            }
            Object[] s = new Object[]{item.getQuantity(), item.getProduct().getShortName(), df.format(item.getPrice().doubleValue())};
            model.addRow(s);
        }
        quantity = 1;
        btnQuantity.setText("Quantity: 1");
        setTotalLabel(sale.getTotal().doubleValue());
        setItemsLabel(sale.getTotalItemCount());
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

    private void addProduct(Product p) {
        try {
            checkRestrictions(p);
            if (p.isOpen()) {
                BigDecimal price;
                if (txtNumber.getText().equals("")) {
                    price = new BigDecimal(Double.toString(NumberEntry.showNumberEntryDialog(GUI.this, "Enter Price") / 100));
                } else {
                    price = new BigDecimal(Double.toString(Integer.parseInt(txtNumber.getText()) / 100));
                    txtNumber.setText("");
                }
                if (price.compareTo(BigDecimal.ZERO) > 0) {
                    p.setPrice(price);
                    lastAdded = sale.addItem(p, quantity);
                    updateList();
                }
            } else {
                lastAdded = sale.addItem(p, quantity);
                updateList();
            }
        } catch (IOException | SQLException | CategoryNotFoundException ex) {
            showError(ex);
        } catch (RestrictionException ex) {
            TouchDialog.showMessageDialog(GUI.this, "Restriction", ex);
        }
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
            for (int i = 0; i < buttons.size(); i++) {
                for (Button b : buttons) {
                    if (b.getOrder() == i) {
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
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            productButtonAction(b);
                                        }
                                    }.start();
                                }
                            });
                            panel.add(pButton);
                        }
                        break;
                    }
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

    private void productButtonAction(Button b) {
        try {
            Product p = sc.getProduct(b.getProduct_id());
            addProduct(p);
        } catch (IOException | SQLException ex) {
            showError(ex);
        } catch (ProductNotFoundException ex) {
            TouchDialog.showMessageDialog(this, "Product", ex);
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
            for (SaleItem item : sale.getSaleItems()) {
                try {
                    sc.purchaseProduct(item.getProduct().getProductCode(), item.getQuantity());
                } catch (IOException | ProductNotFoundException | SQLException | OutOfStockException ex) {

                }
            }
            completeCurrentSale();
        } else {
            setTotalLabel(amountDue);
        }
    }

    private void checkRestrictions(Product p) throws IOException, SQLException, CategoryNotFoundException, RestrictionException {
        Category c = sc.getCategory(p.getCategoryID());
        if (c.isTimeRestrict()) {
            try {
                Calendar now = Calendar.getInstance();
                int hours = now.get(Calendar.HOUR_OF_DAY);
                int minutes = now.get(Calendar.MINUTE);
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                Time timeNow = new Time(sdf.parse(hours + ":" + minutes + ":00").getTime());
                System.out.println(timeNow);
                if (timeNow.before(c.getStartSell())) {
                    System.out.println("To early");
                }
                if (c.getStartSell().before(timeNow) && c.getEndSell().after(timeNow)) {
                } else {
                    throw new RestrictionException("This can only be sold between " + c.getStartSell() + " and " + c.getEndSell());
                }
            } catch (ParseException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (c.getMinAge() > 0) {
            TouchDialog.showMessageDialog(this, "Age Restriction", "Check customer is over " + c.getMinAge());
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
        btn1 = new javax.swing.JButton();
        btn2 = new javax.swing.JButton();
        btn3 = new javax.swing.JButton();
        btn5 = new javax.swing.JButton();
        btn6 = new javax.swing.JButton();
        btn4 = new javax.swing.JButton();
        btn8 = new javax.swing.JButton();
        btn9 = new javax.swing.JButton();
        btn7 = new javax.swing.JButton();
        btn00 = new javax.swing.JButton();
        btnEnter = new javax.swing.JButton();
        btn0 = new javax.swing.JButton();
        txtNumber = new javax.swing.JTextField();
        btnClear = new javax.swing.JButton();
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
        btnLookup = new javax.swing.JButton();
        btnQuantity = new javax.swing.JButton();
        btnVoid = new javax.swing.JButton();
        btnVoidSelected = new javax.swing.JButton();
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
        setUndecorated(true);

        CardsPanel.setMaximumSize(new java.awt.Dimension(1024, 768));
        CardsPanel.setMinimumSize(new java.awt.Dimension(1024, 768));
        CardsPanel.setPreferredSize(new java.awt.Dimension(1024, 768));
        CardsPanel.setLayout(new java.awt.CardLayout());

        btn1.setText("1");
        btn1.setMaximumSize(new java.awt.Dimension(100, 60));
        btn1.setMinimumSize(new java.awt.Dimension(100, 60));
        btn1.setPreferredSize(new java.awt.Dimension(100, 60));
        btn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn1ActionPerformed(evt);
            }
        });

        btn2.setText("2");
        btn2.setMaximumSize(new java.awt.Dimension(100, 60));
        btn2.setMinimumSize(new java.awt.Dimension(100, 60));
        btn2.setPreferredSize(new java.awt.Dimension(100, 60));
        btn2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn2ActionPerformed(evt);
            }
        });

        btn3.setText("3");
        btn3.setMaximumSize(new java.awt.Dimension(100, 60));
        btn3.setMinimumSize(new java.awt.Dimension(100, 60));
        btn3.setPreferredSize(new java.awt.Dimension(100, 60));
        btn3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn3ActionPerformed(evt);
            }
        });

        btn5.setText("5");
        btn5.setMaximumSize(new java.awt.Dimension(100, 60));
        btn5.setMinimumSize(new java.awt.Dimension(100, 60));
        btn5.setPreferredSize(new java.awt.Dimension(100, 60));
        btn5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn5ActionPerformed(evt);
            }
        });

        btn6.setText("6");
        btn6.setMaximumSize(new java.awt.Dimension(100, 60));
        btn6.setMinimumSize(new java.awt.Dimension(100, 60));
        btn6.setPreferredSize(new java.awt.Dimension(100, 60));
        btn6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn6ActionPerformed(evt);
            }
        });

        btn4.setText("4");
        btn4.setMaximumSize(new java.awt.Dimension(100, 60));
        btn4.setMinimumSize(new java.awt.Dimension(100, 60));
        btn4.setPreferredSize(new java.awt.Dimension(100, 60));
        btn4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn4ActionPerformed(evt);
            }
        });

        btn8.setText("8");
        btn8.setMaximumSize(new java.awt.Dimension(100, 60));
        btn8.setMinimumSize(new java.awt.Dimension(100, 60));
        btn8.setPreferredSize(new java.awt.Dimension(100, 60));
        btn8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn8ActionPerformed(evt);
            }
        });

        btn9.setText("9");
        btn9.setMaximumSize(new java.awt.Dimension(100, 60));
        btn9.setMinimumSize(new java.awt.Dimension(100, 60));
        btn9.setPreferredSize(new java.awt.Dimension(100, 60));
        btn9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn9ActionPerformed(evt);
            }
        });

        btn7.setText("7");
        btn7.setMaximumSize(new java.awt.Dimension(100, 60));
        btn7.setMinimumSize(new java.awt.Dimension(100, 60));
        btn7.setPreferredSize(new java.awt.Dimension(100, 60));
        btn7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn7ActionPerformed(evt);
            }
        });

        btn00.setText("00");
        btn00.setMaximumSize(new java.awt.Dimension(100, 60));
        btn00.setMinimumSize(new java.awt.Dimension(100, 60));
        btn00.setPreferredSize(new java.awt.Dimension(100, 60));
        btn00.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn00ActionPerformed(evt);
            }
        });

        btnEnter.setText("Enter");
        btnEnter.setMaximumSize(new java.awt.Dimension(100, 60));
        btnEnter.setMinimumSize(new java.awt.Dimension(100, 60));
        btnEnter.setPreferredSize(new java.awt.Dimension(100, 60));
        btnEnter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnterActionPerformed(evt);
            }
        });

        btn0.setText("0");
        btn0.setMaximumSize(new java.awt.Dimension(100, 60));
        btn0.setMinimumSize(new java.awt.Dimension(100, 60));
        btn0.setPreferredSize(new java.awt.Dimension(100, 60));
        btn0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn0ActionPerformed(evt);
            }
        });

        txtNumber.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txtNumber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNumberActionPerformed(evt);
            }
        });

        btnClear.setText("C");
        btnClear.setMaximumSize(new java.awt.Dimension(100, 60));
        btnClear.setMinimumSize(new java.awt.Dimension(100, 60));
        btnClear.setPreferredSize(new java.awt.Dimension(100, 60));
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelNumberEntryLayout = new javax.swing.GroupLayout(panelNumberEntry);
        panelNumberEntry.setLayout(panelNumberEntryLayout);
        panelNumberEntryLayout.setHorizontalGroup(
            panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelNumberEntryLayout.createSequentialGroup()
                .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelNumberEntryLayout.createSequentialGroup()
                        .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(panelNumberEntryLayout.createSequentialGroup()
                                .addComponent(btn4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(btn5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(panelNumberEntryLayout.createSequentialGroup()
                                    .addComponent(btn7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(btn8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(panelNumberEntryLayout.createSequentialGroup()
                                    .addComponent(btn1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, 0)
                                    .addComponent(btn2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, 0)
                        .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btn9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelNumberEntryLayout.createSequentialGroup()
                        .addComponent(btn0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(btn00, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(btnEnter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(txtNumber)
        );
        panelNumberEntryLayout.setVerticalGroup(
            panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelNumberEntryLayout.createSequentialGroup()
                .addComponent(txtNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0)
                .addGroup(panelNumberEntryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEnter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn00, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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

        btnLookup.setText("Product Lookup");
        btnLookup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLookupActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnLogOut, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLookup, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnComplete, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(btnLogOut, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
            .addComponent(btnComplete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(btnLookup, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        btnQuantity.setText("Quantity: 1");
        btnQuantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuantityActionPerformed(evt);
            }
        });

        btnVoid.setText("Void Last");
        btnVoid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVoidActionPerformed(evt);
            }
        });

        btnVoidSelected.setText("Void Selected");
        btnVoidSelected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVoidSelectedActionPerformed(evt);
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
                            .addGroup(panelMainScreenLayout.createSequentialGroup()
                                .addComponent(btnQuantity)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnVoid)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnVoidSelected))))
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
                .addGroup(panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMainScreenLayout.createSequentialGroup()
                        .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 494, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelCategories, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11))
                    .addGroup(panelMainScreenLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTotal, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblItems, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelMainScreenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnQuantity, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                            .addComponent(btnVoid, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnVoidSelected, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelNumberEntry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
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
            addProduct(p);
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
        new Thread() {
            @Override
            public void run() {
                addMoney(sale.getTotal().doubleValue());
            }
        }.start();
//        if (sale.getSaleItems().isEmpty()) {
//            TouchDialog.showMessageDialog(this, "Sale", "Not in a sale");
//        } else {
//            for (SaleItem p : sale.getSaleItems()) {
//                try {
//                    sc.purchaseProduct(p.getProduct().getProductCode(), p.getQuantity());
//                } catch (IOException | ProductNotFoundException | SQLException | OutOfStockException ex) {
//
//                }
//            }
//            completeCurrentSale();
//        }
    }//GEN-LAST:event_btnExactActionPerformed

    private void btnCustomValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomValueActionPerformed
        if (sale.getSaleItems().isEmpty()) {
            TouchDialog.showMessageDialog(this, "Sale", "Not in a sale");
        } else {
            double val = NumberEntry.showNumberEntryDialog(this, "Enter Amount") / 100;
            new Thread() {
                @Override
                public void run() {
                    addMoney(val);
                }
            }.start();
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

    private void btnEnterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnterActionPerformed
        try {
            Product p = sc.getProductByBarcode(txtNumber.getText());
            sale.addItem(p, quantity);
            updateList();
        } catch (IOException | ProductNotFoundException | SQLException ex) {
            showError(ex);
        }
    }//GEN-LAST:event_btnEnterActionPerformed

    private void btn£5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn£5ActionPerformed
        if (sale.getSaleItems().isEmpty()) {
            TouchDialog.showMessageDialog(this, "Sale", "Not in a sale");
        } else {
            new Thread() {
                @Override
                public void run() {
                    addMoney(5);
                }
            }.start();
        }
    }//GEN-LAST:event_btn£5ActionPerformed

    private void btn£10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn£10ActionPerformed
        if (sale.getSaleItems().isEmpty()) {
            TouchDialog.showMessageDialog(this, "Sale", "Not in a sale");
        } else {
            new Thread() {
                @Override
                public void run() {
                    addMoney(10);
                }
            }.start();
        }
    }//GEN-LAST:event_btn£10ActionPerformed

    private void btn£20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn£20ActionPerformed
        if (sale.getSaleItems().isEmpty()) {
            TouchDialog.showMessageDialog(this, "Sale", "Not in a sale");
        } else {
            new Thread() {
                @Override
                public void run() {
                    addMoney(20);
                }
            }.start();
        }
    }//GEN-LAST:event_btn£20ActionPerformed

    private void btn£50ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn£50ActionPerformed
        if (sale.getSaleItems().isEmpty()) {
            TouchDialog.showMessageDialog(this, "Sale", "Not in a sale");
        } else {
            new Thread() {
                @Override
                public void run() {
                    addMoney(50);
                }
            }.start();
        }
    }//GEN-LAST:event_btn£50ActionPerformed

    private void btnQuantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuantityActionPerformed
        if (txtNumber.getText().equals("")) {
            quantity = (int) NumberEntry.showNumberEntryDialog(this, "Enter Quantity for next Product");
        } else {
            quantity = Integer.parseInt(txtNumber.getText());
            txtNumber.setText("");
        }
        btnQuantity.setText("Quantity: " + quantity);
    }//GEN-LAST:event_btnQuantityActionPerformed

    private void btn7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn7ActionPerformed
        txtNumber.setText(txtNumber.getText() + "7");
    }//GEN-LAST:event_btn7ActionPerformed

    private void btn3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn3ActionPerformed
        txtNumber.setText(txtNumber.getText() + "3");
    }//GEN-LAST:event_btn3ActionPerformed

    private void btn8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn8ActionPerformed
        txtNumber.setText(txtNumber.getText() + "8");
    }//GEN-LAST:event_btn8ActionPerformed

    private void btn9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn9ActionPerformed
        txtNumber.setText(txtNumber.getText() + "9");
    }//GEN-LAST:event_btn9ActionPerformed

    private void btn4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn4ActionPerformed
        txtNumber.setText(txtNumber.getText() + "4");
    }//GEN-LAST:event_btn4ActionPerformed

    private void btn5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn5ActionPerformed
        txtNumber.setText(txtNumber.getText() + "5");
    }//GEN-LAST:event_btn5ActionPerformed

    private void btn6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn6ActionPerformed
        txtNumber.setText(txtNumber.getText() + "6");
    }//GEN-LAST:event_btn6ActionPerformed

    private void btn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn1ActionPerformed
        txtNumber.setText(txtNumber.getText() + "1");
    }//GEN-LAST:event_btn1ActionPerformed

    private void btn2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn2ActionPerformed
        txtNumber.setText(txtNumber.getText() + "2");
    }//GEN-LAST:event_btn2ActionPerformed

    private void btn0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn0ActionPerformed
        txtNumber.setText(txtNumber.getText() + "0");
    }//GEN-LAST:event_btn0ActionPerformed

    private void btn00ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn00ActionPerformed
        txtNumber.setText(txtNumber.getText() + "00");
    }//GEN-LAST:event_btn00ActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        txtNumber.setText("");
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnLookupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLookupActionPerformed
        String terms = JOptionPane.showInputDialog(this, "Enter search terms", "Product Lookup", JOptionPane.PLAIN_MESSAGE);
        try {
            List<Product> products = sc.productLookup(terms);
            if (products.isEmpty()) {
                TouchDialog.showMessageDialog(this, "Lookup", "No matches found");
            } else {
                Product p = ProductSelectDialog.showDialog(this, products);
                if (p != null) {
                    addProduct(p);
                }
            }
        } catch (IOException | SQLException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_btnLookupActionPerformed

    private void btnVoidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoidActionPerformed
        if (lastAdded != null) {
            sale.voidItem(lastAdded);
            lastAdded = null;
            updateList();
        }
    }//GEN-LAST:event_btnVoidActionPerformed

    private void btnVoidSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoidSelectedActionPerformed
        int index = tblProducts.getSelectedRow();
        if (index > -1) {
            SaleItem item = sale.getSaleItems().get(index);
            sale.voidItem(item);
            updateList();
        }
    }//GEN-LAST:event_btnVoidSelectedActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CardsPanel;
    private javax.swing.JButton btn0;
    private javax.swing.JButton btn00;
    private javax.swing.JButton btn1;
    private javax.swing.JButton btn2;
    private javax.swing.JButton btn3;
    private javax.swing.JButton btn4;
    private javax.swing.JButton btn5;
    private javax.swing.JButton btn6;
    private javax.swing.JButton btn7;
    private javax.swing.JButton btn8;
    private javax.swing.JButton btn9;
    private javax.swing.JButton btnAddCustomer;
    private javax.swing.JButton btnAddDiscount;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnCardPayment;
    private javax.swing.JButton btnCheque;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnComplete;
    private javax.swing.JButton btnCustomValue;
    private javax.swing.JButton btnEnter;
    private javax.swing.JButton btnExact;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnLogIn;
    private javax.swing.JButton btnLogOut;
    private javax.swing.JButton btnLookup;
    private javax.swing.JButton btnQuantity;
    private javax.swing.JButton btnVoid;
    private javax.swing.JButton btnVoidSelected;
    private javax.swing.JButton btn£10;
    private javax.swing.JButton btn£20;
    private javax.swing.JButton btn£5;
    private javax.swing.JButton btn£50;
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
