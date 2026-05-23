package views;

import db.Database;
import util.Session;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainView {

    private VBox view;

    // ================= CART =================
    private static class CartItem {
        String name;
        int qty;
        double price;
        double lineTotal;

        CartItem(String name, int qty, double price) {
            this.name = name;
            this.qty = qty;
            this.price = price;
            this.lineTotal = qty * price;
        }

        double getTotal() {
            return lineTotal;
        }
    }

    List<CartItem> cart = new ArrayList<>();

    public MainView(javafx.stage.Stage stage) {

        Label welcome = new Label("Welcome, " + Session.currentUsername + "!");
        Label title = new Label("Cafe POS System");

        // ================= CATEGORY =================
        ComboBox<String> categoryBox = new ComboBox<>();
        ComboBox<String> subcategoryBox = new ComboBox<>();

        loadCategories(categoryBox);

        categoryBox.setOnAction(e -> {
            loadSubcategories(subcategoryBox, categoryBox.getValue());
        });

        // ================= PRODUCT SEARCH =================
        TextField productSearch = new TextField();
        productSearch.setPromptText("Search product...");

        ListView<String> suggestionBox = new ListView<>();
        suggestionBox.setPrefHeight(120);

        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");

        Button addOrderBtn = new Button("Add Order");
        Label orderMsg = new Label();

        productSearch.textProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal.isEmpty()
                    || categoryBox.getValue() == null
                    || subcategoryBox.getValue() == null) {
                suggestionBox.getItems().clear();
                return;
            }

            try {
                Connection conn = Database.connect();

                String sql =
                        "SELECT product_name FROM products " +
                                "WHERE product_name ILIKE ? " +
                                "AND category_id = ? " +
                                "AND subcategory_id = ? " +
                                "LIMIT 5";

                PreparedStatement stmt = conn.prepareStatement(sql);

                stmt.setString(1, "%" + newVal + "%");
                stmt.setInt(2, getCategoryId(categoryBox.getValue()));
                stmt.setInt(3, getSubcategoryId(subcategoryBox.getValue()));

                ResultSet rs = stmt.executeQuery();

                suggestionBox.getItems().clear();

                while (rs.next()) {
                    suggestionBox.getItems().add(rs.getString("product_name"));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        suggestionBox.setOnMouseClicked(e -> {
            String selected = suggestionBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                productSearch.setText(selected);
            }
        });

        // ================= ADD TO CART =================
        addOrderBtn.setOnAction(e -> {

            try {
                String productName = productSearch.getText();
                int qty = Integer.parseInt(qtyField.getText());

                Connection conn = Database.connect();

                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT price FROM products WHERE product_name = ?"
                );

                stmt.setString(1, productName);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    double price = rs.getDouble("price");
                    cart.add(new CartItem(productName, qty, price));

                    orderMsg.setText("Added to cart!");

                    productSearch.clear();
                    qtyField.clear();
                }

            } catch (Exception ex) {
                orderMsg.setText("Error: " + ex.getMessage());
            }
        });

        // ================= RECEIPT =================
        TextArea receiptArea = new TextArea();
        receiptArea.setEditable(false);
        receiptArea.setPrefHeight(200);

        Button checkoutBtn = new Button("Checkout");

        checkoutBtn.setOnAction(e -> {

            try {
                if (cart.isEmpty()) {
                    receiptArea.setText("Cart is empty!");
                    return;
                }

                Connection conn = Database.connect();

                double total = 0;
                for (CartItem item : cart) {
                    total += item.getTotal();
                }

                String orderSQL =
                        "INSERT INTO orders (username, order_date, total_amount) " +
                                "VALUES (?, ?, ?) RETURNING order_id";

                PreparedStatement orderStmt = conn.prepareStatement(orderSQL);

                orderStmt.setString(1, Session.currentUsername);
                orderStmt.setObject(2, LocalDateTime.now());
                orderStmt.setDouble(3, total);

                ResultSet rs = orderStmt.executeQuery();

                int orderId = 0;
                if (rs.next()) orderId = rs.getInt("order_id");

                String itemSQL =
                        "INSERT INTO order_items (order_id, product_name, quantity, price, line_total) " +
                                "VALUES (?, ?, ?, ?, ?)";

                PreparedStatement itemStmt = conn.prepareStatement(itemSQL);

                for (CartItem item : cart) {
                    itemStmt.setInt(1, orderId);
                    itemStmt.setString(2, item.name);
                    itemStmt.setInt(3, item.qty);
                    itemStmt.setDouble(4, item.price);
                    itemStmt.setDouble(5, item.getTotal());
                    itemStmt.addBatch();
                }

                itemStmt.executeBatch();

                StringBuilder receipt = new StringBuilder();

                receipt.append("Transaction Dealer: ").append(Session.currentUsername).append("\n");
                receipt.append("Date: ").append(LocalDateTime.now()).append("\n");
                receipt.append("Order ID: ").append(orderId).append("\n\n");
                receipt.append("Orders:\n");

                for (CartItem item : cart) {
                    receipt.append(item.qty)
                            .append(" ")
                            .append(item.name)
                            .append(" ")
                            .append(item.getTotal())
                            .append("\n");
                }

                receipt.append("\nTotal: ").append(total);

                receiptArea.setText(receipt.toString());

                cart.clear();
                orderMsg.setText("Order saved!");

            } catch (Exception ex) {
                receiptArea.setText("Error: " + ex.getMessage());
            }
        });

        // ================= ADD PRODUCT (RESTORED) =================
        VBox addProductBox = new VBox(10);

        ComboBox<String> catAdd = new ComboBox<>();
        ComboBox<String> subAdd = new ComboBox<>();

        loadCategories(catAdd);

        catAdd.setOnAction(e -> loadSubcategories(subAdd, catAdd.getValue()));

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");

        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        Button saveBtn = new Button("Save Product");
        Label productMsg = new Label();

        saveBtn.setOnAction(e -> {
            try {
                Connection conn = Database.connect();

                String category = catAdd.getValue();
                String subcategory = subAdd.getValue();

                if (category == null || subcategory == null ||
                        nameField.getText().isEmpty()||
                        priceField.getText().isEmpty()){
                    productMsg.setText("Please Fill all Fields.");
                    return;
                }

                  int catId = getCategoryId(category);
                int subId = getSubcategoryId(subcategory);

                if(catId ==-1 || subId ==-1) {
                    productMsg.setText("Invalid category/ subcategory.");
                    return;
                }

                String sql =
                        "INSERT INTO products (product_name, price, category_id, subcategory_id) " +
                                "VALUES (?, ?, ?, ?)";

                PreparedStatement stmt = conn.prepareStatement(sql);

                stmt.setString(1, nameField.getText());
                stmt.setDouble(2, Double.parseDouble(priceField.getText()));
                stmt.setInt(3, catId);
                stmt.setInt(4, subId);

               int rows = stmt.executeUpdate();

               if (rows >0){
                   productMsg.setText("Product saved!");
               } else {
                   productMsg.setText("Error!");
               }

            } catch (Exception ex) {
                productMsg.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        addProductBox.getChildren().addAll(
                new Label("Add Product"),
                catAdd,
                subAdd,
                nameField,
                priceField,
                saveBtn,
                productMsg
        );

        // ================= LAYOUT (UNCHANGED STYLE) =================
        VBox content = new VBox(10,
                welcome,
                title,
                new Separator(),

                categoryBox,
                subcategoryBox,

                new Separator(),

                productSearch,
                suggestionBox,
                qtyField,
                addOrderBtn,
                orderMsg,

                new Separator(),

                checkoutBtn,
                receiptArea,

                new Separator(),

                addProductBox
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setPrefViewportHeight(600);

        view = new VBox(scrollPane);
        view.setPadding(new Insets(10));
    }

    // ================= DB METHODS =================

    private void loadCategories(ComboBox<String> box) {
        try {
            Connection conn = Database.connect();
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT category_name FROM categories"
            );

            box.getItems().clear();
            while (rs.next()) box.getItems().add(rs.getString("category_name"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSubcategories(ComboBox<String> box, String categoryName) {
        try {
            Connection conn = Database.connect();

            String sql =
                    "SELECT subcategory_name FROM subcategories " +
                            "WHERE category_id = (SELECT category_id FROM categories WHERE category_name = ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, categoryName);

            ResultSet rs = stmt.executeQuery();

            box.getItems().clear();
            while (rs.next()) box.getItems().add(rs.getString("subcategory_name"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getCategoryId(String name) throws Exception {
        Connection conn = Database.connect();

        PreparedStatement stmt = conn.prepareStatement(
                "SELECT category_id FROM categories WHERE category_name = ?"
        );

        stmt.setString(1, name);

        ResultSet rs = stmt.executeQuery();

        return rs.next() ? rs.getInt("category_id") : -1;
    }

    private int getSubcategoryId(String name) throws Exception {
        Connection conn = Database.connect();

        PreparedStatement stmt = conn.prepareStatement(
                "SELECT subcategory_id FROM subcategories WHERE subcategory_name = ?"
        );

        stmt.setString(1, name);

        ResultSet rs = stmt.executeQuery();

        return rs.next() ? rs.getInt("subcategory_id") : -1;
    }

    public VBox getView() {
        return view;
    }
}