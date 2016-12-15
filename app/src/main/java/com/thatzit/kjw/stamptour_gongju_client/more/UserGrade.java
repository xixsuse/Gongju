package com.thatzit.kjw.stamptour_gongju_client.more;

/**
 * Created by csc-pc on 2016. 10. 19..
 */

public class UserGrade {
    private String gradeName;
    private int achieve_count;
    private int grade_no;
    public UserGrade(String gradeName, int achieve_count,int grade_no) {
        this.gradeName = gradeName;
        this.achieve_count = achieve_count;
        this.grade_no = grade_no;
    }

    public int getGrade_no() {
        return grade_no;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public int getAchieve_count() {
        return achieve_count;
    }

    public void setAchieve_count(int achieve_count) {
        this.achieve_count = achieve_count;
    }
}
