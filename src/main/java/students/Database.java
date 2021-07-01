package students;

import com.google.gson.JsonObject;
import spark.Request;
import spark.Response;

import java.sql.*;
import java.util.ArrayList;

public class Database {

    /**
     * The database connection.
     */
    private Connection conn;

    /**
     * Creates the database interface object. Connection to the
     * database is performed later.
     */
    public Database(String filename) {
        openConnection(filename);
    }

    /**
     * Opens a connection to the database, using the specified
     * filename (if we'd used a traditional DBMS, such as PostgreSQL
     * or MariaDB, we would have specified username and password
     * instead).
     */
    public boolean openConnection(String filename) {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + filename);

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Closes the connection to the database.
     */
    public void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the connection to the database has been established
     *
     * @return true if the connection has been established
     */
    public boolean isConnected() {
        return conn != null;
    }


    public String reset(Request req, Response res) {
        var resetter = new Resetter();
        return resetter.executeResetting(conn, res);
    }


    public String getInformation(Response res, String table) {

        String statement = new getStatementsCreator().createGetStatement(table);

        try (var ps = conn.createStatement()) {
            var resultSet = ps.executeQuery(statement);
            var resultsAsJson = JSONizer.toJSON(resultSet, table);
            res.status(200);
            res.body(resultsAsJson);
            return resultsAsJson;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String createPallet(Request req, Response res) {
        var jsonObject = new JsonObject();
        PalletsAdder palletsAdder = new PalletsAdder(conn);
        ResultSet rs = palletsAdder.getCookie(req);

        if (rs == null) {
            res.status(400);
            jsonObject.addProperty("status", "no such cookie");
            return jsonObject.toString();
        } else try {
            palletsAdder.updateRawMaterialStorage(rs);
        } catch (SQLException e) {
            res.status(400);
            jsonObject.addProperty("status", "not enough ingredients");
            return jsonObject.toString();
        }
        try {
            palletsAdder.addPallet(req);
            return palletsAdder.getLastAddedPallet(jsonObject, res);
        } catch (SQLException e) {
            res.status(400);
            e.printStackTrace();
        }


        return "nothing happened from createPallets ";
    }


    public String getPalletsInfo(Request req, Response res) {
        var params = new ArrayList<String>();
        var statment =
                "SElECT pallet_id AS id," +
                        " recipe_id AS cookie," +
                        " productionDate," +
                        " company_id AS customer," +
                        " isBlocked AS blocked \n" +

                        "FROM         pallets \n" +
                        "LEFT JOIN    orders \n" +
                        "USING        (order_id) \n" +
                        "WHERE        1 = 1 \n";

        if (req.queryParams("cookie") != null) {
            statment += "AND recipe_id = ? \n";
            params.add(req.queryParams("cookie"));
        }
        if (req.queryParams("blocked") != null) {
            statment += "AND isBlocked = ?\n";
            params.add(req.queryParams("blocked"));
        }
        if (req.queryParams("after") != null) {
            statment += "AND productionDate > ?\n";
            params.add(req.queryParams("after"));
        }
        if (req.queryParams("before") != null) {
            statment += "AND productionDate < ?\n";
            params.add(req.queryParams("before"));
        }


        try (var ps = conn.prepareStatement(statment)) {
            var idx = 0;
            for (var param : params) {
                ps.setString(++idx, param);
            }
            var rs = ps.executeQuery();
            var results = new StringBuilder();
            while (rs.next()) {
                results.append(getPalletFromRS(rs));
            }
            if (!results.toString().equals("")) {
                res.status(200);
                res.body();
                return ("{\n     \"pallets\": [") +
                        results
                                .deleteCharAt(results.length() - 1)
                                .toString() +
                        "\n        ]\n}";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
        }
        return "";
    }

    public String handlePalletBlocking(Request req, Response res, boolean state) {
        var jsonObject = new JsonObject();
        var statement =
                "UPDATE pallets \n" +
                        "SET isBlocked =  ? \n" +
                        "WHERE recipe_id = ? " +
                        "AND productionDate BETWEEN ? AND ?; \n";

        try (var ps = conn.prepareStatement(statement)) {
            ps.setBoolean(1, state);
            ps.setString(2, req.params(":cookie"));
            ps.setString(3, req.params(":from"));
            ps.setString(4, req.params(":to"));
            if (ps.executeUpdate() > 0) {
                res.status(200);
                jsonObject.addProperty("status", "ok");
                return jsonObject.toString();
            }

        } catch (SQLException e) {
            res.status(400);
            e.printStackTrace();
        }
        res.status(400);
        return ""; //"Invalid parameters !!";
    }


    private String getPalletFromRS(ResultSet rs) throws SQLException {
        var space = "         ";
        return "\n       {\n" + space +
                "\"id\": \"" + rs.getString("id") + "\",\n" +
                space +
                "\"cookie\": \"" + rs.getString("cookie") + "\",\n" +
                space +
                "\"productionDate\": \"" + rs.getString("productionDate") + "\",\n" +
                space +
                "\"customer\": " + rs.getString("customer") + ",\n" +
                space +
                "\"blocked\": " + rs.getBoolean("blocked") +
                "\n       },";
    }


}





