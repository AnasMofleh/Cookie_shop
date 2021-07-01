package students;

import com.google.gson.JsonObject;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;

import spark.Response;
import spark.Request;

public class PalletsAdder {
    private final Connection conn;

    public PalletsAdder(Connection conn) {
        this.conn = conn;
    }

    public ResultSet getCookie(Request req) {
        var statement =
                "SELECT material_id, amountStorage, amount\n" +
                        "FROM rawMaterials \n" +
                        "JOIN recipeRows \n" +
                        "USING (material_id) \n" +
                        "WHERE recipe_id = ?";

        try {
            var ps = conn.prepareStatement(statement);
            ps.setString(1, req.queryParams("cookie")); //todo: can't handle two words cookie's name
            return ps.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void updateRawMaterialStorage(ResultSet resultSet) throws SQLException {
        var statement =
                "UPDATE rawMaterials\n" +
                        "SET amountStorage = (amountStorage - ?) \n" +
                        "WHERE material_id = ?; \n";

        var ps = conn.prepareStatement(statement);
        while (resultSet.next()) {
            var ingredient = resultSet.getString("material_id");
            var amountStored = resultSet.getInt("amountStorage");
            var amountNeeded = resultSet.getInt("amount") * 54;
            if (amountNeeded > amountStored) {
                throw new SQLException("not enough ingredients");
            }
            ps.setInt(1, amountNeeded);
            ps.setString(2, ingredient);
            ps.executeUpdate();
        }

    }

    public void addPallet(Request req) throws SQLException {
        var statement =
                "INSERT\n" +
                        "INTO     pallets(recipe_id, productionDate)\n" +
                        "VALUES   (?, CURRENT_DATE); \n ";

        var ps = conn.prepareStatement(statement);
        ps.setString(1, req.queryParams("cookie"));
        ps.executeUpdate();
    }


    public String getLastAddedPallet(JsonObject jsonObject, Response res) throws SQLException {
        var statement =
                "SELECT pallet_id \n" +
                        "FROM pallets \n" +
                        "WHERE rowid = last_insert_rowid()";

        var ps = conn.prepareStatement(statement);
        var rs = ps.executeQuery();

        var newId = rs.getString("pallet_id");
        jsonObject.addProperty("status", "ok");
        jsonObject.addProperty("id", newId);
        var resultAsJson = jsonObject.toString().replace(",", ",\n");
        res.status(200);
        res.body(resultAsJson);
        return resultAsJson;

    }
}
