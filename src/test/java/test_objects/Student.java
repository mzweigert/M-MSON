package test_objects;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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
    public Student[] students;
    public Student student;

    public static Student createRandomStudent(boolean withRecursion){
        Student student = new Student();
        Random random = new Random();
        student.firstName = UUID.randomUUID().toString();
        student.lastName = UUID.randomUUID().toString();
        student.age = random.nextInt(100);

        if(withRecursion){
            student.student = createRandomStudent(false);
            student.students = new Student[5];
            for(int i=0; i< 5; i++) {
                student.students[i] = createRandomStudent(false);
            }
        }
        return student;
    }
}
