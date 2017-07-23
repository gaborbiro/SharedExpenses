package com.gaborbiro.sharedexpenses.api;

import com.gaborbiro.sharedexpenses.SpreadsheetException;
import com.gaborbiro.sharedexpenses.model.BaseItem;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class SheetRowReader<T extends BaseItem> {

    private int[] columnIndex;
    private Class clazz;

    public SheetRowReader(List<Object> header, String[] titles, Class<T> clazz) throws SpreadsheetException {
        columnIndex = new int[titles.length];
        this.clazz = clazz;
        List<String> titleList = Arrays.asList(titles);

        int counter = 0;
        for (Object title : header) {
            int index = titleList.indexOf(title);

            if (index >= 0 && index < columnIndex.length) {
                columnIndex[index] = counter;
            } else {
//                throw new SpreadsheetException("Unknown header column '" + title.toString() + "'");
            }
            counter++;
        }
    }

    public T get(int index, List<Object> row) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object[] parameters = new Object[columnIndex.length + 1];
        Class<?>[] parameterTypes = new Class[columnIndex.length + 1];
        parameters[0] = index;
        parameterTypes[0] = int.class;

        int rowSize = row.size();

        for (int i = 0; i < columnIndex.length; i++) {
            if (rowSize > columnIndex[i]) {
                parameters[i + 1] = row.get(columnIndex[i]).toString();
                parameterTypes[i + 1] = String.class;
            } else {
                parameters[i + 1] = null;
                parameterTypes[i + 1] = String.class;
            }
        }

        return (T) clazz.getConstructor(parameterTypes).newInstance(parameters);
    }
}
