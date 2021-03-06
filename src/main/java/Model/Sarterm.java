package main.java.Model;

import java.util.HashMap;

public class Sarterm {
    private Semester semester;
    private Student student;
    private HashMap<String, CourseEnrollment> enrollments;
    private SartermState state;

    public Sarterm(Semester semester, Student student) {
        this.semester = semester;
        this.student = student;
        enrollments = new HashMap<String, CourseEnrollment>();
        state = new RegisteringSartermState();
    }

    public Sarterm(Semester semester, Student student, HashMap<String, CourseEnrollment> enrollments, SartermState state) {
        this(semester, student);
        this.enrollments = enrollments;
        this.state = state;
    }

    public void enrollCourse(String offeringId) {
        if (hasExclusive())
            throw new IllegalArgumentException("Can't have another course alongside an exclusive course");
        CourseOffering courseOffering = CourseOfferingRepository.get(offeringId);
        if (!student.checkPishniazi(courseOffering.getCourse()))
            throw new IllegalArgumentException("Pishniazi not satisfied");
        if (student.getTotalMaximumCredit().isLessThan(
                student.getNumberOfPassedCredits().sum(courseOffering.getCourse().getCredit())
        ))
            throw new IllegalArgumentException("Total max number of credits not satisfied.");
        if (student.getSemesterMaxCredit().isLessThan(
                this.getNumberOfCredits().sum(courseOffering.getCourse().getCredit())
        ))
            throw new IllegalArgumentException("Semester max number of credits not satisfied.");
        if (student.hasPassedCourse(courseOffering.getCourse()) || student.hasTakenCourse(courseOffering.getCourse()))
            throw new IllegalArgumentException("Can't enroll in the same course twice");
        if (classTimeOverlaps())
            throw new IllegalArgumentException("Class times overlap");
        state.addCourse(
                new CourseEnrollment(courseOffering, getPassGrade(), courseOffering.getCourse().isnoEffectOnGPA()),
                enrollments
        );
    }

    public void removeCourse(String offeringId) {
        if (!enrollments.containsKey(offeringId))
            throw new IllegalArgumentException();
        CourseEnrollment removedCourse = enrollments.get(offeringId);
        state.removeCourse(removedCourse, enrollments);
        if (!student.checkPishniazi(removedCourse.getCourseOffering().getCourse())) {
            enrollments.put(offeringId, removedCourse);
            throw new IllegalArgumentException("Hamniazi not satisfied after remove");
        }
    }

    public NumericGrade getPassGrade() {
        return student.getPassGrade();
    }

    public void enterInProgress() {
        state = new InProgressSartermState();
    }

    public void enterRegistering() {
        state = new RegisteringSartermState();
    }

    public void enterFinishd() {
        state = new FinishdSartermState();
    }

    public void enterWithdrawing() {
        state = new WithdrawingSartermState();
    }

    private boolean hasExclusive() {
        for (CourseEnrollment courseEnrollment : enrollments.values()) {
            if (student.isExclusive(courseEnrollment.getCourseOffering().getCourse()))
                return true;
        }
        return false;
    }

    public Credit getNumberOfCredits() {
        Credit result = new Credit(0, 0);
        for (CourseEnrollment courseEnrollment : enrollments.values()) {
            result = result.sum(courseEnrollment.getCourseOffering().getCourse().getCredit());
        }
        return result;
    }

    public int getNumberOfEnrollments() {
        return enrollments.size();
    }

    public boolean finalCheck() {
        if (getNumberOfCredits().isLessThan(student.getSemesterMinimumCredits()))
            return false;
        return true;
    }

    public Credit getGPACredits() {
        Credit result = new Credit(0, 0);
        for (CourseEnrollment courseEnrollment : enrollments.values()) {
            if (courseEnrollment.isCountedAsPassedUnit())
                result = result.sum(courseEnrollment.getCourseOffering().getCourse().getCredit());
        }
        return result;
    }

    public NumericGrade getGPAGrade() {
        NumericGrade result = new NumericGrade(0);
        for (CourseEnrollment courseEnrollment : enrollments.values()) {
            if (courseEnrollment.isEffectiveOnGPA())
                result = result.sum((NumericGrade) courseEnrollment.getGrade());
        }
        return result;
    }

    public boolean hasTakenCourse(Course course) {
        for (CourseEnrollment courseEnrollment : enrollments.values()) {
            if (courseEnrollment.getCourseOffering().getCourse().isEquivalent(course) ||
                    courseEnrollment.getCourseOffering().getCourse().equals(course))
                if (courseEnrollment.isTakenOrPassed())
                    return true;
        }
        return false;
    }

    public boolean hasPassedCourse(Course course) {
        for (CourseEnrollment courseEnrollment : enrollments.values()) {
            if (courseEnrollment.getCourseOffering().getCourse().isEquivalent(course) ||
                    courseEnrollment.getCourseOffering().getCourse().equals(course))
                if (courseEnrollment.isPassed())
                    return true;
        }
        return true;
    }

    public Credit getNumberOfPassedCredits() {
        Credit result = new Credit(0, 0);
        for (CourseEnrollment courseEnrollment : enrollments.values()) {
            if (courseEnrollment.isPassed())
                result = result.sum(courseEnrollment.getCourseOffering().getCourse().getCredit());
        }
        return result;
    }

    private boolean classTimeOverlaps() {
        for (CourseEnrollment courseEnrollment : enrollments.values()) {
            for (CourseEnrollment courseEnrollment1 : enrollments.values()) {
                for (TimeSlot timeSlot : courseEnrollment.getCourseOffering().getClassTime()) {
                    for (TimeSlot timeSlot1 : courseEnrollment1.getCourseOffering().getClassTime()) {
                        if (timeSlot.overlaps(timeSlot1) && !timeSlot.equals(timeSlot1))
                            return true;
                    }
                }
            }
        }
        return false;
    }
}
