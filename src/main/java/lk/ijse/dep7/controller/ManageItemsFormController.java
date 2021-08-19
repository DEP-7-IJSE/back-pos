package lk.ijse.dep7.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lk.ijse.dep7.dbUtils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;
import lk.ijse.dep7.service.ItemService;
import lk.ijse.dep7.util.ItemTM;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;

public class ManageItemsFormController {

    public AnchorPane root;
    public JFXTextField txtCode;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public JFXButton btnDelete;
    public JFXButton btnSave;
    private final ItemService itemService = new ItemService(SingleConnectionDataSource.getInstance().getConnection());
    public TableView<ItemTM> tblItems;
    public JFXTextField txtUnitPrice;
    public JFXButton btnAddNewItem;

    public void initialize() throws FailedOperationException {
        tblItems.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblItems.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblItems.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        tblItems.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        initUI();

        tblItems.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnDelete.setDisable(newValue == null);
            btnSave.setText(newValue != null ? "Update" : "Save");
            btnSave.setDisable(newValue == null);

            if (newValue != null) {
                txtCode.setText(newValue.getCode());
                txtDescription.setText(newValue.getDescription());
                txtQtyOnHand.setText(String.valueOf(newValue.getQtyOnHand()));
                txtUnitPrice.setText(newValue.getUnitPrice().toString());

                txtCode.setDisable(false);
                txtDescription.setDisable(false);
                txtUnitPrice.setDisable(false);
                txtQtyOnHand.setDisable(false);

                btnSave.setText("Update");
            } else {
                btnSave.setText("Save");
            }
        });

        txtUnitPrice.setOnAction(event -> btnSave.fire());
        loadAllItems();
    }

    @FXML
    private void navigateToHome(MouseEvent event) throws IOException {
        URL resource = this.getClass().getResource("/view/main-form.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.root.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        Platform.runLater(primaryStage::sizeToScene);
    }

    public void btnAddNew_OnAction(ActionEvent actionEvent) throws FailedOperationException {
        txtCode.setDisable(false);
        txtDescription.setDisable(false);
        txtQtyOnHand.setDisable(false);
        txtUnitPrice.setDisable(false);

        txtCode.clear();
        txtCode.setText(generateNewId());
        txtDescription.clear();
        txtQtyOnHand.clear();
        txtUnitPrice.clear();
        txtDescription.requestFocus();
        btnSave.setDisable(false);
        btnSave.setText("Save");
        tblItems.getSelectionModel().clearSelection();
    }

    private String generateNewId() throws FailedOperationException {
        try {
            return itemService.generateNewItemCode();
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            throw e;
        }
    }

    public void btnDelete_OnAction(ActionEvent actionEvent) throws FailedOperationException {
        try {
            itemService.deleteItem(tblItems.getSelectionModel().getSelectedItem().getCode());
            tblItems.getItems().remove(tblItems.getSelectionModel().getSelectedItem());
            tblItems.getSelectionModel().clearSelection();
            initUI();
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            throw e;
        } catch (NotFoundException e) {
            e.printStackTrace();  // This is never going to happen with our UI design
        }
    }

    public void btnSave_OnAction(ActionEvent actionEvent) throws FailedOperationException {
        String code = txtCode.getText();
        String description = txtDescription.getText();
        String unitPrice = txtUnitPrice.getText();
        String qtyOnHand = txtQtyOnHand.getText();

        if (!description.matches("[A-Za-z ]+")) {
            new Alert(Alert.AlertType.ERROR, "Invalid description").show();
            txtDescription.requestFocus();
            return;
        } else if (!unitPrice.matches("\\d*.?\\d+")) {
            new Alert(Alert.AlertType.ERROR, "Invalid price").show();
            txtUnitPrice.requestFocus();
            return;
        } else if (!qtyOnHand.matches("\\d+")) {
            new Alert(Alert.AlertType.ERROR, "Invalid qty").show();
            txtUnitPrice.requestFocus();
            return;
        }

        try {
            if (btnSave.getText().equalsIgnoreCase("save")) {
                try {
                    itemService.saveItem(new ItemDTO(code, description, new BigDecimal(unitPrice), Integer.parseInt(qtyOnHand)));
                } catch (DuplicateIdentifierException e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
                }
                tblItems.getItems().add(new ItemTM(code, description, new BigDecimal(unitPrice), Integer.parseInt(qtyOnHand)));
            } else {
                try {
                    itemService.updateItem(new ItemDTO(code, description, new BigDecimal(unitPrice), Integer.parseInt(qtyOnHand)));
                    ItemTM selectedItem = tblItems.getSelectionModel().getSelectedItem();
                    selectedItem.setDescription(description);
                    selectedItem.setQtyOnHand(Integer.parseInt(qtyOnHand));
                    selectedItem.setUnitPrice(new BigDecimal(unitPrice));
                    tblItems.refresh();
                } catch (NotFoundException e) {
                    e.printStackTrace(); // This is never going to happen with our UI design
                }
            }
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            throw e;
        }
        btnAddNewItem.fire();
    }

    private void loadAllItems() throws FailedOperationException {
        tblItems.getItems().clear();
        try {
            itemService.findAllItems().forEach(dto -> tblItems.getItems().add(new ItemTM(dto.getCode(), dto.getDescription(), dto.getUnitPrice(), dto.getQtyOnHand())));
        } catch (FailedOperationException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            throw e;
        }
    }

    private void initUI() {
        txtCode.clear();
        txtDescription.clear();
        txtQtyOnHand.clear();
        txtUnitPrice.clear();

        txtCode.setDisable(true);
        txtDescription.setDisable(true);
        txtQtyOnHand.setDisable(true);
        txtUnitPrice.setDisable(true);
        txtCode.setEditable(false);
        btnSave.setDisable(true);
        btnDelete.setDisable(true);
    }
}
