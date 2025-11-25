import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryManagementSystem extends JFrame {

    // Data model
    static class Product implements Serializable {
        private static final long serialVersionUID = 1L;
        int id;
        String name;
        String category;
        int quantity;
        double price;

        public Product(int id, String name, String category, int quantity, double price) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.price = price;
        }
    }

    private List<Product> products = new ArrayList<>();
    private int nextId = 1;

    // GUI components
    private JTextField nameField, categoryField, quantityField, priceField, searchField;
    private JTable table;
    private DefaultTableModel tableModel;

    private static final String DATA_FILE = "inventory.dat";

    public InventoryManagementSystem() {
        setTitle("Inventory Management System");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initTopPanel();
        initTable();
        initBottomPanel();

        loadData();
        refreshTable();

        setVisible(true);
    }

    private void initTopPanel() {
        JPanel formPanel = new JPanel(new GridLayout(2, 5, 10, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Details"));

        nameField = new JTextField();
        categoryField = new JTextField();
        quantityField = new JTextField();
        priceField = new JTextField();
        searchField = new JTextField();

        formPanel.add(new JLabel("Name:"));
        formPanel.add(new JLabel("Category:"));
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(new JLabel("Price:"));
        formPanel.add(new JLabel("Search by Name:"));

        formPanel.add(nameField);
        formPanel.add(categoryField);
        formPanel.add(quantityField);
        formPanel.add(priceField);
        formPanel.add(searchField);

        // search feature
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterTable(searchField.getText().trim());
            }
        });

        add(formPanel, BorderLayout.NORTH);
    }

    private void initTable() {
        String[] columns = {"ID", "Name", "Category", "Quantity", "Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false; // no direct editing
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);

        // When row is selected, show data in input fields
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                nameField.setText(tableModel.getValueAt(row, 1).toString());
                categoryField.setText(tableModel.getValueAt(row, 2).toString());
                quantityField.setText(tableModel.getValueAt(row, 3).toString());
                priceField.setText(tableModel.getValueAt(row, 4).toString());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Inventory"));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initBottomPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton deleteBtn = new JButton("Delete");
        JButton clearBtn = new JButton("Clear");
        JButton saveBtn = new JButton("Save Data");

        addBtn.addActionListener(e -> addProduct());
        updateBtn.addActionListener(e -> updateProduct());
        deleteBtn.addActionListener(e -> deleteProduct());
        clearBtn.addActionListener(e -> clearFields());
        saveBtn.addActionListener(e -> {
            saveData();
            JOptionPane.showMessageDialog(this, "Data saved successfully.");
        });

        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(saveBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addProduct() {
        try {
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            int qty = Integer.parseInt(quantityField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());

            if (name.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Category are required.");
                return;
            }

            Product p = new Product(nextId++, name, category, qty, price);
            products.add(p);
            addProductToTable(p);
            clearFields();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity and Price must be numeric.");
        }
    }

    private void updateProduct() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product to update.");
            return;
        }

        try {
            String name = nameField.getText().trim();
            String category = categoryField.getText().trim();
            int qty = Integer.parseInt(quantityField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());

            if (name.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Category are required.");
                return;
            }

            int id = (int) tableModel.getValueAt(row, 0);

            for (Product p : products) {
                if (p.id == id) {
                    p.name = name;
                    p.category = category;
                    p.quantity = qty;
                    p.price = price;
                    break;
                }
            }
            refreshTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity and Price must be numeric.");
        }
    }

    private void deleteProduct() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this product?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) tableModel.getValueAt(row, 0);
            products.removeIf(p -> p.id == id);
            refreshTable();
            clearFields();
        }
    }

    private void clearFields() {
        nameField.setText("");
        categoryField.setText("");
        quantityField.setText("");
        priceField.setText("");
        table.clearSelection();
    }

    private void addProductToTable(Product p) {
        tableModel.addRow(new Object[]{p.id, p.name, p.category, p.quantity, p.price});
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Product p : products) {
            addProductToTable(p);
        }
    }

    private void filterTable(String keyword) {
        tableModel.setRowCount(0);
        for (Product p : products) {
            if (p.name.toLowerCase().contains(keyword.toLowerCase())) {
                addProductToTable(p);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            products = (List<Product>) ois.readObject();
            // find max id for nextId
            int maxId = 0;
            for (Product p : products) {
                if (p.id > maxId) maxId = p.id;
            }
            nextId = maxId + 1;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load data: " + e.getMessage());
        }
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(products);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InventoryManagementSystem::new);
    }
}
