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

}
