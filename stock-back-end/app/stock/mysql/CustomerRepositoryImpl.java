package stock.mysql;

import models.Customer;
import play.db.Database;
import play.db.NamedDatabase;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Singleton
public class CustomerRepositoryImpl implements CustomerRepository{
    private Database database;

    @Inject
    public CustomerRepositoryImpl(@NamedDatabase("stock") Database database) {
        this.database = database;
    }

    @Override
    public CompletableFuture<List<Customer>> getCustomers() throws CompletionException {
        return CompletableFuture.supplyAsync(() -> this.database.withConnection(connection -> {
            List<Customer> customers = new ArrayList<>();
            String sql = "select id, name, reference, phone, address1, address2, email, country, state, city, postal_code, modified_by, modified_on from customers";
            try(CallableStatement stmt = connection.prepareCall(sql)) {
                ResultSet rs = stmt.executeQuery();
                while(rs.next()) {
                    customers.add(deserializeCustomer(rs));
                }
                return customers;
            } catch (CompletionException e) {
                throw e;
            }
        }));
    }
    @Override
    public CompletableFuture<Customer> getCustomer(Long id) throws CompletionException {
        return CompletableFuture.supplyAsync(() -> this.database.withConnection(connection -> {
            String sql = "select id, name, reference,  address1, address2, email, phone, modified_by, modified_on, country, state, city, postal_code from customers where id = ?";
            try (CallableStatement stmt = connection.prepareCall(sql)) {
                stmt.setLong(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return deserializeCustomer(rs);
                } else {
                    return null;
                }
            } catch (CompletionException e) {
                throw e;
            }
        }));
    }
    @Override
    public CompletableFuture<Customer> addNewCustomer  (String name, String reference, String address1, String address2, String phone,
                                                       String email, String country, String state, String city, String postal_code) throws CompletionException {
        return CompletableFuture.supplyAsync(() -> this.database.withConnection(connection -> {
            String sql = "insert into customers (name, reference,  address1, address2, phone, email, modified_by," +
                    "modified_on, country, state, city, postal_code) VALUES (?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
                stmt.setString(2, reference);
                stmt.setString(3, address1);
                stmt.setString(4, address2);
                stmt.setString(5, phone);
                stmt.setString(6, email);
                stmt.setString(7, "admin");
                stmt.setString(8, country);
                stmt.setString(9, state);
                stmt.setString(10, city);
                stmt.setString(11, postal_code);
                stmt.executeUpdate();
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long id = generatedKeys.getLong(1);
                        try{
                            return getCustomer(id).toCompletableFuture().get();
                        } catch (Exception e) {
                            throw new SQLException("Failed to create new customer!");
                        }
                    } else {
                        throw new SQLException("Failed to create new customer!");
                    }
                }
            } catch (CompletionException e) {
                throw e;
            }
        }));
    }
    @Override
    public CompletableFuture<Boolean> deleteCustomer(Long id) throws CompletionException{
        return CompletableFuture.supplyAsync(() -> this.database.withConnection(connection -> {
            String sql ="delete from customers where id = ? ";
            try(CallableStatement stmt = connection.prepareCall(sql)){
                stmt.setLong(1, id);
                int rows = stmt.executeUpdate();
                if(rows > 0)
                    return true;
                else
                    return false;
            }
            catch (CompletionException e) {
                throw e;
            }
        }));
    }
    @Override
    public CompletableFuture<Boolean> updateCustomer(Long id, String name, String reference, String address1, String address2, String phone,
                                                     String email, String country, String state, String city, String postalCode) throws CompletionException{
        return CompletableFuture.supplyAsync(() -> this.database.withConnection(connection -> {
            String sql ="update customers set name=?, reference=?, address1= ?, address2=?, phone=?, email=?, country=?, state=?, city=?, postal_code=?, modified_by=?, modified_on=now()  where id = ?;";
            try(CallableStatement stmt = connection.prepareCall(sql)){
                stmt.setString(1, name);
                stmt.setString(2, reference);
                stmt.setString(3, address1);
                stmt.setString(4, address2);
                stmt.setString(5, phone);
                stmt.setString(6, email);
                stmt.setString(7, country);
                stmt.setString(8, state);
                stmt.setString(9, city);
                stmt.setString(10, postalCode);
                stmt.setString(11,"admin");
                stmt.setLong(12, id);
                int rows = stmt.executeUpdate();
                if(rows > 0)
                    return true;
                else
                    return false;
            }
            catch (CompletionException e) {
                throw e;
            }
        }));

    }
    private Customer deserializeCustomer(ResultSet rs) throws SQLException {
            Long id = rs.getLong("id");
            String name = rs.getString("name");
            String reference = rs.getString("reference");
            String phone = rs.getString("phone");
            String address1 = rs.getString("address1");
        String address2 = rs.getString("address2");
            String email = rs.getString("email");
            String country = rs.getString("country");
            String state = rs.getString("state");
            String city = rs.getString("city");
            String postalCode = rs.getString("postal_code");
            String modified_by = rs.getString("modified_by");
            Date modified_on = rs.getDate("modified_on");
            return new Customer(id, name, reference, phone, address1, address2, email, country, state, city, postalCode, modified_by, modified_on);
        }
}
