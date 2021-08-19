package lk.ijse.dep7.service;

import lk.ijse.dep7.dbUtils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemService {
    private Connection connection = SingleConnectionDataSource.getInstance().getConnection();

    public ItemService() {
    }

    public ItemService(Connection connection) {
        this.connection = connection;
    }

    public void saveItem(ItemDTO item) throws DuplicateIdentifierException, FailedOperationException {
        try {
            if (existItem(item.getCode()))
                throw new DuplicateIdentifierException(item.getCode() + " already exists");

            PreparedStatement pstm = connection.prepareStatement("INSERT INTO item (code, description, unit_price, qty_on_hand) VALUES (?,?,?,?);");
            pstm.setString(1, item.getCode());
            pstm.setString(2, item.getDescription());
            pstm.setBigDecimal(3, item.getUnitPrice());
            pstm.setInt(4, item.getQtyOnHand());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to save the item", e);
        }
    }

    private boolean existItem(String id) throws SQLException {
        PreparedStatement pstm = connection.prepareStatement("SELECT code FROM item WHERE code=?;");
        pstm.setString(1, id);
        return pstm.executeQuery().next();
    }

    public void updateItem(ItemDTO item) throws FailedOperationException, NotFoundException {
        try {
            if (!existItem(item.getCode()))
                throw new NotFoundException("Customer does not exists " + item.getCode());

            PreparedStatement pstm = connection.prepareStatement("UPDATE item SET description=?, qty_on_hand=?, unit_price=? WHERE code=?;");
            pstm.setString(1, item.getDescription());
            pstm.setInt(2, item.getQtyOnHand());
            pstm.setBigDecimal(3, item.getUnitPrice());
            pstm.setString(4, item.getCode());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to update the item", e);
        }
    }

    public void deleteItem(String id) throws NotFoundException, FailedOperationException {
        try {
            if (!existItem(id)) throw new NotFoundException("Not found the item " + id);

            PreparedStatement pstm = connection.prepareStatement("DELETE FROM item WHERE code=?;");
            pstm.setString(1, id);
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to delete item", e);
        }


    }

    public ItemDTO findItem(String id) throws NotFoundException, FailedOperationException {
        try {
            if (!existItem(id)) throw new NotFoundException("Not found the customer " + id);

            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM item WHERE code=?;");
            pstm.setString(1, id);
            ResultSet rst = pstm.executeQuery();
            rst.next();
            return new ItemDTO(id, rst.getString("description"), rst.getBigDecimal("unit_price"), rst.getInt("qty_on_hand"));
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to delete customer", e);
        }
    }

    public List<ItemDTO> findAllItems() throws FailedOperationException {

        try {
            ArrayList<ItemDTO> itemList = new ArrayList<>();
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM item");
            while (rst.next()) {
                itemList.add(new ItemDTO(rst.getString("code"), rst.getString("description"), rst.getBigDecimal("unit_price"), rst.getInt("qty_on_hand")));
            }
            return itemList;
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to delete customer", e);
        }
    }

    public String generateNewItemCode() throws FailedOperationException {
        try {
            ResultSet rst = connection.createStatement().executeQuery("SELECT code FROM item ORDER BY code DESC LIMIT 1;");
            if (rst.next()) {
                String id = rst.getString("code");
                int newItemCode = Integer.parseInt(id.replace("I", "")) + 1;
                return String.format("I%03d", newItemCode);
            } else {
                return "I001";
            }
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to generate new id", e);
        }
    }
}
