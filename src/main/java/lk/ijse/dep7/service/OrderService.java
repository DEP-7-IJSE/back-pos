package lk.ijse.dep7.service;

import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.dto.OrderDetailDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class OrderService {
    private final Connection connection;

    public OrderService(Connection connection) {
        this.connection = connection;
    }

    public void saveOrder(String orderId, LocalDate orderDate, String customerId, List<OrderDetailDTO> orderDetails) throws FailedOperationException, DuplicateIdentifierException, NotFoundException {
        CustomerService customerService = new CustomerService(connection);
        ItemService itemService = new ItemService(connection);

        try {
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM `order` WHERE id=?;");
            pstm.setString(1, orderId);

            if (pstm.executeQuery().next()) throw new DuplicateIdentifierException(orderId + " already exists");

            if (!customerService.existCustomer(customerId)) throw new NotFoundException("Customer id doesn't exists");

            connection.setAutoCommit(false);
            pstm = connection.prepareStatement("INSERT INTO `order` (id, date, customer_id) VALUES (?,?,?);");
            pstm.setString(1, orderId);
            pstm.setDate(2, Date.valueOf(orderDate));
            pstm.setString(3, customerId);

            if (pstm.executeUpdate() != 1) throw new FailedOperationException("Failed to save the order");

            pstm = connection.prepareStatement("INSERT INTO order_detail (order_id, item_code, unit_price, qty) VALUES (?,?,?,?);");
            for (OrderDetailDTO detail : orderDetails) {
                pstm.setString(1, orderId);
                pstm.setString(2, detail.getItemCode());
                pstm.setBigDecimal(3, detail.getUnitPrice());
                pstm.setInt(4, detail.getQty());
                if (pstm.executeUpdate() != 1) throw new FailedOperationException("Failed to save some order details");

                ItemDTO item = itemService.findItem(detail.getItemCode());
                item.setQtyOnHand(item.getQtyOnHand() - detail.getQty());
                itemService.updateItem(item);

            }

            connection.commit();

        } catch (Throwable e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new FailedOperationException("Failed to rollback the transaction", e);
            }

            if (e instanceof DuplicateIdentifierException || e instanceof NotFoundException || e instanceof FailedOperationException) {

                try {
                    throw e;
                } catch (SQLException ex) {
                    throw new FailedOperationException("Failed to save the order", e);
                }
            } else {
                throw new FailedOperationException("Failed to save the order", e);
            }

        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new FailedOperationException("Failed to reset the transaction", e);
            }
        }
    }

    public String generateNewOrderId() throws FailedOperationException {
        try {
            ResultSet rst = connection.createStatement().executeQuery("SELECT id FROM `order` ORDER BY id DESC LIMIT 1;");
            if (rst.next()) {
                String id = rst.getString("id");
                int newOrderId = Integer.parseInt(id.replace("OD", "")) + 1;
                return String.format("OD%03d", newOrderId);
            } else {
                return "OD001";
            }
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to generate new id", e);
        }
    }
}
