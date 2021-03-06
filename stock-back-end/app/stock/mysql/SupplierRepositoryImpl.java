package stock.mysql;

import models.Supplier;
import org.joda.time.DateTime;
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
public class SupplierRepositoryImpl implements SupplierRepository {
    private Database database;

    @Inject
    public SupplierRepositoryImpl(@NamedDatabase("stock") Database database) {
        this.database = database;
    }

    @Override
    public CompletableFuture<List<Supplier>> getSuppliers() throws CompletionException {
        return CompletableFuture.supplyAsync(() -> this.database.withConnection(connection -> {
            List<Supplier> suppliers = new ArrayList<>();
            String sql = "select id, name, reference, address1, address2, email, phone, modified_by, modified_on, country, state, city, postal_code from suppliers";
            try(CallableStatement stmt = connection.prepareCall(sql)) {
                ResultSet rs = stmt.executeQuery();
                while(rs.next()) {
                    suppliers.add(deserializeSupplier(rs));
                }
                return suppliers;
            } catch (CompletionException e) {
                throw e;
            }
        }));
    }
    @Override
    public CompletableFuture<Supplier> getSupplier(Long id) throws CompletionException  {
        return CompletableFuture.supplyAsync(() -> this.database.withConnection(connection -> {
            String sql = "select id, name, reference, address1, address2, email, phone, modified_by, modified_on, country, state, city, postal_code from suppliers where id = ?";
            try(CallableStatement stmt = connection.prepareCall(sql)) {
                stmt.setLong(1, id);
                ResultSet rs = stmt.executeQuery();
                if(rs.next()) {
                    return deserializeSupplier(rs);
                } else {
                    return null;
                }
            } catch (CompletionException e) {
                throw e;
            }
        }));
    }

    @Override
    public CompletableFuture<Supplier> addNewSupplier(String name, String reference, String address1, String address2, String phone,
                               String email, String country, String state, String city, String postal_code) throws CompletionException {
         return CompletableFuture.supplyAsync(() -> this.database.withConnection(connection -> {
            String sql = "insert into `suppliers` (name, reference, address1, address2, phone, email, modified_by, " +
                    "modified_on, country, state, city, postal_code) VALUES (?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
                stmt.setString(2, reference);
                stmt.setString(3, address1);
                stmt.setString(4,address2);
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
                            return getSupplier(id).toCompletableFuture().get();
                        } catch (Exception e) {
                            throw new SQLException("Failed to create new supplier!");
                        }
                    } else {
                        throw new SQLException("Failed to create new supplier!");
                    }
                }

            } catch (CompletionException e) {
                throw e;
            }
        }));
    }
    @Override
    public CompletableFuture<Boolean> deleteSupplier(Long id) throws CompletionException{
        return CompletableFuture.supplyAsync(() -> this.database.withConnection(connection -> {
            String sql ="delete from suppliers where id = ? ";
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
    public CompletableFuture<Boolean> updateSupplier(Long id, String name, String reference, String address1, String address2, String phone,
                                                     String email, String country, String state, String city, String postalCode) throws CompletionException{
        return CompletableFuture.supplyAsync(() -> this.database.withConnection(connection -> {
            String sql ="update suppliers set name=?, reference=?, address1= ?, address2=?, phone=?, email=?, country=?, state=?, city=?, postal_code=?, modified_by=?, modified_on=now()  where id = ?;";
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

    private Supplier deserializeSupplier(ResultSet rs) throws SQLException {
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
        return new Supplier(id, name, reference, phone, address1, address2, email, country, state, city, postalCode, modified_by, modified_on);
    }
}
