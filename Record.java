import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.function.Consumer;

public class Record {
    private ArrayList<Object> values;
    private BitSet nullBitmap;

    public Record() {
        this.values = new ArrayList<>();
        this.nullBitmap = new BitSet();
    }

    public Record(ArrayList<Object> newValues) {
        this.values = newValues;
        this.nullBitmap = new BitSet(this.values.size());
        calculateBitSet();
    }

    public Record(ArrayList<Object> newValues, BitSet nullBitmap) {
        this.values = newValues;
        this.nullBitmap = nullBitmap;
    }

    public void calculateBitSet() {
        for (int i = 0; i < values.size(); i++) {
            if (this.values.get(i) == null && !this.nullBitmap.get(i)) {
                this.nullBitmap.set(i);
            }
        }
    }

    public void modifyAttribute(Object newValue, int index) {
        this.values.set(index, newValue);
        this.calculateBitSet();
    }

    // Set the value of an attribute
    public void setAttribute(Object value) {
        values.add(value);
        this.calculateBitSet();
    }

    // Get the value of an attribute
    public Object getAttribute(int index) {
        return values.get(index);
    }

    // Get the value of an attribute
    public Object deleteAttribute(int index) {
        Object v = values.remove(index);
        this.calculateBitSet();
        return v;
    }

    // Calculate the size of the record in bytes
    public int calculateRecordSize() {
        int size = 0;

        // Add overhead for attribute names list and attribute values list
        size += calculateListSize(values);

        // Add sizes of attribute names and their corresponding values
        for (Object attribute : values) {
            size += calculateObjectSize(attribute);
        }

        return size;
    }

    public ArrayList<Object> getValues() {
        return this.values;
    }

    // Helper method to calculate the size of a list
    private int calculateListSize(ArrayList<Object> list) {
        int size = 4; // For the list size (integer is 4 bytes)
        for (Object obj : list) {
            size += calculateObjectSize(obj);
        }
        return size;
    }

    // Helper method to calculate the size of an object (if it's serializable)
    private int calculateObjectSize(Object obj) {
        if (obj == null) {
            return 0;
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.size();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public byte[] serialize(String tableName) {
        ArrayList<AttributeSchema> attributes = Catalog.getTableSchema(tableName).getAttributeSchema();

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(bos);) {
            // write null bitmap
            int[] nullBitmapIndices = nullBitmap.stream().toArray();
            dataOutputStream.writeInt(nullBitmapIndices.length);
            for (int i : nullBitmapIndices) {
                dataOutputStream.writeInt(i);
            }

            // For each attribute we need to know its type before writing to hardware
            for (int i = 0; i < attributes.size(); i++) {
                if (!nullBitmap.get(i)) {
                    String type = attributes.get(i).getType();
                    // Now write the bytes depending on what the type is
                    if (type.equals("integer")) {
                        dataOutputStream.writeInt((Integer) values.get(i));
                    } else if (type.startsWith("varchar")) {
                        // Convert object to string, write how many bytes it is and write the string
                        String value = (String) values.get(i);
                        dataOutputStream.writeInt(value.length());
                        dataOutputStream.write(value.getBytes("UTF-8"));
                    } else if (type.startsWith("char")) {
                        String value = (String) values.get(i);
                        dataOutputStream.write(value.getBytes("UTF-8"));
                    } else if (type.equals("double")) {
                        dataOutputStream.writeDouble((Double) values.get(i));
                    } else if (type.equals("boolean")) {
                        dataOutputStream.writeBoolean((boolean) values.get(i));
                    }
                }

            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Deserialize a byte array into a record object
    public static Record deserialize(ByteBuffer buffer, String tableName) {
        ArrayList<AttributeSchema> attributes = Catalog.getTableSchema(tableName).getAttributeSchema();
        ArrayList<Object> values = new ArrayList<>();

        // reads null Bitmap
        BitSet nullBitmap = new BitSet();
        Integer numIntsToRead = buffer.getInt();
        for (int i = 0; i < numIntsToRead; i++) {
            nullBitmap.set(buffer.getInt());
        }

        for (int i = 0; i < attributes.size(); i++) {
            // check if type is not null
            if (!nullBitmap.get(i)) {
                String type = attributes.get(i).getType();
                // Now write the bytes depending on what the type is
                if (type.equals("integer")) {
                    Integer attr = buffer.getInt();
                    values.add(attr);
                } else if (type.startsWith("varchar")) {
                    // Get length of the varchar
                    int length = buffer.getInt();
                    // Read the varchar in
                    byte[] stringBytes = new byte[length];
                    buffer.get(stringBytes);
                    // Make it a string
                    String attr = new String(stringBytes);
                    values.add(attr);
                } else if (type.startsWith("char")) {
                    // Get the size of char
                    int numberOfChars = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));
                    byte[] stringBytes = new byte[numberOfChars];
                    buffer.get(stringBytes);
                    // Get the string
                    String attr = new String(stringBytes);
                    values.add(attr);
                } else if (type.equals("double")) {
                    Double attr = buffer.getDouble();
                    values.add(attr);
                } else if (type.equals("boolean")) {
                    boolean attr = buffer.get() != 0;
                    values.add(attr);
                }
            } else {
                values.add(null);
            }
        }

        return new Record(values, nullBitmap);

    }

    // Alternate toString method if objects need to be specified
    public String toString(String tableName) {

        TableSchema tableSchema = Catalog.getTableSchema(tableName);

        ArrayList<AttributeSchema> attributeSchemas = tableSchema.getAttributeSchema();

        String output = "( ";

        for (int i = 0; i < attributeSchemas.size(); i++) {
            String type = attributeSchemas.get(i).getType();
            Object value = values.get(i);
            if (value == null) {
                String s = "null";
                output = output + s;
            } else if (type.equals("integer")) {
                Integer n = (Integer) value;
                output = output + n;
            } else if (type.startsWith("varchar")) {
                String s = (String) value;
                output = output + s.strip();
            } else if (type.startsWith("char")) {
                String c = (String) value;
                output = output + c.strip();
            } else if (type.equals("double")) {
                double d = (double) value;
                output = output + d;
            } else if (type.equals("boolean")) {
                boolean b = (boolean) value;
                output = output + b;
            }
            output = output + " ";
        }
        output = output + ")";
        return output;
    }

    // Gets the size of a given record
    // Requires tablename to get schema for the attributes
    // Record format: [int bitMapSize] [bitMap] [value 1] [value 2] [value 3] [value
    // 5] example: value 4 is null
    public int getRecordSize(String tableName) {
        int size = 0;
        try {

            size += 4; // bitMapSize integer
            size += nullBitmap.toByteArray().length; // Add amount of bytes the nullbitmap is
            TableSchema tableSchema = Catalog.getTableSchema(tableName);
            ArrayList<AttributeSchema> attributes = tableSchema.getAttributeSchema();
            size += 4 * attributes.size(); // Nullmap is stored as [int][int]..[int] for each attribute marking if its
                                           // null or not
            // Go through each attribute/value, only add the size of the values that are not
            // null
            for (int i = 0; i < values.size(); i++) {
                if (!nullBitmap.get(i)) { // If this value is not null
                    String type = attributes.get(i).getType(); // Get what type it is
                    // Now increment size depending on what type it is
                    if (type.equals("integer")) {
                        size += 4; // Integer is size 4, no padding or values before it
                    } else if (type.startsWith("varchar")) {
                        size += 4; // Varchar is stored as [sizeofString] [string], add integer to size
                        // Convert object to string
                        String value = (String) values.get(i);
                        // Get how many bytes the actual string is
                        size += value.getBytes("UTF-8").length;
                    } else if (type.startsWith("char")) {
                        // Char is already padded
                        // We need to change this formula after we alter when we pad char TODO
                        String value = (String) values.get(i);
                        size += value.getBytes("UTF-8").length;
                    } else if (type.equals("double")) {
                        size += 8;
                    } else if (type.equals("boolean")) {
                        size += 1; // Its 1 bit not byte??
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error when calculating record size");
        }

        return size;
    }

    public String prettyPrint(String tableName) {
        TableSchema tableSchema = Catalog.getTableSchema(tableName);

        ArrayList<AttributeSchema> attributeSchemas = tableSchema.getAttributeSchema();

        StringBuilder output = new StringBuilder("|");

        for (int i = 0; i < attributeSchemas.size(); i++) {
            String str = getString(attributeSchemas, i);

            output.append(String.format("%10s", str));

            if (i < attributeSchemas.size() - 1) {
                output.append("||");
            }
        }
        output.append("|");
        return output.toString();
    }

    private String getString(ArrayList<AttributeSchema> attributeSchemas, int i) {
        String type = attributeSchemas.get(i).getType();
        Object value = values.get(i);
        String str = "";
        if (value == null) {
            str = "null";
        } else if (type.equals("integer")) {
            Integer n = (Integer) value;
            str = n.toString();
        } else if (type.startsWith("varchar")) {
            str = (String) value;
        } else if (type.startsWith("char")) {
            str = (String) value;
        } else if (type.equals("double")) {
            Double d = (Double) value;
            str = d.toString();
        } else if (type.equals("boolean")) {
            Boolean b = (Boolean) value;
            str = b.toString();
        }
        return str;
    }

}