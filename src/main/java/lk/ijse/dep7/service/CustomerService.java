package lk.ijse.dep7.service;

import lk.ijse.dep7.dbUtils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.CustomerDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class CustomerService {
    private Connection connection = SingleConnectionDataSource.getInstance().getConnection();

    public CustomerService() {
    }

    public CustomerService(Connection connection) {
        this.connection = connection;
    }

    public void saveCustomer(CustomerDTO customer) throws DuplicateIdentifierException, FailedOperationException {
        try {
            if (existCustomer(customer.getId()))
                throw new DuplicateIdentifierException(customer.getId() + " already exists");

            PreparedStatement pstm = connection.prepareStatement("INSERT INTO customer (id, name, address) VALUES (?,?,?);");
            pstm.setString(1, customer.getId());
            pstm.setString(2, customer.getName());
            pstm.setString(3, customer.getAddress());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to save the customer", e);
        }
    }

    private boolean existCustomer(String id) throws SQLException {
        PreparedStatement pstm = connection.prepareStatement("SELECT id FROM customer WHERE id=?;");
        pstm.setString(1, id);
        return pstm.executeQuery().next();
    }

    public void updateCustomer(CustomerDTO customer) throws FailedOperationException, NotFoundException {
        try {
            if (!existCustomer(customer.getId())) throw new NotFoundException("Customer does not exists");

            PreparedStatement pstm = connection.prepareStatement("UPDATE customer SET name=?, address=? WHERE id=?;");
            pstm.setString(1, customer.getName());
            pstm.setString(2, customer.getAddress());
            pstm.setString(3, customer.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new FailedOperationException("Failed to update the customer", e);
        }
    }

    public void deleteCustomer(String id) {

    }

    public CustomerDTO findCustomer(String id) {
        return null;
    }

    public List<CustomerDTO> findAllCustomer() {
        return null;
    }

}
