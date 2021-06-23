package erestaurant;

import java.util.Date;

public class Paid extends AbstractEvent {

    private String status;
    private Date paiddate;
    private Long cookid;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public Date getPaiddate() {
        return paiddate;
    }

    public void setPaiddate(Date paiddate) {
        this.paiddate = paiddate;
    }
    public Long getCookid() {
        return cookid;
    }

    public void setCookid(Long cookid) {
        this.cookid = cookid;
    }
}