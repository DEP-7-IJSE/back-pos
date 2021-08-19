package lk.ijse.dep7.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.dep7.dbUtils.SingleConnectionDataSource;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;
import lk.ijse.dep7.service.CustomerService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;

public class PlaceOrderFormController {

    public AnchorPane root;
    public JFXButton btnPlaceOrder;
    public JFXTextField txtCustomerName;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public JFXButton btnSave;
    public TableView tblOrderDetails;
    public JFXTextField txtUnitPrice;
    private final CustomerService customerService = new CustomerService(SingleConnectionDataSource.getInstance().getConnection());
    public JFXComboBox<String> cmbCustomerId;
    public JFXTextField txtQty;
    public Label lblId;
    public Label lblDate;
    public Label lblTotal;
    public JFXComboBox<String> cmbItemCode;

    public void initialize() throws FailedOperationException {
        //Todo: we need to generate and set new order id
        lblDate.setText(LocalDate.now().toString());
        btnPlaceOrder.setDisable(true);
        txtCustomerName.setEditable(false);
        cmbCustomerId.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                try {
                    txtCustomerName.setText(customerService.findCustomer(newValue).getName());
                } catch (NotFoundException e) {
                    e.printStackTrace(); //This can't be happened with our UI
                } catch (FailedOperationException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        loadAllCustomers();
    }

    private void loadAllCustomers() throws FailedOperationException {
        try {
            customerService.findAllCustomers().forEach(dto -> cmbCustomerId.getItems().add(dto.getId()));
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load customer ids").show();
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
    }

    public void txtQty_OnAction(ActionEvent actionEvent) {
    }

    public void btnPlaceOrder_OnAction(ActionEvent actionEvent) {
    }
}
