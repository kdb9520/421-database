import java.util.ArrayList;

public class QueryHandler {

    public static QueryHandler queryHandler;

    public static String buildInsertQuery(ArrayList<Record> records, TableSchema tableSchema) {
        StringBuilder tempQuery = new StringBuilder(tableSchema.tableName + " values");

        for (Record record : records) {
            tempQuery.append("(");

            ArrayList<Object> attrs = record.getValues();

            for (int c = 0; c < attrs.size(); c++) {
                Object v = attrs.get(c);
                if (v != null) {
                    String attribute = tableSchema.getAttributeSchema().get(c).attrType;
                    if (attribute.contains("varchar") || attribute.contains("char")) {
                        tempQuery.append("'");
                    }
                    tempQuery.append(v.toString().trim());
                    if (attribute.contains("varchar") || attribute.contains("char")) {
                        tempQuery.append("'");
                    }
                } else {
                    tempQuery.append("null");
                }
                if (c != attrs.size() - 1) {
                    tempQuery.append(" ");
                }

            }
            tempQuery.append("), ");
        }

        tempQuery = tempQuery.replace(tempQuery.length() - 2, tempQuery.length(), ";");

        System.out.println(tempQuery);

        return String.valueOf(tempQuery);

    }

    /**
     * getTableNames takes in a query in the form "SELECT ... FROM t1, <t2>, <tn> WHERE ... ;" and gets the table names.
     * @param query SELECT query
     * @return array of table names strings
     */
    public static String[] getTableNamesFromSelect(String query) {
        String[] names = query.split("from")[1].split("where")[0].split(",");
        for (int i = 0; i < names.length; i++) {
            names[i] = names[i].strip();
            if (i == names.length - 1) {
                names[i] = names[i].split(";")[0].strip();
            }
        }
        return names;
    }

}
