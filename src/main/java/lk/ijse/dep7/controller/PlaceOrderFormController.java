package lk.ijse.dep7.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.dep7.dbUtils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.dto.OrderDetailDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;
import lk.ijse.dep7.service.CustomerService;
import lk.ijse.dep7.service.ItemService;
import lk.ijse.dep7.service.OrderService;
import lk.ijse.dep7.util.OrderDetailTM;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlaceOrderFormController {

    private final CustomerService customerService = new CustomerService(SingleConnectionDataSource.getInstance().getConnection());
    private final ItemService itemService = new ItemService(SingleConnectionDataSource.getInstance().getConnection());
    public AnchorPane root;
    public JFXButton btnPlaceOrder;
    public JFXTextField txtCustomerName;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public JFXButton btnSave;
    public TableView<OrderDetailTM> tblOrderDetails;
    public JFXTextField txtUnitPrice;
    public JFXComboBox<String> cmbCustomerId;
    public JFXTextField txtQty;
    public Label lblId;
    public Label lblDate;
    public Label lblTotal;
    public JFXComboBox<String> cmbItemCode;

    private final OrderService orderService = new OrderService(SingleConnectionDataSource.getInstance().getConnection());

    public void initialize() throws FailedOperationException {
        tblOrderDetails.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblOrderDetails.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblOrderDetails.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblOrderDetails.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tblOrderDetails.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));
        TableColumn<OrderDetailTM, Button> lastCol = (TableColumn<OrderDetailTM, Button>) tblOrderDetails.getColumns().get(5);

        lastCol.setCellValueFactory(param -> {
            Button btnDelete = new Button("Delete");
            btnDelete.setOnAction(event -> {
                tblOrderDetails.getItems().remove(param.getValue());
                tblOrderDetails.getSelectionModel().clearSelection();
                calculateTotal();
                enableOrDisablePlaceOrderButton();
            });

            return new ReadOnlyObjectWrapper<>(btnDelete);
        });

        lblId.setText("ORDER ID : " + generateNewId());
        lblDate.setText(LocalDate.now().toString());
        btnPlaceOrder.setDisable(true);
        txtCustomerName.setEditable(false);
        txtCustomerName.setFocusTraversable(false);
        txtDescription.setEditable(false);
        txtDescription.setFocusTraversable(false);
        txtUnitPrice.setEditable(false);
        txtUnitPrice.setFocusTraversable(false);
        txtQtyOnHand.setEditable(false);
        txtQtyOnHand.setFocusTraversable(false);
        txtQty.setOnAction(event -> btnSave.fire());
        txtQty.setEditable(false);
        btnSave.setDisable(true);

        cmbCustomerId.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            enableOrDisablePlaceOrderButton();

            if (newValue != null) {
                try {
                    txtCustomerName.setText(customerService.findCustomer(newValue).getName());
                } catch (NotFoundException e) {
                    e.printStackTrace(); //This can't be happened with our UI
                } catch (FailedOperationException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to load customer information").show();
                    throw new RuntimeException(e);
                }
            } else {
                txtCustomerName.clear();
            }
        });

        cmbItemCode.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            txtQty.setEditable(newValue != null);
            btnSave.setDisable(newValue == null);

            if (newValue != null) {
                try {
                    ItemDTO item = itemService.findItem(newValue);
                    txtDescription.setText(item.getDescription());
                    txtUnitPrice.setText(item.getUnitPrice().setScale(2).toString());

                    //txtQtyOnHand.setText(tblOrderDetails.getItems().stream().filter(detail-> detail.getCode().equals(item.getCode())).<Integer>map(detail-> item.getQtyOnHand() - detail.getQty()).findFirst().orElse(item.getQtyOnHand()) + "");
                    Optional<OrderDetailTM> optOrderDetail = tblOrderDetails.getItems().stream().filter(detail -> detail.getCode().equals(newValue)).findFirst();
                    txtQtyOnHand.setText(String.valueOf(optOrderDetail.map(detailTM -> item.getQtyOnHand() - detailTM.getQty()).orElseGet(item::getQtyOnHand)));
                } catch (NotFoundException e) {
                    e.printStackTrace();
                } catch (FailedOperationException e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to load item information").show();
                    throw new RuntimeException(e);
                }
            } else {
                txtDescription.clear();
                txtQty.clear();
                txtQtyOnHand.clear();
                txtUnitPrice.clear();
            }
        });

        tblOrderDetails.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedOderDetail) -> {
            if (selectedOderDetail != null) {
                cmbItemCode.setDisable(true);
                cmbItemCode.setValue(selectedOderDetail.getCode());
                btnSave.setText("Update");
                txtQtyOnHand.setText((Integer.parseInt(txtQtyOnHand.getText()) + selectedOderDetail.getQty()) + "");
                txtQty.setText(selectedOderDetail.getQty() + "");
            } else {
                btnSave.setText("Add");
                cmbItemCode.setDisable(false);
                cmbItemCode.getSelectionModel().clearSelection();
                txtQty.clear();
            }
        });

        loadAllCustomersIds();
        loadAllItemCodes();
    }

    private void loadAllCustomersIds() throws FailedOperationException {
        try {
            customerService.findAllCustomers().forEach(dto -> cmbCustomerId.getItems().add(dto.getId()));
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load customer ids").show();
            throw e;
        }
    }

    private void loadAllItemCodes() throws FailedOperationException {
        try {
            itemService.findAllItems().forEach(dto -> cmbItemCode.getItems().add(dto.getCode()));
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load item codes").show();
            throw e;
        }
    }

    @FXML
    private void navigateToHome(MouseEvent event) throws IOException {
        URL resource = this.getClass().getResource("/view/main-form.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.root.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        Platform.runLater(() -> primaryStage.sizeToScene());
    }

    public void btnAdd_OnAction(ActionEvent actionEvent) {

        if (!txtQty.getText().matches("\\d+") || Integer.parseInt(txtQty.getText()) <= 0 ||
                Integer.parseInt(txtQty.getText()) > Integer.parseInt(txtQtyOnHand.getText())) {
            new Alert(Alert.AlertType.ERROR, "Invalid Qty").show();
            txtQty.requestFocus();
            txtQty.selectAll();
            return;
        }

        String itemCode = cmbItemCode.getSelectionModel().getSelectedItem();
        String description = txtDescription.getText();
        BigDecimal unitPrice = new BigDecimal(txtUnitPrice.getText()).setScale(2);
        int qty = Integer.parseInt(txtQty.getText());
        BigDecimal total = unitPrice.multiply(new BigDecimal(qty)).setScale(2);

        boolean exists = tblOrderDetails.getItems().stream().anyMatch(detail -> detail.getCode().equals(itemCode));

        if (exists) {
            OrderDetailTM orderDetailTM = tblOrderDetails.getItems().stream().filter(detail -> detail.getCode().equals(itemCode)).findFirst().get();
            if (btnSave.getText().equalsIgnoreCase("Update")) {
                orderDetailTM.setQty(qty);
                orderDetailTM.setTotal(total);
                btnSave.setText("Add");
                tblOrderDetails.getSelectionModel().clearSelection();
                cmbItemCode.setDisable(false);
            } else {
                orderDetailTM.setQty(orderDetailTM.getQty() + qty);
                total = new BigDecimal(orderDetailTM.getQty()).multiply(unitPrice).setScale(2);
                orderDetailTM.setTotal(total);
            }
            tblOrderDetails.refresh();
        } else {
            tblOrderDetails.getItems().add(new OrderDetailTM(itemCode, description, qty, unitPrice, total));
        }

        cmbItemCode.getSelectionModel().clearSelection();
        cmbItemCode.requestFocus();
        calculateTotal();
        enableOrDisablePlaceOrderButton();
    }

    private void calculateTotal() {
        lblTotal.setText("TOTAL : " + tblOrderDetails.getItems().stream().map(OrderDetailTM::getTotal)
                .reduce(BigDecimal::add).orElse(new BigDecimal(0)).setScale(2));
    }

    private void enableOrDisablePlaceOrderButton() {
        /*        BigDecimal total = new BigDecimal(0);

        for (OrderDetailTM detail : tblOrderDetails.getItems()) {
            total = total.add(detail.getTotal());
        }*/

        btnPlaceOrder.setDisable(!(cmbCustomerId.getSelectionModel().getSelectedItem() != null && !tblOrderDetails.getItems().isEmpty()));
    }

    public void txtQty_OnAction(ActionEvent actionEvent) {
    }

    public void btnPlaceOrder_OnAction(ActionEvent actionEvent) throws FailedOperationException, DuplicateIdentifierException, NotFoundException {
        try {
            orderService.saveOrder(lblId.getText(), LocalDate.now(), cmbCustomerId.getValue(),
                    tblOrderDetails.getItems().stream().map(tm -> new OrderDetailDTO(tm.getCode(), tm.getQty(), tm.getUnitPrice())).collect(Collectors.toList()));
            new Alert(Alert.AlertType.INFORMATION, "Order has been placed successfully").show();
            cmbCustomerId.getSelectionModel().clearSelection();
            cmbItemCode.getSelectionModel().clearSelection();
            tblOrderDetails.getItems().clear();
            lblTotal.setText("TOTAL : ");
            lblId.setText("ORDER ID : " + generateNewId());
        } catch (FailedOperationException | NotFoundException | DuplicateIdentifierException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            throw e;
        }
    }

    private String generateNewId() throws FailedOperationException {
        try {
            return orderService.generateNewOrderId();
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            throw e;
        }
    }
}
