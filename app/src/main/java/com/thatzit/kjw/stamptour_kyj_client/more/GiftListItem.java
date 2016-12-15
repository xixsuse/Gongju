package com.thatzit.kjw.stamptour_kyj_client.more;

/**
 * Created by csc-pc on 2016. 10. 19..
 */

public class GiftListItem {
    private String grade;
    private int achieve;
    private int state;
    private int grade_no;
    //state code
    // 00 : normal
    // 01 : active
    // 02 : complete

    public GiftListItem(String grade, int achieve, int state,int grade_no) {
        this.grade = grade;
        this.achieve = achieve;
        this.state = state;
        this.grade_no = grade_no;
    }

    public int getGrade_no() {
        return grade_no;
    }

    public void setGrade_no(int grade_no) {
        this.grade_no = grade_no;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public int getAchieve() {
        return achieve;
    }

    public void setAchieve(int achieve) {
        this.achieve = achieve;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
