package students;


import com.google.gson.JsonObject;
import spark.Response;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Resetter {

    final private String[] tables =
            {"customers"
                    , "orders"
                    , "pallets"
                    , "cookies"
                    , "cookiesToBake"
                    , "recipeRows"
                    , "rawMaterials"};


    public List<String> prepareDeleteStatements() {
        ArrayList<String> deleteStatements = new ArrayList<>();
        Arrays.stream(tables)
                .forEach(x -> deleteStatements
                        .add("DELETE FROM " + x + "\n" +
                                "WHERE 1=1;\n"));
        return deleteStatements;
    }


    public String executeResetting(Connection conn, Response res) {
        List<String> deleteQueries = prepareDeleteStatements();
        var queries = new ArrayList<>(deleteQueries);
        queries.add(
                "INSERT INTO customers(company_id, address)\n" +
                        "VALUES ('Finkakor AB', 'Helsingborg'),\n" +
                        "       ('Småbröd AB', 'Malmö'),\n" +
                        "       ('Kaffebröd AB', 'Landskrona'),\n" +
                        "       ('Bjudkakor AB', 'Ystad'),\n" +
                        "       ('Kalaskakor AB', 'Trelleborg'),\n" +
                        "       ('Partykakor AB', 'Kristianstad'),\n" +
                        "       ('Gästkakor AB', 'Hässleholm'),\n" +
                        "       ('Skånekakor AB', 'Perstorp');\n ");

        queries.add(
                "INSERT INTO cookies(recipe_id)\n" +
                        "VALUES ('Nut ring'),\n" +
                        "       ('Nut cookie'),\n" +
                        "       ('Amneris'),\n" +
                        "       ('Tango'),\n" +
                        "       ('Almond delight'),\n" +
                        "       ('Berliner');\n ");

        queries.add(
                "INSERT INTO rawMaterials(material_id, amountStorage, unit)\n" +
                        "VALUES ('Flour', 100000, 'g'),\n" +
                        "       ('Butter', 100000, 'g'),\n" +
                        "       ('Icing sugar', 100000, 'g'),\n" +
                        "       ('Roasted, chopped nuts', 100000, 'g'),\n" +
                        "       ('Fine-ground nuts', 100000, 'g'),\n" +
                        "       ('Ground, roasted nuts', 100000, 'g'),\n" +
                        "       ('Bread crumbs', 100000, 'g'),\n" +
                        "       ('Sugar', 100000, 'g'),\n" +
                        "       ('Egg whites', 100000, 'g'),\n" +
                        "       ('Chocolate', 100000, 'g'),\n" +
                        "       ('Marzipan', 100000, 'g'),\n" +
                        "       ('Egg', 100000, 'g'),\n" +
                        "       ('Potato starch', 100000, 'g'),\n" +
                        "       ('Wheat flour', 100000, 'g'),\n" +
                        "       ('Sodium bicarbonate', 100000, 'g'),\n" +
                        "       ('Vanilla', 100000, 'g'),\n" +
                        "       ('Cinnamon', 100000, 'g'),\n" +
                        "       ('Chopped almonds', 100000, 'g'),\n" +
                        "       ('Vanilla sugar', 100000, 'g');\n");

        queries.add(
                "INSERT INTO recipeRows(recipe_id, material_id, amount)\n" +
                        "VALUES ('Nut ring', 'Flour', 450.0),\n" +
                        "       ('Nut ring', 'Butter', 450.0),\n" +
                        "       ('Nut ring', 'Icing sugar', 190.0),\n" +
                        "       ('Nut ring', 'Roasted, chopped nuts', 225.0),\n" +

                        "       ('Nut cookie', 'Fine-ground nuts', 750.0),\n" +
                        "       ('Nut cookie', 'Ground, roasted nuts', 625.0),\n" +
                        "       ('Nut cookie', 'Bread crumbs', 125.0),\n" +
                        "       ('Nut cookie', 'Sugar', 375.0),\n" +
                        "       ('Nut cookie', 'Egg whites', 350.0),\n" +
                        "       ('Nut cookie', 'Chocolate', 50.0),\n" +

                        "       ('Amneris', 'Marzipan', 750.0),\n" +
                        "       ('Amneris', 'Butter', 250.0),\n" +
                        "       ('Amneris', 'Egg', 250.0),\n" +
                        "       ('Amneris', 'Potato starch', 25.0),\n" +
                        "       ('Amneris', 'Wheat flour', 25.0),\n" +

                        "       ('Tango', 'Sugar', 250.0),\n" +
                        "       ('Tango', 'Butter', 200.0),\n" +
                        "       ('Tango', 'Flour', 300.0),\n" +
                        "       ('Tango', 'Sodium bicarbonate', 4.0),\n" +
                        "       ('Tango', 'Vanilla', 2.0),\n" +

                        "       ('Almond delight', 'Butter', 400.0),\n" +
                        "       ('Almond delight', 'Sugar', 270.0),\n" +
                        "       ('Almond delight', 'Chopped almonds', 279.0),\n" +
                        "       ('Almond delight', 'Flour', 400.0),\n" +
                        "       ('Almond delight', 'Cinnamon', 10.0),\n" +

                        "       ('Berliner', 'Flour', 350.0),\n" +
                        "       ('Berliner', 'Butter', 250.0),\n" +
                        "       ('Berliner', 'Icing sugar', 100.0),\n" +
                        "       ('Berliner', 'Egg', 50.0),\n" +
                        "       ('Berliner', 'Vanilla sugar', 5.0),\n" +
                        "       ('Berliner', 'Chocolate', 50.0);\n");


        queries.add(0, "PRAGMA foreign_keys = OFF;");
        var lastIndex = queries.size() - 1;
        queries.add(lastIndex, "PRAGMA foreign_keys = ON;");

        try (var ps = conn.createStatement()) {
            for (String e : queries) {
                ps.addBatch(e);
            }
            ps.executeBatch();
            res.status(200);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "ok");
            return jsonObject.toString();

        } catch (SQLException e) {
            e.printStackTrace();
            res.status(400);
            return "";
        }
    }

    public static void main(String[] args) {

    }
}
