package lk.ijse.dep7.service;

import lk.ijse.dep7.dbUtils.SingleConnectionDataSource;
import lk.ijse.dep7.dto.ItemDTO;
import lk.ijse.dep7.exception.DuplicateIdentifierException;
import lk.ijse.dep7.exception.FailedOperationException;
import lk.ijse.dep7.exception.NotFoundException;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ItemServiceTest {

    private ItemService itemService;

    @BeforeEach
    private void initBeforeEachTest() throws SQLException {
        SingleConnectionDataSource.init("jdbc:mysql://localhost:3306/dep7_backup_pos", "root", "mysql");
        Connection connection = SingleConnectionDataSource.getInstance().getConnection();
        this.itemService = new ItemService(connection);
        connection.setAutoCommit(false);
        connection.prepareStatement("INSERT INTO item VALUES ('I001','Pen','10','5')").executeUpdate();
    }

    @AfterEach
    private void finalizeAfterEachTest() throws SQLException {
        Connection connection = SingleConnectionDataSource.getInstance().getConnection();
        connection.rollback();
        connection.setAutoCommit(true);
    }

    @Order(2)
    @Test
    void saveItem() throws FailedOperationException, DuplicateIdentifierException, SQLException {
        itemService.saveItem(new ItemDTO("I002", "Pencil", new BigDecimal("5"), 8));
        //assertTrue(customerService.existCustomer("C002"));
        assertThrows(DuplicateIdentifierException.class, () -> itemService.saveItem(new ItemDTO("I001", "Pen", new BigDecimal("10"), 5)));
    }

    @Test
    void updateItem() throws FailedOperationException, NotFoundException, SQLException {
        itemService.updateItem(new ItemDTO("I001", "Book", new BigDecimal("80"), 2));
        ResultSet rst = SingleConnectionDataSource.getInstance().getConnection().createStatement().executeQuery("SELECT * FROM item WHERE code='I001';");
        rst.next();
        assertEquals(rst.getString("description"), "Book");
        assertEquals(rst.getInt("qty_on_hand"), 2);
        assertEquals(rst.getBigDecimal("unit_price"), new BigDecimal("80"));
        assertThrows(NotFoundException.class, () -> itemService.updateItem(new ItemDTO("I100", "Book", new BigDecimal("80"), 2)));
    }

    @Test
    void deleteItem() throws NotFoundException, FailedOperationException, SQLException {
        itemService.deleteItem("I001");
        assertFalse(SingleConnectionDataSource.getInstance().getConnection().prepareStatement("SELECT * FROM item WHERE code='C001';").executeQuery().next());
        assertThrows(NotFoundException.class, () -> itemService.deleteItem("I100"));
    }

    @Test
    void findItem() throws FailedOperationException, NotFoundException {
        ItemDTO c001 = itemService.findItem("I001");
        assertNotNull(c001);
        assertEquals(c001.getCode(), "I001");
        assertEquals(c001.getDescription(), "Pen");
        assertEquals(c001.getQtyOnHand(), 5);
        assertEquals(c001.getUnitPrice(), new BigDecimal("10"));
        assertThrows(NotFoundException.class, () -> itemService.findItem("I100"));
    }

    @Test
    void findAllItems() throws FailedOperationException, DuplicateIdentifierException, SQLException {
        saveItem();
        assertEquals(itemService.findAllItems().size(), 2);
    }

    /*@Order(1)
    @Test
    void existItem() throws SQLException {
        assertTrue(itemService.existItem("C001"));
        assertFalse(itemService.existItem("C002"));
    }*/
}
