package students;

import static spark.Spark.*;

import java.sql.*;


public class App {

    public static void main(String[] args) {
        port(8888);
        var db = new Database("src/main/java/students/dataBas.db");
        if (db.isConnected()) System.out.println("connection started ");
        else System.out.println("Sorry! we could not connect you to the server !");

        get("/ping", (req, res) -> ("Hello World"));
        post("/reset", db::reset);
        get("/customers", (request, response) -> db.getInformation(response, "customers"));
        get("/ingredients", (request, response) -> db.getInformation(response, "ingredients"));
        get("/cookies", (request, response) -> db.getInformation(response, "cookies"));
        get("/recipes", (request, response) -> db.getInformation(response, "recipes"));
        post("/pallets", (db::createPallet));
        get("/pallets", db::getPalletsInfo);
        post("/block/:cookie/:from/:to", (req, res) -> db.handlePalletBlocking(req, res, true));
        post("/unblock/:cookie/:from/:to", (req, res) -> db.handlePalletBlocking(req, res, false));

    }
}


/**
 * Auxiliary class for automatically translating a ResultSet to JSON
 */
class JSONizer {

    public static String toJSON(ResultSet rs, String name) throws SQLException {
        StringBuilder sb = new StringBuilder();
        ResultSetMetaData meta = rs.getMetaData();
        boolean first = true;
        sb.append("{\n");
        sb.append("  \"" + name + "\": [\n");
        while (rs.next()) {
            if (!first) {
                sb.append(",");
                sb.append("\n");
            }
            first = false;
            sb.append("    {");
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String label = meta.getColumnLabel(i);
                String value = getValue(rs, i, meta.getColumnType(i));
                sb.append("\"" + label + "\": " + value);
                if (i < meta.getColumnCount()) {
                    sb.append(", ");
                }
            }
            sb.append("}");
        }
        sb.append("\n");
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String getValue(ResultSet rs, int i, int columnType) throws SQLException {
        switch (columnType) {
            case java.sql.Types.INTEGER:
                return String.valueOf(rs.getInt(i));
            case java.sql.Types.REAL:
            case java.sql.Types.DOUBLE:
            case java.sql.Types.FLOAT:
                return String.valueOf(rs.getDouble(i));
            default:
                return "\"" + rs.getString(i) + "\"";
        }
    }
}
