package students;


public class getStatementsCreator {

    public String createGetStatement(String table) {
        switch (table) {
            case "customers":
                return "SELECT company_id AS name, address \n" +
                        "From customers;";

            case "ingredients":
                return "SELECT material_id AS name, amountStorage as quantity, unit \n" +
                        "FROM  rawMaterials \n";

            case "cookies":
                return "SELECT recipe_id AS name \n" +
                        "FROM cookies \n" +
                        "ORDER BY name \n";

            case "recipes":
                return "SELECT recipe_id AS cookie," +
                        "material_id AS ingredient," +
                        "amount AS quantity," +
                        "unit \n" +

                        "FROM    cookies \n" +
                        "JOIN    recipeRows \n" +
                        "USING   (recipe_id) \n" +
                        "JOIN    rawMaterials \n" +
                        "USING   (material_id) \n";


            default:
                return "Should not happen from statementsCreator ";
        }
    }
}
