package com.thatzit.kjw.stamptour_kyj_client.more;

/**
 * Created by csc-pc on 2016. 10. 19..
 */

public class UserAchieve {
    private String grede;
    private String checkTime;
    private int grade_no;
    public UserAchieve(String grede, String checkTime, int grade_no) {
        this.grede = grede;
        this.checkTime = checkTime;
        this.grade_no=grade_no;
    }

    public int getGrade_no() {
        return grade_no;
    }

    public String getGrede() {
        return grede;
    }

    public void setGrede(String grede) {
        this.grede = grede;
    }

    public String getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime;
    }
}
