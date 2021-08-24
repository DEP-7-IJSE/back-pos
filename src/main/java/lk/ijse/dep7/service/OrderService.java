package lk.ijse.dep7.service;

import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.dto.OrderDTO;
import lk.ijse.dep7.dto.OrderDetailDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private final Connection connection;

    public OrderService(Connection connection) {
        this.connection = connection;
    }

    public void saveOrder(String orderId, LocalDate orderDate, String customerId, List<OrderDetailDTO> orderDetails) throws FailedOperationException, DuplicateIdentifierException, NotFoundException {
        final CustomerService customerService = new CustomerService(connection);
        final ItemService itemService = new ItemService(connection);

        try {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM `order` WHERE id=?;");
            stm.setString(1, orderId);

            if (stm.executeQuery().next()) throw new DuplicateIdentifierException(orderId + " already exists");

            if (!customerService.existCustomer(customerId)) throw new NotFoundException("Customer id doesn't exists");

            connection.setAutoCommit(false);
            stm = connection.prepareStatement("INSERT INTO `order` (id, date, customer_id) VALUES (?,?,?);");
            stm.setString(1, orderId);
            stm.setDate(2, Date.valueOf(orderDate));
            stm.setString(3, customerId);

            if (stm.executeUpdate() != 1) throw new FailedOperationException("Failed to save the order");

            stm = connection.prepareStatement("INSERT INTO order_detail (order_id, item_code, unit_price, qty) VALUES (?,?,?,?);");
            for (OrderDetailDTO detail : orderDetails) {
                stm.setString(1, orderId);
                stm.setString(2, detail.getItemCode());
                stm.setBigDecimal(3, detail.getUnitPrice());
                stm.setInt(4, detail.getQty());
                if (stm.executeUpdate() != 1) throw new FailedOperationException("Failed to save some order details");

                ItemDTO item = itemService.findItem(detail.getItemCode());
                item.setQtyOnHand(item.getQtyOnHand() - detail.getQty());
                itemService.updateItem(item);

            }

            connection.commit();
        } catch (SQLException e) {
            failedOperationExecutionContext(connection::rollback);
        } catch (Throwable e) {
            failedOperationExecutionContext(connection::rollback);
            throw e;

        } finally {
            failedOperationExecutionContext(() -> connection.setAutoCommit(true));
        }
    }

    public String generateNewOrderId() throws FailedOperationException {
        try {
            ResultSet rst = connection.createStatement().executeQuery("SELECT id FROM `order` ORDER BY id DESC LIMIT 1;");
            return rst.next() ? String.format("OD%03d", (Integer.parseInt(rst.getString("id").replace("OD", "")) + 1)) : "OD001";
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to generate new order id", e);
        }
    }

    public List<OrderDTO> searchOrders(String query) throws FailedOperationException {
        ArrayList<OrderDTO> orderList = new ArrayList<>();

        try {
            String[] searchWords = query.split("\\s");
            StringBuilder sql = new StringBuilder("SELECT o.*, c.name, order_total.total\n" +
                    "FROM `order` o\n" +
                    "         INNER JOIN customer c on o.customer_id = c.id\n" +
                    "         INNER JOIN\n" +
                    "     (SELECT order_id, SUM(qty * unit_price) AS total FROM order_detail od GROUP BY order_id) AS order_total\n" +
                    "     ON o.id = order_total.order_id\n" +
                    "WHERE (order_id LIKE ?\n" +
                    "   OR date LIKE ?\n" +
                    "   OR customer_id LIKE ?\n" +
                    "   OR name LIKE ?) ");

            for (int i = 1; i < searchWords.length; i++) {
                sql.append("AND (order_id LIKE ?\n" +
                        "    OR date LIKE ?\n" +
                        "    OR customer_id LIKE ?\n" +
                        "    OR name LIKE ?) ");
            }

            sql.append("ORDER BY id;");

            PreparedStatement stm = connection.prepareStatement(sql.toString());

            int j = 0;
            for (int i = 1; i <= searchWords.length * 4; i++) {
                stm.setString(i, "%" + searchWords[j] + "%");
                if (i % 4 == 0) j++;
            }
            ResultSet rst = stm.executeQuery();

            while (rst.next()) {
                orderList.add(new OrderDTO(
                        rst.getString("id"),
                        rst.getDate("date").toLocalDate(),
                        rst.getString("customer_id"),
                        rst.getString("name"),
                        rst.getBigDecimal("total")
                ));
            }

            return orderList;
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to search Orders");
        }
    }

    private void failedOperationExecutionContext(ExecutionContext context) throws FailedOperationException {
        try {
            context.execute();
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to save the order", e);
        }
    }

    @FunctionalInterface
    interface ExecutionContext {
        void execute() throws SQLException;
    }
}
