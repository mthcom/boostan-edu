package main.java.Model;

import java.util.ArrayList;

public class CourseOffering {
    private Course course;
    private Semester semester;
    private int groupId;
    private int capacity;
    private ArrayList<Instructor> instructors;
    private DateTime examDate;
    private ArrayList<TimeSlot> classTime;

    public CourseOffering(Course course, Semester semester, int groupId, int capacity, ArrayList<Instructor> instructors,
                          DateTime examDate, ArrayList<TimeSlot> classTime) {
        if (instructors.size() < 1)
            throw new IllegalArgumentException();
        this.course = course;
        this.semester = semester;
        this.groupId = groupId;
        this.capacity = capacity;
        this.instructors = instructors;
        this.examDate = examDate;
        this.classTime = classTime;
    }
}