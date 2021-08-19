package lk.ijse.dep7.service;

import lk.ijse.dep7.dbUtils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.CustomerDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerServiceTest {

    private CustomerService customerService;

    @BeforeEach
    private void initBeforeEachTest() throws SQLException {
        SingleConnectionDataSource.init("jdbc:mysql://localhost:3306/dep7_backup_pos", "root", "mysql");
        Connection connection = SingleConnectionDataSource.getInstance().getConnection();
        this.customerService = new CustomerService(connection);
        connection.setAutoCommit(false);
        connection.prepareStatement("INSERT INTO customer VALUES ('C001','Pethum','Galle')").executeUpdate();
    }

    @AfterEach
    private void finalizeAfterEachTest() throws SQLException {
        Connection connection = SingleConnectionDataSource.getInstance().getConnection();
        connection.rollback();
        connection.setAutoCommit(true);
    }

    @Order(2)
    @Test
    void saveCustomer() throws FailedOperationException, DuplicateIdentifierException, SQLException {
        customerService.saveCustomer(new CustomerDTO("C002", "Dinusha", "Moratuwa"));
        //assertTrue(customerService.existCustomer("C002"));
        assertThrows(DuplicateIdentifierException.class, () -> customerService.saveCustomer(new CustomerDTO("C001", "Dinusha", "Moratuwa")));
    }

    @Test
    void updateCustomer() throws FailedOperationException, NotFoundException, SQLException {
        customerService.updateCustomer(new CustomerDTO("C001", "Sachintha", "Matara"));
        ResultSet rst = SingleConnectionDataSource.getInstance().getConnection().createStatement().executeQuery("SELECT * FROM customer WHERE id='C001';");
        rst.next();
        assertEquals(rst.getString("name"), "Sachintha");
        assertEquals(rst.getString("address"), "Matara");
        assertThrows(NotFoundException.class, () -> customerService.updateCustomer(new CustomerDTO("C100", "Gayal", "Jaffna")));
    }

    @Test
    void deleteCustomer() throws NotFoundException, FailedOperationException, SQLException {
        customerService.deleteCustomer("C001");
        assertFalse(SingleConnectionDataSource.getInstance().getConnection().prepareStatement("SELECT * FROM customer WHERE id='" + "C001'").executeQuery().next());
        assertThrows(NotFoundException.class, () -> customerService.deleteCustomer("C100"));
    }

    @Test
    void findCustomer() throws FailedOperationException, NotFoundException {
        CustomerDTO c001 = customerService.findCustomer("C001");
        assertNotNull(c001);
        assertEquals(c001.getId(), "C001");
        assertEquals(c001.getName(), "Pethum");
        assertEquals(c001.getAddress(), "Galle");
        assertThrows(NotFoundException.class, () -> customerService.findCustomer("C100"));
    }

    @Test
    void findAllCustomer() throws FailedOperationException, DuplicateIdentifierException, SQLException {
        saveCustomer();
        assertEquals(customerService.findAllCustomers().size(), 2);
    }

    /*@Order(1)
    @Test
    void existCustomer() throws SQLException {
        assertTrue(customerService.existCustomer("C001"));
        assertFalse(customerService.existCustomer("C002"));
    }*/
}
