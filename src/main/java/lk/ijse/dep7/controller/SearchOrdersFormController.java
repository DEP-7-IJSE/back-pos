package lk.ijse.dep7.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.dep7.dbUtils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.OrderDTO;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.service.OrderService;
import lk.ijse.dep7.util.OrderTM;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class SearchOrdersFormController {

    public AnchorPane root;
    public TextField txtSearch;
    private final OrderService orderService = new OrderService(SingleConnectionDataSource.getInstance().getConnection());
    public TableView<OrderTM> tblOrders;

    public void initialize() {
        tblOrders.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("orderId"));
        tblOrders.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        tblOrders.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("customerId"));
        tblOrders.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("customerName"));
        tblOrders.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("orderTotal"));

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            loadOrders();
        });
    }

    private void loadOrders() {
        try {
            List<OrderDTO> orderList = orderService.searchOrders(txtSearch.getText());
            tblOrders.getItems().clear();
            orderList.forEach(order -> tblOrders.getItems().add(new OrderTM(
                    order.getOrderId(),
                    order.getOrderDate(),
                    order.getCustomerId(),
                    order.getCustomerName(),
                    order.getOrderTotal().setScale(2)
            )));
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to fetch orders").show();
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

    public void tblOrders_OnMouseClicked(MouseEvent mouseEvent) {
    }
}
