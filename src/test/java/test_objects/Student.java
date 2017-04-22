package test_objects;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Mateusz on 07.04.2017.
 */
public class Student {
    public String firstName;
    public String lastName;
    public int age;
    public List<Integer> notes = Arrays.asList(1 ,2 ,3);
    public List<String> faculties = Arrays.asList("ble", "bla");
    public int[] intArray = { 4, 5, 6};
    public String[] stringArray = {"foo", "bar"};
}
